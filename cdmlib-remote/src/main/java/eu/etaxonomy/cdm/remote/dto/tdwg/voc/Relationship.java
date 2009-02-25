package eu.etaxonomy.cdm.remote.dto.tdwg.voc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.remote.dto.tdwg.BaseThing;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Relationship", propOrder = {
	    "fromTaxon",
	    "relationshipCategory",
	    "toTaxon"
})
@XmlRootElement(namespace = "http://rs.tdwg.org/ontology/voc/TaxonConcept#")
public class Relationship extends BaseThing {
	
	@XmlElement(namespace = "http://rs.tdwg.org/ontology/voc/TaxonConcept#")
	private FromTaxon fromTaxon;
	
	@XmlElement(namespace = "http://rs.tdwg.org/ontology/voc/TaxonConcept#")
	private RelationshipCategory relationshipCategory;
	
	@XmlElement(namespace = "http://rs.tdwg.org/ontology/voc/TaxonConcept#")
	private ToTaxon toTaxon;
	
	public TaxonConcept getFromTaxon() {
		return fromTaxon != null ? fromTaxon.getTaxonConcept() : null;
	}

	public void setFromTaxon(TaxonConcept fromTaxon) {
		this.fromTaxon = new FromTaxon(fromTaxon);
	}

	public TaxonRelationshipTerm getRelationshipCategory() {
		return relationshipCategory != null ? relationshipCategory.getTaxonRelationshipTerm() : null;
	}

	public void setRelationshipCategory(TaxonRelationshipTerm relationshipCategory) {
		this.relationshipCategory = new RelationshipCategory(relationshipCategory,false);
	}
	
	public TaxonRelationshipTerm getRelationshipCategoryRelation() {
		return relationshipCategory != null ? relationshipCategory.getTaxonRelationshipTerm() : null;
	}

	public void setRelationshipCategoryRelation(TaxonRelationshipTerm relationshipCategory) {
		this.relationshipCategory = new RelationshipCategory(relationshipCategory,true);
	}

	public TaxonConcept getToTaxon() {
		return toTaxon != null ? toTaxon.getTaxonConcept() : null;
	}

	public void setToTaxon(TaxonConcept toTaxon) {
		this.toTaxon = new ToTaxon(toTaxon);
	}

	@XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "taxonConcept"
    })
	public static class FromTaxon extends LinkType {
		
		@XmlElement(name = "TaxonConcept", namespace = "http://rs.tdwg.org/ontology/voc/TaxonConcept#")
		private TaxonConcept taxonConcept;

		protected FromTaxon() {}
		
		protected FromTaxon(TaxonConcept taxonConcept) {
	        if(taxonConcept != null && taxonConcept.getIdentifier() != null) {
			    	this.setResource(taxonConcept.getIdentifier());
	        }
		}
		
		protected TaxonConcept getTaxonConcept() {
			return taxonConcept;
		}

		protected void setTaxonConcept(TaxonConcept taxonConcept) {
			this.taxonConcept = taxonConcept;
		}		
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "taxonConcept"
    })
	public static class ToTaxon extends LinkType {
		
		@XmlElement(name = "TaxonConcept", namespace = "http://rs.tdwg.org/ontology/voc/TaxonConcept#")
		private TaxonConcept taxonConcept;
		
		protected ToTaxon() {}
		
		protected ToTaxon(TaxonConcept taxonConcept) {
			if(taxonConcept != null && taxonConcept.getIdentifier() != null) {
			    this.setResource(taxonConcept.getIdentifier());
			} 
		}

		protected TaxonConcept getTaxonConcept() {
			return taxonConcept;
		}

		protected void setTaxonConcept(TaxonConcept taxonConcept) {
			this.taxonConcept = taxonConcept;
		}
		
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "taxonRelationshipTerm"
    })
	public static class RelationshipCategory extends LinkType {
		
		@XmlElement(name = "TaxonRelationshipTerm", namespace = "http://rs.tdwg.org/ontology/voc/TaxonConcept#")
		private TaxonRelationshipTerm taxonRelationshipTerm;
		
		protected RelationshipCategory() {}
		
		protected RelationshipCategory(TaxonRelationshipTerm taxonRelationshipTerm, boolean useRelation) {
			if(useRelation) {
			    if(taxonRelationshipTerm != null) {
			    	if(taxonRelationshipTerm.getIdentifier() != null)
			    	this.setResource(taxonRelationshipTerm.getIdentifier());
			    }  else {
			    	this.taxonRelationshipTerm = taxonRelationshipTerm;
			    }
			} else {
				this.taxonRelationshipTerm = taxonRelationshipTerm;
			}
		}

		protected TaxonRelationshipTerm getTaxonRelationshipTerm() {
			return taxonRelationshipTerm;
		}

		protected void setTaxonRelationshipTerm(TaxonRelationshipTerm taxonRelationshipTerm) {
			this.taxonRelationshipTerm = taxonRelationshipTerm;
		}

		protected RelationshipCategory(TaxonRelationshipTerm taxonRelationshipTerm) {
			this.taxonRelationshipTerm = taxonRelationshipTerm;
		}
		
	}
}
