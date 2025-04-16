package org.camunda.bpmn.generator;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.instance.EventDefinitionImpl;
import org.camunda.bpm.model.bpmn.impl.instance.MessageImpl;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.Text;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape;
import org.camunda.bpm.model.bpmn.instance.dc.Bounds;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

public class BPMNGenFromXPDL2dot0 {
    public static void main(String[] args) throws Exception {
        //try {

        // Read and parse file
        // Need to pass in two args - arg[0] is the input file and arg[1] is the output file
        if (args.length < 2) {
            System.out.println("Two arguments are required for this converter. One for the input file followed by one for the output file.");
            return;
        }

        // Read in XML file that represents a process from XPDL export
        File file = new File(args[0]);

        // Create hash map to map ids in old file node objects with ids in new file
        HashMap<String, String> idMap = new HashMap<>();

        // Create hash of flow nodes for drawing of sequence flows later
        HashMap< String, Object > flowNodesMap = new HashMap<>();

        // Create another hash map for boundary events since they use the same id as the node they are attached to. This will help later when adding and drawing sequence flows.
        HashMap<String, Object> boundaryMap = new HashMap<>();

        // Read document in preparation for Xpath searches
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);

        doc.getDocumentElement().normalize();

        // Create BPMN model using Camunda Model APIs
        BpmnModelInstance modelInstance = Bpmn.createEmptyModel();

        Definitions definitions = modelInstance.newInstance(Definitions.class);
        definitions.setTargetNamespace("http://camunda.org/examples");
        modelInstance.setDefinitions(definitions);

        // For the diagram, a diagram and a plane element need to be created. The plane is set in a diagram object and the diagram is added as a child element
        BpmnDiagram bpmnDiagram = modelInstance.newInstance(BpmnDiagram.class);
        BpmnPlane plane = modelInstance.newInstance(BpmnPlane.class);

        bpmnDiagram.setBpmnPlane(plane);
        definitions.addChildElement(bpmnDiagram);

        Process process = modelInstance.newInstance(Process.class);
        process.setExecutable(true); // Want to make sure it is executable by default in Modeler

        // Add process to defs November 2024
        definitions.addChildElement(process);
        plane.setBpmnElement(process);

        // Get pool, laneset, and lane information and add to process
        // Look for pools.

        XPathExpression searchRequest = null;
        XPath xpath = XPathFactory.newInstance().newXPath();
        //searchRequest = xpath.compile("//Pools/Pool/NodeGraphicsInfos/NodeGraphicsInfo"); // Does not take into account namespaces
        searchRequest = xpath.compile("//*[local-name() = 'Pools']/*[local-name() = 'Pool']/*[local-name() = 'NodeGraphicsInfos']/*[local-name() = 'NodeGraphicsInfo']");
        //NodeList originalFlowNodes = (NodeList) searchRequest.evaluate(doc, XPathConstants.NODESET);

        NodeList nodeGraphicInfoList = (NodeList) searchRequest.evaluate(doc, XPathConstants.NODESET);

        Double width = new Double(0);
        Double height = new Double(0);
        Double x = new Double(0);
        Double y = new Double(0);

        Double zoomFactor = 1.5d;

        for (int i = 0; i < nodeGraphicInfoList.getLength(); i++) {
            Element nodeGraphicInfoElement = (Element) nodeGraphicInfoList.item(i);
            Element poolElement = (Element) nodeGraphicInfoElement.getParentNode().getParentNode();

            Boolean poolVisible = new Boolean(poolElement.getAttribute("BoundaryVisible"));
            if (poolVisible) {

                Collaboration collab = modelInstance.newInstance(Collaboration.class);

                Participant participant = modelInstance.newInstance(Participant.class);
                participant.setName(poolElement.getAttribute("Name"));

                definitions.addChildElement(collab);
                definitions.addChildElement(process);

                collab.addChildElement(participant);
                participant.setProcess(process);

                plane.setBpmnElement(collab);

                width = new Double(nodeGraphicInfoElement.getAttribute("Width"));
                height = new Double(nodeGraphicInfoElement.getAttribute("Height"));

                // Get x and y coordinates
                NodeList coordinateList = nodeGraphicInfoElement.getElementsByTagNameNS("*","Coordinates");
                Element coordinateElement = (Element) coordinateList.item(0);
                Double poolX = new Double(coordinateElement.getAttribute("XCoordinate"));
                Double poolY = new Double(coordinateElement.getAttribute("YCoordinate"));

                // draw pool in diagram.
                plane = DrawShape.drawShape(plane, modelInstance, participant, poolX*zoomFactor, poolY*zoomFactor, height, width, true);

                // Create laneset and add lanes
                LaneSet laneset = modelInstance.newInstance(LaneSet.class);

                //searchRequest = xpath.compile("//Pool/Lanes/Lane");
                searchRequest = xpath.compile("//*[local-name() = 'Pool']/*[local-name() = 'Lanes']/*[local-name() = 'Lane']");

                NodeList laneList = (NodeList) searchRequest.evaluate(poolElement, XPathConstants.NODESET); //doc.getElementsByTagName("lanes");

                for (int z = 0; z < laneList.getLength(); z++) {
                    process.addChildElement(laneset);
                    Lane lane = modelInstance.newInstance(Lane.class);
                    laneset.addChildElement(lane);
                    Element laneElement = (Element) laneList.item(z);
                    lane.setName(laneElement.getAttribute("Name"));
                    NodeList nodeGraphicsInfoList = laneElement.getElementsByTagNameNS("*","NodeGraphicsInfo");
                    Element ngiElement = (Element) nodeGraphicsInfoList.item(0);

                    height = new Double(ngiElement.getAttribute("Height"));
                    width = new Double(ngiElement.getAttribute("Width"));

                    NodeList coordinatesList = laneElement.getElementsByTagNameNS("*","Coordinates");
                    Element coordinatesElement = (Element) coordinatesList.item(0);

                    x = new Double(coordinatesElement.getAttribute("XCoordinate"));
                    y = new Double(coordinatesElement.getAttribute("YCoordinate"));

                    //plane = DrawShape.drawShape(plane, modelInstance, lane, x + poolX - 20, y + poolY, height, width + 20, true);
                    plane = DrawShape.drawShape(plane, modelInstance, lane, x*zoomFactor, y*zoomFactor, height, width, true);
                }
            }
        }

