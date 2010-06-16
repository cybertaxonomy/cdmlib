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

import java.util.Set;

import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;


/**
 * @author n.hoffmann
 * @created Sep 18, 2009
 * @version 1.0
 */
public interface ITreeNode extends ICdmBase {

	/**
	 * Adds a taxon node as a child of the ITreeNode
	 * 
	 * @param childNode
	 * @param citation
	 * @param microCitation
	 * @param synonymToBeUsed
	 * @return the child node 
	 */
	public TaxonNode addChildNode(TaxonNode childNode, ReferenceBase citation, String microCitation, Synonym synonymToBeUsed);
	
	/**
	 * Adds a taxon as a child of the ITreeNode
	 * 
	 * @param taxon
	 * @param citation
	 * @param microCitation
	 * @param synonymToBeUsed
	 * @return the child node
	 */
	public TaxonNode addChildTaxon(Taxon taxon, ReferenceBase citation, String microCitation, Synonym synonymToBeUsed);
	
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
	 * @return the citation for the parent child relationship or the tree itself
	 */
	public ReferenceBase getReference();
	
	public Set<TaxonNode> getChildNodes();
	
	/**
	 * 
	 * @return the microCitation for the parent child relationship or the tree itself
	 */
	public abstract String getMicroReference();
}
