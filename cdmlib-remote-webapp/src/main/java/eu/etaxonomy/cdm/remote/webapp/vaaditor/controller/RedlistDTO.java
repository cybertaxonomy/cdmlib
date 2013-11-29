package eu.etaxonomy.cdm.remote.webapp.vaaditor.controller;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;

import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.strategy.cache.name.CacheUpdate;
import eu.etaxonomy.cdm.strategy.match.Match;
import eu.etaxonomy.cdm.strategy.match.MatchMode;
import eu.etaxonomy.cdm.strategy.match.Match.ReplaceMode;
import eu.etaxonomy.cdm.validation.Level2;


public class RedlistDTO{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	

    private Taxon taxon;

    private TaxonDescription taxonDescription; 

	private String fullTitleCache;
	private Rank rank;

    
    public RedlistDTO(Taxon taxon){
    	this.taxon = taxon;
    }
    
    
    //----Getter - Setter - methods ----//
    
    public String getFullTitleCache() {
    	return taxon.getName().getFullTitleCache();
    }

    public void setFullTitleCache(String fullTitleCache) {
    	taxon.getName().setFullTitleCache(fullTitleCache, true);
    	taxon.setTitleCache(fullTitleCache, true);
    }
    
    public Taxon getTaxon() {
		return taxon;
	}
    
    public Set<TaxonDescription> getTaxonDescription(){
    	return taxon.getDescriptions();
    }
    
    
    /**
     * Returns the taxonomic {@link Rank rank} of <i>this</i> taxon name.
     *
     * @see 	Rank
     */
    public Rank getRank(){
    	rank = taxon.getName().getRank();
    	return rank;
    }

    /**
     * @see  #getRank()
     */
    public void setRank(Rank rank){
    	taxon.getName().setRank(rank);
    }
    
}
