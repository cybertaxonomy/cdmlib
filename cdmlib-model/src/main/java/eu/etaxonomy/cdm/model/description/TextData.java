/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.description;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.search.MultilanguageTextFieldBridge;
import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.common.IMultiLanguageTextHolder;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageTextHelper;
import eu.etaxonomy.cdm.model.common.TermBase;


/**
 * This class represents information pieces expressed in one or several natural
 * languages (for the {@link Feature feature} "medical use" for instance).
 * A {@link TextFormat format} used for structuring the text may also be stated.
 * <P>
 * This class corresponds partially to NaturalLanguageDescriptionType according
 * to the SDD schema.
 *
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:59
 */
@SuppressWarnings("unused")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TextData", propOrder = {
    "multilanguageText",
    "format"
})
@XmlRootElement(name = "TextData")
@Entity
@Audited
@Indexed(index = "eu.etaxonomy.cdm.model.description.DescriptionElementBase")
public class TextData extends DescriptionElementBase implements IMultiLanguageTextHolder, Cloneable{
    private static final long serialVersionUID = -2165015581278282615L;
    private static final Logger logger = Logger.getLogger(TextData.class);

    //@XmlElement(name = "MultiLanguageText", type = MultilanguageText.class)
    @XmlElement(name = "MultiLanguageText")
    @XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
    @OneToMany (fetch= FetchType.LAZY, orphanRemoval=true)
    @MapKeyJoinColumn(name="multilanguagetext_mapkey_id")
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE, CascadeType.DELETE})
    @Field(name="text", store=Store.YES)
    @FieldBridge(impl=MultilanguageTextFieldBridge.class)
    @NotNull
    private Map<Language, LanguageString> multilanguageText = new HashMap<>();

    @XmlElement(name = "Format")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    private TextFormat format;

    @XmlTransient
    @Transient
    private boolean isHashMapHibernateBugFixed = false;

    // ************* CONSTRUCTORS *************/
    /**
     * Class constructor: creates a new empty text data instance.
     *
     * @see #TextData(Feature)
     */
    public TextData(){
        this(null);
    }

    /**
     * Class constructor: creates a new text data instance with the {@link Feature feature}
     * to be described.
     *
     * @param	feature	the feature the text data refer to
     * @see 			#TextData()
     */
    public TextData(Feature feature){
        super(feature);
    }

    //********* METHODS **************************************/
    /**
     * Creates a new empty text data instance.
     *
     * @see #NewInstance(Feature)
     * @see #NewInstance(String, Language, TextFormat)
     * @see	#NewInstance(Feature, String, Language, TextFormat)
     */
    public static TextData NewInstance(){
        return new TextData();
    }

    /**
     * Creates a new text data instance with the {@link Feature feature}
     * to be described.
     *
     * @param	feature	the feature the text data refer to
     * @see 			#NewInstance()
     * @see 			#NewInstance(String, Language, TextFormat)
     * @see					#NewInstance(Feature, String, Language, TextFormat)
     */
    public static TextData NewInstance(Feature feature){
        return new TextData(feature);
    }

    /**
     * Creates a new text data instance with a given text in a given particular
     * {@link Language language} and with the given text format for structuring it.
     *
     * @param	text		the text string with the content of the description
     * @param	language	the language in which the text string is formulated
     * @param	format		the text format used to structure the text string
     * @see 				#NewInstance()
     * @see 				#NewInstance(Feature)
     * @see					#NewInstance(Feature, String, Language, TextFormat)
     */
    public static TextData NewInstance(String text, Language language, TextFormat format){
        TextData result =  new TextData();
        result.putText(language, text);
        result.setFormat(format);
        return result;
    }


    /**
     * Creates a new text data instance with a given text in a given particular
     * {@link Language language} and with the given text format for structuring it.
     *
     * @param   feature	    the feature the text data refer to
     * @param	text		the text string with the content of the description
     * @param	language	the language in which the text string is formulated
     * @param	format		the text format used to structure the text string
     * @see 				#NewInstance()
     * @see 				#NewInstance(Feature)
     * @see					#NewInstance(String, Language, TextFormat)
     */
    public static TextData NewInstance(Feature feature, String text, Language language, TextFormat format){
        TextData result =  new TextData(feature);
        result.putText(language, text);
        result.setFormat(format);
        return result;
    }

    /**
     * Returns a copy of the multilanguage text with the content of <i>this</i> text data.
     * The different {@link LanguageString language strings} (texts) contained in the
     * multilanguage text should all have the same meaning.
     *
     * @see	#getText(Language)
     */
    public Map<Language, LanguageString> getMultilanguageText() {
        fixHashMapHibernateBug();

//    	HashMap<Language, LanguageString> result = new HashMap<>();
//		result.putAll(multilanguageText);
//		return result;
        return multilanguageText;
    }

