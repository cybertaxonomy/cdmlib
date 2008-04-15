package eu.etaxonomy.cdm.io.berlinModel;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource.HBM2DDL;
import eu.etaxonomy.cdm.io.source.Source;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

public class BerlinModelImportConfigurator {

	//TODO
	private boolean deleteAll = false;
	
	private boolean doAuthors = true;
	//references
	private boolean doReferences = true;
	//names
	private boolean doTaxonNames = true;
	private boolean doRelNames = true;
	private boolean doNameStatus = false;
	private boolean doTypes = false;
	
	//taxa
	private boolean doTaxa = true;
	private boolean doRelTaxa = true;
	private boolean doFacts = false;

	
	private Source berlinModelSource;
	
	private ReferenceBase sourceReference;
	private CdmApplicationController destination;
	
	private HBM2DDL hbm2dll = HBM2DDL.VALIDATE;

/* *****************CONSTRUCTOR *****************************/
	
	
	
	
/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	public BerlinModelImportConfigurator(Source berlinModelSource,
			ReferenceBase sourceReference, CdmApplicationController destination) {
		super();
		this.berlinModelSource = berlinModelSource;
		this.sourceReference = sourceReference;
		this.destination = destination;
	}
	
	/* ****************** GETTER/SETTER **************************/	
	public boolean isDeleteAll() {
		return deleteAll;
	}
	public void setDeleteAll(boolean deleteAll) {
		this.deleteAll = deleteAll;
	}
	public boolean isDoAuthors() {
		return doAuthors;
	}
	public void setDoAuthors(boolean doAuthors) {
		this.doAuthors = doAuthors;
	}
	public boolean isDoReferences() {
		return doReferences;
	}
	public void setDoReferences(boolean doReferences) {
		this.doReferences = doReferences;
	}
	public boolean isDoTaxonNames() {
		return doTaxonNames;
	}
	public void setDoTaxonNames(boolean doTaxonNames) {
		this.doTaxonNames = doTaxonNames;
	}
	public boolean isDoRelNames() {
		return doRelNames;
	}
	public void setDoRelNames(boolean doRelNames) {
		this.doRelNames = doRelNames;
	}
	public boolean isDoNameStatus() {
		return doNameStatus;
	}
	public void setDoNameStatus(boolean doNameStatus) {
		this.doNameStatus = doNameStatus;
	}
	public boolean isDoTypes() {
		return doTypes;
	}
	public void setDoTypes(boolean doTypes) {
		this.doTypes = doTypes;
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
	public boolean isDoFacts() {
		return doFacts;
	}
	public void setDoFacts(boolean doFacts) {
		this.doFacts = doFacts;
	}
	public Source getBerlinModelSource() {
		return berlinModelSource;
	}
	public void setBerlinModelSource(Source berlinModelSource) {
		this.berlinModelSource = berlinModelSource;
	}
	public ReferenceBase getSourceReference() {
		return sourceReference;
	}
	public void setSourceReference(ReferenceBase sourceReference) {
		this.sourceReference = sourceReference;
	}
	public CdmApplicationController getDestination() {
		return destination;
	}
	public void setDestination(CdmApplicationController destination) {
		this.destination = destination;
	}

	public HBM2DDL getHbm2dll() {
		return hbm2dll;
	}

	public void setHbm2dll(HBM2DDL hbm2dll) {
		this.hbm2dll = hbm2dll;
	}
	
	
	
}
