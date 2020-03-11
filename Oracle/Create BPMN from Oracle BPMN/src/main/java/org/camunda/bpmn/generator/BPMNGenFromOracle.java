package org.camunda.bpmn.generator;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

public class BPMNGenFromOracle {
    public static void main(String[] args) throws Exception {
        //try {

            // Read and parse file
            // Need to pass in two args - arg[0] is the input file and arg[1] is the output file
            if (args.length < 2) {
                System.out.println("Two arguments are required for this BPMN from Oracle program. One for the input file followed by one for the output file.");
                return;
            }

            // Read in Oracle BPMN file
            File file = new File(args[0]);

            // Create hash map to map ids in old file node objects with ids in new file
            HashMap< String, Object > idMap = new HashMap<>();

            // Create hash of flow nodes for drawing of sequence flows later
            HashMap< String, Object > flowNodesMap = new HashMap<>();

            // Create hash map of BPMN elements to search for
            HashMap< String, Object > bpmnElementsMap = new HashMap<>();
            BPMNElement bpmnE = new BPMNElement(ServiceTask.class, new Double(80), new Double(100));
            bpmnElementsMap.put("serviceTask", bpmnE);

            bpmnE = new BPMNElement(UserTask.class, new Double(80), new Double(100));
            bpmnElementsMap.put("userTask", bpmnE);

            bpmnE = new BPMNElement(ScriptTask.class, new Double(80), new Double(100));
            bpmnElementsMap.put("scriptTask", bpmnE);

            bpmnE = new BPMNElement(CallActivity.class, new Double(80), new Double(100));
            bpmnElementsMap.put("callActivity", bpmnE);

            bpmnE = new BPMNElement(StartEvent.class, new Double(36), new Double(36));
            bpmnElementsMap.put("startEvent", bpmnE);

            bpmnE = new BPMNElement(EndEvent.class, new Double(36), new Double(36));
            bpmnElementsMap.put("endEvent", bpmnE);

            bpmnE = new BPMNElement(ExclusiveGateway.class, new Double(50), new Double(50));
            bpmnElementsMap.put("exclusiveGateway", bpmnE);

            bpmnE = new BPMNElement(InclusiveGateway.class, new Double(50), new Double(50));
            bpmnElementsMap.put("inclusiveGateway", bpmnE);

            bpmnE = new BPMNElement(ParallelGateway.class, new Double(50), new Double(50));
            bpmnElementsMap.put("parallelGateway", bpmnE);

            bpmnE = new BPMNElement(EventBasedGateway.class, new Double(50), new Double(50));
            bpmnElementsMap.put("eventBasedGateway", bpmnE);

            bpmnE = new BPMNElement(ComplexGateway.class, new Double(50), new Double(50));
            bpmnElementsMap.put("complexGateway", bpmnE);

            bpmnE = new BPMNElement(IntermediateCatchEvent.class, new Double(36), new Double(36));
            bpmnElementsMap.put("intermediateCatchEvent", bpmnE);

            //bpmnE = new BPMNElement(BoundaryEvent.class, new Double(36), new Double(36));
            //bpmnElementsMap.put("boundaryEvent", bpmnE);

            // Create hash map of Event Definitions to search for
            HashMap< String, Object > eventDefinitionsMap = new HashMap<>();
            DefinedEvent de = new DefinedEvent(ErrorEventDefinition.class);
            eventDefinitionsMap.put("errorEventDefinition", de);
            de = new DefinedEvent(SignalEventDefinition.class);
            eventDefinitionsMap.put("signalEventDefinition", de);
            de = new DefinedEvent(MessageEventDefinition.class);
            eventDefinitionsMap.put("messageEventDefinition", de);
            de = new DefinedEvent(EscalationEventDefinition.class);
            eventDefinitionsMap.put("escalationEventDefinition", de);
            de = new DefinedEvent(ConditionalEventDefinition.class);
            eventDefinitionsMap.put("conditionalEventDefinition", de);
            de = new DefinedEvent(TimerEventDefinition.class);
            eventDefinitionsMap.put("timerEventDefinition", de);

            // Read document in preparation for Xpath searches
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);

            doc.getDocumentElement().normalize();

            // Create BPMN model using Camunda Model APIs
            BpmnModelInstance modelInstance = Bpmn.createEmptyModel();

