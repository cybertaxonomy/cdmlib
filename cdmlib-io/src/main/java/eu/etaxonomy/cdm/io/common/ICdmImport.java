package eu.etaxonomy.cdm.io.common;


public interface ICdmImport<T extends IImportConfigurator> {

	public abstract boolean invoke(T tcsiConfig);

}