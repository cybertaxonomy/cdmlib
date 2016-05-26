/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.reference;

import eu.etaxonomy.cdm.strategy.cache.reference.old.GenericDefaultCacheStrategy;

/**
 * This interface represents all references which cannot be clearly assigned to a
 * particular {@link StrictReferenceBase reference} subclass. Therefore attributes which are
 * characteristic for a unique reference subclass are not necessary here.
 * <P>
 * This class corresponds, according to the TDWG ontology, to the publication type
 * terms (from PublicationTypeTerm): <ul>
 * <li> "Generic"
 * <li> "Artwork"
 * <li> "AudiovisualMaterial"
 * <li> "ComputerProgram"
 * <li> "Determination"
 * <li> "Commentary"
 * <li> "SubReference"
 * </ul>
 */
public interface IGeneric extends IPublicationBase, INomenclaturalReference, IVolumeReference{

	/**
	 * Returns the editor of this generic reference
	 */
	public String getEditor();
	
	/**
	 * Sets the editor for this generic reference
	 * @param editor
	 */
	public void setEditor(String editor);
	
	/**
	 * Returns the series of this generic reference
	 */
	public String getSeriesPart();
	
	/**
	 * Sets the series for this generic reference
	 * @param series
	 */
	public void setSeriesPart(String series);
	
	/**
	 * Returns the pages (page span this reference covers in its in-reference) 
	 */
	public String getPages();
	
	/**
	 * Sets the pages (page span this reference covers in its in-reference) 
	 * @param pages
	 */
	public void setPages(String pages);

	/**
	 * Returns the inreference of this generic reference
	 */
	public IGeneric getInReference();
	
	/**
	 * Sets the inreference.
	 * @param inReference
	 */
	public void setInReference(Reference inReference);

	void setCacheStrategy(GenericDefaultCacheStrategy cacheStratefy);
	
}
