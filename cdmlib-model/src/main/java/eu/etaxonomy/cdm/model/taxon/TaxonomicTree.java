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
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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

import eu.etaxonomy.cdm.model.common.IReferencedEntity;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.mueller
 * @created 31.03.2009
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonomicTree", propOrder = {
    "name",
    "rootNodes",
    "reference",
    "microReference"
})
@XmlRootElement(name = "TaxonomicTree")
@Entity
@Audited
public class TaxonomicTree extends IdentifiableEntity implements IReferencedEntity{
	private static final long serialVersionUID = -753804821474209635L;
	private static final Logger logger = Logger.getLogger(TaxonomicTree.class);
	
	@XmlElement(name = "name")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@OneToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	private LanguageString name;
	
//	@XmlElementWrapper(name = "allNodes")
//	@XmlElement(name = "taxonNode")
//    @XmlIDREF
//    @XmlSchemaType(name = "IDREF")
//    @OneToMany(mappedBy="taxonomicTree", fetch=FetchType.LAZY)
//    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
//    @Deprecated // FIXME remove this. A set containing all nodes of the tree is a major performance killer, especially when it is not really in use
//	private Set<TaxonNode> allNodes = new HashSet<TaxonNode>();

	@XmlElementWrapper(name = "rootNodes")
	@XmlElement(name = "rootNode")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	private Set<TaxonNode> rootNodes = new HashSet<TaxonNode>();

	@XmlElement(name = "reference")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	private ReferenceBase reference;
	
	@XmlElement(name = "microReference")
	private String microReference;
	
//	/**
//	 * If this taxonomic view is an alternative view for a subtree in an other view(parent view),
//	 * the alternativeViewRoot is the connection node from this view to the parent view.
//	 * It replaces another node in the parent view.
//	 */
//	private AlternativeViewRoot alternativeViewRoot;
	
	
	public static TaxonomicTree NewInstance(String name){
		return NewInstance(name, null, Language.DEFAULT());
	}
	
	public static TaxonomicTree NewInstance(String name, Language language){
		return NewInstance(name, null, language);
	}
	
	public static TaxonomicTree NewInstance(String name, ReferenceBase reference){
		return NewInstance(name, reference, Language.DEFAULT());
	}
	
	public static TaxonomicTree NewInstance(String name, ReferenceBase reference, Language language){
		return new TaxonomicTree(name, reference, language);
	}
	
	protected TaxonomicTree(String name, ReferenceBase reference, Language language){
		this();
		LanguageString langName = LanguageString.NewInstance(name, language);
		setName(langName);
		setReference(reference);
	}
	
	protected TaxonomicTree(){
		super();
	}
	
	
	
	/**
	 * Adds a taxon to the taxonomic tree and makes it one of the root nodes.
	 * @param taxon
	 * @param synonymUsed
	 * @return
	 */
	public TaxonNode addRoot(Taxon taxon, Synonym synonymUsed, ReferenceBase reference){
		TaxonNode newRoot = new TaxonNode(taxon, this);
		rootNodes.add(newRoot);
		newRoot.setParent(null);
		newRoot.setTaxonomicView(this);
		newRoot.setTaxon(taxon);
		newRoot.setReferenceForParentChildRelation(reference);
		newRoot.setSynonymToBeUsed(synonymUsed);
		return newRoot;
	}
	
	public boolean removeRoot(TaxonNode node){
		boolean result = false;
		if(node.isRootNode()){

			for (TaxonNode childNode : node.getChildNodes()){
				node.removeChild(childNode);
			}
			result = rootNodes.remove(node);

			node.getTaxon().removeTaxonNode(node);
			node.setParent(null);
			node.setTaxonomicView(null);
			node.setTaxon(null);			
		}
		return result;
	}
	
