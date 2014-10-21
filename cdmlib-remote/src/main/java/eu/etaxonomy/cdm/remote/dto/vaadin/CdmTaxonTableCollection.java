package eu.etaxonomy.cdm.remote.dto.vaadin;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.TaxonServiceImpl;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * This class acts like a data transfer object. It is intended to ease the communication
 * between the jpa and vaadin's ui widget/objects. The dto is filled with the desired information
 * especially needed to display. The ui is able to change certain fields. In the end the services 
 * persist the changed values.<p> 
 * 
 * So it reduces the service calls, at least in theory. 
 * 
 * 
 * @author a.oppermann
 *
 */
public class CdmTaxonTableCollection{
	
	Logger logger = Logger.getLogger(CdmTaxonTableCollection.class);

    private Taxon taxon;

    private Collection<DescriptionElementBase> listTaxonDescription;

    
	private String fullTitleCache;
	
	private Rank rank;

    
    public CdmTaxonTableCollection(Taxon taxon, Collection<DescriptionElementBase> listTaxonDescription){
    	this.taxon = CdmBase.deproxy(taxon, Taxon.class);
    	this.listTaxonDescription = listTaxonDescription;
    }
    
    public CdmTaxonTableCollection(Taxon taxon){
    	this.taxon = CdmBase.deproxy(taxon, Taxon.class);
    }
    
    //----Getter - Setter - methods ----//
    /**
     * 
     * @return
     */
    public String getFullTitleCache() {
    	TaxonNameBase name = taxon.getName();
    	name = CdmBase.deproxy(name, TaxonNameBase.class);
    	if(name ==  null){
    		return "-";
    	}
		return name.getFullTitleCache();
    }
    /**
     * 
     * @param fullTitleCache
     */
    public void setFullTitleCache(String fullTitleCache) {
    	taxon.getName().setFullTitleCache(fullTitleCache, true);
    	taxon.setTitleCache(fullTitleCache, true);
    }
    /**
     * 
     * @return
     */
    public Taxon getTaxon() {
		return taxon;
	}
    
    public void setTaxon(Taxon taxon){
    	this.taxon = taxon;
    }

    
    
    /**
     * Returns the taxonomic {@link Rank rank} of <i>this</i> taxon name.
     *
     * @see 	Rank
     */
    public String getRank(){
    	rank = taxon.getName().getRank();
    	if(rank == null){
    		return "-";
    	}
    	return rank.toString();
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
    /**
     * 
     * @return
     */
    public PresenceAbsenceTermBase<?> getDistributionStatus(String distribution){
    	Distribution db = getDistribution(distribution);
    	if(db != null){
    		return db.getStatus();
    	}
		return null;
    }
    
//    public ComboBox getDistributionComboBox(){
//		if(getDistributionStatus() != null && termList != null){
//			BeanItemContainer<PresenceAbsenceTermBase> container = new BeanItemContainer<PresenceAbsenceTermBase>(PresenceAbsenceTermBase.class);
//			container.addAll(termList);
//			final ComboBox box = new ComboBox();
//			box.setContainerDataSource(container);
//			box.setImmediate(true);
////			setValueChangeListener(box);
//			if(getDistributionStatus() != null){
//				box.setValue(getDistributionStatus());
//			}
//			return box;
//		}else{
//			return null;
//		}
//    }
    
    
    
//    private void setValueChangeListener(final ComboBox box){
//    	box.addValueChangeListener(new ValueChangeListener() {
//			private static final long serialVersionUID = 1L;
//			@Override
//			public void valueChange(ValueChangeEvent event) {
//				logger.info("Value Change: "+ box.getValue());
//				setDistributionStatus((PresenceAbsenceTermBase<?>)box.getValue());
//			}
//    	});
//    }
    
    /**
     * 
     * @param status
     */
    public void setDistributionStatus(String distribution, PresenceAbsenceTermBase<?> status){
    	Distribution db = getDistribution(distribution);
    	if(db != null){
    		db.setStatus(status);
//    		DescriptionServiceImpl desc = new DescriptionServiceImpl();
//    		desc.saveDescriptionElement(db);
//    		descriptionService.saveDescriptionElement(db);
    	}
    }
    /**
     * 
     * @return
     */
    public Distribution getDistribution(String distribution){
    	if(listTaxonDescription != null){
    		for(DescriptionElementBase deb : listTaxonDescription){
    			if(deb instanceof Distribution){
    				//FIXME HOW TO IMPLEMENT A FILTER ???
    				Distribution db =  (Distribution)deb;
    				if(db.getArea().getTitleCache().equalsIgnoreCase(distribution)){
    					return db;
    				}
    			}
    		}
    	}
    	return null;
    }
    
    
    //----------- Detail View ------------------//
    
    /**
     * 
     * @return
     */
    public String getTaxonNameCache(){
		return taxon.getName().getTitleCache();
	}
    
    public void setTaxonNameCache(String titlecache){
    	taxon.getName().setTitleCache(titlecache, true);
    }
	/**
	 * 
	 * @return
	 */
	public String getNomenclaturalCode(){
		return taxon.getName().getNomenclaturalCode().getTitleCache();
	}
	/**
	 * 
	 * @return
	 */
	public String getSecundum(){
		return taxon.getSec().toString();
	}
    
    
}
