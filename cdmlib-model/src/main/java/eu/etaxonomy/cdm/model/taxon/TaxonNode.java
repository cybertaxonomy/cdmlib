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

import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.mueller
 * @created 31.03.2009
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonNode", propOrder = {
    "taxon",
    "parent",
    "taxonomicTree",
    "childNodes",
    "referenceForParentChildRelation",
    "microReferenceForParentChildRelation",
    "countChildren",
    "synonymToBeUsed"
})
@XmlRootElement(name = "TaxonNode")
@Entity
@Audited
public class TaxonNode  extends AnnotatableEntity implements ITreeNode{
	private static final long serialVersionUID = -4743289894926587693L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonNode.class);
	
	@XmlElement(name = "taxon")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	private Taxon taxon;
	
	
	@XmlElement(name = "parent")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	private TaxonNode parent;
	
	
	@XmlElement(name = "taxonomicTree")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	private TaxonomicTree taxonomicTree;
	
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
	private ReferenceBase referenceForParentChildRelation;
	
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
	 * to create nodes either use TaxonomicView.addRoot() or TaxonNode.addChild();
	 * @param taxon
	 * @param taxonomicTree
	 * @deprecated setting of taxonomic tree is handled in the addTaxonNode() method,
	 * use TaxonNode(taxon) instead
	 */
	protected TaxonNode (Taxon taxon, TaxonomicTree taxonomicTree){
		this(taxon);
		setTaxonomicView(taxonomicTree);
	}
	
	/**
	 * to create nodes either use TaxonomicView.addRoot() or TaxonNode.addChild();
	 * 
	 * @param taxon
	 */
	protected TaxonNode(Taxon taxon){
		setTaxon(taxon);
	}

	
	
