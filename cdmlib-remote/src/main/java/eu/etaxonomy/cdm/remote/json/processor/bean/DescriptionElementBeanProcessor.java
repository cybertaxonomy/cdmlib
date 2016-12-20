/**
* Copyright (C) 2013 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.List;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.hibernate.Hibernate;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageTextHelper;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.remote.l10n.LocaleContext;


/**
 *
 *
 * @author a.kohlbecker
 * @date Dec 4, 2013
 *
 */
public class DescriptionElementBeanProcessor extends AbstractModifiableThingBeanProcessor<DescriptionElementBase> {

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
        return json;
    }


}
