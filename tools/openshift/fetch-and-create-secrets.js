const axios = require('axios');
const fs = require('fs');
const path = require('path');
const https = require('https');

const keycloakUrl = process.env.KEYCLOAK_URL;
const realm = process.env.KEYCLOAK_REALM;
const openshiftApi = process.env.OPENSHIFT_SERVER;
const gradNamespace = `${process.env.GRAD_NAMESPACE}-${process.env.TARGET_ENV}`;
const openshiftNamespace = process.env.OPENSHIFT_NAMESPACE;
const openshiftToken = process.env.OPENSHIFT_TOKEN;

const config = JSON.parse(fs.readFileSync(path.resolve(__dirname, 'clients.json'), 'utf8'));
const httpsAgent = new https.Agent({ rejectUnauthorized: false }); // for self-signed certs

async function getOpenShiftSecret(openshiftApi, openshiftToken, namespace, secretName) {
  const url = `${openshiftApi}/api/v1/namespaces/${namespace}/secrets/${secretName}`;

  try {
    const resp = await axios.get(url, {
      headers: { Authorization: `Bearer ${openshiftToken}` },
      httpsAgent
    });

    const encodedData = resp.data.data;
    const decodedData = {};

    for (const [key, value] of Object.entries(encodedData)) {
      decodedData[key] = Buffer.from(value, 'base64').toString('utf-8');
    }

    return decodedData;
  } catch (err) {
    throw new Error(`Failed to retrieve secret "${secretName}": ${err.response?.data?.message || err.message}`);
  }
}

async function getAccessToken({username, password}) {
  const url = `${keycloakUrl}/auth/realms/${realm}/protocol/openid-connect/token`;
  const params = new URLSearchParams();
  params.append('grant_type', 'password');
  params.append('client_id', 'admin-cli');
  params.append('username', username);
  params.append('password', password);

  const response = await axios.post(url, params);
  return response.data.access_token;
}

async function getClientCredentials(token, clientId) {
  const headers = { Authorization: `Bearer ${token}` };
  const searchUrl = `${keycloakUrl}/auth/admin/realms/${realm}/clients?clientId=${encodeURIComponent(clientId)}`;
  const clientResp = await axios.get(searchUrl, { headers });

  if (!clientResp.data.length) throw new Error(`Client "${clientId}" not found`);

  const client = clientResp.data[0];
  const secretUrl = `${keycloakUrl}/auth/admin/realms/${realm}/clients/${client.id}/client-secret`;
  const secretResp = await axios.get(secretUrl, { headers });

  return {
    clientId: client.clientId,
    secret: secretResp.data.value
  };
}

async function createOpenshiftSecret({ clientId, secret }) {
  const url = `${openshiftApi}/api/v1/namespaces/${openshiftNamespace}/secrets`;
  const headers = {
    Authorization: `Bearer ${openshiftToken}`,
    'Content-Type': 'application/json'
  };

  const secretName = `${clientId}-secret`;

  const payload = {
    apiVersion: 'v1',
    kind: 'Secret',
    metadata: {
      name: secretName
    },
    type: 'Opaque',
    data: {
      [`${clientId}-NAME`.toUpperCase().replaceAll('-', '_')]: Buffer.from(clientId).toString('base64'),
      [`${clientId}-SECRET`.toUpperCase().replaceAll('-', '_')]: Buffer.from(secret).toString('base64')
    }
  };

  try {
    await axios.post(url, payload, { headers });
    console.log(`✅ Secret "${secretName}" created.`);
  } catch (err) {
    if (err.response?.status === 409) {
      console.log(`🔁 Secret "${secretName}" already exists. Replacing...`);
      await axios.put(`${url}/${secretName}`, payload, { headers });
    } else {
      console.error(`❌ Failed to create secret for "${clientId}":`, err.message);
    }
  }
}

(async () => {
  try {
    const kcCredentials = await getOpenShiftSecret(openshiftApi, openshiftToken, gradNamespace, 'grad-kc-admin');
    const kcToken = await getAccessToken(kcCredentials);

    for (const clientId of config.clients) {
      console.log(`🔍 Fetching secret for "${clientId}"...`);
      const creds = await getClientCredentials(kcToken, clientId);
      await createOpenshiftSecret(creds);
    }

    console.log('🎉 All secrets processed.');
  } catch (err) {
    console.error('❌ Error:', err.response?.data || err.message);
    process.exit(1);
  }
})();