        //searchRequest = xpath.compile("//WorkflowProcesses/WorkflowProcess");
        searchRequest = xpath.compile("//*[local-name() = 'WorkflowProcesses']/*[local-name() = 'WorkflowProcess']");
        NodeList workflowList = (NodeList) searchRequest.evaluate(doc, XPathConstants.NODESET);

        for (int i = 0; i < workflowList.getLength(); i++) {
            Element workflowElement = (Element) workflowList.item(i);
            //searchRequest = xpath.compile("//Activities/Activity/Implementation");
            searchRequest = xpath.compile("//*[local-name() = 'Activities']/*[local-name() = 'Activity']/*[local-name() = 'Implementation']");
            NodeList implementationList = (NodeList) searchRequest.evaluate(workflowElement, XPathConstants.NODESET);
            if (workflowElement.getAttribute("Name") != "" || workflowElement.getAttribute("Id") != "") {
                for (int z = 0; z < implementationList.getLength(); z++) {
                    Element implementationElement = (Element) implementationList.item(z);
                    Element parentElement = (Element) implementationElement.getParentNode();
                    NodeList taskList = implementationElement.getElementsByTagNameNS("*","Task");
                    if(taskList.getLength() == 0) { // it's a call activity
                        CallActivity callActivity = modelInstance.newInstance(CallActivity.class);
                        process.addChildElement(callActivity);
                        callActivity.setAttributeValue("name", parentElement.getAttribute("Name"));

                        NodeList coordinatesList = parentElement.getElementsByTagNameNS("*","Coordinates");
                        Element coordinatesElement = (Element) coordinatesList.item(0);
                        x = new Double(coordinatesElement.getAttribute("XCoordinate"));
                        y = new Double(coordinatesElement.getAttribute("YCoordinate"));

                        // Add new entry in map for sequence flows later
                        idMap.put(parentElement.getAttribute("Id"), callActivity.getId());
                        plane = DrawShape.drawShape(plane, modelInstance, callActivity, x*zoomFactor, y*zoomFactor, 80, 100, true);
                        FlowNodeInfo fni = new FlowNodeInfo(callActivity.getId(), x, y, x*zoomFactor, y*zoomFactor, CallActivity.class.toString(), 80d, 100d);
                        flowNodesMap.put(callActivity.getId(), fni);

                    } else {
                        Task task = modelInstance.newInstance(Task.class);
                        Element taskElement = (Element) taskList.item(0);
                        NodeList taskTypes = taskElement.getChildNodes();

                        if(taskTypes.getLength() > 0) { // Not a None type task

                            switch (taskTypes.item(1).getNodeName()) {

                                case "TaskUser":
                                    task = modelInstance.newInstance(UserTask.class);
                                    break;

                                case "TaskSend":
                                    task = modelInstance.newInstance(SendTask.class);
                                    break;

                                case "TaskReceive":
                                    task = modelInstance.newInstance(ReceiveTask.class);
                                    break;

                                case "TaskManual":
                                    task = modelInstance.newInstance(ManualTask.class);
                                    break;

                                case "TaskBusinessRule":
                                    task = modelInstance.newInstance(BusinessRuleTask.class);
                                    break;

                                case "TaskService":
                                    task = modelInstance.newInstance(ServiceTask.class);
                                    break;

                                case "TaskScript":
                                    task = modelInstance.newInstance(ScriptTask.class);
                                    break;

                                default:
                            }
                        }

                        process.addChildElement(task);
                        task.setAttributeValue("name", parentElement.getAttribute("Name"));
                        NodeList coordinatesList = parentElement.getElementsByTagNameNS("*","Coordinates");
                        Element coordinatesElement = (Element) coordinatesList.item(0);
                        x = new Double(coordinatesElement.getAttribute("XCoordinate"));
                        y = new Double(coordinatesElement.getAttribute("YCoordinate"));

                        // Add new entry in map for sequence flows later
                        idMap.put(parentElement.getAttribute("Id"), task.getId());
                        plane = DrawShape.drawShape(plane, modelInstance, task, x*zoomFactor, y*zoomFactor, 80, 100, true);
                        FlowNodeInfo fni = new FlowNodeInfo(task.getId(), x, y, x*zoomFactor, y*zoomFactor, Task.class.toString(), task.getDiagramElement().getBounds().getHeight(), task.getDiagramElement().getBounds().getWidth());
                        flowNodesMap.put(task.getId(), fni);
                    }

                }
            }
        }

