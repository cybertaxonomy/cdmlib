/**
* Copyright (C) 2025 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.description;

import java.util.List;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TextData;

/**
 * @author kluther
 * @since 29.04.2025
 */
public class TextDataFormatter extends DesciptionElementFormatterBase<TextData>{

    /**
     * @param object
     * @param formatKeys
     * @param clazz
     */
    protected TextDataFormatter(Object object, FormatKey[] formatKeys) {
        super(object, formatKeys, TextData.class);
    }

    public static final TextDataFormatter NewInstance(FormatKey[] formatKeys) {
        return new TextDataFormatter(null, formatKeys);
    }

    @Override
    protected String doFormat(TextData textData, List<Language> preferredLanguages) {
        String text = null;
        if(textData.getFeature().equals(Feature.CITATION())){
            text = "";
            for(DescriptionElementSource source : textData.getSources()){
                if(source.getCitation() != null){
                    text += source.getCitation().getTitleCache();
                }
                if(source.getNameUsedInSource() != null){
                    text += " [" + source.getNameUsedInSource().getTitleCache() + "]";
                }
            }
            if(isBlank(text)){
                text = "No sources provided";
            }
        }else{
            LanguageString languageString = textData.getPreferredLanguageString(preferredLanguages);
            text = languageString != null ? languageString.getText() : "";
        }
        return isBlank(text) ? "No text provided" : text;
    }

}
