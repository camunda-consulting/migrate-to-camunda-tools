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

  public void process(File pathIn, File pathOut, boolean debugOperation) {
    String[] listProcessFile = pathIn.list();
    if (listProcessFile == null)
      return;
    report.info("Found  " + listProcessFile.length + " files in [" + pathIn.getAbsolutePath() + "]");

    String prefix="----------------------------------------------- ";
    for (String oneProcessFile : listProcessFile) {
      if (!oneProcessFile.endsWith(".bpmn")) {
        report.error("Can't transform file[" + oneProcessFile + "] : must be end by .bpmn");
        continue;
      }
      report.info(prefix+"Manage[" + oneProcessFile + "]");
      BpmnDiagramTransport diagramBPMN = new BpmnDiagramTransport(report);
      try {
        diagramBPMN.read(new File(pathIn + "/" + oneProcessFile));

        // verification before any transformation:
        Report.Operation processPreVerification = report.startOperation("preverification");

        if (! executeVerifications(diagramBPMN)) {
          report.error("["+oneProcessFile+"]Verification before transformation failed, to not process");
          continue;
        }
        report.endOperation("  -- End pre verification ", processPreVerification);


        Report.Operation processOperation = report.startOperation("transformation");
        executeTransformations(diagramBPMN, pathOut, debugOperation);
        report.endOperation("  -- End transformation", processOperation);

        Report.Operation processPostVerification = report.startOperation("postverification");
        executeVerifications(diagramBPMN);
        report.endOperation("  -- End post verification ", processPostVerification);
        report.endOperation(prefix+"End[" + oneProcessFile + "]", processPostVerification);
        diagramBPMN.write(pathOut);

      } catch (Exception e) {
        // already logged
      }

    }
    report.logAllOperations();
    report.info("End, process produced in  [" + pathOut.getAbsolutePath() + "]");
  }

  private void executeTransformations(BpmnDiagramTransport diagramBPMN,File pathOut, boolean debugOperation) {
    try {
      TransformFactory transformFactory = TransformFactory.getInstance();
      int transformationNumber=0;
      for (TransformationBpmnInt transformer : transformFactory.getTransformers()) {
        transformationNumber++;
        Report.Operation operation = report.startOperation("Transformation " + transformer.getName());
        // diagramBPMN.write(pathOut, transformationNumber+"_"+transformer.getName()+"_beg");

        diagramBPMN = transformer.apply(diagramBPMN, report);
        if (debugOperation) {
          diagramBPMN.write(pathOut, transformationNumber + "_" + transformer.getName() + "_end");
        }
        String name = (transformer.getName()+"                         ").substring(0,20);
        report.endOperation("     [" + name + "] : " + transformer.getReportOperations(), operation);
      }
    } catch (Exception e) {
      // already logged
      report.info("already logger "+e.getMessage()+" "+e.getCause());
    }
  }

  private boolean executeVerifications(BpmnDiagramTransport diagramBPMN) {
    // now run all verifications
    VerificationFactory verificationFactory = VerificationFactory.getInstance();
    boolean isOk=true;
    for (VerificationInt verification : verificationFactory.getTransformers()) {
      Report.Operation operation = report.startOperation("Transformation " + verification.getName());

      boolean localIsOk = verification.isOk(diagramBPMN, report);

      String name = (verification.getName()+"                         ").substring(0,20);
      report.endOperation("     [" + name + "] : " + localIsOk + " - " + verification.getReportVerification(),
          operation);
      if (!localIsOk)
        isOk=false;

    }
    return isOk;
  }

}