        for (int i = 0; i < workflowList.getLength(); i++) {
            Element workflowElement = (Element) workflowList.item(i);
            //searchRequest = xpath.compile("//Activities/Activity/BlockActivity");
            searchRequest = xpath.compile("//*[local-name() = 'Activities']/*[local-name() = 'Activity']/*[local-name() = 'BlockActivity']");
            NodeList activityList = (NodeList) searchRequest.evaluate(workflowElement, XPathConstants.NODESET);
            if (workflowElement.getAttribute("Name") != "" || workflowElement.getAttribute("Id") != "") {
                for (int z = 0; z < activityList.getLength(); z++) {
                    Element activityElement = (Element) activityList.item(z);
                    Element parentElement = (Element) activityElement.getParentNode();
                    SubProcess sp = modelInstance.newInstance(SubProcess.class);
                    process.addChildElement(sp);
                    sp.setAttributeValue("name", parentElement.getAttribute("Name"));
                    NodeList coordinatesList = parentElement.getElementsByTagNameNS("*","Coordinates");
                    Element coordinatesElement = (Element) coordinatesList.item(0);
                    x = new Double(coordinatesElement.getAttribute("XCoordinate"));
                    y = new Double(coordinatesElement.getAttribute("YCoordinate"));

                    // Add new entry in map for sequence flows later
                    idMap.put(parentElement.getAttribute("Id"), sp.getId());

                    plane = DrawShape.drawShape(plane, modelInstance, sp, x*zoomFactor, y*zoomFactor, 80, 100, true);
                    FlowNodeInfo fni = new FlowNodeInfo(sp.getId(), x, y, x*zoomFactor, y*zoomFactor, Task.class.toString(), 80d, 100d);
                    flowNodesMap.put(sp.getId(), fni);
                }
            }
        }

