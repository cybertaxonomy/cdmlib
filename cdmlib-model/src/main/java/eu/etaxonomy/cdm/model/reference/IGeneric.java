package eu.etaxonomy.cdm.model.reference;

public interface IGeneric extends IPublicationBase, IVolumeReference, INomenclaturalReference{

	public String getEditor();
	
	public void setEditor(String editor);
	
	public String getSeries();
	
	public void setSeries(String series);
	
	public String getVolume();
	
	public void setVolume(String volume);
	
	public String getPages();
	
	public void setPages(String pages);
	
}