	/**
	 * Appends an existing root node to another node of this tree. The existing root node becomes 
	 * an ordinary node.
	 * @param root
	 * @param otherNode
	 * @param ref
	 * @param microReference
	 * @throws IllegalArgumentException
	 */
	public void makeRootChildOfOtherNode(TaxonNode root, TaxonNode otherNode, ReferenceBase ref, String microReference)
				throws IllegalArgumentException{
		if (otherNode == null){
			throw new NullPointerException("other node must not be null");
		}
		if (! getRootNodes().contains(root)){
			throw new IllegalArgumentException("root node to be added as child must already be root node within this tree");
		}
		if (otherNode.getTaxonomicTree() == null || ! otherNode.getTaxonomicTree().equals(this)){
			throw new IllegalArgumentException("other node must already be node within this tree");
		}
		if (otherNode.equals(root)){
			throw new IllegalArgumentException("root node and other node must not be the same");
		}
		otherNode.addChildNote(root, ref, microReference, null);
		getRootNodes().remove(root);
	}
	
//	public void makeThisNodePartOfOtherView(TaxonNode oldRoot, TaxonNode replacedNodeInOtherView, ReferenceBase reference, String microReference){
//		AlternativeViewRoot newRoot = new AlternativeViewRoot(oldRoot, replacedNodeInOtherView, reference, microReference);
//	}
	

	/**
	 * Checks if the given taxon is part of <b>this</b> tree.
	 * @param taxon
	 * @return
	 */
	public boolean isTaxonInTree(Taxon taxon){
		return (getNode(taxon) != null);
	}
	
	/**
	 * Checks if the given taxon is part of <b>this</b> tree. If so the according TaxonNode is returned.
	 * Otherwise null is returned.
	 * @param taxon
	 * @return
	 */
	public TaxonNode getNode(Taxon taxon){
		if (taxon == null){
			return null;
		}
		for (TaxonNode taxonNode: taxon.getTaxonNodes()){
			if (taxonNode.getTaxonomicTree().equals(this)){
				return taxonNode;
			}
		}
		return null;
	}
	
	/**
	 * Checks if the given taxon is one of the root taxa in <b>this</b> tree.
	 * @param taxon
	 * @return
	 */
	public boolean isRootInTree(Taxon taxon){
		return (getRootNode(taxon) != null);
	}
	
	/**
	 * Checks if the taxon is a root taxon in <b>this</b> tree and returns the according node if true.
	 * Returns null otherwise.
	 * @param taxon
	 * @return
	 */
	public TaxonNode getRootNode(Taxon taxon){
		if (taxon == null){
			return null;
		}
		for (TaxonNode taxonNode: taxon.getTaxonNodes()){
			if (taxonNode.getTaxonomicTree().equals(this)){
				if (this.getRootNodes().contains(taxonNode)){
					if (taxonNode.getParent() != null){
						logger.warn("A root node should not have parent");
					}
					return taxonNode;
				}
			}
		}
		return null;
	}

	private boolean handleCitationOverwrite(TaxonNode childNode, ReferenceBase citation, String microCitation){
		if (citation != null){
			if (childNode.getReferenceForParentChildRelation() != null && ! childNode.getReferenceForParentChildRelation().equals(citation)){
				logger.warn("ReferenceForParentChildRelation will be overwritten");
			}
			childNode.setReferenceForParentChildRelation(citation);
		}
		if (microCitation != null){
			if (childNode.getMicroReferenceForParentChildRelation() != null && ! childNode.getMicroReferenceForParentChildRelation().equals(microCitation)){
				logger.warn("MicroReferenceForParentChildRelation will be overwritten");
			}
			childNode.setMicroReferenceForParentChildRelation(microCitation);
		}
		return true;
	}
	
	
	/**
	 * Relates two taxa as parent-child nodes within a taxonomic tree. <BR>
	 * If the taxa are not yet part of the tree they are added to it.<Br>
	 * If the child taxon is a root still it is added as child and deleted from the rootNode set.<Br>
	 * If the child is a child of another parent already an IllegalStateException is thrown because a child can have only 
	 * one parent. <Br>
	 * If the parent-child relationship between these two taxa already exists nothing is changed. Only 
	 * citation and microcitation are overwritten by the new values if these values are not null.
	 * @param parent
	 * @param child
	 * @param citation
	 * @param microCitation
	 * @return
	 * @throws IllegalStateException If the child is a child of another parent already
	 */
	public boolean addParentChild (Taxon parent, Taxon child, ReferenceBase citation, String microCitation)
			throws IllegalStateException{
		try {
			TaxonNode parentNode = this.getNode(parent);
			TaxonNode childNode = this.getNode(child);
			
			//if child exists in tree and has a parent 
			//no multiple parents are allowed in the tree
			if (childNode != null && childNode.getParent() != null){
				//...different to the parent taxon  throw exception
				if (! childNode.getParent().getTaxon().equals(parent) ){
					throw new IllegalStateException("The child taxon is already part of the tree but has an other parent taxon than the one than the parent to be added. Child: " + child.toString() + ", new parent:" + parent.toString() + ", old parent: " + childNode.getParent().getTaxon().toString()) ;
				//... same as the parent taxon do nothing but overwriting citation and microCitation
				}else{
					handleCitationOverwrite(childNode, citation, microCitation);
					return true;
				}
			}
			
			//add parent node if not exist
			if (parentNode == null){
				parentNode = this.addRoot(parent, null, null);
			}
			
			//add child if not exists
			if (childNode == null){
				parentNode.addChild(child, citation, microCitation);
			}else{
				//child is still root
				//TODO test if child is rootNode otherwise thrwo IllegalStateException
				if (! this.isRootInTree(child)){
					throw new IllegalStateException("Child is not a root but must be");
				}
				this.makeRootChildOfOtherNode(childNode, parentNode, citation, microCitation);
			}
		} catch (IllegalStateException e) {
			throw e;
		} catch (RuntimeException e){
			throw e;
		}
		return true;
	}
	
	
	@Transient
	public ReferenceBase getCitation() {
		return reference;
	}
	
