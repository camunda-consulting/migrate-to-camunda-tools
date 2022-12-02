package org.camunda.bpmn.generator.transform;

import org.camunda.bpmn.generator.process.BpmnDiagramToTransform;
import org.camunda.bpmn.generator.report.Report;

public interface TransformationBpmnInt {

  String getName();

  /**
   * Apply a transformation
   *
   * @param diagram progress to transform
   * @return the diagram transformed
   */
  BpmnDiagramToTransform apply(BpmnDiagramToTransform diagram, Report report);

  String getReportOperations();

}
