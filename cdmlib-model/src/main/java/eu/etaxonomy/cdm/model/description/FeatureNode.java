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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * The class for tree nodes within a {@link FeatureTree feature tree} structure.
 * Feature nodes are the elementary components of such a tree since they might
 * be related to other nodes as a parent or as a child. A feature node belongs
 * at most to one feature tree. It cannot have more than one parent node but
 * may have several child nodes. Parent/child relations are bidirectional:
 * a node N1 is the parent of a node N2 if and only if the node N2 is a child of
 * the node N1.
 * 
 * @author  m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:16
 */
@SuppressWarnings("serial")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FeatureNode", propOrder = {
		"feature",
		"parent",
		"children",
		"onlyApplicableIf",
		"inapplicableIf",
		"questions",
		"taxon"
})
@XmlRootElement(name = "FeatureNode")
@Entity
@Audited
public class FeatureNode extends VersionableEntity {
	private static final Logger logger = Logger.getLogger(FeatureNode.class);
	
    @XmlElement(name = "Feature")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
	private Feature feature;
    
    @XmlElement(name = "Parent")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name="parent_fk")
	private FeatureNode parent;
    
    @XmlElementWrapper(name = "Children")
    @XmlElement(name = "Child")
//    @IndexColumn(name="sortIndex", base = 0)
	@OneToMany(fetch = FetchType.LAZY,mappedBy="parent")
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	private List<FeatureNode> children = new ArrayList<FeatureNode>();

	@XmlElementWrapper(name = "OnlyApplicableIf")
	@XmlElement(name = "OnlyApplicableIf")
	@XmlIDREF
	@XmlSchemaType(name="IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinTable(name="FeatureNode_DefinedTermBase_OnlyApplicable")
	private Set<State> onlyApplicableIf = new HashSet<State>();

	@XmlElementWrapper(name = "InapplicableIf")
	@XmlElement(name = "InapplicableIf")
	@XmlIDREF
	@XmlSchemaType(name="IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinTable(name="FeatureNode_DefinedTermBase_InapplicableIf")
	private Set<State> inapplicableIf = new HashSet<State>();

	@XmlElementWrapper(name = "Questions")
	@XmlElement(name = "Question")
    @OneToMany(fetch=FetchType.EAGER)
	@Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE })
	private Set<Representation> questions = new HashSet<Representation>();

	@XmlElement(name = "Taxon")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade(CascadeType.SAVE_UPDATE)
	private Taxon taxon;
	
	/** 
	 * Class constructor: creates a new empty feature node instance.
	 */
	protected FeatureNode() {
		super();
	}

	/** 
	 * Creates a new empty feature node instance.
	 * 
	 * @see #NewInstance(Feature)
	 */
	public static FeatureNode NewInstance(){
		return new FeatureNode();
	}

	/** 
	 * Creates a new feature node instance only with the given {@link Feature feature}
	 * (without parent and children).
	 * 
	 * @param	feature	the feature assigned to the new feature node 
	 * @see 			#NewInstance()
	 */
	public static FeatureNode NewInstance(Feature feature){
		FeatureNode result = new FeatureNode();
		result.setFeature(feature);
		return result;
	}

	//** ********************** FEATURE ******************************/

	/** 
	 * Returns the {@link Feature feature} <i>this</i> feature node is based on.
	 */
	public Feature getFeature() {
		return feature;
	}
	/**
	 * @see	#getFeature() 
	 */
	public void setFeature(Feature feature) {
		this.feature = feature;
	}

