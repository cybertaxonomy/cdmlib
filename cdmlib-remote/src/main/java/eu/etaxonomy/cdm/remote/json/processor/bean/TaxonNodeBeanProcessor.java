/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import eu.etaxonomy.cdm.api.service.l10n.LocaleContext;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageTextHelper;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * @author n.hoffmann
 * @since Apr 9, 2010
 * @version 1.0
 */
public class TaxonNodeBeanProcessor extends AbstractCdmBeanProcessor<TaxonNode>  implements IMultilanguageTextBeanProcessor {

	private static final Logger logger = Logger.getLogger(TaxonNodeBeanProcessor.class);

	 private static final List<String> IGNORE_LIST = Arrays.asList("text");

	 private boolean replaceMultilanguageText;

	@Override
	public List<String> getIgnorePropNames() {
		return Arrays.asList(new String[]{
				"parent", //TODO put in json-config ignore list ?
		});
	}

	@Override
	public JSONObject processBeanSecondStep(TaxonNode bean, JSONObject json,
			JsonConfig jsonConfig) {

	    List<Language> languages = LocaleContext.getLanguages();

	    TaxonNode taxonNode = bean;
        LanguageString languageString;
        if(Hibernate.isInitialized(taxonNode.getStatusNote())){
            languageString = MultilanguageTextHelper.getPreferredLanguageString(taxonNode.getStatusNote(), languages);
            if(languageString != null){
                json.element("statusNote_L10n", languageString, jsonConfig);
            }
            if(isReplaceMultilanguageText()){
                json.remove("statusNote");
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
}
