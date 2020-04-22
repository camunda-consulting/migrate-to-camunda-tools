package org.camunda.bpmn.generator;

import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;

public class BPMNElement {

    private Class type;
    private Double height;
    private Double width;

    public BPMNElement (Class inputType, Double inputHeight, Double inputWidth) {
        type = inputType;
        height = inputHeight;
        width = inputWidth;
    }

    public Class getType() {
        return type;
    }

    public Double getHeight() {
        return height;
    }

    public Double getWidth() {
        return width;
    }
}
