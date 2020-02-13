package org.camunda.bpmn.generator;

import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnEdge;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
import org.camunda.bpm.model.bpmn.instance.di.Waypoint;
import org.camunda.bpm.model.xml.ModelInstance;


public class DrawFlow {

    public static BpmnPlane drawFlow(BpmnPlane plane, ModelInstance modelInstance, SequenceFlow sequenceFlow, String sourceType, String sourcePosition, Double sourceX, Double sourceY, String targetType, String targetPosition, Double targetX, Double targetY) {

        Double startX = new Double(0);
        Double startY= new Double(0);
        Double endX = new Double(100);
        Double endY = new Double(100);

        switch (sourceType) {

            case "Activity":
                startX = determineXForActivity(sourceX, sourcePosition);
                startY = determineYForActivity(sourceY, sourcePosition);
                break;

            case "Gateway":
                startX = determineXForGateway(sourceX, sourcePosition);
                startY = determineYForGateway(sourceY, sourcePosition);
                break;

            case "Event":
                startX = determineXForEvent(sourceX, sourcePosition);
                startY = determineYForEvent(sourceY, sourcePosition);
                break;

            case "BoundaryEvent":
                startX = determineXForBoundaryEvent(sourceX, sourcePosition);
                startY = determineYForBoundaryEvent(sourceY, sourcePosition);
                break;

            default:
        }

        switch (targetType) {

            case "Activity":
                endX = determineXForActivity(targetX, targetPosition);
                endY = determineYForActivity(targetY, targetPosition);
                break;

            case "Gateway":
                endX = determineXForGateway(targetX, targetPosition);
                endY = determineYForGateway(targetY, targetPosition);
                break;

            case "Event":
                endX = determineXForEvent(targetX, targetPosition);
                endY = determineYForEvent(targetY, targetPosition);
                break;

            default:
        }

        BpmnEdge bpmnEdge = modelInstance.newInstance(BpmnEdge.class);
        BaseElement element = sequenceFlow;
        bpmnEdge.setBpmnElement(element);

        Waypoint wp = modelInstance.newInstance(Waypoint.class);
        wp.setX(startX);
        wp.setY(startY);
        bpmnEdge.addChildElement(wp);

        bpmnEdge = determineAdditionalWaypoints(bpmnEdge, modelInstance, startX, startY, sourcePosition, endX, endY, targetPosition);

        wp = modelInstance.newInstance(Waypoint.class);
        wp.setX(endX);
        wp.setY(endY);
        bpmnEdge.addChildElement(wp);

        plane.addChildElement(bpmnEdge);

        return plane;
    }

    private static Double determineXForActivity(Double x, String position) {
        Double calcX = new Double(0);

        switch (position) {

            case "topCenter":
            case "bottomCenter":
                calcX = x + 50;
                break;

            case "topRight":
            case "bottomRight":
                calcX = x + 75;
                break;

            case "topLeft":
            case "bottomLeft":
                calcX = x + 25;
                break;

            case "rightTop":
            case "rightCenter":
            case "rightBottom":
                calcX = x + 100;
                break;

            case "leftTop":
            case "leftCenter":
            case "leftBottom":
                calcX = x;
                break;

            default:
        }

        return calcX;
    }

    private static Double determineYForActivity(Double y, String position) {
        Double calcY = new Double(0);

        switch (position) {

            case "topCenter":
            case "topRight":
            case "topLeft":
                calcY = y;
                break;

            case "bottomCenter":
            case "bottomRight":
            case "bottomLeft":
                calcY = y + 80;
                break;

            case "rightTop":
            case "leftTop":
                calcY = y + 25;
                break;

            case "rightCenter":
            case "leftCenter":
                calcY = y + 38;
                break;

            case "rightBottom":
            case "leftBottom":
                calcY = y + 75;
                break;

            default:
        }

        return calcY;
    }

    private static Double determineXForGateway(Double x, String position) {
        Double calcX = new Double(0);

        switch (position) {

            case "leftCenter":
                calcX = x;
                break;

            case "topCenter":
            case "bottomCenter":
                calcX = x + 25;
                break;

            case "rightCenter":
                calcX = x + 50;
                break;

            default:
        }

        return calcX;
    }

    private static Double determineYForGateway(Double y, String position) {
        Double calcY = new Double(0);

        switch (position) {

            case "topCenter":
                calcY = y;
                break;

            case "rightCenter":
            case "leftCenter":
                calcY = y + 25;
                break;


            case "bottomCenter":
                calcY = y + 50;
                break;

            default:
        }

        return calcY;
    }

    private static Double determineXForEvent(Double x, String position) {
        Double calcX = new Double(0);

        switch (position) {

            case "leftCenter":
                calcX = x;
                break;

            case "topCenter":
            case "bottomCenter":
                calcX = x + 18;
                break;

            case "rightCenter":
                calcX = x + 36;
                break;

            default:
        }

        return calcX;
    }

    private static Double determineYForEvent(Double y, String position) {
        Double calcY = new Double(0);

        switch (position) {

            case "topCenter":
                calcY = y;
                break;

            case "rightCenter":
            case "leftCenter":
                calcY = y + 18;
                break;


            case "bottomCenter":
                calcY = y + 36;
                break;

            default:
        }

        return calcY;
    }

    private static Double determineXForBoundaryEvent(Double x, String position) {

        Double calcX = new Double(0);

        switch (position) {

            case "bottomCenter":
                calcX = x + 18;
                break;

            case "rightCenter":
            case "leftCenter":
                calcX = x + 18;
                break;

            case "topCenter":
                calcX = x + 18;
                break;

            default:
        }

        return calcX;
    }

    private static Double determineYForBoundaryEvent(Double y, String position) {

        Double calcY = new Double(0);

        switch (position) {

            case "leftCenter":
            case "rightCenter":
                calcY = y + 18;
                break;

            case "topCenter":
                calcY = y;
                break;

            case "bottomCenter":
                calcY = y + 36;
                break;

            default:
        }

        return calcY;
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