//************************ METHODS **************************/
	/**
	 * @deprecated developers should be forced to pass in null values if they choose so.
	 */
	@Deprecated
	public TaxonNode addChild(Taxon taxon){
		return addChild(taxon, null, null, null);
	}
	
	/**
	 * @deprecated developers should be forced to pass in null values if they choose so.
	 */
	@Deprecated
	public TaxonNode addChild(Taxon taxon, ReferenceBase ref, String microReference){
		return addChild(taxon, ref, microReference, null);
	}	
	
	/**
	 * 
	 * @param taxon
	 * @param ref
	 * @param microReference
	 * @param synonymUsed
	 * @return
	 * @deprecated use addChildTaxon() instead
	 */
	@Deprecated
	public TaxonNode addChild(Taxon taxon, ReferenceBase ref, String microReference, Synonym synonymUsed){
		return addChildTaxon(taxon, ref, microReference, synonymUsed);
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.taxon.ITreeNode#addChildTaxon(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.reference.ReferenceBase, java.lang.String, eu.etaxonomy.cdm.model.taxon.Synonym)
	 */
	public TaxonNode addChildTaxon(Taxon taxon, ReferenceBase citation,
			String microCitation, Synonym synonymToBeUsed) {
		if (this.getTaxonomicTree().isTaxonInTree(taxon)){
			throw new IllegalArgumentException("Taxon may not be twice in a taxonomic view");
		}
		
		return addChildNode(new TaxonNode(taxon), citation, microCitation, synonymToBeUsed);
	}
	
	/**
	 * 
	 * @param childNode
	 * @param ref
	 * @param microReference
	 * @param synonymUsed
	 * 
	 * @deprecated use addChildNode instead
	 */
	@Deprecated 
	protected void addChildNote(TaxonNode childNode, ReferenceBase ref, String microReference, Synonym synonymUsed){
		if (! childNode.getTaxonomicTree().equals(this.getTaxonomicTree())){
			throw new IllegalArgumentException("addChildNote(): both nodes must be part of the same view");
		}
		childNode.setParent(this);
		childNodes.add(childNode);
		this.countChildren++;
		childNode.setReferenceForParentChildRelation(ref);
		childNode.setMicroReferenceForParentChildRelation(microReference);
		childNode.setSynonymToBeUsed(synonymUsed);
	}
	
	/**
	 * Moves a taxon node to a new parent. Descendents of the node are moved as well 
	 * 
	 * @param childNode the taxon node to be moved to the new parent
	 * @return the child node in the state of having a new parent
	 */
	public TaxonNode addChildNode(TaxonNode childNode, ReferenceBase reference, String microReference, Synonym synonymToBeUsed){
		// check if this node is a descendant of the childNode 
		if(childNode.getParent() != this && childNode.isAscendant(this)){
			throw new IllegalAncestryException("New parent node is a descendant of the node to be moved.");
		}
		
		childNode.setParent(this);
		childNode.setTaxonomicView(this.getTaxonomicTree());
		childNodes.add(childNode);
		this.countChildren++;
		childNode.setReferenceForParentChildRelation(reference);
		childNode.setMicroReferenceForParentChildRelation(microReference);
		childNode.setSynonymToBeUsed(synonymToBeUsed);
		
		for(TaxonNode grandChildNode : childNode.getChildNodes()){
			childNode.addChildNode(grandChildNode, childNode.getReferenceForParentChildRelation(), childNode.getMicroReferenceForParentChildRelation(), childNode.getSynonymToBeUsed());
		}
		
		return childNode;
	}
	
	/**
	 * This removes recursively all child nodes from this node and from this taxonomic view.
	 * TODO remove orphan nodes completely 
	 * 
	 * @param node
	 * @return
	 * @deprecated use removeChildNode() instead
	 */
	@Deprecated
	public boolean removeChild(TaxonNode node){
		return removeChildNode(node);
	}	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.taxon.ITreeNode#removeChildNode(eu.etaxonomy.cdm.model.taxon.TaxonNode)
	 */
	public boolean removeChildNode(TaxonNode node) {
		boolean result = false;
		if (node != null){
			// two iterations because of ConcurrentModificationErrors
            Set<TaxonNode> removeNodes = new HashSet<TaxonNode>(); 
            for (TaxonNode grandChildNode : node.getChildNodes()) { 
                    removeNodes.add(grandChildNode); 
            } 
            for (TaxonNode childNode : removeNodes) { 
                    childNode.removeChildNode(node); 
            } 
            
			result = childNodes.remove(node);
			this.countChildren--;
			if (this.countChildren < 0){
				throw new IllegalStateException("children count must not be negative ");
			}
			node.getTaxon().removeTaxonNode(node);
			node.setParent(null);
			node.setTaxonomicView(null);
			node.setTaxon(null);
		}
		return result;
	}
	
	/**
	 * Remove this taxonNode From its taxonomic view.
	 * 
	 * @return true on success
	 * @deprecated use removeChildNode() instead as it is mandatory by the interface ITreeNode
	 */
	@Deprecated
	public boolean remove(){
		if(isRootNode()){
			return taxonomicTree.removeRoot(this);
		}else{
			return getParent().removeChild(this);
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
	public TaxonNode getParent() {
		return parent;
	}
	protected void setParent(TaxonNode parent) {
		this.parent = parent;
	}
	public TaxonomicTree getTaxonomicTree() {
		return taxonomicTree;
	}
	//invisible part of the bidirectional relationship, for public use TaxonomicView.addRoot() or TaxonNode.addChild()
	protected void setTaxonomicView(TaxonomicTree taxonomicTree) {
		this.taxonomicTree = taxonomicTree;
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
	 * Returns a 
	 * 
	 * @return
	 */
	protected Set<TaxonNode> getAscendants(){
		Set<TaxonNode> nodeSet = new HashSet<TaxonNode>();
		
		
		nodeSet.add(this);
		
		if(this.getParent() != null){
			nodeSet.addAll(this.getParent().getAscendants());
		}
		
		return nodeSet;
	}
	
//	protected void setChildNodes(List<TaxonNode> childNodes) {
//		this.childNodes = childNodes;
//	}
	public ReferenceBase getReferenceForParentChildRelation() {
		return referenceForParentChildRelation;
	}
	public void setReferenceForParentChildRelation(
			ReferenceBase referenceForParentChildRelation) {
		this.referenceForParentChildRelation = referenceForParentChildRelation;
	}
	public String getMicroReferenceForParentChildRelation() {
		return microReferenceForParentChildRelation;
	}
	public void setMicroReferenceForParentChildRelation(
			String microReferenceForParentChildRelation) {
		this.microReferenceForParentChildRelation = microReferenceForParentChildRelation;
	}
	public int getCountChildren() {
		return countChildren;
	}
	public void setCountChildren(int countChildren) {
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
	 * Whether this TaxonNode is a root node
	 * @return
	 */
	@Transient
	public boolean isRootNode(){
		return parent == null;
	}
	
	/**
	 * Whether this TaxonNode is a descendant of the given TaxonNode
	 * 
	 * Caution: use this method with care on big branches. -> performance and memory hungry
	 * 
	 * Protip: Try solving your problem with the isAscendant method which traverses the tree in the 
	 * other direction (up). This will always result in rather small set of recursive parents beeing
	 * generated
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
	public boolean isAscendant(TaxonNode possibleChild){
		return possibleChild.getAscendants().contains(this);
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
}
