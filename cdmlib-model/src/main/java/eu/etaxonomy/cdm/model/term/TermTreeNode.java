/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.model.term;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
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
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.hibernate.HHH_9751_Util;
import eu.etaxonomy.cdm.model.common.ITreeNode;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.State;

/**
 * The class for tree nodes within a {@link TermTree feature tree} structure.
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
@XmlType(name = "TermTreeNode", propOrder = {
		"parent",
		"treeIndex",
		"sortIndex",
		"children",
		"onlyApplicableIf",
		"inapplicableIf"
})
@XmlRootElement(name = "TermTreeNode")
@Entity
@Audited
public class TermTreeNode <T extends DefinedTermBase>
            extends TermRelationBase<T, TermTreeNode<T>, TermTree>
            implements ITreeNode<TermTreeNode<T>> {

    private static final Logger logger = Logger.getLogger(TermTreeNode.class);

    @XmlElement(name = "Parent")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @ManyToOne(fetch = FetchType.LAZY, targetEntity=TermTreeNode.class)
    @Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})
	@JoinColumn(name="parent_id")
	private TermTreeNode<T> parent;


    @XmlElement(name = "treeIndex")
    @Column(length=255)
    private String treeIndex;

    @XmlElementWrapper(name = "Children")
    @XmlElement(name = "Child")
    //see https://dev.e-taxonomy.eu/trac/ticket/3722
    @OrderColumn(name="sortIndex")
    @OrderBy("sortIndex")
	@OneToMany(fetch = FetchType.LAZY, mappedBy="parent", targetEntity=TermTreeNode.class)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	private List<TermTreeNode<T>> children = new ArrayList<>();

    //see https://dev.e-taxonomy.eu/trac/ticket/3722
    private Integer sortIndex;

	@XmlElementWrapper(name = "OnlyApplicableIf")
	@XmlElement(name = "OnlyApplicableIf")
	@XmlIDREF
	@XmlSchemaType(name="IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
//	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})  remove cascade #5755
	@JoinTable(name="TermTreeNode_DefinedTermBase_OnlyApplicable")
	private final Set<State> onlyApplicableIf = new HashSet<>();

	@XmlElementWrapper(name = "InapplicableIf")
	@XmlElement(name = "InapplicableIf")
	@XmlIDREF
	@XmlSchemaType(name="IDREF")
	@ManyToMany(fetch = FetchType.LAZY)
//	@Cascade({CascadeType.SAVE_UPDATE,CascadeType.MERGE})  remove cascade #5755
	@JoinTable(name="TermTreeNode_DefinedTermBase_InapplicableIf")
	private final Set<State> inapplicableIf = new HashSet<>();

// ***************************** FACTORY *********************************/

	//no factory methods should be provided as FeatureNodes should only
	//be created as children of their parent node (#8257)

// ******************** CONSTRUCTOR ***************************************/

	//TODO needed?
    @Deprecated
    protected TermTreeNode(){}

	/**
	 * Class constructor: creates a new empty feature node instance.
	 */
	protected TermTreeNode(TermType termType) {
	    super(termType);
	}

