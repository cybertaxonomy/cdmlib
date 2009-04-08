// $Id: TaxonBeanProcessor.java 5561 2009-04-07 12:25:33Z a.kohlbecker $
/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.json.processor;

import java.util.List;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.remote.i18n.LocaleContext;

/**
 * @author a.kohlbecker
 *
 */
public class TermBaseBeanProcessor implements JsonBeanProcessor {

	public static final Logger logger = Logger.getLogger(TermBaseBeanProcessor.class);

	public JSONObject processBean(Object bean, JsonConfig jsonConfig) {
				
		TermBase term = (TermBase)bean;
		Representation representation;
		List<Language> languages = LocaleContext.getLanguages();
		if(Hibernate.isInitialized(term.getRepresentations())){
			representation = term.getPreferredRepresentation(languages);
			JSONObject json = new JSONObject().element("representationText", representation.getText());
			return json;
		} else {
			logger.warn("representation of term not initialized " + term.getUuid().toString());
			return new JSONObject(true);
		}
	}
	
}
