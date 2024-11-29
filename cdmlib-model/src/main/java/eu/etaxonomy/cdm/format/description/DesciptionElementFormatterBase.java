/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.description;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.format.DefaultCdmFormatter;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.IHasModifyingText;
import eu.etaxonomy.cdm.model.description.IModifiable;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.Representation;

/**
 * Formatter base class for {@link DescriptionElementBase} instances.
 *
 * @author a.mueller
 * @since 12.03.2020
 */
public abstract class DesciptionElementFormatterBase<T extends DescriptionElementBase>
        extends DefaultCdmFormatter{

    protected static final String MISSING_TERM_LABEL = "-no state-"; //TODO

    private Class<T> clazz;

    protected DesciptionElementFormatterBase(Object object, FormatKey[] formatKeys, Class<T> clazz) {
        super(object, formatKeys);
        this.clazz = clazz;
    }

    @Override
    public String format(Object object, FormatKey... formatKeys) {
        if (formatKeys == null){
            return format(object);
        }else{
            throw new UnsupportedOperationException("Formatting with FormatKey is not yet supported by " + this.getClass().getSimpleName());
        }
    }

    @Override
    public String format(Object object) {
        List<Language> preferredLanguages = new ArrayList<>();
        preferredLanguages.add(Language.DEFAULT());
        return format(object, preferredLanguages);
    }

    public String format(Object object, Language preferredLanguage) {
        List<Language> languages = preferredLanguage == null ? new ArrayList<>()
                : Arrays.asList( new Language[] {preferredLanguage});
        return format(object, languages);
    }

    public String format(Object object, List<Language> preferredLanguages) {
        if (! (object instanceof ICdmBase)){
            throw new IllegalArgumentException("object is not of type ICdmBase");
        }
        ICdmBase cdmBase = (ICdmBase)object;
        if (!cdmBase.isInstanceOf(clazz)){
            throw new IllegalArgumentException("object is not of type " + clazz.getSimpleName());
        }
        T descEl = CdmBase.deproxy(object, clazz);
        if (preferredLanguages == null || preferredLanguages.isEmpty()){
            preferredLanguages = new ArrayList<>();
            preferredLanguages.add(Language.DEFAULT());
        }
        String result = doFormat(descEl, preferredLanguages);

        result = handleModifiers(result, descEl, preferredLanguages, " ");
        return result;
    }

    protected String handleModifiers(String result, IModifiable modifiable, List<Language> preferredLanguages,
            String modifierSep) {

        for (DefinedTerm modifier : modifiable.getModifiers()) {
            //TODO order modifiers  (see value.getModifiers(voc)
            Representation prefLabel = modifier.getPreferredRepresentation(preferredLanguages);
            String label = prefLabel!= null ? prefLabel.getLabel() : "";
            //TODO modifier separator between 2 modifiers might be different
            result = CdmUtils.concat(modifierSep, label, result);
        }

        if ( modifiable instanceof IHasModifyingText){
            IHasModifyingText hasModifyingText = (IHasModifyingText)modifiable;
            if (hasModifyingText.getModifyingText() != null && !hasModifyingText.getModifyingText().isEmpty()) {
                Map<Language, LanguageString> modifyingText = hasModifyingText.getModifyingText();
                String modText = getPreferredModifyingText(modifyingText, preferredLanguages);
                result = CdmUtils.concat(" ", modText, result);
            }
        }
        return result;
    }

    private String getPreferredModifyingText(Map<Language, LanguageString> modifyingText,
            List<Language> preferredLanguages) {
        for (Language lang : preferredLanguages) {
            if (modifyingText.get(lang) != null) {
                return modifyingText.get(lang).getText();
            }
        }
        for (Map.Entry<Language, LanguageString> entry : modifyingText.entrySet()){
            String text = entry.getValue().getText();
            if (text != null){  //Note: or do we also want to exclude empty/blank texts?
                return text;
            }
        }
        return null;
    }

    /**
     * To be implemented by subclasses. The caller must guarantee that preferredLanguages
     * is neither <code>null</code> nor empty.
     */
    protected abstract String doFormat(T descEl, List<Language> preferredLanguages);

    protected String getLabel(DefinedTermBase<?> term, List<Language> preferredLanguages) {
        return getLabel(term, preferredLanguages, false);
    }

    protected String getLabel(DefinedTermBase<?> term, List<Language> preferredLanguages, boolean usePlural) {
        if (term == null){
            return MISSING_TERM_LABEL;
        }
        Representation representation = term.getPreferredRepresentation(preferredLanguages);
        if (representation == null){
            return term.toString();
        }
        if (isNotBlank(representation.getLabel())){
            return usePlural && isNotBlank(representation.getPlural()) ? representation.getPlural() : representation.getLabel();
        }else if (isNotBlank(representation.getAbbreviatedLabel())){
            return representation.getAbbreviatedLabel();
        }else if (isNotBlank(representation.getText())){
            return representation.getText();
        }else{
            return representation.toString();
        }
    }
}