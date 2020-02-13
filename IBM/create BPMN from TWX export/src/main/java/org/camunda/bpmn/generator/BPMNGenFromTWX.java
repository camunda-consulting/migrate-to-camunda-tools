package org.camunda.bpmn.generator;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.Text;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.io.File;
import java.util.HashMap;

public class BPMNGenFromTWX {
    public static void main(String[] args) throws Exception {
        //try {

            // Read and parse file
            // Need to pass in two args - arg[0] is the input file and arg[1] is the output file
            if (args.length < 2) {
                System.out.println("Two arguments are required for this BPMN from TWX program. One for the input file followed by one for the output file.");
                return;
            }

            // Read in XML file that represents a process from TWX export
            File file = new File(args[0]);

            // Create hash map to map ids in old file node objects with ids in new file
            HashMap< String, Object > idMap = new HashMap<>();

            // Create another hash map for boundary events since they use the same id as the node they are attached to. This will help later when adding and drawing sequence flows.
            HashMap< String, Object > boundaryMap = new HashMap<>();

            // Create another hash map for lanes as text annotations use relative y coordinates
            HashMap< String, Object > laneMap = new HashMap<>();

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
            // Look for pools. For IBM BPM pools are not used but a default one is defined behind the scenes. We'll add it here
            NodeList poolList = doc.getElementsByTagName("pool");

            for(int i=0; i < poolList.getLength(); i++) {
                Element poolElement = (Element) poolList.item(i);
                NodeList sizeList = poolElement.getElementsByTagName("size");

                Collaboration collab = modelInstance.newInstance(Collaboration.class);

                Participant participant = modelInstance.newInstance(Participant.class);
                participant.setName("Pool title - please change");

                definitions.addChildElement(collab);
                definitions.addChildElement(process);

                collab.addChildElement(participant);
                participant.setProcess(process);

                plane.setBpmnElement((BaseElement) collab);

                Element sizeElement = (Element) sizeList.item(0);
                Double width = new Double(sizeElement.getAttribute("w"));
                Double height = new Double(sizeElement.getAttribute("h"));

                // draw pool in diagram. If there are multiple pools the default space between is set at 2000 pixels. May need to revisit later
                plane = DrawShape.drawShape(plane, modelInstance, participant, 0, 2000*i, height, width, true);

                LaneSet laneset = modelInstance.newInstance(LaneSet.class);

                NodeList laneList = doc.getElementsByTagName("lane");

                if(laneList.getLength() > 0) { // IBM BPM requires at least 1 lane so length should at least be 1
                    process.addChildElement(laneset);
                    int yLane = 0; // y coordinate for lane
                    for (int j = 0; j < laneList.getLength(); j++) {
                        Element laneElement = (Element) laneList.item(j);

                        NodeList nameList = laneElement.getElementsByTagName("name");
                        Lane lane = modelInstance.newInstance(Lane.class);
                        lane.setName(nameList.item(0).getTextContent());
                        laneset.addChildElement(lane);

                        NodeList heightList = laneElement.getElementsByTagName("height");
                        // Should only be one height value in list
                        height = new Double(heightList.item(0).getTextContent());
                        // Lane is offset in pool by 30 pixels though it can be set to any value
                        plane = DrawShape.drawShape(plane, modelInstance, lane , 30, yLane, height, width-30, true);

                        FlowNodeInfo fni = new FlowNodeInfo(lane.getId(), new Double(30), new Double(yLane), "Lane");
                        laneMap.put(laneElement.getAttribute("id"), fni);

                        // Now get the process nodes aka flow objects and add to process and draw them using their original coordinates as given in the xml
                        NodeList flowObjectList = laneElement.getElementsByTagName("flowObject");

                        for(int h=0; h < flowObjectList.getLength(); h++) {
                            Element objectElement = (Element) flowObjectList.item(h);

                            FlowNode flowNode =  modelInstance.newInstance(Task.class);

                            switch (objectElement.getAttribute("componentType")) { // There are three main categories of flowObjects - Activity, Gateway, and Event

                                case("Activity"):
                                    NodeList activityNameList = objectElement.getElementsByTagName("name");
                                    String taskName = activityNameList.item(0).getTextContent();

                                    NodeList bpmnTaskTypeList = objectElement.getElementsByTagName("bpmnTaskType");
                                    String bpmnTaskType = bpmnTaskTypeList.item(0).getTextContent();

                                    NodeList locationList = objectElement.getElementsByTagName("location");

                                    switch (bpmnTaskType) {

                                        case "1": // User task
                                            flowNode = modelInstance.newInstance(UserTask.class);
                                            break;

                                        case "2": // Business rule task
                                            flowNode = modelInstance.newInstance(BusinessRuleTask.class);
                                            break;

                                        case "3": // Service task
                                            flowNode = modelInstance.newInstance(ServiceTask.class);
                                            break;

                                        case "4": // Script task
                                            flowNode = modelInstance.newInstance(ScriptTask.class);
                                            break;

                                        case "5": // Call activity
                                            flowNode = modelInstance.newInstance(CallActivity.class);
                                            break;

                                        case "6": // Sub process
                                            flowNode = modelInstance.newInstance(SubProcess.class);
                                            break;

                                        case "7": // Event sub process
                                            flowNode = modelInstance.newInstance(SubProcess.class);
                                            flowNode.setAttributeValue("triggeredByEvent", "true");
                                            break;

                                        default:
                                    }


                                    flowNode.setName(taskName);
                                    Element locationElement = (Element) locationList.item(0);
                                    Double x = new Double(locationElement.getAttribute("x"));
                                    Double y = new Double(locationElement.getAttribute("y"));

                                    process.addChildElement(flowNode);
                                    plane = DrawShape.drawShape(plane, modelInstance, flowNode , x, y + yLane, 80, 100, true);
                                    // FlowNodeInfo is an object that will contain the new flow node id in the generated BPMN, the x,y coordinates of each node, and the type of node
                                    fni = new FlowNodeInfo(flowNode.getId(), x, y + yLane, "Activity");
                                    idMap.put(objectElement.getAttribute("id"), fni);

                                    // Look for boundary events (aka attachedEvents), determine if it is interrupting or not (cancelActivity),
                                    // their relative position on the activity (of 12 possible), and what action is associated with the event (actionType)
                                    NodeList attachedList = objectElement.getElementsByTagName("attachedEvent");

                                    for(int k=0; k < attachedList.getLength(); k++) {
                                        Element attachedElement = (Element) attachedList.item(k);
                                        NodeList boundaryNameList = attachedElement.getElementsByTagName("name");
                                        String boundaryName =  boundaryNameList.item(0).getTextContent();

                                        NodeList interruptingList = attachedElement.getElementsByTagName("cancelActivity");
                                        NodeList positionList = attachedElement.getElementsByTagName("positionId");
                                        NodeList actionTypeList = attachedElement.getElementsByTagName("actionType");

                                        String interrupting = interruptingList.item(0).getTextContent();
                                        String position = positionList.item(0).getTextContent();
                                        String actionType = actionTypeList.item(0).getTextContent();

                                        BpmnModelElementInstance boundaryElementInstance = modelInstance.newInstance(BoundaryEvent.class);
                                        boundaryElementInstance.setAttributeValue("name", boundaryName);
                                        boundaryElementInstance.setAttributeValue("attachedToRef", flowNode.getAttributeValue("id"));
                                        boundaryElementInstance.setAttributeValue("cancelActivity", interrupting);
                                        process.addChildElement(boundaryElementInstance);


                                        Double xBound = new Double(0);
                                        Double yBound = new Double(0);

                                        // Find the relative position of the boundary event and add or subtract from base x,y of node
                                        switch (position) {
                                            case "leftCenter":
                                                xBound = x - 18;
                                                yBound = y + 22;
                                                break;

                                            case "leftTop":
                                                xBound = x - 18;
                                                yBound = y + 2;
                                                break;

                                            case "leftBottom":
                                                xBound = x - 18;
                                                yBound = y + 44;
                                                break;

                                            case "bottomCenter":
                                                xBound = x + 32;
                                                yBound = y + 62;
                                                break;

                                            case "bottomRight":
                                                xBound = x + 62;
                                                yBound = y + 62;
                                                break;

                                            case "bottomLeft":
                                                xBound = x + 2;
                                                yBound = y + 62;
                                                break;

                                            case "rightCenter":
                                                xBound = x + 82;
                                                yBound = y + 22;
                                                break;

                                            case "rightTop":
                                                xBound = x + 82;
                                                yBound = y + 2;
                                                break;

                                            case "rightBottom":
                                                xBound = x + 82;
                                                yBound = y + 44;
                                                break;

                                            case "topCenter":
                                                xBound = x + 32;
                                                yBound = y - 18;
                                                break;

                                            case "topRight":
                                                xBound = x + 62;
                                                yBound = y - 18;
                                                break;

                                            case "topLeft":
                                                xBound = x + 2;
                                                yBound = y - 18;
                                                break;

                                            default:
                                        }

                                        // Find type of event
                                        switch(actionType) {

                                            case "1": // message boundary
                                                MessageEventDefinition med = modelInstance.newInstance(MessageEventDefinition.class);
                                                boundaryElementInstance.addChildElement(med);
                                                break;

                                            case "2": // timer boundary
                                                TimerEventDefinition ted = modelInstance.newInstance(TimerEventDefinition.class);
                                                boundaryElementInstance.addChildElement(ted);
                                                break;

                                            case "5": // error boundary
                                                ErrorEventDefinition eed = modelInstance.newInstance(ErrorEventDefinition.class);
                                                boundaryElementInstance.addChildElement(eed);
                                                break;

                                            default:
                                        }
                                        // Check for interrupting/non-interrupting behavior and add it
                                        NodeList doCloseTaskList = attachedElement.getElementsByTagName("doCloseTask");
                                        Element doCloseTaskElement = (Element) doCloseTaskList.item(0);
                                        boundaryElementInstance.setAttributeValue("cancelActivity", doCloseTaskElement.getTextContent());

                                        plane = DrawShape.drawShape(plane, modelInstance, boundaryElementInstance , xBound, yBound + yLane, 36, 36, true);
                                        fni = new FlowNodeInfo(boundaryElementInstance.getAttributeValue("id"), xBound, yBound + yLane, "BoundaryEvent");
                                        boundaryMap.put(attachedElement.getAttribute("id"), fni);
                                    }
                                    break;

                                case("Gateway"):
                                    nameList = objectElement.getElementsByTagName("name");
                                    String gatewayName = nameList.item(0).getTextContent();

                                    NodeList gatewayTypeList = objectElement.getElementsByTagName("gatewayType");
                                    String gatewayType = gatewayTypeList.item(0).getTextContent();

                                    locationList = objectElement.getElementsByTagName("location");

                                    switch (gatewayType) {

                                        case "1": // Exclusive
                                            flowNode = modelInstance.newInstance(ExclusiveGateway.class);
                                            break;

                                        case "2": // Event based gateway
                                            flowNode = modelInstance.newInstance(EventBasedGateway.class);
                                            break;

                                        case "3": // Inclusive
                                            flowNode = modelInstance.newInstance(InclusiveGateway.class);
                                            break;

                                        case "5": // Parallel
                                            flowNode = modelInstance.newInstance(ParallelGateway.class);
                                            break;

                                        default:
                                    }

                                    flowNode.setAttributeValue("name", gatewayName);
                                    locationElement = (Element) locationList.item(0);
                                    x = new Double(locationElement.getAttribute("x"));
                                    y = new Double(locationElement.getAttribute("y"));

                                    process.addChildElement(flowNode);
                                    plane = DrawShape.drawShape(plane, modelInstance, flowNode , x, y + yLane, 50, 50, true);
                                    fni = new FlowNodeInfo(flowNode.getId(), x, y + yLane, "Gateway");
                                    idMap.put(objectElement.getAttribute("id"), fni);
                                    break;

                                case("Event"):
                                    nameList = objectElement.getElementsByTagName("name");
                                    String eventName = nameList.item(0).getTextContent();

                                    NodeList eventTypeList = objectElement.getElementsByTagName("eventType");
                                    String eventType = eventTypeList.item(0).getTextContent();

                                    locationList = objectElement.getElementsByTagName("location");

                                    switch (eventType) {

                                        case "1": // start event
                                            flowNode = modelInstance.newInstance(StartEvent.class);
                                            NodeList eventActionList = objectElement.getElementsByTagName("EventAction");
                                            if (eventActionList.getLength() > 0) { // make it a message start event
                                                MessageEventDefinition med = modelInstance.newInstance(MessageEventDefinition.class);
                                                flowNode.addChildElement(med);
                                            }
                                            break;

                                        case "2": // end event
                                            flowNode = modelInstance.newInstance(EndEvent.class);
                                            eventActionList = objectElement.getElementsByTagName("actionType");
                                            if (eventActionList.getLength() > 0) {
                                                Element eventElement = (Element) eventActionList.item(0);
                                                String type = eventElement.getTextContent();

                                                switch (type) {
                                                    case "12": // Message throw end
                                                        MessageEventDefinition med = modelInstance.newInstance(MessageEventDefinition.class);
                                                        flowNode.addChildElement(med);
                                                        break;

                                                    case "5": // Error end
                                                        ErrorEventDefinition eed = modelInstance.newInstance(ErrorEventDefinition.class);
                                                        flowNode.addChildElement(eed);
                                                        break;

                                                    case "8": // Terminate
                                                        TerminateEventDefinition ted = modelInstance.newInstance(TerminateEventDefinition.class);
                                                        flowNode.addChildElement(ted);
                                                        break;

                                                    default:
                                                }

                                            }
                                            break;

                                        case "3": // Some sort of intermediate event
                                            eventActionList = objectElement.getElementsByTagName("actionType");
                                            if (eventActionList.getLength() > 0) {
                                                Element eventElement = (Element) eventActionList.item(0);
                                                String type = eventElement.getTextContent();
                                                switch (type) {

                                                    case "1": // catch intermediate event
                                                        flowNode = modelInstance.newInstance(IntermediateCatchEvent.class);
                                                        MessageEventDefinition med = modelInstance.newInstance(MessageEventDefinition.class);
                                                        flowNode.addChildElement(med);
                                                        break;

                                                    case "12": // throw intermediate event
                                                        flowNode = modelInstance.newInstance(IntermediateThrowEvent.class);
                                                        med = modelInstance.newInstance(MessageEventDefinition.class);
                                                        flowNode.addChildElement(med);
                                                        break;

                                                    case "2": // timer intermediate event
                                                        flowNode = modelInstance.newInstance(IntermediateThrowEvent.class);
                                                        TimerEventDefinition ted = modelInstance.newInstance(TimerEventDefinition.class);
                                                        flowNode.addChildElement(ted);
                                                        break;

                                                    default: // Includes tracking events which have no analog in Camunda
                                                        flowNode = modelInstance.newInstance(IntermediateThrowEvent.class);
                                                }

                                            } else { // Just a none intermediate event though it doesn't seem possible in IBM BPM
                                                flowNode = modelInstance.newInstance(IntermediateThrowEvent.class);
                                            }

                                        default:
                                    }

                                    flowNode.setAttributeValue("name", eventName);
                                    locationElement = (Element) locationList.item(0);
                                    x = new Double(locationElement.getAttribute("x"));
                                    y = new Double(locationElement.getAttribute("y"));

                                    process.addChildElement(flowNode);
                                    plane = DrawShape.drawShape(plane, modelInstance, flowNode , x, y + yLane, 36, 36, true);
                                    fni = new FlowNodeInfo(flowNode.getId(), x, y + yLane, "Event");
                                    idMap.put(objectElement.getAttribute("id"), fni);

                                    break;
                                default:
                            }
                        }
                        yLane += height; // Add height of for next lane y coordinate
                    }
                }
            }

            // Next, look for diagram annotations
            NodeList noteList = doc.getElementsByTagName("note");
            for(int i=0; i < noteList.getLength(); i++) {
                Element noteElement = (Element) noteList.item(i);
                NodeList documentation = noteElement.getElementsByTagName("documentation");
                TextAnnotation textA = modelInstance.newInstance(TextAnnotation.class);
                Text text = modelInstance.newInstance(Text.class);
                text.setTextContent(documentation.item(0).getTextContent());
                textA.setText(text);
                process.addChildElement(textA);

                // Need to get y coordinate of lane associated with annotation as annotation y coordinate is relative to lane and not absolute
                NodeList laneList = noteElement.getElementsByTagName("BpmnObjectId");
                Element laneElement = (Element) laneList.item(0);
                XPathExpression searchRequest = null;
                XPath xpath = XPathFactory.newInstance().newXPath();

                FlowNodeInfo fni = (FlowNodeInfo) laneMap.get(laneElement.getAttribute("id"));

                NodeList locationList = noteElement.getElementsByTagName("location");
                Element locationElement = (Element) locationList.item(0);
                Double x = new Double(locationElement.getAttribute("x"));
                Double y = new Double(locationElement.getAttribute("y"));
                NodeList sizeList = noteElement.getElementsByTagName("size");
                Element sizeElement = (Element) sizeList.item(0);
                Double height = new Double(sizeElement.getAttribute("h"));
                Double width = new Double(sizeElement.getAttribute("w"));
                plane = DrawShape.drawShape(plane, modelInstance, textA, x, y+fni.getY(), height, width, true);
            }

            // Next, look through flow nodes and determine source and target as well as the relative position of the flow attachment points
            XPathExpression searchRequest = null;
            XPath xpath = XPathFactory.newInstance().newXPath();
            searchRequest = xpath.compile("//flow[@id]");
            NodeList originalFlowNodes = (NodeList) searchRequest.evaluate(doc, XPathConstants.NODESET);
            // Variables for label and label display boolean
            Boolean nameVisible = new Boolean(true);
            String sequenceLabel = "";

            for(int i=0; i < originalFlowNodes.getLength(); i++) {
                Element originalElement = (Element) originalFlowNodes.item(i);
                String flowId = originalElement.getAttribute("id");
                // Find if label needs to be displayed
                NodeList labelVisible = originalElement.getElementsByTagName("nameVisible");
                nameVisible = new Boolean(labelVisible.item(0).getTextContent());
                // Get name of label
                NodeList nameList = originalElement.getElementsByTagName("name");
                if(nameList.getLength() > 0){
                    sequenceLabel = nameList.item(0).getTextContent();
                }
                // Find a target of sequence flow
                searchRequest = xpath.compile("//inputPort/flow[@ref='"+flowId+"']");
                NodeList inputNodes = (NodeList) searchRequest.evaluate(doc, XPathConstants.NODESET);

                // Should only have one input. Get flowObject node from inputPort node
                // Then get id from original flowObject to look up id in new model
                // This becomes the target of the sequence flow
                Element inputElement = (Element) inputNodes.item(0);
                // Get relative position on target node for sequence flow attachment point
                Element parentNode = (Element) inputElement.getParentNode();
                NodeList positionIds = parentNode.getElementsByTagName("positionId");
                String targetPosition = positionIds.item(0).getTextContent();
                // Get original flow node, look up id, and retrieve new flow node in HashMap along with other info regarding node in new file
                Node flowObjectNode = inputElement.getParentNode().getParentNode();
                NamedNodeMap attributes = flowObjectNode.getAttributes();
                Node originalTargetId = attributes.getNamedItem("id");
                FlowNodeInfo fni = (FlowNodeInfo) idMap.get(originalTargetId.getTextContent());
                String targetId = fni.getId();
                String targetType = fni.getType();
                Double targetX = fni.getX();
                Double targetY = fni.getY();

                // Now do the same for the source ref
                searchRequest = xpath.compile("//outputPort/flow[@ref='"+flowId+"']");
                NodeList outputNodes = (NodeList) searchRequest.evaluate(doc, XPathConstants.NODESET);

                Element outputElement = (Element) outputNodes.item(0);
                parentNode = (Element) outputElement.getParentNode();
                positionIds = parentNode.getElementsByTagName("positionId");
                String sourcePosition = positionIds.item(0).getTextContent();
                flowObjectNode = outputElement.getParentNode().getParentNode();
                attributes = flowObjectNode.getAttributes();
                Node originalSourceId = attributes.getNamedItem("id");
                fni = (FlowNodeInfo) idMap.get(originalSourceId.getTextContent());
                String sourceId = null;
                String sourceType = null;
                Double sourceX = null;
                Double sourceY = null;
                if(fni != null) {
                    sourceId = fni.getId(); // Means it's a node
                    sourceType = fni.getType();
                    sourceX = fni.getX();
                    sourceY = fni.getY();
                } else { // Means it's a boundary event
                    fni = (FlowNodeInfo) boundaryMap.get(originalSourceId.getTextContent());
                    sourceId = fni.getId();
                    sourceType = fni.getType();
                    sourceX = fni.getX();
                    sourceY = fni.getY();
                }
                FlowNode targetFlowNode = modelInstance.getModelElementById(targetId);
                FlowNode sourceFlowNode = modelInstance.getModelElementById(sourceId);

                // Now create the sequence flows, add to model, and then add to diagram
                SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);

                if(sourceFlowNode != null && targetFlowNode != null) {
                    process.addChildElement(sequenceFlow);
                    sequenceFlow.setSource(sourceFlowNode);
                    sourceFlowNode.getOutgoing().add(sequenceFlow);
                    sequenceFlow.setTarget(targetFlowNode);
                    targetFlowNode.getIncoming().add(sequenceFlow);

                    if(nameVisible) {
                        sequenceFlow.setName(sequenceLabel);
                    }

                    plane = DrawFlow.drawFlow(plane, modelInstance, sequenceFlow, sourceType, sourcePosition, sourceX, sourceY, targetType, targetPosition, targetX, targetY);

                }
            }

            Bpmn.validateModel(modelInstance);
            File outputFile = new File(args[1]);
            Bpmn.writeModelToFile(outputFile, modelInstance);


        /*} catch (Exception e) {
            System.out.println("Exception during processing "+e.getMessage());
        } */


    }
}
