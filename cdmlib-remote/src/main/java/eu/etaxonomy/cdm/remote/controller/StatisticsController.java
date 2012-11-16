package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.IStatisticsService;
import eu.etaxonomy.cdm.api.service.statistics.Statistics;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsConfigurator;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsPartEnum;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * @author sybille
 * @created 07.11.2012
 * 
 */
@Controller
@RequestMapping(value = { "/statistic" })
public class StatisticsController extends
		BaseController<IdentifiableEntity<?>, IStatisticsService> {

	private static final Logger logger = Logger
			.getLogger(StatisticsController.class);

	@Autowired
	private IClassificationService clService;

	@Override
	@Autowired
	public void setService(IStatisticsService service) {
		this.service = service;
//		System.out.println();
	}

	StatisticsConfigurator configurator;

	@RequestMapping(value = { "statistics" }, method = RequestMethod.GET)
	public ModelAndView doStatistics(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		configurator= new StatisticsConfigurator();
		ModelAndView mv = new ModelAndView();
		
		//TODO fill configurator;
//		service.getStatistics(configurator);
		Integer i =((Integer)service.countAll(configurator));
		logger.info(i.toString());
//		mv.addObject((Integer)service.count(Taxon.class));
		mv.addObject(i);
		return mv;
	}

	
	// 16.11.2012
	
	
	// public Statistics count(@RequestParam(value = "All", required = false)
	// boolean all,
	// @RequestParam(value = "Classification", required = false) boolean
	// classification,
	// @RequestParam(value = "Filter", required=false) StatisticsTypeEnum
	// filter) {
	//
	// configurator = new StatisticsConfigurator();
	//
	//
	//
	// if(all||(!all&&!classification)){
	// configurator.addPart(StatisticsPartEnum.ALL);
	// }
	//
	// if(classification ){
	// configurator.addPart(StatisticsPartEnum.CLASSIFICATION);
	// }
	//
	// //TODO count in classification
	// switch (filter) {
	// case CLASSIFICATION:
	// //that does not make to much sense yet:
	// configurator.addFilter(Classification.NewInstance("classification"));
	//
	// case ALL_TAXA:
	// case ACCEPTED_TAXA:
	// case ALL_REFERENCES:
	// case SYNONYMS:
	// case TAXON_NAMES:
	// case NOMECLATURAL_REFERENCES:
	// case DESCRIPTIVE_SOURCE_REFERENCES:
	//
	//
	// break;
	//
	// default:
	//
	// //TODO set to default filter
	// break;
	// }
	//
	// return getStatistics();
	// }

	// this could be a service on it's own:
	private Statistics getStatistics() {
//		Statistics result = new Statistics();
		for (StatisticsPartEnum part : configurator.getPartList()) {
			if (part.compareTo(StatisticsPartEnum.ALL) == 0) {
				// TODO iterate over filter and call countAll(class type);
				// System.out.println(taxonDao.cou);
			} else if (part.compareTo(StatisticsPartEnum.CLASSIFICATION) == 0) {
				// TODO get all classifications
				// TODO find out if i have to parse through references or if
				// there is already a method

			}
		}
		return null;
	}

	// TODO countAll();
	// Statistics countAll(IdentifiableServiceBase<IdentifiableEntity,
	// IIdentifiableDao<T>> service){
	//
	// }

}
