package org.camunda.bpmn.generator.verify;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpmn.generator.process.BpmnDiagramTransport;
import org.camunda.bpmn.generator.report.Report;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class VerificationRead implements  VerificationInt{

  private String errors="";
  @Override
  public String getName() {
    return "Read";
  }

  @Override
  public boolean isOk(BpmnDiagramTransport diagram, Report report) {

    try {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(diagram.getProcessXml());

      ByteArrayOutputStream output = new ByteArrayOutputStream();
      StreamResult result = new StreamResult(output);
      transformer.transform(source, result);

      // now read the output
      ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

      Bpmn.readModelFromStream(input);
      return true;
    }
    catch(Exception e) {
      errors=e.getMessage()+" "+e.getCause();
      return false;
    }
  }

  @Override
  public String getReportVerification() {
    return errors;
  }
}
