package eu.etaxonomy.cdm.search;

public interface ICdmMassIndexer {

	public abstract void reindex();

	public abstract void purge();

}