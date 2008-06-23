package eu.etaxonomy.cdm.io.common;

public interface IIO<T extends ImportConfiguratorBase> {

	public boolean check(T bmiConfig);

	
}