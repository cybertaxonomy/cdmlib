package eu.etaxonomy.cdm.model.reference;


public interface IBookSection extends ISectionBase, INomenclaturalReference{

	public IBook getInBook();
	
	public void setInBook (IBook book);

}
