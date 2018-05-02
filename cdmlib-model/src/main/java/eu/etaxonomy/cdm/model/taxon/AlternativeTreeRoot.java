/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @since 02.04.2009
 * @version 1.0
 */
// Not in use yet
public class AlternativeTreeRoot extends TaxonNode {
	private static final long serialVersionUID = 3194452473289639597L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AlternativeTreeRoot.class);
	
	private TaxonNode replacedTaxonNode; 
	
	
	protected AlternativeTreeRoot(TaxonNode oldRoot, TaxonNode replacedTaxonNodeFromParentView, Reference reference, String microReference) {
		super(oldRoot.getTaxon(), oldRoot.getClassification());
		this.setParent(replacedTaxonNodeFromParentView.getParent());
		this.setReplacedTaxonNode(replacedTaxonNodeFromParentView);
	//	this.childNodes = oldRoot.getChildNodes());
	}


	/**
	 * @return the replacedTaxonNode
	 */
	public TaxonNode getReplacedTaxonNode() {
		return replacedTaxonNode;
	}


	/**
	 * @param replacedTaxonNode the replacedTaxonNode to set
	 */
	private void setReplacedTaxonNode(TaxonNode replacedTaxonNode) {
		this.replacedTaxonNode = replacedTaxonNode;
	}

	
	
}
