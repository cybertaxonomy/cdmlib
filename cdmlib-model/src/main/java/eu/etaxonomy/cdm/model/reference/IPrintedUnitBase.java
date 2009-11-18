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

	public IPrintSeries getInSeries();
	
	public void setInSeries(IPrintSeries inSeries);

	public String getEditor();
	
	public void setEditor(String editor);
	
	public String getSeriesPart();
	
	public void setSeriesPart(String seriesPart);
	
}
