package org.camunda.bpmn.generator.transform.draw;

public class FlowNodeInfo {
    private final String flowNodeID;
    private final Double xCoordinate;
    private final Double yCoordinate;
    private final String type;

    public FlowNodeInfo(String id, Double x, Double y, String typeInput) {
        flowNodeID = id;
        xCoordinate = x;
        yCoordinate = y;
        type = typeInput;
    }

    public String getId() {
        return flowNodeID;
    }

    public Double getX() {
        return xCoordinate;
    }

    public Double getY() {
        return yCoordinate;
    }

    public String getType() {
        return type;
    }
}
