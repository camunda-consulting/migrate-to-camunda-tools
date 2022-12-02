package org.camunda.bpmn.generator.transform.draw;

import org.camunda.bpm.model.xml.instance.ModelElementInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeProcess {

  // represent the process as a tree
  private TreeNode rootNode = new TreeNode();

  private Map<ModelElementInstance, TreeNode> allElements = new HashMap<>();
  private Map<String, TreeNode> allElementsById = new HashMap<>();

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
    TreeNode treeNode = new TreeNode();
    treeNode.element = element;
    parent.listChildren.add(treeNode);
    allElements.put(element, treeNode);
    if (treeNode.getId() != null)
      allElementsById.put(treeNode.getId(), treeNode);
    return treeNode;
  }

  public TreeNode getRoot() {
    return rootNode;
  }

  public class TreeNode {
    private ModelElementInstance element;
    private List<TreeNode> listChildren = new ArrayList<>();

    public record Coordinate(int x, int y) {
    }

    private Coordinate position;
    private Coordinate entry;
    private Coordinate exit;

    private int height = 0;
    private int width = 0;

    public String getId() {
      return element == null ? null : element.getAttributeValue("id");
    }

    /**
     * Return the type name behind the element
     * @return
     */
    public String getTypeName() {
      if (element!=null)
        return element.getElementType().getTypeName();
      return null;
    }


    public boolean isEvent() {
      return typeContains("Event");
    }
    public boolean isGateway() {
      return typeContains("Gateway");
    }
    public boolean isTask() {
      return typeContains("Task");
    }


    private boolean typeContains(String pattern) {
      return getTypeName()!=null && getTypeName().indexOf(pattern)!=-1;

    }
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
        this.entry = new Coordinate(position.x, (int) (position.y + height / 2));
        this.exit = new Coordinate(position.x + width, (int) (position.y + height / 2));
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
  }
}
