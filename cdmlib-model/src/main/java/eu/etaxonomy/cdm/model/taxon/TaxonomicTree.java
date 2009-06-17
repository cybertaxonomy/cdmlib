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
import javax.persistence.OneToOne;
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
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.mueller
 * @created 31.03.2009
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonomicTree", propOrder = {
    "name",
    "allNodes",
    "rootNodes",
    "reference",
    "microReference"
})
@XmlRootElement(name = "TaxonomicTree")
@Entity
@Audited
public class TaxonomicTree extends IdentifiableEntity implements IReferencedEntity{
	private static final long serialVersionUID = -753804821474209635L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonomicTree.class);
	
	@XmlElement(name = "name")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@OneToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	private LanguageString name;
	
	@XmlElementWrapper(name = "allNodes")
	@XmlElement(name = "taxonNode")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(mappedBy="taxonomicTree", fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	private Set<TaxonNode> allNodes = new HashSet<TaxonNode>();

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
		return NewInstance(name, Language.DEFAULT());
	}
	
	public static TaxonomicTree NewInstance(String name, Language language){
		TaxonomicTree result =  new TaxonomicTree();
		LanguageString langName = LanguageString.NewInstance(name, language);
		result.setName(langName);
		return result;
	}
	
	private TaxonomicTree(){
		super();
	}
	
	
	
	public TaxonNode addRoot(Taxon taxon, Synonym synonymUsed){
		TaxonNode newRoot = new TaxonNode(taxon, this);
		rootNodes.add(newRoot);
		newRoot.setParent(null);
		newRoot.setTaxonomicView(this);
		newRoot.setTaxon(taxon);
//		newRoot.setReferenceForParentChildRelation(ref); //ref not needed for root !!
//		newRoot.setMicroReferenceForParentChildRelation(microReference);
		newRoot.setSynonymToBeUsed(synonymUsed);
		return newRoot;
	}
	
	public void makeRootChildOfOtherNode(TaxonNode root, TaxonNode otherNode, ReferenceBase ref, String microReference)
				throws IllegalArgumentException{
		if (otherNode == null){
			throw new NullPointerException("other node must not be null");
		}
		if (! getRootNodes().contains(root)){
			throw new IllegalArgumentException("root node to be added as child must already be root node within this view");
		}
		if (! getAllNodes().contains(otherNode)){
			throw new IllegalArgumentException("root node to be added as child must already be root node within this view");
		}
		if (otherNode.equals(root)){
			throw new IllegalArgumentException("root node and other node must not be the same");
		}
		otherNode.addChildNote(root, ref, microReference, null);
		getRootNodes().remove(root);
		getAllNodes().add(root);
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
				return taxonNode;
			}
		}
		return null;
	}

	/**
	 * Relates two taxa as parent-child nodes within a taxonomic tree. If the taxa are not yet 
	 * part of the tree they are added to it.
	 * If they child taxon is a root still it is added as child and deleted from the rootNode set.
	 * If the child is a child already an IllegalStateException is thrown because a child can have only 
	 * one parent.
	 * @param parent
	 * @param child
	 * @param citation
	 * @param microCitation
	 * @return
	 * @throws IllegalStateException
	 */
	public boolean addParentChild (Taxon parent, Taxon child, ReferenceBase citation, String microCitation)
			throws IllegalStateException{
		try {
			TaxonNode parentNode = this.getNode(parent);
			TaxonNode childNode = this.getNode(child);
			
			//if child exists in tree and has a parent different to the parent taxon  throw exception
			//no multiple parents are allowed in the tree
			if (childNode != null && childNode.getParent() != null && ! childNode.getParent().getTaxon().equals(parent) ){
				throw new IllegalStateException("The child taxon is already part of the tree but has an other parent taxon than the one than the parent to be added. Child: " + child.toString() + ", new parent:" + parent.toString() + ", old parent: " + childNode.getParent().getTaxon().toString()) ;
			}
			
			//add parent node if not exist
			if (parentNode == null){
				//New Root
				parentNode = this.addRoot(parent, null);
			}
			
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
	
	

	public ReferenceBase getCitation() {
		return reference;
	}
	
	public LanguageString getName() {
		return name;
	}

	public void setName(LanguageString name) {
		this.name = name;
	}

	public Set<TaxonNode> getAllNodes() {
		return allNodes;
	}
	//for bidirectional use only
	protected boolean addNode(TaxonNode taxonNode){
		return allNodes.add(taxonNode);
	}
	//for bidirectional use only
	protected boolean removeNode(TaxonNode taxonNode){
		return allNodes.remove(taxonNode);
	}
	public void setAllNodes(Set<TaxonNode> allNodes) {
		this.allNodes = allNodes;
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

}
