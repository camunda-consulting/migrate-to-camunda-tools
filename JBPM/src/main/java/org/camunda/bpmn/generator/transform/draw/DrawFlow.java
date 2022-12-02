package org.camunda.bpmn.generator.transform.draw;

import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnEdge;
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane;
import org.camunda.bpm.model.bpmn.instance.di.Waypoint;
import org.camunda.bpm.model.xml.ModelInstance;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class DrawFlow {
  private BpmnPlane plane;
  private ModelInstance modelInstance;

  public DrawFlow(BpmnPlane plane, ModelInstance modelInstance) {
    this.plane = plane;
    this.modelInstance = modelInstance;
  }

  /**
   * Draw the flow between two elements
   *
   * @param sfElement   the flow two draw. This flow contains the source and the target
   * @param treeProcess tree process to get the position for the two reference
   * @return true if everything is ok.
   */
  public boolean draw(Element sfElement, TreeProcess treeProcess) {

    String sourceRef = sfElement.getAttribute("sourceRef");
    String targetRef = sfElement.getAttribute("targetRef");

    TreeProcess.TreeNode source = treeProcess.getNodeById(sourceRef);
    TreeProcess.TreeNode target = treeProcess.getNodeById(targetRef);

    if (source != null && target != null) {

      BpmnEdge bpmnEdge = modelInstance.newInstance(BpmnEdge.class);
      BaseElement element = modelInstance.getModelElementById(sfElement.getAttribute("id"));
      bpmnEdge.setBpmnElement(element);
      for (Waypoint wp : calculateLine(source.getExit(), target.getEntry(), source, target))
        bpmnEdge.addChildElement(wp);

      plane.addChildElement(bpmnEdge);
      return true;
    }

    return false;
  }

  /**
   * Calculate a line between two positions
   *
   * @param source      source coordinate
   * @param destination destination coordinate
   * @return list of WayPoint
   */
  public List<Waypoint> calculateLine(TreeProcess.TreeNode.Coordinate source,
                                      TreeProcess.TreeNode.Coordinate destination,
  TreeProcess.TreeNode sourceNode,
                                      TreeProcess.TreeNode targetNode) {
    List<Waypoint> listWaypoints = new ArrayList<>();

    listWaypoints.add(generateWayPoint(source.x(), source.y()));

    if (source.x() < destination.x()) {
      // ADVANCE
      if (source.y() == destination.y() && source.x() < destination.x()) {
        // Same line, advance: simple, two points
      } else if (source.y() < destination.y()) {
        // Advance, but not on the same line:
        // add a point at the corner. But where is the best corner? On the bottom.
        listWaypoints.add(generateWayPoint(source.x(), destination.y()));
      } else if (source.y() > destination.y()) {
        // Advance, but not on the same line:
        // add a point at the corner. But where is the best corner? On the bottom.
        listWaypoints.add(generateWayPoint(destination.x(), source.y()));
      }
    } else if (source.x() == destination.x()) {
      // vertical: keep it on one line
    } else {
      // BACK
      // add 4 points, 60 upper the lower level
      int minY = Math.min(source.y(), destination.y());
      // add this point just if the source is not a gateway
      if (sourceNode.isGateway()) {
        listWaypoints.add(generateWayPoint(source.x(), minY - 60));
      } else {
        listWaypoints.add(generateWayPoint(source.x() + 10, source.y()));
        listWaypoints.add(generateWayPoint(source.x() + 10, minY - 60));
      }
        listWaypoints.add(generateWayPoint(destination.x()-10, minY - 60));
      listWaypoints.add(generateWayPoint(destination.x()-10, destination.y()));
    }

    // add the final point
    listWaypoints.add(generateWayPoint(destination.x(), destination.y()));
    return listWaypoints;

  }

  private Waypoint generateWayPoint(int x, int y) {
    Waypoint waypoint = modelInstance.newInstance(Waypoint.class);
    waypoint.setX(x);
    waypoint.setY(y);
    return waypoint;

  }

}
