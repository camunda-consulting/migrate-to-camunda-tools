package org.camunda.bpmn.generator.verify;

import org.camunda.bpmn.generator.process.BpmnDiagramTransport;
import org.camunda.bpmn.generator.report.Report;

public interface VerificationInt {

  String getName();

  /**
   * Apply a transformation
   *
   * @param diagram progress to verify
   * @return if OK or not. errors are describes in the getReportVerification()
   */
  boolean isOk(BpmnDiagramTransport diagram, Report report);

  String getReportVerification();
}
