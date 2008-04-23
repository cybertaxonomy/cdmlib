package eu.etaxonomy.cdm.io.berlinModel;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.io.source.Source;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

public class BerlinModelImportConfigurator {
	private static Logger logger = Logger.getLogger(BerlinModelImportConfigurator.class);

	public static enum DO_REFERENCES{
		NONE,
		NOMENCLATURAL,
		CONCEPT_REFERENCES,
		ALL
	}
	
	//TODO
	private boolean deleteAll = false;
	
	private boolean doAuthors = true;
	//references
	private DO_REFERENCES doReferences = DO_REFERENCES.ALL;
	//names
	private boolean doTaxonNames = true;
	private boolean doRelNames = true;
	private boolean doNameStatus = false;
	private boolean doTypes = false;
	
	//taxa
	private boolean doTaxa = true;
	private boolean doRelTaxa = false;
	private boolean doFacts = true;

	
	private Source source;
	private ReferenceBase sourceReference;
	private ICdmDataSource destination;
	private Person commentator =  Person.NewTitledInstance("automatic BerlinModel2CDM importer");
	
	private Language factLanguage = Language.ENGLISH();
	private DbSchemaValidation dbSchemaValidation = DbSchemaValidation.VALIDATE;

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
		if (destination == null){
			logger.warn("Connection to Cdm could not be established");
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
	
	/**
	 * @return the doReferences
	 */
	public DO_REFERENCES getDoReferences() {
		return doReferences;
	}
	/**
	 * @param doReferences the doReferences to set
	 */
	public void setDoReferences(DO_REFERENCES doReferences) {
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

	public DbSchemaValidation getDbSchemaValidation() {
		return dbSchemaValidation;
	}
	public void setDbSchemaValidation(DbSchemaValidation dbSchemaValidation) {
		this.dbSchemaValidation = dbSchemaValidation;
	}

	public ReferenceBase getSourceReference() {
		if (sourceReference == null){
			sourceReference =  new Database();
			if (source != null){
				sourceReference.setTitleCache(source.getDatabase());
			}
		}
		return sourceReference;
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


	public Person getCommentator() {
		return commentator;
	}

	public void setCommentator(Person commentator) {
		this.commentator = commentator;
	}


	public Language getFactLanguage() {
		return factLanguage;
	}


	public void setFactLanguage(Language factLanguage) {
		this.factLanguage = factLanguage;
	}



	
}
