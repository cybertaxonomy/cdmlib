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

import eu.etaxonomy.cdm.api.service.IStatisticsService;
import eu.etaxonomy.cdm.api.service.statistics.Statistics;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsConfigurator;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsPartEnum;
import eu.etaxonomy.cdm.api.service.statistics.StatisticsTypeEnum;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;

/**
 * @author sybille
 * @created 07.11.2012 this class provides counting of different entities in a
 *          database
 * 
 */
@Controller
@RequestMapping(value = { "/statistics" })
public class StatisticsController {

	private static final Logger logger = Logger
			.getLogger(StatisticsController.class);

	// private static final List<StatisticsTypeEnum> D = null;

	private static final IdentifiableEntity ALL_DB = null;
	private static final IdentifiableEntity DEFAULT_Entity = ALL_DB;

	private IStatisticsService service;

	@Autowired
	public void setService(IStatisticsService service) {
		this.service = service;
	}

	// StatisticsConfigurator configurator;

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

		// configurator = new StatisticsConfigurator();
		ModelAndView mv = new ModelAndView();

		// TODO:
		// service.getStatistics(configurator);
		List<StatisticsConfigurator> configuratorList = createConfiguratorList(
				part, type);
		// configuratorList.add(configurator);
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
		// all the configurators will have the same types
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

		// 2. determine the entities and put each of them in a configurator:

		// if no part was given:
		if (part == null) {
			helperConfigurator.addFilter(DEFAULT_Entity);
			configuratorList.add(helperConfigurator);
		}
		// else parse list of parts:
		else {
			for (String string : part) {
				if(part.equals(StatisticsPartEnum.ALL.toString())){
					helperConfigurator.addFilter(ALL_DB);
					configuratorList.add(helperConfigurator);
				}
				else if(part.equals(StatisticsPartEnum.CLASSIFICATION.toString())){
					//TODO create configurators for classifications
					// do not forget to clone the configurator or create new one.
				}
			}

		}

		return configuratorList;
	}

	// private void createConfigurator(String part, String[] type) {
	//
	// if (part != null) {
	// for (String string : part) {
	// configurator.addPart(StatisticsPartEnum.valueOf(string));
	// }
	// } else
	// configurator.addPart(StatisticsPartEnum.ALL);
	//
	// if (type != null) {
	// for (String string : type) {
	// configurator.addType(StatisticsTypeEnum.valueOf(string));
	// }
	// } else
	// setDefaultType();
	// }
	//
	// private void setDefaultType() {
	// // for default choose all types:
	// for (StatisticsTypeEnum type : StatisticsTypeEnum.values()) {
	// configurator.addType(type);
	// }
	//
	// }
}
