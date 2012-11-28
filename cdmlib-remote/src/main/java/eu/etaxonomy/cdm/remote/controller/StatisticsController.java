package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSON;
import net.sf.json.xml.JSONTypes;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
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
import eu.etaxonomy.cdm.model.taxon.Taxon;

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
    private IClassificationService clService;
    @Autowired
    public void setService(IStatisticsService service) {
        this.service = service;
    }

    StatisticsConfigurator configurator;

    /**
     * example query:
     * <pre>
       parts=ALL&parts=CLASSIFICATION&types=DESCRIPTIVE_SOURCE_REFERENCES&types=ALL_TAXA&types=ACCEPTED_TAXA&types=SYNONYMS&types=TAXON_NAMES&types=ALL_REFERENCES&types=NOMECLATURAL_REFERENCES
      </pre>
     * @param parts
     * @param types
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping(value = { "statistics" }, method = RequestMethod.GET)
    public ModelAndView doStatistics(
			@RequestParam(value = "parts", required = false) String[] parts,
			@RequestParam(value = "types", required = false) String[] types,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {
		// TODO in createConfigurator() create defaults for parts and types and
		// set them to "false"
        configurator = new StatisticsConfigurator();
        ModelAndView mv = new ModelAndView();

        createConfigurator(parts, types);
        // service.getStatistics(configurator);
        Statistics statistics = service.getCountStatistics(configurator);
        logger.info("doStatistics() - " + request.getServletPath());

        mv.addObject(statistics);
        return mv;
    }

    private void createConfigurator(String[] part, String[] type) {
        if (type != null) {
            for (String string : type) {
                configurator.addType(StatisticsTypeEnum.valueOf(string));
            }
        }
        if (part != null) {
            for (String string : part) {
                configurator.addPart(StatisticsPartEnum.valueOf(string));
            }
        }
    }

}
