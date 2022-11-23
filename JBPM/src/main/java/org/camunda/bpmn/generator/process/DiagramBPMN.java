package org.camunda.bpmn.generator.process;

import org.camunda.bpmn.generator.report.Report;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;

public class DiagramBPMN {


  private Document processXml;

  private String processName;

  Report report;
  public DiagramBPMN(Report report) {
    this.report = report;
  }
  public void read(File file) throws Exception{
    // Read document in preparation for Xpath searches
    processName = file.getName();
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    try {
      Report.Operation operation = report.startOperation("ReadProcess");
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      processXml = dBuilder.parse(file);
      report.endOperation(operation);

    } catch(Exception e) {
      report.error("Error parsing file ("+file.getName()+"]");
      throw e;
    }
  }

  public void write(File folderOutput) {
    try (FileOutputStream output =
        new FileOutputStream(folderOutput+"\\"+processName)) {
      Report.Operation operation = report.startOperation("WriteProcess");

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(processXml);
      StreamResult result = new StreamResult(output);

      transformer.transform(source, result);

      report.endOperation(operation);

    } catch (Exception e) {
      report.error("Can't write file ["+processName+"] to path["+folderOutput.getAbsolutePath(), e);
    }
  }

  /**
   * Return all SequenceFlow
   * @return NodesList of sequenceFlow
   */
  public NodeList getSequenceFlow() {
    // search all Sequence flow
    NodeList nodes = processXml.getElementsByTagName("sequenceFlow");
    report.debug("DiagramBPMN.getSequenceFlow(): Found "+nodes.getLength());
  return nodes;
  }
}
