/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.io.common;

import java.util.UUID;

import eu.etaxonomy.cdm.database.DbSchemaValidation;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.io.common.mapping.IInputTransformer;
import eu.etaxonomy.cdm.model.agent.Person;
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
	
	public static enum EDITOR{
		NO_EDITORS,  //leaves out createdBy and updatedBy information
		EDITOR_AS_ANNOTATION,//save createdBy and updatedBy in annotations
		EDITOR_AS_EDITOR, //save createdBy and updatedBy in createdBy and updatedBy
	}
	
	public static enum DO_REFERENCES{
		NONE,
		NOMENCLATURAL,
		CONCEPT_REFERENCES,
		ALL
	}
	
	public boolean isValid();
	
	/**
	 * Factory method. Creates a new state for the import type and adds this coniguration to it.
	 * @return 
	 */
	public <STATE extends ImportStateBase> STATE getNewState();

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
	 * @return the editor 
	 */
	public EDITOR getEditor();

	/**
	 * @param editor sets the way how editing (created, updated) information is handled
	 */
	public void setEditor(EDITOR editor);
	
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
	
	public boolean isDoMarker();
	
	public void setDoMarker(boolean doMarker);

	public void setDoUser(boolean doUser);
	
	public boolean isDoUser();


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

	/**
	 * The reference that represents the source. E.g. if the import source is a database
	 * the returned reference should be of type eu.etaxonomy.cdm.model.reference.Database and 
	 * should represent the according database.
	 * If the import comes from a file (e.g. XML) the returned value should best represent the 
	 * source of this file (e.g. if the source of an XML file is a certain database this database
	 * should be mentioned as the source. Otherwise a eu.etaxonomy.cdm.model.reference.Generic 
	 * reference with the name of the XML file should be returned value
	 * @return
	 */
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

	
	
 	/**
 	 * If this import implicitly represents a taxonomic tree in the destination CDM database
 	 * one can define the taxonomic tree's uuid here. The congrete import class must support this
 	 * functionality otherwise it will have no effect.
 	 * @return
 	 */
 	public UUID getTaxonomicTreeUuid();
	public void setTaxonomicTreeUuid(UUID treeUuid);

	/**
 	 * If one wants do define the uuid of the accepted taxa (except for missaplied names) this can be
 	 * done here 
 	 * @return
 	 */
 	public UUID getSecUuid();
	public void setSecUuid(UUID secUuid);
	
	
	/**
	 * Returns the transformer used during import
	 * @return
	 */
	public IInputTransformer getTransformer();
	
	/**
	 * Sets the transformer used during import
	 * @param transformer
	 */
	public void setTransformer(IInputTransformer transformer);
	
}