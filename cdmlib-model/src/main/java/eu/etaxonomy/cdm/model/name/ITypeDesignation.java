/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.name;

import java.util.Set;

/**
 * @author a.mueller
 * @created 07.08.2008
 * @version 1.0
 */
public interface ITypeDesignation {


	public boolean isLectoType();
	
	/** 
	 * Returns the {@link HomotypicalGroup homotypical group} that is typified
	 * in <i>this</i> type designation.
	 *  
	 * @see   #getTypeSpecimen()
	 */
	public HomotypicalGroup getHomotypicalGroup();
	
	/** 
	 * Returns the set of {@link TaxonNameBase taxon names} included in the
	 * {@link HomotypicalGroup homotypical group} typified in <i>this</i> type designation.
	 */
	public Set<TaxonNameBase> getTypifiedNames();

}
