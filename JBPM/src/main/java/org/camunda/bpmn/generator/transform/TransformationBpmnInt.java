package org.camunda.bpmn.generator.transform;

import org.camunda.bpmn.generator.process.DiagramBPMN;
import org.camunda.bpmn.generator.report.Report;

public interface TransformationBpmnInt {

  String getName();

  /**
   * Apply a transformation
   *
   * @param diagram progress to transform
   * @return the diagram transformed
   */
  DiagramBPMN apply(DiagramBPMN diagram, Report report);

  String getReportOperations();

}
