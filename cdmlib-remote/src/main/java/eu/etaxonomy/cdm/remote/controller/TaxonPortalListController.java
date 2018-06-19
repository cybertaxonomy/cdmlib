package eu.etaxonomy.cdm.remote.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.annotations.Api;

/**
 * Copyright (C) 2009 EDIT European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 See LICENSE.TXT at the top of this package for the full license terms.
 */

/**
 *
 * @author a.kohlbecker
 * @since 26.08.2014
 */
@Controller
@Api("portal_taxon")
@RequestMapping(value = {"/portal/taxon"})
public class TaxonPortalListController extends TaxonListController {

    private static final List<String> SIMPLE_TAXON_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            "name.$",
            "name.rank.representations",
            "name.status.type.representations",
            "name.nomenclaturalReference.authorship",
            "name.nomenclaturalReference.inReference.authorship",
            "taxonNodes.classification",
            "synonym.name.nomenclaturalReference.authorship",
            "synonym.name.nomenclaturalReference.inReference.authorship",
            });

    public TaxonPortalListController() {
        super();
        setInitializationStrategy(SIMPLE_TAXON_INIT_STRATEGY);
    }

    @Override
    protected List<String> getSimpleTaxonInitStrategy() {
        return SIMPLE_TAXON_INIT_STRATEGY;
    }



}
