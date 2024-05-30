/**
* Copyright (C) 2024 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import eu.etaxonomy.cdm.api.nameMatching.NameMatchingCandidateResult;
import eu.etaxonomy.cdm.api.nameMatching.NameMatchingCombinedResult;
import eu.etaxonomy.cdm.api.nameMatching.NameMatchingExactResult;
import eu.etaxonomy.cdm.api.nameMatching.NameMatchingOutputList;
import eu.etaxonomy.cdm.api.nameMatching.NameMatchingOutputObject;
import eu.etaxonomy.cdm.api.nameMatching.RequestedParam;
import eu.etaxonomy.cdm.api.service.INameMatchingService;
import eu.etaxonomy.cdm.api.service.NameMatchingServiceImpl.NameMatchingResult;
import eu.etaxonomy.cdm.api.service.NameMatchingServiceImpl.SingleNameMatchingResult;
import eu.etaxonomy.cdm.api.service.exception.NameMatchingParserException;
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
    private INameMatchingService nameMatchingService;


    @RequestMapping(
            value = {"match"},
            method = RequestMethod.GET)
    public NameMatchingOutputObject doGetNameMatching(
            @RequestParam(value="scientificName", required = true) String scientificName,
            @RequestParam(value="compareAuthor", required = false) boolean compareAuthor,
            @RequestParam(value="maxDistance", required = false) Integer maxDistance,
            HttpServletRequest request,
            @SuppressWarnings("unused") HttpServletResponse response) throws NameMatchingParserException {

        logger.info("doGetNameMatching()" + request.getRequestURI());

        NameMatchingResult result = nameMatchingService.findMatchingNames(scientificName, compareAuthor, maxDistance);
        RequestedParam requestedParam = new RequestedParam(scientificName, compareAuthor, maxDistance);
        return NameMatchingAdapter.invoke(result, requestedParam);
    }

    @RequestMapping(
            value = "matchingList",
            method = RequestMethod.POST)
    public NameMatchingOutputList doPostNameMatching (
            @RequestParam(value="scientificNames", required = true) String scientificName,
            @RequestParam(value="compareAuthor", required = false) boolean compareAuthor,
            @RequestParam(value="maxDistance", required = false) Integer maxDistance,
            HttpServletRequest request,
            @SuppressWarnings("unused") HttpServletResponse response) throws NameMatchingParserException {

        logger.info("doPostNameMatching()" + request.getRequestURI());

        List<String> scientificNamesList = new ArrayList <>(Arrays.asList(scientificName.split(";")));

        Map<String, NameMatchingResult> results = nameMatchingService.compareTaxonListName(scientificNamesList, compareAuthor, maxDistance);

        RequestedParam requestedParam = new RequestedParam(scientificNamesList, compareAuthor, maxDistance);

        return NameMatchingAdapter.invokeList (results, requestedParam);
    }

    /**POST Request with MultipartFile allows the usage of more than one parameters. It works calling the following command
    *curl -v -X POST -F "compareAuthor=false" -F "maxDistance=2" -F "file=@realTest.txt" http://localhost:8082/namematch/matchingList
    the option -v in curl gives more information about the http response (u.a.)
    */

    @PostMapping(
            value = "matchingListTest")
    public String doPostNameMatchingTest (
            @RequestPart("file") MultipartFile file,
            @RequestParam(value="compareAuthor", required = false) boolean compareAuthor,
            @RequestParam(value="maxDistance", required = false) Integer maxDistance,
            HttpServletRequest request,
            @SuppressWarnings("unused") HttpServletResponse response) {

        System.out.println("checkpoint1");

        logger.info("doPostNameMatching()" + request.getRequestURI());

        List <String> scientificNamesList = new ArrayList <>();
        try {
            if (!file.isEmpty()) {
                byte[] content = file.getBytes();
                String fileContent = new String(content, StandardCharsets.UTF_8.name());
                scientificNamesList = Arrays.asList(fileContent.split(";"));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "Checkpoint2 : scientific name: " + scientificNamesList.get(0) + " compare author: " + compareAuthor + " max distance: " + maxDistance;
    }

    private static class NameMatchingAdapter {

        private static NameMatchingOutputList invokeList (Map<String, NameMatchingResult> input, RequestedParam paramteres) {
            NameMatchingOutputList resultObject = new NameMatchingOutputList();
            List <NameMatchingOutputObject> outputList = new ArrayList<>();
            int i = 0 ;
            for (NameMatchingResult x : input.values()) {
                    String inputName = paramteres.getScientificNameList().get(i);
                    i++;
                    RequestedParam individualInputName = new RequestedParam (inputName, paramteres.isCompareAuthor(),paramteres.getMaxDistance());
                    outputList.add(NameMatchingAdapter.invoke(x, individualInputName));
            }
            resultObject.setOutputObject(outputList);
            return resultObject;
        }

        private static NameMatchingOutputObject invoke(NameMatchingResult innerResult, RequestedParam requestedParam) {
            NameMatchingOutputObject outputObject = new NameMatchingOutputObject();
            NameMatchingCombinedResult resultNameMatching = new NameMatchingCombinedResult();
            resultNameMatching.setExactMatches(loadResultListFromPartsList(innerResult.getExactResults()));
            resultNameMatching.setCandidates(loadCandiateResultListFromPartsList(innerResult.getBestResults()));

            outputObject.setRequest(requestedParam);
            outputObject.setResult(resultNameMatching);
            outputObject.setWarning(innerResult.getWarning());
            return outputObject;
        }

        private static List<NameMatchingExactResult> loadResultListFromPartsList(List<SingleNameMatchingResult> partsList) {
            return partsList.stream().map(p->loadResultFromParts(p)).collect(Collectors.toList());
        }

        private static List<NameMatchingCandidateResult> loadCandiateResultListFromPartsList(List<SingleNameMatchingResult> partsList) {
            return partsList.stream().map(p->loadCandidateResultFromParts(p)).collect(Collectors.toList());
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
