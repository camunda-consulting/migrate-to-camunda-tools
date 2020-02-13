package org.camunda.bpmn.generator;

public class FlowNodeInfo {
    private String flowNodeID;
    private Double xCoordinate;
    private Double yCoordinate;
    private String type;

    public FlowNodeInfo (String id, Double x, Double y, String typeInput) {
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
