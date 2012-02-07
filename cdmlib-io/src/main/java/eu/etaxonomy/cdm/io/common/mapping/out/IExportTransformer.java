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

import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.location.NamedArea;


/**
 * Interface for import and export transformer classes. Mainly to transform defined terms.
 * @author a.mueller
 * @created 15.03.2010
 * @version 1.0
 */
public interface IExportTransformer {
	
	//Feature
	public Object getKeyByFeature(Feature feature) throws UndefinedTransformerMethodException;
	public String getCacheByFeature(Feature feature) throws UndefinedTransformerMethodException;
	
	//Language
	public Object getKeyByLanguage(Language language) throws UndefinedTransformerMethodException;
	public String getCacheByLanguage(Language language) throws UndefinedTransformerMethodException;
	
	//Extension Type
	public Object getKeyByExtensionType(ExtensionType extensionType) throws UndefinedTransformerMethodException;
	
	//Presence Term
	public Object getKeyByPresenceAbsenceTerm(PresenceAbsenceTermBase term) throws UndefinedTransformerMethodException;
	public String getCacheByPresenceAbsenceTerm(PresenceAbsenceTermBase term) throws UndefinedTransformerMethodException;

	//NamedArea
	public Object getKeyByNamedArea(NamedArea area) throws UndefinedTransformerMethodException;	
	public String getCacheByNamedArea(NamedArea area) throws UndefinedTransformerMethodException;	


}
