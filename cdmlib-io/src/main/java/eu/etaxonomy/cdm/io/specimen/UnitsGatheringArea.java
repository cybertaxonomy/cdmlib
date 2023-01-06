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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.api.service.IVocabularyService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.Abcd206ImportConfigurator;
import eu.etaxonomy.cdm.io.specimen.excel.in.SpecimenSynthesysExcelImportConfigurator;
import eu.etaxonomy.cdm.io.taxonx2013.TaxonXImportConfigurator;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.term.DefinedTermBase;
import eu.etaxonomy.cdm.model.term.OrderedTermVocabulary;
import eu.etaxonomy.cdm.model.term.TermType;
import eu.etaxonomy.cdm.model.term.TermVocabulary;
import eu.etaxonomy.cdm.persistence.query.MatchMode;

/**
 * @author p.kelbert
 * @since 20.10.2008
 */
public class UnitsGatheringArea {

    private static final Logger logger = LogManager.getLogger();

    private static final boolean DEBUG = false;

    private final ArrayList<DefinedTermBase> areas = new ArrayList<>();

    private boolean useTDWGarea = false;

    private TermVocabulary<NamedArea> continentVocabulary = null;
    private TermVocabulary<Country> countryVocabulary = null;
    private TermVocabulary<NamedArea> specimenImportAreaVocabulary = null;

    private DefinedTermBase<?> wbc;

    public UnitsGatheringArea(){
        //
    }

    public void setParams(String isoCountry, String country, ImportConfiguratorBase<?, ?> config,
            ITermService termService, IVocabularyService vocService){

        this.setCountry(isoCountry, country, config, termService, vocService);
    }

