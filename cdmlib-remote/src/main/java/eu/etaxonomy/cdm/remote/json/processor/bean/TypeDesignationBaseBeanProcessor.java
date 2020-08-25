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

import org.hibernate.Hibernate;

import eu.etaxonomy.cdm.api.service.l10n.LocaleContext;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageTextHelper;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TextualTypeDesignation;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * @author a.kohlbecker
 * @since 09.07.2010
 *
 */
public class TypeDesignationBaseBeanProcessor extends AbstractCdmBeanProcessor<TypeDesignationBase> implements IMultilanguageTextBeanProcessor {


    private static final List<String> IGNORE_LIST = Arrays.asList("text");
    private boolean replaceMultilanguageText;


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.json.processor.bean.AbstractCdmBeanProcessor#processBeanSecondStep(eu.etaxonomy.cdm.model.common.CdmBase, net.sf.json.JSONObject, net.sf.json.JsonConfig)
	 */
	@Override
	public JSONObject processBeanSecondStep(TypeDesignationBase bean, JSONObject json, JsonConfig jsonConfig) {
		json.element("typeStatus", bean.getTypeStatus(), jsonConfig);
		if(bean.getClass().isAssignableFrom(SpecimenTypeDesignation.class)){
			json.element("typeSpecimen", ((SpecimenTypeDesignation)bean).getTypeSpecimen(), jsonConfig);
		} else if (bean.getClass().isAssignableFrom(NameTypeDesignation.class)){
			json.element("typeName", ((NameTypeDesignation)bean).getTypeName(), jsonConfig);
			json.element("citation", ((NameTypeDesignation)bean).getCitation(), jsonConfig);
		}

		if(bean instanceof TextualTypeDesignation){
    		List<Language> languages = LocaleContext.getLanguages();

    		TextualTypeDesignation textualTypeDesignation = (TextualTypeDesignation)bean;
            LanguageString languageString;
            if(Hibernate.isInitialized(textualTypeDesignation.getText())){
                languageString = MultilanguageTextHelper.getPreferredLanguageString(textualTypeDesignation.getText(), languages);
                if(languageString != null){
                    json.element("text_L10n", languageString, jsonConfig);
                }
                if(!isReplaceMultilanguageText()){
                    json.element("text", textualTypeDesignation.getText().values(), jsonConfig);
                }
            }
		}

		return json;
	}

	@Override
    public void setReplaceMultilanguageText(boolean replace) {
        replaceMultilanguageText = replace;
    }

    @Override
    public boolean isReplaceMultilanguageText() {
        return replaceMultilanguageText;
    }

    @Override
    public List<String> getMultilanguageTextIgnoreList() {
        return IGNORE_LIST;
    }

    @Override
    public List<String> getIgnorePropNames() {
        return getMultilanguageTextIgnoreList();
    }


}
