package org.camunda.bpmn.generator;

import org.junit.Test;

import static org.junit.Assert.*;

public class BPMNGenFromTWXTest {

    @Test
    public void main() throws Exception {
        String[] args = {"src/main/resources/TWXOriginal.xml", "src/main/resources/TWXConverted.bpmn"};
        BPMNGenFromTWX.main(args);
        System.out.println("Please visually compare the original process in IBM BPM Process Designer (picture provided) with the generated output in Camunda Modeler");
    }
}