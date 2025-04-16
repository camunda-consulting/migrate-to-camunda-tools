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
import org.camunda.bpm.model.bpmn.instance.dc.Bounds;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.HashMap;

public class BPMNGenFrom1dot0XPDL {
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

        // Create another hash map for boundary events since they use the same id as the node they are attached to. This will help later when adding and drawing sequence flows.
        HashMap<String, Object> boundaryMap = new HashMap<>();

        // Create hash of flow nodes for drawing of sequence flows later
        HashMap< String, Object > flowNodesMap = new HashMap<>();

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

        XPathExpression searchRequest = null;
        XPath xpath = XPathFactory.newInstance().newXPath();

        Double width = new Double(0);
        Double height = new Double(0);
        Double x = new Double(0);
        Double y = new Double(0);


        // Add process to defs
        definitions.addChildElement(process);
        plane.setBpmnElement(process);
        //searchRequest = xpath.compile("//WorkflowProcesses/WorkflowProcess");
        searchRequest = xpath.compile("//*[local-name() = 'WorkflowProcesses']/*[local-name() = 'WorkflowProcess']");
        NodeList workflowList = (NodeList) searchRequest.evaluate(doc, XPathConstants.NODESET);

        System.out.println("WF list length - "+workflowList.getLength());

        for (int i = 0; i < workflowList.getLength(); i++) {
            Element workflowElement = (Element) workflowList.item(i);
            //searchRequest = xpath.compile("//Activities/Activity/Implementation");
            searchRequest = xpath.compile("//*[local-name() = 'Activities']/*[local-name() = 'Activity']");
            NodeList activityList = (NodeList) searchRequest.evaluate(workflowElement, XPathConstants.NODESET);

            System.out.println("Activities list length - "+activityList.getLength());

            for (int z = 0; z < activityList.getLength(); z++) {
                Element activityElement = (Element) activityList.item(z);
                //searchRequest = xpath.compile("//*[local-name() = 'ExtendedAttributes']/*[local-name() = 'ExtendedAttribute']");
                //NodeList xAttrList = (NodeList) searchRequest.evaluate(activityElement, XPathConstants.NODESET);

                //Find X and Y
                NodeList xAttrs = activityElement.getElementsByTagName("ExtendedAttribute");
                String xOffset = "";
                String yOffset = "";
                for (int j = 0; j < xAttrs.getLength(); j++) {
                    Element xAttr = (Element) xAttrs.item(j);

                    switch (xAttr.getAttribute("Name")) {
                        case ("XOffset"):
                            xOffset = xAttr.getAttribute("Value");
                            x = new Double(xAttr.getAttribute("Value"));
                            break;

                        case ("YOffset"):
                            yOffset = xAttr.getAttribute("Value");
                            y = new Double(xAttr.getAttribute("Value"));
                            break;
                    }


                }

                Double zoomFactor = 2.5d;

                if(!activityElement.getAttribute("Name").equals("Condition_Router")) {
                    Task task = modelInstance.newInstance(Task.class);
                    task.setAttributeValue("name", activityElement.getAttribute("Name"));
                    process.addChildElement(task);
                    idMap.put(activityElement.getAttribute("Id"), task.getId());
                    plane = DrawShape.drawShape(plane, modelInstance, task, x*zoomFactor, y*zoomFactor, 80, 100, true);
                    FlowNodeInfo fni = new FlowNodeInfo(task.getAttributeValue("id"), x, y, x*zoomFactor, y*zoomFactor, Task.class.toString(), task.getDiagramElement().getBounds().getHeight(), task.getDiagramElement().getBounds().getWidth());
                    System.out.println(activityElement.getAttribute("Id") + " - " + fni.getLeftX() +", "+fni.getRightX());
                    flowNodesMap.put(activityElement.getAttribute("Id"), fni);
                } else {
                    Gateway gateway = modelInstance.newInstance(ExclusiveGateway.class);
                    //gateway.setAttributeValue("name", activityElement.getAttribute("Name"));
                    process.addChildElement(gateway);
                    idMap.put(activityElement.getAttribute("Id"), gateway.getId());
                    plane = DrawShape.drawShape(plane, modelInstance, gateway, x*zoomFactor, y*zoomFactor, 50, 50, true);
                    FlowNodeInfo fni = new FlowNodeInfo(gateway.getAttributeValue("id"), x, y, x*zoomFactor, y*zoomFactor, Gateway.class.toString(), gateway.getDiagramElement().getBounds().getHeight(), gateway.getDiagramElement().getBounds().getWidth());
                    flowNodesMap.put(activityElement.getAttribute("Id"), fni);
                }

                System.out.println("Activity - "+activityElement.getAttribute("Id") +", "+activityElement.getAttribute("Name")
                        + ", XOffset - " + xOffset + ", YOffset - " + yOffset + ", X - " + x + ", Y - " + y);


        }


            // Now get info for sequence flows aka transitions
            searchRequest = xpath.compile("//*[local-name() = 'Transitions']/*[local-name() = 'Transition']");
            NodeList transitionList = (NodeList) searchRequest.evaluate(doc, XPathConstants.NODESET);

            System.out.println("Transitions list length - "+transitionList.getLength());
            for (int q = 0; q < transitionList.getLength(); q++) {
                Element transitionElement = (Element) transitionList.item(q);

                System.out.println("Transition - "+transitionElement.getAttribute("From") + "," + transitionElement.getAttribute("To"));


                FlowNode targetFlowNode = modelInstance.getModelElementById(idMap.get(transitionElement.getAttribute("To")));
                FlowNode sourceFlowNode = modelInstance.getModelElementById(idMap.get(transitionElement.getAttribute("From")));

                SequenceFlow sequenceFlow = modelInstance.newInstance(SequenceFlow.class);
                process.addChildElement(sequenceFlow);
                sequenceFlow.setSource(sourceFlowNode);
                sequenceFlow.setTarget(targetFlowNode);

                //NodeList coordinateList = transitionElement.getElementsByTagNameNS("*","Coordinates");

                //System.out.println("source flow node " + sourceFlowNode.getDiagramElement().getChildElementsByType(Bounds.class).iterator().next().getAttributeValue("x"));

                System.out.println("From side - "+transitionElement.getElementsByTagName("FromSide").item(0).getTextContent());

                Double fromX = 0d;
                Double fromY = 0d;
                Double toX = 0d;
                Double toY = 0d;

                FlowNodeInfo fromFNI = (FlowNodeInfo) flowNodesMap.get(transitionElement.getAttribute("From"));
                String fromSide = transitionElement.getElementsByTagName("FromSide").item(0).getTextContent();

                switch (fromSide) {
                    case("right"):
                        fromX = fromFNI.getRightX();
                        fromY = fromFNI.getRightY();
                        break;

                    case("bottom"):
                        fromX = fromFNI.getBottomX();
                        fromY = fromFNI.getBottomY();
                        break;

                    case("left"):
                        fromX = fromFNI.getLeftX();
                        fromY = fromFNI.getLeftY();
                        break;

                    case("top"):
                        fromX = fromFNI.getTopX();
                        fromY = fromFNI.getTopY();
                        break;

                }

                System.out.println("To side - "+transitionElement.getElementsByTagName("ToSide").item(0).getTextContent());

                FlowNodeInfo toFNI = (FlowNodeInfo) flowNodesMap.get(transitionElement.getAttribute("To"));
                String toSide = transitionElement.getElementsByTagName("ToSide").item(0).getTextContent();

                switch (toSide) {
                    case("left"):
                        toX = toFNI.getLeftX();
                        toY = toFNI.getLeftY();
                        break;

                    case("top"):
                        toX = toFNI.getTopX();
                        toY = toFNI.getTopY();
                        break;

                    case("bottom"):
                        toX = toFNI.getBottomX();
                        toY = toFNI.getBottomY();
                        break;

                    case("right"):
                        toX = toFNI.getRightX();
                        toY = toFNI.getRightY();
                        break;

                }


                Double[][] coordinateArray = new Double[2][2];
                // From
                System.out.println("From X - "+fromX+", From Y - "+fromY);
                coordinateArray[0][0] = fromX;
                coordinateArray[0][1] = fromY;
                // To
                System.out.println("To X - "+toX+", To Y - "+toY);
                coordinateArray[1][0] = toX;
                coordinateArray[1][1] = toY;

                plane = DrawFlow.drawFlow(plane, modelInstance, sequenceFlow, fromFNI, toFNI, null, 0d, 0d);
                //plane = DrawFlowFromXPDL.drawFlow(plane, modelInstance, sequenceFlow, coordinateArray);

            }
        Bpmn.validateModel(modelInstance);
        File outputFile = new File(args[1]);
        Bpmn.writeModelToFile(outputFile, modelInstance);




        /*} catch (Exception e) {
            System.out.println("Exception during processing "+e.getMessage());
        } */



    }
    }
}

