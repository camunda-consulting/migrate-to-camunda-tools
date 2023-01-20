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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;
import java.util.regex.Pattern;

public class TransformationFEEL implements TransformationBpmnInt {

  private int feelExpressionReplaced = 0;
  private int feelExpressionIgnored = 0;

  @Override
  public String getName() {
    return "Feel";
  }

  @Override
  public boolean init(Report report) {
    return true;
  }

  @Override
  public BpmnDiagramTransport apply(BpmnDiagramTransport bpmnDiagram, Report report) {

    Pattern returnEqualsExpression = Pattern.compile(
        "return [ ]*[a-zA-Z\"]+\\.equals\\([ ]*[a-zA-Z_0-9\"]+[ ]*\\)");//. represents single character
    Pattern equalsExpression = Pattern.compile(
        "[ ]*[a-zA-Z\"]+\\.equals\\([ ]*[a-zA-Z_0-9\"]+[ ]*\\)");//. represents single character

    try {
      List<Node> listSequences = bpmnDiagram.getBpmnTool().getElementsByBpmnName("sequenceFlow");
      for (Node nodeSequence : listSequences) {
        Element sequenceFlow = (Element) nodeSequence;
        // the sequence contains a condition?
        NodeList listChild = sequenceFlow.getChildNodes();
        for (Node nodeCondition : bpmnDiagram.getBpmnTool().getList(listChild)) {
          if (!(nodeCondition instanceof Element)) {
            continue;
          }
          Element condition = (Element) nodeCondition;
          if (condition.getNodeName().equals("conditionExpression") || condition.getNodeName()
              .endsWith(":conditionExpression")) {
            // Yes, get one here
            String textContent = condition.getTextContent();
            if (textContent.trim().endsWith(";"))
              textContent = textContent.substring(0, textContent.length() - 1);
            textContent = textContent.trim();

            // match return <Variable>.equals(<value>) ?

            if (returnEqualsExpression.matcher(textContent).matches()) {
              // transform the content then
              // get the name
              int indexOfEquals = textContent.indexOf(".equals(");
              String variableName = textContent.substring("return".length(), indexOfEquals).trim();
              String value = textContent.substring(indexOfEquals + ".equals(".length());
              // remove the last )
              value = value.substring(0, value.length() - 1);
              condition.setTextContent("${" + variableName + " == " + value + "}");
              // remove the attribut language
              condition.removeAttribute("language");

              feelExpressionReplaced++;
            } else if (equalsExpression.matcher(textContent).matches()) {
              int indexOfEquals = textContent.indexOf(".equals(");
              String variableName = textContent.substring(indexOfEquals).trim();
              String value = textContent.substring(indexOfEquals + ".equals(".length());
              // remove the last )
              value = value.substring(0, value.length() - 1);
              condition.setTextContent("${" + variableName + " == " + value + "}");
              // remove the attribut language
              condition.removeAttribute("language");

              feelExpressionReplaced++;

            } else {
              feelExpressionIgnored++;
              report.info("Expression {" + textContent + "] does not match any transformation expression - ignored");
            }
          } // end contiditionExpression
        } // end sequence Child
      } // end sequence
    } catch (Exception e) {
      report.error("During FEEL operation ", e);
    }
    return bpmnDiagram;
  }

  @Override
  public String getReportOperations() {
    return "Feel Expression replaced " + feelExpressionReplaced + " ignored " + feelExpressionIgnored;
  }
}
