package org.camunda.bpmn.generator.transform.draw;

import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnShape;
import org.camunda.bpm.model.bpmn.instance.dc.Bounds;
import org.camunda.bpm.model.xml.ModelInstance;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

public class DrawShape {

  private final BpmnPlane plane;
  private final ModelInstance modelInstance;

  public DrawShape(BpmnPlane plane, ModelInstance modelInstance) {
    this.plane = plane;
    this.modelInstance = modelInstance;
  }

  public boolean draw(ModelElementInstance element, int x, int y, int height, int width, boolean setHorizontal) {
    BpmnShape bpmnShape = modelInstance.newInstance(BpmnShape.class);
    bpmnShape.setBpmnElement((BaseElement) element);

    if (setHorizontal) {
      bpmnShape.setHorizontal(true);
    }

    Bounds bounds = modelInstance.newInstance(Bounds.class);
    bounds.setX(Double.valueOf(x));
    bounds.setY(Double.valueOf(y));
    bounds.setHeight(Double.valueOf(height));
    bounds.setWidth(Double.valueOf(width));
    bpmnShape.setBounds(bounds);

    plane.addChildElement(bpmnShape);

    return true;
  }
}
