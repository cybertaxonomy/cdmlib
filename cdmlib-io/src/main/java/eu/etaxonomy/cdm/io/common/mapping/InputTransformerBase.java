// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;

/**
 * @author a.mueller
 * @created 15.03.2010
 * @version 1.0
 */
public class InputTransformerBase implements IInputTransformer {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(InputTransformerBase.class);

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getFeatureByKey(java.lang.String)
	 */
	public Feature getFeatureByKey(String key) throws UndefinedTransformerMethodException {
		String warning = "getFeatureByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getFeatureUuid(java.lang.String)
	 */
	public UUID getFeatureUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getFeatureByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);

	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getLanguageByKey(java.lang.String)
	 */
	public Language getLanguageByKey(String key) throws UndefinedTransformerMethodException {
		String warning = "getLanguageByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);

	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getLanguageUuid(java.lang.String)
	 */
	public UUID getLanguageUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getLanguageByUuid is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getExtensionTypeByKey(java.lang.String)
	 */
	public ExtensionType getExtensionTypeByKey(String key) throws UndefinedTransformerMethodException {
		String warning = "getExtensionTypeByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getExtensionTypeUuid(java.lang.String)
	 */
	public UUID getExtensionTypeUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getExtensionTypeUuid is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getNameTypeDesignationStatusByKey(java.lang.String)
	 */
	public NameTypeDesignationStatus getNameTypeDesignationStatusByKey(String key) throws UndefinedTransformerMethodException {
		String warning = "getNameTypeDesignationStatusByKey is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IInputTransformer#getNameTypeDesignationStatusUuid(java.lang.String)
	 */
	public UUID getNameTypeDesignationStatusUuid(String key) throws UndefinedTransformerMethodException {
		String warning = "getNameTypeDesignationStatusUuid is not implemented in implementing transformer class";
		throw new UndefinedTransformerMethodException(warning);
	}
	
	
	
}
