package org.camunda.bpmn.generator;

public class FlowNodeInfo {
    private String newFlowNodeID;
    private Double originalX;
    private Double calcX;
    private Double originalY;
    private Double calcY;
    private Double topX;
    private Double topY;
    private Double bottomX;
    private Double bottomY;
    private Double rightX;
    private Double rightY;
    private Double leftX;
    private Double leftY;
    private String type;

    public FlowNodeInfo (String id, Double originalXInput, Double originalYInput, Double calcXInput, Double calcYInput, String typeInput, Double height, Double width) {
        newFlowNodeID = id;
        originalX = originalXInput;
        calcX = calcXInput;
        calcY = calcYInput;
        originalY = originalYInput;
        type = typeInput;

        switch (type) {
            case "interface org.camunda.bpm.model.bpmn.instance.UserTask":
            case "interface org.camunda.bpm.model.bpmn.instance.ServiceTask":
            case "interface org.camunda.bpm.model.bpmn.instance.SendTask":
            case "interface org.camunda.bpm.model.bpmn.instance.ReceiveTask":
            case "interface org.camunda.bpm.model.bpmn.instance.CallActivity":
            case "interface org.camunda.bpm.model.bpmn.instance.ScriptTask":
                topX = calcX + 50;
                topY = calcY;
                rightX = calcX + 100;
                rightY = calcY + 40;
                bottomX = calcX + 50;
                bottomY = calcY + 80;
                leftX = calcX;
                leftY = calcY + 40;
                break;

            case "interface org.camunda.bpm.model.bpmn.instance.ExclusiveGateway":
            case "interface org.camunda.bpm.model.bpmn.instance.InclusiveGateway":
            case "interface org.camunda.bpm.model.bpmn.instance.ParallelGateway":
            case "interface org.camunda.bpm.model.bpmn.instance.EventBasedGateway":
            case "interface org.camunda.bpm.model.bpmn.instance.ComplexGateway":
                topX = calcX + 25;
                topY = calcY;
                rightX = calcX + 50;
                rightY = calcY + 25;
                bottomX = calcX + 25;
                bottomY = calcY + 50;
                leftX = calcX;
                leftY = calcY + 25;
                break;

            case "interface org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent":
            case "interface org.camunda.bpm.model.bpmn.instance.StartEvent":
            case "interface org.camunda.bpm.model.bpmn.instance.EndEvent":
            case "interface org.camunda.bpm.model.bpmn.instance.BoundaryEvent":
                topX = calcX + 18;
                topY = calcY;
                rightX = calcX + 36;
                rightY = calcY + 18;
                bottomX = calcX + 18;
                bottomY = calcY + 36;
                leftX = calcX;
                leftY = calcY + 18;
                break;

            case "interface org.camunda.bpm.model.bpmn.instance.SubProcess":
                topX = calcX + (width/2);
                topY = calcY;
                rightX = calcX + width;
                rightY = calcY + (height/2);
                bottomX = calcX + (width/2);
                bottomY = calcY + height;
                leftX = calcX;
                leftY = calcY + (height/2);
                break;

            default:

        }
    }

    public String getNewId() {
        return newFlowNodeID;
    }

    public Double getOriginalX() {
        return originalX;
    }

    public Double getOriginalY() {
        return originalY;
    }

    public Double getCalcX() {
        return calcX;
    }

    public Double getCalcY() {
        return calcY;
    }

    public Double getTopX() {
        return topX;
    }

    public Double getTopY() {
        return topY;
    }

    public Double getRightX() {
        return rightX;
    }

    public Double getRightY() {
        return rightY;
    }

    public Double getBottomX() {
        return bottomX;
    }

    public Double getBottomY() {
        return bottomY;
    }

    public Double getLeftX() {
        return leftX;
    }

    public Double getLeftY() {
        return leftY;
    }

    public String getType() {
        return type;
    }

}
