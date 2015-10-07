/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.specimen.abcd206.in;


import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.UriUtils;
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
public class Abcd206ImportConfigurator extends ImportConfiguratorBase<Abcd206ImportState, InputStream> implements IImportConfigurator, IMatchingImportConfigurator {
    private static final Logger logger = Logger.getLogger(Abcd206ImportConfigurator.class);

    private static String sourceReferenceTitle = null;
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
    private boolean addMediaAsMediaSpecimen = false;
    private boolean deduplicateReferences = false;
    private boolean deduplicateClassifications = false;
    private boolean ignoreAuthorship = false;
    private boolean removeCountryFromLocalityText = false;
    private boolean moveNewTaxaToDefaultClassification = true;

    private boolean mapUnitIdToCatalogNumber = true;
    private boolean mapUnitIdToAccessionNumber = false;
    private boolean mapUnitIdToBarcode = false;

    private boolean overwriteExistingSpecimens = false;
    private boolean ignoreImportOfExistingSpecimens = true;

    private final SpecimenUserInteraction specimenUserInteraction = new SpecimenUserInteraction();

    private Map<String,UUID> namedAreaDecisions = new HashMap<String,UUID>();

    //TODO
    private static IInputTransformer defaultTransformer = null;

    private URI sourceUri;
    private URI reportUri;


    @Override
    @SuppressWarnings("unchecked")
    protected void makeIoClassList(){
        System.out.println("makeIOClassList");
        ioClassList = new Class[]{
                Abcd206Import.class,
        };
    }

    public static Abcd206ImportConfigurator NewInstance(URI uri,ICdmDataSource destination){
        return new Abcd206ImportConfigurator(null, uri, destination, false);
    }

    /**
     * @param uri
     * @param object
     * @param b
     * @return
     */
    public static Abcd206ImportConfigurator NewInstance(URI uri, ICdmDataSource destination, boolean interact) {
        return new Abcd206ImportConfigurator(null, uri, destination, interact);
    }

    /**
     * @param uri
     * @param object
     * @param b
     * @return
     */
    public static Abcd206ImportConfigurator NewInstance(InputStream stream, ICdmDataSource destination, boolean interact) {
        return new Abcd206ImportConfigurator(stream, null, destination, interact);
    }



    /**
     * @param berlinModelSource
     * @param sourceReference
     * @param destination
     */
    private Abcd206ImportConfigurator(InputStream stream, URI uri, ICdmDataSource destination, boolean interact) {
        super(defaultTransformer);
        if (stream != null){
        	setSource(stream);
        }else{
        	this.sourceUri = uri;
        }
        setDestination(destination);
        setSourceReferenceTitle("ABCD classic");
        setInteractWithUser(interact);
    }





    @Override
    public Abcd206ImportState getNewState() {
        return new Abcd206ImportState(this);
    }


    @Override
    public InputStream getSource(){
        if (super.getSource() != null){
        	return super.getSource();
        }else if (this.sourceUri != null){
        	try {
				InputStream is = UriUtils.getInputStream(sourceUri);
				setSource(is);
				return is;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
        }else{
        	return null;
        }
    }

    @Override
    public void setSource(InputStream is) {
    	this.sourceUri = null;
    	super.setSource(is);
    }

    public URI getSourceUri(){
    	return this.sourceUri;
    }

    public void setSourceUri(URI sourceUri) {
        this.sourceUri = sourceUri;
        super.setSource(null);
    }


    @Override
    public String getSourceReferenceTitle(){
        return this.sourceReferenceTitle;
    }

    @Override
    public void setSourceReferenceTitle(String name){
        this.sourceReferenceTitle=name;
    }

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

    @Override
    public boolean isReuseExistingTaxaWhenPossible() {
        return reuseExistingTaxaWhenPossible;
    }

    @Override
    public void setReuseExistingTaxaWhenPossible(boolean doMatchTaxa) {
        this.reuseExistingTaxaWhenPossible = doMatchTaxa;
    }

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

    public boolean isAddMediaAsMediaSpecimen() {
        return addMediaAsMediaSpecimen;
    }

    public void setAddMediaAsMediaSpecimen(boolean addMediaAsMediaSpecimen) {
        this.addMediaAsMediaSpecimen = addMediaAsMediaSpecimen;
    }

    public boolean isDeduplicateClassifications() {
        return deduplicateClassifications;
    }

    public void setDeduplicateClassifications(boolean deduplicateClassifications) {
        this.deduplicateClassifications = deduplicateClassifications;
    }

    public boolean isDeduplicateReferences() {
        return deduplicateReferences;
    }

    public void setDeduplicateReferences(boolean deduplicateReferences) {
        this.deduplicateReferences = deduplicateReferences;
    }

    public boolean isAllowReuseOtherClassifications() {
        return allowReuseOtherClassifications;
    }

    public void setAllowReuseOtherClassifications(boolean allowReuseOtherClassifications) {
        this.allowReuseOtherClassifications = allowReuseOtherClassifications;
    }

    public SpecimenUserInteraction getSpecimenUserInteraction() {
        return specimenUserInteraction;
    }

    public void setReportUri(URI reportUri) {
        this.reportUri = reportUri;
    }

    public URI getReportUri() {
        return reportUri;
    }

    public void setIgnoreAuthorship(boolean ignoreAuthorship) {
        this.ignoreAuthorship = ignoreAuthorship;
    }

    public boolean isIgnoreAuthorship() {
        return ignoreAuthorship;
    }

    public boolean isMapUnitIdToAccessionNumber() {
        return mapUnitIdToAccessionNumber;
    }

    public boolean isMapUnitIdToBarcode() {
        return mapUnitIdToBarcode;
    }

    public boolean isMapUnitIdToCatalogNumber() {
        return mapUnitIdToCatalogNumber;
    }

    public void setMapUnitIdToAccessionNumber(boolean mapUnitIdToAccessionNumber) {
        this.mapUnitIdToAccessionNumber = mapUnitIdToAccessionNumber;
    }

    public void setMapUnitIdToBarcode(boolean mapUnitIdToBarcode) {
        this.mapUnitIdToBarcode = mapUnitIdToBarcode;
    }

    public void setMapUnitIdToCatalogNumber(boolean mapUnitIdToCatalogNumber) {
        this.mapUnitIdToCatalogNumber = mapUnitIdToCatalogNumber;
    }

    public void setRemoveCountryFromLocalityText(boolean removeCountryFromLocalityText) {
        this.removeCountryFromLocalityText = removeCountryFromLocalityText;
    }

    public boolean isRemoveCountryFromLocalityText() {
        return removeCountryFromLocalityText;
    }

    public boolean isMoveNewTaxaToDefaultClassification() {
        return moveNewTaxaToDefaultClassification;
    }

    public void setMoveNewTaxaToDefaultClassification(boolean moveNewTaxaToDefaultClassification) {
        this.moveNewTaxaToDefaultClassification = moveNewTaxaToDefaultClassification;
    }

    public boolean isOverwriteExistingSpecimens() {
        return overwriteExistingSpecimens;
    }

    public void setOverwriteExistingSpecimens(boolean overwriteExistingSpecimens) {
        this.overwriteExistingSpecimens = overwriteExistingSpecimens;
    }

    public boolean isIgnoreImportOfExistingSpecimens() {
        return ignoreImportOfExistingSpecimens;
    }

    public void setIgnoreImportOfExistingSpecimens(boolean ignoreImportOfExistingSpecimens) {
        this.ignoreImportOfExistingSpecimens = ignoreImportOfExistingSpecimens;
    }
}
