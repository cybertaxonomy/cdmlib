/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.markup;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.XmlImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

@Component
public class MarkupImportConfigurator extends XmlImportConfiguratorBase<MarkupImportState> implements IImportConfigurator {
    private static final long serialVersionUID = 779807547137921079L;

    private static final Logger logger = Logger.getLogger(MarkupImportConfigurator.class);

	public static MarkupImportConfigurator NewInstance(URI uri, ICdmDataSource destination){
		return new MarkupImportConfigurator(uri, destination);
	}

	private boolean replaceStandardKeyTitles = true;

	private boolean doTaxa = true;

	private boolean reuseExistingState = false;

	private boolean allowCapitalSpeciesEpithet = false;  //set to true if you want to allow specific epithets with capital letter at the beginning. This was allowed by the code for epithets referring to persons such as Beilschmiedia Zenkeri.

	private boolean handlePagesAsDetailWhereNeeded = true;  //often details in publications and citations are tagged as pages, not as details. If value is true, pages are handled as details where possible

	private boolean useEditorAsInAuthorWhereNeeded = true;  //often the inAuthor is stored as "Editor" in citations, atleast in FM.

	//TODO
	private static IInputTransformer defaultTransformer = null;
	private String sourceReferenceTitle = "E-Flora";
	private UUID defaultLanguageUuid;

    private List<String> knownCollections = new ArrayList<>();

    private boolean ignoreLocalityClass = false;

    private boolean handleWriterManually = false;


    private UUID specimenNotSeenMarkerTypeUuid = MarkupTransformer.uuidMarkerNotSeen;
    private String specimenNotSeenMarkerTypeLabel;


    //TODO move to state, but a state gets lost after each import.invoke, so I can't move this information
	//from the one import to another import in case I run 2 imports in line
	private UUID lastTaxonUuid;

	//if true, the keys will be printed after they have been created
	private boolean doPrintKeys = false;

	private MarkupImportState state;

	@Override
	protected void makeIoClassList(){
		ioClassList = new Class[]{
			MarkupDocumentImport.class
		};
	};

// ******************** CONSTRUCTOR ************************/

	protected MarkupImportConfigurator() {
		super(defaultTransformer);
	}


	protected MarkupImportConfigurator(IInputTransformer transformer) {
		super(transformer);
	}


	/**
	 * @param url
	 * @param destination
	 */
	protected MarkupImportConfigurator(URI uri, ICdmDataSource destination) {
		super(defaultTransformer);
		setSource(uri);
		setDestination(destination);
	}

	/**
	 * @param url
	 * @param destination
	 */
	protected MarkupImportConfigurator(URI uri, ICdmDataSource destination, IInputTransformer transformer) {
		super(transformer);
		setSource(uri);
		setDestination(destination);
	}

// *************************

	@Override
    public MarkupImportState getNewState() {
		if (this.isReuseExistingState() == true){
			if (this.state == null){
				this.state = new MarkupImportState(this);
			}else{
				state.reset();
			}
			return this.state;
		}else{
			return new MarkupImportState(this);
		}


	}