            Definitions definitions = modelInstance.newInstance(Definitions.class);
            definitions.setTargetNamespace("http://camunda.org/examples");
            modelInstance.setDefinitions(definitions);

            // For the diagram, a diagram and a plane element needs to be created. The plane is set in a diagram object and the diagram is added as a child element
            BpmnDiagram bpmnDiagram = modelInstance.newInstance(BpmnDiagram.class);
            BpmnPlane plane = modelInstance.newInstance(BpmnPlane.class);

            bpmnDiagram.setBpmnPlane(plane);
            definitions.addChildElement(bpmnDiagram);

            Process process = modelInstance.newInstance(Process.class);
            process.setExecutable(true); // Want to make sure it is executable by default in Modeler

            // Get pool, laneset, and lane information and add to process
            // Look for processes.
            NodeList poolList = doc.getElementsByTagName("process");
            XPathExpression searchRequest = null;
            XPath xpath = XPathFactory.newInstance().newXPath();

            for(int i=0; i < poolList.getLength(); i++) {
                Element poolElement = (Element) poolList.item(i);

                Collaboration collab = modelInstance.newInstance(Collaboration.class);

                Participant participant = modelInstance.newInstance(Participant.class);
                participant.setName(poolElement.getAttribute("name"));

                definitions.addChildElement(collab);
                definitions.addChildElement(process);

                collab.addChildElement(participant);
                participant.setProcess(process);

                plane.setBpmnElement((BaseElement) collab);

                //Element sizeElement = (Element) sizeList.item(0);
                Double width = new Double(0);
                Double height = new Double(0);

                // draw pool in diagram. If there are multiple pools the default space between is set at 2000 pixels. May need to revisit later
                //plane = DrawShape.drawShape(plane, modelInstance, participant, 0, 2000*i, height, width, true);

                LaneSet laneset = modelInstance.newInstance(LaneSet.class);

                NodeList laneSetList = poolElement.getElementsByTagName("laneSet");

                int poolHeight = 0;

                if(laneSetList.getLength() > 0) { // Oracle requires at least 1 lane set so length should at least be 1
                    process.addChildElement(laneset);
                    int y = 0; // y coordinate for lane
                    for (int j = 0; j < laneSetList.getLength(); j++) {
                        Element laneSetElement = (Element) laneSetList.item(j);
                        NodeList laneList = laneSetElement.getElementsByTagName("lane");

                        for (int k = 0; k < laneList.getLength(); k++) {
                            Element laneElement = (Element) laneList.item(k);
                            Lane lane = modelInstance.newInstance(Lane.class);
                            lane.setName(laneElement.getAttribute("name"));

                            searchRequest = xpath.compile("./*[local-name() = 'extensionElements']/*[local-name() = 'OracleExtensions']/*[local-name() = 'GraphicsAttributes']/*[local-name() = 'Position']");
                            NodeList positionNodeList = (NodeList) searchRequest.evaluate(laneElement, XPathConstants.NODESET);

                            Element positionElement = (Element) positionNodeList.item(0);
                            y = Integer.parseInt(positionElement.getAttribute("x"));

                            searchRequest = xpath.compile("//*[local-name() = 'extensionElements']/*[local-name() = 'OracleExtensions']/*[local-name() = 'GraphicsAttributes']/*[local-name() = 'Size']");
                            NodeList sizeNodeList = (NodeList) searchRequest.evaluate(doc, XPathConstants.NODESET);

                            Element sizeElement = (Element) sizeNodeList.item(k);
                            height = new Double(sizeElement.getAttribute("width"));

                            laneset.addChildElement(lane);
                            plane = DrawShape.drawShape(plane, modelInstance, lane, 30, y, height, 5970, true, false);

                            poolHeight = poolHeight + height.intValue();
                        }

                    }
                    plane = DrawShape.drawShape(plane, modelInstance, participant, 0, 2000 * i, poolHeight, 6000, true, false);

                }

                NodeList subProcessList = doc.getElementsByTagName("subProcess");
                for(int j = 0; j < subProcessList.getLength(); j++){
                    Element subProcessElement = (Element) subProcessList.item(j);
                    searchRequest = xpath.compile("./*[local-name() = 'extensionElements']/*[local-name() = 'OracleExtensions']/*[local-name() = 'GraphicsAttributes']/*[local-name() = 'Position']");
                    NodeList positionList = (NodeList) searchRequest.evaluate(subProcessElement, XPathConstants.NODESET);
                    Element positionElement = (Element) positionList.item(0);

                    searchRequest = xpath.compile("./*[local-name() = 'extensionElements']/*[local-name() = 'OracleExtensions']/*[local-name() = 'GraphicsAttributes']/*[local-name() = 'Size']");
                    NodeList sizeList = (NodeList) searchRequest.evaluate(subProcessElement, XPathConstants.NODESET);
                    Element sizeElement = (Element) sizeList.item(0);

                    SubProcess subProcess = modelInstance.newInstance(SubProcess.class);
                    subProcess.setName(subProcessElement.getAttribute("name"));
                    process.addChildElement(subProcess);

                    // the x,y coordinates in the Oracle extension appear to mean the center of the BPMN object, not the upper left hand corner.
                    // Have to get height and width and offset those values
                    Double x = new Double(positionElement.getAttribute("x"));
                    Double y = new Double(positionElement.getAttribute("y"));
                    height = new Double(sizeElement.getAttribute("height"));
                    width = new Double(sizeElement.getAttribute("width"));
                    x -= width/2;
                    y -= (height/2);

                    // check for boundary events
                    XPathExpression boundaryRequest = xpath.compile("//*[@attachedToRef='" + subProcessElement.getAttribute("id") + "']");
                    NodeList boundaryEvents = (NodeList) boundaryRequest.evaluate(doc, XPathConstants.NODESET);
                    for (int q = 0; q < boundaryEvents.getLength(); q++) {
                        Element boundaryElement = (Element) boundaryEvents.item(q);
                        BoundaryEvent bde = modelInstance.newInstance(BoundaryEvent.class);
                        bde.setName(boundaryElement.getAttribute("name"));
                        bde.setAttachedTo(subProcess);
                        XPathExpression boundaryEventTypeSearch = xpath.compile("./*[contains(local-name(), 'EventDefinition')]");

                        NodeList boundaryEventTypeList = (NodeList) boundaryEventTypeSearch.evaluate(boundaryElement, XPathConstants.NODESET);
                        if(boundaryEventTypeList.getLength() == 1) {  // There is an event definition found, should only be one
                            Element boundaryEventTypeElement = (Element) boundaryEventTypeList.item(0);
                            DefinedEvent eventType = (DefinedEvent) eventDefinitionsMap.get(boundaryEventTypeElement.getTagName());
                            EventDefinition ed = (EventDefinition) modelInstance.newInstance(eventType.getType());
                            bde.addChildElement(ed);
                        }

                        process.addChildElement(bde);

                        XPathExpression boundaryPositionSearch = xpath.compile("./*[local-name() = 'extensionElements']/*[local-name() = 'OracleExtensions']/*[local-name() = 'GraphicsAttributes']/*[local-name() = 'Position']");

                        NodeList boundaryPositionList = (NodeList) boundaryPositionSearch.evaluate(boundaryElement, XPathConstants.NODESET);
                        Element boundaryPositionElement = (Element) boundaryPositionList.item(0);

                        Double bx = new Double(x + (width/2));
                        Double by = y - 18;

                        FlowNodeInfo fni = new FlowNodeInfo(bde.getId(),new Double(boundaryPositionElement.getAttribute("x")), new Double(boundaryPositionElement.getAttribute("y")), bx, by, boundaryElement.getTagName(), new Double(0), new Double(0));
                        flowNodesMap.put(boundaryElement.getAttribute("id"), fni);

                        plane = DrawShape.drawShape(plane, modelInstance, bde, bx, by, 36, 36, true, false);
                    }

                    FlowNodeInfo fni = new FlowNodeInfo(subProcess.getId(),new Double(positionElement.getAttribute("x")), new Double(positionElement.getAttribute("y")),x,y,"subProcess", height, width);
                    flowNodesMap.put(subProcessElement.getAttribute("id"), fni);

                    plane = DrawShape.drawShape(plane, modelInstance, subProcess, x, y, height, width, true, true);
                }

                for (HashMap.Entry<String, Object> entry : bpmnElementsMap.entrySet()) {
                    String key = entry.getKey();
                    BPMNElement bpmnElement = (BPMNElement) entry.getValue();
                    NodeList elementTaskList = doc.getElementsByTagName(key);
                    for(int j = 0; j < elementTaskList.getLength(); j++) {
                        Element flowElement = (Element) elementTaskList.item(j);
                        searchRequest = xpath.compile("./*[local-name() = 'extensionElements']/*[local-name() = 'OracleExtensions']/*[local-name() = 'GraphicsAttributes']/*[local-name() = 'Position']");

                        NodeList positionList = (NodeList) searchRequest.evaluate(flowElement, XPathConstants.NODESET);
                        Element positionElement = (Element) positionList.item(0);
                        BpmnModelElementInstance element = (BpmnModelElementInstance) modelInstance.newInstance(bpmnElement.getType());
                        element.setAttributeValue("name", flowElement.getAttribute("name"));

                        String parent = flowElement.getParentNode().getNodeName();

                        Double x = new Double(positionElement.getAttribute("x"));
                        x -= bpmnElement.getWidth()/2;
                        Double y = new Double(positionElement.getAttribute("y"));
                        y -= bpmnElement.getHeight()/2;
                        if(parent == "subProcess") {
                            Element parentElement = (Element) flowElement.getParentNode();
                            FlowNodeInfo parentNode = (FlowNodeInfo) flowNodesMap.get(parentElement.getAttribute("id"));
                            x += parentNode.getCalcX();
                            y += parentNode.getCalcY();
                            FlowNode subProcessNode = modelInstance.getModelElementById(parentNode.getNewId());
                            subProcessNode.addChildElement(element);
                        } else {
                            process.addChildElement(element);
                        }

                        // Check for event definitions
                        XPathExpression eventDefinitionSearch = xpath.compile("./*[contains(local-name(), 'EventDefinition')]");
                        NodeList eventDefinitionList = (NodeList) eventDefinitionSearch.evaluate(flowElement, XPathConstants.NODESET);
                        if(eventDefinitionList.getLength() == 1){
                            Element eventDefinitionElement = (Element) eventDefinitionList.item(0);
                            DefinedEvent eventType = (DefinedEvent) eventDefinitionsMap.get(eventDefinitionElement.getTagName());
                            EventDefinition ed = (EventDefinition) modelInstance.newInstance(eventType.getType());
                            element.addChildElement(ed);
                        }

                        // check for boundary events
                        XPathExpression boundaryRequest = xpath.compile("//*[@attachedToRef='" + flowElement.getAttribute("id") + "']");
                        NodeList boundaryEvents = (NodeList) boundaryRequest.evaluate(doc, XPathConstants.NODESET);
                        for (int q = 0; q < boundaryEvents.getLength(); q++) {
                            Element boundaryElement = (Element) boundaryEvents.item(q);
                            BoundaryEvent bde = modelInstance.newInstance(BoundaryEvent.class);
                            bde.setName(boundaryElement.getAttribute("name"));
                            bde.setAttachedTo((Activity)element);
                            XPathExpression boundaryEventTypeSearch = xpath.compile("./*[contains(local-name(), 'EventDefinition')]");

                            NodeList boundaryEventTypeList = (NodeList) boundaryEventTypeSearch.evaluate(boundaryElement, XPathConstants.NODESET);
                            if(boundaryEventTypeList.getLength() == 1) {  // There is an event definition found, should only be one
                                Element boundaryEventTypeElement = (Element) boundaryEventTypeList.item(0);
                                DefinedEvent eventType = (DefinedEvent) eventDefinitionsMap.get(boundaryEventTypeElement.getTagName());
                                EventDefinition ed = (EventDefinition) modelInstance.newInstance(eventType.getType());
                                bde.addChildElement(ed);
                            }

                            process.addChildElement(bde);

                            XPathExpression boundaryPositionSearch = xpath.compile("./*[local-name() = 'extensionElements']/*[local-name() = 'OracleExtensions']/*[local-name() = 'GraphicsAttributes']/*[local-name() = 'Position']");

                            NodeList boundaryPositionList = (NodeList) boundaryPositionSearch.evaluate(boundaryElement, XPathConstants.NODESET);
                            Element boundaryPositionElement = (Element) boundaryPositionList.item(0);

                            Double bx = new Double(boundaryPositionElement.getAttribute("x"));
                            bx += 25; // +25 for right, -18 center, -65 left. Need to find algo for multiple events on task
                            Double by = new Double(boundaryPositionElement.getAttribute("y"));
                            by += 20; // +20 bottom, -57 top
                            if(parent == "subProcess") {
                                Element parentElement = (Element) boundaryElement.getParentNode();
                                FlowNodeInfo parentNode = (FlowNodeInfo) flowNodesMap.get(parentElement.getAttribute("id"));
                                bx += parentNode.getCalcX();
                                by += parentNode.getCalcY();
                            }

                            FlowNodeInfo fni = new FlowNodeInfo(bde.getId(),new Double(boundaryPositionElement.getAttribute("x")), new Double(boundaryPositionElement.getAttribute("y")), bx, by, boundaryElement.getTagName(), new Double(0), new Double(0));
                            flowNodesMap.put(boundaryElement.getAttribute("id"), fni);

                            plane = DrawShape.drawShape(plane, modelInstance, bde, bx, by, 36, 36, true, false);
                        }

                        FlowNodeInfo fni = new FlowNodeInfo(element.getAttributeValue("id"),new Double(positionElement.getAttribute("x")), new Double(positionElement.getAttribute("y")), x, y,flowElement.getTagName(), new Double(0), new Double(0));
                        flowNodesMap.put(flowElement.getAttribute("id"), fni);

                        plane = DrawShape.drawShape(plane, modelInstance, element, x, y, bpmnElement.getHeight(), bpmnElement.getWidth(), true, false);
                    }
                }

            }