//************************* PARENT ******************************/

	/**
	 * Returns the feature node <i>this</i> feature node is a child of.
	 *
	 * @see	#getChildNodes()
	 */
	@Override
    public TermTreeNode<T> getParent() {
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
	protected void setParent(TermTreeNode<T> parent) {
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
    public List<TermTreeNode<T>> getChildNodes() {
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
	 * @see				#addChild(TermTreeNode, int)
	 * @see				#removeChild(TermTreeNode)
	 * @see				#removeChild(int)
	 */
	public void addChild(TermTreeNode<T> child){
		addChild(child, children.size());
	}

	/**
     * Creates a new node without a term and adds it to the end of
     * the list of children of
     * <i>this</i> node. Due to bidirectionality this method must also
     * assign <i>this</i> feature node as the parent of the new child.
     *
     * @return the newly created child node
     * @see             #getChildNodes()
     * @see             #setChildren(List)
     * @see             #removeChild(FeatureNode)
     * @see             #removeChild(int)
     */
    public TermTreeNode<T> addChild(){
        return addChild((T)null, children.size());
    }

	/**
	 * Creates a new node for the given term and adds it to the end of
	 * the list of children of
	 * <i>this</i> node. Due to bidirectionality this method must also
	 * assign <i>this</i> feature node as the parent of the new child.
	 *
	 * @param	term	the term to be added
	 * @return the newly created child node
	 * @see				#getChildNodes()
	 * @see				#setChildren(List)
	 * @see				#removeChild(FeatureNode)
	 * @see				#removeChild(int)
	 */
	public TermTreeNode<T> addChild(T term){
	    return addChild(term, children.size());
	}

    /**
     * Creates a new node for the given term and adds it at the
     * given (index + 1) position of the list of children of
     * <i>this</i> node. Due to bidirectionality this method must also
     * assign <i>this</i> feature node as the parent of the new child.
     *
     * @param   term    the term to be added
     * @return the newly created child node
     * @see             #getChildNodes()
     * @see             #setChildren(List)
     * @see             #removeChild(FeatureNode)
     * @see             #removeChild(int)
     */
	public TermTreeNode<T> addChild(T term, int index){
	    TermTreeNode<T> child = new TermTreeNode<>(getTermType());
	    if(term!=null){
	        child.setTerm(term);
	    }
	    checkTermType(child);

	    List<TermTreeNode<T>> children = this.getChildNodes();
	    if (index < 0 || index > children.size() + 1){
	        throw new IndexOutOfBoundsException("Wrong index: " + index);
	    }
	    child.setParent(this);
	    child.setGraph(this.getGraph());
	    children.add(index, child);
	    //TODO workaround (see sortIndex doc)
	    for(int i = 0; i < children.size(); i++){
	        children.get(i).setSortIndex(i);
	    }
	    child.setSortIndex(index);
	    return child;
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
	 * @see				#addChild(TermTreeNode)
	 * @see				#removeChild(TermTreeNode)
	 * @see				#removeChild(int)
	 */
	public void addChild(TermTreeNode<T> child, int index){
	    checkTermType(child);
	    List<TermTreeNode<T>> children = this.getChildNodes();
		if (index < 0 || index > children.size() + 1){
			throw new IndexOutOfBoundsException("Wrong index: " + index);
		}
		if (child.getParent() != null){
			child.getParent().removeChild(child);
		}
		child.setParent(this);
		child.setGraph(this.getGraph());
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
	 * @see				#addChild(TermTreeNode, int)
	 * @see				#addChild(TermTreeNode)
	 * @see				#removeChild(int)
	 */
	public void removeChild(TermTreeNode<T> child){

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
	 * @see				#addChild(TermTreeNode, int)
	 * @see				#addChild(TermTreeNode)
	 * @see				#removeChild(TermTreeNode)
	 */
	public void removeChild(int index){
	   TermTreeNode<T> child = children.get(index);
	   if (child != null){
			children.remove(index);
			child.setParent(null);
			child.setGraph(null);
			//TODO workaround (see sortIndex doc)
			for(int i = 0; i < children.size(); i++){
				TermTreeNode<T> childAt = children.get(i);
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
	 * @see					#addChild(TermTreeNode, int)
	 * @see					#removeChild(int)
	 */
	public TermTreeNode<T> getChildAt(int childIndex) {
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
	 * @see			#addChild(TermTreeNode, int)
	 * @see			#removeChild(int)
	 */
	public int getIndex(TermTreeNode<T> node) {
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
	 * Whether <code>this</code> node is the root node of the associated {@link TermTree feature tree}.
	 *
	 * @return <code>true</code> if <code>this</code> is the feature trees root node, <code>false</code> if not
	 */
	@Transient
	public boolean isRoot(){
		if(getGraph() != null){
			return this.equals(getGraph().getRoot());
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
     * term has not the same term type as this term or if it is no sub or super type
     * or if term type is null.
     * @param term
     */
    private void checkTermTypeKindOf(IHasTermType descendant) {
        IHasTermType.checkTermTypeEqualOrDescendant(this, descendant);
    }

//*********************** Terms ************************************/

	/**
	 * Returns all terms that are contained in this node or a child node
	 *
	 * @param terms
	 * @return
	 */
	@Transient
	public Set<T> getDistinctTermsRecursive(Set<T> terms){
		T term = this.getTerm();
		if(term != null){
		    terms.add(term);
		}
		for(TermTreeNode<T> childNode : this.getChildNodes()){
			terms.addAll(childNode.getDistinctTermsRecursive(terms));
		}
		return terms;
	}

    /**
     * @return a list of terms which includes first the
     * term of this node and then recursively the list
     * of all children and grandChildren
     */
    public Collection<? extends T> asTermListRecursive(List<T> terms) {
        T term = this.getTerm();
        if(term != null){
            terms.add(term);
        }
        for(TermTreeNode<T> childNode : this.getChildNodes()){
            terms.addAll(childNode.asTermListRecursive(terms));
        }
        return terms;
    }


//*********************** CLONE ********************************************************/

	/**
	 * Clones <i>this</i> {@link TermTreeNode}. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> tree node by
	 * modifying only some of the attributes.
	 * The parent, the feature and the featureTree are the same as for the original feature node
	 * the children are removed
	 *
	 * @see eu.etaxonomy.cdm.model.common.VersionableEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		TermTreeNode<T> result;
		try {
			result = (TermTreeNode<T>)super.clone();
			result.children = new ArrayList<>();
			return result;
		}catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}

    public TermTreeNode<T> cloneDescendants(){
        TermTreeNode<T> clone = (TermTreeNode<T>)this.clone();
        TermTreeNode<T> childClone;

        for(TermTreeNode<T> childNode : this.getChildNodes()){
            childClone = (TermTreeNode<T>) childNode.clone();
            for (TermTreeNode<T> childChild:childNode.getChildNodes()){
                childClone.addChild(childChild.cloneDescendants());
            }
            clone.addChild(childClone);

        }
        return clone;
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
		if (this.getGraph() == null){
			return -1;
		}else{
			return this.getGraph().getId();
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
