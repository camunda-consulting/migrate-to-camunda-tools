/* ******************************************************************** */
/*                                                                      */
/*  TransformationDraw                                                */
/*                                                                      */
/*  Position each item to have a nice display                       */
/* Visit https://docs.camunda.org/manual/7.18/user-guide/model-api/bpmn-model-api/
/* ******************************************************************** */
package org.camunda.bpmn.generator.transform.draw;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.camunda.bpm.model.bpmn.instance.Collaboration;
import org.camunda.bpm.model.bpmn.instance.Definitions;
import org.camunda.bpm.model.bpmn.instance.Lane;
import org.camunda.bpm.model.bpmn.instance.LaneSet;
import org.camunda.bpm.model.bpmn.instance.Participant;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.camunda.bpmn.generator.process.BpmnDiagramTransport;
import org.camunda.bpmn.generator.process.BpmnTool;
import org.camunda.bpmn.generator.report.Report;
import org.camunda.bpmn.generator.transform.TransformationBpmnInt;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class TransformationDraw implements TransformationBpmnInt {

  public static final int EVENT_HEIGHT = 36;
  public static final int EVENT_WIDTH = 36;
  public static final int EVENT_OFFSETY = 22;
  public static final int ACTIVITY_HEIGHT = 80;
  public static final int ACTIVITY_WIDTH = 100;
  // Keep it all context on the diagram
  BpmnModelInstance modelInstance;
  XPath xpath = XPathFactory.newInstance().newXPath();
  Document docXML;
  BpmnPlane plane;
  int numberOfItems = 0;
  int numberOfStartEvent = 0;

  @Override
  public String getName() {
    return "Draw";
  }

  @Override
  public String getReportOperations() {
    return numberOfItems + " items, " + numberOfStartEvent + " startEvents";
  }

  @Override
  public boolean init(Report report) {
    return true;
  }

  @Override
  public BpmnDiagramTransport apply(BpmnDiagramTransport diagram, Report report) {

    try {
      docXML = diagram.getProcessXml();

      // remove the current diagram

      XPathExpression searchRequest = xpath.compile("//*[contains(name(),'bpmndi:BPMNDiagram')]");
      NodeList diagramNodeXmlList = (NodeList) searchRequest.evaluate(docXML, XPathConstants.NODESET);
      for (int i = 0; i < diagramNodeXmlList.getLength(); i++) {
        Element diagramNodeXml = (Element) diagramNodeXmlList.item(i);
        diagramNodeXml.getParentNode().removeChild(diagramNodeXml);
      }

      // Load the XML in the Camunda API
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      BpmnTool.xmlToOutputStream(docXML, byteArrayOutputStream);
      ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

      modelInstance = Bpmn.readModelFromStream(inputStream);

      // run the different process and build a tree for each item

      Definitions definitions = modelInstance.getDefinitions();

      /* ------------------
      Collaboration. A collaboration contains a participant, and the participant is link to the process
      The collaboration must be linked to the process to draw the border
       */
      List<Collaboration> collaborations = (List<Collaboration>) modelInstance.getModelElementsByType(
          Collaboration.class);
      ArrayList<Process> processes = (ArrayList<Process>) modelInstance.getModelElementsByType(Process.class);
      List<Participant> participants = (List<Participant>) modelInstance.getModelElementsByType(Participant.class);

      // For the diagram, a diagram and a plane element need to be created. The plane is set in a diagram object and the diagram is added as a child element
      BpmnDiagram bpmnDiagram = modelInstance.newInstance(BpmnDiagram.class);
      int participantIndex = 0;

      int baseProcess = 0;
      for (Process process : processes) {
        plane = modelInstance.newInstance(BpmnPlane.class);

        BaseElement baseElementProcess;

        if (collaborations.isEmpty() && processes.isEmpty()) {
          throw new Exception("Exception - neither collaboration nor process was found");
        }
        if (collaborations.size() > 1)
          throw new Exception("Exception - more than one collaboration in BPMN input which is not allowed");

        if (collaborations.size() == 1)
          baseElementProcess = collaborations.get(0);
        else
          baseElementProcess = processes.get(0);

        plane.setBpmnElement(baseElementProcess);

        DrawShape drawShape = new DrawShape(plane, modelInstance);

        Envelope enveloppeProcess = drawProcess(process, baseProcess, report);

        if (participantIndex < participants.size()) {
          BpmnModelElementInstance element = participants.get(participantIndex);
          drawShape.draw(element, 0, baseProcess, enveloppeProcess.height(), enveloppeProcess.width(), true);
        }
        baseProcess += enveloppeProcess.height() + 20;

      }
      bpmnDiagram.setBpmnPlane(plane);
      definitions.addChildElement(bpmnDiagram);

      Bpmn.validateModel(modelInstance);

      MyBpmn myBpm = new MyBpmn();

      diagram.setProcessXml(myBpm.getDocument(modelInstance));

    } catch (Exception e) {
      report.error("During DRAW operation ", e);
    }
    return diagram;

  }

  /**
   * Draw a process
   *
   * @param process      Process to draw
   * @param baseXProcess baseX process
   * @param report       report any error
   * @return the envelope, the size the process used on (height, width)
   * @throws Exception in case of error
   */
  private Envelope drawProcess(Process process, int baseXProcess, Report report) throws Exception {
    int baseLane = 0;
    TreeProcess treeProcess = new TreeProcess();
    DrawFlow drawFlow = new DrawFlow(plane, modelInstance);
    DrawShape drawShape = new DrawShape(plane, modelInstance);

    // First, place all elements, don't take care of lane
    ModelElementType startEventType = modelInstance.getModel().getType(StartEvent.class);
    Collection<ModelElementInstance> startEvents = modelInstance.getModelElementsByType(startEventType);
    numberOfStartEvent += startEvents.size();

    for (ModelElementInstance startEvent : startEvents) {
      TreeProcess.TreeNode startNode = treeProcess.addNode(treeProcess.getRoot(), startEvent);
      startNode.setSize(EVENT_HEIGHT, EVENT_WIDTH);

      // Explore from the starter and populate the tree
      LinkedList<ModelElementInstance> queue = new LinkedList<>();
      queue.add(startEvent);
      while (!queue.isEmpty()) {
        ModelElementInstance item = queue.poll();
        report.debug(
            "   Detect[" + item.getAttributeValue("id") + "] type[" + item.getElementType().getTypeName() + "]");
        numberOfItems++;

        TreeProcess.TreeNode itemNode = treeProcess.getNodeByElement(item);
        XPathExpression searchRequest = xpath.compile("//*[@sourceRef='" + item.getAttributeValue("id") + "']");
        NodeList nextItems = (NodeList) searchRequest.evaluate(docXML, XPathConstants.NODESET);
        for (int i = 0; i < nextItems.getLength(); i++) {
          Element nextItemXml = (Element) nextItems.item(i);
          // the NextItem is a sequenceFlow
          if (nextItemXml.getNodeName().contains("sequenceFlow")) {
            ModelElementInstance nextItem = modelInstance.getModelElementById(nextItemXml.getAttribute("targetRef"));
            if (treeProcess.contains(nextItem)) {

              treeProcess.addDependencie(itemNode, treeProcess.getNodeByElement(nextItem));
              continue;
            }
            // add in the tree
            TreeProcess.TreeNode nextTreeNode = treeProcess.addNode(itemNode, nextItem);
            if (nextTreeNode.isTask()) {
              nextTreeNode.setSize(ACTIVITY_HEIGHT, ACTIVITY_WIDTH);
            }
            if (nextTreeNode.isEvent() || nextTreeNode.isGateway()) {
              nextTreeNode.setSize(EVENT_HEIGHT, EVENT_WIDTH);
            }

            if (nextTreeNode.getElement() != null && nextTreeNode.getWidth() == 0)
              nextTreeNode.setSize(ACTIVITY_HEIGHT, ACTIVITY_WIDTH);
            queue.add(nextItem);
          }
        }
      }

      // Now, run the tree to calculate a X,Y on each node
      // limit on 3 loop. We don't want to indefinitely move item, because the alogorithm detect the parent dependencies in the tree, and it should use a
      // graph instead. So, we detect some dependencies which may not be a real dependency and move again the graph.
      // Example of dependency not correctly detected:
      // A -> B -> C -> D -> E -> F
      //      B -> G -> H -> E
      // and then F loop back to H. But if the tree is build by A,B,C,D,E,F, then H is not detected as a parent, but he is actually.
      int count = 3;
      CalculatePositionTreeResult positionResult;
      do {
        positionResult = calculatePositionTree(treeProcess.getRoot(), 20, 50 + baseXProcess);
        count--;
      } while (count > 0 && positionResult.changeDetected);

    }
    // Build a Shape for each item now
    int maxWidth = 0;
    int maxHeight = 0;
    for (TreeProcess.TreeNode treeNode : treeProcess.getAllElements().values()) {
      if (treeNode.getElement() != null) {
        drawShape.draw(treeNode.getElement(), treeNode.getPosition().x(), treeNode.getPosition().y(),
            treeNode.getHeight(), treeNode.getWidth(), true);
        maxWidth = Math.max(maxWidth, treeNode.getPosition().x() + treeNode.getWidth());
        maxHeight = Math.max(maxHeight, treeNode.getPosition().y() + treeNode.getHeight());
      }
    }

    XPathExpression searchRequest = xpath.compile("//*[contains(name(),'sequenceFlow')]");
    NodeList sfNodes = (NodeList) searchRequest.evaluate(docXML, XPathConstants.NODESET);

    for (int i = 0; i < sfNodes.getLength(); i++) {
      Element sfElement = (Element) sfNodes.item(i);
      drawFlow.draw(sfElement, treeProcess);
    }

    // Lanes impact the position,

    for (LaneSet lane : process.getLaneSets()) {
      int baseSubLane = 0;
      for (Lane subLane : lane.getLanes()) {
        int heighSubLane = drawLane(subLane, treeProcess, baseSubLane + baseLane);
        baseSubLane += heighSubLane;
      }
      baseLane += baseSubLane;
    }
    return new Envelope(maxHeight + 60, maxWidth + 50);

  }

  /**
   * process tree to give a position on each item
   * <p>
   * Attention, the "changedetected" can be true and item are moved when we don't want to.
   * The algorithm detect the parent dependencies in the tree, and it should use a
   * graph instead. So, we detect some dependencies which may not be a real dependency and move again the graph.
   * Example of dependency not correctly detected:
   * A -> B -> C -> D -> E -> F
   * B -> G -> H -> E
   * and then F loop back to H. But if the tree is build by A,B,C,D,E,F, then H is not detected as a parent, but he is actually.
   *
   * @param treenode tree node to start the calculation (this is a recursive call)
   * @param baseX    base X to place the item. Due to dependency, the item may be place AFTER this X
   * @param baseY    base Y to place the item. Due to dependency, the item may be place AFTER this X
   * @return a record to indicate the height and if something is recalculated
   */
  private CalculatePositionTreeResult calculatePositionTree(TreeProcess.TreeNode treenode, int baseX, int baseY) {
    boolean positionChanged = false;
    // parent ask me top be place at baseX, baseY : but maybe one dependency is AFTER this position?
    // Then I need to adjust this calculation to be AFTER that.
    TreeProcess.TreeNode.Coordinate currentPosition = treenode.getPosition();
    int itemBaseX = baseX;
    int itemBaseY = baseY;
    // Attention, for an event, just move down the position by the oversetY
    if (treenode.isEvent() || treenode.isGateway()) {
      itemBaseY = baseY + EVENT_OFFSETY;
    }

    if (currentPosition != null) {
      itemBaseX = Math.max(itemBaseX, currentPosition.x());
      itemBaseY = Math.max(itemBaseY, currentPosition.y());
    }
    for (TreeProcess.TreeNode parentByDependencie : treenode.getDependencies()) {
      TreeProcess.TreeNode.Coordinate parentPosition = parentByDependencie.getPosition();
      if (parentPosition != null && parentPosition.x() > itemBaseX)
        itemBaseX = parentPosition.x() + parentByDependencie.getWidth() + 50;
      // we don't want to change the Y, only the X
    }

    // is the position changed?
    if (currentPosition == null || currentPosition.x() != itemBaseX || currentPosition.y() != itemBaseY)
      positionChanged = true;
    treenode.setPosition(itemBaseX, itemBaseY);

    int childHeight = 0;
    for (TreeProcess.TreeNode childNode : treenode.getChildren()) {

      CalculatePositionTreeResult childPositionResult = calculatePositionTree(childNode,
          baseX + treenode.getWidth() + 50, baseY + childHeight);
      childHeight += childPositionResult.heigth();
      if (childPositionResult.changeDetected)
        positionChanged = true;

    }
    return new CalculatePositionTreeResult(Math.max(treenode.getHeight() + 30, childHeight), positionChanged);
  }

  private int drawLane(Lane lane, TreeProcess treeProcess, int baseLane) {
    // lane may have lane

    return baseLane + 10;
  }

  public record Envelope(int height, int width) {
  }

  public record CalculatePositionTreeResult(int heigth, boolean changeDetected) {
  }

  public static class MyBpmn extends Bpmn {

    public Document getDocument(BpmnModelInstance modelInstance) throws Exception {

      ByteArrayOutputStream containerOutputStream = new ByteArrayOutputStream();
      writeModelToStream(containerOutputStream, modelInstance);

      ByteArrayInputStream inStream = new ByteArrayInputStream(containerOutputStream.toByteArray());

      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      return dBuilder.parse(inStream);

    }
  }

}