            // Now draw the sequence flows
            NodeList sequenceFlowList = doc.getElementsByTagName("sequenceFlow");
            for(int i = 0; i < sequenceFlowList.getLength(); i++){
                Element sequenceFlowElement = (Element) sequenceFlowList.item(i);
                String sourceRef = sequenceFlowElement.getAttribute("sourceRef");
                String targetRef = sequenceFlowElement.getAttribute("targetRef");

                FlowNodeInfo source = (FlowNodeInfo) flowNodesMap.get(sourceRef);
                FlowNodeInfo target = (FlowNodeInfo) flowNodesMap.get(targetRef);

                SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
                String parent = sequenceFlowElement.getParentNode().getNodeName();
                Double subProcessXOffset = new Double(0);
                Double subProcessYOffset = new Double(0);
                if(parent == "subProcess") {
                    Element parentElement = (Element) sequenceFlowElement.getParentNode();
                    FlowNodeInfo parentNode = (FlowNodeInfo) flowNodesMap.get(parentElement.getAttribute("id"));
                    FlowNode subProcessNode = modelInstance.getModelElementById(parentNode.getNewId());
                    subProcessXOffset = parentNode.getCalcX();
                    subProcessYOffset = parentNode.getCalcY();
                    subProcessNode.addChildElement(sequenceFlow);

                } else {
                    process.addChildElement(sequenceFlow);
                }

                String targetId = target.getNewId();
                String sourceId = source.getNewId();

                FlowNode targetFlowNode = modelInstance.getModelElementById(targetId);
                FlowNode sourceFlowNode = modelInstance.getModelElementById(sourceId);

                sequenceFlow.setSource(sourceFlowNode);
                sequenceFlow.setTarget(targetFlowNode);

                // Search for sequence flow 'curves' that define flows with an extra waypoint. The waypoint defines the 'middle' of the 'curved' flow
                XPathExpression sequenceCurveSearch = xpath.compile("./*[local-name() = 'extensionElements']/*[local-name() = 'OracleExtensions']/*[local-name() = 'SequenceFlowAttributes']/*[local-name() = 'Positions']");
                NodeList sequenceCurveList = (NodeList) sequenceCurveSearch.evaluate(sequenceFlowElement, XPathConstants.NODESET);

                plane = DrawFlow.drawFlow(plane, modelInstance, sequenceFlow, source, target, sequenceCurveList, subProcessXOffset, subProcessYOffset);
            }

            Bpmn.validateModel(modelInstance);
            File outputFile = new File(args[1]);
            Bpmn.writeModelToFile(outputFile, modelInstance);

            System.out.println("Diagram " + args[0]+ " converted from Oracle BPMN and can be found at "+args[1]);

        /*} catch (Exception e) {
            System.out.println("Exception during processing "+e.getMessage());
        } */


    }
}
