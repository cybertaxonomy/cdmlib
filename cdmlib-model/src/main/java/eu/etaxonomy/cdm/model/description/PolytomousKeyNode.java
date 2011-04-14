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
import java.util.Map.Entry;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
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

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.jaxb.MultilanguageTextAdapter;
import eu.etaxonomy.cdm.model.common.IMultiLanguageTextHolder;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageText;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * The class represents a node within a {@link PolytomousKey polytomous key}
 * structure. The structure such key is a directed acyclic graph where as
 * instances of <code>PolytomousKeyNode</code>, {@Taxon Taxa}, or even
 * of other {@link PolytomousKey PolytomousKeys} are forming the nodes. A
 * <code>PolytomousKeyNode</code> can have multiple kinds of edges:
 * <p>
 * <h4>Edges</h4>
 * In very rare cases a polytomous key node can have same time an edge to a taxon and other edges to futher child nodes. 
 * In this case the determination process may be terminated at this taxon or can proceed if a more accurate determination
 * is wanted. 
 * <li>{@link #getChildren() children}:<br>
 * One or multiple subordinate <code>PolytomousKeyNodes</code>. The source node
 * poses a {@link #getQuestion question}, to which the children are providing
 * predefined answers by their {@link #getStatement() statement}. It is also
 * possible that the children's {@link #getStatement() statements} are
 * formulated in a way which makes an explicit question at the source node
 * obsolete. In this case the question may be empty. There is furthermore the
 * {@link #getFeature() feature} property which also plays the role as question.
 * The <code>feature</code> property will most likely be used if the key has
 * been generated automatically. Hand written keys will have a
 * <code>question</code>. An existing question should always be given
 * <b>priority</b> over the <code>feature</code>.
 * <p>
 * <b>Special case:</b>
 * <i>Child nodes with empty statements but taxa as leaf</i> are to treated as if all
 * those taxa where direct children of the source node. That is the nodes in
 * with empty statements are not to be shown, they are only a structural element
 * to connect multiple taxa to a single node, which is otherwise not possible.</li>
 * <li>{@link #getOtherNode() otherNode}: <br>
 * Without this kind of edge which points to other nodes in the same graph edge
 * the key would be a perfect tree structure.</li>
 * <li>{@link #getSubkey() subkey}:<br>
 * Connects two key with each other. The path in the decision graph spans over
 * multiple keys.</li>
 * <li>{@link #getTaxon() taxon}:<br>
 * The taxa are the final leaf nodes of the decision graph, the decision process
 * ends and the respective object has been determined as being a representative
 * of this {@link Taxon}.</li>
 * 
 * <h4>Notes</h4>
 * <p>
 * A polytomous key node can be referenced from multiple other nodes. Therefore
 * a node does not have a single parent. Nevertheless it always belongs to a
 * main key though it may be referenced also by other key nodes.
 * 
 * 
 * @author a.mueller
 * @created 13-Oct-2010
 * 
 */
@SuppressWarnings("serial")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PolytomousKeyNode", propOrder = {
		"key",
		"parent",
		"children",
		"sortIndex",
		"nodeNumber",
		"statement",
		"question",
		"feature",
		"taxon",
		"subkey",
		"otherNode",
		"modifyingText"
})
@XmlRootElement(name = "FeatureNode")
@Entity
@Audited
public class PolytomousKeyNode extends VersionableEntity implements IMultiLanguageTextHolder {
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
    @OrderBy("sortIndex")
	@OneToMany(fetch = FetchType.LAZY, mappedBy="parent")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE})
	private List<PolytomousKeyNode> children = new ArrayList<PolytomousKeyNode>();

    
    @XmlElement(name = "Parent")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @Cascade(CascadeType.SAVE_UPDATE)
    @ManyToOne(fetch = FetchType.LAZY, targetEntity=PolytomousKeyNode.class)
    @JoinColumn(name="parent_id" /*, insertable=false, updatable=false, nullable=false*/)
    private PolytomousKeyNode parent;
    
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

	@XmlElement(name = "Feature")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private Feature feature;
	
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
	
	private Integer nodeNumber = 0;
    
    
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
	 * Creates a new empty polytomous key node instance.
	 */
	public static PolytomousKeyNode NewInstance(){
		return new PolytomousKeyNode();
	}
	
	/** 
	 * Creates a new empty polytomous key node instance and sets the node number to 0.
	 */
	public static PolytomousKeyNode NewRootInstance(){
		PolytomousKeyNode result = new PolytomousKeyNode();
		result.setNodeNumber(0);
		return result;
	}

	/** 
	 * Creates a new polytomous key node instance.
	 * 
	 */
	public static PolytomousKeyNode NewInstance(String statement){
		PolytomousKeyNode result = new PolytomousKeyNode();
		result.setStatement(KeyStatement.NewInstance(statement));
		return result;
	}
	
	/** 
	 * Creates a new polytomous key node instance.
	 * 
	 */
	public static PolytomousKeyNode NewInstance(String statement, String question, Taxon taxon, Feature feature){
		PolytomousKeyNode result = new PolytomousKeyNode();
		result.setTaxon(taxon);
		result.setStatement(KeyStatement.NewInstance(statement));
		result.setQuestion(KeyStatement.NewInstance(question));
		result.setFeature(feature);
		return result;
	}

	
	//** ********************** CHILDREN ******************************/

	
	/**
	 * @return
	 */
	public PolytomousKey getKey() {
		return key;
	}

	/**
	 * @param key
	 */
	public void setKey(PolytomousKey key) {
		this.key = key;
	}
	

	/**
	 * The node number is the number of the node within the key. This corresponds to the
	 * number for key choices in written keys.
	 */
	public Integer getNodeNumber() {
		return nodeNumber;
	}
	
	/**
	 * Is computed automatically and therefore should not be set by the user.
	 */
	private void setNodeNumber(Integer nodeNumber) {
		this.nodeNumber = nodeNumber;
	}
	
	/**
	 * Returns the parent node of <code>this</code> child.
	 * @return
	 */
	public PolytomousKeyNode getParent() {
		return parent;
	}


	/**
	 * For bidirectional use only !
	 * @param parent
	 */
	protected void setParent(PolytomousKeyNode parent) {
		this.parent = parent;
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
		if (index < 0 || index > children.size() + 1){
			throw new IndexOutOfBoundsException("Wrong index: " + index);
		}
		
		children.add(index, child);
		child.setKey(this.getKey());
		//TODO workaround (see sortIndex doc)
		for(int i = 0; i < children.size(); i++){
			children.get(i).sortIndex = i;
		}
		child.sortIndex = index;
		updateNodeNumber();
		child.setParent(this);
	}
	
	private void updateNodeNumber() {
		int nodeNumber = 0;
		PolytomousKeyNode root = getKey().getRoot();
		root.setNodeNumber(nodeNumber++);
		nodeNumber = updateChildNodeNumbers(nodeNumber, root);
		
	}

	private int updateChildNodeNumbers(int nodeNumber, PolytomousKeyNode parent) {
		if (parent.isLeaf()){
			parent.setNodeNumber(null);
		}else{
			for (PolytomousKeyNode child : parent.getChildren()){
				child.setNodeNumber(nodeNumber++);
				nodeNumber = updateChildNodeNumbers(nodeNumber, child);
			}
		}
		return nodeNumber;
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
		
		PolytomousKeyNode child = children.get(index);
		if (child != null){
			children.remove(index);
//			child.setParent(null);
			//TODO workaround (see sortIndex doc)
			for(int i = 0; i < children.size(); i++){
				PolytomousKeyNode childAt = children.get(i);
				childAt.sortIndex = i;
		}
			child.sortIndex = null;
		}
		updateNodeNumber();
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

	
//** ********************** QUESTIONS AND STATEMENTS ******************************/

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
	 * This is a convenience method to set the statement text for this node 
	 * in the given language. <BR>
	 * If no statement exists yet a new statement is created. <BR>
	 * If a statement text in the given language exists already it is overwritten 
	 * and the old text is returned.
	 * If language is <code>null</code> the default language is used instead.
	 *  
	 * @param text the statement text
	 * @param language the language of the statement text
	 * @return the old statement text in the given language as LanguageString
	 */
	 public LanguageString addStatementText(String text, Language language){
		if (language == null){
			language = Language.DEFAULT();
		}
		if (this.statement == null){
			setStatement(KeyStatement.NewInstance());
		}
		return getStatement().putLabel(language, text);
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
	 * This is a convenience method to sets the question text for this node 
	 * in the given language. <BR>
	 * If no question exists yet a new question is created. <BR>
	 * If a question text in the given language exists already it is overwritten 
	 * and the old text is returned.
	 * If language is <code>null</code> the default language is used instead.
	 *  
	 * @param text
	 * @param language
	 * @return
	 */
	 public LanguageString addQuestionText(String text, Language language){
		if (language == null){
			language = Language.DEFAULT();
		}
		if (this.question == null){
			setQuestion(KeyStatement.NewInstance());
		}
		return getQuestion().putLabel(language, text);
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
	 * Returns the {@link MultilanguageText} like "an unusual form of", commenting the determined taxon. 
	 * That is a modifyingText may by used to comment or to constraint the decision step represented by the edge leading to <i>this</i> node
	 * <p>
	 * All {@link LanguageString language strings}
	 * contained in the multilanguage texts should all have the same meaning.<BR>
	 */
	public Map<Language,LanguageString> getModifyingText(){
		return this.modifyingText;
	}



	/**
	 * See {@link  #getModifyingText}
	 * 
	 * @param description	the language string describing the validity
	 * 						in a particular language
	 * @see    	   			#getModifyingText()
	 * @see    	   			#putModifyingText(Language, String)
	 * @deprecated			should follow the put semantic of maps, this method will be removed in v4.0
	 * 						Use the {@link #putModifyingText(LanguageString) putModifyingText} method instead
	 */
	public LanguageString addModifyingText(LanguageString description){
		return this.putModifyingText(description);
	}
	
	/**
	 * See {@link  #getModifyingText}
	 * 
	 * @param description	the language string describing the validity
	 * 						in a particular language
	 * @see    	   			#getModifyingText()
	 * @see    	   			#putModifyingText(Language, String)
	 */
	public LanguageString putModifyingText(LanguageString description){
		return this.modifyingText.put(description.getLanguage(),description);
	}
	
	/**
	 * See {@link  #getModifyingText}
	 * 
	 * @param text		the string describing the validity
	 * 					in a particular language
	 * @param language	the language in which the text string is formulated
	 * @see    	   		#getModifyingText()
	 * @see    	   		#putModifyingText(LanguageString)
	 * @deprecated		should follow the put semantic of maps, this method will be removed in v4.0
	 * 					Use the {@link #putModifyingText(Language, String) putModifyingText} method instead
	 */
	public LanguageString addModifyingText(String text, Language language){
		return this.putModifyingText(language, text);
	}
	
	/**
	 * See {@link  #getModifyingText}
	 * 
	 * @param text		the string describing the validity
	 * 					in a particular language
	 * @param language	the language in which the text string is formulated
	 * @see    	   		#getModifyingText()
	 * @see    	   		#putModifyingText(LanguageString)
	 */
	public LanguageString putModifyingText(Language language, String text){
		return this.modifyingText.put(language, LanguageString.NewInstance(text, language));
	}
	/** 
	 * See {@link  #getModifyingText}
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

//	TODO
	public void setFeature(Feature feature) {
		this.feature = feature;
	}

	public Feature getFeature() {
		return feature;
	}

	
	//*********************** CLONE ********************************************************/
		
		/** 
		 * Clones <i>this</i> PolytomousKeyNode. This is a shortcut that enables to create
		 * a new instance that differs only slightly from <i>this</i> PolytomousKeyNode by
		 * modifying only some of the attributes.
		 * The parent, the feature and the key are the are the same as for the original feature node
		 * the children are removed.
		 * 
		 * @see eu.etaxonomy.cdm.model.common.VersionableEntity#clone()
		 * @see java.lang.Object#clone()
		 */
		@Override
		public Object clone() {
			PolytomousKeyNode result;
			try {
				result = (PolytomousKeyNode)super.clone();
				result.children = new ArrayList<PolytomousKeyNode>();
				
				result.modifyingText = new HashMap<Language, LanguageString>();
				for (Entry<Language, LanguageString> entry: this.modifyingText.entrySet()){
					result.putModifyingText(entry.getValue());
				}
				
				
				return result;
			}catch (CloneNotSupportedException e) {
				logger.warn("Object does not implement cloneable");
				e.printStackTrace();
				return null;
			}
		}

			
			
			
		

}