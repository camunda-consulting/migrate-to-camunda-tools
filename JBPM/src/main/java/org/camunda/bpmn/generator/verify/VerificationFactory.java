package org.camunda.bpmn.generator.verify;

import java.util.Arrays;
import java.util.List;

public class VerificationFactory {

  private static final VerificationFactory verificationFactory = new VerificationFactory();

  public static VerificationFactory getInstance() {
    return verificationFactory;
  }

  public List<VerificationInt> getTransformers() {
    return Arrays.asList(new VerificationReadBpmnModel());
  }
}