        for (int i = 0; i < workflowList.getLength(); i++) {
            Element workflowElement = (Element) workflowList.item(i);
            //searchRequest = xpath.compile("//Activities/Activity/Event");
            searchRequest = xpath.compile("//*[local-name() = 'Activities']/*[local-name() = 'Activity']/*[local-name() = 'Event']");
            NodeList eventList = (NodeList) searchRequest.evaluate(workflowElement, XPathConstants.NODESET);
            if (workflowElement.getAttribute("Name") != "" || workflowElement.getAttribute("Id") != "") {
                for (int z = 0; z < eventList.getLength(); z++) {

                    Element eventElement = (Element) eventList.item(z);
                    BpmnModelElementInstance boundaryElementInstance = null;
                    Event event = null;
                    boolean boundary = false;

                    NodeList startEventList = eventElement.getElementsByTagNameNS("*","StartEvent");
                    if (startEventList.getLength() > 0) { // It's a start event
                        event = modelInstance.newInstance(StartEvent.class);
                        Element startElement = (Element) startEventList.item(0);
                        String trigger = startElement.getAttribute("Trigger");

                        Element parentElement = (Element) startElement.getParentNode().getParentNode();
                        event.setAttributeValue("name", parentElement.getAttribute("Name"));

                        switch(trigger) {
                            case "None":
                                break;

                            case "Message":
                                MessageEventDefinition med = modelInstance.newInstance(MessageEventDefinition.class);
                                event.addChildElement(med);
                                break;

                            case "Timer":
                                TimerEventDefinition ted = modelInstance.newInstance(TimerEventDefinition.class);
                                event.addChildElement(ted);
                                break;

                            case "Signal":
                                SignalEventDefinition sed = modelInstance.newInstance(SignalEventDefinition.class);
                                event.addChildElement(sed);
                                break;

                            case "Conditional":
                                ConditionalEventDefinition ced = modelInstance.newInstance(ConditionalEventDefinition.class);
                                Condition condition = modelInstance.newInstance(Condition.class);
                                ced.setCondition(condition);
                                event.addChildElement(ced);
                                break;

                            default:
                        }
                    }

                    NodeList interEventList = eventElement.getElementsByTagNameNS("*","IntermediateEvent");
                    if (interEventList.getLength() > 0) { // It's an intermediate event

                        Element interEventElement = (Element) interEventList.item(0);
                        String eventType = interEventElement.getAttribute("Trigger");

                        switch (eventType) {
                            case "None":
                                event = modelInstance.newInstance(IntermediateThrowEvent.class);
                                Element parentElement = (Element) interEventElement.getParentNode().getParentNode();
                                event.setAttributeValue("name", parentElement.getAttribute("Name"));
                                MessageEventDefinition evd = modelInstance.newInstance(MessageEventDefinition.class);

                                if (interEventElement.getAttribute("Target") != "") {

                                    boundaryElementInstance = modelInstance.newInstance(BoundaryEvent.class);
                                    boundaryElementInstance.setAttributeValue("name", parentElement.getAttribute("Name"));
                                    boundaryElementInstance.setAttributeValue("attachedToRef", (String) idMap.get(interEventElement.getAttribute("Target")));
                                    Boolean interrupt = new Boolean(interEventElement.getAttribute("Interrupting"));

                                    if (interEventElement.getAttribute("Interrupting") == "" ){
                                        interrupt = true;
                                    }
                                    boundaryElementInstance.setAttributeValue("cancelActivity", interrupt.toString());
                                    boundaryElementInstance.addChildElement(evd);
                                    boundary = true;
                                } else {
                                    // Need to figure out how XPDL determines catch or throw
                                    event = modelInstance.newInstance(IntermediateThrowEvent.class);
                                    event.setAttributeValue("name", parentElement.getAttribute("Name"));
                                    event.addChildElement(evd);
                                }
                                break;

                            case "Message":
                                //String isAttached = interEventElement.getAttribute("IsAttached");
                                NodeList throwList = interEventElement.getElementsByTagNameNS("*","TriggerResultMessage");
                                Element throwElement = (Element) throwList.item(0);
                                parentElement = (Element) interEventElement.getParentNode().getParentNode();
                                MessageEventDefinition med = modelInstance.newInstance(MessageEventDefinition.class);

                                if (throwElement.getAttribute("CatchThrow").equals("THROW")) { // It's a throw inter
                                    event = modelInstance.newInstance(IntermediateThrowEvent.class);
                                    event.setAttributeValue("name", parentElement.getAttribute("Name"));
                                    event.addChildElement(med);
                                } else {
                                    //if (isAttached != "") {
                                    if (interEventElement.getAttribute("Target") != "") {
                                        boundaryElementInstance = modelInstance.newInstance(BoundaryEvent.class);
                                        boundaryElementInstance.setAttributeValue("name", parentElement.getAttribute("Name"));
                                        boundaryElementInstance.setAttributeValue("attachedToRef", (String) idMap.get(interEventElement.getAttribute("Target")));
                                        Boolean interrupt = new Boolean(interEventElement.getAttribute("Interrupting"));

                                        if (interEventElement.getAttribute("Interrupting") == "" ){
                                            interrupt = true;
                                        }
                                        boundaryElementInstance.setAttributeValue("cancelActivity", interrupt.toString());
                                        boundaryElementInstance.addChildElement(med);
                                        boundary = true;
                                    } else {
                                        event = modelInstance.newInstance(IntermediateCatchEvent.class);
                                        event.setAttributeValue("name", parentElement.getAttribute("Name"));
                                        event.addChildElement(med);
                                    }
                                }
                                break;

                            case "Timer":
                                //isAttached = interEventElement.getAttribute("IsAttached");
                                parentElement = (Element) interEventElement.getParentNode().getParentNode();
                                TimerEventDefinition ted = modelInstance.newInstance(TimerEventDefinition.class);

                                //if (isAttached != "") {
                                if (interEventElement.getAttribute("Target") != "") {
                                    boundaryElementInstance = modelInstance.newInstance(BoundaryEvent.class);
                                    boundaryElementInstance.setAttributeValue("name", parentElement.getAttribute("Name"));
                                    boundaryElementInstance.setAttributeValue("attachedToRef", (String) idMap.get(interEventElement.getAttribute("Target")));
                                    Boolean interrupt = new Boolean(interEventElement.getAttribute("Interrupting"));

                                    if (interEventElement.getAttribute("Interrupting") == "" ){
                                        interrupt = true;
                                    }
                                    boundaryElementInstance.setAttributeValue("cancelActivity", interrupt.toString());
                                    boundaryElementInstance.addChildElement(ted);
                                    boundary = true;
                                } else {
                                    event = modelInstance.newInstance(IntermediateCatchEvent.class);
                                    event.setAttributeValue("name", parentElement.getAttribute("Name"));
                                    event.addChildElement(ted);
                                }
                                break;

                            case "Escalation":
                                //isAttached = interEventElement.getAttribute("IsAttached");
                                parentElement = (Element) interEventElement.getParentNode().getParentNode();
                                EscalationEventDefinition eed = modelInstance.newInstance(EscalationEventDefinition.class);

                                //if (isAttached != "") {
                                if (interEventElement.getAttribute("Target") != "") {

                                    boundaryElementInstance = modelInstance.newInstance(BoundaryEvent.class);
                                    boundaryElementInstance.setAttributeValue("name", parentElement.getAttribute("Name"));
                                    boundaryElementInstance.setAttributeValue("attachedToRef", (String) idMap.get(interEventElement.getAttribute("Target")));
                                    Boolean interrupt = new Boolean(interEventElement.getAttribute("Interrupting"));

                                    if (interEventElement.getAttribute("Interrupting") == "" ){
                                        interrupt = true;
                                    }
                                    boundaryElementInstance.setAttributeValue("cancelActivity", interrupt.toString());
                                    boundaryElementInstance.addChildElement(eed);
                                    boundary = true;
                                } else {
                                    // Need to figure out how XPDL determines catch or throw
                                    event = modelInstance.newInstance(IntermediateThrowEvent.class);
                                    event.setAttributeValue("name", parentElement.getAttribute("Name"));
                                    event.addChildElement(eed);
                                }
                                break;

                            case "Conditional":
                                //isAttached = interEventElement.getAttribute("IsAttached");
                                parentElement = (Element) interEventElement.getParentNode().getParentNode();
                                ConditionalEventDefinition ced = modelInstance.newInstance(ConditionalEventDefinition.class);
                                Condition condition = modelInstance.newInstance(Condition.class);
                                ced.setCondition(condition);

                                //if (isAttached != "") {
                                if (interEventElement.getAttribute("Target") != "") {

                                    boundaryElementInstance = modelInstance.newInstance(BoundaryEvent.class);
                                    boundaryElementInstance.setAttributeValue("name", parentElement.getAttribute("Name"));
                                    boundaryElementInstance.setAttributeValue("attachedToRef", (String) idMap.get(interEventElement.getAttribute("Target")));
                                    Boolean interrupt = new Boolean(interEventElement.getAttribute("Interrupting"));

                                    if (interEventElement.getAttribute("Interrupting") == "" ){
                                        interrupt = true;
                                    }
                                    boundaryElementInstance.setAttributeValue("cancelActivity", interrupt.toString());
                                    boundaryElementInstance.addChildElement(ced);
                                    boundary = true;
                                } else {
                                    event = modelInstance.newInstance(IntermediateThrowEvent.class);
                                    event.setAttributeValue("name", parentElement.getAttribute("Name"));
                                    event.addChildElement(ced);
                                }
                                break;

                            case "Signal":
                                //isAttached = interEventElement.getAttribute("IsAttached");
                                parentElement = (Element) interEventElement.getParentNode().getParentNode();
                                throwList = interEventElement.getElementsByTagNameNS("*","TriggerResultSignal");
                                throwElement = (Element) throwList.item(0);
                                SignalEventDefinition sed = modelInstance.newInstance(SignalEventDefinition.class);

                                if (throwElement.getAttribute("CatchThrow").equals("THROW")) { // It's a throw inter
                                    event = modelInstance.newInstance(IntermediateThrowEvent.class);
                                    event.setAttributeValue("name", parentElement.getAttribute("Name"));
                                    event.addChildElement(sed);
                                } else {
                                    //if (isAttached != "") {
                                    if (interEventElement.getAttribute("Target") != "") {
                                        boundaryElementInstance = modelInstance.newInstance(BoundaryEvent.class);
                                        boundaryElementInstance.setAttributeValue("name", parentElement.getAttribute("Name"));
                                        boundaryElementInstance.setAttributeValue("attachedToRef", (String) idMap.get(interEventElement.getAttribute("Target")));
                                        Boolean interrupt = new Boolean(interEventElement.getAttribute("Interrupting"));

                                        if (interEventElement.getAttribute("Interrupting") == "") {
                                            interrupt = true;
                                        }
                                        boundaryElementInstance.setAttributeValue("cancelActivity", interrupt.toString());
                                        boundaryElementInstance.addChildElement(sed);
                                        boundary = true;
                                    } else {
                                        event = modelInstance.newInstance(IntermediateCatchEvent.class);
                                        event.setAttributeValue("name", parentElement.getAttribute("Name"));
                                        event.addChildElement(sed);
                                    }
                                }
                                break;

                            case "Error":
                                //isAttached = interEventElement.getAttribute("IsAttached");
                                parentElement = (Element) interEventElement.getParentNode().getParentNode();
                                ErrorEventDefinition ered = modelInstance.newInstance(ErrorEventDefinition.class);

                                //if (isAttached != "") {
                                if (interEventElement.getAttribute("Target") != "") {
                                    boundaryElementInstance = modelInstance.newInstance(BoundaryEvent.class);
                                    boundaryElementInstance.setAttributeValue("name", parentElement.getAttribute("Name"));
                                    boundaryElementInstance.setAttributeValue("attachedToRef", (String) idMap.get(interEventElement.getAttribute("Target")));
                                    Boolean interrupt = new Boolean(interEventElement.getAttribute("Interrupting"));

                                    if (interEventElement.getAttribute("Interrupting") == "" ){
                                        interrupt = true;
                                    }
                                    boundaryElementInstance.setAttributeValue("cancelActivity", interrupt.toString());
                                    boundaryElementInstance.addChildElement(ered);
                                    boundary = true;
                                } else {
                                    event = modelInstance.newInstance(IntermediateThrowEvent.class);
                                    event.setAttributeValue("name", parentElement.getAttribute("Name"));
                                    event.addChildElement(ered);
                                }
                                break;

                            case "Cancel":
                                //isAttached = interEventElement.getAttribute("IsAttached");
                                parentElement = (Element) interEventElement.getParentNode().getParentNode();
                                CancelEventDefinition caed = modelInstance.newInstance(CancelEventDefinition.class);

                                //if (isAttached != "") {
                                if (interEventElement.getAttribute("Target") != "") {
                                    boundaryElementInstance = modelInstance.newInstance(BoundaryEvent.class);
                                    boundaryElementInstance.setAttributeValue("name", parentElement.getAttribute("Name"));
                                    boundaryElementInstance.setAttributeValue("attachedToRef", (String) idMap.get(interEventElement.getAttribute("Target")));
                                    Boolean interrupt = new Boolean(interEventElement.getAttribute("Interrupting"));

                                    if (interEventElement.getAttribute("Interrupting") == "" ){
                                        interrupt = true;
                                    }
                                    boundaryElementInstance.setAttributeValue("cancelActivity", interrupt.toString());
                                    boundaryElementInstance.addChildElement(caed);
                                    boundary = true;
                                } else {
                                    event = modelInstance.newInstance(IntermediateThrowEvent.class);
                                    event.setAttributeValue("name", parentElement.getAttribute("Name"));
                                    event.addChildElement(caed);
                                }
                                break;

                            case "Compensation":
                                //isAttached = interEventElement.getAttribute("IsAttached");
                                parentElement = (Element) interEventElement.getParentNode().getParentNode();
                                CompensateEventDefinition coed = modelInstance.newInstance(CompensateEventDefinition.class);

                                //if (isAttached != "") {
                                if (interEventElement.getAttribute("Target") != "") {
                                    boundaryElementInstance = modelInstance.newInstance(BoundaryEvent.class);
                                    boundaryElementInstance.setAttributeValue("name", parentElement.getAttribute("Name"));
                                    boundaryElementInstance.setAttributeValue("attachedToRef", (String) idMap.get(interEventElement.getAttribute("Target")));
                                    Boolean interrupt = new Boolean(interEventElement.getAttribute("Interrupting"));

                                    if (interEventElement.getAttribute("Interrupting") == "" ){
                                        interrupt = true;
                                    }
                                    boundaryElementInstance.setAttributeValue("cancelActivity", interrupt.toString());
                                    boundaryElementInstance.addChildElement(coed);
                                    boundary = true;
                                } else {
                                    event = modelInstance.newInstance(IntermediateThrowEvent.class);
                                    event.setAttributeValue("name", parentElement.getAttribute("Name"));
                                    event.addChildElement(coed);
                                }
                                break;

                            default:
                                event = modelInstance.newInstance(IntermediateThrowEvent.class);
                        }
                    }

                        NodeList endEventList = eventElement.getElementsByTagNameNS("*","EndEvent");
                        if (endEventList.getLength() > 0) { // It's an end event
                            event = modelInstance.newInstance(EndEvent.class);
                            Element endElement = (Element) endEventList.item(0);
                            String result = endElement.getAttribute("Result");

                            Element parentElement = (Element) endElement.getParentNode().getParentNode();
                            event.setAttributeValue("name", parentElement.getAttribute("Name"));

                            switch(result) {
                                case "None":
                                    break;

                                case "Message":
                                    MessageEventDefinition med = modelInstance.newInstance(MessageEventDefinition.class);
                                    event.addChildElement(med);
                                    break;

                                case "Terminate":
                                    TerminateEventDefinition ted = modelInstance.newInstance(TerminateEventDefinition.class);
                                    event.addChildElement(ted);
                                    break;

                                case "Error":
                                    ErrorEventDefinition eed = modelInstance.newInstance(ErrorEventDefinition.class);
                                    event.addChildElement(eed);
                                    break;

                                case "Signal":
                                    SignalEventDefinition sed = modelInstance.newInstance(SignalEventDefinition.class);
                                    event.addChildElement(sed);
                                    break;

                                case "Escalation":
                                    EscalationEventDefinition esed = modelInstance.newInstance(EscalationEventDefinition.class);
                                    event.addChildElement(esed);
                                    break;

                                case "Compensation":
                                    CompensateEventDefinition coed = modelInstance.newInstance(CompensateEventDefinition.class);
                                    event.addChildElement(coed);
                                    break;

                                default:
                            }
                        }


                        Element parentElement = (Element) eventElement.getParentNode();

                        NodeList coordinatesList = parentElement.getElementsByTagNameNS("*","Coordinates");
                        Element coordinatesElement = (Element) coordinatesList.item(0);
                        x = new Double(coordinatesElement.getAttribute("XCoordinate"));
                        y = new Double(coordinatesElement.getAttribute("YCoordinate"));

                        if (boundary) {
                            process.addChildElement(boundaryElementInstance);

                            // Add new entry in map for sequence flows later
                            idMap.put(parentElement.getAttribute("Id"), boundaryElementInstance.getAttributeValue("id"));

                            plane = DrawShape.drawShape(plane, modelInstance, boundaryElementInstance, x*zoomFactor, y*zoomFactor, 36, 36, true);
                            FlowNodeInfo fni = new FlowNodeInfo(boundaryElementInstance.getAttributeValue("id"), x, y, x*zoomFactor, y*zoomFactor, Event.class.toString(), 36d, 36d);
                            flowNodesMap.put(boundaryElementInstance.getAttributeValue("id"), fni);


                        } else {
                            process.addChildElement(event);

                            // Add new entry in map for sequence flows later
                            idMap.put(parentElement.getAttribute("Id"), event.getId());

                            plane = DrawShape.drawShape(plane, modelInstance, event, x*zoomFactor, y*zoomFactor, 36, 36, true);
                            FlowNodeInfo fni = new FlowNodeInfo(event.getId(), x, y, x*zoomFactor, y*zoomFactor, Event.class.toString(), 36d, 36d);
                            flowNodesMap.put(event.getId(), fni);
                        }
                    }
                }
        }

