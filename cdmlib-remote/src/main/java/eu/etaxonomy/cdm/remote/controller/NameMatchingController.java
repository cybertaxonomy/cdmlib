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
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import au.com.bytecode.opencsv.CSVWriter;
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
    @Autowired
    private ObjectMapper objectMapper;


    @RequestMapping(
            value = {"match"},
            method = RequestMethod.GET)
    public void doGetNameMatching(
            @RequestParam(value="scientificName", required = true) String scientificName,
            @RequestParam(value="compareAuthor", required = false) boolean compareAuthor,
            @RequestParam(value="maxDistance", required = false) Integer maxDistance,
            HttpServletRequest request,
            HttpServletResponse response) throws NameMatchingParserException {

        logger.info("doGetNameMatching()" + request.getRequestURI());

        NameMatchingResult result = nameMatchingService.findMatchingNames(scientificName, compareAuthor, maxDistance);
        RequestedParam requestedParam = new RequestedParam(scientificName, compareAuthor, maxDistance);
        NameMatchingOutputObject outputObject = NameMatchingAdapter.invoke(result, requestedParam);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter writer = response.getWriter()){
            String jsonResponse = objectMapper.writeValueAsString(outputObject);
            writer.write(jsonResponse);
        } catch (IOException e) {
            logger.info("doGetNameMatching())" + request.getRequestURI());
        }
    }

    /**POST Request with MultipartFile allows the usage of more than one parameters. It works calling the following command:
     *
     * request with json as output:
     * curl -v -H "Accept: application/json" -X POST -F "compareAuthor=false" -F "maxDistance=2" -F "file=@test.txt" http://localhost:8082/namematch/matchingList
     *
     * request with csv as output:
     * curl -v -H "Accept: text/csv" -X POST -F "compareAuthor=false" -F "maxDistance=2" -F "file=@test.txt" http://localhost:8082/namematch/matchingList
     *
     * the option -v in curl gives more information about the http response (u.a.)
     *
     * @throws IOException
     * @throws NameMatchingParserException
    */

    @PostMapping(
            value = "matchingList")
    public void doPostNameMatching (
            @RequestPart("file") MultipartFile file,
            @RequestParam(value="compareAuthor", required = false) boolean compareAuthor,
            @RequestParam(value="maxDistance", required = false) Integer maxDistance,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException, NameMatchingParserException {

        logger.info("doPostNameMatching()" + request.getRequestURI());

        byte [] bytes = file.getBytes();
        String namesString = new String (bytes, StandardCharsets.UTF_8);
        List <String> namesList = Arrays.asList(namesString.split("\\r?\\n"));
        Map<String, NameMatchingResult> result = nameMatchingService.compareTaxonListName(namesList, compareAuthor, maxDistance);
        RequestedParam requestedParam = new RequestedParam(namesList, compareAuthor, maxDistance);
        NameMatchingOutputList outputObjectList = NameMatchingAdapter.invokeList(result, requestedParam);

        String acceptHeader = request.getHeader("Accept");

        if (acceptHeader == "*/*" || acceptHeader.equals("application/json")) {
            NameMatchingAdapter.jsonResponse(request, response, objectMapper, outputObjectList);
        } else if (acceptHeader.equals("text/csv")) {
            NameMatchingAdapter.csvResponse(request, response, outputObjectList);
        }
    }

    private static class NameMatchingAdapter {

        private static NameMatchingOutputList invokeList (Map<String, NameMatchingResult> input, RequestedParam paramteres) {
            Collections.sort(paramteres.getScientificNameList());
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

        private static void fillExactMatchesCSVRow(List<String> csvStringBuilder, NameMatchingOutputObject outputObject,
                List<NameMatchingExactResult> exactResults, int x) {

            String inputParam = outputObject.getRequest().getScientificName() + ";" +
                    String.valueOf(outputObject.getRequest().isCompareAuthor() + ";" +
                            outputObject.getRequest().getMaxDistance().toString());
            String rowContent = inputParam + ";exactMatch"
                    + ";0;"
                    + exactResults.get(x).getTaxonNameUuid() + ";"
                    + exactResults.get(x).getNameWithAuthor() + ";"
                    + exactResults.get(x).getAuthorship() + ";"
                    + exactResults.get(x).getPureName() + ";"
                    + exactResults.get(x).getTaxonNameId();
            csvStringBuilder.add(rowContent);
        }

        private static void fillCandidatesCSVRow(List<String> csvStringBuilder, NameMatchingOutputObject outputObject,
                List<NameMatchingCandidateResult> candidateResults, int x) {

            String inputParam = outputObject.getRequest().getScientificName() + ";" +
                    String.valueOf(outputObject.getRequest().isCompareAuthor() + ";" +
                            outputObject.getRequest().getMaxDistance().toString());
            String rowContent = inputParam + ";candidates;"
                    + candidateResults.get(x).getDistance() + ";"
                    + candidateResults.get(x).getTaxonNameUuid() + ";"
                    + candidateResults.get(x).getNameWithAuthor() + ";"
                    + candidateResults.get(x).getAuthorship() + ";"
                    + candidateResults.get(x).getPureName() + ";"
                    + candidateResults.get(x).getTaxonNameId();
            csvStringBuilder.add(rowContent);
        }

        private static void jsonResponse(HttpServletRequest request, HttpServletResponse response, ObjectMapper objectMapper,
                NameMatchingOutputList outputObjectList) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try (PrintWriter writer = response.getWriter()){
                String jsonResponse = objectMapper.writeValueAsString(outputObjectList);
                writer.write(jsonResponse);
            } catch (IOException e) {
                logger.info("doPostNameMatching()" + request.getRequestURI());
            }
        }

        private static void csvResponse(HttpServletRequest request, HttpServletResponse response, NameMatchingOutputList outputObjectList) throws IOException {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename= \"name_matching.csv\"");
            try (PrintWriter writer = response.getWriter();
                    CSVWriter csvWriter = new CSVWriter (writer, ';');){

                List <String> csvStringBuilder = new ArrayList<String>();
                csvWriter.writeNext(new String[]{
                        "inputName",
                        "compareAuthor",
                        "maxDistance",
                        "matchingType",
                        "retrievedDistance",
                        "taxonNameUuid",
                        "nameWithAuthorship",
                        "authorship",
                        "pureName",
                        "taxonNameId"
                        });

                for (NameMatchingOutputObject outputObject : outputObjectList.getOutputObject()) {
                    List<NameMatchingExactResult> exactResults = outputObject.getResult().getExactMatches();
                    List<NameMatchingCandidateResult> candidateResults = outputObject.getResult().getCandidates();
                    if (!exactResults.isEmpty()) {
                        for ( int x = 0 ; x < exactResults.size(); x++) {
                            fillExactMatchesCSVRow(csvStringBuilder, outputObject, exactResults, x);
                        }
                    }
                    if (!candidateResults.isEmpty()) {
                        for ( int x = 0 ; x < candidateResults.size(); x++) {
                            fillCandidatesCSVRow(csvStringBuilder, outputObject, candidateResults, x);
                        }
                    }
                }
                List <String> csvStringBuilder2 = new ArrayList<String>();
                csvStringBuilder2.addAll(csvStringBuilder);
                String [] csvRows = new String [] {};
                for (String x : csvStringBuilder2) {
                    csvRows = x.split(";");
                    csvWriter.writeNext(csvRows);
                }
            } catch (IOException e) {
                logger.info("doPostNameMatching()" + request.getRequestURI());
            }
        }
    }
}
