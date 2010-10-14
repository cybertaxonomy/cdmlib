/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy 
 * http://www.e-taxonomy.eu
 * 
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.model.description;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
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

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageText;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * The class represents a node within a {@link PolytomousKey polytomous key} structure.
 * A polytomous key node can be referenced from multiple other nodes. Therefore a node does
 * not have a single parent. Nevertheless it always belongs to a main key though it may be
 * referenced also by other key nodes.
 * 
 * @author  a.mueller
 * @created 13-Oct-2010
 * 
 */
@SuppressWarnings("serial")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FeatureNode", propOrder = {
		"feature",
		"parent",
		"children",
		"sortIndex",
		"onlyApplicableIf",
		"inapplicableIf",
		"questions",
		"taxon"
})
@XmlRootElement(name = "FeatureNode")
@Entity
@Audited
public class PolytomousKeyLeaf extends PolytomousKeyNodeBase implements IPolytomousKeyPart {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PolytomousKeyLeaf.class);
	

//******************* Result *************************/
//TODO better use common interface (IResult) but then we need taxa not to be a Collection
	
  	@XmlElementWrapper(name = "Taxa")
	@XmlElement(name = "Taxon")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade(CascadeType.SAVE_UPDATE)
	private Set<Taxon> taxa;
  	
    //Refers to an entire key
	//<code>this</code> node, a node usually belongs to a given key.
	@XmlElement(name = "PolytomousKey")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private PolytomousKey subkey;
  	
    //Refers to an other node within this key or an other key
	@XmlElement(name = "PolytomousKey")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private PolytomousKeyNode otherNode;
  	
	
	//a modifying text may be a text like "an unusual form of", commenting the taxa
	//TODO should be available for each taxon/result
	@XmlElement(name = "ModifyingText")
    @XmlJavaTypeAdapter(MultilanguageTextAdapter.class)
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "DescriptionElementBase_ModifyingText")
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	private Map<Language,LanguageString> modifyingText = new HashMap<Language,LanguageString>();

	
	
	/** 
	 * Class constructor: creates a new empty feature node instance.
	 */
	protected PolytomousKeyLeaf() {
		super();
	}

	/** 
	 * Creates a new empty feature node instance.
	 * 
	 * @see #NewInstance(Feature)
	 */
	public static PolytomousKeyLeaf NewInstance(){
		return new PolytomousKeyLeaf();
	}

	
//****************** taxa **************************/


	public Set<Taxon> getTaxa() {
		return taxa;
	}
	public boolean addTaxon(Taxon taxon){
		return taxa.add(taxon);	
	}
	//TODO needed?
//	public void setTaxa(Set<Taxon> taxa) {
//		this.taxa = taxa;
//	}
	public boolean removeTaxon(Taxon taxon){
		return taxa.remove(taxon);
	}


//**************** modifying text ***************************************
	
	/** 
	 * Returns the {@link MultilanguageText multilanguage text} used to qualify the validity
	 * of <i>this</i> description element.  The different {@link LanguageString language strings}
	 * contained in the multilanguage text should all have the same meaning.<BR>
	 * A multilanguage text does not belong to a controlled {@link TermVocabulary term vocabulary}
	 * as a {@link Modifier modifier} does.
	 * <P>
	 * NOTE: the actual content of <i>this</i> description element is NOT
	 * stored in the modifying text. This is only metainformation
	 * (like "Some experts express doubt about this assertion").
	 */
	public Map<Language,LanguageString> getModifyingText(){
		return this.modifyingText;
	}

	/**
	 * Adds a translated {@link LanguageString text in a particular language}
	 * to the {@link MultilanguageText multilanguage text} used to qualify the validity
	 * of <i>this</i> description element.
	 * 
	 * @param description	the language string describing the validity
	 * 						in a particular language
	 * @see    	   			#getModifyingText()
	 * @see    	   			#addModifyingText(String, Language)
	 */
	public LanguageString addModifyingText(LanguageString description){
		return this.modifyingText.put(description.getLanguage(),description);
	}
	/**
	 * Creates a {@link LanguageString language string} based on the given text string
	 * and the given {@link Language language} and adds it to the {@link MultilanguageText multilanguage text} 
	 * used to qualify the validity of <i>this</i> description element.
	 * 
	 * @param text		the string describing the validity
	 * 					in a particular language
	 * @param language	the language in which the text string is formulated
	 * @see    	   		#getModifyingText()
	 * @see    	   		#addModifyingText(LanguageString)
	 */
	public LanguageString addModifyingText(String text, Language language){
		return this.modifyingText.put(language, LanguageString.NewInstance(text, language));
	}
	/** 
	 * Removes from the {@link MultilanguageText multilanguage text} used to qualify the validity
	 * of <i>this</i> description element the one {@link LanguageString language string}
	 * with the given {@link Language language}.
	 *
	 * @param  language	the language in which the language string to be removed
	 * 					has been formulated
	 * @see     		#getModifyingText()
	 */
	public LanguageString removeModifyingText(Language language){
		return this.modifyingText.remove(language);
	}

	@Override
	public List<IPolytomousKeyPart> getChildren() {
		ArrayList<IPolytomousKeyPart> result = new ArrayList<IPolytomousKeyPart>();
		result.add(otherNode);
		result.add(subkey);
		return result;
	}
	

	

}