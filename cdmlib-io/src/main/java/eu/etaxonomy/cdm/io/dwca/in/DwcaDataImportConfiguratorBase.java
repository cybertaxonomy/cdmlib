/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.dwca.in;


import java.net.URI;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.stream.StreamImportConfiguratorBase;

/**
 * @author a.mueller
 * @created 04.06.2013
 */
public abstract class DwcaDataImportConfiguratorBase<STATE extends DwcaDataImportStateBase>
        extends StreamImportConfiguratorBase<STATE, URI> implements IImportConfigurator {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaDataImportConfiguratorBase.class);
//	private static IInputTransformer defaultTransformer = new DwcaImportTransformer();

//	private static final String DEFAULT_REF_TITLE = "DwC-A Import";

	private boolean doSplitRelationshipImport = false;
	private boolean doSynonymRelationships = true;
	private boolean doHigherRankRelationships = true;
	private boolean doLowerRankRelationships = true;

	//taxon
	private boolean deduplicateNamePublishedIn = true;
	private boolean scientificNameIdAsOriginalSourceId = false;
	private DatasetUse datasetUse = DatasetUse.CLASSIFICATION;
	private boolean useSourceReferenceAsSec = false;
	private boolean useParentAsAcceptedIfAcceptedNotExists = true;

	//distribution
	private boolean excludeLocality = false;   //if set to true the dwc locality is not considered during distribution import

	//reference
	private boolean guessNomenclaturalReferences = false;
	private boolean handleAllRefsAsCitation = false;

	//validation
	private boolean validateRankConsistency = true;

	public enum DatasetUse{
		CLASSIFICATION,
		SECUNDUM,
		ORIGINAL_SOURCE
	}


	/**
	 * @param uri
	 * @param destination
	 * @param defaultTransformer
	 */
	protected DwcaDataImportConfiguratorBase(URI uri, ICdmDataSource destination, IInputTransformer defaultTransformer) {
		super(defaultTransformer);
		this.setSource(uri);
		this.setDestination(destination);
	}

//************************** GETTER / SETTER **********************************/

	public boolean isDeduplicateNamePublishedIn() {
		return deduplicateNamePublishedIn;
	}

	public void setDeduplicateNamePublishedIn(boolean deduplicateNamePublishedIn) {
		this.deduplicateNamePublishedIn = deduplicateNamePublishedIn;
	}

	public void setScientificNameIdAsOriginalSourceId(
			boolean scientificNameIdAsOriginalSourceId) {
		this.scientificNameIdAsOriginalSourceId = scientificNameIdAsOriginalSourceId;
	}

	public boolean isScientificNameIdAsOriginalSourceId() {
		return scientificNameIdAsOriginalSourceId;
	}

	public boolean isDatasetsAsClassifications() {
		return this.datasetUse.equals(DatasetUse.CLASSIFICATION);
	}

	public boolean isExcludeLocality() {
		return excludeLocality;
	}

	public void setExcludeLocality(boolean excludeLocality) {
		this.excludeLocality = excludeLocality;
	}

	public boolean isValidateRankConsistency() {
		return validateRankConsistency;
	}

	public void setValidateRankConsistency(boolean validateRankConsistency) {
		this.validateRankConsistency = validateRankConsistency;
	}

	public boolean isDatasetsAsSecundumReference() {
		return this.datasetUse.equals(DatasetUse.SECUNDUM);
	}

	public boolean isDatasetsAsOriginalSource() {
		return this.datasetUse.equals(DatasetUse.ORIGINAL_SOURCE);
	}


	public void setDatasetUse(DatasetUse datasetUse) {
		this.datasetUse = datasetUse;
	}

	public boolean isGuessNomenclaturalReferences() {
		return guessNomenclaturalReferences;
	}

	public void setGuessNomenclaturalReferences(boolean guessNomenclaturalReferences) {
		this.guessNomenclaturalReferences = guessNomenclaturalReferences;
	}

	public boolean isHandleAllRefsAsCitation() {
		return handleAllRefsAsCitation;
	}

	public void setHandleAllRefsAsCitation(boolean handleAllRefsAsCitation) {
		this.handleAllRefsAsCitation = handleAllRefsAsCitation;
	}

	public boolean isUseSourceReferenceAsSec() {
		return useSourceReferenceAsSec;
	}

	public void setUseSourceReferenceAsSec(boolean useSourceReferenceAsSec) {
		this.useSourceReferenceAsSec = useSourceReferenceAsSec;
	}

	public boolean isUseParentAsAcceptedIfAcceptedNotExists() {
		return useParentAsAcceptedIfAcceptedNotExists;
	}

	public void setUseParentAsAcceptedIfAcceptedNotExists(boolean useParentAsAcceptedIfAcceptedNotExists) {
		this.useParentAsAcceptedIfAcceptedNotExists = useParentAsAcceptedIfAcceptedNotExists;
	}


    /**
     * @return the doSplitRelationshipImport
     */
    public boolean isDoSplitRelationshipImport() {
        return doSplitRelationshipImport;
    }

    /**
     * @param doSplitRelationshipImport the doSplitRelationshipImport to set
     */
    public void setDoSplitRelationshipImport(boolean doSplitRelationshipImport) {
        this.doSplitRelationshipImport = doSplitRelationshipImport;
    }

    /**
     * @return the doSynonymRelationships
     */
    public boolean isDoSynonymRelationships() {
        return doSynonymRelationships;
    }

    /**
     * @param doSynonymRelationships the doSynonymRelationships to set
     */
    public void setDoSynonymRelationships(boolean doSynonymRelationships) {
        this.doSynonymRelationships = doSynonymRelationships;
    }

    /**
     * @return the doHigherRankRelationships
     */
    public boolean isDoHigherRankRelationships() {
        return doHigherRankRelationships;
    }

    /**
     * @param doHigherRankRelationships the doHigherRankRelationships to set
     */
    public void setDoHigherRankRelationships(boolean doHigherRankRelationships) {
        this.doHigherRankRelationships = doHigherRankRelationships;
    }

    /**
     * @return the doLowerRankRelationships
     */
    public boolean isDoLowerRankRelationships() {
        return doLowerRankRelationships;
    }

    /**
     * @param doLowerRankRelationships the doLowerRankRelationships to set
     */
    public void setDoLowerRankRelationships(boolean doLowerRankRelationships) {
        this.doLowerRankRelationships = doLowerRankRelationships;
    }
}
