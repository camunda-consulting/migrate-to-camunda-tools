package org.camunda.bpmn.generator.process;

import org.camunda.bpmn.generator.report.Report;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class BpmnTool {

  private final BpmnDiagramTransport bpmnDiagramTransport;
  private final Report report;

  protected BpmnTool(BpmnDiagramTransport bpmnDiagramTransport, Report report) {
    this.bpmnDiagramTransport = bpmnDiagramTransport;
    this.report = report;
  }

  /**
   * Return all SequenceFlow
   *
   * @return NodesList of sequenceFlow
   */
  public NodeList getSequenceFlow() {
    // search all Sequence flow
    NodeList nodes = bpmnDiagramTransport.getProcessXml().getElementsByTagName("sequenceFlow");
    report.debug("DiagramBPMN.getSequenceFlow(): Found " + nodes.getLength());
    return nodes;
  }



  public List<Node> getElementsByTagName(String tagName) {
    // search all Sequence flow
    NodeList nodes = bpmnDiagramTransport.getProcessXml().getElementsByTagName(tagName);
    report.debug("DiagramBPMN.getElementByTagName(" + tagName + "): Found " + nodes.getLength());
    return getList(nodes);
  }

  /**
   * Return a list of bpmn elements by its name,
   *
   * @param bpmnName    search by name
   * @return list of nodes for the bpmn name
   */
  public List<Node> getElementsByBpmnName(String bpmnName) {
    // add bpmn prefix
    return getElementsByBpmnName(bpmnName, List.of("bpmn"));
  }

  /**
   * Return a list of bpmn elements by its name, and a list of prefix (like bpmn:, ns5: )
   * @param bpmnName    search by name
   * @param listPrefix lis to prefix to search. Attention, list must be complete (don't forget bpmn)
   * @return list of nodes for the bpmn name
   */
  public List<Node> getElementsByBpmnName(String bpmnName, List<String> listPrefix) {
    List<Node> listNodes = new ArrayList<>();
    List<String> listPrefixIteration = new ArrayList<>(listPrefix);
    listPrefixIteration.add("");
    for (String prefix : listPrefixIteration) {
      listNodes.addAll(getElementsByTagName((prefix.isEmpty() ? "" : prefix + ":") + bpmnName));
    } return listNodes;

  }

  public static List<Node> getBpmnChildren(Node node, String filterBpmnName) {
    List<Node> listNodes = new ArrayList<>();
    for (Node child : getList(node.getChildNodes())) {
      if (BpmnTool.equalsNodeName(child, filterBpmnName))
        listNodes.add(child);
    }
    return listNodes;
  }

  public final static List<String> listOfStartEvents = List.of("startEvent");
  public final static List<String> listOfGateways = List.of("exclusiveGateway", "inclusiveGateway", "parallelGateway",
      "eventBasedGateway");

  public final static List<String> listOfTasks = List.of("receiveTask", "receiveTask", "task", "sendTask", "userTask",
      "manualTask", "businessRuleTask", "serviceTask", "scriptTask", "callActivity", "subProcess");

  public final static List<String> listOfEndEvents = List.of("endEvent");

  public final static List<String> listOfIntermediateEvents = List.of("intermediateCatchEvent", "intermediateThrowEvent");

  public final static List<String> listOfBoundaryEvents = List.of("boundaryEvent");

  public final static List<String> getListAllArtefacts() {
    List<String> listArtefacts = new ArrayList<>();
    listArtefacts.addAll(listOfStartEvents);
    listArtefacts.addAll(listOfGateways);
    listArtefacts.addAll(listOfTasks);
    listArtefacts.addAll(listOfEndEvents);
    listArtefacts.addAll(listOfIntermediateEvents);
    listArtefacts.addAll(listOfBoundaryEvents);
    return listArtefacts;

  }

  /**
   * XML Function
   */


  /**
   * return the list of node under a List, to simplify the code
   *
   * @param nodeList nodeList
   * @return the list of node under a List<>
   */
  public static List<Node> getList(NodeList nodeList) {
    ArrayList<Node> nodeArrayList = new ArrayList<>();
    for (int i = 0; i < nodeList.getLength(); i++)
      nodeArrayList.add(nodeList.item(i));

    return nodeArrayList;
  }

  public static String getAttributName(Node element, String attibutName) {
    if (element instanceof Element) {
      return ((Element) element).getAttribute(attibutName);
    }
    return null;
  }

  public static boolean equalsNodeName(Node element, String nodeNameCompare) {
    if (element.getNodeName().equals(nodeNameCompare))
      return true;
    String nodeName = element.getNodeName();
    if (nodeName.lastIndexOf(":")!=-1)
      nodeName= nodeName.substring(nodeName.lastIndexOf(":")+1);
    return nodeName.equals(nodeNameCompare);
  }
}
