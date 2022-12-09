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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.IndexedEmbedded;

import eu.etaxonomy.cdm.hibernate.search.MultilanguageTextFieldBridge;
import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.common.IMultiLanguageTextHolder;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageText;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.TermVocabulary;

/**
 * This class represents the assignment of values ({@link State state terms}) to {@link Feature features}
 * corresponding to {@link CategoricalData categorical data}. A state data instance
 * constitutes an atomized part of an information piece (categorical data) so
 * that several state data instances may belong to one categorical data
 * instance.
 * <P>
 * This class corresponds to CharacterStateDataType according to the SDD schema.
 *
 * @author m.doering
 * @since 08-Nov-2007 13:06:53
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StateData", propOrder = {
    "categoricalData",
    "state",
    "count",
    "modifiers",
    "modifyingText"
})
@XmlRootElement(name = "StateData")
@Entity
@Audited
public class StateData
        extends VersionableEntity
        implements IModifiable, IMultiLanguageTextHolder{

    private static final long serialVersionUID = -4380314126624505415L;
    private static final Logger logger = LogManager.getLogger();

    @XmlElement(name = "CategoricalData")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @IndexedEmbedded(depth=1)
    private CategoricalData categoricalData;

    @XmlElement(name = "State", type=DefinedTermBase.class)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY, targetEntity=DefinedTermBase.class)
    @IndexedEmbedded(depth=1, targetElement=DefinedTermBase.class)
    //we could also use DefinedTermBase here, we use only DefinedTermBase<?> for
    //better understanding the code #10196
    private DefinedTermBase<?> state;

    //#8625 for statistically counting aggregated state data
    @Column(name="number")  //rename to avoid conflicts with SQL syntax
    private Integer count;

    @XmlElementWrapper(name = "Modifiers")
    @XmlElement(name = "Modifier")
    @ManyToMany(fetch = FetchType.LAZY)
    //no  cascade #5755
    @IndexedEmbedded(depth=1)
//	@NotNull // avoids creating a UNIQUE key for this field -> not needed for ManyToMany
    private Set<DefinedTerm> modifiers = new HashSet<>();

    @XmlElement(name = "ModifyingText")
    @XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
    @OneToMany(fetch = FetchType.LAZY)
    @MapKeyJoinColumn(name="modifyingtext_mapkey_id")
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
    @Field(name="modifyingText")
    @FieldBridge(impl=MultilanguageTextFieldBridge.class)
    private Map<Language,LanguageString> modifyingText = new HashMap<>();

//********************* FACTORY METHODS ************************\

    /**
     * Creates a new empty state data instance.
     */
    public static StateData NewInstance(){
        return new StateData();
    }

    /**
     * Creates a new empty state data instance.
     *
     * <b>NOTE:</b> {@link State}  is a sub class of {@link DefinedTermBase}.
     * If the state passed as parameter has been created newly it <b>has to be persisted before</b> it is possible to save the StateData.
     */
    public static StateData NewInstance(DefinedTermBase<?> state){
        StateData stateData = new StateData();
        stateData.setState(state);
        return stateData;
    }

//*************************** CONSTRUCTOR ************************\

    /**
     * Class constructor: creates a new empty state data instance.
     */
    public StateData() {
        super();
    }

