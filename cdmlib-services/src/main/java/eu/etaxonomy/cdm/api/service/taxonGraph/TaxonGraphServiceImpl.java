/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.taxonGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.config.IncludedTaxonConfiguration;
import eu.etaxonomy.cdm.api.service.dto.IncludedTaxaDTO;
import eu.etaxonomy.cdm.api.service.dto.IncludedTaxaDTO.IncludedTaxon;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.ITaxonGraphDao;
import eu.etaxonomy.cdm.persistence.dao.taxonGraph.TaxonGraphException;
import eu.etaxonomy.cdm.persistence.dto.TaxonGraphEdgeDTO;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author a.kohlbecker
 * @since Oct 2, 2018
 *
 */
@Service
@Transactional(readOnly = true)
public class TaxonGraphServiceImpl implements ITaxonGraphService {

    static private final Logger logger = Logger.getLogger(TaxonGraphServiceImpl.class);


    @Autowired
    private ITaxonGraphDao taxonGraphDao;

    @Autowired
    private INameService nameService;

    @Autowired
    private ITaxonService taxonService;


    @Override
    public List<TaxonGraphEdgeDTO> edges(UUID fromtaxonUuid, UUID toTaxonUuid, boolean includeUnpublished) throws TaxonGraphException{
        return taxonGraphDao.edges(fromtaxonUuid, toTaxonUuid, includeUnpublished);
    }

    @Override
    public List<TaxonName> listIncludedNames(String queryString, MatchMode matchMode) {

        if(matchMode == null){
            matchMode = MatchMode.BEGINNING;
        }

        List<TaxonName> includedNames = new ArrayList<>();

        IncludedTaxonConfiguration configuration = new IncludedTaxonConfiguration(null, false, false, false);

        List<TaxonName> matchingNames = nameService.findNamesByTitleCache(queryString, matchMode, null);
        for(TaxonName name : matchingNames){
            if(logger.isDebugEnabled()){
                logger.debug("pageIncludedNames() - matching name: " + name.getTitleCache());
            }
            try {
                Taxon graphTaxon = taxonGraphDao.assureSingleTaxon(name, false);
                IncludedTaxaDTO includedTaxaDTO = taxonService.listIncludedTaxa(graphTaxon.getUuid(), configuration);
                List<UUID> includedTaxaUUIDs = includedTaxaDTO.getIncludedTaxa().stream().map(IncludedTaxon::getTaxonUuid).collect(Collectors.toList());
                List<TaxonBase> includedTaxa = taxonService.load(includedTaxaUUIDs, null);
                List<TaxonName> iclNames = includedTaxa.stream().map(TaxonBase::getName).collect(Collectors.toList());
                includedNames.addAll(iclNames);

            } catch(TaxonGraphException e){
                logger.error(e.getMessage());
            }
        }
        return includedNames;

    }
}
