// $Id$
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
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * @author a.mueller
 * @created 15.03.2010
 * @version 1.0
 */
public class ExportTransformerBase implements IExportTransformer {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExportTransformerBase.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer#getKeyByFeature(eu.etaxonomy.cdm.model.description.Feature)
	 */
	@Override
	public Feature getKeyByFeature(Feature feature) throws UndefinedTransformerMethodException {
		String warning = "getKeyByFeature is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer#getCacheByFeature(eu.etaxonomy.cdm.model.description.Feature)
	 */
	@Override
	public String getCacheByFeature(Feature feature) throws UndefinedTransformerMethodException {
		String warning = "getCacheByFeature is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer#getKeyByLanguage(eu.etaxonomy.cdm.model.common.Language)
	 */
	@Override
	public Language getKeyByLanguage(Language language) throws UndefinedTransformerMethodException {
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
	public ExtensionType getKeyByExtensionType(ExtensionType extensionType) throws UndefinedTransformerMethodException {
		String warning = "getKeyByExtensionType is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer#getKeyByPresenceAbsenceTerm(eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase)
	 */
	@Override
	public Object getKeyByPresenceAbsenceTerm(PresenceAbsenceTermBase term) throws UndefinedTransformerMethodException {
		String warning = "getKeyByPresenceAbsenceTerm is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer#getCacheByPresenceAbsenceTerm(eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase)
	 */
	@Override
	public String getCacheByPresenceAbsenceTerm(PresenceAbsenceTermBase term) throws UndefinedTransformerMethodException {
		String warning = "getKeyByPresenceAbsenceTerm is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer#getKeyByNamedArea(eu.etaxonomy.cdm.model.location.NamedArea)
	 */
	@Override
	public Object getKeyByNamedArea(NamedArea area) throws UndefinedTransformerMethodException {
		String warning = "getKeyByNamedArea is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer#getCacheByNamedArea(eu.etaxonomy.cdm.model.location.NamedArea)
	 */
	@Override
	public String getCacheByNamedArea(NamedArea area) throws UndefinedTransformerMethodException{
		String warning = "getCacheByNamedArea is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	
}
