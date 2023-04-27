package org.camunda.bpmn.generator;

public class PegaToBPMNElement {

    private Class type;
    private Double height;
    private Double width;

    public PegaToBPMNElement(Class inputType, Double inputHeight, Double inputWidth) {
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
