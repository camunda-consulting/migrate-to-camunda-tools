package org.camunda.bpmn.generator.transform;

import org.camunda.bpmn.generator.transform.draw.TransformationDraw;

import java.util.Arrays;
import java.util.List;

public class TransformFactory {

  private final static TransformFactory transformFactory = new TransformFactory();

  public static TransformFactory getInstance() {
    return transformFactory;
  }

  public List<TransformationBpmnInt> getTransformers() {
    return Arrays.asList(
        new TransformationFEEL(),
        new TransformationDraw());
  }
}
