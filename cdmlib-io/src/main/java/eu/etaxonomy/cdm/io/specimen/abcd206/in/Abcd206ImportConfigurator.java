/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.specimen.abcd206.in;


import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IMatchingImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.specimen.SpecimenUserInteraction;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author p.kelbert
 * @created 20.10.2008
 * @version 1.0
 */
public class Abcd206ImportConfigurator extends ImportConfiguratorBase<Abcd206ImportState, URI> implements IImportConfigurator, IMatchingImportConfigurator {
    private static final Logger logger = Logger.getLogger(Abcd206ImportConfigurator.class);
    private static String sourceReferenceTitle = null;
    private boolean parseNameAutomatically = false;
    private boolean reuseExistingMetadata = true;
    private String taxonReference = null;
    private boolean addIndividualsAssociationsSuchAsSpecimenAndObservations = true;
    private boolean reuseExistingDescriptiveGroups = false;
    private boolean reuseExistingTaxaWhenPossible = true;
    private final Map<UUID, UUID> taxonToDescriptionMap = new HashMap<UUID, UUID>();
    private Map<String, Team> titleCacheTeam;
    private Map<String, Person> titleCachePerson;
    private String defaultAuthor="";
    private boolean allowReuseOtherClassifications =true;

    private final SpecimenUserInteraction specimenUserInteraction = new SpecimenUserInteraction();

    private Map<String,UUID> namedAreaDecisions = new HashMap<String,UUID>();

    //TODO
    private static IInputTransformer defaultTransformer = null;

    @Override
    @SuppressWarnings("unchecked")
    protected void makeIoClassList(){
        System.out.println("makeIOClassList");
        ioClassList = new Class[]{
                Abcd206Import.class,
        };
    };

    public static Abcd206ImportConfigurator NewInstance(URI uri,
            ICdmDataSource destination){
        return new Abcd206ImportConfigurator(uri, destination);
    }

    /**
     * @param uri
     * @param object
     * @param b
     * @return
     */
    public static Abcd206ImportConfigurator NewInstance(URI uri, ICdmDataSource destination, boolean interact) {
        return new Abcd206ImportConfigurator(uri, destination,interact);
    }


    /**
     * @param berlinModelSource
     * @param sourceReference
     * @param destination
     */
    private Abcd206ImportConfigurator(URI uri, ICdmDataSource destination) {
        super(defaultTransformer);
        setSource(uri);
        setDestination(destination);
        setSourceReferenceTitle("ABCD classic");
    }

    /**
     * @param berlinModelSource
     * @param sourceReference
     * @param destination
     */
    private Abcd206ImportConfigurator(URI uri, ICdmDataSource destination, boolean interact) {
        super(defaultTransformer);
        setSource(uri);
        setDestination(destination);
        setSourceReferenceTitle("ABCD classic");
        setInteractWithUser(interact);
    }





    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
     */
    @Override
    public Abcd206ImportState getNewState() {
        return new Abcd206ImportState(this);
    }


    @Override
    public URI getSource(){
        return super.getSource();
    }

    /**
     * @param file
     */
    @Override
    public void setSource(URI uri) {
        super.setSource(uri);
    }


    @Override
    public String getSourceReferenceTitle(){
        return this.sourceReferenceTitle;
    }

    @Override
    public void setSourceReferenceTitle(String name){
        this.sourceReferenceTitle=name;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
     */
    @Override
    public Reference getSourceReference() {
        //TODO
        return sourceReference;
    }

    public void setTaxonReference(String taxonReference) {
        this.taxonReference = taxonReference;
    }

    public Reference getTaxonReference() {
        //TODO
        if (this.taxonReference == null){
            logger.info("getTaxonReference not yet fully implemented");
        }
        return sourceReference;
    }

    public void setParseNameAutomatically(boolean doParsing){
        this.parseNameAutomatically=doParsing;
    }

    public boolean isParseNameAutomatically(){
        return this.parseNameAutomatically;
    }

    public void setReuseExistingMetadata(boolean reuseMetadata){
        this.reuseExistingMetadata = reuseMetadata;
    }

    public boolean isReUseExistingMetadata(){
        return this.reuseExistingMetadata;
    }

    public void setAddIndividualsAssociationsSuchAsSpecimenAndObservations(
            boolean doCreateIndividualsAssociations) {
        this.addIndividualsAssociationsSuchAsSpecimenAndObservations = doCreateIndividualsAssociations;
    }

    /**
     * Create an IndividualsAssociations for each determination element in the ABCD data. ABCD has no such concept as IndividualsAssociations so the only way to
     *
     * @return
     */
    public boolean isAddIndividualsAssociationsSuchAsSpecimenAndObservations() {
        return addIndividualsAssociationsSuchAsSpecimenAndObservations;
    }

    /**
     * @param doReuseExistingDescription the doReuseExistingDescription to set
     * NOT USED YET
     */
    public void reuseExistingDescriptiveGroups(boolean doReuseExistingDescription) {
        this.reuseExistingDescriptiveGroups = doReuseExistingDescription;
    }

    /**
     * @return the doReuseExistingDescription
     */
    public boolean isReuseExistingDescriptiveGroups() {
        return reuseExistingDescriptiveGroups;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.common.IMatchingImportConfigurator#isDoMatchTaxa()
     */
    @Override
    public boolean isReuseExistingTaxaWhenPossible() {
        return reuseExistingTaxaWhenPossible;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.io.common.IMatchingImportConfigurator#setDoMatchTaxa(boolean)
     */
    @Override
    public void setReuseExistingTaxaWhenPossible(boolean doMatchTaxa) {
        this.reuseExistingTaxaWhenPossible = doMatchTaxa;
    }

    /**
     * @return
     */
    public Map<UUID, UUID> getTaxonToDescriptionMap() {
        // TODO Auto-generated method stub
        return taxonToDescriptionMap ;
    }



    public Map<String, Team> getTeams() {
        return titleCacheTeam;
    }

    public void setTeams(Map<String, Team> titleCacheTeam) {
        this.titleCacheTeam = titleCacheTeam;
    }

    public Map<String, Person> getPersons() {
        return titleCachePerson;
    }

    public void setPersons(Map<String, Person> titleCachePerson) {
        this.titleCachePerson = titleCachePerson;
    }

    /**
     * @param string
     */
    public void setDefaultAuthor(String string) {
        defaultAuthor=string;

    }

    public String getDefaultAuthor(){
        return defaultAuthor;
    }

    public Map<String,UUID> getNamedAreaDecisions() {
        return namedAreaDecisions;
    }

    public void setNamedAreaDecisions(Map<String,UUID> namedAreaDecisions) {
        this.namedAreaDecisions = namedAreaDecisions;
    }

    public void putNamedAreaDecision(String areaStr,UUID uuid){
        this.namedAreaDecisions.put(areaStr,uuid);
    }

    public UUID getNamedAreaDecision(String areaStr){
        return namedAreaDecisions.get(areaStr);
    }


    /**
     * @return the allowReuseOtherClassifications
     */
    public boolean isAllowReuseOtherClassifications() {
        return allowReuseOtherClassifications;
    }

    /**
     * @param allowReuseOtherClassifications the allowReuseOtherClassifications to set
     */
    public void setAllowReuseOtherClassifications(boolean allowReuseOtherClassifications) {
        this.allowReuseOtherClassifications = allowReuseOtherClassifications;
    }

    /**
     * @return the specimenUserInteraction
     */
    public SpecimenUserInteraction getSpecimenUserInteraction() {
        return specimenUserInteraction;
    }




}
