package eu.etaxonomy.cdm.remote.webapp.vaaditor.controller;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Taxon;


public class RedlistDTO{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	

    private Taxon taxon;

    private Collection<DescriptionElementBase> listTaxonDescription;

	private String fullTitleCache;
	private Rank rank;

    
    public RedlistDTO(Taxon taxon, Collection<DescriptionElementBase> listTaxonDescription){
    	this.taxon = taxon;
    	this.listTaxonDescription = listTaxonDescription;
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

    
    
    /**
     * Returns the taxonomic {@link Rank rank} of <i>this</i> taxon name.
     *
     * @see 	Rank
     */
    public Rank getRank(){
    	rank = taxon.getName().getRank();
    	return rank;
    }
    
    public UUID getUUID(){
    	return taxon.getUuid();
    }

    /**
     * @see  #getRank()
     */
    public void setRank(Rank rank){
    	taxon.getName().setRank(rank);
    }
    
    public PresenceAbsenceTermBase<?> getDistributionStatus(){
    	Distribution db = getDistribution();
    	if(db != null){
    		return db.getStatus();
    	}
		return null;
    }
    
    public void setDistributionStatus(PresenceAbsenceTermBase<?> status){
    	Distribution db = getDistribution();
    	if(db != null){
    		db.setStatus(status);
    	}
    }
    
    private Distribution getDistribution(){
    	for(DescriptionElementBase deb : listTaxonDescription){
			if(deb instanceof Distribution){
				return (Distribution)deb;
			}
    	}
    	return null;
    }
    
}
