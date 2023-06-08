/**
* Copyright (C) 2023 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.description;

import java.util.Map;

import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;

/**
 * @author a.mueller
 * @date 30.05.2023
 */
public interface IHasModifyingText {

    public Map<Language,LanguageString> getModifyingText();

    public LanguageString putModifyingText(Language language, String text);

    public LanguageString putModifyingText(LanguageString languageText);

    public LanguageString removeModifyingText(Language language);

}