//** ********************** PARENT ******************************/

	/** 
	 * Returns the feature node <i>this</i> feature node is a child of.
	 * 
	 * @see	#getChildren()
	 */
	public FeatureNode getParent() {
		return parent;
	}
	/**
	 * Assigns the given feature node as the parent of <i>this</i> feature node.
	 * Due to bidirectionality this method must also add <i>this</i> feature node
	 * to the list of children of the given parent.
	 * 
	 * @param	parent	the feature node to be set as parent 
	 * @see				#getParent() 
	 */
	protected void setParent(FeatureNode parent) {
		this.parent = parent;
	}
	
	//** ********************** CHILDREN ******************************/

	/** 
	 * Returns the (ordered) list of feature nodes which are children nodes of
	 * <i>this</i> feature node.
	 */
	public List<FeatureNode> getChildren() {
		return children;
	}

	/**
	 * Adds the given feature node at the end of the list of children of
	 * <i>this</i> feature node. Due to bidirectionality this method must also
	 * assign <i>this</i> feature node as the parent of the given child.
	 * 
	 * @param	child	the feature node to be added 
	 * @see				#getChildren() 
	 * @see				#setChildren(List)
	 * @see				#addChild(FeatureNode, int) 
	 * @see				#removeChild(FeatureNode)
	 * @see				#removeChild(int) 
	 */
	public void addChild(FeatureNode child){
		addChild(child, children.size());
	}
	/**
	 * Inserts the given feature node in the list of children of <i>this</i> feature node
	 * at the given (index + 1) position. If the given index is out of bounds
	 * an exception will arise.<BR>
	 * Due to bidirectionality this method must also assign <i>this</i> feature node
	 * as the parent of the given child.
	 * 
	 * @param	child	the feature node to be added 
	 * @param	index	the integer indicating the position at which the child
	 * 					should be added 
	 * @see				#getChildren() 
	 * @see				#setChildren(List)
	 * @see				#addChild(FeatureNode) 
	 * @see				#removeChild(FeatureNode)
	 * @see				#removeChild(int) 
	 */
	public void addChild(FeatureNode child, int index){
		if (index < 0 || index > children.size() + 1){
			throw new IndexOutOfBoundsException("Wrong index: " + index);
		}
		if (child.getParent() != null){
			child.getParent().removeChild(child);
		}
		child.setParent(this);		
		children.add(index, child);
	}
	/** 
	 * Removes the given feature node from the list of {@link #getChildren() children}
	 * of <i>this</i> feature node.
	 *
	 * @param  child	the feature node which should be removed
	 * @see     		#getChildren()
	 * @see				#addChild(FeatureNode, int) 
	 * @see				#addChild(FeatureNode) 
	 * @see				#removeChild(int) 
	 */
	public void removeChild(FeatureNode child){
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
	 * @see				#addChild(FeatureNode, int) 
	 * @see				#addChild(FeatureNode) 
	 * @see				#removeChild(FeatureNode) 
	 */
	public void removeChild(int index){
		FeatureNode child = children.get(index);
		if (child != null){
			children.remove(index);
			child.setParent(child);
		}
	}

	/** 
	 * Returns the feature node placed at the given (childIndex + 1) position
	 * within the list of {@link #getChildren() children} of <i>this</i> feature node.
	 * If the given index is out of bounds no child will be returned. 
	 * 
	 * @param  childIndex	the integer indicating the position of the feature node
	 * @see     			#getChildren()
	 * @see					#addChild(FeatureNode, int) 
	 * @see					#removeChild(int) 
	 */
	public FeatureNode getChildAt(int childIndex) {
			return children.get(childIndex);
	}

	/** 
	 * Returns the number of children nodes of <i>this</i> feature node.
	 * 
	 * @see	#getChildren()
	 */
	@Transient
	public int getChildCount() {
		return children.size();
	}

	/** 
	 * Returns the integer indicating the position of the given feature node
	 * within the list of {@link #getChildren() children} of <i>this</i> feature node.
	 * If the list does not contain this node then -1 will be returned. 
	 * 
	 * @param  node	the feature node the position of which is being searched
	 * @see			#addChild(FeatureNode, int) 
	 * @see			#removeChild(int) 
	 */
	public int getIndex(FeatureNode node) {
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

	/**
	 * Returns the set of {@link State states} implying rendering the
	 * concerned {@link Feature feature} applicable.
	 * If at least one state is present in this set, in a given description
	 * the {@link Feature feature} in <i>this</i> feature node is inapplicable
	 * unless any of the listed controlling states is present in the parent
	 * {@link Feature feature} description element {@link CategoricalData
	 * categoricalData}.
	 * This attribute is not equivalent to onlyApplicableIf in SDD as it is
	 * attached directly to the child feature rather than the parent, which
	 * allow having different applicable states for each child feature.
	 * 
	 * @see    #addApplicableState(State)
	 * @see    #removeApplicableState(State)
	 */
	public Set<State> getOnlyApplicableIf() {
		return onlyApplicableIf;
	}

	/**
	 * Adds an existing {@link State applicable state} to the set of
	 * {@link #getOnlyApplicableIf() applicable states} described in
	 * <i>this</i> feature node.<BR>
	 * 
	 * @param applicableState	the applicable state to be added to <i>this</i> feature node
	 * @see    	   								#getApplicableState()
	 */
	public void addApplicableState(State applicableState) {
		logger.debug("addApplicableState");
		this.onlyApplicableIf.add(applicableState);
	}

	/** 
	 * Removes one element from the set of
	 * {@link #getOnlyApplicableIf() applicable states} described in
	 * <i>this</i> feature node.<BR>
	 *
	 * @param  applicableState   the applicable state which should be removed
	 * @see    	   								#getApplicableState()
	 * @see     		  						#addApplicableState(State)
	 */
	public void removeApplicableState(State applicableState) {
		this.onlyApplicableIf.remove(applicableState);
	}

	/**
	 * Returns the set of {@link State states} implying rendering the
	 * concerned {@link Feature feature} inapplicable.
	 * If at least one {@link State inapplicable state} is defined in the set,
	 * in a given description the {@link Feature feature} attribute of
	 * <i>this</i> feature node is inapplicable when any of the listed
	 * controlling states is present.
	 * This attribute is not equivalent to inapplicableIf in SDD as it is
	 * attached directly to the child feature rather than the parent, which
	 * allow having different inapplicability rules for each child feature.
	 * 
	 * @see    #addInapplicableState(State)
	 * @see    #removeInapplicableState(State)
	 */
	public Set<State> getInapplicableIf() {
		return inapplicableIf;
	}

	/**
	 * Adds an existing {@link State inapplicable state} to the set of
	 * {@link #getInapplicableIf() inapplicable states} described in
	 * <i>this</i> feature node.<BR>
	 * 
	 * @param inapplicableState	the inapplicable state to be added to <i>this</i> feature node
	 * @see    	   								#getInapplicableState()
	 */
	public void addInapplicableState(State inapplicableState) {
		logger.debug("addInapplicableState");
		this.inapplicableIf.add(inapplicableState);
	}

	/** 
	 * Removes one element from the set of
	 * {@link #getInapplicableIf() inapplicable states} described in
	 * <i>this</i> feature node.<BR>
	 *
	 * @param  inapplicableState   the inapplicable state which should be removed
	 * @see    	   								#getInapplicableState()
	 * @see     		  						#addInapplicableState(State)
	 */
	public void removeInapplicableState(State inapplicableState) {
		this.inapplicableIf.remove(inapplicableState);
	}
	
	//** ********************** QUESTIONS ******************************/

	/** 
	 * Returns the {@link Representation question} formulation that
	 * corresponds to <i>this</i> feature node and the corresponding
	 * {@link Feature feature} in case it is part of a 
	 * {@link PolytomousKey polytomous key}.
	 */
	public Set<Representation> getQuestions() {
		return this.questions;
	}

	public void addQuestion(Representation question) {
		this.questions.add(question);
	}

	public void removeQuestion(Representation question) {
		this.questions.remove(question);
	}

	@Transient
	public Representation getQuestion(Language lang) {
		for (Representation question : questions){
			Language reprLanguage = question.getLanguage();
			if (reprLanguage != null && reprLanguage.equals(lang)){
				return question;
			}
		}
		return null;
	}
	
	/**
	 * Returns all features that are contained in this node or a child node
	 * 
	 * @param featureNode
	 * @param features
	 * @return
	 */
	@Transient
	public Set<Feature> getDistinctFeaturesRecursive(Set<Feature> features){
		Feature feature = this.getFeature();
		
		features.add(feature);
		
		for(FeatureNode childNode : this.getChildren()){
			features.addAll(childNode.getDistinctFeaturesRecursive(features));
		}
		
		return features;
	}
	
	//** ********************** TAXON ******************************/
	
	/** 
	 * Returns the {@link Taxon taxon} <i>this</i> terminal node is
	 * associated with.
	 */
	public Taxon getTaxon() {
		return taxon;
	}

	/**
	 * Assigns the given taxon to <i>this</i> feature node.
	 * 
	 * @param	taxon	the taxon to be set 
	 * @see				#getTaxon() 
	 */
	public void setTaxon(Taxon taxon) {
		this.taxon = taxon;
	}
}