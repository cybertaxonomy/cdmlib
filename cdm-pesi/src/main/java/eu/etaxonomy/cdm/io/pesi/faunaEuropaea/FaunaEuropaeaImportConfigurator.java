/**
* Copyright (C) 2008 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.pesi.faunaEuropaea;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportConfiguratorBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.io.pesi.out.PesiTransformer;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.babadshanjan
 * @created 08.05.2009
 * @version 1.0
 */
public class FaunaEuropaeaImportConfigurator extends ImportConfiguratorBase<FaunaEuropaeaImportState, Source> implements IImportConfigurator {
	private static final Logger logger = Logger.getLogger(FaunaEuropaeaImportConfigurator.class);

	//TODO
	private static IInputTransformer defaultTransformer = null;
	
	private boolean doBasionyms = true;
	private boolean doTaxonomicallyIncluded = true;
	private boolean doMisappliedNames = true;
	private boolean doHeterotypicSynonyms = true;
	private boolean doHeterotypicSynonymsForBasionyms ;
	private boolean doOccurrence = true;
	
	/* Max number of taxa to be saved with one service call */
	private int limitSave = 5000;
	private Reference<?> auctReference;
	
	@SuppressWarnings("unchecked")
	protected void makeIoClassList() {
		ioClassList = new Class[] {
				FaunaEuropaeaAuthorImport.class,
				FaunaEuropaeaTaxonNameImport.class,
				FaunaEuropaeaRelTaxonIncludeImport.class,
				FaunaEuropaeaRefImport.class,
				FaunaEuropaeaUsersImport.class,
				FaunaEuropaeaDistributionImport.class,
				FaunaEuropaeaHeterotypicSynonymImport.class,
				FaunaEuropaeaAdditionalTaxonDataImport.class,
		};
	};
	
	public static FaunaEuropaeaImportConfigurator NewInstance(Source source, ICdmDataSource destination){
		return new FaunaEuropaeaImportConfigurator(source, destination);
}
	
	private FaunaEuropaeaImportConfigurator(Source source, ICdmDataSource destination) {
		super(defaultTransformer);
		setSource(source);
		setDestination(destination);
		setNomenclaturalCode(NomenclaturalCode.ICBN);
	}
	
//	public static FaunaEuropaeaImportConfigurator NewInstance(ICdmDataSource source, ICdmDataSource destination){
//		return new FaunaEuropaeaImportConfigurator(source, destination);
//}
	
//	private FaunaEuropaeaImportConfigurator(ICdmDataSource source, ICdmDataSource destination) {
//		super(defaultTransformer);
//		setSource(source);
//		setDestination(destination);
//		setNomenclaturalCode(NomenclaturalCode.ICBN);
//	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
	@Override
	public Reference<?> getSourceReference() {
		//TODO
		if (this.sourceReference == null){
			logger.warn("getSource Reference not yet fully implemented");
			sourceReference = ReferenceFactory.newDatabase();
			
			sourceReference.setTitleCache("Fauna Europaea database", true);
			if (this.getSourceRefUuid() != null){
				sourceReference.setUuid(this.getSourceRefUuid());
			}
		}
		return sourceReference;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.ImportConfiguratorBase#getSourceReference()
	 */
	public Reference<?> getAuctReference() {
		//TODO
		if (auctReference == null){
			auctReference = ReferenceFactory.newPersonalCommunication();
			
			auctReference.setTitleCache("auct.", true);
			auctReference.setUuid(PesiTransformer.uuidSourceRefAuct);
		}
		return auctReference;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getSourceNameString()
	 */
	public String getSourceNameString() {
		if (this.getSource() == null) {
			return null;
		}else{
			return this.getSource().toString();
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IImportConfigurator#getNewState()
	 */
	public FaunaEuropaeaImportState getNewState() {
		return new FaunaEuropaeaImportState(this);
	}

	/**
	 * @return the doBasionyms
	 */
	public boolean isDoBasionyms() {
		return doBasionyms;
	}

	/**
	 * @param doBasionyms the doBasionyms to set
	 */
	public void setDoBasionyms(boolean doBasionyms) {
		this.doBasionyms = doBasionyms;
	}

	/**
	 * @return the doTaxonomicallyIncluded
	 */
	public boolean isDoTaxonomicallyIncluded() {
		return doTaxonomicallyIncluded;
	}

	/**
	 * @param doTaxonomicallyIncluded the doTaxonomicallyIncluded to set
	 */
	public void setDoTaxonomicallyIncluded(boolean doTaxonomicallyIncluded) {
		this.doTaxonomicallyIncluded = doTaxonomicallyIncluded;
	}

	/**
	 * @return the doMisappliedNames
	 */
	public boolean isDoMisappliedNames() {
		return doMisappliedNames;
	}

	/**
	 * @param doMisappliedNames the doMisappliedNames to set
	 */
	public void setDoMisappliedNames(boolean doMisappliedNames) {
		this.doMisappliedNames = doMisappliedNames;
	}

	/**
	 * @return the doHeterotypicSynonyms
	 */
	public boolean isDoHeterotypicSynonyms() {
		return doHeterotypicSynonyms;
	}

	/**
	 * @param doHeterotypicSynonyms the doHeterotypicSynonyms to set
	 */
	public void setDoHeterotypicSynonyms(boolean doHeterotypicSynonyms) {
		this.doHeterotypicSynonyms = doHeterotypicSynonyms;
	}

	/**
	 * @param auctReference the auctReference to set
	 */
	public void setAuctReference(Reference<?> auctReference) {
		this.auctReference = auctReference;
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
	 * @param doHeterotypicSynonymsForBasionyms the doHeterotypicSynonymsForBasionyms to set
	 */
	public void setDoHeterotypicSynonymsForBasionyms(
			boolean doHeterotypicSynonymsForBasionyms) {
		this.doHeterotypicSynonymsForBasionyms = doHeterotypicSynonymsForBasionyms;
	}

	/**
	 * @return the doHeterotypicSynonymsForBasionyms
	 */
	public boolean isDoHeterotypicSynonymsForBasionyms() {
		return doHeterotypicSynonymsForBasionyms;
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#isDoOccurrence()
	 */
	public boolean isDoOccurrence() {
		return doOccurrence;
	}
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.tcsrdf.IImportConfigurator#setDoOccurrence(boolean)
	 */
	public void setDoOccurrence(boolean doOccurrence) {
		this.doOccurrence = doOccurrence;
	}

	
	private boolean doAuthors = true;
	//references
	private DO_REFERENCES doReferences = DO_REFERENCES.ALL;
	//names
	private boolean doTypes = true;
	
	//taxa
	private boolean doTaxa = true;
	private boolean doRelTaxa = true;

	public boolean isDoAuthors() {
		return doAuthors;
	}
	public void setDoAuthors(boolean doAuthors) {
		this.doAuthors = doAuthors;
	}

	public DO_REFERENCES getDoReferences() {
		return doReferences;
	}
	public void setDoReferences(DO_REFERENCES doReferences) {
		this.doReferences = doReferences;
	}

//	public boolean isDoTypes() {
//		return doTypes;
//	}
//	public void setDoTypes(boolean doTypes) {
//		this.doTypes = doTypes;
//	}

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

	
}
