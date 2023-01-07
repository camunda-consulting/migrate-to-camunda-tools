package org.camunda.bpmn.generator.transform;

import org.camunda.bpmn.generator.process.BpmnDiagramTransport;
import org.camunda.bpmn.generator.report.Report;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TransformationDelAssignment implements TransformationBpmnInt {

  private int assignmentDeleted = 0;

  @Override
  public String getName() {
    return "DeleteAssignment";
  }

  @Override
  public BpmnDiagramTransport apply(BpmnDiagramTransport diagramBPMN, Report report) {

    try {
      NodeList listDataInput = diagramBPMN.getElementsByTagName("dataInputAssociation");
      for (int i = 0; i < listDataInput.getLength(); i++) {
        Node dataInput = listDataInput.item(i);
        NodeList listChildInput = dataInput.getChildNodes();
        for (int j = 0; j < listChildInput.getLength(); j++) {
          if ("assignment".equals(listChildInput.item(j).getNodeName())) {
            dataInput.removeChild(listChildInput.item(j));
            assignmentDeleted++;
          }
        }
      }
    } catch (Exception e) {
      report.error("During FEEL operation ", e);

    }
    return diagramBPMN;
  }

  @Override
  public String getReportOperations() {
    return "Assignement deleted " + assignmentDeleted;
  }
}
