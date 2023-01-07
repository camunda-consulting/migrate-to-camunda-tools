package org.camunda.bpmn.generator.transform.draw;

import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeProcess {

  // represent the process as a tree
  private final TreeNode rootNode = new TreeNode(null);

  private final Map<ModelElementInstance, TreeNode> allElements = new HashMap<>();
  private final Map<String, TreeNode> allElementsById = new HashMap<>();

  public TreeProcess() {
  }

  public Map<ModelElementInstance, TreeNode> getAllElements() {
    return allElements;
  }

  public boolean contains(ModelElementInstance element) {
    return allElements.containsKey(element);
  }

  public TreeNode getNodeByElement(ModelElementInstance element) {
    return allElements.get(element);
  }

  public TreeNode getNodeById(String id) {
    return allElementsById.get(id);
  }

  public TreeNode addNode(TreeNode parent, ModelElementInstance element) {
    TreeNode treeNode = new TreeNode(parent);
    treeNode.element = element;
    parent.listChildren.add(treeNode);
    allElements.put(element, treeNode);
    if (treeNode.getId() != null)
      allElementsById.put(treeNode.getId(), treeNode);

    // reference the parent as a dependency for the calculation
    treeNode.listDependencies.add(parent);

    return treeNode;
  }

  public void addDependencie(TreeNode sourceNode, TreeNode targetNode) {
    // Is this is a dependency? Only if the targetNode is not a parent of the sourceNode
    boolean foundAsParent = false;
    TreeNode indexNode = sourceNode;
    int count = 10000; // protection
    while (indexNode != null && count > 0) {
      if (indexNode.isEquals(targetNode)) {
        foundAsParent = true;
        break;
      }
      indexNode = indexNode.getParent();
      count--;
    }
    if (foundAsParent)
      return; // not consider as a dependency
    targetNode.listDependencies.add(sourceNode);
  }

  public TreeNode getRoot() {
    return rootNode;
  }

  /**
   * TreeNode
   */

  public static class TreeNode {
    private final List<TreeNode> listChildren = new ArrayList<>();
    /**
     * To calculate a nice draw, keep the dependencies of the node.
     * The dependencie is an another node comming to this node. By default, there is one dependencies per node: its parent
     * A dependencies is a node which is not a parent (we don't want to keep a loop)
     * Example:
     * A -> B -> C -> A : C is not a dependency for A because A is parent of C
     * <p>
     * A->B->C->D
     * B->E->F->D
     * <p>
     * D as a dependency C and F. We want to place D in the next column of F
     */
    private final List<TreeNode> listDependencies = new ArrayList<>();
    TreeNode parent;
    private ModelElementInstance element;
    private Coordinate position;
    private Coordinate entry;
    private Coordinate exit;
    private int height = 0;
    private int width = 0;
    public TreeNode(TreeNode parent) {
      this.parent = parent;
    }

    public boolean isEquals(TreeNode otherNode) {
      if (getId() == null || otherNode.getId() == null)
        return false;
      return getId().equals(otherNode.getId());
    }

    /**
     * Determine the type of node
     */

    public boolean isEvent() {
      return typeContains("event");
    }

    public boolean isGateway() {
      return typeContains("gateway");
    }

    public boolean isTask() {
      return typeContains("task");
    }

    private boolean typeContains(String pattern) {
      return getTypeName() != null && getTypeName().toLowerCase().contains(pattern);

    }

    /**
     * Set the position of a node
     *
     * @param x X coordinate
     * @param y Y coordinate
     */
    public void setPosition(int x, int y) {
      this.position = new Coordinate(x, y);
      refreshEntryExit();
    }

    public void setSize(int height, int width) {
      this.height = height;
      this.width = width;
      refreshEntryExit();
    }

    private void refreshEntryExit() {
      if (position != null) {
        this.entry = new Coordinate(position.x, position.y + height / 2);
        this.exit = new Coordinate(position.x + width, position.y + height / 2);
      }
    }

    public Coordinate getPosition() {
      return position;
    }

    public Coordinate getEntry() {
      return entry;
    }

    public Coordinate getExit() {
      return exit;
    }

    public int getHeight() {
      return height;
    }

    public int getWidth() {
      return width;
    }

    public ModelElementInstance getElement() {
      return element;
    }

    public List<TreeNode> getChildren() {
      return listChildren;
    }

    public List<TreeNode> getDependencies() {
      return listDependencies;
    }

    public TreeNode getParent() {
      return parent;
    }

    public String getId() {
      return element == null ? null : element.getAttributeValue("id");
    }

    /**
     * Return the type name behind the element
     *
     * @return typeName as a String
     */
    public String getTypeName() {
      if (element != null)
        return element.getElementType().getTypeName();
      return null;
    }

    public record Coordinate(int x, int y) {
    }
  }
}
