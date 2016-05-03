// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller.dto;

import io.swagger.annotations.Api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IPolytomousKeyService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.MultilanguageTextHelper;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.KeyStatement;
import eu.etaxonomy.cdm.model.description.PolytomousKey;
import eu.etaxonomy.cdm.model.description.PolytomousKeyNode;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.remote.controller.AbstractController;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;
import eu.etaxonomy.cdm.remote.dto.polytomouskey.AbstractLinkDto;
import eu.etaxonomy.cdm.remote.dto.polytomouskey.LinkedPolytomousKeyNodeRowDto;
import eu.etaxonomy.cdm.remote.dto.polytomouskey.PolytomousKeyNodeLinkDto;
import eu.etaxonomy.cdm.remote.dto.polytomouskey.TaxonLinkDto;
import eu.etaxonomy.cdm.remote.l10n.LocaleContext;

/**
 * @author l.morris
 * @date Feb 21, 2013
 *
 */
@Controller
@Api("polytomousKeyDTO")
@RequestMapping(value = {"/dto/polytomousKey/"})
public class PolytomousKeyNodeDtoController extends AbstractController<PolytomousKey, IPolytomousKeyService> {

    public static final Logger logger = Logger
            .getLogger(PolytomousKeyNodeDtoController.class);

    private static final List<String> KEY_INIT_STRATEGY = Arrays.asList(new String[]{
            "root.children"
    });

    private static final List<String> NODE_INIT_STRATEGY = Arrays.asList(new String[]{
            "*",
            "question.label",
            "statement.label",
            "taxon.name.$"
    });

    public PolytomousKeyNodeDtoController() {
        super();
        setInitializationStrategy(NODE_INIT_STRATEGY);
    }

    private ITaxonService taxonService;

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.remote.controller.AbstractController#setService(eu.etaxonomy.cdm.api.service.IService)
     */
    @Override
    @Autowired
    public void setService(IPolytomousKeyService service) {
        this.service = service;
    }

    @Autowired
    public void setService(ITaxonService taxonService) {
        this.taxonService = taxonService;
    }


    @RequestMapping(value = {"{uuid}/linkedStyle"}, method = RequestMethod.GET)
    public ModelAndView doLinkedStyle(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doLinkedStyle() - " + requestPathAndQuery(request));

        ModelAndView mv = new ModelAndView();
        List<LinkedPolytomousKeyNodeRowDto> polytomousKeyNodeRowList = new ArrayList<LinkedPolytomousKeyNodeRowDto>();

        List<String> nodePaths = new ArrayList<String>();
        nodePaths.add("$");//initialize all to 1 relations e.g. subkey, statement
        nodePaths.add("statement.$");
        //nodePaths.add("taxon.name.nomenclaturalReference");
        nodePaths.add("taxon.name.$");

        List<String> propertyPaths = new ArrayList<String>();
        propertyPaths.add("sources");
        propertyPaths.add("annotations");

        //PolytomousKeyListController - don't want a pager and how do we use property paths.
        //create ticket for portal implementation of key stuff so appears in roadmap.

        //TaxonBase taxon = taxonService.find(taxonUuid);
        //Pager<PolytomousKey> pager = service.findByTaxonomicScope(taxon, pagerParameters.getPageSize(), pagerParameters.getPageIndex(), null);

        //PolytomousKey key = service.findByTaxonomicScope(taxon, DEFAULT_PAGE_SIZE, DEFAULT_PAGE_SIZE, initializationStrategy);
        //request mapping /dto/polytomousKey/{uuid}/linkedStyle

        PolytomousKey key = service.loadWithNodes(uuid, propertyPaths, nodePaths);
        PolytomousKeyNode keyNode = key.getRoot();

        processPolytomousKeyNode(keyNode, polytomousKeyNodeRowList);
        logger.info("size of polytomousKeyNodeRowList - " + polytomousKeyNodeRowList.size());

        //return a List of LinkedPolytomousKeyNodeRowDto
        mv.addObject(polytomousKeyNodeRowList);
        return mv;
    }

