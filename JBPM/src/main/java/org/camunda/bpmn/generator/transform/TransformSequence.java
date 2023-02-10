package org.camunda.bpmn.generator.transform;

import org.camunda.bpmn.generator.process.BpmnDiagramTransport;
import org.camunda.bpmn.generator.process.BpmnTool;
import org.camunda.bpmn.generator.report.Report;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public class TransformSequence implements TransformationBpmnInt {
  private final List<SequenceFlow> relations = new ArrayList<>();
  int nbCreations = 0;
  int nbDeletions = 0;

  @Override
  public String getName() {
    return "Sequence";
  }

  @Override
  public boolean init(Report report) {
    return true;
  }

  @Override
  public BpmnDiagramTransport apply(BpmnDiagramTransport bpmnDiagram, Report report) {

    // build relations
    List<Node> listSequences = bpmnDiagram.getBpmnTool().getElementsByBpmnName("sequenceFlow");
    for (Node sequence : listSequences) {
      relations.add(new SequenceFlow(BpmnTool.getAttributeName(sequence, "id"), // id of sequence
          BpmnTool.getAttributeName(sequence, "sourceRef"), // id of source
          BpmnTool.getAttributeName(sequence, "targetRef"))); // id of targer
    }

    // Build all bpmn:incoming and outgoing
    for (String artefact : BpmnTool.getListAllArtefacts()) {
      List<Node> listArtefacts = bpmnDiagram.getBpmnTool().getElementsByBpmnName(artefact);
      for (Node artefactNode : listArtefacts) {
        // purge all bpmn:incoming and ns5:incoming
        List<Node> removeList = new ArrayList<>();
        removeList.addAll(BpmnTool.getBpmnChildren(artefactNode, "incoming"));
        removeList.addAll(BpmnTool.getBpmnChildren(artefactNode, "outgoing"));
        nbDeletions += removeList.size();

        for (Node removeItem : removeList) {
          artefactNode.removeChild(removeItem);
        }
        // get the reference node: all child must be inserted before
        List<Node> listNodes = BpmnTool.getList(artefactNode.getChildNodes());
        Node referenceNode = listNodes.isEmpty() ? null : listNodes.get(0);

        // if one child is "extensionElements", ignore it, there is a issue at this moment
        // ERROR >>>>>>>>>>>>>>>>>>>>>>> During DRAW operation  SAXException while parsing input stream org.xml.sax.SAXException: Error: URI=null Line=44: cvc-complex-type.2.4.a: Invalid content was found starting with element '{"http://www.omg.org/spec/BPMN/20100524/MODEL":extensionElements}'. One of '{"http://www.omg.org/spec/BPMN/20100524/MODEL":incoming, "http://www.omg.org/spec/BPMN/20100524/MODEL":outgoing, "http://www.omg.org/spec/BPMN/20100524/MODEL":ioSpecification, "http://www.omg.org/spec/BPMN/20100524/MODEL":property, "http://www.omg.org/spec/BPMN/20100524/MODEL":dataInputAssociation, "http://www.omg.org/spec/BPMN/20100524/MODEL":dataOutputAssociation, "http://www.omg.org/spec/BPMN/20100524/MODEL":resourceRole, "http://www.omg.org/spec/BPMN/20100524/MODEL":loopCharacteristics, "http://www.omg.org/spec/BPMN/20100524/MODEL":rendering}' is expected.
        boolean noExtension = listNodes.stream()
            .filter(t -> BpmnTool.equalsNodeName(t, "extensionElements"))
            .findAny()
            .isEmpty();
        if (!noExtension) {
          referenceNode = null;
          // extension must be placed before incoming / outgoing
        }
        if (referenceNode != null) {
          report.info("This node contains child, place incoming/outcoming first " + artefactNode.getNodeName() + " id=["
              + BpmnTool.getAttributeName(artefactNode, "id") + "] name=[" + BpmnTool.getAttributeName(artefactNode,
              "name") + "] contains extensionElements, ignore it");
        }
        if (BpmnTool.getBpmnChildren(artefactNode, "incoming").isEmpty()) {
          // we can add the incoming node
          List<SequenceFlow> listIncoming = getAllIncomingTask(BpmnTool.getAttributeName(artefactNode, "id"));
          for (SequenceFlow incoming : listIncoming) {
            Element incomingElement = bpmnDiagram.getProcessXml().createElement("bpmn:incoming");
            incomingElement.setTextContent(incoming.id);
            if (referenceNode == null)
              artefactNode.appendChild(incomingElement);
            else
              artefactNode.insertBefore(incomingElement, referenceNode);
            nbCreations++;
          }
        }

        if (BpmnTool.getBpmnChildren(artefactNode, "outgoing").isEmpty()) {
          // we can add the incoming node
          List<SequenceFlow> listOutgoing = getAllOutgoing(BpmnTool.getAttributeName(artefactNode, "id"));
          for (SequenceFlow outgoing : listOutgoing) {
            Element outgoingElement = bpmnDiagram.getProcessXml().createElement("bpmn:outgoing");
            outgoingElement.setTextContent(outgoing.id);
            if (referenceNode == null)
              artefactNode.appendChild(outgoingElement);
            else
              artefactNode.insertBefore(outgoingElement, referenceNode);
            nbCreations++;
          }
        }

      }
    }
    return bpmnDiagram;
  }

  @Override
  public String getReportOperations() {
    return "relations created " + nbCreations + " element purged " + nbDeletions;
  }

  /**
   * For the source task, collect all outgoing tasks
   *
   * @param taskName task to search from
   * @return list of outgoing task
   */
  private List<SequenceFlow> getAllOutgoing(String taskName) {

    return relations.stream().filter(t -> t.sourceRef.equals(taskName)).toList();
  }

  /**
   * for a task, collect all incoming task
   *
   * @param taskName task to search from
   * @return list of incoming task
   */
  private List<SequenceFlow> getAllIncomingTask(String taskName) {
    return relations.stream().filter(t -> t.targetRef().equals(taskName)).toList();
  }

  public record SequenceFlow(String id, String sourceRef, String targetRef) {
  }

}
