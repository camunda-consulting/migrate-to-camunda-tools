package org.camunda.bpmn.generator.transform;

import org.camunda.bpmn.generator.process.BpmnDiagramTransport;
import org.camunda.bpmn.generator.report.Report;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
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
  public BpmnDiagramTransport apply(BpmnDiagramTransport diagramBPMN, Report report) {
    try {
      NodeList listUserTaskInput = diagramBPMN.getElementsByTagName("userTask");
      for (Node userTask : getList(listUserTaskInput)) {

        Map<String, DataAssociation> inputAssociation = collectDataAssociation(userTask, "dataInputAssociation");
        Map<String, DataAssociation> outputAssociation = collectDataAssociation(userTask, "dataOutputAssociation");

        for (Node childUserTask : getList(userTask.getChildNodes())) {
          if ("ioSpecification".equals(childUserTask.getNodeName())) {
            ioSpecification++;

            Element extensionElement = diagramBPMN.getProcessXml().createElement(BPMN_ELEMENT_EXTENSION_ELEMENTS);
            userTask.appendChild(extensionElement);
            Element inputOutputElement = diagramBPMN.getProcessXml().createElement(BPMN_ELEMENT_INPUT_OUTPUT);
            extensionElement.appendChild(inputOutputElement);
            completeExtension(childUserTask, inputOutputElement, inputAssociation, outputAssociation,
                diagramBPMN.getProcessXml());
          }
        }
        // second pass: delete all nodes
        for (Node childUserTask : getList(userTask.getChildNodes())) {
          if ("ioSpecification".equals(childUserTask.getNodeName())) {
            userTask.removeChild(childUserTask);
          }
          if ("dataInputAssociation".equals(childUserTask.getNodeName())) {
            userTask.removeChild(childUserTask);
          }
          if ("dataOutputAssociation".equals(childUserTask.getNodeName())) {
            userTask.removeChild(childUserTask);
          }
        }
      }
    } catch (Exception e) {
      report.error("During UserTaskInput operation ", e);

    }
    return diagramBPMN;
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
  private Map<String, DataAssociation> collectDataAssociation(Node userTaskNode, String name) {
    Map<String, DataAssociation> mapDataAssociation = new HashMap<>();
    for (Node userTaskChildNode : getList(userTaskNode.getChildNodes())) {
      if (name.equals(userTaskChildNode.getNodeName())) {
        DataAssociation dataAssociation = new DataAssociation();
        for (Node itemAssociation : getList(userTaskChildNode.getChildNodes())) {
          if ("sourceRef".equals(itemAssociation.getNodeName())) {
            dataAssociation.sourceRef = itemAssociation.getTextContent();
          }
          if ("targetRef".equals(itemAssociation.getNodeName())) {
            dataAssociation.targetRef = itemAssociation.getTextContent();
          }
          if ("assignment".equals(itemAssociation.getNodeName())) {
            dataAssociation.fromExpression = getList(itemAssociation.getChildNodes()).stream()
                .filter(t -> t.getNodeName().equalsIgnoreCase("from"))
                .map(Node::getTextContent)
                .findFirst()
                .get();
          }
        }

        mapDataAssociation.put(dataAssociation.targetRef, dataAssociation);
      }

    }
    return mapDataAssociation;

  }

  /**
   * Complete the extension
   * @param ioSpecificationNode the ioSpecification node
   * @param extensionElementsNode the extensionElementsNode : new node is created here
   * @param inputAssociation all inputAssociation, to give a default value is exists
   * @param outputAssociation all outputAssociation , to give a default value is exists
   * @param document XML Document to create element from.
   */
  private void completeExtension(Node ioSpecificationNode,
                                 Node extensionElementsNode,
                                 Map<String, DataAssociation> inputAssociation,
                                 Map<String, DataAssociation> outputAssociation,
                                 Document document) {
    for (Node childSpecification : getList(ioSpecificationNode.getChildNodes())) {
      Element parameter = null;
      DataAssociation dataAssociation=null;
      if ("dataInput".equals(childSpecification.getNodeName())) {
        //     <dataInput id="_jbpm-unique-1_wfActionInput" name="wfAction" />
        // to
        //     <camunda:inputParameter name="wfAction">${wfAction}</camunda:inputParameter>
        parameter = document.createElement(BPMN_ELEMENT_INPUT_PARAMETER);
        dataAssociation = inputAssociation.get(((Element) childSpecification).getAttribute("id"));
      }
      if ("dataOutput".equals(childSpecification.getNodeName())) {
        parameter = document.createElement(BPMN_ELEMENT_OUTPUT_PARAMETER);
        dataAssociation = outputAssociation.get(((Element) childSpecification).getAttribute("id"));
      }

      if (parameter != null) {
        String name = ((Element) childSpecification).getAttribute("name");
        if (name == null || name.isEmpty())
          continue;
        extensionElementsNode.appendChild(parameter);
        parameter.setAttribute("name", name);
        if ("TaskName".equals(name) || "NodeName".equals(name)) {
          // ignore it
        } else if (dataAssociation==null) {
          // ignore it
        }
        else if(dataAssociation.sourceRef != null) {
          parameter.setTextContent("${" + dataAssociation.sourceRef + "}");
        } else if (dataAssociation.fromExpression != null) {
          parameter.setTextContent(dataAssociation.fromExpression);
        }
      }
    }
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

  /**
   * return the list of node under a List, to simplify the code
   *
   * @param nodeList nodeList
   * @return the list of node under a List<>
   */
  private List<Node> getList(NodeList nodeList) {
    ArrayList<Node> nodeArrayList = new ArrayList<>();
    for (int i = 0; i < nodeList.getLength(); i++)
      nodeArrayList.add(nodeList.item(i));

    return nodeArrayList;
  }
}
