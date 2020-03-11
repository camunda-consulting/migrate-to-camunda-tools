package org.camunda.bpmn.generator;

import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnEdge;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
import org.camunda.bpm.model.bpmn.instance.di.Waypoint;
import org.camunda.bpm.model.xml.ModelInstance;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;


public class DrawFlow {

    public static BpmnPlane drawFlow(BpmnPlane plane, ModelInstance modelInstance, SequenceFlow sequenceFlow, FlowNodeInfo source, FlowNodeInfo target, NodeList sequenceCurveList, Double subProcessXOffset, Double subProcessYOffset) {

        BpmnEdge bpmnEdge = modelInstance.newInstance(BpmnEdge.class);
        BaseElement element = sequenceFlow;
        bpmnEdge.setBpmnElement(element);

        // Get the original x,y coordinates of source and target to determine relative positions
        Double sourceOriginalX = source.getOriginalX();
        Double sourceOriginalY = source.getOriginalY();

        Double targetOriginalX = target.getOriginalX();
        Double targetOriginalY = target.getOriginalY();

        Double curveX = subProcessXOffset;
        Double curveY = subProcessYOffset;

        if(sequenceCurveList.getLength() > 0) { // curve found
            Element sequenceCurveElement = (Element) sequenceCurveList.item(0);
            curveX += new Double(sequenceCurveElement.getAttribute("x"));
            curveY += new Double(sequenceCurveElement.getAttribute("y"));

            if(curveY > sourceOriginalY) { // Curve is below
                Waypoint wp = modelInstance.newInstance(Waypoint.class);
                wp.setX(source.getBottomX());
                wp.setY(source.getBottomY());
                bpmnEdge.addChildElement(wp);

                wp = modelInstance.newInstance(Waypoint.class);
                wp.setX(source.getBottomX());
                wp.setY(curveY);
                bpmnEdge.addChildElement(wp);

                wp = modelInstance.newInstance(Waypoint.class);
                wp.setX(target.getBottomX());
                wp.setY(curveY);
                bpmnEdge.addChildElement(wp);

                wp = modelInstance.newInstance(Waypoint.class);
                wp.setX(target.getBottomX());
                wp.setY(target.getBottomY());
                bpmnEdge.addChildElement(wp);

                plane.addChildElement(bpmnEdge);

            }

            if(curveY < sourceOriginalY) { // Curve is above
                Waypoint wp = modelInstance.newInstance(Waypoint.class);
                wp.setX(source.getTopX());
                wp.setY(source.getTopY());
                bpmnEdge.addChildElement(wp);

                wp = modelInstance.newInstance(Waypoint.class);
                wp.setX(source.getTopX());
                wp.setY(curveY);
                bpmnEdge.addChildElement(wp);

                wp = modelInstance.newInstance(Waypoint.class);
                wp.setX(target.getTopX());
                wp.setY(curveY);
                bpmnEdge.addChildElement(wp);

                wp = modelInstance.newInstance(Waypoint.class);
                wp.setX(target.getTopX());
                wp.setY(target.getTopY());
                bpmnEdge.addChildElement(wp);

                plane.addChildElement(bpmnEdge);
            }

        } else { // No curve found

            if (targetOriginalX > sourceOriginalX) { // target is to the right of source
                if (targetOriginalY.equals(sourceOriginalY)) { // target is on the same level
                    Waypoint wp = modelInstance.newInstance(Waypoint.class);
                    wp.setX(source.getRightX());
                    wp.setY(source.getRightY());
                    bpmnEdge.addChildElement(wp);

                    wp = modelInstance.newInstance(Waypoint.class);
                    wp.setX(target.getLeftX());
                    wp.setY(target.getLeftY());
                    bpmnEdge.addChildElement(wp);

                    plane.addChildElement(bpmnEdge);
                }

                if (targetOriginalY > sourceOriginalY) { // target is below and to the right of the source
                    Waypoint wp = modelInstance.newInstance(Waypoint.class);
                    wp.setX(source.getBottomX());
                    wp.setY(source.getBottomY());
                    bpmnEdge.addChildElement(wp);

                    wp = modelInstance.newInstance(Waypoint.class);
                    wp.setX(source.getBottomX());
                    wp.setY(target.getLeftY());
                    bpmnEdge.addChildElement(wp);

                    wp = modelInstance.newInstance(Waypoint.class);
                    wp.setX(target.getLeftX());
                    wp.setY(target.getLeftY());
                    bpmnEdge.addChildElement(wp);

                    plane.addChildElement(bpmnEdge);

                }

                if (targetOriginalY < sourceOriginalY) { // target is above and to the right of the source
                    Waypoint wp = modelInstance.newInstance(Waypoint.class);
                    wp.setX(source.getRightX());
                    wp.setY(source.getRightY());
                    bpmnEdge.addChildElement(wp);

                    wp = modelInstance.newInstance(Waypoint.class);
                    wp.setX(target.getBottomX());
                    wp.setY(source.getRightY());
                    bpmnEdge.addChildElement(wp);

                    wp = modelInstance.newInstance(Waypoint.class);
                    wp.setX(target.getBottomX());
                    wp.setY(target.getBottomY());
                    bpmnEdge.addChildElement(wp);

                    plane.addChildElement(bpmnEdge);

                }

            }
        }
        //bpmnEdge = determineAdditionalWaypoints(bpmnEdge, modelInstance, startX, startY, sourcePosition, endX, endY, targetPosition);



        return plane;
    }

