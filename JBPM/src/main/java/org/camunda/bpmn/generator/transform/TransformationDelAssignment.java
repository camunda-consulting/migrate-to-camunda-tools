package org.camunda.bpmn.generator.transform;

import org.camunda.bpmn.generator.process.BpmnDiagramTransport;
import org.camunda.bpmn.generator.process.BpmnTool;
import org.camunda.bpmn.generator.report.Report;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

public class TransformationDelAssignment implements TransformationBpmnInt {

  private int assignmentDeleted = 0;

  @Override
  public String getName() {
    return "DeleteAssignment";
  }

  @Override
  public BpmnDiagramTransport apply(BpmnDiagramTransport bpmnDiagram, Report report) {

    try {
      List<Node> listDataInput = bpmnDiagram.getBpmnTool().getElementsByTagName("dataInputAssociation");
      for (Node dataInput : listDataInput) {
        NodeList listChildInput = dataInput.getChildNodes();
        for (Node assignmentNode : BpmnTool.getList(listChildInput)) {
          if (BpmnTool.equalsNodeName(assignmentNode,"assignment")) {
            dataInput.removeChild(assignmentNode);
            assignmentDeleted++;
          }
        }
      }
    } catch (Exception e) {
      report.error("During FEEL operation ", e);

    }
    return bpmnDiagram;
  }

  @Override
  public String getReportOperations() {
    return "Assignement deleted " + assignmentDeleted;
  }
}
