// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
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
import org.hibernate.envers.Audited;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @created 31.03.2009
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonNode", propOrder = {
    "taxon",
    "parent",
    "classification",
    "childNodes",
    "referenceForParentChildRelation",
    "microReferenceForParentChildRelation",
    "countChildren",
    "synonymToBeUsed"
})
@XmlRootElement(name = "TaxonNode")
@Entity
@org.hibernate.annotations.Entity (selectBeforeUpdate = true)
@Audited
public class TaxonNode extends AnnotatableEntity implements ITreeNode, Cloneable{
	private static final long serialVersionUID = -4743289894926587693L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonNode.class);

	@XmlElement(name = "taxon")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	private Taxon taxon;


	@XmlElement(name = "parent")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	private TaxonNode parent;


	@XmlElement(name = "classification")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})

//	TODO @NotNull // avoids creating a UNIQUE key for this field
	private Classification classification;

	@XmlElementWrapper(name = "childNodes")
	@XmlElement(name = "childNode")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(mappedBy="parent", fetch=FetchType.LAZY)
   @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	private Set<TaxonNode> childNodes = new HashSet<TaxonNode>();

	@XmlElement(name = "reference")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	private Reference referenceForParentChildRelation;

	@XmlElement(name = "microReference")
	private String microReferenceForParentChildRelation;

	@XmlElement(name = "countChildren")
	private int countChildren;

//	private Taxon originalConcept;
//	//or
	@XmlElement(name = "synonymToBeUsed")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	private Synonym synonymToBeUsed;


	protected TaxonNode(){
		super();
	}

	/**
     * to create nodes either use {@link Classification#addChildTaxon(Taxon, Reference, String, Synonym)}
     * or {@link TaxonNode#addChildTaxon(Taxon, Reference, String, Synonym)}
	 * @param taxon
	 * @param classification
	 * @deprecated setting of classification is handled in the addTaxonNode() method,
	 * use TaxonNode(taxon) instead
	 */
	protected TaxonNode (Taxon taxon, Classification classification){
		this(taxon);
		setClassification(classification);
	}

	/**
     * to create nodes either use {@link Classification#addChildTaxon(Taxon, Reference, String, Synonym)}
     * or {@link TaxonNode#addChildTaxon(Taxon, Reference, String, Synonym)}
     *
	 * @param taxon
	 */
	protected TaxonNode(Taxon taxon){
		setTaxon(taxon);
	}



