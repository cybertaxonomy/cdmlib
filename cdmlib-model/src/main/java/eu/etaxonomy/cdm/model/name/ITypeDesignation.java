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

import eu.etaxonomy.cdm.model.reference.ReferenceBase;

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

//	/**
//	 * @see   #getTypifiedNames()
//	 */
//	public void setTypifiedNames(Set<TaxonNameBase> typifiedNames); 

	
	/** 
	 * Returns the details string of the reference corresponding to <i>this</i> taxon 
	 * type designation if it is a lectotype. The details describe the exact
	 * localisation within the publication used for the lectotype assignation.
	 * These are mostly (implicitly) pages but can also be figures or tables or
	 * any other element of a publication. A lectotype micro reference (details)
	 * requires the existence of a lectotype reference.
	 * 
	 * @see   #getLectoTypeReference()
	 */
	public String getLectoTypeMicroReference();
	
	/**
	 * @see   #getLectoTypeMicroReference()
	 */
	public void setLectoTypeMicroReference(String lectoTypeMicroReference);

	/** 
	 * Returns the {@link reference.ReferenceBase reference} used in case <i>this</i> 
	 * taxon type designation is a lectotype. This reference is different
	 * to the nomenclatural reference of the typified taxon name.
	 *  
	 * @see   #isLectoType()
	 */
	public ReferenceBase getLectoTypeReference();
	
	/**
	 * @see   #getLectoTypeReference()
	 */
	public void setLectoTypeReference(ReferenceBase lectoTypeReference);
	
}
