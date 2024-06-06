/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TODO move this class to another package.
 *
 * Represents a node of the Tree<T> class. The TreeNode<T,S> is also a container, and
 * can be thought of as instrumentation to determine the location of the type T
 * in the Tree<T>.
 */
public class TreeNode<T,S> {

    private T data;
    private S nodeId;
    public List<TreeNode<T,S>> children;


    public boolean containsChild(TreeNode<T,S> TreeNode){
        boolean result = false;
        Iterator<TreeNode<T,S>> it = this.children.iterator();

        while (!result && it.hasNext()) {
             if (it.next().data.equals(TreeNode.data)){
                 result = true;
             }
        }
        return result;
    }

    public TreeNode<T,S> getChild(TreeNode<T,S> TreeNode) {
        boolean found = false;
        TreeNode<T,S> result = null;
        Iterator<TreeNode<T,S>> it = children.iterator();
        while (!found && it.hasNext()) {
            result = it.next();
            if (result.data.equals(TreeNode.data)){
                found = true;
            }
        }
        if (!found){
            try {
                throw new Exception("The node was not found in among children and that is a precondition of getChild(node) method");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

//    /**
//     * Convenience ctor to create a Node<T> with an instance of T.
//     * @param data an instance of T.
//     */
//    public TreeNode(T data) {
//        this.data = data;
//    }

    public TreeNode(S nodeId) {
        this.nodeId = nodeId;
    }

    public TreeNode(S nodeId, T data) {
        this.nodeId = nodeId;
        this.data = data;
    }

    public TreeNode() {
        super();
    }

    /**
     * Return the children of TreeNode<T,S>. The Tree<T> is represented by a single
     * root TreeNode<T,S> whose children are represented by a List<TreeNode<T,S>>. Each of
     * these TreeNode<T,S> elements in the List can have children. The getChildren()
     * method will return the children of a TreeNode<T,S>.
     * @return the children of TreeNode<T,S>
     */
    public List<TreeNode<T,S>> getChildren() {
        if (this.children == null) {
            return new ArrayList<TreeNode<T,S>>();
        }
        return this.children;
    }

    /**
     * Sets the children of a TreeNode<T,S> object. See docs for getChildren() for
     * more information.
     * @param children the List<TreeNode<T,S>> to set.
     */
    public void setChildren(List<TreeNode<T,S>> children) {
        this.children = children;
    }

    /**
     * Returns the number of immediate children of this TreeNode<T,S>.
     * @return the number of immediate children.
     */
    public int getNumberOfChildren() {
        if (children == null) {
            return 0;
        }
        return children.size();
    }

    /**
     * Adds a child to the list of children for this TreeNode<T,S>. The addition of
     * the first child will create a new List<TreeNode<T,S>>.
     * @param child a TreeNode<T,S> object to set.
     * @return
     */
    public TreeNode<T, S> addChild(TreeNode<T,S> child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
        return child;
    }

    /**
     * Inserts a TreeNode<T,S> at the specified position in the child list. Will
     * throw an ArrayIndexOutOfBoundsException if the index does not exist.
     * @param index the position to insert at.
     * @param child the TreeNode<T,S> object to insert.
     * @throws IndexOutOfBoundsException if thrown.
     */
    public void insertChildAt(int index, TreeNode<T,S> child) throws IndexOutOfBoundsException {
        if (index == getNumberOfChildren()) {
            // this is really an append
            addChild(child);
            return;
        } else {
            children.get(index); //just to throw the exception, and stop here
            children.add(index, child);
        }
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public S getNodeId() {
        return nodeId;
    }
    public void setNodeId(S nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * Returns the (first) child node (of type TreeNode) with the given nodeID.
     * @return the found node or null
     */
    public TreeNode<T,S> findChildNode(S nodeID) {
        if (this.getChildren() == null) {
            return null;
        }

        for (TreeNode<T, S> node : this.getChildren()) {
            if (node.getNodeId().equals(nodeID)) {
                return node;
            }
        }
        return null;
    }

//**************************** TO STRING *************************/

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (this.nodeId != null) {
            sb.append(nodeId.toString());
        }
        sb.append("{").append(getData() == null ? "null" : getData().toString()).append(",[");
        int i = 0;
        for (TreeNode<T,S> e : getChildren()) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(e.getData().toString());
            i++;
        }
        sb.append("]").append("}");
        return sb.toString();
    }
}
