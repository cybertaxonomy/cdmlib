package eu.etaxonomy.cdm.common;

import java.util.ArrayList;
import java.util.List;


public class Tree<T,S> {

    private TreeNode<T,S> rootElement;

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
    public TreeNode<T,S> getRootElement() {
        return this.rootElement;
    }

    /**
     * Set the root Element for the tree.
     * @param rootElement the root element to set.
     */
    public void setRootElement(TreeNode<T,S> rootElement) {
        this.rootElement = rootElement;
    }

    /**
     * Returns the Tree<T> as a List of TreeNode<T,S> objects. The elements of the
     * List are generated from a pre-order traversal of the tree.
     * @return a List<TreeNode<T,S>>.
     */
    public List<TreeNode<T,S>> toList() {
        List<TreeNode<T,S>> list = new ArrayList<TreeNode<T,S>>();
        walk(rootElement, list);
        return list;
    }

    /**
     * Returns a String representation of the Tree. The elements are generated
     * from a pre-order traversal of the Tree.
     * @return the String representation of the Tree.
     */
    @Override
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
    private void walk(TreeNode<T,S> element, List<TreeNode<T,S>> list) {
        list.add(element);
        for (TreeNode<T,S> data : element.getChildren()) {
            walk(data, list);
        }
    }
}
