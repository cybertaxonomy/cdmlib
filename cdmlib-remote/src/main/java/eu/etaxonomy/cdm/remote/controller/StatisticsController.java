package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.IStatisticsService;
import eu.etaxonomy.cdm.api.service.statistics.Statistics;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsConfigurator;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsPartEnum;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsTypeEnum;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.taxon.Classification;

/**
 * this controller provides a method to count different entities in the entire
 * database as well as from a particular classification items of different types
 * can be choosen have a look at doStatistics method
 * 
 * @author s.buers
 * @created 07.11.2012
 * 
 */
@Controller
@RequestMapping(value = { "/statistics" })
public class StatisticsController {

	private static final Logger logger = Logger
			.getLogger(StatisticsController.class);

	private static final IdentifiableEntity ALL_DB = null;
	private static final IdentifiableEntity DEFAULT_TYPES = null;

	@Autowired
	private IClassificationService classificationService;

	private IStatisticsService service;

	@Autowired
	public void setService(IStatisticsService service) {
		this.service = service;
	}

	/**
	 * example query:
	 * 
	 * <pre>
	 *        part=ALL&part=CLASSIFICATION&type=DESCRIPTIVE_SOURCE_REFERENCES&type=ALL_TAXA&type=ACCEPTED_TAXA&type=SYNONYMS&type=TAXON_NAMES&type=ALL_REFERENCES&type=NOMECLATURAL_REFERENCES
	 * </pre>
	 * 
	 * @param part
	 * @param type
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView doStatistics(
			@RequestParam(value = "part", required = false) String[] part,
			@RequestParam(value = "type", required = false) String[] type,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		ModelAndView mv = new ModelAndView();

		List<StatisticsConfigurator> configuratorList = createConfiguratorList(
				part, type);
		List<Statistics> statistics = service
				.getCountStatistics(configuratorList);
		logger.info("doStatistics() - " + request.getServletPath());

		mv.addObject(statistics);
		return mv;
	}

	private List<StatisticsConfigurator> createConfiguratorList(String[] part,
			String[] type) {

		ArrayList<StatisticsConfigurator> configuratorList = new ArrayList<StatisticsConfigurator>();

		// 1. get types for configurators:
		// in our case all the configurators will have the same types
		// so we calculate the types once and save them in a helperConfigurator
		StatisticsConfigurator helperConfigurator = new StatisticsConfigurator();

		if (type != null) {
			for (String string : type) {
				helperConfigurator.addType(StatisticsTypeEnum.valueOf(string));
			}
		} else {
			for (StatisticsTypeEnum enumValue : StatisticsTypeEnum.values()) {
				helperConfigurator.addType(enumValue);
			}
		}

		// 2. determine the search areas (entire db or classifications) and put
		// each of them in a configurator:

		// if no part was given:
		if (part == null) {
			helperConfigurator.addFilter(DEFAULT_TYPES);
			configuratorList.add(helperConfigurator);
		}
		// else parse list of parts and create configurator for each:
		else {
			helperConfigurator.addFilter(DEFAULT_TYPES);
			for (String string : part) {
				// System.out.println(StatisticsPartEnum.ALL.toString());
				if (string.equals(StatisticsPartEnum.ALL.toString())) {
					configuratorList.add(helperConfigurator);
				} 
				else if (string.equals(StatisticsPartEnum.CLASSIFICATION
						.toString())) {
					List<Classification> classificationsList = classificationService
							.listClassifications(null, 0, null, null);
					for (Classification classification : classificationsList) {

						StatisticsConfigurator newConfigurator = new StatisticsConfigurator();
						newConfigurator.setType(helperConfigurator.getType());
						newConfigurator.getFilter().addAll(
								helperConfigurator.getFilter());
						newConfigurator.addFilter(classification);
						configuratorList.add(newConfigurator);
					}
				}
			}

		}

		return configuratorList;
	}

}
