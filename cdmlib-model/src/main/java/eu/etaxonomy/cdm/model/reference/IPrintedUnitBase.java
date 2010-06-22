/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.reference;


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

	
	public String getEditor();
	
	public void setEditor(String editor);
	
	public String getSeriesPart();
	
	public void setSeriesPart(String seriesPart);
	
}