	public LanguageString getName() {
		return name;
	}

	public void setName(LanguageString name) {
		this.name = name;
	}

	/**
	 * Returns a set containing all nodes in this taxonomic tree.
	 * 
	 * Caution: Use this method with care. It can be very time and resource consuming and might
	 * run into OutOfMemoryExceptions for big trees. 
	 * 
	 * @return
	 */
	@Transient
	@Deprecated
	public Set<TaxonNode> getAllNodes() {
		Set<TaxonNode> allNodes = new HashSet<TaxonNode>();
		
		for(TaxonNode rootNode : getRootNodes()){
			allNodes.addAll(rootNode.getDescendants());
		}
		
		return allNodes;
	}	
	
	public Set<TaxonNode> getRootNodes() {
		return rootNodes;
	}

	public void setRootNodes(Set<TaxonNode> rootNodes) {
		this.rootNodes = rootNodes;
	}

	public ReferenceBase getReference() {
		return reference;
	}

	public void setReference(ReferenceBase reference) {
		this.reference = reference;
	}
	

	/**
	 * @return the microReference
	 */
	public String getMicroReference() {
		return microReference;
	}

	/**
	 * @param microReference the microReference to set
	 */
	public void setMicroReference(String microReference) {
		this.microReference = microReference;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.common.IdentifiableEntity#generateTitle()
	 */
	@Override
	public String generateTitle() {
		return name.getText();
	}

	public int compareTo(Object o) {
		return 0;
	}
	
	/**
	 * Returns all TaxonNodes of the tree for a given Rank.
	 * If a branch does not contain a TaxonNode with a TaxonName at the given
	 * Rank the node associated with the next lower Rank is taken as root node.
	 * If the <code>rank</code> is null the absolute root nodes will be returned.
	 * 
	 * @param rank may be null
	 * @return
	 */
	public List<TaxonNode> getRankSpecificRootNodes(Rank rank) {
		List<TaxonNode> baseNodes = new ArrayList<TaxonNode>();
		if(rank != null){
			findNodesForRank(rank, getRootNodes(), baseNodes);
		} else {
			baseNodes.addAll(getRootNodes());
		}
		return baseNodes;
	}

	/**
	 * Walks the tree returning all nodes of exactly the given <code>rank</code>
	 * or the next lower rank if an exact match by rank is not possible.
	 * 
	 * @param baseRank
	 *            the rank to return nodes for
	 * @param nodeSet
	 *            the set of nodes to search in
	 * @param rootNodes
	 *            a List to put the found nodes in
	 * @return the <code>rootNodes</code> List given as parameter
	 */
	private List<TaxonNode> findNodesForRank(Rank baseRank, Set<TaxonNode> nodeSet, List<TaxonNode> rootNodes) {
		for(TaxonNode node : nodeSet){
			Rank thisRank = node.getTaxon().getName().getRank();
			if(thisRank.isHigher(baseRank)){
				// if the current rank still is higher than the given baseRank iterate deeper into tree
				findNodesForRank(baseRank, node.getChildNodes(), rootNodes);
			} else {
				// gotsha! It is a base node of this level
				rootNodes.add(node);
			}
		}
		return rootNodes;
	}

}
