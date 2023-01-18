package org.camunda.bpmn.generator.transform;

import org.camunda.bpmn.generator.process.BpmnDiagramTransport;
import org.camunda.bpmn.generator.process.BpmnTool;
import org.camunda.bpmn.generator.report.Report;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Change
 * <userTask id="_jbpm-unique-1" name="Returned To Initiator" >
 * <ioSpecification>
 * <dataInput id="_jbpm-unique-1_wfActionInput" name="wfAction" />
 * <dataInput id="_jbpm-unique-1_TaskNameInput" name="TaskName" />
 * <dataInput id="_jbpm-unique-1_NodeNameInput" name="NodeName" />
 * <dataOutput id="_jbpm-unique-1_wfActionOutput" name="wfAction" />
 * <inputSet>
 * <dataInputRefs>_jbpm-unique-1_wfActionInput</dataInputRefs>
 * <dataInputRefs>_jbpm-unique-1_TaskNameInput</dataInputRefs>
 * <dataInputRefs>_jbpm-unique-1_NodeNameInput</dataInputRefs>
 * </inputSet>
 * <outputSet>
 * <dataOutputRefs>_jbpm-unique-1_wfActionOutput</dataOutputRefs>
 * </outputSet>
 * </ioSpecification>
 * <dataInputAssociation>
 * <sourceRef>wfAction</sourceRef>
 * <targetRef>_jbpm-unique-35_wfActionInput</targetRef>
 * </dataInputAssociation>
 * <dataInputAssociation>
 * <targetRef>_jbpm-unique-35_TaskNameInput</targetRef>
 * <assignment>
 * <from xsi:type="tFormalExpression">srInitTask1</from>
 * <to xsi:type="tFormalExpression">_jbpm-unique-35_TaskNameInput</to>
 * </assignment>
 * </dataInputAssociation>
 * <dataInputAssociation>
 * <targetRef>_jbpm-unique-35_NodeNameInput</targetRef>
 * <assignment>
 * <from xsi:type="tFormalExpression">Initiate Request</from>
 * <to xsi:type="tFormalExpression">_jbpm-unique-35_NodeNameInput</to>
 * </assignment>
 * </dataInputAssociation>
 * <dataOutputAssociation>
 * <sourceRef>_jbpm-unique-35_wfActionOutput</sourceRef>
 * <targetRef>wfAction</targetRef>
 * </dataOutputAssociation>
 * to
 * <bpmn:userTask id="_jbpm-unique-35" name="Initiate Request">
 * <bpmn:extensionElements>
 * <camunda:inputOutput>
 * <camunda:inputParameter name="wfAction">${wfAction}</camunda:inputParameter>
 * <camunda:inputParameter name="TaskName" />
 * <camunda:inputParameter name="NodeName" />
 * <camunda:outputParameter name="wfAction">${wfAction}</camunda:outputParameter>
 * </camunda:inputOutput>
 * </bpmn:extensionElements>
 * </bpmn:userTask>
 */
public class TransformUserTaskInput implements TransformationBpmnInt {

  public static final String BPMN_ELEMENT_EXTENSION_ELEMENTS = "bpmn:extensionElements";
  public static final String BPMN_ELEMENT_INPUT_OUTPUT = "camunda:inputOutput";
  public static final String BPMN_ELEMENT_OUTPUT_PARAMETER = "camunda:outputParameter";
  public static final String BPMN_ELEMENT_INPUT_PARAMETER = "camunda:inputParameter";

  private int ioSpecification = 0;

  @Override
  public String getName() {
    return "UserTaskInput";
  }

  @Override
  public BpmnDiagramTransport apply(BpmnDiagramTransport bpmnDiagram, Report report) {
    try {
      List<Node> listUserTaskInput = bpmnDiagram.getBpmnTool().getElementsByTagName("userTask");
      for (Node userTask : listUserTaskInput) {

        Map<String, DataAssociation> inputAssociation = collectDataAssociation(userTask, "dataInputAssociation",
            report);
        Map<String, DataAssociation> outputAssociation = collectDataAssociation(userTask, "dataOutputAssociation",
            report);

        for (Node childUserTask : bpmnDiagram.getBpmnTool().getList(userTask.getChildNodes())) {
          if (BpmnTool.equalsNodeName(childUserTask, "ioSpecification")) {
            ioSpecification++;

            Element extensionElement = bpmnDiagram.getProcessXml().createElement(BPMN_ELEMENT_EXTENSION_ELEMENTS);
            userTask.appendChild(extensionElement);
            Element inputOutputElement = bpmnDiagram.getProcessXml().createElement(BPMN_ELEMENT_INPUT_OUTPUT);
            extensionElement.appendChild(inputOutputElement);
            int numberOfChildren = completeExtension(childUserTask, inputOutputElement, inputAssociation,
                outputAssociation, bpmnDiagram.getProcessXml());

            if (numberOfChildren == 0)
              userTask.removeChild(extensionElement);
          }
        }
        // second pass: delete all nodes
        for (Node childUserTask : bpmnDiagram.getBpmnTool().getList(userTask.getChildNodes())) {
          if (BpmnTool.equalsNodeName(childUserTask, "ioSpecification")) {
            userTask.removeChild(childUserTask);
          }
          if (BpmnTool.equalsNodeName(childUserTask, "dataInputAssociation")) {
            userTask.removeChild(childUserTask);
          }
          if (BpmnTool.equalsNodeName(childUserTask, "dataOutputAssociation")) {
            userTask.removeChild(childUserTask);
          }
        }
      }
    } catch (Exception e) {
      report.error("During UserTaskInput operation ", e);

    }
    return bpmnDiagram;
  }

