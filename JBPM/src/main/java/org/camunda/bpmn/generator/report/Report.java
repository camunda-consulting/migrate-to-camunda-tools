package org.camunda.bpmn.generator.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Report {

  private final Map<String, Long> cumulOperationTime = new HashMap<>();
  private final Map<String, Long> cumulOperationOccurence = new HashMap<>();
  Logger logger = LoggerFactory.getLogger(Report.class.getName());
  Level level = Level.INFO;

  public void setLevel(Level level) {
    this.level = level;
  }

  public void info(String message) {
    logger.info(message);
    if (this.level == Level.DEBUG || this.level == Level.INFO)
      System.out.println("INFO  " + message);
  }

  public void error(String message) {
    logger.error(message);
    System.out.println("ERROR " + message);
  }

  public void error(String message, Exception e) {
    error(message + " " + e);
  }

  public void debug(String message) {
    logger.debug(message);
    if (this.level == Level.DEBUG)
      System.out.println("DEBUG " + message);
  }

  public Operation startOperation(String name) {
    return new Operation(name, System.currentTimeMillis());
  }

  public void endOperation(String message, Operation operation) {
    long duration = endOperationInternal(operation);
    info(message + " in " + duration);
  }

  public void endOperation(Operation operation) {
    long duration = endOperationInternal(operation);
    if (duration > 100)
      info("operation [" + operation.name + "] done in " + duration);

  }

  private long endOperationInternal(Operation operation) {
    long duration = System.currentTimeMillis() - operation.timeBegin();
    long cumulTime = cumulOperationTime.getOrDefault(operation.name, 0L);
    cumulOperationTime.put(operation.name, cumulTime + duration);

    long cumulOccurence = cumulOperationOccurence.getOrDefault(operation.name, 0L);
    cumulOperationOccurence.put(operation.name, cumulOccurence + 1);
    return duration;
  }

  public void logAllOperations() {
    List<String> synthesis = new ArrayList<>();
    for (Map.Entry<String, Long> operation : cumulOperationTime.entrySet()) {
      synthesis.add(
          operation.getKey() + ":" + operation.getValue() + " ms (" + cumulOperationOccurence.get(operation.getKey())
              + ")");
    }
    info(String.join(", ", synthesis));
  }

  public record Operation(String name, long timeBegin) {
  }
}
