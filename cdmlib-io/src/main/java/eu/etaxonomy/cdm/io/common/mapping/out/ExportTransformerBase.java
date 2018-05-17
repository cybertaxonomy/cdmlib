/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;

/**
 * @author a.mueller
 * @since 15.03.2010
 * @version 1.0
 */
public class ExportTransformerBase implements IExportTransformer {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExportTransformerBase.class);

	@Override
	public Object getKeyByFeature(Feature feature) throws UndefinedTransformerMethodException {
		String warning = "getKeyByFeature is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	@Override
	public String getCacheByFeature(Feature feature) throws UndefinedTransformerMethodException {
		String warning = "getCacheByFeature is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	@Override
	public Object getKeyByLanguage(Language language) throws UndefinedTransformerMethodException {
		String warning = "getKeyByLanguage is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer#getCacheByLanguage(eu.etaxonomy.cdm.model.common.Language)
	 */
	@Override
	public String getCacheByLanguage(Language language) throws UndefinedTransformerMethodException {
		String warning = "getCacheByLanguage is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer#getKeyByExtensionType(eu.etaxonomy.cdm.model.common.ExtensionType)
	 */
	@Override
	public Object getKeyByExtensionType(ExtensionType extensionType) throws UndefinedTransformerMethodException {
		String warning = "getKeyByExtensionType is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}	@Override
	public String getCacheByExtensionType(ExtensionType extensionType) throws UndefinedTransformerMethodException {
		String warning = "getCacheByExtensionType is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	@Override
	public Object getKeyByPresenceAbsenceTerm(PresenceAbsenceTerm term) throws UndefinedTransformerMethodException {
		String warning = "getKeyByPresenceAbsenceTerm is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	@Override
	public String getCacheByPresenceAbsenceTerm(PresenceAbsenceTerm term) throws UndefinedTransformerMethodException {
		String warning = "getKeyByPresenceAbsenceTerm is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	@Override
	public Object getKeyByNamedArea(NamedArea area) throws UndefinedTransformerMethodException {
		String warning = "getKeyByNamedArea is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}
	
	@Override
	public String getCacheByNamedArea(NamedArea area) throws UndefinedTransformerMethodException{
		String warning = "getCacheByNamedArea is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}
	
	@Override
	public String getCacheByNomStatus(NomenclaturalStatusType status) throws UndefinedTransformerMethodException {
		String warning = "getCacheByNomStatus is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}
	
	
	@Override
	public Object getKeyByNomStatus(NomenclaturalStatusType status) throws UndefinedTransformerMethodException {
		String warning = "getKeyByNomStatus is not yet implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}
	@Override
	public String getQualityStatusCacheByKey(Integer qualityStatusFk) throws UndefinedTransformerMethodException {
		String warning = "getQualityStatusCacheByKey is not yet implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}
	@Override
	public String getTaxonStatusCacheByKey(Integer taxonStatusId) throws UndefinedTransformerMethodException {
		String warning = "getTaxonStatusCacheByKey is not yet implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

		
}
