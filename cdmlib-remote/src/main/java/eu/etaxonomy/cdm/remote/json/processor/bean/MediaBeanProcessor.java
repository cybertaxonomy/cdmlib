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
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.remote.l10n.LocaleContext;

/**
 * @author a.kohlbecker
 *
 */
public class MediaBeanProcessor extends AbstractCdmBeanProcessor<Media> {

	public static final Logger logger = Logger.getLogger(MediaBeanProcessor.class);

	private static final List<String> IGNORE_LIST = Arrays.asList(new String[] { 
			"title",
			"titleCache",
			"description",
			});

	private boolean replaceTitle = false;
	
	private boolean replaceDescription = false;

	public boolean isReplaceTitle() {
		return replaceTitle;
	}

	public void setReplaceTitle(boolean replaceTitle) {
		this.replaceTitle = replaceTitle;
	}

	public boolean isReplaceDescription() {
		return replaceDescription;
	}

	public void setReplaceDescription(boolean replaceDescription) {
		this.replaceDescription = replaceDescription;
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
	public JSONObject processBeanSecondStep(Media media, JSONObject json,	JsonConfig jsonConfig) {
		
		List<Language> languages = LocaleContext.getLanguages();
		LanguageString langString;
		
		// title
		if(Hibernate.isInitialized(media.getTitle())){
			langString = MultilanguageTextHelper.getPreferredLanguageString(media.getAllTitles(), languages);
			if(langString != null){
				if(langString.getText() != null && langString.getText().length() != 0){
					json.element("title_L10n", langString.getText());
				} 
			}
			if(!replaceTitle){
				json.element("title", media.getTitle(), jsonConfig);
			}
		} else {
			logger.debug("title of media not initialized  " + media.getUuid().toString());
		}
		
		// description
		if(Hibernate.isInitialized(media.getAllDescriptions())){
			langString = MultilanguageTextHelper.getPreferredLanguageString(media.getAllDescriptions(), languages);
			if(langString != null){
				if(langString.getText() != null && langString.getText().length() != 0){
					json.element("description_L10n", langString.getText());
				} 
			}
			if(!replaceDescription){
				json.element("description", media.getAllDescriptions(), jsonConfig);
			}
		} else {
			logger.debug("description of media not initialized  " + media.getUuid().toString());
		}
		
		return json;
	}
	
}