        for (int i = 0; i < workflowList.getLength(); i++) {
            Element workflowElement = (Element) workflowList.item(i);
            //searchRequest = xpath.compile("//Activities/Activity/Route");
            searchRequest = xpath.compile("//*[local-name() = 'Activities']/*[local-name() = 'Activity']/*[local-name() = 'Route']");
            NodeList gatewayList = (NodeList) searchRequest.evaluate(workflowElement, XPathConstants.NODESET);
            //NodeList gatewayList = activityElement.getElementsByTagName("Route");
            if (workflowElement.getAttribute("Name") != "" || workflowElement.getAttribute("Id") != "") {
                for (int z = 0; z < gatewayList.getLength(); z++) {
                    Element gatewayElement = (Element) gatewayList.item(z);
                    Element parentElement = (Element) gatewayElement.getParentNode();
                    Gateway gateway = modelInstance.newInstance(ExclusiveGateway.class);
                    String gatewayType =  gatewayElement.getAttribute("GatewayType");

                    switch(gatewayType){
                        case "Parallel":
                            gateway = modelInstance.newInstance(ParallelGateway.class);
                            break;

                        case "Inclusive":
                            gateway = modelInstance.newInstance(InclusiveGateway.class);
                            break;

                        default:
                    }

                    process.addChildElement(gateway);
                    gateway.setAttributeValue("name", parentElement.getAttribute("Name"));
                    NodeList coordinatesList = parentElement.getElementsByTagNameNS("*","Coordinates");
                    Element coordinatesElement = (Element) coordinatesList.item(0);
                    x = new Double(coordinatesElement.getAttribute("XCoordinate"));
                    y = new Double(coordinatesElement.getAttribute("YCoordinate"));

                    // Add new entry in map for sequence flows later
                    idMap.put(parentElement.getAttribute("Id"), gateway.getId());

                    plane = DrawShape.drawShape(plane, modelInstance, gateway, x*zoomFactor, y*zoomFactor, 50, 50, true);
                    FlowNodeInfo fni = new FlowNodeInfo(gateway.getId(), x, y, x*zoomFactor, y*zoomFactor, Gateway.class.toString(), gateway.getDiagramElement().getBounds().getHeight(), gateway.getDiagramElement().getBounds().getWidth());
                    flowNodesMap.put(gateway.getId(), fni);
                }
            }
        }
        // Find annotations
        xpath = XPathFactory.newInstance().newXPath();
        //searchRequest = xpath.compile("//Artifacts/Artifact/NodeGraphicsInfos/NodeGraphicsInfo");
        searchRequest = xpath.compile("//*[local-name() = 'Artifacts']/*[local-name() = 'Artifact']/*[local-name() = 'NodeGraphicsInfos']/*[local-name() = 'NodeGraphicsInfo']");
        NodeList artifactList = (NodeList) searchRequest.evaluate(doc, XPathConstants.NODESET);
        for (int i = 0; i < artifactList.getLength(); i++) {
            Element artifactElement = (Element) artifactList.item(i);
            Element parentElement = (Element) artifactElement.getParentNode().getParentNode();

            switch(parentElement.getAttribute("ArtifactType")) {
                case "Annotation":
                    height = 80d; //new Double(artifactElement.getAttribute("Height"));
                    width = 100d; //new Double(artifactElement.getAttribute("Width"));

                    NodeList coordinatesList = parentElement.getElementsByTagNameNS("*","Coordinates");
                    Element coordinatesElement = (Element) coordinatesList.item(0);
                    x = new Double(coordinatesElement.getAttribute("XCoordinate"));
                    y = new Double(coordinatesElement.getAttribute("YCoordinate"));

                    TextAnnotation textA = modelInstance.newInstance(TextAnnotation.class);
                    Text text = modelInstance.newInstance(Text.class);
                    text.setTextContent(parentElement.getAttribute("TextAnnotation"));
                    textA.setText(text);
                    process.addChildElement(textA);

                    idMap.put(parentElement.getAttribute("Id"), textA.getId());
                    plane = DrawShape.drawShape(plane, modelInstance, textA, x*zoomFactor, y*zoomFactor, height, width, true);
                    break;

                default:
            }

        }