    @RequestMapping(value = {"linkedStyle"}, method = RequestMethod.GET)
    public ModelAndView doLinkedStyleByTaxonomicScope(
            @RequestParam(value = "findByTaxonomicScope") UUID taxonUuid,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doLinkedStyle() - " + requestPathAndQuery(request));

        ModelAndView mv = new ModelAndView();
        List<LinkedPolytomousKeyNodeRowDto> polytomousKeyNodeRowList = new ArrayList<LinkedPolytomousKeyNodeRowDto>();

        List<String> nodePaths = new ArrayList<String>();
        nodePaths.add("$");//initialize all to 1 relations e.g. subkey, statement
        nodePaths.add("statement.$");
        //nodePaths.add("taxon.name.nomenclaturalReference");
        nodePaths.add("taxon.name.$");

        List<String> propertyPaths = new ArrayList<String>();
        propertyPaths.add("sources");
        propertyPaths.add("annotations");

        //PolytomousKeyListController - don't want a pager and how do we use property paths.
        //create ticket for portal implementation of key stuff so appears in roadmap.

        TaxonBase taxon = taxonService.find(taxonUuid);
        //TaxonBase taxon = taxonService.find(taxonUuid);
        logger.error("The taxon uuid " + taxonUuid + " and taxon is " + taxon);
        //Pager<PolytomousKey> pager = service.findByTaxonomicScope(taxon, pagerParameters.getPageSize(), pagerParameters.getPageIndex(), null);

        //not getting a result when I use the method findByTaxonomicScope. Maybe I'm giving it the wrong taxonUUID. Ask andreas for a taxon that works
      PagerParameters pagerParameters = new PagerParameters(pageSize, pageNumber);
      pagerParameters.normalizeAndValidate(response);


        Pager<PolytomousKey> pager = service.findByTaxonomicScope(taxon, pagerParameters.getPageSize(), pagerParameters.getPageIndex(), initializationStrategy, NODE_INIT_STRATEGY);

        List<PolytomousKey> keyList = pager.getRecords(); // a pager is returned containing 1 key but pager.getRecords is empty

        for(PolytomousKey key : keyList) {
            //request mapping /dto/polytomousKey/{uuid}/linkedStyle
            //PolytomousKey key = service.loadWithNodes(uuid, propertyPaths, nodePaths);
            PolytomousKeyNode keyNode = key.getRoot();
            processPolytomousKeyNode(keyNode, polytomousKeyNodeRowList);
            logger.info("size of polytomousKeyNodeRowList - " + polytomousKeyNodeRowList.size());
            logger.error("The key is " + key.getTitleCache());//TODO: Need to add this to the mv
            //return a List of LinkedPolytomousKeyNodeRowDto

            Map<String, Object> modelMap = new HashMap<String, Object>();
            modelMap.put("titleCache", key.getTitleCache());
            modelMap.put("records", polytomousKeyNodeRowList);

            //mv.addObject(polytomousKeyNodeRowList);
            //mv contains the list of LinkedPolytomousKeyNodeRowDto objects and the titleCache of the key.
            mv.addObject(modelMap);
            //mv.addAllObjects(modelMap);
        }
        return mv;
    }

