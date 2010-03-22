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

import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;


/**
 * @author a.mueller
 * @created 15.03.2010
 * @version 1.0
 */
public interface IInputTransformer {
	
	public Feature getFeatureByKey(String key) throws UndefinedTransformerMethodException;
	
	public UUID getFeatureUuid(String key) throws UndefinedTransformerMethodException;
	
	public Language getLanguageByKey(String key) throws UndefinedTransformerMethodException;
	
	public UUID getLanguageUuid(String key) throws UndefinedTransformerMethodException;

	public ExtensionType getExtensionTypeByKey(String key) throws UndefinedTransformerMethodException;
	
	public UUID getExtensionTypeUuid(String key) throws UndefinedTransformerMethodException;

	public NameTypeDesignationStatus getNameTypeDesignationStatusByKey(String key) throws UndefinedTransformerMethodException;
	
	public UUID getNameTypeDesignationStatusUuid(String key) throws UndefinedTransformerMethodException;
 
	
}
