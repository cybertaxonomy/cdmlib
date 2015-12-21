/**
 *
 */
package eu.etaxonomy.cdm.model.description;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.common.IMultiLanguageTextHolder;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageTextHelper;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.VersionableEntity;

/**
 * This class represents a statement or a question within a (polytomous) key.
 * Compare with SDD SimpleRepresentation.
 *
 * @author a.mueller
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KeyStatement", propOrder = {
    "label"
//    ,"mediaObject"
})
@XmlRootElement(name = "KeyStatement")
@Entity
@Audited
public class KeyStatement extends VersionableEntity implements IMultiLanguageTextHolder{
	private static final long serialVersionUID = 3771323100914695139L;
	private static final Logger logger = Logger.getLogger(KeyStatement.class);


	@XmlElement(name = "MultiLanguageText")
    @XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
    @OneToMany (fetch= FetchType.LAZY, orphanRemoval=true)
	@MapKeyJoinColumn(name="label_mapkey_id")
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE, CascadeType.DELETE })
//    @IndexedEmbedded
    private Map<Language, LanguageString> label = new HashMap<Language, LanguageString>();

	//private mediaObjects needs to be discussed (how to implement the role of the media)



	public static KeyStatement NewInstance(){
		KeyStatement result = new KeyStatement();
		return result;
	}


	public static KeyStatement NewInstance(String defaultLanguageLabel){
		KeyStatement result = new KeyStatement();
		result.putLabel(Language.DEFAULT(), defaultLanguageLabel);
		return result;
	}

	/**
	 * Factory method for a key statement.
	 * @param language the language of the first representation of the statement. Must not be <code>null</code>.
	 * @param label the text of statement in the given language.
	 * @return
	 */
	public static KeyStatement NewInstance(Language language, String label){
		assert language != null : "Language for KeyStatement must not be null";
		KeyStatement result = new KeyStatement();
		result.putLabel(language, label);
		return result;
	}

	/**
	 *
	 */
	public KeyStatement() {
	}

// ********************************* METHODS ***************************/

	/**
	 * Returns the label with the content of <i>this</i> key statement.
	 * The different {@link LanguageString language strings} (texts) contained in the
	 * label should all have the same meaning.
	 *
	 * @see	#getText(Language)
	 */
    public Map<Language, LanguageString> getLabel() {
		return label;
	}

    /**
     * Returns the label with the content of <i>this</i> key statement for
     * a specific language.
     *
     * @param language the language in which the label is formulated
     * @return
     */
    public LanguageString getLabel(Language language){
    	return label.get(language);
    }

    public void setLabel(Map<Language,LanguageString> label) {
    	this.label = label;
    }

	/**
	 * Returns the text string in the given {@link Language language} with the content
	 * of <i>this</i> key statement.
	 *
	 * @param language	the language in which the label is formulated
	 * @see				#getLabel(Language)
	 */
	public String getLabelText(Language language) {
		LanguageString languageString = label.get(language);
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
	 * Representation in Language.DEFAULT() and if necessary further falls back
	 * to return the first element found if any.
	 *
	 * TODO think about this fall-back strategy &
	 * see also {@link TermBase#getPreferredRepresentation(List)}
	 *
	 * @param languages
	 * @return
	 */
	public LanguageString getPreferredLanguageString(List<Language> languages) {
		return MultilanguageTextHelper.getPreferredLanguageString(label, languages);
	}

	/**
	 * Creates a {@link LanguageString language string} based on the given text string
	 * and the given {@link Language language}, returns it and adds it to the multilanguage
	 * text representing the content of <i>this</i> text data.
	 *
	 * @param text		the string representing the content of the text data
	 * 					in a particular language
	 * @param language	the language in which the text string is formulated
	 * @return			the language string
	 * @see    	   		#getMultilanguageText()
	 * @see    	   		#putText(LanguageString)
	 * @deprecated 		should follow the put semantic of maps, this method will be removed in v4.0
	 * 					Use the {@link #putLabel(Language, String) putLabel} method
	 */
	@Deprecated
    public LanguageString putLabel(String label, Language language) {
		return putLabel(language, label);
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
	 * @return			the language string
	 * @see    	   		#getLabel()
	 * @see    	   		#putLabel(LanguageString)
	 *
	 */
	public LanguageString putLabel(Language language, String label) {
		LanguageString result = this.label.put(language , LanguageString.NewInstance(label, language));
		return (result == null ? null : result);
	}
	/**
	 * Adds a translated {@link LanguageString text in a particular language}
	 * to the label.
	 * The given language string will be returned.
	 *
	 * @param languageString	the language string representing the content of
	 * 							the text data in a particular language
	 * @return					the language string
	 * @see    	   				#getLabel()
	 * @see    	   				#putLabel(String, Language)
	 */
	public LanguageString putLabel(LanguageString languageString) {
		if (languageString == null){
			return null;
		}else{
			Language language = languageString.getLanguage();
			return this.label.put(language, languageString);
		}
	}

	/**
	 * Adds a translated {@link LanguageString text in a particular language}
	 * to the label.
	 * The given language string will be returned.
	 *
	 * @param languageString	the language string representing the content of
	 * 							the text data in a particular language
	 * @return					the language string
	 * @see    	   				#getLabel()
	 * @see    	   				#putLabel(String, Language)
	 * @deprecated				This method will be removed in v4.0
	 * 							Use the {@link #putLabel(LanguageString) putLabel} method instead
	 */
	@Deprecated
    public LanguageString putText(LanguageString languageString) {
		return putLabel(languageString);
	}

	/**
	 * Removes from label the one {@link LanguageString language string}
	 * with the given {@link Language language}. Returns the removed
	 * language string.
	 *
	 * @param  language	the language in which the language string to be removed
	 * 					has been formulated
	 * @return			the language string associated with the given language
	 * @see     		#getLabelText()
	 * @deprecated		This method will be removed in v4.0
	 * 					Use the {@link #removeLabel(Language)} method instead
	 */
	@Deprecated
    public LanguageString removeText(Language language) {
		return removeLabel(language);
	}

	/**
	 * Removes from label the one {@link LanguageString language string}
	 * with the given {@link Language language}. Returns the removed
	 * language string.
	 *
	 * @param  language	the language in which the language string to be removed
	 * 					has been formulated
	 * @return			the language string associated with the given language
	 * @see     		#getLabelText()
	 */
	public LanguageString removeLabel(Language language) {
		return this.label.remove(language);
	}


	/**
	 * Returns the number of {@link Language languages} in which the label
	 * of <i>this</i> key statement has been formulated.
	 *
	 * @see	#getMultilanguageText()
	 */
	public int countLanguages(){
		return label.size();
	}

//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> KeyStatement. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> KeyStatement by
	 * modifying only some of the attributes.
	 *
	 * @see eu.etaxonomy.cdm.model.common.VersionableEntitity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		KeyStatement result;
		try {
			result = (KeyStatement) super.clone();

			result.label = new HashMap<Language, LanguageString>();


			for (Entry<Language,LanguageString> entry: this.label.entrySet()){

				result.label.put(entry.getKey(), entry.getValue());
			}

			return result;
		}catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}

//********************* toString() *************************************/

	@Override
	public String toString(){
		if (label != null && ! label.isEmpty()){
			String result = label.values().iterator().next().getText();
			return result;
		}else{
			return super.toString();
		}

	}

}
