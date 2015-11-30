/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.specimen;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IAgentService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.Abcd206ImportConfigurator;
import eu.etaxonomy.cdm.io.specimen.excel.in.SpecimenSynthesysExcelImportConfigurator;
import eu.etaxonomy.cdm.io.taxonx2013.TaxonXImportConfigurator;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.Point;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;

/**
 * @author p.kelbert
 * @created 20.10.2008
 * @version 1.0
 */
public class UnitsGatheringEvent {

    private static final Logger logger = Logger.getLogger(UnitsGatheringEvent.class);
    private static final boolean DEBUG = false;
    private final GatheringEvent gatheringEvent = GatheringEvent.NewInstance();

    /*
     * Constructor
     * Fill in the locality, coordinates and the collector(s) for the current GatheringEvent
     * @param app: the CDM Application Controller
     * @param locality
     * @param languageIso
     * @param longitude
     * @param latitude
     * @param collectorNames
     */
    public UnitsGatheringEvent(ITermService termService, String locality, String languageIso, Double longitude,
            Double latitude, String collectorName, String team, SpecimenSynthesysExcelImportConfigurator config){
        this.setLocality(termService, locality, languageIso);
        this.setCoordinates(longitude, latitude);
        if (!collectorName.isEmpty()) {
            this.setCollector(collectorName, config);
        }
        if (!team.isEmpty()) {
            this.setTeam(team, config);
        }
    }
    //
    public UnitsGatheringEvent(ITermService termService, String locality, String collectorName, Double longitude,
            Double latitude, TaxonXImportConfigurator config,IAgentService agentService){
        if (!StringUtils.isEmpty(locality)) {
            this.setLocality(termService, locality, null);
        }
        this.setCoordinates(longitude, latitude);
        if (!StringUtils.isEmpty(collectorName)) {
            this.setCollector(collectorName, config, agentService);
        }
        //        if (!team.isEmpty()) {
        //            this.setTeam(team, config);
        //        }
    }

    /*
     * Constructor
     * Fill in the locality, coordinates and the collector(s) for the current GatheringEvent
     * @param app: the CDM Application Controller
     * @param locality
     * @param languageIso
     * @param longitude
     * @param latitude
     * @param collectorNames
     */
    public UnitsGatheringEvent(ITermService termService, String locality, String languageIso, Double longitude,
            Double latitude, String elevationText, String elevationMin, String elevationMax, String elevationUnit,
            String date, String gatheringNotes, ReferenceSystem referenceSystem, List<String> collectorName,
            List<String> team, Abcd206ImportConfigurator config) {
        this.setLocality(termService, locality, languageIso);
        this.setCoordinates(longitude, latitude, referenceSystem);
        this.setDate(date);
        this.setNotes(gatheringNotes);
        this.setElevation(elevationText, elevationMin, elevationMax, elevationUnit);
        if (!collectorName.isEmpty()) {
            List<String> tmp =  new ArrayList<String>(new HashSet<String>(collectorName));
            this.setCollector(tmp.get(0), config);
        }
        if (!team.isEmpty()) {
            List<String> tmpTeam = new ArrayList<String>(new HashSet<String>(team));
            this.setTeam(StringUtils.join(tmpTeam," & "), config);
        }
    }

    public GatheringEvent getGatheringEvent(){
        return this.gatheringEvent;
    }

