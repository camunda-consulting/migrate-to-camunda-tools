package org.camunda.bpmn.generator;

import org.camunda.bpmn.generator.process.BpmnDiagramTransport;
import org.camunda.bpmn.generator.report.Report;
import org.camunda.bpmn.generator.transform.TransformFactory;
import org.camunda.bpmn.generator.transform.TransformationBpmnInt;
import org.camunda.bpmn.generator.verify.VerificationFactory;
import org.camunda.bpmn.generator.verify.VerificationInt;

import java.io.File;

public class Pilot {

  Report report;

  public Pilot(Report report) {
    this.report = report;
  }

  public void process(File pathIn, File pathOut) {
    String[] listProcessFile = pathIn.list();
    if (listProcessFile == null)
      return;
    report.info("Found  " + listProcessFile.length + " files in [" + pathIn.getAbsolutePath() + "]");

    for (String oneProcessFile : listProcessFile) {
      if (!oneProcessFile.endsWith(".bpmn")) {
        report.error("Can't transform file[" + oneProcessFile + "] : must be end by .bpmn");
        continue;
      }
      report.info("------------ Manage[" + oneProcessFile + "]");
      BpmnDiagramTransport diagramBPMN = new BpmnDiagramTransport(report);
      try {
        diagramBPMN.read(new File(pathIn + "/" + oneProcessFile));
        Report.Operation processOperation = report.startOperation("transformation");
        executeTransformations(diagramBPMN);
        report.endOperation("  -- End transformation", processOperation);

        Report.Operation processVerification = report.startOperation("verification");
        executeVerifications(diagramBPMN);
        report.endOperation("  -- End verification ", processVerification);
        report.endOperation("------------ End process [" + oneProcessFile + "]", processVerification);
        diagramBPMN.write(pathOut);

      } catch (Exception e) {
        // already logged
      }

    }
    report.logAllOperations();
    report.info("End, process produced in  [" + pathOut.getAbsolutePath() + "]");
  }

  private void executeTransformations(BpmnDiagramTransport diagramBPMN) {
    try {
      TransformFactory transformFactory = TransformFactory.getInstance();
      for (TransformationBpmnInt transformer : transformFactory.getTransformers()) {
        Report.Operation operation = report.startOperation("Transformation " + transformer.getName());

        diagramBPMN = transformer.apply(diagramBPMN, report);
        report.endOperation("     " + transformer.getName() + ": " + transformer.getReportOperations(), operation);
      }
    } catch (Exception e) {
      // already logged
    }
  }

  private void executeVerifications(BpmnDiagramTransport diagramBPMN) {
    // now run all verifications
    VerificationFactory verificationFactory = VerificationFactory.getInstance();
    for (VerificationInt verification : verificationFactory.getTransformers()) {
      Report.Operation operation = report.startOperation("Transformation " + verification.getName());

      boolean isOk = verification.isOk(diagramBPMN, report);
      report.endOperation("     " + verification.getName() + ": " + isOk + " - " + verification.getReportVerification(),
          operation);
    }

  }

}
