package eu.etaxonomy.cdm.common;

import java.util.ArrayList;
import java.util.List;

//import eu.etaxonomy.cdm.model.location.TdwgAreaTest.NamedAreaTree;

public class Tree<T> {
	 
	private TreeNode<T> rootElement;
     
    /**
     * Default ctor.
     */
    public Tree() {
        super();
    }
 
    /**
     * Return the root TreeNode of the tree.
     * @return the root element.
     */
    public TreeNode<T> getRootElement() {
        return this.rootElement;
    }
 
    /**
     * Set the root Element for the tree.
     * @param rootElement the root element to set.
     */
    public void setRootElement(TreeNode<T> rootElement) {
        this.rootElement = rootElement;
    }
     
    /**
     * Returns the Tree<T> as a List of TreeNode<T> objects. The elements of the
     * List are generated from a pre-order traversal of the tree.
     * @return a List<TreeNode<T>>.
     */
    public List<TreeNode<T>> toList() {
        List<TreeNode<T>> list = new ArrayList<TreeNode<T>>();
        walk(rootElement, list);
        return list;
    }
     
    /**
     * Returns a String representation of the Tree. The elements are generated
     * from a pre-order traversal of the Tree.
     * @return the String representation of the Tree.
     */
    public String toString() {
        return toList().toString();
    }
     
    /**
     * Walks the Tree in pre-order style. This is a recursive method, and is
     * called from the toList() method with the root element as the first
     * argument. It appends to the second argument, which is passed by reference 
     * as it recurses down the tree.
     * @param element the starting element.
     * @param list the output of the walk.
     */
    private void walk(TreeNode<T> element, List<TreeNode<T>> list) {
        list.add(element);
        for (TreeNode<T> data : element.getChildren()) {
            walk(data, list);
        }
    } 
}
