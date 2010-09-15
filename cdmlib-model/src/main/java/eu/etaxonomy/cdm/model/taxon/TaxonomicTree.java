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
import javax.persistence.JoinColumn;
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
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

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
@Indexed(index = "eu.etaxonomy.cdm.model.taxon.TaxonomicTree")
public class TaxonomicTree extends IdentifiableEntity implements IReferencedEntity, ITreeNode{
	private static final long serialVersionUID = -753804821474209635L;
	private static final Logger logger = Logger.getLogger(TaxonomicTree.class);
	
	@XmlElement(name = "Name")
	@OneToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	@JoinColumn(name = "name_id", referencedColumnName = "id")
	@IndexedEmbedded
	private LanguageString name;
	
	@XmlElementWrapper(name = "rootNodes")
	@XmlElement(name = "rootNode")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	//TODO 
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
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.taxon.ITreeNode#addChildNode(eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.model.reference.ReferenceBase, java.lang.String, eu.etaxonomy.cdm.model.taxon.Synonym)
	 */
	public TaxonNode addChildNode(TaxonNode childNode, ReferenceBase citation,
			String microCitation, Synonym synonymToBeUsed) {
		
		childNode.setParentTreeNode(this);
		
		childNode.setReference(citation);
		childNode.setMicroReference(microCitation);
		childNode.setSynonymToBeUsed(synonymToBeUsed);
		
		return childNode;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.taxon.ITreeNode#addChildTaxon(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.reference.ReferenceBase, java.lang.String, eu.etaxonomy.cdm.model.taxon.Synonym)
	 */
	public TaxonNode addChildTaxon(Taxon taxon, ReferenceBase citation,
			String microCitation, Synonym synonymToBeUsed) {
		return addChildNode(new TaxonNode(taxon), citation, microCitation, synonymToBeUsed);
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.taxon.ITreeNode#removeChildNode(eu.etaxonomy.cdm.model.taxon.TaxonNode)
	 */
	public boolean deleteChildNode(TaxonNode node) {
		boolean result = removeChildNode(node);
		
		node.getTaxon().removeTaxonNode(node);
		node.setTaxon(null);	
		
		ArrayList<TaxonNode> childNodes = new ArrayList<TaxonNode>(node.getChildNodes()); 
		for (TaxonNode childNode : childNodes){
			node.deleteChildNode(childNode);
		}
		return result;
	}
	
	/**
	 * 
	 * @param node
	 * @return
	 */
	protected boolean removeChildNode(TaxonNode node){
		boolean result = false;
		
		if(!rootNodes.contains(node)){
			throw new IllegalArgumentException("TaxonNode is a not a root node of this taxonomic tree");
		}
		
		result = rootNodes.remove(node);

		node.setParent(null);
		node.setTaxonomicTree(null);
		
		return result;
	}
	
	/**
	 * Appends an existing topmost node to another node of this tree. The existing topmost node becomes 
	 * an ordinary node.
	 * @param topmostNode
	 * @param otherNode
	 * @param ref
	 * @param microReference
	 * @throws IllegalArgumentException
	 */
	public void makeTopmostNodeChildOfOtherNode(TaxonNode topmostNode, TaxonNode otherNode, ReferenceBase ref, String microReference)
				throws IllegalArgumentException{
		if (otherNode == null){
			throw new NullPointerException("other node must not be null");
		}
		if (! getChildNodes().contains(topmostNode)){
			throw new IllegalArgumentException("root node to be added as child must already be root node within this tree");
		}
		if (otherNode.getTaxonomicTree() == null || ! otherNode.getTaxonomicTree().equals(this)){
			throw new IllegalArgumentException("other node must already be node within this tree");
		}
		if (otherNode.equals(topmostNode)){
			throw new IllegalArgumentException("root node and other node must not be the same");
		}
		otherNode.addChildNode(topmostNode, ref, microReference, null);
		//getRootNodes().remove(root);
	}
	

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
	 * Checks if the given taxon is one of the topmost taxa in <b>this</b> tree.
	 * @param taxon
	 * @return
	 */
	public boolean isTopmostInTree(Taxon taxon){
		return (getTopmostNode(taxon) != null);
	}	
	
	
	/**
	 * Checks if the taxon is a direct child of <b>this</b> tree and returns the according node if true.
	 * Returns null otherwise.
	 * @param taxon
	 * @return
	 */
	public TaxonNode getTopmostNode(Taxon taxon){
		if (taxon == null){
			return null;
		}
		for (TaxonNode taxonNode: taxon.getTaxonNodes()){
			if (taxonNode.getTaxonomicTree().equals(this)){
				if (this.getChildNodes().contains(taxonNode)){
					if (taxonNode.getParentTreeNode() instanceof TaxonNode){
						logger.warn("A topmost node should have a TaxonomicTree as parent but actually has a TaxonNode parent");
					}
					return taxonNode;
				}
			}
		}
		return null;
	}

	private boolean handleCitationOverwrite(TaxonNode childNode, ReferenceBase citation, String microCitation){
		if (citation != null){
			if (childNode.getReference() != null && ! childNode.getReference().equals(citation)){
				logger.warn("ReferenceForParentChildRelation will be overwritten");
			}
			childNode.setReference(citation);
		}
		if (microCitation != null){
			if (childNode.getMicroReference() != null && ! childNode.getMicroReference().equals(microCitation)){
				logger.warn("MicroReferenceForParentChildRelation will be overwritten");
			}
			childNode.setMicroReference(microCitation);
		}
		return true;
	}
	
	/**
	 * Relates two taxa as parent-child nodes within a taxonomic tree. <BR>
	 * If the taxa are not yet part of the tree they are added to it.<Br>
	 * If the child taxon is a topmost node still it is added as child and deleted from the rootNode set.<Br>
	 * If the child is a child of another parent already an IllegalStateException is thrown because a child can have only 
	 * one parent. <Br>
	 * If the parent-child relationship between these two taxa already exists nothing is changed. Only 
	 * citation and microcitation are overwritten by the new values if these values are not null.
	 *  
	 * @param parent
	 * @param child
	 * @param citation
	 * @param microCitation
	 * @return the childNode
	 * @throws IllegalStateException If the child is a child of another parent already
	 */
	public TaxonNode addParentChild (Taxon parent, Taxon child, ReferenceBase citation, String microCitation)
			throws IllegalStateException{
		try {
			if (parent == null || child == null){
				logger.warn("Child or parent taxon is null.");
				return null;
			}
			if (parent == child){
				logger.warn("A taxon should never be its own child. Chilc not added");
				return null;
			}
			TaxonNode parentNode = this.getNode(parent);
			TaxonNode childNode = this.getNode(child);
			
			//if child exists in tree and has a parent 
			//no multiple parents are allowed in the tree
			if (childNode != null && ! childNode.isTopmostNode()){
				//...different to the parent taxon  throw exception
				if ((childNode.getParentTreeNode() instanceof TaxonNode) && !((TaxonNode)childNode.getParent()).getTaxon().equals(parent) ){
					throw new IllegalStateException("The child taxon is already part of the tree but has an other parent taxon than the one than the parent to be added. Child: " + child.toString() + ", new parent:" + parent.toString() + ", old parent: " + ((TaxonNode) childNode.getParent()).getTaxon().toString()) ;
				//... same as the parent taxon do nothing but overwriting citation and microCitation
				}else{
					handleCitationOverwrite(childNode, citation, microCitation);
					return childNode;
				}
			}
			
			//add parent node if not exist
			if (parentNode == null){
				parentNode = this.addChildTaxon(parent, null, null, null);
			}
			
			//add child if not exists
			if (childNode == null){
				childNode = parentNode.addChildTaxon(child, citation, microCitation, null);
			}else{
				//child is still topmost node
				//TODO test if child is topmostNode otherwise throw IllegalStateException
				if (! this.isTopmostInTree(child)){
					throw new IllegalStateException("Child is not a topmost node but must be");
				}
				this.makeTopmostNodeChildOfOtherNode(childNode, parentNode, citation, microCitation);
			}
			return childNode;
		} catch (IllegalStateException e) {
			throw e;
		} catch (RuntimeException e){
			throw e;
		}
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
	public Set<TaxonNode> getAllNodes() {
		Set<TaxonNode> allNodes = new HashSet<TaxonNode>();
		
		for(TaxonNode rootNode : getChildNodes()){
			allNodes.addAll(rootNode.getDescendants());
		}
		
		return allNodes;
	}	
	
	public Set<TaxonNode> getChildNodes() {
		return rootNodes;
	}

	private void setRootNodes(Set<TaxonNode> rootNodes) {
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

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.model.taxon.ITreeNode#hasChildNodes()
	 */
	public boolean hasChildNodes() {
		return getChildNodes().size() > 0;
	}
}
