package org.camunda.bpmn.generator;

public class DefinedEvent {

    private Class type;

    public DefinedEvent(Class inputType) {
        type = inputType;
    }

    public Class getType() {
        return type;
    }

}
