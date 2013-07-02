// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import java.util.List;
import java.util.Set;

import eu.etaxonomy.cdm.model.common.IAnnotatableEntity;
import eu.etaxonomy.cdm.model.reference.Reference;


/**
 * @author n.hoffmann
 * @created Sep 18, 2009
 */
public interface ITreeNode extends IAnnotatableEntity {

	/**
	 * Adds a taxon node as a child of the ITreeNode
	 * 
	 * @param childNode
	 * @param citation
	 * @param microCitation
	 * @param synonymToBeUsed
	 * @return the child node 
	 */
	public TaxonNode addChildNode(TaxonNode childNode, Reference citation, String microCitation);
	
	/**
	 * Adds a taxon as a child of the ITreeNode
	 * 
	 * @param taxon
	 * @param citation
	 * @param microCitation
	 * @param synonymToBeUsed
	 * @return the child node
	 */
	public TaxonNode addChildTaxon(Taxon taxon, Reference citation, String microCitation);
	
	/**
	 * Whether this TreeNode has child nodes attached
	 * 
	 * @return true if this node has children
	 */
	public boolean hasChildNodes();
	
	/**
	 * This recursively removes all child nodes from this node and from this taxonomic view.
	 * 
	 * TODO remove orphan nodes completely 
	 * 
	 * @param node
	 * @return true on success
	 */
	public boolean deleteChildNode(TaxonNode node);

	/**
	 * Returns the list of direct child nodes of <code>this</code> ITreeNode.
	 * @return
	 */
	public List<TaxonNode> getChildNodes();
	
	/**
	 * @return the citation for the parent child relationship or the tree itself
	 */
	public Reference getReference();
	
	/**
	 * 
	 * @return the microCitation for the parent child relationship or the tree itself
	 */
	public abstract String getMicroReference();	

}
