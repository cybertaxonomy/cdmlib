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

import eu.etaxonomy.cdm.api.service.l10n.LocaleContext;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MultilanguageTextHelper;
import eu.etaxonomy.cdm.persistence.dto.TaxonNodeDto;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

public class TaxonNodeDtoBeanProcessor extends AbstractBeanProcessor<TaxonNodeDto> implements IMultilanguageTextBeanProcessor {

    private static final List<String> IGNORE_LIST = Arrays.asList(new String[]{"statusNote"});

    private boolean replaceMultilanguageText = false;

    @Override
    public JSONObject processBeanSecondStep(TaxonNodeDto bean, JSONObject json, JsonConfig jsonConfig) {

        List<Language> languages = LocaleContext.getLanguages();
        if(!bean.getStatusNote().isEmpty()) {
            String statusNoteText = MultilanguageTextHelper.getPreferredLanguageObject(bean.getStatusNote(), languages);
            json.element("statusNote_L10n", statusNoteText);

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
