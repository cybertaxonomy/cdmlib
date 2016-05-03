package eu.etaxonomy.cdm.remote.controller;

import io.swagger.annotations.Api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * this controller provides a method to count different entities in the entire
 * database as well as from a particular classification items of different types
 * can be chosen have a look at doStatistics method
 *
 * @author s.buers
 * @created 07.11.2012
 *
 */
@Controller
@Api("statistics")
@RequestMapping(value = { "/statistics" })
public class StatisticsController {

    private static final Logger logger = Logger
            .getLogger(StatisticsController.class);

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
     *        part=ALL&part=CLASSIFICATION&type=DESCRIPTIVE_SOURCE_REFERENCES&type=ALL_TAXA&type=ACCEPTED_TAXA&type=SYNONYMS&type=TAXON_NAMES&type=ALL_REFERENCES&type=NOMENCLATURAL_REFERENCES
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
            @RequestParam(value = "classificationName", required = false) String[] classificationName,
            @RequestParam(value = "classificationUuid", required = false) String[] classificationUuid,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        ModelAndView mv = new ModelAndView();

        List<StatisticsConfigurator> configuratorList = createConfiguratorList(
                part, type, classificationName, classificationUuid);
        List<Statistics> statistics = service
                .getCountStatistics(configuratorList);
        logger.info("doStatistics() - " + request.getRequestURI());

        mv.addObject(statistics);
        return mv;
    }

    private List<StatisticsConfigurator> createConfiguratorList(String[] part,
            String[] type, String[] classificationName,
            String[] classificationUuid) {

        ArrayList<StatisticsConfigurator> configuratorList = new ArrayList<StatisticsConfigurator>();

        // 1. get types for configurators:
        // in our case all the configurators will have the same types
        // so we calculate the types once and save them in a helperConfigurator
        StatisticsConfigurator helperConfigurator = new StatisticsConfigurator();

        if (type != null) {
            for (String string : type) {
                helperConfigurator.addType(StatisticsTypeEnum.valueOf(string));
            }
        } else { // if nothing is chosen, count all:
            for (StatisticsTypeEnum enumValue : StatisticsTypeEnum.values()) {
                helperConfigurator.addType(enumValue);
            }
        }

        // 2. determine the search areas (entire db, all classifications or
        // specific classifications) and put
        // each of them in a configurator:

        // gather classifications from names and uuids:

        Set<Classification> classificationFilters = new HashSet<Classification>();

        if (classificationName != null) {
            for (String string : classificationName) {
                    List <Classification> classifications = classificationService
                            .listByTitle(Classification.class, string,
                                    MatchMode.EXACT, null, null, null, null,
                                    null);
                    classificationFilters.addAll(classifications);

            }
        }
        if (classificationUuid != null && classificationUuid.length > 0) {
            for (String string : classificationUuid) {
                if (classificationService.exists(UUID.fromString(string))) {
                    classificationFilters.add(classificationService.find(UUID
                            .fromString(string)));
                }
            }
        }

        // if no part at all was given:
        if (part == null && classificationFilters.isEmpty()) {
            helperConfigurator.addFilter(service.getFilterALL_DB());
            configuratorList.add(helperConfigurator);
        }

        // else parse list of parts and create configurator for each:
        if (part != null) {
            helperConfigurator.addFilter(service.getFilterALL_DB());
            for (String string : part) {
                // System.out.println(StatisticsPartEnum.ALL.toString());
                if (string.equals(StatisticsPartEnum.ALL.toString())) {
                    configuratorList.add(helperConfigurator);
                } else if (string.equals(StatisticsPartEnum.CLASSIFICATION
                        .toString())) {
                    List<Classification> classificationsList = classificationService
                            .listClassifications(null, 0, null, null);
                    classificationFilters.addAll(classificationsList);

                }
            }
        }
        for (Classification classification : classificationFilters) {

            StatisticsConfigurator newConfigurator = new StatisticsConfigurator();
            newConfigurator.setType(helperConfigurator.getType());
            newConfigurator.getFilter().addAll(helperConfigurator.getFilter());
            newConfigurator.addFilter(classification);
            configuratorList.add(newConfigurator);
        }

        return configuratorList;
    }

}
