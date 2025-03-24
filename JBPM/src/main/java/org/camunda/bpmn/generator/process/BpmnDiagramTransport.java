/* ******************************************************************** */
/*                                                                      */
/*  BpmnDiagramTransport                                                */
/*                                                                      */
/*  Keep the current diagram and pass it to each transformer            */
/*                                                                      */
/* ******************************************************************** */

package org.camunda.bpmn.generator.process;

import org.camunda.bpmn.generator.report.Report;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;

public class BpmnDiagramTransport {

  Report report;
  private Document processXml;
  private String processName;

  public BpmnDiagramTransport(Report report) {
    this.report = report;
  }

  public void read(File file) throws Exception {
    // Read document in preparation for Xpath searches
    processName = file.getName();

    try {
      Report.Operation operation = report.startOperation("ReadProcess");
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      processXml = dBuilder.parse(file);
      report.endOperation(operation);

    } catch (Exception e) {
      report.error("Error parsing file (" + file.getName() + "]");
      throw e;
    }
  }

  public void write(File folderOutput) {
    write(folderOutput, null);
  }

  /**
   * @param folderOutput pth to write the result
   * @param prefixName   prefix name of the file
   */
  public void write(File folderOutput, String prefixName) {
    try (FileOutputStream output = new FileOutputStream(
        folderOutput + File.separator + "out_" + processName + (prefixName != null ? "_" + prefixName : ""))) {
      Report.Operation operation = report.startOperation("WriteProcess");

      TransformerFactory transformerFactory = TransformerFactory.newInstance();

      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      // transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, ignoreDeclaration ? "yes" : "no");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.INDENT, "2");

      DOMSource source = new DOMSource(processXml);
      StreamResult result = new StreamResult(output);

      transformer.transform(source, result);

      report.endOperation(operation);

    } catch (Exception e) {
      report.error("Can't write file [" + processName + "] to path[" + folderOutput.getAbsolutePath(), e);
    }
  }

  public Document getProcessXml() {
    return processXml;
  }

  public void setProcessXml(Document processXml) {
    this.processXml = processXml;
  }

  public BpmnTool getBpmnTool() {
    return new BpmnTool(this, report);
  }

}