    /**
     * @param keyNode
     */
    private void processPolytomousKeyNode(PolytomousKeyNode keyNode, List<LinkedPolytomousKeyNodeRowDto> polytomousKeyNodeRowList) {

        KeyStatement rowQuestion = null;
        Integer nodeNumber = null;
        Integer edgeNumber = null;
        Feature rowFeature = null;
        KeyStatement childStatement = null;

        int childIndex = 1;

        // Get edges of the current node.
        for(PolytomousKeyNode childKeyNode : keyNode.getChildren()){

            List<AbstractLinkDto> links = new ArrayList<AbstractLinkDto>();
            childStatement = childKeyNode.getStatement(); //is this the correct statement
            Taxon taxon = childKeyNode.getTaxon();
            UUID taxonUuid = (taxon == null) ? null : taxon.getUuid();

            // Skip node with empty statements (see below for explanation: "Special Case")
            if (childStatement == null && taxonUuid != null) {
                logger.info("Continuing - nodeNumber " + keyNode.getNodeNumber() + "Child nodeNumber " + childKeyNode.getNodeNumber());
                continue;
            }

            /*
             * Special case: Child nodes with empty statements but taxa as leaf are to treated as if
             * all those taxa where direct children of the source node.
             */
            List<PolytomousKeyNode> children = childKeyNode.getChildren();
            boolean islinkToManyTaxa = false;

            if (children != null && children.size() > 0) {

                Taxon childTaxon = children.get(0).getTaxon();
                if (childTaxon != null) {
                    islinkToManyTaxa = children.get(0).getStatement() != null && childTaxon.getUuid() == null;
                    //logger.info("islinkToManyTaxa " + islinkToManyTaxa + " node number " + keyNode.getNodeNumber());
                }
            }

            boolean islinkToTaxon = (taxonUuid == null) ? false : true;
            boolean islinkToSubKey = false;
            if (childKeyNode.getSubkey() != null)
            {
                islinkToSubKey = (childKeyNode.getSubkey().getUuid() == null) ? false : true;
            }
            boolean islinkToOtherNode = (childKeyNode.getOtherNode() == null) ? false : true;
            boolean islinkToNode = ((childKeyNode.getNodeNumber() != null) && !islinkToManyTaxa && !islinkToOtherNode) ? true : false;

            AbstractLinkDto link;

            // a PolytomousKeyNode can either link to Taxa or to Nodes
            if (islinkToManyTaxa || islinkToTaxon) {
                if (islinkToManyTaxa) {
                    for(PolytomousKeyNode child : childKeyNode.getChildren()){
                        logger.info("islinkToManyTaxa " + islinkToManyTaxa + " node number " + keyNode.getNodeNumber());
                        link = new TaxonLinkDto(child.getTaxon().getName());
                        link.setUuid(taxonUuid);
                        links.add(link);
                    }
                } else {
                    if (islinkToTaxon) {
                        logger.info("islinkToTaxon " + islinkToTaxon + " node number " + keyNode.getNodeNumber());
                        link = new TaxonLinkDto(childKeyNode.getTaxon().getName());
                        link.setUuid(childKeyNode.getTaxon().getUuid());
                        links.add(link);
                    }
                }
            } else {
                if (islinkToNode) {
                    logger.info("islinkToNode " + islinkToNode + " node number " + keyNode.getNodeNumber());
                    link = new PolytomousKeyNodeLinkDto(childKeyNode.getNodeNumber());
                    link.setUuid(childKeyNode.getUuid());
                    links.add(link);
                }
                if (islinkToOtherNode) {
                    logger.info("islinkToOtherNode " + islinkToOtherNode + " node number " + keyNode.getNodeNumber());
                    link = new PolytomousKeyNodeLinkDto(childKeyNode.getOtherNode().getNodeNumber());
                    link.setUuid(childKeyNode.getOtherNode().getUuid());
                    links.add(link);
                }
            }

            /*if (islinkToManyTaxa) {
                for(PolytomousKeyNode child : childKeyNode.getChildren()){
                    logger.info("islinkToManyTaxa " + islinkToManyTaxa + " node number " + keyNode.getNodeNumber());
                    link = new TaxonLinkDto(child.getTaxon().getName());
                    link.setUuid(taxonUuid);
                    links.add(link);
                }
            } else {
                if (islinkToTaxon) {
                    logger.info("islinkToTaxon " + islinkToTaxon + " node number " + keyNode.getNodeNumber());
                    link = new TaxonLinkDto(childKeyNode.getTaxon().getName());
                    link.setUuid(childKeyNode.getTaxon().getUuid());
                    links.add(link);
                }
            }*/
            //TODO: isLinkToSubkey

            boolean hasQuestion = false;
            if (keyNode.getQuestion() != null)
            {
                hasQuestion = (keyNode.getQuestion().getLabel() == null) ? false : true;
            }
            rowQuestion = keyNode.getQuestion();
            logger.info("rowQuestion " + rowQuestion + " node number " + keyNode.getNodeNumber());

            boolean hasFeature = (keyNode.getFeature() == null) ? false : true;
            rowFeature = keyNode.getFeature();
            logger.info("rowFeature " + rowFeature + " node number " + keyNode.getNodeNumber());

            if (childStatement != null) {
            logger.info("childStatement " + childStatement.getLabel() + " node number " + keyNode.getNodeNumber());
            }

            nodeNumber = keyNode.getNodeNumber();

            //set the parameters in the LinkedPolytomousKeyodeRowDto and add it to the list
            LinkedPolytomousKeyNodeRowDto keyRow = new LinkedPolytomousKeyNodeRowDto();
            keyRow.setKeyNodeUuid(keyNode.getUuid());
            keyRow.setEdgeNumber(new Integer(childIndex));

            List<Language> languages = LocaleContext.getLanguages();

            //if(Hibernate.isInitialized(childStatement.getLabel())){
            if (childStatement != null) {
                LanguageString label = MultilanguageTextHelper.getPreferredLanguageString(childStatement.getLabel(), languages);
                keyRow.setChildStatement(label.getText());
            }

            keyRow.setRowFeature(rowFeature);
            keyRow.setRowQuestion(rowQuestion);
            keyRow.setNodeNumber(nodeNumber);
            //set links
            keyRow.setLinks(links);
            polytomousKeyNodeRowList.add(keyRow);

            childIndex++;
        }

        //edgeNumber = new Integer(childIndex);
        //set the parameters in the LinkedPolytomousKeyodeRowDto and add it to the list
        /*LinkedPolytomousKeyNodeRowDto keyRow = new LinkedPolytomousKeyNodeRowDto();
        keyRow.setKey(keyNode);
        keyRow.setEdgeNumber(edgeNumber);
        keyRow.setChildStatement(childStatement);
        keyRow.setRowFeature(rowFeature);
        keyRow.setRowQuestion(rowQuestion);
        keyRow.setNodeNumber(nodeNumber);//isLinkToManyTaxa
        //set links
        keyRow.setLinks(links);*/

        // Recurse into child nodes.
        for(PolytomousKeyNode childKeyNode : keyNode.getChildren()) {
            processPolytomousKeyNode(childKeyNode, polytomousKeyNodeRowList);
        }

    }
}