//************************ METHODS **************************/

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.taxon.ITreeNode#addChildTaxon(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String, eu.etaxonomy.cdm.model.taxon.Synonym)
	 */
	public TaxonNode addChildTaxon(Taxon taxon, Reference citation, String microCitation, Synonym synonymToBeUsed) {
		if (this.getClassification().isTaxonInTree(taxon)){
 			throw new IllegalArgumentException(String.format("Taxon may not be in a taxonomic view twice: %s", taxon.getTitleCache()));
		}

		return addChildNode(new TaxonNode(taxon), citation, microCitation, synonymToBeUsed);
	}

	/**
     * Moves a taxon node to a new parent. Descendents of the node are moved as well
     *
	 * @param childNode the taxon node to be moved to the new parent
	 * @return the child node in the state of having a new parent
	 */
	public TaxonNode addChildNode(TaxonNode childNode, Reference reference, String microReference, Synonym synonymToBeUsed){

        // check if this node is a descendant of the childNode
		if(childNode.getParentTreeNode() != this && childNode.isAncestor(this)){
			throw new IllegalAncestryException("New parent node is a descendant of the node to be moved.");
		}

		childNode.setParentTreeNode(this);

		childNode.setReference(reference);
		childNode.setMicroReference(microReference);
		childNode.setSynonymToBeUsed(synonymToBeUsed);

		return childNode;
	}

	/**
	 * Sets this nodes classification. Updates classification of child nodes recursively
     *
	 * If the former and the actual tree are equal() this method does nothing
     *
	 * @param newTree
	 */
	@Transient
	private void setClassificationRecursively(Classification newTree) {
		if(! newTree.equals(this.getClassification())){
			this.setClassification(newTree);
			for(TaxonNode childNode : this.getChildNodes()){
				childNode.setClassificationRecursively(newTree);
			}
		}
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.taxon.ITreeNode#removeChildNode(eu.etaxonomy.cdm.model.taxon.TaxonNode)
	 */
	public boolean deleteChildNode(TaxonNode node) {
		boolean result = removeChildNode(node);

		node.getTaxon().removeTaxonNode(node);
		node.setTaxon(null);

        ArrayList<TaxonNode> childNodes = new ArrayList<TaxonNode>(node.getChildNodes());
		for(TaxonNode childNode : childNodes){
			node.deleteChildNode(childNode);
		}

//		// two iterations because of ConcurrentModificationErrors
//        Set<TaxonNode> removeNodes = new HashSet<TaxonNode>();
//        for (TaxonNode grandChildNode : node.getChildNodes()) {
//                removeNodes.add(grandChildNode);
//        }
//        for (TaxonNode childNode : removeNodes) {
//                childNode.deleteChildNode(node);
//        }

		return result;
	}

	/**
     * Removes the child node from this node. Sets the parent and the classification of the child
	 * node to null
     *
	 * @param childNode
	 * @return
	 */
	protected boolean removeChildNode(TaxonNode childNode){
		boolean result = false;

		if(childNode == null){
			throw new IllegalArgumentException("TaxonNode may not be null");
		}
		if(HibernateProxyHelper.deproxy(childNode.getParent(), TaxonNode.class) != this){
			throw new IllegalArgumentException("TaxonNode must be a child of this node");
		}

		result = childNodes.remove(childNode);
		this.countChildren--;
		if (this.countChildren < 0){
			throw new IllegalStateException("children count must not be negative ");
		}
		childNode.setParent(null);
		childNode.setClassification(null);

		return result;
	}


	/**
	 * Remove this taxonNode From its taxonomic parent
     *
	 * @return true on success
	 */
	public boolean delete(){
		if(isTopmostNode()){
			return classification.deleteChildNode(this);
		}else{
			return getParent().deleteChildNode(this);
	}
    }

//*********** GETTER / SETTER ***********************************/
	
	public Taxon getTaxon() {
		return taxon;
	}
	
	
	protected void setTaxon(Taxon taxon) {
		this.taxon = taxon;
		if (taxon != null){
			taxon.addTaxonNode(this);
		}
	}
	@Transient
	public ITreeNode getParentTreeNode() {
		if(isTopmostNode())
			return getClassification();
		return parent;
	}

	public TaxonNode getParent(){
		return parent;
	}

	/**
	 * Sets the parent of this taxon node.
     *
	 * In most cases you would want to call setParentTreeNode(ITreeNode) which
     * handles updating of the bidirectional relationship
     *
	 * @param parent
     *
	 * @see setParentTreeNode(ITreeNode)
	 */
	protected void setParent(ITreeNode parent) {
		if(parent instanceof Classification){
			this.parent = null;
			return;
		}
		this.parent = (TaxonNode) parent;
	}

	/**
     * Sets the parent of this taxon node to the given parent. Cleans up references to
     * old parents and sets the classification to the new parents classification
     *
	 * @param parent
	 */
	@Transient
    protected void setParentTreeNode(ITreeNode parent){
		// remove ourselves from the old parent
		ITreeNode formerParent = this.getParentTreeNode();
		if(formerParent instanceof TaxonNode){  //child was a child itself
            ((TaxonNode) formerParent).removeChildNode(this);
		}
		else if((formerParent instanceof Classification) && ! formerParent.equals(parent)){ //child was root in old tree
			((Classification) formerParent).removeChildNode(this);
        }

		// set the new parent
		setParent(parent);

        // set the classification to the parents classification
		Classification classification = (parent instanceof Classification) ? (Classification) parent : ((TaxonNode) parent).getClassification();
		setClassificationRecursively(classification);

		// add this node to the parent child nodes
		parent.getChildNodes().add(this);

		// update the children count
		if(parent instanceof TaxonNode){
			TaxonNode parentTaxonNode = (TaxonNode) parent;
			parentTaxonNode.setCountChildren(parentTaxonNode.getCountChildren() + 1);
		}
	}

	public Classification getClassification() {
		return classification;
	}
	/**
	 * THIS METHOD SHOULD NOT BE CALLED!
	 * invisible part of the bidirectional relationship, for public use TaxonomicView.addRoot() or TaxonNode.addChild()
	 * @param classification
	 */
	protected void setClassification(Classification classification) {
		this.classification = classification;
	}

	public Set<TaxonNode> getChildNodes() {
		return childNodes;
	}

	/**
	 * Returns a set containing this node and all nodes that are descendants of this node
     *
     * @return
	 */
	protected Set<TaxonNode> getDescendants(){
		Set<TaxonNode> nodeSet = new HashSet<TaxonNode>();

		nodeSet.add(this);

		for(TaxonNode childNode : getChildNodes()){
			nodeSet.addAll(childNode.getDescendants());
        }

		return nodeSet;
	}

	/**
	 * Returns a set containing a clone of this node and of all nodes that are descendants of this node
     *
     * @return
	 */
	protected TaxonNode cloneDescendants(){

		TaxonNode clone = (TaxonNode)this.clone();
		TaxonNode childClone;

		for(TaxonNode childNode : getChildNodes()){
			childClone = (TaxonNode) childNode.clone();
			for (TaxonNode childChild:childNode.getChildNodes()){
				childClone.addChildNode(childChild.cloneDescendants(), childChild.getReference(), childChild.getMicroReference(), childChild.getSynonymToBeUsed());
			}
			clone.addChildNode(childClone, childNode.getReference(), childNode.getMicroReference(), childNode.getSynonymToBeUsed());


			//childClone.addChildNode(childNode.cloneDescendants());
        }
		return clone;
	}

	/**
     * Returns a
     *
	 * @return
	 */
	protected Set<TaxonNode> getAncestors(){
		Set<TaxonNode> nodeSet = new HashSet<TaxonNode>();


		nodeSet.add(this);

		if(this.getParent() != null){
			nodeSet.addAll(((TaxonNode) this.getParent()).getAncestors());
		}

		return nodeSet;
	}

	/**
	 * The reference for the parent child relationship
     *
	 * @see eu.etaxonomy.cdm.model.taxon.ITreeNode#getReference()
	 */
	public Reference getReference() {
		return referenceForParentChildRelation;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.taxon.ITreeNode#setReference(eu.etaxonomy.cdm.model.reference.Reference)
	 */
	public void setReference(Reference reference) {
		this.referenceForParentChildRelation = reference;
	}

	/**
     *
     *
	 * @see eu.etaxonomy.cdm.model.taxon.ITreeNode#getMicroReference()
	 */
	public String getMicroReference() {
		return microReferenceForParentChildRelation;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.taxon.ITreeNode#setMicroReference(java.lang.String)
	 */
	public void setMicroReference(String microReference) {
		this.microReferenceForParentChildRelation = microReference;
	}

	/**
	 * @return the count of children this taxon node has
	 */
	public int getCountChildren() {
		return countChildren;
	}

	/**
	 * @param countChildren
	 */
	protected void setCountChildren(int countChildren) {
		this.countChildren = countChildren;
	}
//	public Taxon getOriginalConcept() {
//		return originalConcept;
//	}
//	public void setOriginalConcept(Taxon originalConcept) {
//		this.originalConcept = originalConcept;
//	}
	public Synonym getSynonymToBeUsed() {
		return synonymToBeUsed;
	}
	public void setSynonymToBeUsed(Synonym synonymToBeUsed) {
		this.synonymToBeUsed = synonymToBeUsed;
	}

	/**
	 * Whether this TaxonNode is a direct child of the classification TreeNode
	 * @return
	 */
	@Transient
	public boolean isTopmostNode(){
		return parent == null;
	}

	/**
	 * Whether this TaxonNode is a descendant of the given TaxonNode
     *
	 * Caution: use this method with care on big branches. -> performance and memory hungry
     *
     * Protip: Try solving your problem with the isAscendant method which traverses the tree in the
	 * other direction (up). It will always result in a rather small set of consecutive parents beeing
	 * generated.
     *
	 * TODO implement more efficiently without generating the set of descendants first
     *
	 * @param possibleParent
	 * @return true if this is a descendant
	 */
	@Transient
	public boolean isDescendant(TaxonNode possibleParent){
		return possibleParent.getDescendants().contains(this);
	}

	/**
	 * Whether this TaxonNode is an ascendant of the given TaxonNode
     *
     *
	 * @param possibleChild
	 * @return true if there are ascendants
	 */
	@Transient
	public boolean isAncestor(TaxonNode possibleChild){
		return possibleChild.getAncestors().contains(this);
	}

	/**
	 * Whether this taxon has child nodes
     *
	 * @return true if the taxonNode has childNodes
	 */
	@Transient
	public boolean hasChildNodes(){
		return childNodes.size() > 0;
	}

//*********************** CLONE ********************************************************/
    /**
	 * Clones <i>this</i> taxon node. This is a shortcut that enables to create
	 * a new instance that differs only slightly from <i>this</i> taxon node by
	 * modifying only some of the attributes.<BR><BR>
	 * The child nodes are not copied.<BR>
	 * The taxon and parent are the same as for the original taxon node. <BR>
     *
	 * @see eu.etaxonomy.cdm.model.media.IdentifiableEntity#clone()
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone()  {
		TaxonNode result;
		try{
		result = (TaxonNode)super.clone();
		result.getTaxon().addTaxonNode(result);
		result.childNodes = new HashSet<TaxonNode>();
		result.countChildren = 0;

		return result;
		}catch (CloneNotSupportedException e) {
			logger.warn("Object does not implement cloneable");
			e.printStackTrace();
			return null;
		}
	}
}