	@Override
	public Reference getSourceReference() {
		//TODO
		if (this.sourceReference == null){
			logger.warn("getSource Reference not yet fully implemented");
			sourceReference = ReferenceFactory.newGeneric();
			sourceReference.setTitleCache(sourceReferenceTitle, true);
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


	public UUID getLastTaxonUuid() {
		return lastTaxonUuid;
	}

	public void setLastTaxonUuid(UUID lastTaxonUuid) {
		this.lastTaxonUuid = lastTaxonUuid;
	}

	public void setDoPrintKeys(boolean doPrintKeys) {
		this.doPrintKeys = doPrintKeys;
	}

	public boolean isDoPrintKeys() {
		return doPrintKeys;
	}

	public UUID getDefaultLanguageUuid() {
		return this.defaultLanguageUuid;
	}

	public void setDefaultLanguageUuid(UUID defaultLanguageUuid) {
		this.defaultLanguageUuid = defaultLanguageUuid;
	}

	public boolean isDoTaxa() {
		return doTaxa;
	}
	public void setDoTaxa(boolean doTaxa) {
		this.doTaxa = doTaxa;
	}

	public void setReplaceStandardKeyTitles(boolean replaceStandardKeyTitles) {
		this.replaceStandardKeyTitles = replaceStandardKeyTitles;
	}

	public boolean isReplaceStandardKeyTitles() {
		return replaceStandardKeyTitles;
	}

	/**
	 * If true, the state is saved in the configurator between 2 imports using this same configurator.
	 * Use with care as you may run into memory issues or also data consistency issues otherwise.
	 * This value must be set before getNewState is called for the <b>first</b> time. The feature is
	 * experimental.
	 * @return if reuse existing state is set to true.
	 */
	public boolean isReuseExistingState() {
		return reuseExistingState;
	}

	/**
	 * @see #isReuseExistingState()
	 * @param reuseExistingState
	 */
	public void setReuseExistingState(boolean reuseExistingState) {
		this.reuseExistingState = reuseExistingState;
	}

	/**
	 * If {@link #isReuseExistingState()} is true, this method returns the state.
	 * This is an experimental workaround for Markup import. The functionality
	 * should better be moved to CdmImportBase somewhere.
	 * @return
	 */
	public MarkupImportState getState() {
		return state;
	}

	public boolean isAllowCapitalSpeciesEpithet() {
		return allowCapitalSpeciesEpithet;
	}

	public void setAllowCapitalSpeciesEpithet(boolean allowCapitalSpeciesEpithet) {
		this.allowCapitalSpeciesEpithet = allowCapitalSpeciesEpithet;
	}

	public boolean isHandlePagesAsDetailWhereNeeded() {
		return this.handlePagesAsDetailWhereNeeded;
	}

	public void setHandlePagesAsDetailWhereNeeded(boolean handlePagesAsDetailWhereNeeded) {
		this.handlePagesAsDetailWhereNeeded = handlePagesAsDetailWhereNeeded;
	}

	public boolean isUseEditorAsInAuthorWhereNeeded() {
		return useEditorAsInAuthorWhereNeeded;
	}

	public void setUseEditorAsInAuthorWhereNeeded(boolean useEditorAsInAuthorWhereNeeded) {
		this.useEditorAsInAuthorWhereNeeded = useEditorAsInAuthorWhereNeeded;
	}

	boolean useFotGSpecimenTypeCollectionAndTypeOnly = false;


	public boolean isUseFotGSpecimenTypeCollectionAndTypeOnly() {
		return useFotGSpecimenTypeCollectionAndTypeOnly;
	}

	public void setUseFotGSpecimenTypeCollectionAndTypeOnly(
			boolean useFotGSpecimenTypeCollectionAndTypeOnly) {
		this.useFotGSpecimenTypeCollectionAndTypeOnly = useFotGSpecimenTypeCollectionAndTypeOnly;
	}

    public void setKnownCollections(List<String> knownCollections) {
        this.knownCollections =knownCollections;
    }
    public List<String> getKnownCollections(){
        return this.knownCollections;
    }

    public boolean isIgnoreLocalityClass() {
        return this.ignoreLocalityClass;
    }
    public void setIgnoreLocalityClass(boolean ignoreLocalityClass) {
        this.ignoreLocalityClass = ignoreLocalityClass;
    }

    public boolean isHandleWriterManually() {
        return this.handleWriterManually;
    }
    public void setHandleWriterManually(boolean handleWriterManually) {
        this.handleWriterManually = handleWriterManually;
    }

    public UUID getSpecimenNotSeenMarkerTypeUuid() {
        return specimenNotSeenMarkerTypeUuid;
    }
    public void setSpecimenNotSeenMarkerTypeUuid(UUID specimenNotSeenMarkerTypeUuid) {
        this.specimenNotSeenMarkerTypeUuid = specimenNotSeenMarkerTypeUuid;
    }

    public String getSpecimenNotSeenMarkerTypeLabel() {
        return specimenNotSeenMarkerTypeLabel;
    }
    public void setSpecimenNotSeenMarkerTypeLabel(String specimenNotSeenMarkerTypeLabel) {
        this.specimenNotSeenMarkerTypeLabel = specimenNotSeenMarkerTypeLabel;
    }

}
