package org.camunda.bpmn.generator.transform;

import org.camunda.bpmn.generator.process.BpmnDiagramTransport;
import org.camunda.bpmn.generator.report.Report;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Check the <bpmndefinition> node and add all missing definition
 */
public class TransformBpmnDefinition implements TransformationBpmnInt {

  private final List<String> addMissingDeclarations = new ArrayList<>();

  public Map<String, String> mandatoryDeclaration = Map.of("xmlns:bpmn", "http://www.omg.org/spec/BPMN/20100524/MODEL",
      "xmlns:bpmndi", "http://www.omg.org/spec/BPMN/20100524/DI", "xmlns:di", "http://www.omg.org/spec/DD/20100524/DI",
      "xmlns:dc", "http://www.omg.org/spec/DD/20100524/DC", "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance",
      "xmlns:camunda", "http://camunda.org/schema/1.0/bpmn");

  @Override
  public String getName() {
    return "BpmnDefinition";
  }

  @Override
  public BpmnDiagramTransport apply(BpmnDiagramTransport diagramBPMN, Report report) {
    NodeList listUserTaskInput = diagramBPMN.getElementsByTagName("definitions");
    if (listUserTaskInput.getLength()==0)
      listUserTaskInput = diagramBPMN.getElementsByTagName("bpmn:definitions");

    if (listUserTaskInput.getLength() != 1) {
      report.error("No bpmn:definition found");
      return diagramBPMN;
    }

    Element bpmnDefinition = (Element) listUserTaskInput.item(0);
    for (Map.Entry<String, String> oneDeclaration : mandatoryDeclaration.entrySet()) {
      String attributValue =bpmnDefinition.getAttribute(oneDeclaration.getKey());
      if ( attributValue == null || attributValue.trim().isEmpty()) {
        bpmnDefinition.setAttribute(oneDeclaration.getKey(), oneDeclaration.getValue());
        addMissingDeclarations.add(oneDeclaration.getKey());
      }
    }
    return diagramBPMN;
  }

  @Override
  public String getReportOperations() {
    return (addMissingDeclarations.isEmpty() ?
        "XML was complete" :
        ("Added " + String.join(", ", addMissingDeclarations)));
  }
}
