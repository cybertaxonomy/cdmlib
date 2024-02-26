/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.dto.TaxonFindDto;
import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.config.FindTaxaAndNamesConfiguratorImpl;
import eu.etaxonomy.cdm.api.service.config.IFindTaxaAndNamesConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.initializer.EntityInitStrategy;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.persistence.query.NameSearchOrder;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;
import io.swagger.annotations.Api;

/**
 *
 * @author a.kohlbecker
 * @since 26.08.2014
 */
@Controller
@Api("portal_taxon")
@RequestMapping(value = {"/portal/taxon"})
public class TaxonPortalListController extends TaxonListController {

    private static final Logger logger = LogManager.getLogger();

    private static final EntityInitStrategy SIMPLE_TAXON_INIT_STRATEGY = TaxonPortalController.SIMPLE_TAXON_INIT_STRATEGY.clone().extend(
            null,
            Arrays.asList(
            "annotations.$",
            "annotations.annotationType.$",
            "annotations.annotationType.includes.$",
            "synonyms.annotations.$",
            "synonyms.annotations.annotationType.$",
            "synonyms.annotations.annotationType.includes.$",
            "synonyms.name.nomenclaturalSource.citation.authorship",
            "synonyms.name.nomenclaturalSource.citation.inReference.authorship",
            "relationsFromThisTaxon.toTaxon.taxonNodes" // needed for misapplications, see Taxon.isMisapplicationOnly()
            ),
            false
            );

    @Autowired
    private IClassificationService classificationService;

    @Autowired
    private ITaxonNodeService taxonNodeService;

    public TaxonPortalListController() {
        setInitializationStrategy(SIMPLE_TAXON_INIT_STRATEGY.getPropertyPaths());
    }

    @Override
    protected List<String> getSimpleTaxonInitStrategy() {
        return SIMPLE_TAXON_INIT_STRATEGY.getPropertyPaths();
    }

    /**
     * Same as {@link #doFind(String, UUID, UUID, Set, Integer, Integer, Boolean, Boolean, Boolean, Boolean, MatchMode, NameSearchOrder, Boolean, HttpServletRequest, HttpServletResponse)}
     * but as wrapper with 2 additional values: nomenclaturalString and acceptedTaxonUuid.
     *
     * @return a Pager on a list of {@link IdentifiableEntity}s initialized by
     *         the following strategy {@link #SIMPLE_TAXON_INIT_STRATEGY}
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value={"findDto"})
    public Pager<TaxonFindDto> doFindDto(
            @RequestParam(value = "query", required = true) String query,
            @RequestParam(value = "tree", required = false) UUID classificationUuid,
            @RequestParam(value = "subtree", required = false) UUID subtreeUuid,
            @RequestParam(value = "area", required = false) Set<NamedArea> areas,
            @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "doTaxa", required = false) Boolean doTaxa,
            @RequestParam(value = "doSynonyms", required = false) Boolean doSynonyms,
            @RequestParam(value = "doMisappliedNames", required = false) Boolean doMisappliedNames,
            @RequestParam(value = "doTaxaByCommonNames", required = false) Boolean doTaxaByCommonNames,
            @RequestParam(value = "matchMode", required = false) MatchMode matchMode,
            @RequestParam(value = "order", required = false, defaultValue="ALPHA") NameSearchOrder order,
            @RequestParam(value = "includeAuthors", required = false) Boolean includeAuthors,
            HttpServletRequest request,
            HttpServletResponse response
            )
             throws IOException {

        boolean includeUnpublished = NO_UNPUBLISHED;

        logger.info("doFindDto() " + requestPathAndQuery(request));

        PagerParameters pagerParams = new PagerParameters(pageSize, pageIndex);
        pagerParams.normalizeAndValidate(response);

        IFindTaxaAndNamesConfigurator config = FindTaxaAndNamesConfiguratorImpl.NewInstance();
        config.setIncludeUnpublished(includeUnpublished);
        config.setPageNumber(pagerParams.getPageIndex());
        config.setPageSize(pagerParams.getPageSize());
        config.setTitleSearchString(query);
        config.setDoTaxa(doTaxa!= null ? doTaxa : Boolean.FALSE );
        config.setDoSynonyms(doSynonyms != null ? doSynonyms : Boolean.FALSE );
        config.setDoMisappliedNames(doMisappliedNames != null ? doMisappliedNames : Boolean.FALSE);
        config.setDoTaxaByCommonNames(doTaxaByCommonNames != null ? doTaxaByCommonNames : Boolean.FALSE );
        config.setMatchMode(matchMode != null ? matchMode : MatchMode.BEGINNING);
        config.setTaxonPropertyPath(getSimpleTaxonInitStrategy());
        config.setNamedAreas(areas);
        config.setDoIncludeAuthors(includeAuthors != null ? includeAuthors : Boolean.FALSE);
        config.setOrder(order);

        if(classificationUuid != null){
            Classification classification = classificationService.find(classificationUuid);
            config.setClassification(classification);
        }

        TaxonNode subtree = getSubtreeOrError(subtreeUuid, taxonNodeService, response);
        config.setSubtree(subtree);

        return service.findTaxaAndNamesDto(config);
    }
}