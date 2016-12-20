package eu.etaxonomy.cdm.remote.controller;

import io.swagger.annotations.Api;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

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
 * @date 26.08.2014
 */
@Controller
@Api("portal_taxon")
@RequestMapping(value = {"/portal/taxon"})
public class TaxonPortalListController extends TaxonListController {

    private static final List<String> SIMPLE_TAXON_INIT_STRATEGY = Arrays.asList(new String []{
            "$",
            // the name
            "name.$",
            "name.rank.representations",
            "name.status.type.representations",
            "name.nomenclaturalReference.authorship",
            "name.nomenclaturalReference.inReference",
            "taxonNodes.classification",
            });

    @Override
    protected List<String> getSimpleTaxonInitStrategy() {
        // TODO Auto-generated method stub
        return SIMPLE_TAXON_INIT_STRATEGY;
    }

}
