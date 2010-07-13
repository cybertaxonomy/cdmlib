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
 * This interface represents printed {@link IPublicationBase published references} which
 * are recurrent products of publishing companies or of research organizations.
 * In this case it is generally possible to distinguish authors, editors and
 * publishers. 
 */
public interface IPrintedUnitBase extends IPublicationBase, ISectionBase, IVolumeReference {

	/**
	 * Returns the print series of this printed unit
	 * @return
	 */
	public IPrintSeries getInSeries();
	
	/**
	 * Sets the print series of this printed unit
	 * @param series
	 */
	public void setInSeries(IPrintSeries series);

	
	/**
	 * Returns the editor of this reference
	 */
	public String getEditor();
	
	/**
	 * Sets the editor for this reference
	 * @param editor
	 */
	public void setEditor(String editor);
	
	/**
	 * Returns the series part for this printed unit
	 */
	public String getSeriesPart();
	
	/**
	 * Sets the series part for this printed unit
	 * @param seriesPart
	 */
	public void setSeriesPart(String seriesPart);
	
}
