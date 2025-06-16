package ca.bc.gov.educ.api.graduation.config;

import ca.bc.gov.educ.api.graduation.util.EducGraduationApiConstants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private final EducGraduationApiConstants constants;

    public SwaggerConfig(EducGraduationApiConstants constants) {
        this.constants = constants;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("OAuth 2.0 API").version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList("OAUTH2"))
                .schemaRequirement("OAuth2", new SecurityScheme()
                        .type(SecurityScheme.Type.OAUTH2)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .flows(new OAuthFlows()
                                .clientCredentials( new OAuthFlow()
                                        .tokenUrl(constants.getTokenUrl())
                                )));
    }
}