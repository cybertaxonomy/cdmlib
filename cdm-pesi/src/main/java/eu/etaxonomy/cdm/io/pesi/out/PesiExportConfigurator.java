// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.pesi.out;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.DbExportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author e.-m.lee
 * @date 12.02.2010
 *
 */
public class PesiExportConfigurator extends DbExportConfiguratorBase<PesiExportState, PesiTransformer> implements IExportConfigurator<PesiExportState, PesiTransformer> {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(PesiExportConfigurator.class);
	private int limitSave = 2000;

	private Reference<?> auctReference;
	
	
	private DO_REFERENCES doReferences = DO_REFERENCES.ALL;
	private boolean doTaxa = true;
	private boolean doRelTaxa = true;
	private boolean doNotes = true;
	private boolean doNoteSources = true;
	private boolean doAdditionalTaxonSource = true;
	private boolean doOccurrence = true;
	private boolean doOccurrenceSource = true;
	private boolean doImages = true;
	private boolean doTreeIndex = true;
	private boolean doParentAndBiota = true;
	private boolean doInferredSynonyms = true;
	private boolean doRank = true;
	private boolean doPureNames = true;
	private boolean doDescription = true;
	
	private int nameIdStart = 10000000;

	public static PesiExportConfigurator NewInstance(Source pesiDestination, ICdmDataSource source, PesiTransformer transformer) {
			return new PesiExportConfigurator(pesiDestination, source, transformer);
	}
	
	@SuppressWarnings("unchecked")
	protected void makeIoClassList() {
		ioClassList = new Class[]{
				PesiSourceExport.class,
				PesiTaxonExport.class,
				PesiRelTaxonExport.class, // RelTaxonId's could be deleted from state hashmap
				PesiDescriptionExport.class,
				PesiFinalUpdateExport.class
//				PesiNoteExport.class,
//				PesiNoteSourceExport.class, // NoteId's could be deleted from state hashmap
//				PesiAdditionalTaxonSourceExport.class,
//				PesiOccurrenceExport.class,
//				PesiOccurrenceSourceExport.class,
//				PesiImageExport.class,
		};

	}
	
	/**
	 * @param pesiSource
	 * @param cdmSource
	 * @param transformer 
	 */
	private PesiExportConfigurator(Source pesiSource, ICdmDataSource cdmSource, PesiTransformer transformer) {
	   super(transformer);
	   setSource(cdmSource);
	   setDestination(pesiSource);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IExportConfigurator#getNewState()
	 */
	public PesiExportState getNewState() {
		return new PesiExportState(this);
	}
	
	/**
	 * @return the limitSave
	 */
	public int getLimitSave() {
		return limitSave;
	}

	/**
	 * @param limitSave the limitSave to set
	 */
	public void setLimitSave(int limitSave) {
		this.limitSave = limitSave;
	}

	/**
	 * Returns the Reference for a Misapplied Name.
	 * Copied from FaunaEuropaeaImportConfigurator.
	 * @return
	 */
	public Reference<?> getAuctReference() {
		if (auctReference == null){
			auctReference = ReferenceFactory.newGeneric();
			
			auctReference.setTitleCache("auct.", true);
		}
		return auctReference;
	}
	
	public boolean isDoOccurrence() {
		return doOccurrence;
	}
	public void setDoOccurrence(boolean doOccurrence) {
		this.doOccurrence = doOccurrence;
	}
	
	
	public boolean isDoImages() {
		return doImages;
	}
	public void setDoImages(boolean doImages) {
		this.doImages = doImages;
	}
	


	public DO_REFERENCES getDoReferences() {
		return doReferences;
	}
	public void setDoReferences(DO_REFERENCES doReferences) {
		this.doReferences = doReferences;
	}

	public boolean isDoTaxa() {
		return doTaxa;
	}
	public void setDoTaxa(boolean doTaxa) {
		this.doTaxa = doTaxa;
	}
	
	public boolean isDoRelTaxa() {
		return doRelTaxa;
	}
	public void setDoRelTaxa(boolean doRelTaxa) {
		this.doRelTaxa = doRelTaxa;
	}

	/**
	 * Number that is added to the cdm id in case a name is stored by its own id
	 * not the taxons id.
	 * @return
	 */
	public int getNameIdStart() {
		return nameIdStart;
	}

	public void setNameIdStart(int nameIdStart) {
		this.nameIdStart = nameIdStart;
	}

	public boolean isDoNotes() {
		return doNotes;
	}

	public void setDoNotes(boolean doNotes) {
		this.doNotes = doNotes;
	}

	public boolean isDoNoteSources() {
		return doNoteSources;
	}

	public void setDoNoteSources(boolean doNoteSources) {
		this.doNoteSources = doNoteSources;
	}

	public boolean isDoAdditionalTaxonSource() {
		return doAdditionalTaxonSource;
	}

	public void setDoAdditionalTaxonSource(boolean doAdditionalTaxonSource) {
		this.doAdditionalTaxonSource = doAdditionalTaxonSource;
	}

	public boolean isDoOccurrenceSource() {
		return doOccurrenceSource;
	}

	public void setDoOccurrenceSource(boolean doOccurrenceSource) {
		this.doOccurrenceSource = doOccurrenceSource;
	}

	public boolean isDoTreeIndex() {
		return this.doTreeIndex;
	}

	public void setDoTreeIndex(boolean doTreeIndex) {
		this.doTreeIndex = doTreeIndex;
	}

	public boolean isDoInferredSynonyms() {
		return doInferredSynonyms;
	}

	public void setDoInferredSynonyms(boolean doInferredSynonyms) {
		this.doInferredSynonyms = doInferredSynonyms;
	}

	public boolean isDoRank() {
		return doRank;
	}

	public void setDoRank(boolean doRank) {
		this.doRank = doRank;
	}

	public boolean isDoPureNames() {
		return doPureNames;
	}

	public void setDoPureNames(boolean doPureNames) {
		this.doPureNames = doPureNames;
	}

	public boolean isDoDescription() {
		return doDescription;
	}

	public void setDoDescription(boolean doDescription) {
		this.doDescription = doDescription;
	}

	public boolean isDoParentAndBiota() {
		return doParentAndBiota;
	}

	public void setDoParentAndBiota(boolean doParentAndBiota) {
		this.doParentAndBiota = doParentAndBiota;
	}




}
