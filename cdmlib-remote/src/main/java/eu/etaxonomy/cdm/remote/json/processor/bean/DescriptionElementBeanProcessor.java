/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.Arrays;
import java.util.List;

import org.hibernate.Hibernate;

import eu.etaxonomy.cdm.api.service.l10n.LocaleContext;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageTextHelper;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TextData;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;


/**
 *
 *
 * @author a.kohlbecker
 * @since Dec 4, 2013
 *
 */
public class DescriptionElementBeanProcessor extends AbstractModifiableThingBeanProcessor<DescriptionElementBase> {

    private static final List<String> IGNORE_LIST = Arrays.asList(new String[]{"modifyingText", "multilanguageText"});


    @Override
    public JSONObject processBeanSecondStep(DescriptionElementBase bean, JSONObject json, JsonConfig jsonConfig) {

        List<Language> languages = LocaleContext.getLanguages();

        // --- general processing for all DescriptionElement types
        if(Hibernate.isInitialized(bean.getModifyingText())){
            LanguageString modifyingText = MultilanguageTextHelper.getPreferredLanguageString(bean.getModifyingText(), languages);
            if(modifyingText != null){
                json.element("modifyingText_l10n", modifyingText.getText());
            }
            if(!isReplaceMultilanguageText()){
                json.element("modifyingText", bean.getModifyingText().values(), jsonConfig);
            }
        }

        // --- special processing for specific types
        if(TextData.class.isAssignableFrom(bean.getClass())){
            TextData textdata = (TextData) bean;
            LanguageString languageString;
            //textdata.getSources().iterator().next()
            textdata = HibernateProxyHelper.deproxy(textdata, TextData.class);
            if(Hibernate.isInitialized(textdata)){
	            if(Hibernate.isInitialized(textdata.getMultilanguageText())){
	                languageString = MultilanguageTextHelper.getPreferredLanguageString(textdata.getMultilanguageText(), languages);
	                if(languageString != null){
	                    json.element("multilanguageText_L10n", languageString, jsonConfig);
	                }
	                if(!isReplaceMultilanguageText()){
	                    json.element("multilanguageText", textdata.getMultilanguageText().values(), jsonConfig);
	                }
	            }
            }
        }
        return json;
    }

    @Override
    public List<String> getMultilanguageTextIgnoreList() {
        return IGNORE_LIST;
    }


}