    /*
     * Constructor
     * Set a list of NamedAreas
     */
    public void setAreas(Map<String, String> namedAreaList, ImportConfiguratorBase<?, ?> config, ITermService termService, IVocabularyService vocabularyService){
        this.setAreaNames(namedAreaList, config, termService, vocabularyService);
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
    public void setAreaNames(Map<String, String> namedAreaList, ImportConfiguratorBase<?, ?> config, ITermService termService, IVocabularyService vocabularyService){

        if (DEBUG) {
            logger.info(termService.list(NamedArea.class, 0, 0, null, null));
        }

        HashSet<UUID> areaSet = new HashSet<>();

        HashMap<String, UUID> matchingTermsToUuid = new HashMap<>();
        for (java.util.Map.Entry<String, String> entry : namedAreaList.entrySet()){
            String namedAreaStr = entry.getKey();
            String namedAreaClass = entry.getValue();
            UUID areaUUID = null;
            areaUUID = getNamedAreaDecision(namedAreaStr,config);
            //first, check if there is an exact match
            List<DefinedTermBase> exactMatchingTerms = termService.findByTitleWithRestrictions(DefinedTermBase.class, namedAreaStr, MatchMode.EXACT, null, null, null, null, null).getRecords();
            if(!exactMatchingTerms.isEmpty()){
                //check for continents
                List<DefinedTermBase> exactMatchingContinentTerms = new ArrayList<>();
                if(namedAreaClass!=null && namedAreaClass.equalsIgnoreCase("continent")){
                    if (continentVocabulary == null){
                        continentVocabulary = vocabularyService.load(NamedArea.uuidContinentVocabulary);
                    }
                   Set terms = continentVocabulary.getTerms();
                   for (Object object : terms) {
                       if(object instanceof DefinedTermBase && exactMatchingTerms.contains(object)){
                           exactMatchingContinentTerms.add(HibernateProxyHelper.deproxy(object, DefinedTermBase.class));
                       }
                   }
                   if(exactMatchingContinentTerms.size()==1){
                       areaUUID = exactMatchingContinentTerms.iterator().next().getUuid();
                   }
                }else{
                    if(exactMatchingTerms.size()==1){
                        areaUUID = exactMatchingTerms.iterator().next().getUuid();
                    }
                }
            }
            if (areaUUID == null && config.isInteractWithUser()){
                Pager<DefinedTermBase> matchingTerms = termService.findByTitleWithRestrictions(DefinedTermBase.class, namedAreaStr, MatchMode.ANYWHERE, null, null, null, null, null);
                String packagePrefix = "eu.etaxonomy.cdm.model.location.";
                for (DefinedTermBase matchingTerm : matchingTerms.getRecords()) {
                    String termLabel = matchingTerm.getTitleCache();
                    if(matchingTerm.getClass().toString().contains(packagePrefix)){
                        termLabel += " ("+matchingTerm.getClass().toString().split(packagePrefix)[1] + ")";
                    }
                    matchingTermsToUuid.put(termLabel, matchingTerm.getUuid());
                }
                areaUUID = askForArea(namedAreaStr, matchingTermsToUuid, "area");
            }
            if (DEBUG) {
                logger.info("selected area: "+areaUUID);
            }
            if (areaUUID == null){
                if (namedAreaStr != null){
                    createNamedArea(config, termService, vocabularyService, namedAreaStr, namedAreaClass);
                }
            } else {
                areaSet.add(areaUUID);
                addNamedAreaDecision(namedAreaStr,areaUUID, config);
            }

        }
//        for (String areaStr:areaToAdd){
//            if (areaStr != null){
//                NamedArea ar = NamedArea.NewInstance(areaStr, areaStr, areaStr);
//                ar.setTitleCache(areaStr, true);
//
//                termService.saveOrUpdate(ar);
//                this.areas.add(ar);
//                addNamedAreaDecision(areaStr,ar.getUuid(), config);
//            }
//        }
        if (!areaSet.isEmpty()){
            List<DefinedTermBase> ldtb = termService.find(areaSet);
            if (!ldtb.isEmpty()) {
                this.areas.addAll(ldtb);
            }
        }
    }

    private void createNamedArea(ImportConfiguratorBase<?, ?> config, ITermService termService,
            IVocabularyService vocabularyService, String namedAreaStr, String namedAreaClass) {
        if (!StringUtils.isBlank(namedAreaStr)){
            NamedArea ar = NamedArea.NewInstance(namedAreaStr, namedAreaStr, namedAreaStr);
            if (namedAreaClass != null ){
                if (namedAreaClass.equalsIgnoreCase("province")){
                    ar.setLevel(NamedAreaLevel.PROVINCE());
                }
                if (namedAreaClass.equalsIgnoreCase("state")){
                    ar.setLevel(NamedAreaLevel.STATE());
                }
                if (namedAreaClass.equalsIgnoreCase("departmenet")){
                    ar.setLevel(NamedAreaLevel.DEPARTMENT());
                }
                if (namedAreaClass.equalsIgnoreCase("town")){
                    ar.setLevel(NamedAreaLevel.TOWN());
                }
                if (namedAreaClass.equalsIgnoreCase("country")){
                    ar.setLevel(NamedAreaLevel.COUNTRY());
                }
                if (namedAreaClass.equalsIgnoreCase("nature_reserve")){
                    ar.setLevel(NamedAreaLevel.NATURE_RESERVE());
                }
            }


            ar.setTitleCache(namedAreaStr, true);
            if (specimenImportAreaVocabulary == null){
                specimenImportAreaVocabulary = vocabularyService.load(CdmImportBase.uuidUserDefinedNamedAreaVocabulary);
                if (specimenImportAreaVocabulary == null){
                    specimenImportAreaVocabulary = OrderedTermVocabulary.NewOrderedInstance(TermType.NamedArea, NamedArea.class, "User defined vocabulary for named areas", "User Defined Named Areas", null, null);
                    specimenImportAreaVocabulary.setUuid(CdmImportBase.uuidUserDefinedNamedAreaVocabulary);
                    specimenImportAreaVocabulary = vocabularyService.save(specimenImportAreaVocabulary);
                }
            }
            DefinedTermBase<?> term =  specimenImportAreaVocabulary.getTermByIdInvocabulary(namedAreaStr);
            if (term == null){
                specimenImportAreaVocabulary.addTerm(ar);
                termService.saveOrUpdate(ar);
                this.areas.add(ar);
            }else{
                this.areas.add(term);
            }
            addNamedAreaDecision(namedAreaStr,ar.getUuid(), config);
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
    public void setCountry(String iso, String fullName, ImportConfiguratorBase<?, ?> config,
            ITermService termService, IVocabularyService vocService){


        if (!StringUtils.isEmpty(iso)){
        	try {
        		wbc = termService.getCountryByIso(iso);
        	}catch(NullPointerException e) {
        		wbc = null;
        	}
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
                	Pager<Country> countryList;
                	try {
                		countryList = termService.findByRepresentationText(fullName, Country.class, 100, 0);
                	}catch(NullPointerException e) {
                		countryList = null;
                	}
                	if (countryList != null) {
	                	for (NamedArea na:countryList.getRecords()){
		                   	if (na.getTitleCache().equalsIgnoreCase(fullName)) {
		                   		countryUuids.add(na.getUuid());
		                   	}
			                if (na.getTitleCache().toLowerCase().indexOf(fullName.toLowerCase()) != -1) {
			                	matchingTerms.put(na.getTitleCache()+" ("+na.getTermType().getLabel() + ")",na.getUuid());
			                }
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
                    createNamedArea(config, termService, vocService, fullName, "country");
                    NamedArea ar = NamedArea.NewInstance(fullName, fullName, null);
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

    public void useTDWGareas(boolean useTDWGarea) {
        this.useTDWGarea=useTDWGarea;
    }

    public DefinedTermBase<?> getCountry() {
        return wbc;
    }
}