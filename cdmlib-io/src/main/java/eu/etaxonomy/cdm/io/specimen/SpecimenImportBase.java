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
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.api.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.api.service.config.FindOccurrencesConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.SpecimenImportReport;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.strategy.parser.ParserProblem;


/**
 * @author p.kelbert
 * @created 20.10.2008
 */
public abstract class SpecimenImportBase<CONFIG extends IImportConfigurator, STATE extends SpecimenImportStateBase>  extends CdmImportBase<CONFIG, STATE> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SpecimenImportBase.class);
	protected final boolean DEBUG = true;

	@Override
    protected abstract void doInvoke(STATE state);

	/**
	 * Handle a single unit
	 * @param state
	 * @param item
	 */
	@SuppressWarnings("rawtypes")
	protected abstract void handleSingleUnit(STATE state, Object item) ;



	protected TaxonNameBase<?, ?> getOrCreateTaxonName(String scientificName, Rank rank, boolean preferredFlag, STATE state, int unitIndexInAbcdFile){
        TaxonNameBase<?, ?> taxonName = null;
        SpecimenImportConfiguratorBase config = (SpecimenImportConfiguratorBase) state.getConfig();

        //check atomised name data for rank
        //new name will be created
        NonViralName<?> atomisedTaxonName = null;
        if (rank==null && unitIndexInAbcdFile>=0 && (state.getDataHolder().getAtomisedIdentificationList() != null || state.getDataHolder().getAtomisedIdentificationList().size() > 0)) {
            atomisedTaxonName = setTaxonNameByType(state.getDataHolder().getAtomisedIdentificationList().get(unitIndexInAbcdFile), scientificName, state);
            if(atomisedTaxonName!=null){
                rank = atomisedTaxonName.getRank();
            }
        }
        if(config.isReuseExistingTaxaWhenPossible()){
            NonViralName<?> parsedName = atomisedTaxonName;
            if(parsedName==null){
                parsedName = parseScientificName(scientificName, state, state.getReport());
            }
            if(config.isIgnoreAuthorship() && parsedName!=null && preferredFlag){
                // do not ignore authorship for non-preferred names because they need
                // to be created for the determination history
                String nameCache = parsedName.getNameCache();
                List<NonViralName> names = getNameService().findNamesByNameCache(nameCache, MatchMode.EXACT, null);
                taxonName = getBestMatchingName(scientificName, new ArrayList<TaxonNameBase>(names), state);
            }
            else {
                //search for existing names
                List<TaxonNameBase> names = getNameService().listByTitle(TaxonNameBase.class, scientificName, MatchMode.EXACT, null, null, null, null, null);
                taxonName = getBestMatchingName(scientificName, names, state);
                //still nothing found -> try with the atomised name full title cache
                if(taxonName==null && atomisedTaxonName!=null){
                    names = getNameService().listByTitle(TaxonNameBase.class, atomisedTaxonName.getFullTitleCache(), MatchMode.EXACT, null, null, null, null, null);
                    taxonName = getBestMatchingName(atomisedTaxonName.getTitleCache(), names, state);
                    //still nothing found -> try with the atomised name title cache
                    if(taxonName==null){
                        names = getNameService().listByTitle(TaxonNameBase.class, atomisedTaxonName.getTitleCache(), MatchMode.EXACT, null, null, null, null, null);
                        taxonName = getBestMatchingName(atomisedTaxonName.getTitleCache(), names, state);
                    }
                }
            }
        }

        if(taxonName==null && atomisedTaxonName!=null){
            taxonName = atomisedTaxonName;
            state.getReport().addName(taxonName);
            logger.info("Created new taxon name "+taxonName);
            if(taxonName.hasProblem()){
                state.getReport().addInfoMessage(String.format("Created %s with parsing problems", taxonName));
            }
            if(!atomisedTaxonName.getTitleCache().equals(scientificName)){
                state.getReport().addInfoMessage(String.format("Taxon %s was parsed as %s", scientificName, atomisedTaxonName.getTitleCache()));
            }
        }
        else if(taxonName==null){
            //create new taxon name
            taxonName = NonViralName.NewInstance(rank);
            taxonName.setFullTitleCache(scientificName,true);
            taxonName.setTitleCache(scientificName, true);
            state.getReport().addName(taxonName);
            logger.info("Created new taxon name "+taxonName);
        }
        save(taxonName, state);
        return taxonName;
    }

	 protected TaxonNameBase<?, ?> getBestMatchingName(String scientificName, java.util.Collection<TaxonNameBase> names, STATE state){
	        List<TaxonNameBase> namesWithAcceptedTaxa = new ArrayList<TaxonNameBase>();
	        for (TaxonNameBase name : names) {
	            if(!name.getTaxa().isEmpty()){
	                namesWithAcceptedTaxa.add(name);
	            }
	        }
	        String message = "More than one taxon name was found for "+scientificName+"!";
	        //check for names with accepted taxa
	        if(namesWithAcceptedTaxa.size()>0){
	            if(namesWithAcceptedTaxa.size()>1){
	                state.getReport().addInfoMessage(message);
	                logger.warn(message);
	                return null;
	            }
	            return namesWithAcceptedTaxa.iterator().next();
	        }
	        //no names with accepted taxa found -> check accepted taxa of synonyms
	        List<Taxon> taxaFromSynonyms = new ArrayList<Taxon>();
	        for (TaxonNameBase name : names) {
	            Set<TaxonBase> taxonBases = name.getTaxonBases();
	            for (TaxonBase taxonBase : taxonBases) {
	                if(taxonBase.isInstanceOf(Synonym.class)){
	                    Synonym synonym = HibernateProxyHelper.deproxy(taxonBase, Synonym.class);
	                    taxaFromSynonyms.addAll(synonym.getAcceptedTaxa());
	                }
	            }
	        }
	        if(taxaFromSynonyms.size()>0){
	            if(taxaFromSynonyms.size()>1){
	                state.getReport().addInfoMessage(message);
	                logger.warn(message);
	                return null;
	            }
	            return taxaFromSynonyms.iterator().next().getName();
	        }
	        return null;
	    }
	 /**
	     * Parse automatically the scientific name
	     * @param scientificName the scientific name to parse
	     * @param state the current import state
	     * @param report the import report
	     * @return a parsed name
	     */
	    protected NonViralName<?> parseScientificName(String scientificName, STATE state, SpecimenImportReport report) {
	        NonViralNameParserImpl nvnpi = NonViralNameParserImpl.NewInstance();
	        NonViralName<?> taxonName = null;
	        boolean problem = false;

	        if(DEBUG){
	            logger.info("parseScientificName " + state.getDataHolder().getNomenclatureCode().toString());
	        }

	        if (state.getDataHolder().getNomenclatureCode().toString().equals("Zoological") || state.getDataHolder().getNomenclatureCode().toString().contains("ICZN")) {
	            taxonName = nvnpi.parseFullName(scientificName, NomenclaturalCode.ICZN, null);
	            if (taxonName.hasProblem()) {
	                problem = true;
	            }
	        }
	        else if (state.getDataHolder().getNomenclatureCode().toString().equals("Botanical") || state.getDataHolder().getNomenclatureCode().toString().contains("ICBN")) {
	            taxonName = nvnpi.parseFullName(scientificName, NomenclaturalCode.ICNAFP, null);
	            if (taxonName.hasProblem()) {
	                problem = true;
	            }
	        }
	        else if (state.getDataHolder().getNomenclatureCode().toString().equals("Bacterial") || state.getDataHolder().getNomenclatureCode().toString().contains("ICBN")) {
	            taxonName = nvnpi.parseFullName(scientificName, NomenclaturalCode.ICNB, null);
	            if (taxonName.hasProblem()) {
	                problem = true;
	            }
	        }
	        else if (state.getDataHolder().getNomenclatureCode().toString().equals("Cultivar") || state.getDataHolder().getNomenclatureCode().toString().contains("ICNCP")) {
	            taxonName = nvnpi.parseFullName(scientificName, NomenclaturalCode.ICNCP, null);
	            if (taxonName.hasProblem()) {
	                problem = true;
	            }
	        }
	        if (problem) {
	            String message = String.format("Parsing problems for %s", scientificName);
	            if(taxonName!=null){
	                for (ParserProblem parserProblem : taxonName.getParsingProblems()) {
	                    message += "\n\t- "+parserProblem;
	                }
	            }
	            report.addInfoMessage(message);
	            logger.info(message);
	        }
	        return taxonName;

	    }

	    /**
	     * Create the name without automatic parsing, either because it failed, or because the user deactivated it.
	     * The name is built upon the ABCD fields
	     * @param atomisedMap : the ABCD atomised fields
	     * @param fullName : the full scientific name
	     * @param state
	     * @return the corresponding Botanical or Zoological or... name
	     */
	    protected NonViralName<?> setTaxonNameByType(
	            HashMap<String, String> atomisedMap, String fullName, STATE state) {
	        boolean problem = false;
	        if(DEBUG) {
	            logger.info("settaxonnamebytype " + state.getDataHolder().getNomenclatureCode().toString());
	        }

	        if (state.getDataHolder().getNomenclatureCode().equals("Zoological")) {
	            NonViralName<ZoologicalName> taxonName = ZoologicalName.NewInstance(null);
	            taxonName.setFullTitleCache(fullName, true);
	            taxonName.setGenusOrUninomial(NB(getFromMap(atomisedMap, "Genus")));
	            taxonName.setInfraGenericEpithet(NB(getFromMap(atomisedMap, "SubGenus")));
	            taxonName.setSpecificEpithet(NB(getFromMap(atomisedMap,"SpeciesEpithet")));
	            taxonName.setInfraSpecificEpithet(NB(getFromMap(atomisedMap,"SubspeciesEpithet")));

	            if (taxonName.getGenusOrUninomial() != null){
	                taxonName.setRank(Rank.GENUS());
	            }

	            if (taxonName.getInfraGenericEpithet() != null){
	                taxonName.setRank(Rank.SUBGENUS());
	            }

	            if (taxonName.getSpecificEpithet() != null){
	                taxonName.setRank(Rank.SPECIES());
	            }

	            if (taxonName.getInfraSpecificEpithet() != null){
	                taxonName.setRank(Rank.SUBSPECIES());
	            }

	            Team team = null;
	            if (getFromMap(atomisedMap, "AuthorTeamParenthesis") != null) {
	                team = Team.NewInstance();
	                team.setTitleCache(getFromMap(atomisedMap, "AuthorTeamParenthesis"), true);
	            }
	            else {
	                if (getFromMap(atomisedMap, "AuthorTeamAndYear") != null) {
	                    team = Team.NewInstance();
	                    team.setTitleCache(getFromMap(atomisedMap, "AuthorTeamAndYear"), true);
	                }
	            }
	            if (team != null) {
	                taxonName.setBasionymAuthorship(team);
	            }
	            else {
	                if (getFromMap(atomisedMap, "AuthorTeamParenthesis") != null) {
	                    taxonName.setAuthorshipCache(getFromMap(atomisedMap, "AuthorTeamParenthesis"));
	                }
	                else if (getFromMap(atomisedMap, "AuthorTeamAndYear") != null) {
	                    taxonName.setAuthorshipCache(getFromMap(atomisedMap, "AuthorTeamAndYear"));
	                }
	            }
	            if (getFromMap(atomisedMap, "CombinationAuthorTeamAndYear") != null) {
	                team = Team.NewInstance();
	                team.setTitleCache(getFromMap(atomisedMap, "CombinationAuthorTeamAndYear"), true);
	                taxonName.setCombinationAuthorship(team);
	            }
	            if (taxonName.hasProblem()) {
	                logger.info("pb ICZN");
	                problem = true;
	            }
	            else {
	                return taxonName;
	            }
	        }
	        else if (state.getDataHolder().getNomenclatureCode().equals("Botanical")) {
	            BotanicalName taxonName = (BotanicalName) parseScientificName(fullName, state, state.getReport());
	            if (taxonName != null){
	                return taxonName;
	            }
	            else{
	                taxonName = BotanicalName.NewInstance(null);
	            }
	            taxonName.setFullTitleCache(fullName, true);
	            taxonName.setGenusOrUninomial(NB(getFromMap(atomisedMap, "Genus")));
	            taxonName.setSpecificEpithet(NB(getFromMap(atomisedMap, "FirstEpithet")));
	            taxonName.setInfraSpecificEpithet(NB(getFromMap(atomisedMap, "InfraSpeEpithet")));
	            try {
	                taxonName.setRank(Rank.getRankByName(getFromMap(atomisedMap, "Rank")));
	            } catch (Exception e) {
	                if (taxonName.getInfraSpecificEpithet() != null){
	                    taxonName.setRank(Rank.SUBSPECIES());
	                }
	                else if (taxonName.getSpecificEpithet() != null){
	                    taxonName.setRank(Rank.SPECIES());
	                }
	                else if (taxonName.getInfraGenericEpithet() != null){
	                    taxonName.setRank(Rank.SUBGENUS());
	                }
	                else if (taxonName.getGenusOrUninomial() != null){
	                    taxonName.setRank(Rank.GENUS());
	                }
	            }
	            Team team = null;
	            if (getFromMap(atomisedMap, "AuthorTeamParenthesis") != null) {
	                team = Team.NewInstance();
	                team.setTitleCache(getFromMap(atomisedMap, "AuthorTeamParenthesis"), true);
	                taxonName.setBasionymAuthorship(team);
	            }
	            if (getFromMap(atomisedMap, "AuthorTeam") != null) {
	                team = Team.NewInstance();
	                team.setTitleCache(getFromMap(atomisedMap, "AuthorTeam"), true);
	                taxonName.setCombinationAuthorship(team);
	            }
	            if (team == null) {
	                if (getFromMap(atomisedMap, "AuthorTeamParenthesis") != null) {
	                    taxonName.setAuthorshipCache(getFromMap(atomisedMap, "AuthorTeamParenthesis"));
	                }
	                else if (getFromMap(atomisedMap, "AuthorTeam") != null) {
	                    taxonName.setAuthorshipCache(getFromMap(atomisedMap, "AuthorTeam"));
	                }
	            }
	            if (getFromMap(atomisedMap, "CombinationAuthorTeamAndYear") != null) {
	                team = Team.NewInstance();
	                team.setTitleCache(getFromMap(atomisedMap, "CombinationAuthorTeamAndYear"), true);
	                taxonName.setCombinationAuthorship(team);
	            }
	            if (taxonName.hasProblem()) {
	                logger.info("pb ICBN");
	                problem = true;
	            }
	            else {
	                return taxonName;
	            }
	        }
	        else if (state.getDataHolder().getNomenclatureCode().equals("Bacterial")) {
	            NonViralName<BacterialName> taxonName = BacterialName.NewInstance(null);
	            taxonName.setFullTitleCache(fullName, true);
	            taxonName.setGenusOrUninomial(getFromMap(atomisedMap, "Genus"));
	            taxonName.setInfraGenericEpithet(NB(getFromMap(atomisedMap, "SubGenus")));
	            taxonName.setSpecificEpithet(NB(getFromMap(atomisedMap, "Species")));
	            taxonName.setInfraSpecificEpithet(NB(getFromMap(atomisedMap, "SubspeciesEpithet")));

	            if (taxonName.getGenusOrUninomial() != null){
	                taxonName.setRank(Rank.GENUS());
	            }
	            else if (taxonName.getInfraGenericEpithet() != null){
	                taxonName.setRank(Rank.SUBGENUS());
	            }
	            else if (taxonName.getSpecificEpithet() != null){
	                taxonName.setRank(Rank.SPECIES());
	            }
	            else if (taxonName.getInfraSpecificEpithet() != null){
	                taxonName.setRank(Rank.SUBSPECIES());
	            }

	            if (getFromMap(atomisedMap, "AuthorTeamAndYear") != null) {
	                Team team = Team.NewInstance();
	                team.setTitleCache(getFromMap(atomisedMap, "AuthorTeamAndYear"), true);
	                taxonName.setCombinationAuthorship(team);
	            }
	            if (getFromMap(atomisedMap, "ParentheticalAuthorTeamAndYear") != null) {
	                Team team = Team.NewInstance();
	                team.setTitleCache(getFromMap(atomisedMap, "ParentheticalAuthorTeamAndYear"), true);
	                taxonName.setBasionymAuthorship(team);
	            }
	            if (taxonName.hasProblem()) {
	                logger.info("pb ICNB");
	                problem = true;
	            }
	            else {
	                return taxonName;
	            }
	        }
	        else if (state.getDataHolder().getNomenclatureCode().equals("Cultivar")) {
	            CultivarPlantName taxonName = CultivarPlantName.NewInstance(null);

	            if (taxonName.hasProblem()) {
	                logger.info("pb ICNCP");
	                problem = true;
	            }
	            else {
	                return taxonName;
	            }
	            return taxonName;
	        }

	        if (problem) {
	            logger.info("Problem im setTaxonNameByType ");
	            NonViralName<?> taxonName = NonViralName.NewInstance(null);
	            taxonName.setFullTitleCache(fullName, true);
	            return taxonName;
	        }
	        NonViralName<?> tn = NonViralName.NewInstance(null);
	        return tn;
	    }

	    /**
	     * Get a formated string from a hashmap
	     * @param atomisedMap
	     * @param key
	     * @return
	     */
	    private String getFromMap(HashMap<String, String> atomisedMap, String key) {
	        String value = null;
	        if (atomisedMap.containsKey(key)) {
	            value = atomisedMap.get(key);
	        }

	        try {
	            if (value != null && key.matches(".*Year.*")) {
	                value = value.trim();
	                if (value.matches("[a-z A-Z ]*[0-9]{4}$")) {
	                    String tmp = value.split("[0-9]{4}$")[0];
	                    int year = Integer.parseInt(value.split(tmp)[1]);
	                    if (year >= 1752) {
	                        value = tmp;
	                    }
	                    else {
	                        value = null;
	                    }
	                }
	                else {
	                    value = null;
	                }
	            }
	        }
	        catch (Exception e) {
	            value = null;
	        }
	        return value;
	    }

	    /**
	     * Very fast and dirty implementation to allow handling of transient objects as described in
	     * https://dev.e-taxonomy.eu/trac/ticket/3726
	     *
	     * Not yet complete.
	     *
	     * @param cdmBase
	     * @param state
	     */
	    protected void save(CdmBase cdmBase, SpecimenImportStateBase state) {
	        ICdmApplicationConfiguration cdmRepository = state.getConfig().getCdmAppController();
	        if (cdmRepository == null){
	            cdmRepository = this;
	        }

	        if (cdmBase.isInstanceOf(LanguageString.class)){
	            cdmRepository.getTermService().saveLanguageData(CdmBase.deproxy(cdmBase, LanguageString.class));
	        }else if (cdmBase.isInstanceOf(SpecimenOrObservationBase.class)){
	            cdmRepository.getOccurrenceService().saveOrUpdate(CdmBase.deproxy(cdmBase, SpecimenOrObservationBase.class));
	        }else if (cdmBase.isInstanceOf(Reference.class)){
	            cdmRepository.getReferenceService().saveOrUpdate(CdmBase.deproxy(cdmBase, Reference.class));
	        }else if (cdmBase.isInstanceOf(Classification.class)){
	            cdmRepository.getClassificationService().saveOrUpdate(CdmBase.deproxy(cdmBase, Classification.class));
	        }else if (cdmBase.isInstanceOf(AgentBase.class)){
	            cdmRepository.getAgentService().saveOrUpdate(CdmBase.deproxy(cdmBase, AgentBase.class));
	        }else if (cdmBase.isInstanceOf(Collection.class)){
	            cdmRepository.getCollectionService().saveOrUpdate(CdmBase.deproxy(cdmBase, Collection.class));
	        }else if (cdmBase.isInstanceOf(DescriptionBase.class)){
	            cdmRepository.getDescriptionService().saveOrUpdate(CdmBase.deproxy(cdmBase, DescriptionBase.class));
	        }else if (cdmBase.isInstanceOf(TaxonBase.class)){
	            cdmRepository.getTaxonService().saveOrUpdate(CdmBase.deproxy(cdmBase, TaxonBase.class));
	        }else if (cdmBase.isInstanceOf(TaxonNameBase.class)){
	            cdmRepository.getNameService().saveOrUpdate(CdmBase.deproxy(cdmBase, TaxonNameBase.class));
	        }else{
	            throw new IllegalArgumentException("Class not supported in save method: " + CdmBase.deproxy(cdmBase, CdmBase.class).getClass().getSimpleName());
	        }

	    }


	    protected SpecimenOrObservationBase findExistingSpecimen(String unitId, SpecimenImportStateBase state){
	        ICdmApplicationConfiguration cdmAppController = state.getConfig().getCdmAppController();
	        if(cdmAppController==null){
	            cdmAppController = this;
	        }
	        FindOccurrencesConfigurator config = new FindOccurrencesConfigurator();
	        config.setSignificantIdentifier(unitId);
	        Pager<SpecimenOrObservationBase> existingSpecimens = cdmAppController.getOccurrenceService().findByTitle(config);
	        if(!existingSpecimens.getRecords().isEmpty()){
	            if(existingSpecimens.getRecords().size()==1){
	                return existingSpecimens.getRecords().iterator().next();
	            }
	        }
	        return null;
	    }

	    protected abstract void importAssociatedUnits(STATE state, Object item, DerivedUnitFacade derivedUnitFacade);



}