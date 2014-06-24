/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.specimen;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.Abcd206ImportConfigurator;
import eu.etaxonomy.cdm.io.specimen.excel.in.SpecimenSynthesysExcelImportConfigurator;
import eu.etaxonomy.cdm.io.taxonx2013.TaxonXImportConfigurator;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;

/**
 * @author p.kelbert
 * @created 20.10.2008
 */
public class UnitsGatheringArea {
    private static final Logger logger = Logger.getLogger(UnitsGatheringArea.class);

    private static final boolean DEBUG = false;
    private final ArrayList<DefinedTermBase> areas = new ArrayList<DefinedTermBase>();
    private boolean useTDWGarea = false;

    private DefinedTermBase<?> wbc;


    public UnitsGatheringArea(){
        //
    }

    public void setParams(String isoCountry, String country, ImportConfiguratorBase<?, ?> config, ITermService termService,
            IOccurrenceService occurrenceService){

        this.setCountry(isoCountry, country, config, termService, occurrenceService);
    }

    /*
     * Constructor
     * Set a list of NamedAreas
     */
    public void setAreas(List<String> namedAreaList, ImportConfiguratorBase<?, ?> config, ITermService termService ){
        this.setAreaNames(namedAreaList, config, termService);
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
    public void setAreaNames(List<String> namedAreas, ImportConfiguratorBase<?, ?> config, ITermService termService){
        List<NamedArea> termsList = termService.list(NamedArea.class,0,0,null,null);
        termsList.addAll(termService.list(Country.class, 0, 0, null, null));

        if (DEBUG) {
            logger.info(termService.list(NamedArea.class, 0, 0, null, null));
        }

        HashSet<String> areaToAdd= new HashSet<String>();
        HashSet<UUID> areaSet = new HashSet<UUID>();

        HashMap<String, UUID> matchingTerms = new HashMap<String, UUID>();
        for (String namedAreaStr : namedAreas){
            for (DefinedTermBase na:termsList){
                if (na.getTitleCache().toLowerCase().indexOf(namedAreaStr.toLowerCase()) != -1) {
                    if (na.getClass().toString().indexOf("eu.etaxonomy.cdm.model.location.") != -1) {
                        matchingTerms.put(na.getTitleCache()+" ("+na.getClass().toString().split("eu.etaxonomy.cdm.model.location.")[1]+")",na.getUuid());
                    }
                }
            }
            //            logger.info("matchingterms: "+matchingTerms.keySet().toString());
            UUID areaUUID = null;
            areaUUID = getNamedAreaDecision(namedAreaStr,config);

            if (areaUUID == null && config.isInteractWithUser()){
                areaUUID = askForArea(namedAreaStr, matchingTerms, "area");
            }
            if (DEBUG) {
                logger.info("selected area: "+areaUUID);
            }
            if (areaUUID == null){
                areaToAdd.add(namedAreaStr);
            } else {
                areaSet.add(areaUUID);
                addNamedAreaDecision(namedAreaStr,areaUUID, config);
            }

        }
        for (String areaStr:areaToAdd){
            NamedArea ar = NamedArea.NewInstance();
            ar.setTitleCache(areaStr, true);
            termService.saveOrUpdate(ar);
            this.areas.add(ar);
            addNamedAreaDecision(areaStr,ar.getUuid(), config);
        }
        if (!areaSet.isEmpty()){
            List<DefinedTermBase> ldtb = termService.find(areaSet);
            if (!ldtb.isEmpty()) {
                this.areas.addAll(ldtb);
            }
        }
    }

    private UUID askForArea(String namedAreaStr, HashMap<String, UUID> matchingTerms, String areaType){
//        matchingTerms.put("Nothing matches, create a new area",null);
        
        //FIXME names with same label will not make it to the map
        JTextArea textArea = new JTextArea("Several CDM-areas could match the current '"+namedAreaStr+"'");
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize( new Dimension( 700, 50 ) );
        String s=null;
        List<String> list = new ArrayList<String>(matchingTerms.keySet());
        list.add("Nothing matches, create a new area");
        
        if (list.size() <= 1){
        	return null;
        }
        while (s == null) {
            s= (String)JOptionPane.showInputDialog(
                    null,
                    scrollPane,
                    "Select the right " + areaType + " from the list",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    list.toArray(),
                    null);
        }

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
    public void setCountry(String iso, String fullName, ImportConfiguratorBase<?, ?> config, ITermService termService,
            IOccurrenceService occurrenceService){
  
       
        if (!StringUtils.isEmpty(iso)){
            wbc = occurrenceService.getCountryByIso(iso);
        }
        if (wbc == null){
            if (!StringUtils.isEmpty(fullName)){


                //                logger.info("matchingterms: "+matchingTerms.keySet().toString());
                UUID areaUUID = null;
                //TODO Critical, should be a country decision
                areaUUID = getNamedAreaDecision(fullName,config);
                 
                if (areaUUID == null){
                	List<UUID> countryUuids = new ArrayList<UUID>();
                	HashMap<String, UUID> matchingTerms = new HashMap<String, UUID>();
                		 
                	List<Country> countryList = termService.list(Country.class, 0, 0, null, null);
                	for (NamedArea na:countryList){
	                   	if (na.getTitleCache().equalsIgnoreCase(fullName)) {
	                   		countryUuids.add(na.getUuid());
	                   	}
		                if (na.getTitleCache().toLowerCase().indexOf(fullName.toLowerCase()) != -1) {
		                	matchingTerms.put(na.getTitleCache()+" ("+na.getTermType().getMessage() + ")",na.getUuid());
		                }
	                }
                	if (countryUuids.isEmpty()){
                		List<NamedArea> namedAreaList = termService.list(NamedArea.class,0,0,null,null);
                		
                		for (NamedArea na:namedAreaList){
                			if (! na.getClass().isAssignableFrom(Country.class) && na.getTitleCache().toLowerCase().indexOf(fullName.toLowerCase()) != -1) {
                				matchingTerms.put(na.getTitleCache()+" ("+na.getType().getLabel() + ")",na.getUuid());
                			}
                		}
                	}
                	if (countryUuids.size() == 1){
                		areaUUID = countryUuids.get(0);
                	}else{
                    	if ((matchingTerms.keySet().size()>0) && config.isInteractWithUser()){
                    		areaUUID = askForArea(fullName, matchingTerms, "country");
                    		logger.info("selected area: "+areaUUID);
                    	}else{
                    		logger.warn("Non interaction not yet implemented correctly");
                    	}
                	}
                    
                }
                if (areaUUID == null){
                    NamedArea ar = NamedArea.NewInstance();
                    //FIXME add vocabulary
                    logger.warn("Vocabulary not yet set for new country");
                    ar.setTitleCache(fullName, true);
                    termService.saveOrUpdate(ar);
                    wbc = ar;
                } else {
                    wbc = termService.find(areaUUID);
                }
                addNamedAreaDecision(fullName,wbc.getUuid(),config);
            }
        }
    }

    /**
     * @param fullName
     * @param uuid
     */
    private void addNamedAreaDecision(String fullName, UUID uuid,ImportConfiguratorBase<?, ?> config) {
        if (config.getClass().equals(SpecimenSynthesysExcelImportConfigurator.class)) {
            ((SpecimenSynthesysExcelImportConfigurator) config).putNamedAreaDecision(fullName, uuid);
        }
        if (config.getClass().equals(Abcd206ImportConfigurator.class)) {
            ((Abcd206ImportConfigurator) config).putNamedAreaDecision(fullName, uuid);
        }
        if (config.getClass().equals(TaxonXImportConfigurator.class)) {
            ((TaxonXImportConfigurator) config).putNamedAreaDecision(fullName, uuid);
        }

    }

    /**
     * @param fullName
     * @return
     */
    private UUID getNamedAreaDecision(String fullName, ImportConfiguratorBase<?, ?> config) {
        UUID areaUUID = null;
        if (config.getClass().equals(SpecimenSynthesysExcelImportConfigurator.class)) {
            areaUUID = ((SpecimenSynthesysExcelImportConfigurator) config).getNamedAreaDecision(fullName);
        }
        if (config.getClass().equals(Abcd206ImportConfigurator.class)) {
            areaUUID = ((Abcd206ImportConfigurator) config).getNamedAreaDecision(fullName);
        }
        if (config.getClass().equals(TaxonXImportConfigurator.class)) {
            areaUUID = ((TaxonXImportConfigurator) config).getNamedAreaDecision(fullName);
        }
        return areaUUID;
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
