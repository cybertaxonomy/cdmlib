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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.location.Continent;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;

/**
 * @author p.kelbert
 * @created 20.10.2008
 * @version 1.0
 */
public class UnitsGatheringArea {

    private final ArrayList<DefinedTermBase> areas = new ArrayList<DefinedTermBase>();
    private boolean useTDWGarea = false;
    private ITermService termService;
    private IOccurrenceService occurrenceService;
    private DefinedTermBase<?> wbc = null;
    Logger logger = Logger.getLogger(this.getClass());


    /*
     * Constructor
     * Set/create country
     * @param isoCountry (try to used the isocode first)
     * @param country
     * @param app
     */
    public UnitsGatheringArea(String isoCountry, String country, IOccurrenceService occurrenceService, ITermService termService){
        this.termService = termService;
        this.occurrenceService = occurrenceService;
        this.setCountry(isoCountry, country);
    }

    public UnitsGatheringArea(){

    }

    public void setParams(String isoCountry, String country, IOccurrenceService occurrenceService, ITermService termService){
        this.termService = termService;
        this.occurrenceService = occurrenceService;
        this.setCountry(isoCountry, country);
    }

    /*
     * Constructor
     * Set a list of NamedAreas
     */
    public UnitsGatheringArea(List<String> namedAreaList,ITermService termService ){
        this.termService = termService;
        this.setAreaNames(namedAreaList);
    }


    /*
     * Return the current list of NamedAreas
     */
    public ArrayList<DefinedTermBase> getAreas(){
        return this.areas;
    }

    /*
     * Set the list of NamedAreas
     * @param namedAreas
     */
    @SuppressWarnings("rawtypes")
    public void setAreaNames(List<String> namedAreas){
        List<DefinedTermBase> termsList = termService.list(NamedArea.class,0,0,null,null);
        termsList.addAll(termService.list(Continent.class, 0, 0, null, null));
        termsList.addAll(termService.list(WaterbodyOrCountry.class, 0, 0, null, null));

        logger.info(termService.list(Continent.class, 0, 0, null, null));

        HashSet<String> areaToAdd= new HashSet<String>();
        HashSet<UUID> areaSet = new HashSet<UUID>();

        HashMap<String, UUID> matchingTerms = new HashMap<String, UUID>();
        for (String namedAreaStr : namedAreas){
            for (DefinedTermBase na:termsList){
                if (na.getTitleCache().toLowerCase().indexOf(namedAreaStr.toLowerCase()) != -1) {
                    if (na.getClass().toString().indexOf("eu.etaxonomy.cdm.model.location.") != -1) {
                        matchingTerms.put(na.toString()+" ("+na.getClass().toString().split("eu.etaxonomy.cdm.model.location.")[1]+")",na.getUuid());
                    }
                }
            }
            logger.info("matchingterms: "+matchingTerms.keySet().toString());
            UUID areaUUID = askForArea(namedAreaStr, matchingTerms);
            logger.info("selected area: "+areaUUID);
            if (areaUUID == null){
                areaToAdd.add(namedAreaStr);
            } else {
                areaSet.add(areaUUID);
            }

        }
        for (String areaStr:areaToAdd){
            NamedArea ar = NamedArea.NewInstance();
            ar.setTitleCache(areaStr, true);
            termService.saveOrUpdate(ar);
            this.areas.add(ar);
        }
        if (!areaSet.isEmpty()){
            List<DefinedTermBase> ldtb = termService.find(areaSet);
            if (!ldtb.isEmpty()) {
                this.areas.addAll(ldtb);
            }
        }
    }

    private UUID askForArea(String namedAreaStr, HashMap<String, UUID> matchingTerms){
        matchingTerms.put("Nothing matches, create a new area",null);
    JFrame frame = new JFrame("I have a question");
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    String s = (String)JOptionPane.showInputDialog(
            frame,
            "Several CDM-areas could match the current '"+namedAreaStr,
            "Select the right one from the list",
            JOptionPane.QUESTION_MESSAGE,
            null,
            matchingTerms.keySet().toArray(),
            null);

    return matchingTerms.get(s);
    }

    /*
     * Set the current Country
     * Search in the DB if the isoCode is known
     * If not, search if the country name is in the DB
     * If not, create a new Label with the Level Country
     * @param iso: the country iso code
     * @param fullName: the country's full name
     * @param app: the CDM application controller
     */
    public void setCountry(String iso, String fullName){
        List<DefinedTermBase> termsList = termService.list(NamedArea.class,0,0,null,null);
        termsList.addAll(termService.list(WaterbodyOrCountry.class, 0, 0, null, null));

        HashMap<String, UUID> matchingTerms = new HashMap<String, UUID>();

        if (iso != null & !iso.isEmpty()){
            wbc = occurrenceService.getCountryByIso(iso);
        }
        if (wbc == null){
            if (fullName != null && !fullName.isEmpty()){

                    for (DefinedTermBase<?> na:termsList){
                        if (na.getTitleCache().toLowerCase().indexOf(fullName.toLowerCase()) != -1) {
                            if (na.getClass().toString().indexOf("eu.etaxonomy.cdm.model.location.") != -1) {
                                matchingTerms.put(na.toString()+" ("+na.getClass().toString().split("eu.etaxonomy.cdm.model.location.")[1]+")",na.getUuid());
                            }
                        }
                    }
                    logger.info("matchingterms: "+matchingTerms.keySet().toString());
                    UUID areaUUID = askForArea(fullName, matchingTerms);
                    logger.info("selected area: "+areaUUID);
                    if (areaUUID == null){
                        NamedArea ar = NamedArea.NewInstance();
                        ar.setTitleCache(fullName, true);
                        termService.saveOrUpdate(ar);
                        wbc = ar;
                    } else {
                        wbc = termService.find(areaUUID);
                    }
            }
        }
    }

    /**
     * @param useTDWGarea2
     */
    public void useTDWGareas(boolean useTDWGarea) {
        this.useTDWGarea=useTDWGarea;

    }

    /**
     * @return
     */
    public DefinedTermBase<?> getCountry() {
        return wbc;
    }

}
