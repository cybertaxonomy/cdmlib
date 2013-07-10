/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.pesi.erms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.mapping.DbIgnoreMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportAnnotationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportExtensionMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMapping;
import eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportStringMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportTruncatedStringMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbNotYetImplementedMapper;
import eu.etaxonomy.cdm.io.common.mapping.IMappingImport;
import eu.etaxonomy.cdm.io.pesi.erms.validation.ErmsReferenceImportValidator;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;


/**
 * @author a.mueller
 * @created 20.02.2010
 * @version 1.0
 */
@Component
public class ErmsReferenceImport  extends ErmsImportBase<Reference> implements IMappingImport<Reference, ErmsImportState>{
	private static final Logger logger = Logger.getLogger(ErmsReferenceImport.class);
	
	private DbImportMapping mapping;
	
	
	private int modCount = 10000;
	private static final String pluralString = "sources";
	private static final String dbTableName = "sources";
	private static final Class cdmTargetClass = Reference.class;

	public ErmsReferenceImport(){
		super(pluralString, dbTableName, cdmTargetClass);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(ErmsImportConfigurator config) {
		String strRecordQuery = 
			" SELECT * " + 
			" FROM sources " +
			" WHERE ( sources.id IN (" + ID_LIST_TOKEN + ") )";
		return strRecordQuery;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#getMapping()
	 */
	protected DbImportMapping getMapping() {
		if (mapping == null){
			mapping = new DbImportMapping();
			
			mapping.addMapper(DbImportObjectCreationMapper.NewInstance(this, "id", REFERENCE_NAMESPACE)); //id
			ExtensionType imisExtType = getExtensionType( ErmsTransformer.IMIS_UUID, "imis", "imis", "imis");
			mapping.addMapper(DbImportExtensionMapper.NewInstance("imis_id", imisExtType));
			
			mapping.addMapper(DbImportTruncatedStringMapper.NewInstance("source_name", "titleCache", "title"));
			mapping.addMapper(DbImportStringMapper.NewInstance("source_abstract", "referenceAbstract"));
			mapping.addMapper(DbImportAnnotationMapper.NewInstance("source_note", AnnotationType.EDITORIAL(), Language.DEFAULT()));
			
			//or as Extension?
			mapping.addMapper(DbImportExtensionMapper.NewInstance("source_link", ExtensionType.URL()));
			
			//not yet implemented
			mapping.addMapper(DbIgnoreMapper.NewInstance("source_type", "Handled by ObjectCreateMapper - but mapping not yet fully correct. See comments there."));
			mapping.addMapper(DbIgnoreMapper.NewInstance("source_orig_fn", "Currently not needed. Holds information about pdf files."));
			mapping.addMapper(DbIgnoreMapper.NewInstance("source_openaccess", "Currently not needed. Holds information about open access of the source."));

		}
		return mapping;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IMappingImport#createObject(java.sql.ResultSet, eu.etaxonomy.cdm.io.common.ImportStateBase)
	 */
	public Reference createObject(ResultSet rs, ErmsImportState state) throws SQLException {
		int id = rs.getInt("id");
		String type = rs.getString("source_type");
		Reference ref;
		if (type.equalsIgnoreCase("p")){
			//TDOO is this correct? maybe mark as 'publication'
			ref = ReferenceFactory.newGeneric();
		}else if (type.equalsIgnoreCase("d")){
			ref = ReferenceFactory.newDatabase();
		}else if (type.equalsIgnoreCase("e")){
			//TODO is this correct, maybe mark as "informal"
			ref = ReferenceFactory.newGeneric();  
		}else if (type.equalsIgnoreCase("i")){
			//TODO is this correct?
			ref = ReferenceFactory.newGeneric();
		}else{
			ref = ReferenceFactory.newGeneric();
			logger.warn("Unknown reference type: " + type + ". Created generic instead.");
		}

		return ref;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs) {
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		return result;  //not needed
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(ErmsImportState state){
		IOValidator<ErmsImportState> validator = new ErmsReferenceImportValidator();
		return validator.validate(state);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(ErmsImportState state){
		//TODO
		return state.getConfig().getDoReferences() != IImportConfigurator.DO_REFERENCES.ALL;
	}





}
