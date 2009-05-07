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
@XmlType(name = "TaxonomicView", propOrder = {
    "name",
    "allNodes",
    "rootNodes",
    "reference",
    "microReference"
})
@XmlRootElement(name = "TaxonomicView")
@Entity
@Audited
public class TaxonomicView extends IdentifiableEntity implements IReferencedEntity{
	private static final long serialVersionUID = -753804821474209635L;
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(TaxonomicView.class);
	
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
    @OneToMany(mappedBy="taxonomicView", fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE})
	private Set<TaxonNode> allNodes = new HashSet<TaxonNode>();

	@XmlElementWrapper(name = "rootNodes")
	@XmlElement(name = "rootNode")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE})
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
	
	
	public static TaxonomicView NewInstance(String name){
		return NewInstance(name, Language.DEFAULT());
	}
	
	public static TaxonomicView NewInstance(String name, Language language){
		TaxonomicView result =  new TaxonomicView();
		LanguageString langName = LanguageString.NewInstance(name, language);
		result.setName(langName);
		return result;
	}
	
	private TaxonomicView(){
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
	

	public boolean isTaxonInView(Taxon taxon){
		if (taxon == null){
			return false;
		}
		for (TaxonNode taxonNode: taxon.getTaxonNodes()){
			if (taxonNode.getTaxonomicView().equals(this)){
				return true;
			}
		}
		return false;
	}
	
	

	
	
//	public TaxonRelationship addChild(Taxon parent, Taxon child){
//		
//		parent.addTaxonomicChild(child, null, null);
//	}
	
	
	

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
