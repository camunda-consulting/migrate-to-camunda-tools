package org.camunda.bpmn.generator;

import org.camunda.bpmn.generator.report.Report;
import org.slf4j.event.Level;

import java.io.File;

public class BPMNFromJBPM {

  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("USAGE: [--debug] <PathIn> <PathOut>");
      return;
    }

    Report report = new Report();
    report.setLevel(Level.INFO);
    int arg = 0;
    boolean debugOperation = false;

    if (args[arg].equals("--debug")) {
      debugOperation = true;
      arg++;
    }
    String pathIn = args[arg];
    arg++;

    String pathOut = args[arg];

    report.info("Migrate JPMB process in [" + pathIn + "] to Camunda 7 process saved in [" + pathOut + "]" + (
        debugOperation ?
            " -debug-" :
            ""));

    Pilot pilot = new Pilot(report);
    pilot.process(new File(pathIn), new File(pathOut), debugOperation);
    report.info("Terminated");
  }

}
