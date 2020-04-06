/**
* Copyright (C) 2019 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model.name;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Store;

import eu.etaxonomy.cdm.hibernate.search.MultilanguageTextFieldBridge;
import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * The class representing a typification of one or several {@link TaxonName taxon names} by text only.<BR>
 * This is for<BR>
 *   1. verbatim type citations<BR>
 *   2. rapid data entry<BR>
 *   3. type information that can not be atomized with current data model<BR>
 *
 * @author a.mueller
 * @since 23.01.2019
 */
@XmlRootElement(name = "TextualTypeDesignation")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TextualTypeDesignation", propOrder = {
      "text",
      "isVerbatim"
})
@Entity
@Audited
public class TextualTypeDesignation extends TypeDesignationBase<SpecimenTypeDesignationStatus> {

    private static final long serialVersionUID = 7610574857727305296L;

    @XmlElement(name = "Text")
    @XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
    @OneToMany (fetch= FetchType.LAZY, orphanRemoval=true)
    @MapKeyJoinColumn(name="text_mapkey_id")
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE, CascadeType.DELETE})
    @Field(name="text", store=Store.YES)
    @FieldBridge(impl=MultilanguageTextFieldBridge.class)
    @NotNull
    private Map<Language, LanguageString> text = new HashMap<>();

    private boolean isVerbatim;

    @XmlTransient
    @Transient
    private boolean isHashMapHibernateBugFixed = false;

//********************** FACTORY *********************************/

    public static TextualTypeDesignation NewInstance() {
        TextualTypeDesignation result = new TextualTypeDesignation();
        return result;
    }

    public static TextualTypeDesignation NewInstance(String text, Language language, boolean isVerbatim,
            Reference citation, String citationMicroReference, String originalNameString) {
        TextualTypeDesignation result = new TextualTypeDesignation(text, language, isVerbatim,
                citation, citationMicroReference, originalNameString);
        return result;
    }



//********************** CONSTRUCTOR *********************************/

    protected TextualTypeDesignation() {
        super();
    }

    protected TextualTypeDesignation(String text, Language language, boolean isVerbatim, Reference citation, String citationMicroReference, String originalNameString) {
        super(citation, citationMicroReference, originalNameString);
        language = Language.UNDETERMINED();
        LanguageString ls = LanguageString.NewInstance(text, language);
        this.putText(ls);
        this.setVerbatim(isVerbatim);
    }

//********************** GETTER /SETTER *********************************/

    public Map<Language, LanguageString> getText() {
        return text;
    }

    /**
     * Creates a {@link LanguageString language string} based on the given text string
     * and the given {@link Language language}, returns it and adds it to the multilanguage
     * text representing the content of <i>this</i> text data.
     *
     * @param language  the language in which the text string is formulated
     * @param text      the string representing the content of the text data
     *                  in a particular language
     *
     * @see             #getMultilanguageText()
     * @see             #putText(LanguageString)
     * @return          the previous language string associated with the given Language, or null if there was no mapping for the given Language
     */
    public LanguageString putText(Language language, String text) {
        fixHashMapHibernateBug();
        //** end workaround
        LanguageString languageString = this.text.get(language);
        if (languageString != null){
            languageString.setText(text);
        }else{
            languageString = LanguageString.NewInstance(text, language);
        }
        this.text.put(language , languageString);
        return languageString;
    }


    /**
     * Adds a translated {@link LanguageString text in a particular language}
     * to the multi-language text representing the content of <i>this</i> text data.
     * The given language string will be returned.
     *
     * @param languageString    the language string representing the content of
     *                          the text data in a particular language
     * @see                     #getText()
     * @see                     #putText(String, Language)
     * @see                     HashMap#put(Object, Object)
     * @return                  the previous language string associated with key, or null if there was no mapping for key
     */
    public LanguageString putText(LanguageString languageString) {
        if (languageString == null){
            return null;
        }else{
            Language language = languageString.getLanguage();
            return this.text.put(language, languageString);
        }
    }
    /**
     * Removes from the multilanguage representing the content of
     * <i>this</i> text data the one {@link LanguageString language string}
     * with the given {@link Language language}. Returns the removed
     * language string.
     *
     * @param  language the language in which the language string to be removed
     *                  has been formulated
     * @return          the language string associated with the given language or null if there was no mapping for the given Language
     * @see             #getMultilanguageText()
     */
    public LanguageString removeText(Language language) {
        fixHashMapHibernateBug();
        return this.text.remove(language);
    }
    /**
     * Returns the multi-language text with the content of <i>this</i> text data for
     * a specific language.
     *
     * @param language the language in which the text string looked for is formulated
     * @return
     */
    public LanguageString getLanguageText(Language language){
        return getText().get(language);
    }
    public String getText(Language language){
        if (getText().get(language) != null){
            return getText().get(language).getText();
        }else {
            return null;
        }
    }


    /**
     * Flag indicating if this textual type designation is a citation (e.g. original citation).
     * This may have influence on the correct formatting of type designations.
     */
    public boolean isVerbatim() {
        return isVerbatim;
    }

    public void setVerbatim(boolean isVerbatim) {
        this.isVerbatim = isVerbatim;
    }

    //copy from TextData
    private void fixHashMapHibernateBug() {
        //workaround for key problem
        if(! isHashMapHibernateBugFixed){
            HashMap<Language, LanguageString> tmp = new HashMap<>();
            tmp.putAll(text);
            text.clear();
            text.putAll(tmp);

            isHashMapHibernateBugFixed = true;
        }
    }

    //copied and adapted from TermBase
    public String getPreferredText(Language language) {
        String repr = getText(language);
        if(repr == null){
            repr = getText(Language.DEFAULT());
        }
        if(repr == null){
            repr = getText().isEmpty() ? null : getText().values().iterator().next().getText();
        }
        return repr;
    }

    /**
     * {@inheritDoc}
     * @deprecated usually a {@link TextualTypeDesignation} may have multiple types,
     * therefore the type is not defined
     */
    @Deprecated
    @Override
    public boolean hasDesignationSource() {
        return false;
    }

    /**
     * @deprecated not relevant for {@link TextualTypeDesignation} throws Exception
     */
    @Deprecated
    @Override
    public void setTypeStatus(SpecimenTypeDesignationStatus typeStatus) {
        throw new RuntimeException("Method should not be called in textbased type designaiton");
    }

    /**
     * @deprecated not relevant for {@link TextualTypeDesignation} throws Exception
     */@Deprecated
    @Override
    public void setNotDesignated(boolean notDesignated) {
        throw new RuntimeException("Method should not be called in textbased type designaiton");
    }

    /**
     * @deprecated not relevant for {@link TextualTypeDesignation} throws Exception
     */@Deprecated
    @Override
    public void setCitationMicroReference(String citationMicroReference) {
        throw new RuntimeException("Method should not be called in textbased type designaiton");
    }

    /**
     * @deprecated not relevant for {@link TextualTypeDesignation} throws Exception
     */
    @Deprecated
    @Override
    public void setCitation(Reference citation) {
        throw new RuntimeException("Method should not be called in textbased type designaiton");
    }

    /**
     * {@inheritDoc}
     * @deprecated a textual type designation has no specific type
     * therefore the type can not be removed.
     */
    @Override
    @Deprecated
    public void removeType() {
        //nothing to do
    }
}