        // Draw sequence flows
        for (int i = 0; i < workflowList.getLength(); i++) {
            Element workflowElement = (Element) workflowList.item(i);
            //searchRequest = xpath.compile("//Transitions/Transition");
            searchRequest = xpath.compile("//*[local-name() = 'Transitions']/*[local-name() = 'Transition']");
            NodeList transitionList = (NodeList) searchRequest.evaluate(doc, XPathConstants.NODESET);

            for (int z = 0; z < transitionList.getLength(); z++) {
                Element transitionElement = (Element) transitionList.item(z);

                FlowNode targetFlowNode = modelInstance.getModelElementById(idMap.get(transitionElement.getAttribute("To")));
                FlowNode sourceFlowNode = modelInstance.getModelElementById(idMap.get(transitionElement.getAttribute("From")));

                SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
                process.addChildElement(sequenceFlow);
                sequenceFlow.setSource(sourceFlowNode);
                sequenceFlow.setTarget(targetFlowNode);

                NodeList coordinateList = transitionElement.getElementsByTagNameNS("*","Coordinates");

                System.out.println("source node "+ sourceFlowNode.getDiagramElement().getChildElementsByType(Bounds.class));

                Iterator boundsIter = sourceFlowNode.getDiagramElement().getChildElementsByType(Bounds.class).iterator();

                Double[][] coordinateArray = new Double[2][2];
                int v=0;

                while(boundsIter.hasNext()){
                    Bounds bounds = (Bounds) boundsIter.next();

                    coordinateArray[0][0] = bounds.getX();
                    coordinateArray[0][1] = bounds.getY();
                    //coordinateArray[1][0] = 1000d;
                    //coordinateArray[1][1] = 1000d;

                }

                boundsIter = targetFlowNode.getDiagramElement().getChildElementsByType(Bounds.class).iterator();

                while(boundsIter.hasNext()){
                    Bounds bounds = (Bounds) boundsIter.next();

                    //coordinateArray[0][0] = bounds.getX();
                    //coordinateArray[0][1] = bounds.getY();
                    coordinateArray[1][0] = bounds.getX();
                    coordinateArray[1][1] = bounds.getY();

                }
                //Double[][] coordinateArray = new Double[coordinateList.getLength()][2];


                /*coordinateArray[0][0] = Double.valueOf(sourceFlowNode.getDiagramElement()..getAttributeValue("x"));
                coordinateArray[0][1] = Double.valueOf(sourceFlowNode.getAttributeValue("y"));
                coordinateArray[1][0] = Double.valueOf(targetFlowNode.getAttributeValue("x"));
                coordinateArray[1][1] = Double.valueOf(targetFlowNode.getAttributeValue("y"));

                 */


                /*for (int k = 0; k < coordinateList.getLength(); k++) {
                    Element coordinateElement = (Element) coordinateList.item(k);
                    coordinateArray[k][0] = new Double(coordinateElement.getAttribute("XCoordinate"));
                    coordinateArray[k][1] = new Double(coordinateElement.getAttribute("YCoordinate"));
                }

                 */

                FlowNodeInfo fromFNI = (FlowNodeInfo) flowNodesMap.get(sourceFlowNode.getId());
                FlowNodeInfo toFNI = (FlowNodeInfo) flowNodesMap.get(targetFlowNode.getId());

                plane = DrawFlow.drawFlow(plane, modelInstance, sequenceFlow, fromFNI, toFNI, null, 0d, 0d);
                //plane = DrawFlowFromXPDL.drawFlow(plane, modelInstance, sequenceFlow, coordinateArray);
            }
        }

