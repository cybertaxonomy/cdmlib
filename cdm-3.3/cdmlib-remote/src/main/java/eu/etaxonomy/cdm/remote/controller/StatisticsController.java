package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
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

/**
 * @author sybille
 * @created 07.11.2012 this class provides counting of different entities in a
 *          database
 * 
 */
@Controller
@RequestMapping(value = { "/statistic" })
public class StatisticsController {

	private static final Logger logger = Logger
			.getLogger(StatisticsController.class);

	private static final List<StatisticsTypeEnum> D = null;

	private IStatisticsService service;

	@Autowired
	public void setService(IStatisticsService service) {
		this.service = service;
	}

	StatisticsConfigurator configurator;

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

		configurator = new StatisticsConfigurator();
		ModelAndView mv = new ModelAndView();

		createConfigurator(part, type);
		// service.getStatistics(configurator);
		Statistics statistics = service.getCountStatistics(configurator);
		logger.info("doStatistics() - " + request.getRequestURI());

		mv.addObject(statistics);
		return mv;
	}

	private void createConfigurator(String[] part, String[] type) {

		if (part != null) {
			for (String string : part) {
				configurator.addPart(StatisticsPartEnum.valueOf(string));
			}
		} else
			configurator.addPart(StatisticsPartEnum.ALL);

		if (type != null) {
			for (String string : type) {
				configurator.addType(StatisticsTypeEnum.valueOf(string));
			}
		} else
			setDefaultType();
	}

	private void setDefaultType() {
		// for default choose all types:
		for (StatisticsTypeEnum type : StatisticsTypeEnum.values()) {
			configurator.addType(type);
		}

	}
}