//    /**
//     * Sets the multilanguage text.
//	 * The different {@link LanguageString language strings} (texts) contained in the
//	 * multilanguage text should all have the same meaning.
//     *
//     * @param multilanguageText
//     */
//    private void setMultilanguageText(Map<Language,LanguageString> multilanguageText) {
//    	this.multilanguageText = multilanguageText;
//    }

    /**
     * Returns the multilanguage text with the content of <i>this</i> text data for
     * a specific language.
     *
     * @param language the language in which the text string looked for is formulated
     * @return
     */
    public LanguageString getLanguageText(Language language){
        //work around for the problem that contains does not work correctly in persisted maps.
        //This is because the persisted uuid is not present when loading the map key and
        //therefore the hash code for language is not computed correctly
        //see DescriptionElementDaoHibernateTest and #2114
//    	for (Map.Entry<Language, LanguageString> entry : multilanguageText.entrySet()){
//    		if (entry.getKey() != null){
//        		if (entry.getKey().equals(language)){
//        			return entry.getValue();
//        		}
//    		}else{
//    			if (language == null){
//    				return entry.getValue();
//    			}
//    		}
//    	}
//    	return null;
        //old
        return getMultilanguageText().get(language);
    }

    /**
     * Returns the text string in the given {@link Language language} with the content
     * of <i>this</i> text data.
     *
     * @param language	the language in which the text string looked for is formulated
     * @see				#getMultilanguageText(Language)
     */
    public String getText(Language language) {
        LanguageString languageString = getLanguageText(language);
        if (languageString == null){
            return null;
        }else{
            return languageString.getText();
        }
    }

    /**
     * Returns the LanguageString in the preferred language. Preferred languages
     * are specified by the parameter languages, which receives a list of
     * Language instances in the order of preference. If no representation in
     * any preferred languages is found the method falls back to return the
     * Representation in Language.DEFAULT() and if neccesary further falls back
     * to return the first element found if any.
     *
     * TODO think about this fall-back strategy &
     * see also {@link TermBase#getPreferredRepresentation(List)}
     *
     * @param languages
     * @return
     */
    public LanguageString getPreferredLanguageString(List<Language> languages) {
        return MultilanguageTextHelper.getPreferredLanguageString(getMultilanguageText(), languages);
    }

    private void fixHashMapHibernateBug() {
        //workaround for key problem
        if(! isHashMapHibernateBugFixed){
            HashMap<Language, LanguageString> tmp = new HashMap<>();
            tmp.putAll(multilanguageText);
            multilanguageText.clear();
            multilanguageText.putAll(tmp);

            isHashMapHibernateBugFixed = true;
        }
    }


    /**
     * Creates a {@link LanguageString language string} based on the given text string
     * and the given {@link Language language}, returns it and adds it to the multilanguage
     * text representing the content of <i>this</i> text data.
     *
     * @param language	the language in which the text string is formulated
     * @param text		the string representing the content of the text data
     * 					in a particular language
     *
     * @see    	   		#getMultilanguageText()
     * @see    	   		#putText(LanguageString)
     * @return			the previous language string associated with the given Language, or null if there was no mapping for the given Language
     */
    public LanguageString putText(Language language, String text) {
        fixHashMapHibernateBug();
        //** end workaround
        LanguageString languageString = multilanguageText.get(language);
        if (languageString != null){
            languageString.setText(text);
        }else{
            languageString = LanguageString.NewInstance(text, language);
        }
        this.multilanguageText.put(language , languageString);
        return languageString;
    }


    /**
     * Adds a translated {@link LanguageString text in a particular language}
     * to the multi-language text representing the content of <i>this</i> text data.
     * The given language string will be returned.
     *
     * @param languageString	the language string representing the content of
     * 							the text data in a particular language
     * @see    	   				#getMultilanguageText()
     * @see    	   				#putText(String, Language)
     * @see						HashMap#put(Object, Object)
     * @return					the previous language string associated with key, or null if there was no mapping for key
     */
    public LanguageString putText(LanguageString languageString) {

        if (languageString == null){
            return null;
        }else{
            Language language = languageString.getLanguage();
            return this.multilanguageText.put(language, languageString);
        }
    }
    /**
     * Removes from the multilanguage representing the content of
     * <i>this</i> text data the one {@link LanguageString language string}
     * with the given {@link Language language}. Returns the removed
     * language string.
     *
     * @param  language	the language in which the language string to be removed
     * 					has been formulated
     * @return			the language string associated with the given language or null if there was no mapping for the given Language
     * @see     		#getMultilanguageText()
     */
    public LanguageString removeText(Language language) {
        fixHashMapHibernateBug();
        return this.multilanguageText.remove(language);
    }

    /**
     * Returns the number of {@link Language languages} in which the content
     * of <i>this</i> text data has been formulated.
     *
     * @see	#getMultilanguageText()
     */
    public int countLanguages(){
        return multilanguageText.size();
    }


    /**
     * Returns the {@link TextFormat format} used for structuring the text representing
     * the content of <i>this</i> text data.
     *
     * @see	#getMultilanguageText()
     */
    public TextFormat getFormat() {
        return format;
    }
    /**
     * @see	#getFormat()
     */
    public void setFormat(TextFormat format) {
        this.format = format;
    }

    /**
     * @see {@link java.util.Map#containsKey(Object)}
     * @param language
     * @return
     */
    public boolean containsKey(Language language){
        return getMultilanguageText().containsKey(language);
    }

    /**
     * @see {@link java.util.Map#containsValue(Object)}
     * @param languageString
     * @return
     */
    public boolean containsValue(LanguageString languageString){
        return getMultilanguageText().containsValue(languageString);
    }


    /**
     * Returns the number of languages available for this text data.
     * @see {@link java.util.Map#size()}
     * @return
     */
    public int size(){
        return this.multilanguageText.size();
    }




//*********************************** CLONE *****************************************/

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        Map<Language, LanguageString> multiLangText = getMultilanguageText();
        if (multiLangText.isEmpty()){
            return super.toString();
        }else{
            String result = null;
            for(LanguageString ls : multiLangText.values()){
                result = CdmUtils.concat(";", result, ls.toString());
            }
            return "[" + result + "]";
        }
    }

    /**
     * Clones <i>this</i> text data. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> text data by
     * modifying only some of the attributes.
     *
     * @see eu.etaxonomy.cdm.model.description.DescriptionElementBase#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {

        try {
            TextData result = (TextData)super.clone();

            //description
            result.multilanguageText = new HashMap<Language, LanguageString>();
            for (Language language : getMultilanguageText().keySet()){
                //TODO clone needed? See also IndividualsAssociation
                LanguageString newLanguageString = (LanguageString)getMultilanguageText().get(language).clone();
                result.multilanguageText.put(language, newLanguageString);
            }

            return result;
            //no changes to: format
        } catch (CloneNotSupportedException e) {
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }
    }

}
