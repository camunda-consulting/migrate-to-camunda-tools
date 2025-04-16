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

        Double sourceX = source.getCalcX();
        Double sourceY = source.getCalcY();

        Double targetX = target.getCalcX();
        Double targetY = target.getCalcY();

        // Target is below and to the right of the source

        //if(targetX > sourceX && (targetY - sourceY) > 1) {
        // Target is to the right of the source
        if(targetX > sourceX) {

            Waypoint wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(source.getRightX());
            wp.setY(source.getRightY());
            bpmnEdge.addChildElement(wp);

            wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(source.getRightX() + ((target.getLeftX()-source.getRightX())/2));
            wp.setY(source.getRightY());
            bpmnEdge.addChildElement(wp);

            wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(source.getRightX() + ((target.getLeftX()-source.getRightX())/2));
            wp.setY(target.getRightY());
            bpmnEdge.addChildElement(wp);

            wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(target.getLeftX());
            wp.setY(target.getLeftY());
            bpmnEdge.addChildElement(wp);

            plane.addChildElement(bpmnEdge);
            return plane;
        }


        // Target is below the source and not really to the right of it
        if(targetY > sourceY && Math.abs(targetX - sourceX) <= 1) {
        //if(targetY > sourceY && (targetX - sourceX) <= 1) {
            Waypoint wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(source.getBottomX());
            wp.setY(source.getBottomY());
            bpmnEdge.addChildElement(wp);

            wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(source.getBottomX());
            wp.setY(source.getBottomY() + (target.getTopY() - source.getBottomY())/2);
            bpmnEdge.addChildElement(wp);

            wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(target.getTopX());
            wp.setY(source.getBottomY() + (target.getTopY() - source.getBottomY())/2);
            bpmnEdge.addChildElement(wp);

            wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(target.getTopX());
            wp.setY(target.getTopY());
            bpmnEdge.addChildElement(wp);

            plane.addChildElement(bpmnEdge);
            return plane;
        }


        // Target is above and to the right of the source
        if(targetX > sourceX && (targetY - sourceY) < -1) {

            Waypoint wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(source.getRightX());
            wp.setY(source.getRightY());
            bpmnEdge.addChildElement(wp);

            wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(source.getRightX() + ((target.getLeftX()-source.getRightX())/2));
            wp.setY(source.getRightY());
            bpmnEdge.addChildElement(wp);

            wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(source.getRightX() + ((target.getLeftX()-source.getRightX())/2));
            wp.setY(target.getRightY());
            bpmnEdge.addChildElement(wp);

            wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(target.getLeftX());
            wp.setY(target.getLeftY());
            bpmnEdge.addChildElement(wp);

            plane.addChildElement(bpmnEdge);
            return plane;
        }

        // Target is below and to the right of the source
        if(targetX > sourceX && (targetY - sourceY) < -1) {
            Waypoint wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(source.getRightX());
            wp.setY(source.getRightY());
            bpmnEdge.addChildElement(wp);

            wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(source.getRightX() + ((target.getLeftX()-source.getRightX())/2));
            wp.setY(source.getRightY());
            bpmnEdge.addChildElement(wp);

            wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(source.getRightX() + ((target.getLeftX()-source.getRightX())/2));
            wp.setY(target.getRightY());
            bpmnEdge.addChildElement(wp);

            wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(target.getLeftX());
            wp.setY(target.getLeftY());
            bpmnEdge.addChildElement(wp);

            plane.addChildElement(bpmnEdge);
            return plane;
        }

        // Target is above and to the left of the source
        if(targetX < sourceX && (targetY - sourceY) <= -1) {
            Waypoint wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(source.getTopX());
            wp.setY(source.getTopY());
            bpmnEdge.addChildElement(wp);

            wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(source.getTopX());
            wp.setY(source.getTopY() - ((source.getTopY()-target.getBottomY())/2));
            bpmnEdge.addChildElement(wp);

            wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(target.getBottomX());
            wp.setY(target.getBottomY() + ((source.getTopY()-target.getBottomY())/2));
            bpmnEdge.addChildElement(wp);

            wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(target.getBottomX());
            wp.setY(target.getBottomY());
            bpmnEdge.addChildElement(wp);

            plane.addChildElement(bpmnEdge);
            return plane;
        }

        // Target is below and to the left of the source
        if(targetX < sourceX && (targetY - sourceY) >= 50) {
            Waypoint wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(source.getBottomX());
            wp.setY(source.getBottomY());
            bpmnEdge.addChildElement(wp);

            wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(source.getBottomX());
            wp.setY(source.getBottomY() - ((source.getBottomY()-target.getTopY())/2));
            bpmnEdge.addChildElement(wp);

            wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(target.getTopX());
            wp.setY(target.getTopY() + ((source.getBottomY()-target.getTopY())/2));
            bpmnEdge.addChildElement(wp);

            wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(target.getTopX());
            wp.setY(target.getTopY());
            bpmnEdge.addChildElement(wp);

            plane.addChildElement(bpmnEdge);
            return plane;
        }

        // Target is to the left of the source but not really above or below
        if(targetX < sourceX && (Math.abs(targetY - sourceY) < 50)) {
            Waypoint wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(source.getBottomX());
            wp.setY(source.getBottomY());
            bpmnEdge.addChildElement(wp);

            wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(source.getBottomX());
            wp.setY(source.getBottomY() + 50);
            bpmnEdge.addChildElement(wp);

            wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(target.getBottomX());
            wp.setY(source.getBottomY() + 50);
            bpmnEdge.addChildElement(wp);

            wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(target.getBottomX());
            wp.setY(target.getBottomY());
            bpmnEdge.addChildElement(wp);

            plane.addChildElement(bpmnEdge);
            return plane;
        }

        // If none of the above matches then simply draw a sequence flow from the right side of the source to the left side of the target
        Waypoint wp = modelInstance.newInstance(Waypoint.class);
        wp.setX(source.getRightX());
        wp.setY(source.getRightY());
        bpmnEdge.addChildElement(wp);

        wp = modelInstance.newInstance(Waypoint.class);
        wp.setX(target.getLeftX());
        wp.setY(target.getLeftY());
        bpmnEdge.addChildElement(wp);

        plane.addChildElement(bpmnEdge);

        plane.addChildElement(bpmnEdge);

        return plane;
    }

}

