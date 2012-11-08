package eu.etaxonomy.cdm.remote.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.IdentifiableServiceBase;
import eu.etaxonomy.cdm.api.service.statistics.Statistics;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsConfigurator;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsPartEnum;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsTypeEnum;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.IdentifiableDaoBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.taxon.TaxonDaoHibernateImpl;
import eu.etaxonomy.cdm.persistence.dao.taxon.IClassificationDao;
import eu.etaxonomy.cdm.persistence.dao.taxon.ITaxonDao;
import eu.etaxonomy.cdm.strategy.cache.common.IdentifiableEntityDefaultCacheStrategy;

/**
 * @author sybille
 * @created 07.11.2012
 * 
 */
@Controller
@RequestMapping(value = { "/statistic" })
public class StatisticsController extends
		BaseController<TaxonBase, ITaxonService> {

	
	@Autowired
    private IClassificationService clService;
	
	@Autowired
	private ITaxonDao taxonDao;
	
	@Autowired
	private IClassificationDao classificationDao;
	
	@Override
	@Autowired
	public void setService(ITaxonService service) {
		// TODO Auto-generated method stub

	}
	
	

	
	
	StatisticsConfigurator configurator;
	
	@RequestMapping(value = { "count" }, method = RequestMethod.GET)
	public Statistics count(@RequestParam(value = "All", required = false) boolean all,
			@RequestParam(value = "Classification", required = false) boolean classification,
			@RequestParam(value = "Filter", required=false) StatisticsTypeEnum filter) {
		
		configurator = new StatisticsConfigurator();
		
		
		
		if(all||(!all&&!classification)){
			configurator.addPart(StatisticsPartEnum.ALL);
		}
		
		if(classification ){
			configurator.addPart(StatisticsPartEnum.CLASSIFICATION);
		}
		
		//TODO count in classification
		switch (filter) {
		case CLASSIFICATION:
			//that does not make to much sense yet:
			configurator.addFilter(Classification.NewInstance("classification"));
			
		case ALL_TAXA:
		case ACCEPTED_TAXA:
		case ALL_REFERENCES:
		case SYNONYMS:
		case TAXON_NAMES:
		case NOMECLATURAL_REFERENCES:
		case DESCRIPTIVE_SOURCE_REFERENCES:
			
			
			break;

		default:
			
			//TODO set to default filter
			break;
		}
		
		return getStatistics();
	}
	
	// this could be a service on it's own:
	private Statistics getStatistics() {
		Statistics result = new Statistics();
		for (StatisticsPartEnum part : configurator.getPartList()) {
			if (part.compareTo(StatisticsPartEnum.ALL)==0){
				//TODO iterate over filter and call countAll(class type);
//				System.out.println(taxonDao.cou);
			}
			else if (part.compareTo(StatisticsPartEnum.CLASSIFICATION)==0){
				// TODO get all classifications
				//TODO find out if i have to parse through references or if there is already a method
				
			}
		}
		return null;
	}

// TODO countAll();
//	Statistics countAll(IdentifiableServiceBase<IdentifiableEntity, IIdentifiableDao<T>> service){
//		
//	}
	

}
