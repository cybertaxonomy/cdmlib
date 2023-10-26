/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.common.URI;
import eu.etaxonomy.cdm.ext.occurrence.OccurenceQuery;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationType;

/**
 * @author k.luther
 * @since 15.07.2016
 */
public abstract class SpecimenImportConfiguratorBase<CONFIG extends SpecimenImportConfiguratorBase, STATE extends SpecimenImportStateBase<CONFIG,STATE>, InputStream>
        extends ImportConfiguratorBase<STATE, InputStream> {

    private static final long serialVersionUID = 4741134251527063988L;

    public SpecimenImportConfiguratorBase(IInputTransformer transformer) {
        super(transformer);
    }

    //im-/export uses Classification for is_taxonomically_included_in relationships
    private boolean useClassification = true;

    private boolean ignoreImportOfExistingSpecimen = true;
    private boolean reuseExistingTaxaWhenPossible = true;
    private final Map<UUID, UUID> taxonToDescriptionMap = new HashMap<>();

    private Map<String, Team> titleCacheTeam;
    private Map<String, Person> titleCachePerson;
    private boolean ignoreAuthorship = false;
    private boolean removeCountryFromLocalityText = false;
    protected OccurenceQuery query ;

    private boolean addMediaAsMediaSpecimen = false;
    private boolean reuseExistingMetaData = true;

    private String sourceReferenceTitle = null;

    private String taxonReference = null;
    private boolean addIndividualsAssociations = true;
    private boolean addDeterminations = false;
    private boolean reuseExistingDescriptiveGroups = false;


    private String defaultAuthor="";
    private boolean allowReuseOtherClassifications =true;

    private boolean deduplicateReferences = false;
    private boolean deduplicateClassifications = false;

    private boolean moveNewTaxaToDefaultClassification = true;

    private boolean mapUnitIdToCatalogNumber = true;
    private boolean mapUnitIdToAccessionNumber = false;
    private boolean mapUnitIdToBarcode = false;

    private boolean overwriteExistingSpecimens = false;
    private boolean downloadSequenceData = false;

    private SpecimenOrObservationType type;

    private final SpecimenUserInteraction specimenUserInteraction = new SpecimenUserInteraction();

    protected Map<String,UUID> namedAreaDecisions = new HashMap<>();

    private URI reportUri;

    public SpecimenOrObservationType getType() {
        return type;
    }

    public void setType(SpecimenOrObservationType type) {
        this.type = type;
    }

    public boolean isIgnoreImportOfExistingSpecimen() {
        return ignoreImportOfExistingSpecimen;
    }

    public void setIgnoreImportOfExistingSpecimen(boolean ignoreImportOfExistingSpecimen) {
        this.ignoreImportOfExistingSpecimen = ignoreImportOfExistingSpecimen;
    }

    public Map<String, Team> getTitleCacheTeam() {
        return titleCacheTeam;
    }

    public void setTitleCacheTeam(Map<String, Team> titleCacheTeam) {
        this.titleCacheTeam = titleCacheTeam;
    }

    public Map<String, Person> getTitleCachePerson() {
        return titleCachePerson;
    }

    public void setTitleCachePerson(Map<String, Person> titleCachePerson) {
        this.titleCachePerson = titleCachePerson;
    }

    public OccurenceQuery getQuery() {
        return query;
    }

    public void setQuery(OccurenceQuery query) {
        this.query = query;
    }

    public String getTaxonReference() {
        return taxonReference;
    }

    public void setTaxonReference(String taxonReference) {
        this.taxonReference = taxonReference;
    }

    public boolean isAddIndividualsAssociations() {
        return addIndividualsAssociations;
    }

    public boolean isAddDeterminations() {
        return addDeterminations;
    }

    public void setAddDeterminations(boolean addDeterminations) {
        this.addDeterminations = addDeterminations;
    }

    public void setAddIndividualsAssociations(
            boolean addIndividualsAssociations) {
        this.addIndividualsAssociations = addIndividualsAssociations;
    }

    public boolean isReuseExistingDescriptiveGroups() {
        return reuseExistingDescriptiveGroups;
    }

    public void setReuseExistingDescriptiveGroups(boolean reuseExistingDescriptiveGroups) {
        this.reuseExistingDescriptiveGroups = reuseExistingDescriptiveGroups;
    }

    public String getDefaultAuthor() {
        return defaultAuthor;
    }

    public void setDefaultAuthor(String defaultAuthor) {
        this.defaultAuthor = defaultAuthor;
    }

    public boolean isAllowReuseOtherClassifications() {
        return allowReuseOtherClassifications;
    }

    public void setAllowReuseOtherClassifications(boolean allowReuseOtherClassifications) {
        this.allowReuseOtherClassifications = allowReuseOtherClassifications;
    }

    public boolean isDeduplicateReferences() {
        return deduplicateReferences;
    }

    public void setDeduplicateReferences(boolean deduplicateReferences) {
        this.deduplicateReferences = deduplicateReferences;
    }

    public boolean isDeduplicateClassifications() {
        return deduplicateClassifications;
    }

    public void setDeduplicateClassifications(boolean deduplicateClassifications) {
        this.deduplicateClassifications = deduplicateClassifications;
    }

    public boolean isMoveNewTaxaToDefaultClassification() {
        return moveNewTaxaToDefaultClassification;
    }

    public void setMoveNewTaxaToDefaultClassification(boolean moveNewTaxaToDefaultClassification) {
        this.moveNewTaxaToDefaultClassification = moveNewTaxaToDefaultClassification;
    }

    public boolean isMapUnitIdToCatalogNumber() {
        return mapUnitIdToCatalogNumber;
    }

    public void setMapUnitIdToCatalogNumber(boolean mapUnitIdToCatalogNumber) {
        this.mapUnitIdToCatalogNumber = mapUnitIdToCatalogNumber;
    }

    public boolean isMapUnitIdToAccessionNumber() {
        return mapUnitIdToAccessionNumber;
    }

    public void setMapUnitIdToAccessionNumber(boolean mapUnitIdToAccessionNumber) {
        this.mapUnitIdToAccessionNumber = mapUnitIdToAccessionNumber;
    }

    public boolean isMapUnitIdToBarcode() {
        return mapUnitIdToBarcode;
    }

    public void setMapUnitIdToBarcode(boolean mapUnitIdToBarcode) {
        this.mapUnitIdToBarcode = mapUnitIdToBarcode;
    }

    public Map<String, UUID> getNamedAreaDecisions() {
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

    public Map<UUID, UUID> getTaxonToDescriptionMap() {
        return taxonToDescriptionMap;
    }



    public SpecimenUserInteraction getSpecimenUserInteraction() {
        return specimenUserInteraction;
    }

    public void setReuseExistingTaxaWhenPossible(boolean reuseExistingTaxaWhenPossible) {
        this.reuseExistingTaxaWhenPossible = reuseExistingTaxaWhenPossible;
    }

    public void setIgnoreAuthorship(boolean ignoreAuthorship) {
        this.ignoreAuthorship = ignoreAuthorship;
    }

    public void setRemoveCountryFromLocalityText(boolean removeCountryFromLocalityText) {
        this.removeCountryFromLocalityText = removeCountryFromLocalityText;
    }

    public void setAddMediaAsMediaSpecimen(boolean addMediaAsMediaSpecimen) {
        this.addMediaAsMediaSpecimen = addMediaAsMediaSpecimen;
    }

    public void setOverwriteExistingSpecimens(boolean overwriteExistingSpecimens) {
        this.overwriteExistingSpecimens = overwriteExistingSpecimens;
    }

    public boolean isOverwriteExistingSpecimens(){
        return overwriteExistingSpecimens;
    }

    public boolean isReuseExistingTaxaWhenPossible() {
        return reuseExistingTaxaWhenPossible;
    }

    public void setTeams(Map<String, Team> titleCacheTeam) {
       this.titleCacheTeam  = titleCacheTeam;
    }

    public Team getTeam(String titleCache){
        return titleCacheTeam.get(titleCache);
    }

    public Map<String, Team> getTeams(){
        return titleCacheTeam;
    }

    public void setPersons(Map<String, Person> titleCachePerson) {
        this.titleCachePerson = titleCachePerson;
    }

    public Map<String, Person> getPersons(){
        return titleCachePerson;
    }

    public Person getPerson(String titleCache){
        return titleCachePerson.get(titleCache);
    }

    public boolean isAddMediaAsMediaSpecimen(){
        return addMediaAsMediaSpecimen;
    }

    public void addMediaAsMediaSpecimen(boolean addMediaAsMediaSpecimen){
       this.addMediaAsMediaSpecimen = addMediaAsMediaSpecimen;
    }

    public boolean isIgnoreAuthorship() {
        return ignoreAuthorship;
    }

    public boolean isRemoveCountryFromLocalityText() {
        return removeCountryFromLocalityText;
    }

    public OccurenceQuery getOccurenceQuery(){
        return query;
    }

    public void setOccurenceQuery(OccurenceQuery query){
        this.query = query;
    }

    public boolean isReuseExistingMetaData() {
        return reuseExistingMetaData;
    }

    public void setReuseExistingMetaData(boolean reuseExistingMetaData) {
        this.reuseExistingMetaData = reuseExistingMetaData;
    }

    public void setReportUri(URI reportUri) {
        this.reportUri = reportUri;
    }

    public URI getReportUri() {
        return reportUri;
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
    public boolean isValid(){
        return true;
    }

    public boolean isDownloadSequenceData() {
        return downloadSequenceData;
    }

    public void setDownloadSequenceData(boolean downloadSequenceData) {
        this.downloadSequenceData = downloadSequenceData;
    }

    public boolean isUseClassification() {
        return useClassification;
    }
    public void setUseClassification(boolean useClassification) {
        this.useClassification = useClassification;
    }
}