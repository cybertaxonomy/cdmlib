package eu.etaxonomy.cdm.remote.dto.vaadin;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Taxon;

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

	private static final long serialVersionUID = 1L;
	
	Logger logger = Logger.getLogger(CdmTaxonTableCollection.class);

    private Taxon taxon;

    private Collection<DescriptionElementBase> listTaxonDescription;

    private List<PresenceAbsenceTermBase> termList;
    
	private String fullTitleCache;
	
	private Rank rank;

    
    public CdmTaxonTableCollection(Taxon taxon, Collection<DescriptionElementBase> listTaxonDescription, List<PresenceAbsenceTermBase> termList){
    	this.taxon = taxon;
    	this.listTaxonDescription = listTaxonDescription;
    	this.termList = termList;
    }
    
    
    //----Getter - Setter - methods ----//
    /**
     * 
     * @return
     */
    public String getFullTitleCache() {
    	return taxon.getName().getFullTitleCache();
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
    public PresenceAbsenceTermBase<?> getDistributionStatus(){
    	Distribution db = getDistribution();
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
    public void setDistributionStatus(PresenceAbsenceTermBase<?> status){
    	Distribution db = getDistribution();
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
    public Distribution getDistribution(){
    	for(DescriptionElementBase deb : listTaxonDescription){
			if(deb instanceof Distribution){
				return (Distribution)deb;
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
