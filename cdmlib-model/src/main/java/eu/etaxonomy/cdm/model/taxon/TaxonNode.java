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
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
    "taxonomicView",
    "childNodes",
    "referenceForParentChildRelation",
    "microReferenceForParentChildRelation",
    "countChildren",
    "synonymToBeUsed"
})
@XmlRootElement(name = "TaxonNode")
@Entity
@Audited
public class TaxonNode  extends AnnotatableEntity {
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
	
	
	@XmlElement(name = "taxonomicView")
	@XmlIDREF
	@XmlSchemaType(name = "IDREF")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({CascadeType.SAVE_UPDATE})
	private TaxonomicView taxonomicView;
	
	@XmlElementWrapper(name = "childNodes")
	@XmlElement(name = "childNode")
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    @OneToMany(mappedBy="parent", fetch=FetchType.LAZY)
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.MERGE})
	private List<TaxonNode> childNodes = new ArrayList<TaxonNode>();
	
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
	
	//to create nodes either use TaxonomicView.addRoot() or TaxonNode.addChild();
	protected TaxonNode (Taxon taxon, TaxonomicView taxonomicView){
		setTaxon(taxon);
		setTaxonomicView(taxonomicView);
		taxonomicView.addNode(this);
	}

	
	
//************************ METHODS **************************/
	
	public TaxonNode addChild(Taxon taxon){
		return addChild(taxon, null, null, null);
	}
	
	public TaxonNode addChild(Taxon taxon, ReferenceBase ref, String microReference){
		return addChild(taxon, ref, microReference, null);
	}	
	
	public TaxonNode addChild(Taxon taxon, ReferenceBase ref, String microReference, Synonym synonymUsed){
		if (this.getTaxonomicView().isTaxonInView(taxon)){
			throw new IllegalArgumentException("Taxon may not be twice in a taxonomic view");
		}
		TaxonNode childNode = new TaxonNode(taxon, this.getTaxonomicView());
		this.getTaxonomicView().addNode(childNode);
		addChildNote(childNode, ref, microReference, synonymUsed);
		return childNode;
	}
	
	protected void addChildNote(TaxonNode childNode, ReferenceBase ref, String microReference, Synonym synonymUsed){
		if (! childNode.getTaxonomicView().equals(this.getTaxonomicView())){
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
	 * This removes recursivly all child from this node and from this taxonomic view.
	 * TODO remove orphan nodes completely 
	 * 
	 * @param node
	 * @return
	 */
	public boolean removeChild(TaxonNode node){
		boolean result = false;
		if (node != null){
			for (TaxonNode grandChildNode : node.getChildNodes()){
				node.removeChild(grandChildNode);
			}
			result = childNodes.remove(node);
			node.getTaxonomicView().removeNode(node);
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
	public TaxonomicView getTaxonomicView() {
		return taxonomicView;
	}
	//invisible part of the bidirectional relationship, for public use TaxonomicView.addRoot() or TaxonNode.addChild()
	protected void setTaxonomicView(TaxonomicView taxonomicView) {
		this.taxonomicView = taxonomicView;
	}
	public List<TaxonNode> getChildNodes() {
		return childNodes;
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
	

}
