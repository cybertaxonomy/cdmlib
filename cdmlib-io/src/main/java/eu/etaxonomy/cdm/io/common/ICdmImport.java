package eu.etaxonomy.cdm.io.common;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.tcs.TcsImportConfigurator;

public interface ICdmImport {

	public abstract boolean invoke(TcsImportConfigurator tcsiConfig);

}