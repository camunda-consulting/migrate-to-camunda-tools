/* ******************************************************************** */
/*                                                                      */
/*  TransformationFEEL                                                  */
/*                                                                      */
/*  Transform each simple groovy expression to FEEL                     */
/* Expression:                                                          */
/*   return processVar.equals("Hello") ==> processVar == "Hello"        */
/* ******************************************************************** */
package org.camunda.bpmn.generator.transform;

import org.camunda.bpmn.generator.process.BpmnDiagramTransport;
import org.camunda.bpmn.generator.report.Report;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.regex.Pattern;

public class TransformationFEEL implements TransformationBpmnInt {

  private int feelExpressionReplaced = 0;

  @Override
  public String getName() {
    return "FEEL";
  }

  @Override
  public BpmnDiagramTransport apply(BpmnDiagramTransport diagramBPMN, Report report) {


    Pattern equalsExpression = Pattern.compile(
        "return [a-zA-Z]+\\.equals\\([a-zA-Z_0-9\"]+\\)");//. represents single character

    try {
      NodeList listSequences = diagramBPMN.getSequenceFlow();
      for (int i = 0; i < listSequences.getLength(); i++) {
        Element sequenceFlow = (Element) listSequences.item(i);
        // the sequence contains a condition?
        NodeList listChild = sequenceFlow.getChildNodes();
        for (int j = 0; j < listChild.getLength(); j++) {
          if (!(listChild.item(j) instanceof Element)) {
            continue;
          }
          Element condition = (Element) listChild.item(j);
          if (condition.getNodeName().equals("conditionExpression")) {
            // Yes, get one here
            String textContent = condition.getTextContent();
            if (textContent.trim().endsWith(";"))
              textContent=textContent.substring(0,textContent.length()-1);
            textContent = textContent.trim();

            // match return <Variable>.equals(<value>) ?

            if (equalsExpression.matcher(textContent).matches()) {
              // transform the content then
              // get the name
              int indexOfEquals = textContent.indexOf(".equals(");
              String variableName = textContent.substring("return".length(), indexOfEquals);
              String value = textContent.substring(indexOfEquals + ".equals(".length());
              // remove the last )
              value = value.substring(0, value.length() - 1);
              condition.setTextContent(variableName + " == " + value);
              feelExpressionReplaced++;
            }

          } // end contiditionExpression
        } // end sequence Child
      } // end sequence
    } catch (Exception e) {
      report.error("During FEEL operation ", e);
    }
    return diagramBPMN;
  }

  @Override
  public String getReportOperations() {
    return "FEEL Expression replaced "+feelExpressionReplaced;
  }
}
