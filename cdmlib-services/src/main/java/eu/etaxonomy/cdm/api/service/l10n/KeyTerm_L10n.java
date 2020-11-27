/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.l10n;

import java.util.List;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.term.IKeyTerm;

/**
 * @author a.kohlbecker
 * @since May 28, 2020
 */
public class KeyTerm_L10n<T extends IKeyTerm> {

    private T keyTerm;

    public KeyTerm_L10n(T enumTerm) {
        this.keyTerm = enumTerm;
    }

    public String localizedLabel() {

        List<Language> languages = LocaleContext.getLanguages();
        String label_L10n = null;
        if(languages != null){
            for(Language language : languages) {
                label_L10n = keyTerm.getLabel(language);
                if(label_L10n != null){
                    return label_L10n;
                }
            }
        }
        label_L10n = keyTerm.getLabel();

        return label_L10n;
    }

}
