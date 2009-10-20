package eu.etaxonomy.cdm.model.reference;

public interface IBook extends IPrintedUnitBase, INomenclaturalReference{
	
	public void setEdition(String edition);
	
	public String getEdition();
	
	public String getIsbn();
	
	public void setIsbn(String isbn);
	
	


}
