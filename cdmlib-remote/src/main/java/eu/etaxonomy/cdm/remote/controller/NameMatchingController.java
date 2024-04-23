/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.etaxonomy.cdm.api.nameMatching.NameMatchingCandidateResult;
import eu.etaxonomy.cdm.api.nameMatching.NameMatchingCombinedResult;
import eu.etaxonomy.cdm.api.nameMatching.NameMatchingExactResult;
import eu.etaxonomy.cdm.api.nameMatching.NameMatchingOtherCandidateResult;
import eu.etaxonomy.cdm.api.service.INameMatchingService;
import eu.etaxonomy.cdm.api.service.NameMatchingServiceImpl.NameMatchingResult;
import eu.etaxonomy.cdm.api.service.NameMatchingServiceImpl.SingleNameMatchingResult;
import eu.etaxonomy.cdm.persistence.dto.NameMatchingParts;
import io.swagger.annotations.Api;

/**
 * @author andreabee90
 * @since 05.03.2024
 */
@RestController
@Api("name_matching")
@RequestMapping(value = {"/namematch/" })
public class NameMatchingController {

    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private INameMatchingService nameMatchingservice;


    @RequestMapping(
            value = {"match"},
            method = RequestMethod.GET)
    public NameMatchingCombinedResult doGetNameMatching(
            @RequestParam(value="namecache", required = true) String nameCache,
            @RequestParam(value="author", required = false) boolean compareAuthor,
            @RequestParam(value="distance", required = false) int distance,
            @RequestParam(value="relaxedsearch", required = false) boolean relaxedSearch,
            @RequestParam(value="otherCandidates", required = false) boolean otherCandidates,
            HttpServletRequest request,
            @SuppressWarnings("unused") HttpServletResponse response) {

        logger.info("doGetNameMatching()" + request.getRequestURI());

        NameMatchingResult result;

        result = nameMatchingservice.wrapperResults(nameCache, compareAuthor, distance, relaxedSearch, otherCandidates);
        return NameMatchingAdapter.invoke(result);
    }

    private static class NameMatchingAdapter {

        private static NameMatchingCombinedResult invoke(NameMatchingResult nameMatchingResult) {
            NameMatchingCombinedResult result = new NameMatchingCombinedResult();
            result.setExactMatches(loadResultListFromPartsList(nameMatchingResult.getExactResults()));
            result.setClosestMatches(loadCandiateResultListFromPartsList(nameMatchingResult.getClosestResults()));
            result.setOtherCandidates(loadOtherCandiateResultListFromPartsList(nameMatchingResult.getOtherCandidatesResults()));
            return result;
        }

        private static List<NameMatchingExactResult> loadResultListFromPartsList(List<SingleNameMatchingResult> partsList) {
            return partsList.stream().map(p->loadResultFromParts(p)).collect(Collectors.toList());
        }

        private static List<NameMatchingCandidateResult> loadCandiateResultListFromPartsList(List<SingleNameMatchingResult> partsList) {
            return partsList.stream().map(p->loadCandidateResultFromParts(p)).collect(Collectors.toList());
        }

        private static List<NameMatchingOtherCandidateResult> loadOtherCandiateResultListFromPartsList(List<SingleNameMatchingResult> partsList) {
            return partsList.stream().map(p->loadOtherCandidateResultFromParts(p)).collect(Collectors.toList());
        }

        private static NameMatchingExactResult loadResultFromParts(NameMatchingParts parts) {
           return loadResultFromParts(parts, new NameMatchingExactResult());
        }

        private static NameMatchingCandidateResult loadCandidateResultFromParts(SingleNameMatchingResult parts) {
            NameMatchingCandidateResult result = new NameMatchingCandidateResult();
            loadResultFromParts(parts, result);
            result.setDistance(parts.getDistance());
            return result;
        }

        private static NameMatchingOtherCandidateResult loadOtherCandidateResultFromParts(SingleNameMatchingResult parts) {
            NameMatchingOtherCandidateResult result = new NameMatchingOtherCandidateResult();
            loadResultFromParts(parts, result);
            result.setDistance(parts.getDistance());
            return result;
        }

        private static NameMatchingExactResult loadResultFromParts(NameMatchingParts parts, NameMatchingExactResult result) {
            result.setTaxonNameId(parts.getTaxonNameId());
            result.setTaxonNameUuid(parts.getTaxonNameUuid());
            result.setAuthorship(parts.getAuthorshipCache());
            result.setNameWithAuthor(parts.getTitleCache());
            result.setPureName(parts.getNameCache());
            return result;
        }
    }
}