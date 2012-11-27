// $Id: TaxonBeanProcessor.java 5561 2009-04-07 12:25:33Z a.kohlbecker $
/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageTextHelper;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.remote.l10n.LocaleContext;

/**
 * @author a.kohlbecker
 *
 */
public class TextDataBeanProcessor extends AbstractCdmBeanProcessor<TextData> {

	public static final Logger logger = Logger.getLogger(TextDataBeanProcessor.class);

	private static final List<String> IGNORE_LIST = Arrays.asList(new String[]{"multilanguageText"});

	private boolean replaceMultilanguageText = false;
	
	public boolean isReplaceMultilanguageText() {
		return replaceMultilanguageText;
	}

	public void setReplaceMultilanguageText(boolean replace) {
		this.replaceMultilanguageText = replace;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.json.processor.AbstractCdmBeanProcessor#getIgnorePropNames()
	 */
	@Override
	public List<String> getIgnorePropNames() {
		return IGNORE_LIST;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.json.processor.AbstractCdmBeanProcessor#processBeanSecondStep(eu.etaxonomy.cdm.model.common.CdmBase, net.sf.json.JSONObject, net.sf.json.JsonConfig)
	 */
	@Override
	public JSONObject processBeanSecondStep(TextData bean, JSONObject json,	JsonConfig jsonConfig) {
		
		TextData textdata = (TextData)bean;
		LanguageString languageString;
		List<Language> languages = LocaleContext.getLanguages();
		//textdata.getSources().iterator().next()
		if(Hibernate.isInitialized(textdata.getMultilanguageText())){
			languageString = MultilanguageTextHelper.getPreferredLanguageString(textdata.getMultilanguageText(), languages);
			if(languageString != null){
				json.element("multilanguageText_L10n", languageString, jsonConfig);
			}
			if(!replaceMultilanguageText){
				json.element("multilanguageText", textdata.getMultilanguageText().values(), jsonConfig);
			}
		}
		return json;
	}
	
}
