package org.camunda.bpmn.generator.transform;

import org.camunda.bpmn.generator.process.BpmnDiagramTransport;
import org.camunda.bpmn.generator.process.BpmnTool;
import org.camunda.bpmn.generator.report.Report;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class TransformAddBpmnPrefix implements TransformationBpmnInt {
  @Override
  public String getName() {
    return "AddBpmnPrefix";
  }

  int nbReplacements = 0;

  @Override
  public boolean init(Report report) {
    return true;
  }

  @Override
  public BpmnDiagramTransport apply(BpmnDiagramTransport bpmnDiagram, Report report) {

    try {

      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      BpmnTool.xmlToOutputStream(bpmnDiagram.getProcessXml(), byteArrayOutputStream);

      String textXml = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
      for (String artefact : BpmnTool.getListAllArtefacts()) {
        nbReplacements += countOccurrence(textXml, "<" + artefact);

        textXml = textXml.replaceAll("<" + artefact, "<bpmn:" + artefact);
        textXml = textXml.replaceAll("</" + artefact, "</bpmn:" + artefact);
      }

      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(textXml.getBytes(StandardCharsets.UTF_8));
      bpmnDiagram.setProcessXml(BpmnTool.InputStreamToXml(byteArrayInputStream));
      return bpmnDiagram;
/*
      for (String artefact : BpmnTool.getListAllArtefacts()) {

        List<Node> listNodes = bpmnDiagram.getBpmnTool().getElementsByBpmnName(artefact);
        for (Node artefactNode : listNodes) {

          // this is not working, XML complains that we faced a Exception
          bpmnDiagram.getProcessXml().renameNode(artefactNode, null, "bpmn:"+artefact);
          // this is not working too: it add a attribut and do not rename the node
          // bpmnDiagram.getProcessXml().renameNode(artefactNode, "bpmn", artefact);
          nbReplacements++;
        }
      }
      */

    } catch (Exception e) {
      report.error("Can't change name ", e);
    }
    return bpmnDiagram;
  }

  private int countOccurrence(String text, String pattern) {
    int count = 0;
    int indexOf = 0;
    while (indexOf != -1) {
      indexOf = text.indexOf(pattern, indexOf);
      if (indexOf != -1) {
        count++;
        indexOf++;
      }

    }
    return count;
  }

  @Override
  public String getReportOperations() {
    return "artefacts renamed " + nbReplacements;
  }

}
