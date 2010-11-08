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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.media.Media;
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
@XmlType(name = "PolytomousKeyNode", propOrder = {
		"key",
		"children",
		"sortIndex",
		"statement",
		"question",
		"taxon",
		"subkey",
		"otherNode",
		"modifyingText"
})
@XmlRootElement(name = "FeatureNode")
@Entity
@Audited
public class PolytomousKeyNode extends VersionableEntity  {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(PolytomousKeyNode.class);
    
    //This is the main key a node belongs to. Although other keys may also reference
	//<code>this</code> node, a node usually belongs to a given key.
	@XmlElement(name = "PolytomousKey")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private PolytomousKey key;
    
    @XmlElementWrapper(name = "Children")
    @XmlElement(name = "Child")
//    @OrderColumn("sortIndex")  //JPA 2.0 same as @IndexColumn
    // @IndexColumn does not work because not every FeatureNode has a parent. But only NotNull will solve the problem (otherwise 
    // we will need a join table 
    // http://stackoverflow.com/questions/2956171/jpa-2-0-ordercolumn-annotation-in-hibernate-3-5
    // http://docs.jboss.org/hibernate/stable/annotations/reference/en/html_single/#entity-hibspec-collection-extratype-indexbidir
    //see also https://forum.hibernate.org/viewtopic.php?p=2392563
    //http://opensource.atlassian.com/projects/hibernate/browse/HHH-4390
    // reading works, but writing doesn't
    //
    @IndexColumn(name="sortIndex", base = 0)
    @JoinColumn(name="parent_id")
    @OrderBy("sortIndex")
	@OneToMany(fetch = FetchType.LAZY /*, mappedBy="parent"*/)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
	private List<PolytomousKeyNode> children = new ArrayList<PolytomousKeyNode>();

    //see comment on children @IndexColumn
    private Integer sortIndex;
 
