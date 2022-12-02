/* ******************************************************************** */
/*                                                                      */
/*  transformPositionBPMN                                                */
/*                                                                      */
/*  Repositionne each item to have a nice display                       */
/* Visit https://docs.camunda.org/manual/7.18/user-guide/model-api/bpmn-model-api/
/* ******************************************************************** */
package org.camunda.bpmn.generator.transform.draw;

import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.camunda.bpm.model.bpmn.instance.Collaboration;
import org.camunda.bpm.model.bpmn.instance.Lane;
import org.camunda.bpm.model.bpmn.instance.Participant;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.StartEvent;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Definitions;
import org.camunda.bpm.model.bpmn.instance.LaneSet;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.camunda.bpmn.generator.process.BpmnDiagramToTransform;
import org.camunda.bpmn.generator.report.Report;
import org.camunda.bpmn.generator.transform.TransformationBpmnInt;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

public class TransformationDraw implements TransformationBpmnInt {

  public static final int EVENT_HEIGHT = 36;
  public static final int EVENT_WIDTH = 36;
  public static final int EVENT_OFFSETY = 22;
  public static final int ACTIVITY_HEIGHT = 80;
  public static final int ACTIVITY_WIDTH = 100;

  @Override
  public String getName() {
    return "Draw BPMN diagram";
  }

  // Keep it all context on the diagram
  BpmnModelInstance modelInstance;
  XPath xpath = XPathFactory.newInstance().newXPath();
  Document docXML;
  BpmnPlane plane;

  @Override
  public String getReportOperations() {
    return "redraw";
  }

  @Override
  public BpmnDiagramToTransform apply(BpmnDiagramToTransform diagram, Report report) {

    // Create hash map to map ids in old file node objects with ids in new file
    HashMap<String, Object> idMap = new HashMap<>();

    // Create another hash map for boundary events since they use the same id as the node they are attached to. This will help later when adding and drawing sequence flows.
    HashMap<String, Object> boundaryMap = new HashMap<>();

    // Create another hash map for lanes as text annotations use relative y coordinates
    HashMap<String, Object> laneMap = new HashMap<>();
    try {
      docXML = diagram.getProcessXml();

      // remove the current diagram
      XPathExpression searchRequest = xpath.compile("//*[contains(name(),'bpmndi:BPMNDiagram')]");
      NodeList diagramNodeXmlList = (NodeList) searchRequest.evaluate(docXML, XPathConstants.NODESET);
      for (int i = 0; i < diagramNodeXmlList.getLength(); i++) {
        Element diagramNodeXml = (Element) diagramNodeXmlList.item(i);
        diagramNodeXml.getParentNode().removeChild(diagramNodeXml);
      }

      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      XmlToOutputStream(docXML, byteArrayOutputStream);
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
      int participantIndex=0;


      int baseProcess = 0;
      for (Process process : processes) {
        plane = modelInstance.newInstance(BpmnPlane.class);
        plane.setBpmnElement(process);

        // ---------------------- associate the collaboration
        switch (collaborations.size()) {
        case 0:
          // Search for process instead of a collaboration and set plane bpmn element to the process
          if (processes.size() == 0) {
            throw new Exception("Exception - neither collaboration nor process was found");
          } else {
            plane.setBpmnElement((BaseElement) processes.get(0));
          }
          break;

        case 1:
          plane.setBpmnElement((BaseElement) collaborations.get(0));
          break;

        default:
          throw new Exception("Exception - more than one collaboration in BPMN input which is not allowed");
        }


        DrawShape drawShape = new DrawShape(plane, modelInstance);

        Enveloppe enveloppeProcess = drawProcess(process, baseProcess, report);

        if (participantIndex <= participants.size()) {
          BpmnModelElementInstance element = participants.get(participantIndex);
          drawShape.draw(element,
              0,
              baseProcess,
              enveloppeProcess.height(),
              enveloppeProcess.width(),
              true);
        }
        baseProcess += enveloppeProcess.height()+20;


      }
      bpmnDiagram.setBpmnPlane(plane);
      definitions.addChildElement(bpmnDiagram);

      Bpmn.validateModel(modelInstance);
      // bpmn.doWriteModelToOutputStream
      File outputFile = new File("d:/temp/f.bpmn");
      Bpmn.writeModelToFile(outputFile, modelInstance);
      MyBpmn myBpm = new MyBpmn();
      //

      diagram.setProcessXml(myBpm.getDocument(modelInstance));

    } catch (Exception e) {
      report.error("During DRAW operation ", e);
    }
    return diagram;

  }

