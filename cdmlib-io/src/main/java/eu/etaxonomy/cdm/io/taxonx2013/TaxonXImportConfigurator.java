/**
 * Copyright (C) 2007 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */

package eu.etaxonomy.cdm.io.taxonx2013;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;


/**
 * @author a.mueller
 * @created 29.07.2008
 * @version 1.0
 */
public class TaxonXImportConfigurator extends ImportConfiguratorBase<TaxonXImportState, URI> implements IImportConfigurator {
    //	private static final Logger logger = Logger.getLogger(TaxonXImportConfigurator.class);

    private String defaultImportClassification = null;
    //if true the information in the mods part (taxonxHeader)
    private boolean doMods = true;
    private boolean doFacts = true;
    private boolean doTypes = true;
    private boolean alwaysUseDefaultClassification = false;


    //TODO
    private static IInputTransformer defaultTransformer = null;

    //the original TaxonXImport extracted Synonyms by creating acc Taxa with partial names
    //I (AM) do not understand this but don't want to destroy code which maybe works in some cases) there
    //I created this switch for old
    //for Spiders the new version is preferred
    private boolean isUseOldUnparsedSynonymExtraction = true;

    //if false references in this rdf file are not published in the bibliography list
    private boolean isPublishReferences = true;

    private String originalSourceTaxonNamespace = "TaxonConcept";
    private String originalSourceId;

    private Map<String, Person> titleCachePerson;
    private Map<String,UUID> namedAreaDecisions = new HashMap<String,UUID>();



    private static Reference sourceRef = null;

    private Reference secundum;
    private boolean keepOriginalSecundum;
    private Rank maxRank;
    private boolean askedForHRank =false;
    private Reference sourceURL;
    private boolean lastImport=false;


    @SuppressWarnings("unchecked")
    @Override
    protected void makeIoClassList(){
        ioClassList = new Class[]{
                TaxonXImport.class,
        };
    }

    /**
     * @param uri
     * @param destination
     * @return
     */
    public static TaxonXImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
        return new TaxonXImportConfigurator(uri, destination);
    }


    /**
     * @param url
     * @param destination
     */
    private TaxonXImportConfigurator(URI uri, ICdmDataSource destination) {
        super(defaultTransformer);
        setSource(uri);
        setDestination(destination);
    }

    /**
     * @param url
     * @param destination
     */
    private TaxonXImportConfigurator(ICdmDataSource destination) {
        super(defaultTransformer);
        setDestination(destination);
    }

    @SuppressWarnings("unchecked")
    @Override
    public TaxonXImportState getNewState() {
        return new TaxonXImportState(this);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Reference getSourceReference() {
        if (sourceReference == null){
            sourceReference =  ReferenceFactory.newGeneric();

            if (getSourceRefUuid() != null){
                sourceReference.setUuid(getSourceRefUuid());
            }
            if (sourceRef != null){
                sourceReference.setTitleCache(sourceRef.getTitleCache(), true);
            }
        }
        return sourceReference;
    }

    @Override
    public String getSourceNameString() {
        if (this.getSource() == null){
            return null;
        }else{
            return this.getSource().toString();
        }
    }

    public String getOriginalSourceTaxonNamespace() {
        return originalSourceTaxonNamespace;
    }

    public void setOriginalSourceTaxonNamespace(String originalSourceTaxonNamespace) {
        this.originalSourceTaxonNamespace = originalSourceTaxonNamespace;
    }

    public String getOriginalSourceId() {
        return originalSourceId;
    }

    public void setOriginalSourceId(String originalSourceId) {
        this.originalSourceId = originalSourceId;
    }


    /**
     * @return the doMods
     */
    public boolean isDoMods() {
        return doMods;
    }

    /**
     * @param doMods the doMods to set
     */
    public void setDoMods(boolean doMods) {
        this.doMods = doMods;
    }


    public boolean isDoFacts() {
        return doFacts;
    }
    public void setDoFacts(boolean doFacts) {
        this.doFacts = doFacts;
    }



    public boolean isDoTypes() {
        return doTypes;
    }
    public void setDoTypes(boolean doTypes) {
        this.doTypes = doTypes;
    }


    /**
     * @return the isPublishReferences
     */
    public boolean isPublishReferences() {
        return isPublishReferences;
    }

    /**
     * @param isPublishReferences the isPublishReferences to set
     */
    public void setPublishReferences(boolean isPublishReferences) {
        this.isPublishReferences = isPublishReferences;
    }

    /**
     * @param b
     */
    public void setDoAutomaticParsing(boolean b) {

    }

    /**
     * @param destination
     * @return
     */
    public static TaxonXImportConfigurator NewInstance(ICdmDataSource destination) {
        return new TaxonXImportConfigurator(destination);
    }

    /**
     * @param reference
     */
    public static void setSourceRef(Reference reference) {
        sourceRef = reference;

    }

    /**
     * @return
     */
    public Map<String, Person> getPersons() {
        return titleCachePerson;
    }

    /**
     * @param titleCachePerson
     */
    public void setPersons(Map<String, Person> titleCachePerson) {
        this.titleCachePerson=titleCachePerson;
        //System.out.println(titleCachePerson);

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
     * @return
     */
    public boolean doKeepOriginalSecundum() {
        return keepOriginalSecundum;
    }

    public void setKeepOriginalSecundum(boolean reuseSecundum) {
        this.keepOriginalSecundum = reuseSecundum;
    }

    /**
     * @return
     */
    public Reference getSecundum() {
        if(secundum == null){
            secundum = ReferenceFactory.newGeneric();
            secundum.setTitle("default secundum");
        }

        return secundum;
    }


    public void setSecundum(Reference reference){
        this.secundum=reference;
    }

    public Rank getMaxRank() {
        return maxRank;
    }

    public void setMaxRank(Rank maxRank) {
        this.maxRank = maxRank;
    }


    public boolean hasAskedForHigherRank(){
        return askedForHRank;
    }

    public void setHasAskedForHigherRank(boolean asked){
        askedForHRank=asked;
    }

    /**
     * @return
     */
    public String getImportClassificationName() {
       return defaultImportClassification;
    }

    public void setImportClassificationName(String className){
        defaultImportClassification=className;
    }

    /**
     * @param referenceUrl
     */
    public void addOriginalSource(Reference referenceUrl) {
       this.sourceURL = referenceUrl;

    }

    /**
     * @return the sourceURL
     */
    public Reference getOriginalSourceURL() {
        return sourceURL;
    }

    /**
     * @param b
     */
    public void setLastImport(boolean b) {
        lastImport=b;
    }

    public boolean getLastImport(){
        return  lastImport;
    }

	public boolean isAlwaysUseDefaultClassification() {
		return alwaysUseDefaultClassification;
	}

	public void setAlwaysUseDefaultClassification(
			boolean alwaysUseDefaultClassification) {
		this.alwaysUseDefaultClassification = alwaysUseDefaultClassification;
	}


	public boolean isUseOldUnparsedSynonymExtraction() {
		return isUseOldUnparsedSynonymExtraction;
	}

	public void setUseOldUnparsedSynonymExtraction(boolean isUseOldUnparsedSynonymExtraction) {
		this.isUseOldUnparsedSynonymExtraction = isUseOldUnparsedSynonymExtraction;
	}



}
