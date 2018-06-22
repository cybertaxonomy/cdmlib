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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Representation;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * @author a.kohlbecker
 *
 */
public class RepresentationBeanProcessor extends AbstractCdmBeanProcessor<Representation> {

    public static final Logger logger = Logger.getLogger(RepresentationBeanProcessor.class);

    private static final List<String> IGNORE_LIST = Arrays.asList(new String[] {
//            "representations",
            });



    @Override
    public List<String> getIgnorePropNames() {
        return IGNORE_LIST;
    }

    @Override
    public JSONObject processBeanSecondStep(Representation representation, JSONObject json,	JsonConfig jsonConfig) {

        if (representation.getText() != null){
            json.element("description", representation.getText());
        }
        Language language = representation.getLanguage();
        if (language != null) {
            String iso = StringUtils.isEmpty(language.getIso639_2())? language.getIso639_1() : language.getIso639_2();
            if (iso != null){
                json.element("languageIso", iso);
            }
            json.element("languageUuid", language.getUuid().toString());
        }

        return json;
    }

}