    private static BpmnEdge determineAdditionalWaypoints(BpmnEdge bpmnEdge, ModelInstance modelInstance, Double startX, Double startY, String sourcePosition, Double endX, Double endY, String targetPosition) {

        if (endY > startY) { // target is below source
            switch (sourcePosition) {
                case "rightTop":
                case "rightCenter":
                case "rightBottom":
                case "bottomRight":
                case "bottomCenter":
                case "bottomLeft":
                case "topCenter": // Boundary event
                    switch (targetPosition) {

                        // The 'normal' case where the target is on the left hand side of the flow node
                        case "leftTop":
                        case "leftCenter":
                        case "leftBottom":
                            // create a waypoint that's the x of the source and the y of the target
                            Waypoint wp = modelInstance.newInstance(Waypoint.class);
                            wp.setX(startX);
                            wp.setY(endY);
                            bpmnEdge.addChildElement(wp);
                        break;

                        case "topLeft":
                        case "topCenter":
                        case "topRight":
                            // create two waypoints in addition to the end point
                            wp = modelInstance.newInstance(Waypoint.class);
                            wp.setX(startX);
                            wp.setY(((endY-startY)/2) + startY);
                            bpmnEdge.addChildElement(wp);

                            wp = modelInstance.newInstance(Waypoint.class);
                            wp.setX(endX);
                            wp.setY(((endY-startY)/2) + startY);
                            bpmnEdge.addChildElement(wp);

                            wp = modelInstance.newInstance(Waypoint.class);
                            wp.setX(endX);
                            wp.setY(endY-1);
                            bpmnEdge.addChildElement(wp);
                            break;

                        default:
                    }
                    break;

                default:
            }
        } else  { // target is above source

            switch (sourcePosition) {
                case "rightTop":
                case "rightCenter":
                case "rightBottom":
                case "bottomCenter": // Boundary event
                    switch (targetPosition) {

                        case "leftTop":
                        case "leftCenter":
                        case "leftBottom":
                            // create two waypoints
                            Waypoint wp = modelInstance.newInstance(Waypoint.class);
                            wp.setX(((endX - startX)/2) + startX);
                            wp.setY(startY);
                            bpmnEdge.addChildElement(wp);

                            wp = modelInstance.newInstance(Waypoint.class);
                            wp.setX(((endX - startX)/2) + startX);
                            wp.setY(endY);
                            bpmnEdge.addChildElement(wp);

                            wp = modelInstance.newInstance(Waypoint.class);
                            wp.setX(endX);
                            wp.setY(endY);
                            bpmnEdge.addChildElement(wp);
                            break;

                        case "bottomLeft":
                        case "bottomCenter":
                        case "bottomRight":
                            wp = modelInstance.newInstance(Waypoint.class);
                            wp.setX(endX);
                            wp.setY(startY);
                            bpmnEdge.addChildElement(wp);
                            break;

                        default:
                    }
                    break;

                case "topRight":
                case "topCenter":
                case "topLeft":

                    switch (targetPosition) {

                        case "leftTop":
                        case "leftCenter":
                        case "leftBottom":
                            // create a waypoint that's the y of the source and the x of the target
                            Waypoint wp = modelInstance.newInstance(Waypoint.class);
                            wp.setX(startX);
                            wp.setY(endY);
                            bpmnEdge.addChildElement(wp);
                            break;

                        case "bottomLeft":
                        case "bottomCenter":
                        case "bottomRight":
                            //
                            wp = modelInstance.newInstance(Waypoint.class);
                            wp.setX(endX);
                            wp.setY(startY);
                            bpmnEdge.addChildElement(wp);
                            break;

                        default:
                    }
                    break;
                default:
            }

        }

        return bpmnEdge;
    }
}