	@XmlElement(name = "Statement")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN})
	private KeyStatement statement;

	@XmlElement(name = "Question")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE_ORPHAN})
	private KeyStatement question;

    
 	@XmlElement(name = "Taxon")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade(CascadeType.SAVE_UPDATE)
	private Taxon taxon;
  	
    //Refers to an entire key
	//<code>this</code> node, a node usually belongs to a given key.
	@XmlElement(name = "SubKey")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	@Cascade(CascadeType.SAVE_UPDATE)
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
//    @JoinTable(name = "DescriptionElementBase_ModifyingText")
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	private Map<Language,LanguageString> modifyingText = new HashMap<Language,LanguageString>();

    
	
	/** 
	 * Class constructor: creates a new empty feature node instance.
	 */
	protected PolytomousKeyNode() {
		super();
	}

	/** 
	 * Creates a new empty feature node instance.
	 * 
	 * @see #NewInstance(Feature)
	 */
	public static PolytomousKeyNode NewInstance(){
		return new PolytomousKeyNode();
	}

	
	//** ********************** CHILDREN ******************************/

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.description.IPolytomousKeyPart#getKey()
	 */
	public PolytomousKey getKey() {
		return key;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.description.IPolytomousKeyPart#setKey(eu.etaxonomy.cdm.model.description.PolytomousKey)
	 */
	public void setKey(PolytomousKey key) {
		this.key = key;
	}
	
	/** 
	 * Returns the (ordered) list of feature nodes which are children nodes of
	 * <i>this</i> feature node.
	 */
	public List<PolytomousKeyNode> getChildren() {
		return children;
	}

	/**
	 * Adds the given polytomous key node at the end of the list of children of
	 * <i>this</i> polytomous key node. 
	 * 
	 * @param	child	the feature node to be added 
	 * @see				#getChildren() 
	 * @see				#setChildren(List)
	 * @see				#addChild(PolytomousKeyNode, int) 
	 * @see				#removeChild(PolytomousKeyNode)
	 * @see				#removeChild(int) 
	 */
	public void addChild(PolytomousKeyNode child){
		addChild(child, children.size());
	}
	/**
	 * Inserts the given child node in the list of children of <i>this</i> polytomous key node
	 * at the given (index + 1) position. If the given index is out of bounds
	 * an exception will be thrown.<BR>
	 * 
	 * @param	child	the polytomous key node to be added 
	 * @param	index	the integer indicating the position at which the child
	 * 					should be added 
	 * @see				#getChildren() 
	 * @see				#setChildren(List)
	 * @see				#addChild(PolytomousKeyNode) 
	 * @see				#removeChild(PolytomousKeyNode)
	 * @see				#removeChild(int) 
	 */
	public void addChild(PolytomousKeyNode child, int index){
		children.add(index, child);
		child.setKey(this.getKey());
	}
	/** 
	 * Removes the given polytomous key node from the list of {@link #getChildren() children}
	 * of <i>this</i> polytomous key node.
	 *
	 * @param  child	the feature node which should be removed
	 * @see     		#getChildren()
	 * @see				#addChild(PolytomousKeyNode, int) 
	 * @see				#addChild(PolytomousKeyNode) 
	 * @see				#removeChild(int) 
	 */
	public void removeChild(PolytomousKeyNode child){
		int index = children.indexOf(child);
		if (index >= 0){
			removeChild(index);
		}
	}
	/** 
	 * Removes the feature node placed at the given (index + 1) position from
	 * the list of {@link #getChildren() children} of <i>this</i> feature node.
	 * If the given index is out of bounds no child will be removed. 
	 *
	 * @param  index	the integer indicating the position of the feature node to
	 * 					be removed
	 * @see     		#getChildren()
	 * @see				#addChild(PolytomousKeyNode, int) 
	 * @see				#addChild(PolytomousKeyNode) 
	 * @see				#removeChild(PolytomousKeyNode) 
	 */
	public void removeChild(int index){
		children.remove(index);
	}

	/** 
	 * Returns the feature node placed at the given (childIndex + 1) position
	 * within the list of {@link #getChildren() children} of <i>this</i> feature node.
	 * If the given index is out of bounds no child will be returned. 
	 * 
	 * @param  childIndex	the integer indicating the position of the feature node
	 * @see     			#getChildren()
	 * @see					#addChild(PolytomousKeyNode, int) 
	 * @see					#removeChild(int) 
	 */
	public PolytomousKeyNode getChildAt(int childIndex) {
			return children.get(childIndex);
	}

	/** 
	 * Returns the number of children nodes of <i>this</i> feature node.
	 * 
	 * @see	#getChildren()
	 */
	@Transient
	public int childCount() {
		return children.size();
	}

	/** 
	 * Returns the integer indicating the position of the given feature node
	 * within the list of {@link #getChildren() children} of <i>this</i> feature node.
	 * If the list does not contain this node then -1 will be returned. 
	 * 
	 * @param  node	the feature node the position of which is being searched
	 * @see			#addChild(PolytomousKeyNode, int) 
	 * @see			#removeChild(int) 
	 */
	public int getIndex(PolytomousKeyNode node) {
		if (! children.contains(node)){
			return -1;
		}else{
			return children.indexOf(node);
		}
	}

	/** 
	 * Returns the boolean value indicating if <i>this</i> feature node has
	 * children (false) or not (true). A node without children is at the
	 * bottommost level of a tree and is called a leaf.
	 * 
	 * @see	#getChildren()
	 * @see	#getChildCount()
	 */
	@Transient
	public boolean isLeaf() {
		return children.size() < 1;
	}

	
	//** ********************** QUESTIONS ******************************/

	/**
	 * Returns the statement for <code>this</code> PolytomousKeyNode. If the user
	 * agrees with the statement, the node will be followed.
	 * @return the statement
	 * @see #getQuestion()
	 */
	public KeyStatement getStatement() {
		return statement;
	}

	/**
	 * @param statement
	 * @see #getStatement()
	 */
	public void setStatement(KeyStatement statement) {
		this.statement = statement;
	}

	/**
	 * Returns the question for <code>this</code> PolytomousKeyNode. <BR>
	 * A question is answered by statements in leads below this tree node.
	 * Questions are optional and are usually empty in traditional keys. 
	 * @return the statement
	 * @see #getStatement()
	 */
	public KeyStatement getQuestion() {
		return question;
	}

	/**
	 * @param question
	 * @see #getQuestion()
	 */
	public void setQuestion(KeyStatement question) {
		this.question = question;
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


	/**
	 * Returns the taxon this node links to. This is usually the case when this node is a leaf.
	 * 
	 * @return
	 * @see #setTaxon(Taxon)
	 * @see #getSubkey()
	 * @see #getChildren()
	 * @see #getOtherNode()
	 */
	public Taxon getTaxon() {
		return taxon;
	}
	
	/**
	 * Sets the taxon this node links to. <BR>
	 * If a tax
	 * @param taxon
	 * @see #getTaxon()
	 */
	public void setTaxon(Taxon taxon) {
		this.taxon = taxon;
	}

	/**
	 * @return
	 * @see #setSubkey(PolytomousKey)
	 * @see #getTaxon()
	 * @see #getChildren()
	 * @see #getOtherNode()
	 */
	public PolytomousKey getSubkey() {
		return subkey;
	}

	/**
	 * @param subkey
	 * @see #getSubkey()
	 */
	public void setSubkey(PolytomousKey subkey) {
		this.subkey = subkey;
	}

	/**
	 * @return
	 * @see #setOtherNode(PolytomousKeyNode)
	 * @see #getTaxon()
	 * @see #getChildren()
	 * @see #getSubkey()
	 */
	public PolytomousKeyNode getOtherNode() {
		return otherNode;
	}

	/**
	 * @param otherNode
	 * @see #getOtherNode()
	 */
	public void setOtherNode(PolytomousKeyNode otherNode) {
		this.otherNode = otherNode;
	}

	

}