//************************** GETTER /SETTER *************************\

    /**
     * Returns the {@link State state term} used in <i>this</i> state data.
     */
    public DefinedTermBase<?> getState(){
        return this.state;
    }
    /**
     * @see	#getState()
     */
    public void setState(DefinedTermBase<?> state){
        this.state = state;
    }

    /**
     * Returns the number of single data using this state if <B>this</B>
     * StateData was created by aggregation.
     */
    public Integer getCount() {
        return count;
    }
    /**
     * @see #getCount()
     */
    public void setCount(Integer count) {
        this.count = count;
    }
    /**
     * Increments the count by 1.
     * @see #getCount()
     */
    public void incrementCount(){
        if(count==null){
            count = 0;
        }
        count++;
    }

    /**
     * Returns the {@link CategoricalData state term} <i>this</i> state data
     * belongs to.
     */
    public CategoricalData getCategoricalData(){
        return this.categoricalData;
    }
    //for bidirectional use only
    @Deprecated
    protected void setCategoricalData(CategoricalData categoricalData) {
        this.categoricalData = categoricalData;
    }

    /**
     * Returns the set of {@link Modifier modifiers} used to qualify the validity
     * of <i>this</i> state data. This is only metainformation.
     */
    @Override
    public Set<DefinedTerm> getModifiers(){
        return this.modifiers;
    }

    /**
     * Adds a {@link Modifier modifier} to the set of {@link #getModifiers() modifiers}
     * used to qualify the validity of <i>this</i> state data.
     *
     * @param modifier	the modifier to be added to <i>this</i> state data
     * @see    	   		#getModifiers()
     */
    @Override
    public void addModifier(DefinedTerm modifier){
        this.modifiers.add(modifier);
    }
    /**
     * Removes one element from the set of {@link #getModifiers() modifiers}
     * used to qualify the validity of <i>this</i> state data.
     *
     * @param  modifier	the modifier which should be removed
     * @see     		#getModifiers()
     * @see     		#addModifier(Modifier)
     */
    @Override
    public void removeModifier(DefinedTerm modifier){
        this.modifiers.remove(modifier);
    }

    /**
     * Returns the {@link MultilanguageText multilanguage text} used to qualify the validity
     * of <i>this</i> state data.  The different {@link LanguageString language strings}
     * contained in the multilanguage text should all have the same meaning.<BR>
     * A multilanguage text does not belong to a controlled {@link TermVocabulary term vocabulary}
     * as a {@link Modifier modifier} does.
     * <P>
     * NOTE: the actual content of <i>this</i> state data is NOT
     * stored in the modifying text. This is only metainformation
     * (like "Some experts express doubt about this assertion").
     */
    public Map<Language,LanguageString> getModifyingText(){
        return this.modifyingText;
    }

    /**
     * Creates a {@link LanguageString language string} based on the given text string
     * and the given {@link Language language} and adds it to the {@link MultilanguageText multilanguage text}
     * used to qualify the validity of <i>this</i> state data.
     *
     * @param language	the language in which the text string is formulated
     * @param text		the string describing the validity
     * 					in a particular language
     *
     * @see    	   		#getModifyingText()
     * @see    	   		#addModifyingText(LanguageString)
     */
    public LanguageString putModifyingText(Language language, String text){
        return this.modifyingText.put(language, LanguageString.NewInstance(text, language));
    }

    /**
     * Adds a translated {@link LanguageString text in a particular language}
     * to the {@link MultilanguageText multilanguage text} used to qualify the validity
     * of <i>this</i> state data.
     *
     * @param text	the language string describing the validity
     * 				in a particular language
     * @see    	   	#getModifyingText()
     * @see    	   	#putModifyingText(Language, String)
     */
    public LanguageString putModifyingText(LanguageString text){
        return this.modifyingText.put(text.getLanguage(),text);
    }
    /**
     * Removes from the {@link MultilanguageText multilanguage text} used to qualify the validity
     * of <i>this</i> state data the one {@link LanguageString language string}
     * with the given {@link Language language}.
     *
     * @param  lang	the language in which the language string to be removed
     * 				has been formulated
     * @see     	#getModifyingText()
     */
    public LanguageString removeModifyingText(Language lang){
        return this.modifyingText.remove(lang);
    }

//*********************************** TO STRING *****************************************/


    @Override
    public String toString() {
        return "[" + state
                + (modifiers.isEmpty() ? "" : ", modifiers=" + modifiers)
                + (modifyingText.isEmpty() ? "" : ", modifyingText=" + modifyingText)
                + (count==null?"": "("+count+")")
                + "]";
    }

//*********************************** CLONE *****************************************/

    /**
     * Clones <i>this</i> state data. This is a shortcut that enables to create
     * a new instance that differs only slightly from <i>this</i> state data by
     * modifying only some of the attributes.
     *
     * @see eu.etaxonomy.cdm.model.common.VersionableEntity#clone()
     * @see java.lang.Object#clone()
     */
    @Override
    public StateData clone() {

        try {
            StateData result = (StateData)super.clone();

            //modifiers
            result.modifiers = new HashSet<>();
            for (DefinedTerm modifier : getModifiers()){
                result.modifiers.add(modifier);
            }

            //modifying text
            result.modifyingText = cloneLanguageString(this.modifyingText);

            return result;
            //no changes to: state
        } catch (CloneNotSupportedException e) {
            logger.warn("Object does not implement cloneable");
            e.printStackTrace();
            return null;
        }
    }
}