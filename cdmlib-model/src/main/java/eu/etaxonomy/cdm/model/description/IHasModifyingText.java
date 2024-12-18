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
import eu.etaxonomy.cdm.model.common.MultilanguageText;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * @author a.mueller
 * @date 30.05.2023
 */
public interface IHasModifyingText {

    /**
     * Returns the {@link MultilanguageText multilanguage text} used to qualify the validity
     * of <i>this</i> information.  The different {@link LanguageString language strings}
     * contained in the multilanguage text should all have the same meaning.<BR>
     * A multilanguage text does not belong to a controlled {@link TermVocabulary term vocabulary}
     * as a {@link Modifier modifier} does.
     * <P>
     * NOTE: the actual content of <i>this</i> information is NOT
     * stored in the modifying text. This is only metainformation
     * (like "Some experts express doubt about this assertion").
     */
    public Map<Language,LanguageString> getModifyingText();

    /**
     * Creates a {@link LanguageString language string} based on the given text string
     * and the given {@link Language language} and adds it to the {@link MultilanguageText multilanguage text}
     * used to qualify the validity of <i>this</i> information.
     *
     * @param language  the language in which the text string is formulated
     * @param text      the string describing the validity
     *                  in a particular language
     *
     * @see             #getModifyingText()
     * @see             #putModifyingText(LanguageString)
     *
     */
    public LanguageString putModifyingText(Language language, String text);

    /**
     * Adds a translated {@link LanguageString text in a particular language}
     * to the {@link MultilanguageText multilanguage text} used to qualify the validity
     * of <i>this</i> information.
     *
     * @param description   the language string describing the validity
     *                      in a particular language
     * @see                 #getModifyingText()
     * @see                 #putModifyingText(Language, String)
     */
    public LanguageString putModifyingText(LanguageString languageText);

    /**
     * Removes from the {@link MultilanguageText multilanguage text} used to qualify the validity
     * of <i>this</i> information the one {@link LanguageString language string}
     * with the given {@link Language language}.
     *
     * @param  language the language in which the language string to be removed
     *                  has been formulated
     * @see             #getModifyingText()
     */
    public LanguageString removeModifyingText(Language language);

}