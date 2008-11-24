package eu.etaxonomy.cdm.io.common;

import java.util.UUID;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

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
	
	public abstract boolean isValid();

	/* ****************** GETTER/SETTER **************************/
	public abstract boolean isDeleteAll();

	public abstract void setDeleteAll(boolean deleteAll);

	public abstract boolean isDoAuthors();

	public abstract void setDoAuthors(boolean doAuthors);

	/**
	 * @return the doReferences
	 */
	public abstract DO_REFERENCES getDoReferences();

	/**
	 * @param doReferences the doReferences to set
	 */
	public abstract void setDoReferences(DO_REFERENCES doReferences);

	/**
	 * @return the doReferences
	 */
	public abstract CHECK getCheck();

	/**
	 * @param doReferences the doReferences to set
	 */
	public abstract void setCheck(CHECK check);

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
	
	public abstract boolean isDoTaxonNames();

	public abstract void setDoTaxonNames(boolean doTaxonNames);

	public abstract boolean isDoRelNames();

	public abstract void setDoRelNames(boolean doRelNames);

	public abstract boolean isDoNameStatus();

	public abstract void setDoNameStatus(boolean doNameStatus);

	public abstract boolean isDoNameFacts();

	public abstract void setDoNameFacts(boolean doNameFacts);

	public abstract boolean isDoTypes();

	public abstract void setDoTypes(boolean doTypes);

	public abstract boolean isDoTaxa();

	public abstract void setDoTaxa(boolean doTaxa);

	public abstract boolean isDoRelTaxa();

	public abstract void setDoRelTaxa(boolean doRelTaxa);

	public abstract boolean isDoFacts();

	public abstract void setDoFacts(boolean doFacts);

	/**
	 * @return the doOccurrence
	 */
	public abstract boolean isDoOccurrence();

	/**
	 * @param doOccurrence the doOccurrence to set
	 */
	public abstract void setDoOccurrence(boolean doOccurrence);

	
	public abstract ICdmDataSource getDestination();

	public abstract void setDestination(ICdmDataSource destination);

	public abstract DbSchemaValidation getDbSchemaValidation();

	public abstract void setDbSchemaValidation(
			DbSchemaValidation dbSchemaValidation);

	public abstract ReferenceBase getSourceReference();

	
	/**
	 * A String representation of the used source (e.g. BerlinModel Cichorieae Database)
	 * @return
	 */
	public abstract String getSourceNameString();
	
	/**
	 * Any object that represents the Source. The implementing class must cast this to 
	 * the correct class type
	 * @return
	 */
	public abstract Object getSource();

	//public abstract void setSource(Object url);

	public abstract void setSourceReference(ReferenceBase sourceReference);

	public abstract String getSourceReferenceTitle();

	public abstract void setSourceReferenceTitle(String sourceReferenceTitle);

	public abstract Person getCommentator();

	public abstract void setCommentator(Person commentator);

	public abstract Language getFactLanguage();

	public abstract void setFactLanguage(Language factLanguage);
	
	public abstract NomenclaturalCode getNomenclaturalCode();
	
	public abstract void setNomenclaturalCode(NomenclaturalCode nomenclaturalCode);
	
	public Class<ICdmIO>[] getIoClassList();
	
	/**
	 * Returns a <code>CdmApplicationController</code> created by the values of this configuration.
	 * If a controller was already created before the last created controller is returned.
	 * @return
	 */
	public CdmApplicationController getCdmAppController();
	public CdmApplicationController getCdmAppController(boolean createNew, boolean omitTermLoading);
	
	public Object getSourceSecId();

	public UUID getSecUuid();
	public void setSecUuid(UUID secUuid);
	
	/* 
	 * For Jaxb Import
	 */
//	public boolean isDoTerms();
//	public void setDoTerms(boolean doTerms);
//	
//	public boolean isDoTermVocabularies();
//	public void setDoTermVocabularies(boolean doTermVocabularies);
//	
//	public boolean isDoHomotypicalGroups();
//	public void setDoHomotypicalGroups(boolean doHomotypicalGroups);
//	
//	public boolean isDoReferencedEntities();
//	public void setDoReferencedEntities(boolean doReferencedEntities);
//	
//	public boolean isDoFeatureData();
//	public void setDoFeatureData(boolean doFeatureData);
//	
//	public boolean isDoMedia();
//	public void setDoMedia(boolean doMedia);
//	
//	public boolean isDoLanguageData();
//	public void setDoLanguageData(boolean doLanguageData);
		
}