  /**
   * Search
   * *       <dataInputAssociation>
   * *         <sourceRef>wfAction</sourceRef>
   * *         <targetRef>_jbpm-unique-35_wfActionInput</targetRef>
   * *       </dataInputAssociation>
   * *       <dataInputAssociation>
   * *         <targetRef>_jbpm-unique-35_TaskNameInput</targetRef>
   * *         <assignment>
   * *           <from xsi:type="tFormalExpression">srInitTask1</from>
   * *           <to xsi:type="tFormalExpression">_jbpm-unique-35_TaskNameInput</to>
   * *         </assignment>
   * *       </dataInputAssociation>
   *
   * @param userTaskNode userTask node
   * @param name         node of child to filter
   * @return all data association
   */
  private Map<String, DataAssociation> collectDataAssociation(Node userTaskNode, String name, Report report) {
    Map<String, DataAssociation> mapDataAssociation = new HashMap<>();
    for (Node userTaskChildNode : BpmnTool.getList(userTaskNode.getChildNodes())) {
      if (BpmnTool.equalsNodeName(userTaskChildNode, name)) {
        DataAssociation dataAssociation = new DataAssociation();
        for (Node itemAssociation : BpmnTool.getList(userTaskChildNode.getChildNodes())) {
          if (BpmnTool.equalsNodeName(itemAssociation, "sourceRef")) {
            dataAssociation.sourceRef = itemAssociation.getTextContent();
          }
          if (BpmnTool.equalsNodeName(itemAssociation, "targetRef")) {
            dataAssociation.targetRef = itemAssociation.getTextContent();
          }
          if (BpmnTool.equalsNodeName(itemAssociation, "assignment")) {
            try {
              dataAssociation.fromExpression = BpmnTool.getList(itemAssociation.getChildNodes())
                  .stream()
                  .filter(t -> BpmnTool.equalsNodeName(t, "from"))
                  .map(Node::getTextContent)
                  .findFirst()
                  .get();
            } catch (Exception e) {
              report.error("No child [from] in this element : id=[" + BpmnTool.getAttributName(userTaskNode, "id")
                  + "] dataAssociationI=[" + BpmnTool.getAttributName(itemAssociation, "id"));
            }
          }
        }

        mapDataAssociation.put(dataAssociation.targetRef, dataAssociation);
      }

    }
    return mapDataAssociation;

  }

  /**
   * Complete the extension
   *
   * @param ioSpecificationNode   the ioSpecification node
   * @param extensionElementsNode the extensionElementsNode : new node is created here
   * @param inputAssociation      all inputAssociation, to give a default value is exists
   * @param outputAssociation     all outputAssociation , to give a default value is exists
   * @param document              XML Document to create element from.
   */
  private int completeExtension(Node ioSpecificationNode,
                                Node extensionElementsNode,
                                Map<String, DataAssociation> inputAssociation,
                                Map<String, DataAssociation> outputAssociation,
                                Document document) {
    int numberOfChildren = 0;
    for (Node childSpecification : BpmnTool.getList(ioSpecificationNode.getChildNodes())) {
      Element parameter = null;
      DataAssociation dataAssociation = null;
      if (BpmnTool.equalsNodeName(childSpecification, "dataInput")) {
        //     <dataInput id="_jbpm-unique-1_wfActionInput" name="wfAction" />
        // to
        //     <camunda:inputParameter name="wfAction">${wfAction}</camunda:inputParameter>
        parameter = document.createElement(BPMN_ELEMENT_INPUT_PARAMETER);
        dataAssociation = inputAssociation.get(((Element) childSpecification).getAttribute("id"));
      }
      if (BpmnTool.equalsNodeName(childSpecification, "dataOutput")) {
        parameter = document.createElement(BPMN_ELEMENT_OUTPUT_PARAMETER);
        dataAssociation = outputAssociation.get(((Element) childSpecification).getAttribute("id"));
      }

      if (parameter != null) {
        String name = ((Element) childSpecification).getAttribute("name");
        if (name == null || name.isEmpty())
          continue;
        extensionElementsNode.appendChild(parameter);
        numberOfChildren++;
        parameter.setAttribute("name", name);
        if ("TaskName".equals(name) || "NodeName".equals(name)) {
          // ignore it
        } else if (dataAssociation == null) {
          // ignore it
        } else if (dataAssociation.sourceRef != null) {
          parameter.setTextContent("${" + dataAssociation.sourceRef + "}");
        } else if (dataAssociation.fromExpression != null) {
          parameter.setTextContent(dataAssociation.fromExpression);
        }
      }
    }
    return numberOfChildren;
  }

  @Override
  public String getReportOperations() {
    return "iospecification deleted " + ioSpecification;
  }

  private static class DataAssociation {
    String targetRef;
    String sourceRef;
    String fromExpression;
  }

}
