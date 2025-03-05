package ca.bc.gov.educ.api.graduation.service;

import org.apache.commons.codec.binary.Base64;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BaseReportService {


  protected String getEncodedPdfFromBytes(byte[] bytesSAR) {
    byte[] encoded = Base64.encodeBase64(bytesSAR);
    return new String(encoded, StandardCharsets.US_ASCII);
  }

  protected byte[] mergeDocuments(List<InputStream> sources) throws IOException {
    ByteArrayOutputStream tempOutStream = new ByteArrayOutputStream();
    PDFMergerUtility mergedDoc = new PDFMergerUtility();
    mergedDoc.setDestinationStream(tempOutStream);
    mergedDoc.addSources(sources);
    mergedDoc.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
    return new ByteArrayInputStream(tempOutStream.toByteArray()).readAllBytes();
  }
}
