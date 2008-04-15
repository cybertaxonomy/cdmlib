package eu.etaxonomy.cdm.io.berlinModel;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource.HBM2DDL;
import eu.etaxonomy.cdm.io.source.Source;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

public class BerlinModelImportConfigurator {
	private static Logger logger = Logger.getLogger(BerlinModelImportConfigurator.class);

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

	
	private Source source;
	private ReferenceBase sourceReference;
	private ICdmDataSource destination;
	
	private HBM2DDL hbm2dll = HBM2DDL.VALIDATE;

/* *****************CONSTRUCTOR *****************************/
	
	
	public static BerlinModelImportConfigurator NewInstance(Source berlinModelSource,
			eu.etaxonomy.cdm.database.ICdmDataSource destination){
		return new BerlinModelImportConfigurator(berlinModelSource, destination);
	}
	
	
	/**
	 * @param berlinModelSource
	 * @param sourceReference
	 * @param destination
	 */
	private BerlinModelImportConfigurator(Source berlinModelSource, ICdmDataSource destination) {
		super();
		this.source = berlinModelSource;
		this.destination = destination;
	}
	

	public boolean isValid(){
		boolean result = true;
		if (source == null){
			logger.warn("Connection to BerlinModel could not be established");
			result = false;
		}
		
		
		return result;
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
	public Source getSource() {
		return source;
	}
	public void setSource(Source berlinModelSource) {
		this.source = berlinModelSource;
	}
	public ICdmDataSource getDestination() {
		return destination;
	}
	public void setDestination(ICdmDataSource destination) {
		this.destination = destination;
	}

	public HBM2DDL getHbm2dll() {
		return hbm2dll;
	}
	public void setHbm2dll(HBM2DDL hbm2dll) {
		this.hbm2dll = hbm2dll;
	}

	public ReferenceBase getSourceReference() {
		ReferenceBase result = sourceReference;
		if (sourceReference == null){
			result =  new Database();
			if (source != null){
				result.setTitleCache(source.getDatabase());
			}
		}
		return result;
	}
	public void setSourceReference(ReferenceBase sourceReference) {
		this.sourceReference = sourceReference;
	}
	public String getSourceReferenceTitle() {
		return getSourceReference().getTitleCache();
	}
	public void setSourceReferenceTitle(String sourceReferenceTitle) {
		getSourceReference().setTitleCache(sourceReferenceTitle);
	}
	
	
	
	
	
}