        // Draw associations

        //searchRequest = xpath.compile("//Associations/Association");
        searchRequest = xpath.compile("//*[local-name() = 'Associations']/*[local-name() = 'Association']");
        NodeList associationList = (NodeList) searchRequest.evaluate(doc, XPathConstants.NODESET);

        for (int z = 0; z < associationList.getLength(); z++) {
            Element associationElement = (Element) associationList.item(z);

            Association association = modelInstance.newInstance(Association.class);
            process.addChildElement(association);

            BaseElement baseESource =  modelInstance.getModelElementById(idMap.get(associationElement.getAttribute("Source")));
            BaseElement baseETarget =  modelInstance.getModelElementById(idMap.get(associationElement.getAttribute("Target")));

            association.setSource(baseESource);
            association.setTarget(baseETarget);

            NodeList coordinateList = associationElement.getElementsByTagNameNS("*","Coordinates");

            Double[][] coordinateArray = new Double[coordinateList.getLength()][2];

            for (int k = 0; k < coordinateList.getLength(); k++) {
                Element coordinateElement = (Element) coordinateList.item(k);
                coordinateArray[k][0] = new Double(coordinateElement.getAttribute("XCoordinate"));
                coordinateArray[k][1] = new Double(coordinateElement.getAttribute("YCoordinate"));
            }

            plane = DrawFlowFromXPDL.drawAssociation(plane, modelInstance, association, coordinateArray);
        }

        Bpmn.validateModel(modelInstance);
        File outputFile = new File(args[1]);
        Bpmn.writeModelToFile(outputFile, modelInstance);


        /*} catch (Exception e) {
            System.out.println("Exception during processing "+e.getMessage());
        } */



    }
}

