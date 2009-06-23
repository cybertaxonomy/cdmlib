package eu.etaxonomy.cdm.io.common;


public interface ICdmImporter<T extends IImportConfigurator> {

	public abstract boolean invoke(T config);

}