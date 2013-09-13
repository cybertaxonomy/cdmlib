package eu.etaxonomy.cdm.remote.dto.tdwg.voc;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.sun.xml.bind.CycleRecoverable;

import eu.etaxonomy.cdm.remote.dto.tdwg.Description;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.TaxonConcept.HasName;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.TaxonConcept.HasRelationship;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpeciesProfileModel", propOrder = {
	    "aboutTaxon",
	    "hasInformations"
})
@XmlRootElement(name = "SpeciesProfileModel", namespace = "http://rs.tdwg.org/ontology/voc/SpeciesProfileModel#")
public class SpeciesProfileModel extends Description implements CycleRecoverable {
	
	@XmlElement(name = "aboutTaxon", namespace = "http://rs.tdwg.org/ontology/voc/SpeciesProfileModel#")
	private AboutTaxon aboutTaxon;
	
	@XmlElement(name = "hasInformation", namespace = "http://rs.tdwg.org/ontology/voc/SpeciesProfileModel#")
	private Set<HasInformation> hasInformations = null;
	
	public TaxonConcept getAboutTaxon() {
		return aboutTaxon != null ? aboutTaxon.getTaxonConcept() : null;
	}

	public void setAboutTaxon(TaxonConcept taxonConcept) {
		this.aboutTaxon = new AboutTaxon(taxonConcept, false);
	}
	
	public TaxonConcept getAboutTaxonRelation() {
		return aboutTaxon != null ? aboutTaxon.getTaxonConcept() : null;
	}

	public void setAboutTaxonRelation(TaxonConcept taxonConcept) {
		this.aboutTaxon = new AboutTaxon(taxonConcept, true);
	}
	
	public Set<InfoItem> getHasInformation() {
		if(hasInformations != null) {
			Set<InfoItem> infoItems = new HashSet<InfoItem>();
			for(HasInformation hasInformation : hasInformations) {
				infoItems.add(hasInformation.getInfoItem());
			}
			return infoItems;
		} else {
			return null;
		}
	}

	public void setHasInformation(Set<InfoItem> infoItems) {
		if(infoItems != null) {
		  this.hasInformations = new HashSet<HasInformation>();
		  for(InfoItem infoItem : infoItems) {
			hasInformations.add( new HasInformation(infoItem));
		  }
		} else {
			hasInformations = null;
		}
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "AboutTaxon", propOrder = {
        "taxonConcept"
    })
	public static class AboutTaxon extends LinkType {
		@XmlElement(name = "TaxonConcept", namespace = "http://rs.tdwg.org/ontology/voc/TaxonConcept#")
		private TaxonConcept taxonConcept;
		
        protected AboutTaxon() {}
		
        protected AboutTaxon(TaxonConcept taxonConcept, boolean useRelation) {
        	if(useRelation) {
			    if(taxonConcept != null && taxonConcept.getIdentifier() != null) {
			    	this.setResource(taxonConcept.getIdentifier());
			    }  else {
			    	this.taxonConcept = taxonConcept;
			    }
			} else {
				this.taxonConcept = taxonConcept;
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
    @XmlType(name = "HasInformation", propOrder = {
        "infoItem"
    })
	public static class HasInformation extends LinkType {

		@XmlElement(name = "InfoItem", namespace = "http://rs.tdwg.org/ontology/voc/SpeciesProfileModel#")
		private InfoItem infoItem;
		
		protected HasInformation() {}
		
		protected HasInformation(InfoItem infoItem) {
			this.infoItem = infoItem;
		}
		
		protected InfoItem getInfoItem() {
			return infoItem;
		}

		protected void setInfoItem(InfoItem infoItem) {
			this.infoItem = infoItem;
		}
	}

	public Object onCycleDetected(Context context) {
		SpeciesProfileModel speciesProfileModel = new SpeciesProfileModel();
		speciesProfileModel.setIdentifier(super.getIdentifier());
		speciesProfileModel.setTitle(super.getTitle());
		return speciesProfileModel;
	}
}
