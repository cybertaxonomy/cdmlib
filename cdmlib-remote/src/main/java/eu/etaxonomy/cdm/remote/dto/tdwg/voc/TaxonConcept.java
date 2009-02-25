package eu.etaxonomy.cdm.remote.dto.tdwg.voc;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.remote.dto.tdwg.Actor;
import eu.etaxonomy.cdm.remote.dto.tdwg.Concept;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaxonConcept", propOrder = {
	    "primary",
	    "accordingTo",
	    "hasName",
	    "hasRelationships"
})
@XmlRootElement(name = "TaxonConcept", namespace = "http://rs.tdwg.org/ontology/voc/TaxonConcept#")
public class TaxonConcept extends Concept {

	@XmlElement(namespace = "http://rs.tdwg.org/ontology/voc/TaxonConcept#")
	private Boolean primary;

	@XmlElement(namespace = "http://rs.tdwg.org/ontology/voc/TaxonConcept#")
	private AccordingTo accordingTo;
	
	@XmlElement(name = "hasName", namespace = "http://rs.tdwg.org/ontology/voc/TaxonConcept#")
	private HasName hasName;
	
	@XmlElement(name = "hasRelationship", namespace = "http://rs.tdwg.org/ontology/voc/TaxonConcept#")
	private Set<HasRelationship> hasRelationships = null;
	
	public Set<Relationship> getHasRelationship() {
		if(hasRelationships != null) {
			Set<Relationship> relationships = new HashSet<Relationship>();
			for(HasRelationship hasRelationship : hasRelationships) {
				relationships.add(hasRelationship.getRelationship());
			}
			return relationships;
		} else {
			return null;
		}
	}

	public void setHasRelationship(Set<Relationship> relationships) {
		if(relationships != null) {
		  this.hasRelationships = new HashSet<HasRelationship>();
		  for(Relationship relationship : relationships) {
			hasRelationships.add( new HasRelationship(relationship));
		  }
		} else {
			hasRelationships = null;
		}
	}

	public TaxonName getHasName() {
		return hasName != null ? hasName.getTaxonName() : null;
	}

	public void setHasName(TaxonName taxonName) {
		this.hasName = new HasName(taxonName, false);
	}
	
	public TaxonName getHasNameRelation() {
		return hasName != null ? hasName.getTaxonName() : null;
	}

	public void setHasNameRelation(TaxonName taxonName) {
		this.hasName = new HasName(taxonName, true);
	}

	public Boolean isPrimary() {
		return primary;
	}
	
	public Actor getAccordingTo() {
		return accordingTo != null ? accordingTo.getActor() : null;
	}

	public void setAccordingTo(Actor accordingTo) {
		this.accordingTo = new AccordingTo(accordingTo, false);
	}
	
	public Actor getAccordingToRelation() {
		return accordingTo != null ? accordingTo.getActor() : null;
	}

	public void setAccordingToRelation(Actor accordingTo) {
		this.accordingTo = new AccordingTo(accordingTo, true);
	}

	public void setPrimary(Boolean primary) {
		this.primary = primary;
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "taxonName"
    })
	public static class HasName extends LinkType {
		@XmlElement(name = "TaxonName", namespace = "http://rs.tdwg.org/ontology/voc/TaxonName#")
		private TaxonName taxonName;
		
        protected HasName() {}
		
        protected HasName(TaxonName taxonName, boolean useRelation) {
        	if(useRelation) {
			    if(taxonName != null && taxonName.getIdentifier() != null) {
			    	this.setResource(taxonName.getIdentifier());
			    }  else {
			    	this.taxonName = taxonName;
			    }
			} else {
				this.taxonName = taxonName;
			}
		}

		protected TaxonName getTaxonName() {
			return taxonName;
		}

		protected void setTaxonName(TaxonName taxonName) {
			this.taxonName = taxonName;
		}
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "relationship"
    })
	public static class HasRelationship extends LinkType {

		@XmlElement(name = "Relationship")
		private Relationship relationship;
		
		protected HasRelationship() {}
		
		protected HasRelationship(Relationship relationship) {
			this.relationship = relationship;
		}
		
		protected Relationship getRelationship() {
			return relationship;
		}

		protected void setRelationship(Relationship relationship) {
			this.relationship = relationship;
		}
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "actor"
    })
	public static class AccordingTo extends LinkType {

		@XmlElements({
			@XmlElement(name = "Person", namespace = "http://rs.tdwg.org/ontology/voc/Person#", type = Person.class),
			@XmlElement(name = "Team", namespace = "http://rs.tdwg.org/ontology/voc/Team#", type = Team.class)
		})
		private Actor actor;
		
		protected AccordingTo() {}
		
		protected AccordingTo(Actor actor, boolean useRelation) {
			if(useRelation) {
			    if(actor != null && actor.getIdentifier() != null) {
			    	this.setResource(actor.getIdentifier());
			    }  else {
			    	this.actor = actor;
			    }
			} else {
				this.actor = actor;
			}
		}
		
		protected Actor getActor() {
			return actor;
		}

		protected void setActor(Actor actor) {
			this.actor = actor;
		}
	}
}
