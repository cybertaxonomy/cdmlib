// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.config;

import org.apache.log4j.Logger;

/**
 * This class is used to configure term deletion.
 * 
 * @see ITermService#delete(eu.etaxonomy.cdm.common.DefinedTermBase)
 * 
 * @author a.mueller
 * @date 09.11.2011
 *
 */
public class TermDeletionConfigurator extends DeleteConfiguratorBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TermDeletionConfigurator.class);

	private boolean deleteIncludedTerms = false;
	
	private boolean deleteIncludedRelations = false;

	private boolean deletePartOfRelations = true;
	
	private boolean deleteGeneralizationOfRelations = false;

	private boolean deleteKindOfRelations = true;

	private boolean deleteMediaIfPossible = false;

//	/**
//	 * If <code>true</code> all included terms are also deleted (recursivly).<BR>
//	 * Default value is <code>true</code>. 
//	 */
//	public boolean isDeleteIncludedTerms() {
//		return deleteIncludedTerms;
//	}
//	public void setDeleteIncludedTerms(boolean deleteIncludedTerms) {
//		this.deleteIncludedTerms = deleteIncludedTerms;
//	}

	/**
	 * If <code>true</code> included terms will be attached to the parent
	 * of <code>this</code> term. If <code>this</code> term has no parent, included terms
	 * will become top level terms. <BR>
	 * Default value is <code>false</code>. 
	 */
	public boolean isDeleteIncludedRelations() {
		return deleteIncludedRelations;
	}
	public void setDeleteIncludedRelations(boolean deleteIncludedRelations) {
		this.deleteIncludedRelations = deleteIncludedRelations;
	}
	
	/**
	 * If <code>true</code> <code>this</code> term will be detached from its parent
	 * term. If <code>false</code> the deletion will be aborted if <code>this</code> 
	 * term is part of another term.<BR>
	 * Default value is <code>true</code>. 
	 */
	public boolean isDeletePartOfRelations() {
		return deletePartOfRelations;
	}
	public void setDeletePartOfRelations(boolean deletePartOfRelations) {
		this.deletePartOfRelations = deletePartOfRelations;
	}

	
	/**
	 * If <code>true</code> more specific terms will be detached from this term prior to
	 * deletion. If <code>this</code> term has a generalization itself the specific terms will be
	 * attached to this generalization.<BR>
	 * If <code>false</code> deletion will be aborted if <code>this</code> 
	 * term is a generalization of any another term.
	 * <BR>
	 * Default value is <code>false</code>. 
	 */
	public boolean isDeleteGeneralizationOfRelations() {
		return deleteGeneralizationOfRelations;
	}
	public void setDeleteGeneralizationOfRelations( boolean deleteGeneralizationOfRelations) {
		this.deleteGeneralizationOfRelations = deleteGeneralizationOfRelations;
	}
	
	/**
	 * If <code>true</code> <code>this</code> term will be detached from the general term.
	 * If <code>false</code> the deletion will be aborted if <code>this</code> 
	 * term is kind of another term.<BR>
	 * Default value is <code>true</code>. 
	 */
	public boolean isDeleteKindOfRelations() {
		return deleteKindOfRelations;
	}
	public void setDeleteKindOfRelations(boolean deleteKindOfRelations) {
		this.deleteKindOfRelations = deleteKindOfRelations;
	}

}