    /**
     * Set the locality for the current GatheringEvent
     * @param locality
     * @param langageIso
     */
    public void setLocality(ITermService termService, String locality, String languageIso){
        LanguageString loc = null;
        List<LanguageString> languages = termService.getAllLanguageStrings(0, 0);
        boolean locFound=false;
        if ((languageIso == null) || (termService.getLanguageByIso(languageIso) == null)){
            //            if (languageIso != null && termService.getLanguageByIso(languageIso) == null ){
            //                logger.info("unknown iso used for the locality: "+languageIso);
            //            }
            for (LanguageString ls:languages){
                if (ls.getText().equalsIgnoreCase(locality)){
                    loc=ls;
                    locFound=true;
                    //                    System.out.println("REUSE LOCALITY");
                }
            }
            if (!locFound){
                loc = LanguageString.NewInstance(locality, Language.DEFAULT());
                termService.saveLanguageData(loc);
                languages.add(loc);
            }
        }else{
            for (LanguageString ls:languages){
                if (ls.getText().equalsIgnoreCase(locality) && ls.getLanguage().equals(termService.getLanguageByIso(languageIso))){
                    loc=ls;
                    locFound=true;
                    //                    System.out.println("REUSE LOCALITY");
                }
            }
            if (!locFound) {
                loc = LanguageString.NewInstance(locality, termService.getLanguageByIso(languageIso));
                termService.saveLanguageData(loc);
                languages.add(loc);
            }
        }
        if (loc == null){logger.warn("PROBLEM LOCALITY");}
        this.gatheringEvent.setLocality(loc);
    }

    /*
     * return the locality associated to the GatheringEvent
     */
    public LanguageString getLocality(){
        return this.gatheringEvent.getLocality();
    }

    /*
     * Set the coordinates for the current GatheringEvent
     * @param: longitude
     * @param: latitude
     */
    public void setCoordinates(Double longitude, Double latitude){
        setCoordinates(longitude, latitude, null);
    }

    public void setCoordinates(Double longitude, Double latitude, ReferenceSystem referenceSystem){
        //create coordinates point
        if((longitude == null) || (latitude == null)){
            return;
        }
        Point coordinates = Point.NewInstance();
        //add coordinates
        if (longitude != 0.0) {
            coordinates.setLongitude(longitude);
        }
        if (latitude != 0.0) {
            coordinates.setLatitude(latitude);
        }
        coordinates.setReferenceSystem(referenceSystem);
        this.gatheringEvent.setExactLocation(coordinates);

    }

    public void setNotes(String gatheringNotes){
        this.gatheringEvent.addAnnotation(Annotation.NewDefaultLanguageInstance(gatheringNotes));
    }

    public void setDate(String date){
        TimePeriod timeperiod = this.gatheringEvent.getTimeperiod();
        if(timeperiod==null){
            timeperiod = TimePeriod.NewInstance();
            this.gatheringEvent.setTimeperiod(timeperiod);
        }
        timeperiod.setFreeText(date);
    }

    public void setElevation(String elevationText, String elevationMin, String elevationMax, String elevationUnit){
        if(elevationText!=null){
            this.gatheringEvent.setAbsoluteElevationText(elevationText);
        }
        else{
            //TODO check for unit at string end
            String pattern = "\\D";// regex for non-digits
            if(elevationMin!=null){
                Integer min = Integer.parseInt(elevationMin.replaceAll(pattern, ""));
                this.gatheringEvent.setAbsoluteElevation(min);
            }
            if(elevationMax!=null){
                Integer max = Integer.parseInt(elevationMax.replaceAll(pattern, ""));
                this.gatheringEvent.setAbsoluteElevationMax(max);
            }
            if(elevationUnit!=null){
                if(!elevationUnit.equals("m")){
                    //TODO convert if necessary
                }
            }
        }
    }

    /*
     * Add a NamedArea to the GatheringEvent
     * @param area: the NamedArea to add
     */

    public void addArea(DefinedTermBase area){
        if (area.isInstanceOf(NamedArea.class)) {
            this.gatheringEvent.addCollectingArea((NamedArea) area);
        } else {
            logger.info("OUPPPPSS :"+area.getClass());
        }
    }



    /*
     * Create a new collector or collector's team
     * @param: collectorNames: the list of names to add as collector/collectorTeam
     * USED - create each time a new Collector
     */
    public void setCollector(String collectorName, SpecimenSynthesysExcelImportConfigurator config){
        //        System.out.println("collectors : "+collectorNames.toString());
        Person collector;
        collector = Person.NewInstance();
        collector.setTitleCache(collectorName, true);
        if (DEBUG) {
            System.out.println("getcoll:"+config.getPersons().get(collector.getTitleCache()));
        }
        this.gatheringEvent.setCollector(config.getPersons().get(collector.getTitleCache()));

    }

