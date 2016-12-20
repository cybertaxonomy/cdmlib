/**
* Copyright (C) 2009 EDIT
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
import eu.etaxonomy.cdm.model.description.KeyStatement;
import eu.etaxonomy.cdm.remote.l10n.LocaleContext;

/**
 * @author a.kohlbecker
 * @date 24.03.2011
 *
 */
public class KeyStatementBeanProcessor extends AbstractBeanProcessor<KeyStatement> {

    @Override
    public List<String> getIgnorePropNames() {
        return null;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.json.processor.bean.AbstractBeanProcessor#processBeanSecondStep(java.lang.Object, net.sf.json.JSONObject, net.sf.json.JsonConfig)
     */
    @Override
    public JSONObject processBeanSecondStep(KeyStatement bean, JSONObject json,	JsonConfig jsonConfig) {

        List<Language> languages = LocaleContext.getLanguages();

        if(Hibernate.isInitialized(bean.getLabel())){
            LanguageString label = MultilanguageTextHelper.getPreferredLanguageString(bean.getLabel(), languages);
            json.element("label_l10n", label.getText());
        }

        return json;
    }



}
