/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.etaxonomy.cdm.persistence.dao.initializer.EntityInitStrategy;
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

    private static final EntityInitStrategy SIMPLE_TAXON_INIT_STRATEGY = TaxonPortalController.SIMPLE_TAXON_INIT_STRATEGY.clone().extend(
            null,
            Arrays.asList(
            "synonym.name.nomenclaturalSource.citation.authorship",
            "synonym.name.nomenclaturalSource.citation.inReference.authorship",
            "relationsFromThisTaxon.toTaxon.taxonNodes" // needed for misapplications, see Taxon.isMisapplicationOnly()
            ),
            false
            );


    public TaxonPortalListController() {
        super();
        setInitializationStrategy(SIMPLE_TAXON_INIT_STRATEGY.getPropertyPaths());
    }

    @Override
    protected List<String> getSimpleTaxonInitStrategy() {
        return SIMPLE_TAXON_INIT_STRATEGY.getPropertyPaths();
    }



}
