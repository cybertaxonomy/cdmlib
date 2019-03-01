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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.hibernate.HHH_9751_Util;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.IHasTermType;
import eu.etaxonomy.cdm.model.common.ITreeNode;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.VersionableEntity;

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
 * @since 08-Nov-2007 13:06:16
 */
@SuppressWarnings("serial")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FeatureNode", propOrder = {
		"featureTree",
		"termType",
		"feature",
		"parent",
		"treeIndex",
		"sortIndex",
		"children",
		"onlyApplicableIf",
		"inapplicableIf"
})
@XmlRootElement(name = "FeatureNode")
@Entity
@Audited
@Table(name="FeatureNode", indexes = { @Index(name = "featureNodeTreeIndex", columnList = "treeIndex") })
public class FeatureNode <T extends DefinedTermBase> extends VersionableEntity
            implements ITreeNode<FeatureNode<T>>, IHasTermType, Cloneable {
	private static final Logger logger = Logger.getLogger(FeatureNode.class);

    //This is the main key a node belongs to. Although other keys may also reference
	//<code>this</code> node, a node usually belongs to a given key.
	@XmlElement(name = "FeatureTree")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY, targetEntity=FeatureTree.class)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE}) //TODO this usage is incorrect, needed only for OneToMany, check why it is here, can it be removed??
	 //TODO Val #3379
