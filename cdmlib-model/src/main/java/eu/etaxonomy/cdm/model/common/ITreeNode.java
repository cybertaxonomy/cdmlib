/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.common;

import java.util.Collection;
import java.util.List;

import org.hibernate.event.spi.SaveOrUpdateEventListener;

/**
 * Common interface for all tree data structures supporting tree indexing.
 * Mainly used by {@link SaveOrUpdateEventListener} to update the indices.
 * 
 * @author a.mueller
 * @since 12.08.2013
 *
 */
public interface ITreeNode<T extends ITreeNode<T>> extends ICdmBase {
	
	//Constants
	//the separator used in the tree index to separate the id's of the parent nodes
	public static final String separator = "#";
	
	//The prefix used in the tree index for the id of the tree itself
	public static final String treePrefix = "t";
	
	
	//METHODS
	
	
	/**
	 * Returns the tree index of this tree node.
	 * @return the tree index
	 */
	public String treeIndex();

	
	/**
	 * Returns the parent node of this node.
	 * Returns <code>null</code> if this
	 * node is a root node.
	 * @return
	 */
	public ITreeNode<T> getParent();

	/**
	 * Sets the tree index of this node.
	 * @deprecated preliminary implementation for updating the treeindex.
	 * Maybe removed once index updating is improved.
	 * @param newTreeIndex
	 */
	public void setTreeIndex(String newTreeIndex);
	
	/**
	 * Returns all direct child nodes of this node.
	 * As tree node children do not necessarily need to be
	 * {@link List lists} the return type of this method may change
	 * to {@link Collection} in future. Therefore the use
	 * at the moment is deprecated.
	 * @deprecated return type may become {@link Collection} in future 
	 * @return the list of children
	 */
	public List<T> getChildNodes();

	/**
	 * Returns the {@link ICdmBase#getId() id} of the tree object,
	 * this node belongs to.
	 * @deprecated may be removed in future when index updating does not
	 * use this anymore
	 * @return the id of the tree
	 */
	public int treeId();

}
