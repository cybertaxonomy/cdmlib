/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.reference;

/**
 * This interface represents isolated parts (usually papers or abstracts) within
 * {@link IProceedings conference proceedings}.
 * <P>
 * This class corresponds, according to the TDWG ontology, partially to the
 * publication type term (from PublicationTypeTerm): "SubReference".
 */
public interface IInProceedings extends ISection{
	

	/**
	 * Returns the proceedings for these inProceedings
	 */
	public IProceedings getInProceedings();
	
	/**
	 * Sets the proceedings for these inProceedings
	 * @param inProceedings
	 */
	public void setInProceedings(IProceedings inProceedings);
	
}