//    @NotNull
	private FeatureTree<T> featureTree;

    /**
     * The {@link TermType type} of this term node.
     * Must be the same type as for the {@link FeatureTree term collection}
     * this node belongs to and as the term type of the term this node links to.
     */
    @XmlAttribute(name ="TermType")
    @Column(name="termType")
    @NotNull
    @Type(type = "eu.etaxonomy.cdm.hibernate.EnumUserType",
        parameters = {@org.hibernate.annotations.Parameter(name  = "enumClass", value = "eu.etaxonomy.cdm.model.common.TermType")}
    )
    @Audited
    private TermType termType;

    @XmlElement(name = "Feature")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY, targetEntity=DefinedTermBase.class)
	private T feature;

    @XmlElement(name = "Parent")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY, targetEntity=FeatureNode.class)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	@JoinColumn(name="parent_id")
	private FeatureNode<T> parent;


    @XmlElement(name = "treeIndex")
    @Column(length=255)
    private String treeIndex;

    @XmlElementWrapper(name = "Children")
    @XmlElement(name = "Child")
    //see https://dev.e-taxonomy.eu/trac/ticket/3722
    @OrderColumn(name="sortIndex")
    @OrderBy("sortIndex")
	@OneToMany(fetch = FetchType.LAZY, mappedBy="parent", targetEntity=FeatureNode.class)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	private List<FeatureNode<T>> children = new ArrayList<>();

    //see https://dev.e-taxonomy.eu/trac/ticket/3722
    private Integer sortIndex;

	@XmlElementWrapper(name = "OnlyApplicableIf")
	@XmlElement(name = "OnlyApplicableIf")
	@XmlIDREF
	@XmlSchemaType(name="IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
//	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})  remove cascade #5755
	@JoinTable(name="FeatureNode_DefinedTermBase_OnlyApplicable")
	private final Set<State> onlyApplicableIf = new HashSet<>();

	@XmlElementWrapper(name = "InapplicableIf")
	@XmlElement(name = "InapplicableIf")
	@XmlIDREF
	@XmlSchemaType(name="IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
//	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})  remove cascade #5755
	@JoinTable(name="FeatureNode_DefinedTermBase_InapplicableIf")
	private final Set<State> inapplicableIf = new HashSet<>();

// ***************************** FACTORY *********************************/

	/**
     * Creates a new empty term node instance.
     *
     * @see #NewInstance(Feature)
     */
    public static <T extends DefinedTermBase<T>> FeatureNode<T> NewInstance(TermType termType){
        return new FeatureNode<>(termType);
    }

	/**
	 * Creates a new empty feature node instance.
	 *
	 * @see #NewInstance(Feature)
	 */
	public static FeatureNode<Feature> NewInstance(){
		return new FeatureNode<>(TermType.Feature);
	}

	/**
	 * Creates a new feature node instance only with the given {@link DefinedTermBase term}
	 * (without parent and children). The termType of the feature node is the termType of
	 * the given term
	 *
	 * @param	term	the term assigned to the new feature node
	 * @see 			#NewInstance()
	 */
	public static <T extends DefinedTermBase<T>> FeatureNode<T> NewInstance(T term){
	    FeatureNode<T> result = new FeatureNode<>(term.getTermType());
	    result.setTerm(term);
	    return result;
	}

// ******************** CONSTRUCTOR ***************************************/

	//TODO needed?
    @Deprecated
    protected FeatureNode(){}

	/**
	 * Class constructor: creates a new empty feature node instance.
	 */
	protected FeatureNode(TermType termType) {
	    this.termType = termType;
	    IHasTermType.checkTermTypeNull(this);
	}

    @Override
    public TermType getTermType() {
        return termType;
    }

//*************************** TREE ************************************/

	public FeatureTree<T> getFeatureTree() {
		return featureTree;
	}

	protected void setFeatureTree(FeatureTree<T> featureTree) {
		checkTermType(featureTree);
	    this.featureTree = featureTree;
	}

//** ********************** FEATURE ******************************/

	/**
     * Returns the {@link Feature feature} <i>this</i> feature node is based on.
     */
    public T getTerm() {
        return feature;
    }
    /**
     * @see #getFeature()
     */
    public void setTerm(T term) {
        checkTermType(term);
        this.feature = term;
    }

//** ********************** PARENT ******************************/

	/**
	 * Returns the feature node <i>this</i> feature node is a child of.
	 *
	 * @see	#getChildNodes()
	 */
	@Override
    public FeatureNode<T> getParent() {
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
	protected void setParent(FeatureNode<T> parent) {
		checkTermType(parent);
	    this.parent = parent;
	}

//** ********************** CHILDREN ******************************/


	/**
	 * @deprecated for internal use only.
	 */
	//see #4278 , #4200
	@Deprecated
    protected void setSortIndex(Integer sortIndex) {
		this.sortIndex = sortIndex;
	}

	/**
	 * Returns the (ordered) list of feature nodes which are children nodes of
	 * <i>this</i> feature node.
	 */
	@Override
    public List<FeatureNode<T>> getChildNodes() {
	    return children;
	}

	/**
	 * Adds the given feature node at the end of the list of children of
	 * <i>this</i> feature node. Due to bidirectionality this method must also
	 * assign <i>this</i> feature node as the parent of the given child.
	 *
	 * @param	child	the feature node to be added
	 * @see				#getChildNodes()
	 * @see				#setChildren(List)
	 * @see				#addChild(FeatureNode, int)
	 * @see				#removeChild(FeatureNode)
	 * @see				#removeChild(int)
	 */
	public void addChild(FeatureNode<T> child){
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
	 * @see				#getChildNodes()
	 * @see				#setChildren(List)
	 * @see				#addChild(FeatureNode)
	 * @see				#removeChild(FeatureNode)
	 * @see				#removeChild(int)
	 */
	public void addChild(FeatureNode<T> child, int index){
	    checkTermType(child);
	    List<FeatureNode<T>> children = this.getChildNodes();
		if (index < 0 || index > children.size() + 1){
			throw new IndexOutOfBoundsException("Wrong index: " + index);
		}
		if (child.getParent() != null){
			child.getParent().removeChild(child);
		}
		child.setParent(this);
		child.setFeatureTree(this.getFeatureTree());
		children.add(index, child);
		//TODO workaround (see sortIndex doc)
		for(int i = 0; i < children.size(); i++){
			children.get(i).setSortIndex(i);
		}
		child.setSortIndex(index);
	}


    /**
	 * Removes the given feature node from the list of {@link #getChildNodes() children}
	 * of <i>this</i> feature node.
	 *
	 * @param  child	the feature node which should be removed
	 * @see     		#getChildNodes()
	 * @see				#addChild(FeatureNode, int)
	 * @see				#addChild(FeatureNode)
	 * @see				#removeChild(int)
	 */
	public void removeChild(FeatureNode<T> child){

	    int index = children.indexOf(child);
		if (index >= 0){
			removeChild(index);
		}
	}
	/**
	 * Removes the feature node placed at the given (index + 1) position from
	 * the list of {@link #getChildNodes() children} of <i>this</i> feature node.
	 * If the given index is out of bounds no child will be removed.
	 *
	 * @param  index	the integer indicating the position of the feature node to
	 * 					be removed
	 * @see     		#getChildNodes()
	 * @see				#addChild(FeatureNode, int)
	 * @see				#addChild(FeatureNode)
	 * @see				#removeChild(FeatureNode)
	 */
	public void removeChild(int index){
	   FeatureNode<T> child = children.get(index);
	   if (child != null){
			children.remove(index);
			child.setParent(null);
			child.setFeatureTree(null);
			//TODO workaround (see sortIndex doc)
			for(int i = 0; i < children.size(); i++){
				FeatureNode<T> childAt = children.get(i);
				childAt.setSortIndex(i);
			}
			child.setSortIndex(null);
		}
	}

	/**
	 * Returns the feature node placed at the given (childIndex + 1) position
	 * within the list of {@link #getChildNodes() children} of <i>this</i> feature node.
	 * If the given index is out of bounds no child will be returned.
	 *
	 * @param  childIndex	the integer indicating the position of the feature node
	 * @see     			#getChildNodes()
	 * @see					#addChild(FeatureNode, int)
	 * @see					#removeChild(int)
	 */
	public FeatureNode<T> getChildAt(int childIndex) {
	    return children.get(childIndex);
	}

	/**
	 * Returns the number of children nodes of <i>this</i> feature node.
	 *
	 * @see	#getChildNodes()
	 */
	@Transient
	public int getChildCount() {
		return children.size();
	}

	/**
	 * Returns the integer indicating the position of the given feature node
	 * within the list of {@link #getChildNodes() children} of <i>this</i> feature node.
	 * If the list does not contain this node then -1 will be returned.
	 *
	 * @param  node	the feature node the position of which is being searched
	 * @see			#addChild(FeatureNode, int)
	 * @see			#removeChild(int)
	 */
	public int getIndex(FeatureNode<T> node) {
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
	 * @see	#getChildNodes()
	 * @see	#getChildCount()
	 */
	@Transient
	public boolean isLeaf() {
		return children.size() < 1;
	}

	/**
	 * Whether <code>this</code> node is the root node of the associated {@link FeatureTree feature tree}.
	 *
	 * @return <code>true</code> if <code>this</code> is the feature trees root node, <code>false</code> if not
	 */
	@Transient
	public boolean isRoot(){
		if(getFeatureTree() != null){
			return this.equals(getFeatureTree().getRoot());
		}
		return false;
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

//	//** ********************** QUESTIONS ******************************/
//
//	/**
//	 * Returns the {@link Representation question} formulation that
//	 * corresponds to <i>this</i> feature node and the corresponding
//	 * {@link Feature feature} in case it is part of a
//	 * {@link PolytomousKey polytomous key}.
//	 */
//	public Set<Representation> getQuestions() {
//		return this.questions;
//	}
//
//	public void addQuestion(Representation question) {
//		this.questions.add(question);
//	}
//
//	public void removeQuestion(Representation question) {
//		this.questions.remove(question);
//	}
//
//	@Transient
//	public Representation getQuestion(Language lang) {
//		for (Representation question : questions){
//			Language reprLanguage = question.getLanguage();
//			if (reprLanguage != null && reprLanguage.equals(lang)){
//				return question;
//			}
//		}
//		return null;
//	}

	/**
     * Throws {@link IllegalArgumentException} if the given
     * term has not the same term type as this term or if term type is null.
     * @param term
     */
    private void checkTermType(IHasTermType term) {
        IHasTermType.checkTermTypes(term, this);
    }

	/**
	 * Returns all terms that are contained in this node or a child node
	 *
	 * @param featureNode
	 * @param features
	 * @return
	 */
	@Transient
	public Set<T> getDistinctFeaturesRecursive(Set<T> features){
		T term = this.getTerm();

		if(term!=null){
		    features.add(term);
		}

		for(FeatureNode<T> childNode : this.getChildNodes()){
			features.addAll(childNode.getDistinctFeaturesRecursive(features));
		}

		return features;
	}

	public FeatureNode<T> cloneDescendants(){
		FeatureNode<T> clone = (FeatureNode<T>)this.clone();
		FeatureNode<T> childClone;

		for(FeatureNode<T> childNode : this.getChildNodes()){
			childClone = (FeatureNode<T>) childNode.clone();
			for (FeatureNode<T> childChild:childNode.getChildNodes()){
				childClone.addChild(childChild.cloneDescendants());
			}
			clone.addChild(childClone);

		}
		return clone;
	}

//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> FeatureNode. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> FeatureNode by
	 * modifying only some of the attributes.
	 * The parent, the feature and the featureTree are the are the same as for the original feature node
	 * the children are removed
	 *
	 * @see eu.etaxonomy.cdm.model.common.VersionableEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		FeatureNode<T> result;
		try {
			result = (FeatureNode<T>)super.clone();
			result.children = new ArrayList<>();
			return result;
		}catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}



	}

// ********************** TREE NODE METHODS ******************************/

	@Override
	public String treeIndex() {
		return this.treeIndex;
	}    @Override
    public String treeIndexLike() {
        return treeIndex + "%";
    }
    @Override
    public String treeIndexWc() {
        return treeIndex + "*";
    }

	@Override
	@Deprecated
	public void setTreeIndex(String newTreeIndex) {
		this.treeIndex = newTreeIndex;
	}


	@Override
	@Deprecated
	public int treeId() {
		if (this.featureTree == null){
			return -1;
		}else{
			return this.featureTree.getId();
		}
	}

	private void updateSortIndex(){
	 // TODO workaround (see sortIndex doc)
        for (int i = 0; i < children.size(); i++) {
            children.get(i).setSortIndex(i);
        }
	}

	public void removeNullValueFromChildren(){
	    HHH_9751_Util.removeAllNull(children);
	    updateSortIndex();
	}


}