    /*
     * Create a new collector or collector's team
     * @param: collectorNames: the list of names to add as collector/collectorTeam
     * USED - create each time a new Collector
     */
    public void setCollector(String collectorName, TaxonXImportConfigurator config, IAgentService agentService){
        //        System.out.println("collectors : "+collectorNames.toString());
        Person collector;
        collector = Person.NewInstance();
        collector.setTitleCache(collectorName, true);
        Person collector_db = config.getPersons().get(collector.getTitleCache());
        if (collector_db == null) {
            UUID uuid = agentService.saveOrUpdate(collector);
            collector_db=(Person) agentService.find(uuid);
        }
        this.gatheringEvent.setCollector(collector_db);

    }


    /*
     * Create a new collector or collector's team
     * @param: collectorNames: the list of names to add as collector/collectorTeam
     * USED - create each time a new Collector
     */
    public void setCollector(String collectorName, Abcd206ImportConfigurator config){
        //        System.out.println("collectors : "+collectorNames.toString());
        Person collector;
        collector = Person.NewInstance();
        collector.setTitleCache(collectorName, true);
        if (DEBUG) {
            System.out.println("getcoll:"+config.getPersons().get(collector.getTitleCache()));
        }
        this.gatheringEvent.setCollector(config.getPersons().get(collector.getTitleCache()));
    }

    /**
     * @param tp
     */
    public void setGatheringDate(TimePeriod tp) {
        this.gatheringEvent.setTimeperiod(tp);
    }

    /**
     * @param gatheringTeam
     */
    public void setTeam(String gatheringTeam, SpecimenSynthesysExcelImportConfigurator config) {
        Team t = new Team();
        if ((gatheringTeam != null) && !gatheringTeam.isEmpty()) {
            if ((gatheringTeam.indexOf("et al.") != -1) || (gatheringTeam.indexOf("& al.") != -1) || (gatheringTeam.indexOf(" al.") != -1)){
                t.setTitleCache(gatheringTeam);
            } else{
                String[] tmp1 = gatheringTeam.split(" et ");
                for (String elt:tmp1){
                    String tmp2[] = elt.split("&");
                    for (String elt2:tmp2) {
                        if (!elt2.trim().isEmpty()) {
                            Person p = Person.NewInstance();
                            p.setTitleCache(elt2);
                            t.addTeamMember(p);
                        }
                    }
                }
            }
        }
        if (DEBUG) {
            System.out.println("getteam:"+config.getTeams().get(t.getTitleCache()));
        }
        this.gatheringEvent.setCollector(config.getTeams().get(t.getTitleCache()));
    }

    /**
     * @param gatheringTeam
     */
    public void setTeam(String gatheringTeam, Abcd206ImportConfigurator config) {
        Team t = new Team();
        if ((gatheringTeam != null) && !gatheringTeam.isEmpty()) {
            if ((gatheringTeam.indexOf("et al.") != -1) || (gatheringTeam.indexOf("& al.") != -1) || (gatheringTeam.indexOf(" al.") != -1)){
                t.setTitleCache(gatheringTeam);
            } else{
                String[] tmp1 = gatheringTeam.split(" et ");
                for (String elt:tmp1){
                    String tmp2[] = elt.split("&");
                    for (String elt2:tmp2) {
                        if (!elt2.trim().isEmpty()) {
                            Person p = Person.NewInstance();
                            p.setTitleCache(elt2);
                            t.addTeamMember(p);
                        }
                    }
                }
            }
        }
        if (DEBUG) {
            System.out.println("getteam:"+config.getTeams().get(t.getTitleCache()));
        }
        this.gatheringEvent.setCollector(config.getTeams().get(t.getTitleCache()));
    }


}
