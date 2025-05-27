package org.camunda.bpmn.generator;

import org.junit.Test;

import static org.junit.Assert.*;

public class BPMNGenFromXPDLTest {

    @Test
    public void main() throws Exception {
        String[] args = {"src/main/resources/XPDLTest.xpdl", "src/main/resources/XPDLConverted.bpmn"};
        BPMNGenFromXPDL.main(args);
        System.out.println("Please visually compare the original process in the XPDL Editor with the generated output in Camunda Modeler, a picture of the original diagram is provided in /resources");
    }
}