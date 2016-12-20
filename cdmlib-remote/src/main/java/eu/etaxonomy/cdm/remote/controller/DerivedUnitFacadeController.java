/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import io.swagger.annotations.Api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacadeNotSupportedException;
import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;

/**
 * @author a.kohlbecker
 * @date 28.06.2010
 *
 */
@Controller
@Api("derivedUnitFacade")
@RequestMapping(value = {"/derivedUnitFacade/{uuid}"})
public class DerivedUnitFacadeController extends AbstractController<SpecimenOrObservationBase, IOccurrenceService>{


    private IOccurrenceService service;

    private final List<String> ocurrenceInitSrategy = Arrays.asList(new String []{
            "$",
            "titleCache"
    });

    public DerivedUnitFacadeController(){
        setInitializationStrategy(ocurrenceInitSrategy);
    }

    @Override
    @Autowired
    public void setService(IOccurrenceService service) {
        this.service = service;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(UUID.class, new UUIDPropertyEditor());
    }

    @RequestMapping(method = RequestMethod.GET)
    public DerivedUnitFacade doGet(
            @PathVariable("uuid") UUID occurrenceUuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("getGet() - " + request.getRequestURI());
        DerivedUnitFacade duf = newFacadeFrom(occurrenceUuid, response, null);
        return duf;
    }

    @RequestMapping(value = {"derivedUnitMedia"}, method = RequestMethod.GET)
    public ModelAndView doGetDerivedUnitMedia(
        @PathVariable("uuid") UUID occurrenceUuid,
        HttpServletRequest request,
        HttpServletResponse response) throws IOException {

        logger.info("doGetDerivedUnitMedia() - " + request.getRequestURI());
        ModelAndView mv = new ModelAndView();
        DerivedUnitFacade duf = newFacadeFrom(occurrenceUuid, response,Arrays.asList(new String []{
                "derivedUnitMedia", "derivedUnitMedia.title"}));
        if(duf != null){
            mv.addObject(duf.getDerivedUnitMedia());
        }
        return mv;
    }

    @RequestMapping(value = {"fieldObjectMedia"}, method = RequestMethod.GET)
    public ModelAndView doGetFieldObjectMedia(
        @PathVariable("uuid") UUID occurrenceUuid,
        HttpServletRequest request,
        HttpServletResponse response) throws IOException {

        logger.info("doGetFieldObjectMedia() - " + request.getRequestURI());
        ModelAndView mv = new ModelAndView();
        DerivedUnitFacade duf = newFacadeFrom(occurrenceUuid, response,Arrays.asList(new String []{
                "fieldObjectMedia", "fieldObjectMedia.title"}));
        mv.addObject(duf.getFieldObjectMedia());
        return mv;
    }

// TODO
    //@RequestMapping(method = RequestMethod.GET, value = "{uuid}/collectingareas")
        @RequestMapping(
            value = {"collectingareas"},
            method = RequestMethod.GET)
    public Object doGetCollectingAreas(
            @PathVariable("uuid") UUID occurrenceUuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doGetCollectingAreas() - " + request.getRequestURI());
        DerivedUnitFacade duf = newFacadeFrom(occurrenceUuid,
                response,
                Arrays.asList(new String []{"ecology"}));
        return duf.getCollectingAreas();
    }

    @RequestMapping(method = RequestMethod.GET, value = "collection")
    public Object doGetCollection(
            @PathVariable("uuid") UUID uuid,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        logger.info("doGetCollection() - " + request.getRequestURI());
        DerivedUnitFacade duf = newFacadeFrom(uuid,
                response,
                Arrays.asList(new String []{"collection"}));
        return duf.getCollection();
    }


    //TODO:
    // public Point getExactLocation() => valueProcessor?


    // public Collection getCollection() {
    // public AgentBase getCollector() {
    // public DerivedUnit getDerivedUnit() {
    // public Map<Language, LanguageString> getDerivedUnitDefinitions(){
    // public List<Media> getDerivedUnitMedia() {
    // public Set<DeterminationEvent> getDeterminations() {
    // public Set<Specimen> getDuplicates(){
    // public Map<Language, LanguageString> getEcologyAll(){
    // public Map<Language, LanguageString> getFieldObjectDefinition() {
    // public List<Media> getFieldObjectMedia() {
    // public FieldUnit getFieldUnit(){
    // public GatheringEvent getGatheringEvent() {
    // public String getGatheringEventDescription() {
    // public Map<Language, LanguageString> getPlantDescriptionAll(){ ==> representation !!
    // public PreservationMethod getPreservationMethod()
    // public Set<IdentifiableSource> getSources(){
    // public TaxonNameBase getStoredUnder() {


    /**
     * @param occurrenceUuid
     * @param response
     * @param extendedInitStrategy
     * @return the requesed <code>DerivedUnitFacade</code> instance or <code>null</code>
     * @throws IOException
     */
    private DerivedUnitFacade newFacadeFrom(UUID occurrenceUuid, HttpServletResponse response, List<String> extendedInitStrategy)
    throws IOException {
        List<String> initStrategy = new ArrayList<String>(initializationStrategy);
        if(extendedInitStrategy != null && extendedInitStrategy.size() > 0){
            initStrategy.addAll(extendedInitStrategy);
        }
        SpecimenOrObservationBase<?> sob = service.load(occurrenceUuid, null);
        if(sob instanceof DerivedUnit){
            try {
                return service.getDerivedUnitFacade(((DerivedUnit)sob), initStrategy);
            } catch (DerivedUnitFacadeNotSupportedException e) {
                logger.error(e); //TODO ...
            }
        } else {
            HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
        }
        return null;
    }











}
