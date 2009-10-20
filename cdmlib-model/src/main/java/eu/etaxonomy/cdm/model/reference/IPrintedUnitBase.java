package eu.etaxonomy.cdm.model.reference;

import eu.etaxonomy.cdm.model.common.CdmBase;

public interface IPrintedUnitBase extends IPublicationBase, IVolumeReference {

	public IPrintSeries getInSeries();
	
	public void setInSeries(IPrintSeries inSeries);

	public String getEditor();
	
	public void setEditor(String editor);
	
	public String getVolume();
	
	public void setVolume(String volume);
	
	public String getPages();
	
	public void setPages(String pages);
	
	
}
