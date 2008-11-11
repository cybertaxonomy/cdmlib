package eu.etaxonomy.cdm.io.tcsxml;

import org.jdom.Element;

import eu.etaxonomy.cdm.model.reference.ReferenceBase;

import org.apache.log4j.Logger;

public interface ITcsXmlPlaceholderClass {

	public abstract boolean makeMetaDataDetailed(
			TcsXmlImportConfigurator tcsConfig, Element elMetaDataDetailed);

	public abstract boolean makePublicationDetailed(
			TcsXmlImportConfigurator tcsConfig, Element elPublicationDetailed,
			ReferenceBase publication);

}