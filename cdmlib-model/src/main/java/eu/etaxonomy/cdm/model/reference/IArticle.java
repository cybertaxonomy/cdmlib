package eu.etaxonomy.cdm.model.reference;

public interface IArticle extends IVolumeReference, INomenclaturalReference{
	
	public void setSeries(String series);
	
	public String getSeries();
	
	public String getVolume();
	
	public void setVolume(String volume);

	public String getPages();

	public void setPages(String pages);

	public IJournal getInJournal();
	
	public void setInJournal(IJournal journal);
}
