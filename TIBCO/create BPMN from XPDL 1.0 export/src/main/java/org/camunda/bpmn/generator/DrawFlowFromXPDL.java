package org.camunda.bpmn.generator;

import org.camunda.bpm.model.bpmn.instance.Association;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnEdge;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
import org.camunda.bpm.model.bpmn.instance.di.Waypoint;
import org.camunda.bpm.model.xml.ModelInstance;

public class DrawFlowFromXPDL {

    public static BpmnPlane drawFlow(BpmnPlane plane, ModelInstance modelInstance, SequenceFlow sequenceFlow, Double[][] coordinateArray) {

        BpmnEdge bpmnEdge = modelInstance.newInstance(BpmnEdge.class);
        BaseElement element = sequenceFlow;
        bpmnEdge.setBpmnElement(element);

        for(int i = 0; i < coordinateArray.length; i++) {
            Waypoint wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(coordinateArray[i][0]);
            wp.setY(coordinateArray[i][1]);
            bpmnEdge.addChildElement(wp);
        }

        plane.addChildElement(bpmnEdge);

        return plane;
    }

    public static BpmnPlane drawAssociation(BpmnPlane plane, ModelInstance modelInstance, Association association, Double[][] coordinateArray) {

        BpmnEdge bpmnEdge = modelInstance.newInstance(BpmnEdge.class);
        BaseElement element = association;
        bpmnEdge.setBpmnElement(element);

        for(int i = 0; i < coordinateArray.length; i++) {
            Waypoint wp = modelInstance.newInstance(Waypoint.class);
            wp.setX(coordinateArray[i][0]);
            wp.setY(coordinateArray[i][1]);
            bpmnEdge.addChildElement(wp);
        }

        plane.addChildElement(bpmnEdge);

        return plane;
    }
}