/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.model.reference;


public interface IPrintedUnitBase extends IPublicationBase, IVolumeReference {

	/**
	 * Same as {@link #getSeries()}
	 * @deprecated use {@link #getSeries()} instead
	 * @return
	 */
	@Deprecated
	public IPrintSeries getInSeries();
	
	/**
	 * Same as {@link #setInSeries(IPrintSeries)}
	 * @deprecated use {@link #setInSeries(IPrintSeries)} instead
	 * @param series
	 */
	@Deprecated
	public void setInSeries(IPrintSeries series);

	
	/**
	 * Returns the print series of this printed unit
	 * @return
	 */
	public IPrintSeries getSeries();
	
	/**
	 * Sets the pritn series of this printed unit
	 * @param series
	 */
	public void setSeries(IPrintSeries series);

	
	public String getEditor();
	
	public void setEditor(String editor);
	
	public String getSeriesPart();
	
	public void setSeriesPart(String seriesPart);
	
}
