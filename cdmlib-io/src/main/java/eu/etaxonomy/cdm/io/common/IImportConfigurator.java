package eu.etaxonomy.cdm.io.common;

import java.util.UUID;

import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.mueller
 * @created 29.01.2009
 * @version 1.0
 */
public interface IImportConfigurator extends IIoConfigurator {

	public static enum CHECK{
		CHECK_ONLY,
		IMPORT_WITHOUT_CHECK,
		CHECK_AND_IMPORT,
	}
	
	public static enum DO_REFERENCES{
		NONE,
		NOMENCLATURAL,
		CONCEPT_REFERENCES,
		ALL
	}
	
	public boolean isValid();

	/* ****************** GETTER/SETTER **************************/
	public boolean isDeleteAll();

	public void setDeleteAll(boolean deleteAll);

	public boolean isDoAuthors();

	public void setDoAuthors(boolean doAuthors);

	/**
	 * @return the doReferences
	 */
	public DO_REFERENCES getDoReferences();

	/**
	 * @param doReferences the doReferences to set
	 */
	public void setDoReferences(DO_REFERENCES doReferences);

	/**
	 * @return the doReferences
	 */
	public CHECK getCheck();

	/**
	 * @param doReferences the doReferences to set
	 */
	public void setCheck(CHECK check);

	/**
	 * If true, no errors occurs if objects are not found that should exist. This may
	 * be needed e.g. when only subsets of the data are imported.
	 * Default value is <cod>false</code>.
	 * @return the ignoreNull
	 */
	public boolean isIgnoreNull();
	/**
	 * @param ignoreNull the ignoreNull to set
	 */
	public void setIgnoreNull(boolean ignoreNull);
	
	public boolean isDoTaxonNames();

	public void setDoTaxonNames(boolean doTaxonNames);

	public boolean isDoRelNames();

	public void setDoRelNames(boolean doRelNames);

	public boolean isDoNameStatus();

	public void setDoNameStatus(boolean doNameStatus);

	public boolean isDoNameFacts();

	public void setDoNameFacts(boolean doNameFacts);

	public boolean isDoTypes();

	public void setDoTypes(boolean doTypes);

	public boolean isDoTaxa();

	public void setDoTaxa(boolean doTaxa);

	public boolean isDoRelTaxa();

	public void setDoRelTaxa(boolean doRelTaxa);

	public boolean isDoFacts();

	public void setDoFacts(boolean doFacts);

	/**
	 * @return the doOccurrence
	 */
	public boolean isDoOccurrence();

	/**
	 * @param doOccurrence the doOccurrence to set
	 */
	public void setDoOccurrence(boolean doOccurrence);
	
	/**
	 * The destination data source for the import 
	 * Don't use when using a spring data source
	 * @return
	 */
	public ICdmDataSource getDestination();

	public void setDestination(ICdmDataSource destination);

	public DbSchemaValidation getDbSchemaValidation();

	public void setDbSchemaValidation(
			DbSchemaValidation dbSchemaValidation);

	public ReferenceBase getSourceReference();

	
	/**
	 * Any object that represents the Source. The implementing class must cast this to 
	 * the correct class type
	 * @return
	 */
	public Object getSource();

	//public abstract void setSource(Object url);

	public void setSourceReference(ReferenceBase sourceReference);

	public String getSourceReferenceTitle();

	public void setSourceReferenceTitle(String sourceReferenceTitle);

	public Person getCommentator();

	public void setCommentator(Person commentator);

	public Language getFactLanguage();

	public void setFactLanguage(Language factLanguage);
	
	public NomenclaturalCode getNomenclaturalCode();
	
	public void setNomenclaturalCode(NomenclaturalCode nomenclaturalCode);
	
	public Class<ICdmIO>[] getIoClassList();
	
//	public String[] getIoBeans();
//	public void setIoBeans(String[] ioBeans);
	
	/**
	 * Returns a <code>CdmApplicationController</code> created by the values of this configuration.
	 * If a controller was already created before the last created controller is returned.
	 * @return
	 */
//	public CdmApplicationController getCdmAppController();
//	public CdmApplicationController getCdmAppController(boolean createNew, boolean omitTermLoading);
//	
	public Object getSourceSecId();

	public UUID getSecUuid();
	public void setSecUuid(UUID secUuid);

	
}