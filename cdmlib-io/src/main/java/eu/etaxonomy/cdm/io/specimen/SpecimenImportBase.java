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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.api.application.ICdmRepository;
import eu.etaxonomy.cdm.api.service.config.FindOccurrencesConfigurator;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.Identification;
import eu.etaxonomy.cdm.io.specimen.abcd206.in.SpecimenImportReport;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.DescriptionType;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.RankClass;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;
import eu.etaxonomy.cdm.model.reference.ISourceable;
import eu.etaxonomy.cdm.model.reference.OriginalSourceBase;
import eu.etaxonomy.cdm.model.reference.OriginalSourceType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.model.term.DefinedTerm;
import eu.etaxonomy.cdm.persistence.query.MatchMode;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;
import eu.etaxonomy.cdm.strategy.parser.ParserProblem;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;

/**
 * @author p.kelbert
 * @since 20.10.2008
 */
public abstract class SpecimenImportBase<CONFIG extends IImportConfigurator, STATE extends SpecimenImportStateBase>
        extends CdmImportBase<CONFIG, STATE> {

    private static final long serialVersionUID = 4423065367998125678L;
    private static final Logger logger = LogManager.getLogger();

	protected static final UUID SPECIMEN_SCAN_TERM = UUID.fromString("acda15be-c0e2-4ea8-8783-b9b0c4ad7f03");

	private static final String COLON = ":";

	protected Map<String, DefinedTerm> kindOfUnitsMap;


	@Override
    protected abstract void doInvoke(STATE state);

	/**
	 * Handle a single unit
	 */
	protected abstract void handleSingleUnit(STATE state, Object item) ;

	protected TaxonName getOrCreateTaxonName(String scientificName, Rank rank, boolean preferredFlag, STATE state, int unitIndexInAbcdFile){
	    TaxonName taxonName = null;
        SpecimenImportConfiguratorBase<?,?,?> config = state.getConfig();

        //check atomised name data for rank
        //new name will be created
        TaxonName atomisedTaxonName = null;
        if (rank==null && unitIndexInAbcdFile>=0 && ((state.getDataHolder().getAtomisedIdentificationList() != null && !state.getDataHolder().getAtomisedIdentificationList().isEmpty())|| state.getDataHolder().getAtomisedIdentificationList().size() > 0)) {
            atomisedTaxonName = setTaxonNameByType(state.getDataHolder().getAtomisedIdentificationList().get(unitIndexInAbcdFile), scientificName, state);
            if(atomisedTaxonName!=null){
                rank = atomisedTaxonName.getRank();
            }
        }
        if(config.isReuseExistingTaxaWhenPossible()){
            TaxonName parsedName = atomisedTaxonName;
            if(parsedName==null){

                parsedName = parseScientificName(scientificName, state, state.getReport(), rank);

            }
            atomisedTaxonName = parsedName;
            if(config.isIgnoreAuthorship() && parsedName!=null){// && preferredFlag){
                // do not ignore authorship for non-preferred names because they need
                // to be created for the determination history
                String nameCache = TaxonName.castAndDeproxy(parsedName).getNameCache();
                List<TaxonName> names = getNameService().findNamesByNameCache(nameCache, MatchMode.EXACT, null);
                if (!names.isEmpty()){
                     taxonName = getBestMatchingName(scientificName, new ArrayList<>(names), state);
                }
                if (taxonName == null && !names.isEmpty()){
                    taxonName = names.get(0);
                }

            } else {
                //search for existing names
                List<TaxonName> names = getNameService().listByTitleWithRestrictions(TaxonName.class, scientificName, MatchMode.EXACT, null, null, null, null, null);
                taxonName = getBestMatchingName(scientificName, names, state);
                //still nothing found -> try with the atomised name full title cache
                if(taxonName==null && atomisedTaxonName!=null){
                    names = getNameService().listByTitleWithRestrictions(TaxonName.class, atomisedTaxonName.getFullTitleCache(), MatchMode.EXACT, null, null, null, null, null);
                    taxonName = getBestMatchingName(atomisedTaxonName.getTitleCache(), names, state);
                    //still nothing found -> try with the atomised name title cache
                    if(taxonName==null){
                        names = getNameService().listByTitleWithRestrictions(TaxonName.class, atomisedTaxonName.getTitleCache(), MatchMode.EXACT, null, null, null, null, null);
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

            if (state.getDataHolder().getNomenclatureCode().equals(NomenclaturalCode.ICNAFP)){
                taxonName = TaxonNameFactory.NewBotanicalInstance(rank);
            }else if (state.getDataHolder().getNomenclatureCode().equals(NomenclaturalCode.ICZN)){
                taxonName = TaxonNameFactory.NewZoologicalInstance(rank);
            }else{
                taxonName = TaxonNameFactory.NewNonViralInstance(rank);
            }
            taxonName.setFullTitleCache(scientificName,true);
            taxonName.setTitleCache(scientificName, true);
            state.getReport().addName(taxonName);
            logger.info("Created new taxon name "+taxonName);
        }
        if (taxonName != null){
            state.names.put(taxonName.getNameCache(), taxonName);
        }
        if(!taxonName.isPersisted()) {
            save(taxonName, state);
        }
        return taxonName;
    }

	protected TaxonName getBestMatchingName(String scientificName, java.util.Collection<TaxonName> names, STATE state){
        Set<TaxonName> namesWithAcceptedTaxa = new HashSet<>();
        List<TaxonName> namesWithAcceptedTaxaInClassification = new ArrayList<>();
        for (TaxonName name : names) {
            if(!name.getTaxonBases().isEmpty()){
                Set<TaxonBase> taxa = name.getTaxonBases();
                for (TaxonBase taxonBase:taxa){
                    Taxon acceptedTaxon= null;
                    if (taxonBase instanceof Synonym) {
                        Synonym syn = (Synonym) taxonBase;
                        acceptedTaxon = syn.getAcceptedTaxon();
                    }else {
                        acceptedTaxon = (Taxon)taxonBase;
                    }
                    if (!(acceptedTaxon).getTaxonNodes().isEmpty()){
                        //use only taxa included in a classification
                        for (TaxonNode node:(acceptedTaxon).getTaxonNodes()){
                            if (state.getClassification() != null && node.getClassification().equals(state.getClassification())){
                                namesWithAcceptedTaxaInClassification.add(name);
                            }else {
                                namesWithAcceptedTaxa.add(name);
                            }
                        }
                    }
                }
            }
        }
        String message = String.format("More than one taxon name was found for %s, maybe in other classifications!", scientificName);
        //check for names with accepted taxa in classification
        if(namesWithAcceptedTaxaInClassification.size()>0){
            if(namesWithAcceptedTaxaInClassification.size()>1){

                state.getReport().addInfoMessage(message);
                logger.warn(message);
                return null;
            }
            return namesWithAcceptedTaxaInClassification.iterator().next();
        }
        //check for any names with accepted taxa
        if(namesWithAcceptedTaxa.size()>0){
            if(namesWithAcceptedTaxa.size()>1){

                state.getReport().addInfoMessage(message);
                logger.warn(message);
                return null;
            }
            return namesWithAcceptedTaxa.iterator().next();
        }
//	        //no names with accepted taxa found -> check accepted taxa of synonyms -> this is handled in the first block now!
//	        List<Taxon> taxaFromSynonyms = new ArrayList<>();
//	        for (TaxonName name : names) {
//	            Set<TaxonBase> taxonBases = name.getTaxonBases();
//	            for (TaxonBase taxonBase : taxonBases) {
//	                if(taxonBase.isInstanceOf(Synonym.class)){
//	                    Synonym synonym = HibernateProxyHelper.deproxy(taxonBase, Synonym.class);
//	                    taxaFromSynonyms.add(synonym.getAcceptedTaxon());
//	                }
//	            }
//	        }
//	        if(taxaFromSynonyms.size()>0){
//	            if(taxaFromSynonyms.size()>1){
//	                state.getReport().addInfoMessage(message);
//	                logger.warn(message);
//	                return null;
//	            }
//	            return taxaFromSynonyms.iterator().next().getName();
//	        }
	        //no accepted and no synonyms -> return one of the names and create a new taxon
        if (names.isEmpty()){
            return null;
        }else{
            return names.iterator().next();
        }
    }

	/**
     * Parse automatically the scientific name
     * @param scientificName the scientific name to parse
     * @param state the current import state
     * @param report the import report
     * @return a parsed name
     */
    protected TaxonName parseScientificName(String scientificName, STATE state, SpecimenImportReport report, Rank rank) {

        NonViralNameParserImpl nvnpi = NonViralNameParserImpl.NewInstance();
        TaxonName taxonName = null;
        boolean problem = false;

        if (logger.isDebugEnabled()){
            logger.debug("parseScientificName " + state.getDataHolder().getNomenclatureCode().toString());
        }

        if (state.getDataHolder().getNomenclatureCode() != null && (state.getDataHolder().getNomenclatureCode().toString().equals("Zoological") || state.getDataHolder().getNomenclatureCode().toString().contains("ICZN"))) {
            taxonName = (TaxonName)nvnpi.parseFullName(scientificName, NomenclaturalCode.ICZN, rank);
            if (taxonName.hasProblem()) {
                problem = true;
            }
        }
        else if (state.getDataHolder().getNomenclatureCode() != null && (state.getDataHolder().getNomenclatureCode().toString().equals("Botanical") || state.getDataHolder().getNomenclatureCode().toString().contains("ICBN")  || state.getDataHolder().getNomenclatureCode().toString().contains("ICNAFP"))) {
            taxonName = (TaxonName)nvnpi.parseFullName(scientificName, NomenclaturalCode.ICNAFP, rank);
            if (taxonName.hasProblem()) {
                problem = true;
            }
        }
        else if (state.getDataHolder().getNomenclatureCode() != null && (state.getDataHolder().getNomenclatureCode().toString().equals("Bacterial") || state.getDataHolder().getNomenclatureCode().toString().contains("ICBN"))) {
            taxonName = (TaxonName)nvnpi.parseFullName(scientificName, NomenclaturalCode.ICNP, rank);
            if (taxonName.hasProblem()) {
                problem = true;
            }
        }
        else if (state.getDataHolder().getNomenclatureCode() != null && (state.getDataHolder().getNomenclatureCode().toString().equals("Cultivar") || state.getDataHolder().getNomenclatureCode().toString().contains("ICNCP"))) {
            taxonName = (TaxonName)nvnpi.parseFullName(scientificName, NomenclaturalCode.ICNCP, rank);
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
    protected TaxonName setTaxonNameByType(
            HashMap<String, String> atomisedMap, String fullName, STATE state) {
        boolean problem = false;
        if (logger.isDebugEnabled()){
            logger.debug("settaxonnamebytype " + state.getDataHolder().getNomenclatureCode().toString());
        }

        if (state.getDataHolder().getNomenclatureCode().equals("Zoological") || state.getDataHolder().getNomenclatureCode().equals(NomenclaturalCode.ICZN.getUuid())) {
            TaxonName taxonName = TaxonNameFactory.NewZoologicalInstance(null);
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
        else if (state.getDataHolder().getNomenclatureCode().equals("Botanical") || state.getDataHolder().getNomenclatureCode().equals(NomenclaturalCode.ICNAFP.getUuid())) {
            TaxonName taxonName = parseScientificName(fullName, state, state.getReport(), null);
            if (taxonName != null){
                return taxonName;
            }
            else{
                taxonName = TaxonNameFactory.NewBotanicalInstance(null);
            }
            taxonName.setFullTitleCache(fullName, true);
            taxonName.setGenusOrUninomial(NB(getFromMap(atomisedMap, "Genus")));
            taxonName.setSpecificEpithet(NB(getFromMap(atomisedMap, "FirstEpithet")));
            taxonName.setInfraSpecificEpithet(NB(getFromMap(atomisedMap, "InfraSpeEpithet")));
            try {
                taxonName.setRank(Rank.getRankByLatinName(getFromMap(atomisedMap, "Rank")));
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
        else if (state.getDataHolder().getNomenclatureCode().equals("Bacterial") || state.getDataHolder().getNomenclatureCode().equals(NomenclaturalCode.ICNP.getUuid())) {
            TaxonName taxonName = TaxonNameFactory.NewBacterialInstance(null);
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
                logger.info("pb ICNP");
                problem = true;
            }
            else {
                return taxonName;
            }
        }
        else if (state.getDataHolder().getNomenclatureCode().equals("Cultivar")) {
            TaxonName taxonName = TaxonNameFactory.NewCultivarInstance(null);

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
            TaxonName taxonName = TaxonNameFactory.NewNonViralInstance(null);
            taxonName.setFullTitleCache(fullName, true);
            return taxonName;
        }
        TaxonName tn = TaxonNameFactory.NewNonViralInstance(null);
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
     * https://dev.e-taxonomy.eu/redmine/issues/3726
     *
     * Not yet complete.
     */
    protected UUID save(CdmBase cdmBase, SpecimenImportStateBase<?,?> state) {
        ICdmRepository cdmRepository = state.getConfig().getCdmAppController();
        if (cdmRepository == null){
            cdmRepository = this;
        }

        if (cdmBase.isInstanceOf(LanguageString.class)){
            return cdmRepository.getTermService().saveLanguageData(CdmBase.deproxy(cdmBase, LanguageString.class));
        }else if (cdmBase.isInstanceOf(SpecimenOrObservationBase.class)){
            SpecimenOrObservationBase<?> specimen = CdmBase.deproxy(cdmBase, SpecimenOrObservationBase.class);
            return cdmRepository.getOccurrenceService().saveOrUpdate(specimen);
        }else if (cdmBase.isInstanceOf(Reference.class)){
            return cdmRepository.getReferenceService().saveOrUpdate(CdmBase.deproxy(cdmBase, Reference.class));
        }else if (cdmBase.isInstanceOf(Classification.class)){
            return cdmRepository.getClassificationService().saveOrUpdate(CdmBase.deproxy(cdmBase, Classification.class));
        }else if (cdmBase.isInstanceOf(AgentBase.class)){
            return cdmRepository.getAgentService().saveOrUpdate(CdmBase.deproxy(cdmBase, AgentBase.class));
        }else if (cdmBase.isInstanceOf(Collection.class)){
            return cdmRepository.getCollectionService().saveOrUpdate(CdmBase.deproxy(cdmBase, Collection.class));
        }else if (cdmBase.isInstanceOf(DescriptionBase.class)){
            DescriptionBase<?> description = CdmBase.deproxy(cdmBase, DescriptionBase.class);
            return cdmRepository.getDescriptionService().saveOrUpdate(description);
        }else if (cdmBase.isInstanceOf(TaxonBase.class)){
            return cdmRepository.getTaxonService().saveOrUpdate(CdmBase.deproxy(cdmBase, TaxonBase.class));
        }else if (cdmBase.isInstanceOf(TaxonName.class)){
            return cdmRepository.getNameService().saveOrUpdate(CdmBase.deproxy(cdmBase, TaxonName.class));
        }else if (cdmBase.isInstanceOf(TaxonNode.class)){
            return cdmRepository.getTaxonNodeService().saveOrUpdate(CdmBase.deproxy(cdmBase, TaxonNode.class));
        }else{
            throw new IllegalArgumentException("Class not supported in save method: " + CdmBase.deproxy(cdmBase, CdmBase.class).getClass().getSimpleName());
        }
    }

    protected SpecimenOrObservationBase<?> findExistingSpecimen(String unitId, SpecimenImportStateBase<?,?> state){
        ICdmRepository cdmAppController = state.getConfig().getCdmAppController();
        if(cdmAppController==null){
            cdmAppController = this;
        }
        FindOccurrencesConfigurator config = new FindOccurrencesConfigurator();
        config.setSignificantIdentifier(unitId);
        List<String> propertyPaths = new ArrayList<>();
        propertyPaths.add("derivedFrom.*");
        config.setPropertyPaths(propertyPaths);
        commitTransaction(state.getTx());
        state.setTx(startTransaction());
        try{
	        @SuppressWarnings("rawtypes")
            Pager<SpecimenOrObservationBase> existingSpecimens = cdmAppController.getOccurrenceService().findByTitle(config);
	        if(!existingSpecimens.getRecords().isEmpty()){
	            if(existingSpecimens.getRecords().size()==1){
	                return existingSpecimens.getRecords().iterator().next();
	            }
	        }

        }catch(NullPointerException e){
        	logger.error("searching for existing specimen creates NPE: " + config.getSignificantIdentifier());
        	e.printStackTrace();
        }


        return null;
    }

    protected abstract void importAssociatedUnits(STATE state, Object item, DerivedUnitFacade derivedUnitFacade);

    /**
     * getFacade : get the DerivedUnitFacade based on the recordBasis
     * @param state
     *
     * @return DerivedUnitFacade
     */
    protected DerivedUnitFacade getFacade(STATE state) {
        if (logger.isDebugEnabled()){
            logger.info("getFacade()");
        }
        SpecimenOrObservationType type = null;

        // create specimen
        if (NB((state.getDataHolder().getRecordBasis())) != null) {
            if (state.getDataHolder().getRecordBasis().toLowerCase().startsWith("s") || state.getDataHolder().getRecordBasis().toLowerCase().indexOf("specimen")>-1) {// specimen
                type = SpecimenOrObservationType.PreservedSpecimen;
            }
            if (state.getDataHolder().getRecordBasis().toLowerCase().startsWith("o") ||state.getDataHolder().getRecordBasis().toLowerCase().indexOf("observation")>-1 ) {
                type = SpecimenOrObservationType.Observation;
            }
            if (state.getDataHolder().getRecordBasis().toLowerCase().indexOf("fossil")>-1){
                type = SpecimenOrObservationType.Fossil;
            }
            if (state.getDataHolder().getRecordBasis().toLowerCase().indexOf("living")>-1) {
                type = SpecimenOrObservationType.LivingSpecimen;
            }
            if (type == null) {
                logger.info("The basis of record does not seem to be known: " + state.getDataHolder().getRecordBasis());
                type = SpecimenOrObservationType.DerivedUnit;
            }
            // TODO fossils?
        } else {
            logger.info("The basis of record is null");
            type = SpecimenOrObservationType.DerivedUnit;
        }
        DerivedUnitFacade derivedUnitFacade = DerivedUnitFacade.NewInstance(type);
        return derivedUnitFacade;
    }

    /**
     * Look if the Institution does already exist
     * @param institutionCode: a string with the institutioncode
     * @param config : the configurator
     * @return the Institution (existing or new)
     */
    protected Institution getInstitution(String institutionCode, STATE state) {
        SpecimenImportConfiguratorBase<?,?,?> config = state.getConfig();
        Institution institution=null;
        institution = (Institution)state.institutions.get(institutionCode);
        if (institution != null){
            return institution;
        }
        List<Institution> institutions;
        try {
            institutions = getAgentService().searchInstitutionByCode(institutionCode);

        } catch (Exception e) {
            institutions = new ArrayList<Institution>();
            logger.warn(e);
        }
        if (institutions.size() > 0 && config.isReuseExistingMetaData()) {
            for (Institution institut:institutions){
                try{
                    if (institut.getCode().equalsIgnoreCase(institutionCode)) {
                        institution=institut;
                        break;
                    }
                }catch(Exception e){logger.warn("no institution code in the db");}
            }
        }
        if (logger.isDebugEnabled()){
            if(institution !=null) {
                logger.info("getinstitution " + institution.toString());
            }
        }
        if (institution == null){
            // create institution
            institution = Institution.NewInstance();
            institution.setCode(institutionCode);
            institution.setTitleCache(institutionCode, true);
            save(institution, state);
        }

        state.institutions.put(institutionCode, institution);
        return institution;
    }

    /**
     * Look if the Collection does already exist
     * @param collectionCode
     * @param collectionCode: a string
     * @param config : the configurator
     * @return the Collection (existing or new)
     */
    protected Collection getCollection(Institution institution, String collectionCode, STATE state) {

        SpecimenImportConfiguratorBase<?,?,?> config = state.getConfig();
        Collection collection = null;
        List<Collection> collections;
        collection = (Collection) state.collections.get(collectionCode);
        if (collection != null){
            return collection;
        }
        try {
            collections = getCollectionService().searchByCode(collectionCode);
        } catch (Exception e) {
            collections = new ArrayList<>();
        }
        if (collections.size() > 0 && config.isReuseExistingMetaData()) {
            for (Collection coll:collections){
                if (coll.getCode() != null && coll.getInstitute() != null
                        && coll.getCode().equalsIgnoreCase(collectionCode) && coll.getInstitute().equals(institution)) {
                    collection = coll;
                    break;
                }
            }
        }

        if(collection == null){
            collection =Collection.NewInstance();
            collection.setCode(collectionCode);
            collection.setInstitute(institution);
            save(collection, state);
        }

        state.collections.put(collectionCode, collection);

        return collection;
    }

    /**
     * @param reference
     * @param citationDetail
     * @return
     */
    //FIXME this method is highly critical, because
    //  * it will have serious performance and memory problems with large databases
    //        (databases may easily have >1 Mio source records)
    //  * it does not make sense to search for existing sources and then clone them
    //    we need to search for existing references instead and use them (if exist)
    //    for our new source.
    protected IdentifiableSource getIdentifiableSource(Reference reference, String citationDetail) {

        IdentifiableSource sour = IdentifiableSource.NewInstance(OriginalSourceType.Import,null,null, reference,citationDetail);
        return sour;
    }

    /**
     * Add the hierarchy for a Taxon(add higher taxa)
     * @param classification
     * @param taxon: a taxon to add as a node
     * @param state: the ABCD import state
     */
    protected void addParentTaxon(Taxon taxon, STATE state, boolean preferredFlag, Classification classification){
        INonViralName  nvname = taxon.getName();
        Rank rank = nvname.getRank();
        Taxon genus =null;
        Taxon subgenus =null;
        Taxon species = null;
        Taxon subspecies = null;
        Taxon parent = null;
        if(rank!=null){
            if (rank.isLowerThan(RankClass.Genus)){
                String genusOrUninomial = nvname.getGenusOrUninomial();
                TaxonName taxonName = getOrCreateTaxonName(genusOrUninomial, Rank.GENUS(), preferredFlag, state, -1);
                genus = getOrCreateTaxonForName(taxonName, state);
                if (genus == null){
                    logger.debug("The genus should not be null " + taxonName);
                }
                if (preferredFlag) {
                    parent = linkParentChildNode(null, genus, classification, state);
                }

            }
            if (rank.isLower(Rank.SUBGENUS())){
                String prefix = nvname.getGenusOrUninomial();
                String name = nvname.getInfraGenericEpithet();
                if (name != null){
                    TaxonName taxonName = getOrCreateTaxonName(prefix+" "+name, Rank.SUBGENUS(), preferredFlag, state, -1);
                    subgenus = getOrCreateTaxonForName(taxonName, state);
                    if (preferredFlag) {
                        parent = linkParentChildNode(genus, subgenus, classification, state);
                    }
                }
            }
            if (rank.isLowerThan(RankClass.Species)){
                if (subgenus!=null){
                    String prefix = nvname.getGenusOrUninomial();
                    String name = nvname.getInfraGenericEpithet();
                    String spe = nvname.getSpecificEpithet();
                    if (spe != null){
                        TaxonName taxonName = getOrCreateTaxonName(prefix+" "+name+" "+spe, Rank.SPECIES(), preferredFlag, state, -1);
                        species = getOrCreateTaxonForName(taxonName, state);
                        if (preferredFlag) {
                            parent = linkParentChildNode(subgenus, species, classification, state);
                        }
                    }
                }
                else{
                    String prefix = nvname.getGenusOrUninomial();
                    String name = nvname.getSpecificEpithet();
                    if (name != null){
                        TaxonName taxonName = getOrCreateTaxonName(prefix+" "+name, Rank.SPECIES(), preferredFlag, state, -1);
                        species = getOrCreateTaxonForName(taxonName, state);
                        if (preferredFlag) {
                            parent = linkParentChildNode(genus, species, classification, state);
                        }
                    }
                }
            }
            if (rank.isLower(Rank.INFRASPECIES())){
                TaxonName taxonName = getOrCreateTaxonName(nvname.getFullTitleCache(), Rank.SUBSPECIES(), preferredFlag, state, -1);
                subspecies = getOrCreateTaxonForName(taxonName, state);
                if (preferredFlag) {
                    parent = linkParentChildNode(species, subspecies, classification, state);
                }
            }
        }else{
            //handle cf. and aff. taxa
            String genusEpithet = null;
            if (nvname.getTitleCache().contains("cf.")){
                genusEpithet = nvname.getTitleCache().substring(0, nvname.getTitleCache().indexOf("cf."));
            } else if (nvname.getTitleCache().contains("aff.")){
                genusEpithet = nvname.getTitleCache().substring(0, nvname.getTitleCache().indexOf("aff."));
            }
            if (genusEpithet != null){
	            genusEpithet = genusEpithet.trim();
	            TaxonName taxonName = null;
	            if (genusEpithet.contains(" ")){
	                taxonName = getOrCreateTaxonName(genusEpithet, Rank.SPECIES(), preferredFlag, state, -1);
	            }else{
	                taxonName = getOrCreateTaxonName(genusEpithet, Rank.GENUS(), preferredFlag, state, -1);
	            }
	            genus = getOrCreateTaxonForName(taxonName, state);
                if (genus == null){
                    logger.debug("The genus should not be null " + taxonName);
                }
                if (preferredFlag) {
                    parent = linkParentChildNode(null, genus, classification, state);
                }
            }
        }
        if (preferredFlag && parent!=taxon ) {
            linkParentChildNode(parent, taxon, classification, state);
        }
    }

    /**
     * Link a parent to a child and save it in the current classification
     * @param parent: the higher Taxon
     * @param child : the lower (or current) Taxon
     * return the Taxon from the new created Node
     * @param classification
     * @param state
     */
    protected Taxon linkParentChildNode(Taxon parent, Taxon child, Classification classification, STATE state) {
        TaxonNode node =null;
        List<String> propertyPaths = new ArrayList<>();
        propertyPaths.add("childNodes");
        if (parent != null) {

            parent = (Taxon) getTaxonService().load(parent.getUuid(), propertyPaths);
            child = (Taxon) getTaxonService().load(child.getUuid(), propertyPaths);
            //here we do not have to check if the taxon nodes already exists
            //this is done by classification.addParentChild()
            //do not add child node if it already exists
            if(hasTaxonNodeInClassification(child, classification)){
                return child;
            }
            else{
                node = classification.addParentChild(parent, child, state.getRef(), "");
                save(node, state);
            }
        }
        else {
            if (child == null){
                logger.debug("The child should not be null!");
            }
            child = (Taxon) getTaxonService().find(child.getUuid());
            //do not add child node if it already exists
            if(hasTaxonNodeInClassification(child, classification)){
                return child;
            }
            else{
                node = classification.addChildTaxon(child, state.getRef(), null);
                save(node, state);
            }
        }
        if(node!=null){
            state.getReport().addTaxonNode(node);
            return node.getTaxon();
        }
        String message = "Could not create taxon node for " +child;
        state.getReport().addInfoMessage(message);
        logger.warn(message);
        return null;
    }

    protected Taxon getOrCreateTaxonForName(TaxonName taxonName, STATE state){
        if (taxonName != null){
	        Set<Taxon> acceptedTaxa = taxonName.getTaxa();
	        if(acceptedTaxa.size()>0){
	            Taxon firstAcceptedTaxon = acceptedTaxa.iterator().next();
	            if(acceptedTaxa.size()>1){
	                String message = "More than one accepted taxon was found for taxon name: "
	                        + taxonName.getTitleCache() + "!\n" + firstAcceptedTaxon + "was chosen for "+state.getDerivedUnitBase();
	                state.getReport().addInfoMessage(message);
	                logger.warn(message);
	            }
	            else{
	                return firstAcceptedTaxon;
	            }
	        }
	        else{
	            @SuppressWarnings("rawtypes")
                Set<TaxonBase> taxonAndSynonyms = taxonName.getTaxonBases();
	            for (TaxonBase<?> taxonBase : taxonAndSynonyms) {
	                if(taxonBase.isInstanceOf(Synonym.class)){
	                    Synonym synonym = HibernateProxyHelper.deproxy(taxonBase, Synonym.class);
	                    Taxon acceptedTaxonOfSynonym = synonym.getAcceptedTaxon();
	                    if(acceptedTaxonOfSynonym == null){
	                        String message = "No accepted taxon could be found for taxon name: "
	                                + taxonName.getTitleCache()
	                                + "!";
	                        state.getReport().addInfoMessage(message);
	                        logger.warn(message);
	                    }
	                    else{
	                        return acceptedTaxonOfSynonym;
	                    }
	                }
	            }
	        }
	        Taxon taxon = Taxon.NewInstance(taxonName, state.getRef());
	        save(taxon, state);
	        state.getReport().addTaxon(taxon);
	        logger.info("Created new taxon "+ taxon);
	        return taxon;
        }
        return null;
    }

    private boolean hasTaxonNodeInClassification(Taxon taxon, Classification classification){
        if(taxon.getTaxonNodes()!=null){
            for (TaxonNode node : taxon.getTaxonNodes()){
                if(node.getClassification().equals(classification)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * HandleIdentifications : get the scientific names present in the ABCD
     * document and store link them with the observation/specimen data
     * @param state: the current ABCD import state
     * @param derivedUnitFacade : the current derivedunitfacade
     */
    protected void handleIdentifications(STATE state, DerivedUnitFacade derivedUnitFacade) {

        SpecimenImportConfiguratorBase<?,?,?> config = state.getConfig();

        String scientificName = "";
        boolean preferredFlag = false;

        if (state.getDataHolder().getNomenclatureCode() == ""){
            if (config.getNomenclaturalCode() != null){
                if (config.getNomenclaturalCode() != null){
                    state.getDataHolder().setNomenclatureCode(config.getNomenclaturalCode().toString());

                }
            }
        }

        for (int i = 0; i < state.getDataHolder().getIdentificationList().size(); i++) {
            Identification identification = state.getDataHolder().getIdentificationList().get(i);
            scientificName = identification.getScientificName().replaceAll(" et ", " & ");

            String preferred = identification.getPreferred();
            preferredFlag = false;
            if (preferred != null || state.getDataHolder().getIdentificationList().size()==1){
                if (state.getDataHolder().getIdentificationList().size()==1){
                    preferredFlag = true;
                }else if (preferred != null && (preferred.equals("1") || preferred.toLowerCase().indexOf("true") != -1) ) {
	                preferredFlag = true;
	            }

            }
            if (identification.getCode() != null){
	            if (identification.getCode().indexOf(':') != -1) {
	                state.getDataHolder().setNomenclatureCode(identification.getCode().split(COLON)[1]);
	            }
	            else{
	                state.getDataHolder().setNomenclatureCode(identification.getCode());
	            }
            }
            TaxonName taxonName = getOrCreateTaxonName(scientificName, null, preferredFlag, state, i);
            Taxon taxon = getOrCreateTaxonForName(taxonName, state);
            addTaxonNode(taxon, state,preferredFlag);
            linkDeterminationEvent(state, taxon, preferredFlag, derivedUnitFacade, identification.getIdentifier(), identification.getDate(), identification.getModifier());
        }
    }

    /**
     * @param taxon : a taxon to add as a node
     * @param state : the ABCD import state
     */
    protected void addTaxonNode(Taxon taxon, STATE state, boolean preferredFlag) {
        SpecimenImportConfiguratorBase<?,?,?> config = state.getConfig();
        logger.info("link taxon to a taxonNode "+taxon.getTitleCache());
        //only add nodes if not already existing in current classification or default classification

        //check if node exists in current classification
        //NOTE: we cannot use hasTaxonNodeInClassification() here because we are first creating it here
        if (!existsInClassification(taxon,state.getClassification(), state)){
            if(config.isMoveNewTaxaToDefaultClassification()){
                //check if node exists in default classification
                if (!existsInClassification(taxon, state.getDefaultClassification(true), state)){
                    addParentTaxon(taxon, state, preferredFlag, state.getDefaultClassification(true));
                }
            }else{
                //add non-existing taxon to current classification
                addParentTaxon(taxon, state, preferredFlag, state.getClassification());
            }

        }
    }


    private boolean existsInClassification(Taxon taxon, Classification classification, STATE state){
        boolean exist = false;
        ICdmRepository cdmAppController = state.getConfig().getCdmAppController();
        if(cdmAppController==null){
            cdmAppController = this;
        }
        if (classification != null){
            if (!taxon.getTaxonNodes().isEmpty()){
                for (TaxonNode node:taxon.getTaxonNodes()){
                    if (node.getClassification().equals(classification)){
                        return true;
                    }
                }
            }
// we do not need this because we already searched for taxa in db in the previous steps
//    	        List<UuidAndTitleCache<TaxonNode>> uuidAndTitleCacheOfAllTaxa = cdmAppController.getClassificationService().getTaxonNodeUuidAndTitleCacheOfAcceptedTaxaByClassification(classification.getUuid());
//    	        if (uuidAndTitleCacheOfAllTaxa != null){
//        	        for (UuidAndTitleCache p : uuidAndTitleCacheOfAllTaxa){
//        	            try{
//        	                if(p.getTitleCache().equals(taxon.getTitleCache())) {
//        	                    exist = true;
//        	                }
//        	            }
//        	            catch(Exception e){
//        	                logger.warn("TaxonNode doesn't seem to have a taxon");
//        	            }
//        	        }
//    	        }
        }
        return exist;
    }

    /**
     * join DeterminationEvent to the Taxon Object
     * @param state : the ABCD import state
     * @param taxon: the current Taxon
     * @param preferredFlag :if the current name is preferred
     * @param derivedFacade : the derived Unit Facade
     */
    @SuppressWarnings("rawtypes")
    protected void linkDeterminationEvent(STATE state, Taxon taxon, boolean preferredFlag,  DerivedUnitFacade derivedFacade, String identifierStr, String dateStr, String modifier) {
        SpecimenImportConfiguratorBase config = state.getConfig();
        if (logger.isDebugEnabled()){
            logger.info("start linkdetermination with taxon:" + taxon.getUuid()+", "+taxon);
        }

        DeterminationEvent determinationEvent = DeterminationEvent.NewInstance();
        //determinationEvent.setTaxon(taxon);
        determinationEvent.setTaxonName(taxon.getName());
        determinationEvent.setPreferredFlag(preferredFlag);


        determinationEvent.setIdentifiedUnit(state.getDerivedUnitBase());
        if (state.getPersonStore().get(identifierStr) != null){
            determinationEvent.setActor((AgentBase)state.getPersonStore().get(identifierStr));
        } else if (identifierStr != null){
            Person identifier = Person.NewTitledInstance(identifierStr);
            determinationEvent.setActor(identifier);
        }
        if (dateStr != null){
            determinationEvent.setTimeperiod(TimePeriodParser.parseString(dateStr));
        }
        if (modifier != null){
            if (modifier.equals("cf.")){
                determinationEvent.setModifier(DefinedTerm.DETERMINATION_MODIFIER_CONFER());
            }else if (modifier.equals("aff.")){
                determinationEvent.setModifier(DefinedTerm.DETERMINATION_MODIFIER_AFFINIS());
            }
        }
        if (config.isAddDeterminations()) {
            state.getDerivedUnitBase().addDetermination(determinationEvent);
        }


        if (logger.isDebugEnabled()){
            logger.debug("NB TYPES INFO: "+ state.getDataHolder().getStatusList().size());
        }
        for (SpecimenTypeDesignationStatus specimenTypeDesignationstatus : state.getDataHolder().getStatusList()) {
            if (specimenTypeDesignationstatus != null) {
                if (logger.isDebugEnabled()){
                    logger.debug("specimenTypeDesignationstatus :"+ specimenTypeDesignationstatus);
                }

                ICdmRepository cdmAppController = config.getCdmAppController();
                if(cdmAppController == null){
                    cdmAppController = this;
                }
                specimenTypeDesignationstatus = HibernateProxyHelper.deproxy(cdmAppController.getTermService().find(specimenTypeDesignationstatus.getUuid()), SpecimenTypeDesignationStatus.class);
                //Designation
                TaxonName name = taxon.getName();
                SpecimenTypeDesignation designation = SpecimenTypeDesignation.NewInstance();

                designation.setTypeStatus(specimenTypeDesignationstatus);
                designation.setTypeSpecimen(state.getDerivedUnitBase());
                name.addTypeDesignation(designation, false);
            }
        }
        save(state.getDerivedUnitBase(), state);

        for (String[] fullReference : state.getDataHolder().getReferenceList()) {


            String strReference=fullReference[0];
            String citationDetail = fullReference[1];
            String citationURL = fullReference[2];
            List<Reference> references = getReferenceService().listByTitleWithRestrictions(Reference.class, "strReference", MatchMode.EXACT, null, null, null, null, null);

            if (!references.isEmpty()){
                Reference reference = null;
                for (Reference refe: references) {
                    if (refe.getTitleCache().equalsIgnoreCase(strReference)) {
                        reference =refe;
                        break;
                    }
                }
                if (reference ==null){
                    reference = ReferenceFactory.newGeneric();
                    reference.setTitleCache(strReference, true);
                    save(reference, state);
                }
                determinationEvent.addReference(reference);
            }
        }
        save(state.getDerivedUnitBase(), state);

        if (config.isAddIndividualsAssociations() && preferredFlag) {
            //do not add IndividualsAssociation to non-preferred taxa
            if (logger.isDebugEnabled()){
                logger.debug("isDoCreateIndividualsAssociations");
            }

            makeIndividualsAssociation(state, taxon, determinationEvent);

            save(state.getDerivedUnitBase(), state);
        }
    }

    /**
     * create and link each association (specimen, observation..) to the accepted taxon
     * @param state : the ABCD import state
     * @param taxon: the current Taxon
     * @param determinationEvent:the determinationevent
     */
    protected void makeIndividualsAssociation(STATE state, Taxon taxon, DeterminationEvent determinationEvent) {
        SpecimenImportConfiguratorBase<?,?,?> config = state.getConfig();
        SpecimenUserInteraction sui = config.getSpecimenUserInteraction();

        if (logger.isDebugEnabled()){
            logger.info("MAKE INDIVIDUALS ASSOCIATION");
        }

        TaxonDescription taxonDescription = null;
        Set<TaxonDescription> descriptions= taxon.getDescriptions();
        if (state.getIndividualsAssociationDescriptionPerTaxon(taxon.getUuid()) != null){
            taxonDescription = state.getIndividualsAssociationDescriptionPerTaxon(taxon.getUuid());
        }
       if (taxonDescription == null && !descriptions.isEmpty() && state.getConfig().isReuseExistingDescriptiveGroups()){
           for (TaxonDescription desc: descriptions){
               if (desc.getTypes().contains(DescriptionType.INDIVIDUALS_ASSOCIATION)){
                   taxonDescription = desc;
                   break;
               }
           }
       }

       if (taxonDescription == null){
            taxonDescription = TaxonDescription.NewInstance(taxon, false);
            taxonDescription.setTypes(EnumSet.of(DescriptionType.INDIVIDUALS_ASSOCIATION));
            if(sourceNotLinkedToElement(taxonDescription,state.getRef(),null)) {
                taxonDescription.addSource(OriginalSourceType.Import, null, null, state.getRef(), null);
            }
            state.setIndividualsAssociationDescriptionPerTaxon(taxonDescription);
            taxon.addDescription(taxonDescription);
        }

        //PREPARE REFERENCE QUESTIONS

        Map<String,OriginalSourceBase> sourceMap = new HashMap<>();

        List<IdentifiableSource> issTmp = new ArrayList<>();//getCommonService().list(IdentifiableSource.class, null, null, null, null);
        List<DescriptionElementSource> issTmp2 = new ArrayList<>();//getCommonService().list(DescriptionElementSource.class, null, null, null, null);

        Set<OriginalSourceBase> osbSet = new HashSet<>();
        if(issTmp2!=null) {
            osbSet.addAll(issTmp2);
        }
        if(issTmp!=null) {
            osbSet.addAll(issTmp);
        }


        addToSourceMap(sourceMap, osbSet);


        if(sourceNotLinkedToElement(taxonDescription,state.getRef(),null)) {
            taxonDescription.addSource(OriginalSourceType.Import,null, null, state.getRef(), null);
        }

        state.setIndividualsAssociationDescriptionPerTaxon(taxonDescription);

        IndividualsAssociation indAssociation = IndividualsAssociation.NewInstance();
        Feature feature = makeFeature(state.getDerivedUnitBase());
        indAssociation.setAssociatedSpecimenOrObservation(state.getDerivedUnitBase());
        indAssociation.setFeature(feature);

        if(sourceNotLinkedToElement(indAssociation,state.getImportReference(state.getActualAccessPoint()),null)) {
            indAssociation.addSource(OriginalSourceType.Import,null, null, state.getImportReference(state.getActualAccessPoint()), null);
        }
        if(sourceNotLinkedToElement(state.getDerivedUnitBase(), state.getImportReference(state.getActualAccessPoint()),null)) {
            state.getDerivedUnitBase().addSource(OriginalSourceType.Import,null, null, state.getImportReference(state.getActualAccessPoint()), null);
        }
        for (Reference citation : determinationEvent.getReferences()) {
            if(sourceNotLinkedToElement(indAssociation,citation,null))
            {
                indAssociation.addSource(DescriptionElementSource.NewInstance(OriginalSourceType.Import, null, null, citation, null));
            }
            if(sourceNotLinkedToElement(state.getDerivedUnitBase(), state.getImportReference(state.getActualAccessPoint()),null)) {
                state.getDerivedUnitBase().addSource(OriginalSourceType.Import,null, null, state.getImportReference(state.getActualAccessPoint()), null);
            }
        }

        taxonDescription.addElement(indAssociation);

        save(taxonDescription, state);
        save(taxon, state);
        state.getReport().addDerivate(state.getDerivedUnitBase(), config);
        state.getReport().addIndividualAssociation(taxon, state.getDataHolder().getUnitID(), state.getDerivedUnitBase());
    }

    private boolean sourceNotLinkedToElement(DerivedUnit derivedUnitBase2, Reference b, String d) {
        Set<IdentifiableSource> linkedSources = derivedUnitBase2.getSources();
        for (IdentifiableSource is:linkedSources){
            Reference a = is.getCitation();
            String c = is.getCitationMicroReference();

            boolean refMatch=false;
            boolean microMatch=false;

            try{
                if (a==null && b==null) {
                    refMatch=true;
                }
                if (a!=null && b!=null) {
                    if (a.getTitleCache().equalsIgnoreCase(b.getTitleCache())) {
                        refMatch=true;
                    }
                }
            }catch(Exception e){}

            try{
                if (c==null && d==null) {
                    microMatch=true;
                }
                if(c!=null && d!=null) {
                    if(c.equalsIgnoreCase(d)) {
                        microMatch=true;
                    }
                }
            }
            catch(Exception e){}

            if (microMatch && refMatch) {
                return false;
            }
        }
        return true;
    }

    private <T extends OriginalSourceBase> boolean  sourceNotLinkedToElement(
            ISourceable<T> sourcable, Reference reference, String microReference) {

        Set<T> linkedSources = sourcable.getSources();
        for (T is:linkedSources){
            Reference unitReference = is.getCitation();
            String unitMicroReference = is.getCitationMicroReference();

            boolean refMatch=false;
            boolean microMatch=false;

            try{
                if (unitReference==null && reference==null) {
                    refMatch=true;
                }
                if (unitReference!=null && reference!=null) {
                    if (unitReference.getTitleCache().equalsIgnoreCase(reference.getTitleCache())) {
                        refMatch=true;
                    }
                }
            }catch(Exception e){}

            try{
                if (unitMicroReference==null && microReference==null) {
                    microMatch=true;
                }
                if(unitMicroReference!=null && microReference!=null) {
                    if(unitMicroReference.equalsIgnoreCase(microReference)) {
                        microMatch=true;
                    }
                }
            }
            catch(Exception e){}

            if (microMatch && refMatch) {
                return false;
            }
        }
        return true;
    }

    /**
     * look for the Feature object (FieldObs, Specimen,...)
     * @param unit : a specimen or obersvation base
     * @return the corresponding Feature
     */
    private Feature makeFeature(SpecimenOrObservationBase<?> unit) {
        SpecimenOrObservationType type = unit.getRecordBasis();

        if (type.isFeatureObservation()){
            return Feature.OBSERVATION();
        }else if (type.isFeatureSpecimen()){
            return Feature.SPECIMEN();
        }else if (type == SpecimenOrObservationType.DerivedUnit){
            return Feature.OBSERVATION();
            //            return getFeature("Specimen or observation");
        }else{
            String message = "Unhandled record basis '%s' for defining individuals association feature type. Use default.";
            logger.warn(String.format(message, type.getLabel()));
            return Feature.OBSERVATION();
            //            return getFeature("Specimen or observation");
        }
    }

    protected void addToSourceMap(Map<String, OriginalSourceBase> sourceMap, Set<OriginalSourceBase> osbSet) {
        for( OriginalSourceBase osb:osbSet) {
            if(osb.getCitation()!=null && osb.getCitationMicroReference() !=null  && !osb.getCitationMicroReference().isEmpty()) {
                try{
                    sourceMap.put(osb.getCitation().getTitleCache()+ "---"+osb.getCitationMicroReference(),osb);
                }catch(NullPointerException e){logger.warn("null pointer problem (no ref?) with "+osb);}
            } else if(osb.getCitation()!=null){
                try{
                    sourceMap.put(osb.getCitation().getTitleCache(),osb);
                }catch(NullPointerException e){logger.warn("null pointer problem (no ref?) with "+osb);}
            }
        }
    }
}