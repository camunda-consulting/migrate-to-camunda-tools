package org.camunda.bpmn.generator;

import org.camunda.bpmn.generator.process.DiagramBPMN;
import org.camunda.bpmn.generator.report.Report;
import org.camunda.bpmn.generator.transform.TransformFactory;
import org.camunda.bpmn.generator.transform.TransformationBpmnInt;

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
    report.info("Found  "+listProcessFile.length+" files in ["+pathIn.getAbsolutePath()+"]");

    for (String oneProcessFile : listProcessFile) {
      if (! oneProcessFile.endsWith(".bpmn")) {
        report.error("Can't transform file["+oneProcessFile+"] : must be end by .bpmn");
        continue;
      }
      report.info("------------ Manage["+oneProcessFile+"]");
      Report.Operation processOperation = report.startOperation("Process");
      DiagramBPMN diagramBPMN = new DiagramBPMN(report);
      try {
        diagramBPMN.read(new File(pathIn+"/"+oneProcessFile));
        TransformFactory transformFactory = TransformFactory.getInstance();
        for (TransformationBpmnInt transformer : transformFactory.getTransformers()) {
          Report.Operation operation= report.startOperation("Transformation "+transformer.getName());

          diagramBPMN = transformer.apply(diagramBPMN, report);
          report.endOperation("     "+transformer.getName()+": "+transformer.getReportOperations(),operation);
        }
        diagramBPMN.write(pathOut);
      } catch (Exception e) {
        // already logged
      }
      report.endOperation("------------ End process ["+oneProcessFile+"] ",processOperation);

    }
    report.logAllOperations();
    report.info("End, process produced in  ["+pathOut.getAbsolutePath()+"]");
  }

}
