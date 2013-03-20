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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.Abcd206ImportConfigurator;
import eu.etaxonomy.cdm.io.specimen.excel.in.SpecimenSynthesysExcelImportConfigurator;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
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
            Double latitude, List<String> collectorName, List<String> team, Abcd206ImportConfigurator config){
        this.setLocality(termService, locality, languageIso);
        this.setCoordinates(longitude, latitude);
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

    /*
     * Set the locality for the current GatheringEvent
     * @param locality
     * @param langageIso
     */
    public void setLocality(ITermService termService, String locality, String languageIso){
        LanguageString loc;
        if (languageIso == null || termService.getLanguageByIso(languageIso) == null){
            if (languageIso != null && termService.getLanguageByIso(languageIso) == null ){
                logger.info("unknown iso used for the locality: "+languageIso);
            }
            //FIXME should be UNKNOWN
            loc = LanguageString.NewInstance(locality, Language.DEFAULT());
        }else{
            loc = LanguageString.NewInstance(locality, termService.getLanguageByIso(languageIso));
        }
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
        //create coordinates point
        if(longitude == null || latitude == null){
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
        //for proiBiosphere Quentin's data
        coordinates.setReferenceSystem(ReferenceSystem.WGS84());
        this.gatheringEvent.setExactLocation(coordinates);

    }

    public void setElevation(Integer elevation){
        this.gatheringEvent.setAbsoluteElevation(elevation);
    }

    /*
     * Add a NamedArea to the GatheringEvent
     * @param area: the NamedArea to add
     */

    public void addArea(NamedArea area){
        this.gatheringEvent.addCollectingArea(area);
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
        if (gatheringTeam != null && !gatheringTeam.isEmpty()) {
            if (gatheringTeam.indexOf("et al.") != -1 || gatheringTeam.indexOf("& al.") != -1 || gatheringTeam.indexOf(" al.") != -1){
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
        if (gatheringTeam != null && !gatheringTeam.isEmpty()) {
            if (gatheringTeam.indexOf("et al.") != -1 || gatheringTeam.indexOf("& al.") != -1 || gatheringTeam.indexOf(" al.") != -1){
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
