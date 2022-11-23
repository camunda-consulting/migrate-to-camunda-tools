package org.camunda.bpmn.generator;

import org.camunda.bpmn.generator.report.Report;
import org.slf4j.event.Level;

import java.io.File;

public class BPMNFromJBPM {

  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("USAGE: <PathIn> <PathOut>");
      return;
    }

    Report report = new Report();
    report.setLevel(Level.INFO);
    int arg = 0;

    String pathIn = args[arg];
    arg++;

    String pathOut = args[arg];
    arg++;

    Pilot pilot = new Pilot(report);
    pilot.process(new File(pathIn), new File(pathOut));
    report.info("Terminated");
  }

}