  public record Enveloppe(int height, int width ){};

  /**
   * Draw a process
   *
   * @param process
   * @param baseProcess
   * @param report
   * @return
   * @throws Exception
   */
  private Enveloppe drawProcess(Process process, int baseProcess, Report report) throws Exception {
    int baseLane = 0;
    TreeProcess treeProcess = new TreeProcess();
    DrawFlow drawFlow = new DrawFlow(plane, modelInstance);
    DrawShape drawShape = new DrawShape(plane, modelInstance);

    // First, place all elements, don't take care of lane
    ModelElementType startEventType = modelInstance.getModel().getType(StartEvent.class);
    Collection<ModelElementInstance> startEvents = modelInstance.getModelElementsByType(startEventType);


    for (ModelElementInstance startEvent : startEvents) {
      TreeProcess.TreeNode startNode = treeProcess.addNode(treeProcess.getRoot(), startEvent);
      startNode.setSize(EVENT_HEIGHT, EVENT_WIDTH);

      // Explore from the starter and populate the tree
      LinkedList<ModelElementInstance> queue = new LinkedList();
      queue.add(startEvent);
      while (!queue.isEmpty()) {
        ModelElementInstance item = queue.poll();
        report.info(
            "   Detect[" + item.getAttributeValue("id") + "] type[" + item.getElementType().getTypeName() + "]");

        TreeProcess.TreeNode itemNode = treeProcess.getNodeByElement(item);
        XPathExpression searchRequest = xpath.compile("//*[@sourceRef='" + item.getAttributeValue("id") + "']");
        NodeList nextItems = (NodeList) searchRequest.evaluate(docXML, XPathConstants.NODESET);
        for (int i = 0; i < nextItems.getLength(); i++) {
          Element nextItemXml = (Element) nextItems.item(i);
          // the NextItem is a sequenceFlow
          if (nextItemXml.getNodeName().equals("bpmn:sequenceFlow")) {
            ModelElementInstance nextItem = modelInstance.getModelElementById(nextItemXml.getAttribute("targetRef"));
            if (treeProcess.contains(nextItem)) {
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

            if (nextTreeNode.getElement()!=null && nextTreeNode.getWidth() == 0)
              nextTreeNode.setSize(ACTIVITY_HEIGHT, ACTIVITY_WIDTH);
            queue.add(nextItem);
          }
        }
      }

      // Now, run the tree to calculate a X,Y on each node

      processTree(treeProcess.getRoot(), 20, 50+baseProcess);
    }
    // Build a Shape for each item now
    int maxWidth=0;
    int maxHeight=0;
    for (TreeProcess.TreeNode treeNode : treeProcess.getAllElements().values()) {
      if (treeNode.getElement() != null) {
        drawShape.draw( treeNode.getElement(), treeNode.getPosition().x(),
            treeNode.getPosition().y(), treeNode.getHeight(), treeNode.getWidth(), true);
        maxWidth = Math.max(maxWidth, treeNode.getPosition().x()+treeNode.getWidth());
        maxHeight = Math.max(maxHeight, treeNode.getPosition().y()+treeNode.getHeight());
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
    return new Enveloppe(maxHeight + 60, maxWidth+50);

  }

  /**
   * process tree to give a position on each item
   *
   * @param treenode
   * @param baseX
   * @param baseY
   * @return the heigth of the tree
   */
  private int processTree(TreeProcess.TreeNode treenode, int baseX, int baseY) {
    treenode.setPosition(baseX, baseY);

    // Only the root treenode should not have a element
    // Attention, for an event, just move down the position by the oversetY
    if (treenode.isEvent() || treenode.isGateway()) {
      treenode.setPosition(baseX, baseY + EVENT_OFFSETY);
    }

    int childHeight=0;
    for (TreeProcess.TreeNode childNode : treenode.getChildren()) {
      childHeight+= processTree(childNode, baseX + treenode.getWidth() + 50, baseY + childHeight);

    }
    return Math.max(treenode.getHeight() + 30, childHeight);
  }

  private int drawLane(Lane lane, TreeProcess treeProcess, int baseLane) {
    // lane may have lane

    return baseLane + 10;
  }
  // Draw the lane
  // consider only starter from the lane

        /*




      if(participants.size() > 0) {
        for (int i = 0; i < participants.size(); i++) {
          BpmnModelElementInstance element = participants.get(i);
          plane = DrawShape.drawShape(plane, modelInstance, element, 70, 100 + (1000 * i) + (i > 0 ? 100 : 0), 1000, 1530, true);
          poolRefPoints.put(participants.get(i).getId(), new LanePoolReferencePoints(70, 100 + (1000 * i) + (i > 0 ? 100 : 0)));

          // Get process and then the lane sets
          int counter = 0;
          Collection<LaneSet> laneSets = participants.get(i).getProcess().getLaneSets();
          Iterator<LaneSet> iter = laneSets.iterator();
          while (iter.hasNext()) {
            LaneSet ls = iter.next();
            Collection<Lane> lanes = ls.getLanes();
            Iterator<Lane> iterLane = lanes.iterator();
            while (iterLane.hasNext()) {
              Lane lane = iterLane.next();
              element = lane;
              // Get flow nodes in lane and create map entries
              Collection<FlowNode> flowNodes = lane.getFlowNodeRefs();








      // Set collaboration attribute on plane. There should only be one collaboration if there is any
      ArrayList<Collaboration> collaborations = (ArrayList<Collaboration>) modelInstance.getModelElementsByType(Collaboration.class);

      switch (collaborations.size()) {
      case 0:
        // Search for process instead of a collaboration and set plane bpmn element to the process
        ArrayList<Process> processes = (ArrayList<Process>) modelInstance.getModelElementsByType(Process.class);
        if(processes.size() == 0) {
          throw new Exception("Exception - neither collaboration nor process was found");
        } else {
          plane.setBpmnElement((BaseElement) processes.get(0));
        }
        break;

      case 1:
        plane.setBpmnElement((BaseElement) collaborations.get(0));
        break;

      default :
        throw new Exception("Exception - more than one collaboration in BPMN input which is not allowed");
      }

      // Create a map of Pool and Lane reference points in order to draw the correct shapes in the correct lane
      HashMap<String, LanePoolReferencePoints> poolRefPoints = new HashMap<>();
      HashMap<String, LanePoolReferencePoints> laneRefPoints = new HashMap<>();

      // Create a map of elements and their associated lanes if lanes are detected
      HashMap<String, String> laneElementContent = new HashMap<>();

      // Draw pools (aka participants) - need to calculate size based on the number of swimlanes
      ArrayList<Participant> participants = (ArrayList<Participant>) modelInstance.getModelElementsByType(Participant.class);

      // Some vendors do not use pools. If they do, add the pool and any lanes. If they don't, just add lanes (the else portion)
      if(participants.size() > 0) {
        for (int i = 0; i < participants.size(); i++) {
          BpmnModelElementInstance element = participants.get(i);
          plane = DrawShape.drawShape(plane, modelInstance, element, 70, 100 + (1000 * i) + (i > 0 ? 100 : 0), 1000, 1530, true);
          poolRefPoints.put(participants.get(i).getId(), new LanePoolReferencePoints(70, 100 + (1000 * i) + (i > 0 ? 100 : 0)));

          // Get process and then the lane sets
          int counter = 0;
          Collection<LaneSet> laneSets = participants.get(i).getProcess().getLaneSets();
          Iterator<LaneSet> iter = laneSets.iterator();
          while (iter.hasNext()) {
            LaneSet ls = iter.next();
            Collection<Lane> lanes = ls.getLanes();
            Iterator<Lane> iterLane = lanes.iterator();
            while (iterLane.hasNext()) {
              Lane lane = iterLane.next();
              element = lane;
              // Get flow nodes in lane and create map entries
              Collection<FlowNode> flowNodes = lane.getFlowNodeRefs();
              Iterator<FlowNode> iterFn = flowNodes.iterator();
              while(iterFn.hasNext()) {
                FlowNode fn = iterFn.next();
                laneElementContent.put(fn.getId(),lane.getId());
              }
              // Draw lane
              plane = DrawShape.drawShape(plane, modelInstance, element, 100, 100 + (500 * counter) + (i > 0 ? 1100 * i : 0), 500, 1500, true);
              laneRefPoints.put(lane.getId(), new LanePoolReferencePoints(0, 300 * counter ));
              counter++;
            }
          }
        }
      } else { // If no collaboration defined then check for processes, lane sets, and lanes
        int counter = 0;
        ArrayList<Process> processes = (ArrayList<Process>) modelInstance.getModelElementsByType(
            Process.class);
        Iterator<Process> iterProcess = processes.iterator();
        for (Iterator<Process> it = processes.iterator(); it.hasNext(); ) {
          Process process = it.next();
          Collection<LaneSet> laneSets = process.getLaneSets();
          Iterator<LaneSet> iterLaneSets = laneSets.iterator();
          while (iterLaneSets.hasNext()) {
            Collection<Lane> lanes = iterLaneSets.next().getLanes();
            Iterator<Lane> iterLanes = lanes.iterator();
            while (iterLanes.hasNext()) {
              Lane lane = iterLanes.next();
              BpmnModelElementInstance element = lane;
              // Get flow nodes in lane and create map entries
              Collection<FlowNode> flowNodes = lane.getFlowNodeRefs();
              Iterator<FlowNode> iterFn = flowNodes.iterator();
              while(iterFn.hasNext()) {
                FlowNode fn = iterFn.next();
                laneElementContent.put(fn.getId(),lane.getId());
              }
              // Draw lane
              plane = DrawShape.drawShape(plane, modelInstance, element, 100, 100 + (500 * counter), 500, 1500, true);
              laneRefPoints.put(lane.getId(), new LanePoolReferencePoints(0, 500 * counter));
              counter++;
            }
          }
        }
      }

      // Create a hash map of the x and y coordinates of the start and endpoints of each shape to be a reference later on for the sequence flows.
      // As we add shapes we'll use the id of each element as a key and save the coordinates
      HashMap<String, SequenceReferencePoints> refPoints = new HashMap<>();

      // An array for shape source references to place process shapes in relative sequential order
      ArrayList<String> sourceRefs = new ArrayList<String>();
      ArrayList<String> nextSourceRefs = new ArrayList<String>();

      // Objects to search for and save references and coordinates
      xpath = XPathFactory.newInstance().newXPath();

      // Get start events
      searchRequest = xpath.compile("//*[contains(name(),'startEvent')]");
      NodeList eventNodes = (NodeList) searchRequest.evaluate(doc, XPathConstants.NODESET);

      int x = 0;
      // Begin diagram by drawing start events
      for (int i = 0; i < eventNodes.getLength(); i++) {
        Element eElement = (Element) eventNodes.item(i);
        BpmnModelElementInstance element = modelInstance.getModelElementById(eElement.getAttribute("id"));
        double xLane = getLaneXOffset(laneElementContent, laneRefPoints, eElement.getAttribute("id"));
        double yLane = getLaneYOffset(laneElementContent, laneRefPoints, eElement.getAttribute("id"));
        plane = DrawShape.drawShape(plane, modelInstance, element, xLane + 200, yLane + 200 + (200*i), 36, 36, false );
        refPoints.put(eElement.getAttribute("id"), new SequenceReferencePoints(xLane + 200, (yLane + 220 + i * 200),xLane + 236,(yLane + 220 + i * 200)));
        sourceRefs.add(eElement.getAttribute("id"));
      }

      x += 180;

      // Draw next shapes
      while (sourceRefs.size() > 0) {
        // Move over 180 pixels to draw the next set of shapes
        x += 180;
        // y will determine the y axis of shape placement and the set to zero at the start of each run
        int yOffset = 0;

        for (int i = 0; i < sourceRefs.size(); i++) {
          searchRequest = xpath.compile("//*[@sourceRef='" + sourceRefs.get(i) + "']");
          NodeList nextShapes = (NodeList) searchRequest.evaluate(doc, XPathConstants.NODESET);

          for (int y = 0; y < nextShapes.getLength(); y++) {
            Element tElement = (Element) nextShapes.item(y);
            xpath = XPathFactory.newInstance().newXPath();
            searchRequest = xpath.compile("//*[@id='" + tElement.getAttribute("targetRef") + "']");
            NodeList shapes = (NodeList) searchRequest.evaluate(doc, XPathConstants.NODESET);

            for (int z = 0; z < shapes.getLength(); z++) {
              Element sElement = (Element) shapes.item(z);
              if (!refPoints.containsKey(sElement.getAttribute("id"))) {
                nextSourceRefs.add(sElement.getAttribute("id"));

                String type = sElement.getNodeName();

                switch (type) {
                case ("userTask"):
                case ("bpmn:userTask"):
                case ("serviceTask"):
                case ("bpmn:serviceTask"):
                case ("businessRuleTask"):
                case ("bpmn:businessRuleTask"):
                case ("task"):
                case ("bpmn:task"):
                case ("receiveTask"):
                case ("bpmn:receiveTask"):
                case ("sendTask"):
                case ("bpmn:sendTask"):
                case ("scriptTask"):
                case ("bpmn:scriptTask"):
                case ("manualTask"):
                case ("bpmn:manualTask"):
                case ("callActivity"):
                case ("bpmn:callActivity"):
                  BpmnModelElementInstance element = modelInstance.getModelElementById(sElement.getAttribute("id"));
                  double xLane = getLaneXOffset(laneElementContent, laneRefPoints, sElement.getAttribute("id"));
                  double yLane = getLaneYOffset(laneElementContent, laneRefPoints, sElement.getAttribute("id"));
                  plane = DrawShape.drawShape(plane, modelInstance, element, xLane + x, (yLane + 180 + yOffset) + y * 200, 80, 100, false );
                  refPoints.put(sElement.getAttribute("id"), new SequenceReferencePoints(xLane + x, ((220 + yOffset + yLane) + y * 200), (xLane+ x + 100), ((yLane + 220 + yOffset) + y * 200)));

                  // check for boundary events
                  XPathExpression boundaryRequest = xpath.compile("//*[@attachedToRef='" + sElement.getAttribute("id") + "']");
                  NodeList boundaryEvents = (NodeList) boundaryRequest.evaluate(doc, XPathConstants.NODESET);

                  for (int q = 0; q < boundaryEvents.getLength(); q++) {
                    Element bdElement = (Element) boundaryEvents.item(q);
                    element = modelInstance.getModelElementById(bdElement.getAttribute("id"));
                    plane = DrawShape.drawShape(plane, modelInstance, element, xLane + x + q * 40, (yLane + 240 + yOffset) + y * 200, 36, 36, false );
                    refPoints.put(bdElement.getAttribute("id"), new SequenceReferencePoints(0, 0, ((xLane + x + 15) + q * 40), ((yLane + 275 + yOffset) + y * 200)));
                    nextSourceRefs.add(bdElement.getAttribute("id"));
                  }
                  break;

                case ("exclusiveGateway"):
                case ("bpmn:exclusiveGateway"):
                case ("inclusiveGateway"):
                case ("bpmn:inclusiveGateway"):
                case ("parallelGateway"):
                case ("bpmn:parallelGateway"):
                case ("eventBasedGateway"):
                case ("bpmn:eventBasedGateway"):
                  element = modelInstance.getModelElementById(sElement.getAttribute("id"));
                  xLane = getLaneXOffset(laneElementContent, laneRefPoints, sElement.getAttribute("id"));
                  yLane = getLaneYOffset(laneElementContent, laneRefPoints, sElement.getAttribute("id"));
                  plane = DrawShape.drawShape(plane, modelInstance, element, xLane + x, ((yLane + 195 + yOffset) + y * 200), 50, 50, false );
                  refPoints.put(sElement.getAttribute("id"), new SequenceReferencePoints(xLane + x, ((yLane + 220 + yOffset) + y * 200), (xLane + x + 50), ((yLane + 220 + yOffset) + y * 200)));
                  break;

                case ("intermediateThrowEvent"):
                case ("bpmn:intermediateThrowEvent"):
                case ("intermediateCatchEvent"):
                case ("bpmn:intermediateCatchEvent"):
                case ("endEvent"):
                case ("bpmn:endEvent"):
                  element = modelInstance.getModelElementById(sElement.getAttribute("id"));
                  xLane = getLaneXOffset(laneElementContent, laneRefPoints, sElement.getAttribute("id"));
                  yLane = getLaneYOffset(laneElementContent, laneRefPoints, sElement.getAttribute("id"));
                  plane = DrawShape.drawShape(plane, modelInstance, element, xLane + x, ((yLane + 200 + yOffset) + y * 200), 36, 36, false );
                  refPoints.put(sElement.getAttribute("id"), new SequenceReferencePoints(xLane + x, ((yLane + 220 + yOffset) + y * 200), (xLane + x + 36), ((yLane + 220 + yOffset) + y * 200)));
                  break;

                case ("textAnnotation"):
                case ("bpmn:textAnnotation"):
                  element = modelInstance.getModelElementById(sElement.getAttribute("id"));
                  xLane = getLaneXOffset(laneElementContent, laneRefPoints, sElement.getAttribute("id"));
                  yLane = getLaneYOffset(laneElementContent, laneRefPoints, sElement.getAttribute("id"));
                  plane = DrawShape.drawShape(plane, modelInstance, element, xLane + x, ((yLane + 200 + yOffset) + y * 80), 200, 200, false );
                  refPoints.put(sElement.getAttribute("id"), new SequenceReferencePoints(xLane + x, ((yLane + 220 + yOffset) + y * 80), (xLane + x + 36), ((yLane + 220 + yOffset) + y * 80)));
                  break;

                default:
                  System.out.println("Type not found "+type);
                }
              }
            }
          }

          // If there are additional shapes move the y axis by 200 pixels
          yOffset += 200;
        }

        sourceRefs.clear();
        sourceRefs.addAll(nextSourceRefs);
        nextSourceRefs.clear();
      }

      // Find and draw sequence flows now that the shapes have been drawn and the reference points for the sequence flows
      // have been established
      searchRequest = xpath.compile("//*[contains(name(),'sequenceFlow')]");
      NodeList sfNodes = (NodeList) searchRequest.evaluate(doc, XPathConstants.NODESET);

      for (int i = 0; i < sfNodes.getLength(); i++) {
        // TODO - add logic in drawFlow to create 'elbow' waypoints based on the relative xExit, xEntry and yExit, yEntry coordinates
        Element sfElement = (Element) sfNodes.item(i);
        plane = DrawFlow.drawFlow(plane, modelInstance,sfElement, refPoints);
      }

      searchRequest = xpath.compile("//*[contains(name(),'association')]");
      NodeList associationNodes = (NodeList) searchRequest.evaluate(doc, XPathConstants.NODESET);

      for (int i = 0; i < associationNodes.getLength(); i++) {
        Element aElement = (Element) associationNodes.item(i);
        plane = DrawFlow.drawFlow(plane, modelInstance,aElement, refPoints);
      }


  }

*/

  private void XmlToOutputStream(Document document, OutputStream outputStream) throws Exception {
    DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();

    final DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
    final LSSerializer writer = impl.createLSSerializer();

    writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);

    LSOutput output = impl.createLSOutput();
    output.setEncoding("UTF-8");

    output.setByteStream(outputStream);
    writer.write(document, output);

  }


  public class MyBpmn extends Bpmn {

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
