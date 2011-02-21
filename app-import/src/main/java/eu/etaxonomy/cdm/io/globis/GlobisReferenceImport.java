/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.globis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.mapping.DbIgnoreMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportExtensionMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMapping;
import eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportStringMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbNotYetImplementedMapper;
import eu.etaxonomy.cdm.io.common.mapping.IMappingImport;
import eu.etaxonomy.cdm.io.globis.validation.GlobisReferenceImportValidator;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;


/**
 * @author a.mueller
 * @created 20.02.2010
 * @version 1.0
 */
@Component
public class GlobisReferenceImport  extends GlobisImportBase<Reference> implements IMappingImport<Reference, GlobisImportState>{
	private static final Logger logger = Logger.getLogger(GlobisReferenceImport.class);
	
	private DbImportMapping mapping;
	
	
	private int modCount = 10000;
	private static final String pluralString = "references";
	private static final String dbTableName = "literature";
	private static final Class cdmTargetClass = Reference.class;

	public GlobisReferenceImport(){
		super(pluralString, dbTableName, cdmTargetClass);
	}


	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.globis.GlobisImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery() {
		String strRecordQuery = 
			" SELECT refID " + 
			" FROM " + dbTableName; 
		return strRecordQuery;	
	}




	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(GlobisImportConfigurator config) {
		String strRecordQuery = 
			" SELECT * " + 
			" FROM literature " +
			" WHERE ( literature.refId IN (" + ID_LIST_TOKEN + ") )";
		return strRecordQuery;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.globis.GlobisImportBase#getMapping()
	 */
	protected DbImportMapping getMapping() {
		if (mapping == null){
			mapping = new DbImportMapping();
			
			mapping.addMapper(DbImportObjectCreationMapper.NewInstance(this, "refID", REFERENCE_NAMESPACE)); //id
			mapping.addMapper(DbIgnoreMapper.NewInstance("CountryDummy"));
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("CreatedBy"));
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("DateCreated"));
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("DateModified"));
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("ModifiedBy"));
			mapping.addMapper(DbImportStringMapper.NewInstance("RefBookTitle", "title", false));
			//mapping.addMapper(DbImportTimePeriodMapper.NewInstance("RefDatePublished", "datePublished", false));
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("RefDatePublished"));
//			mapping.addMapper(DbImportExtensionTypeCreationMapper.NewInstance(dbIdAttribute, extensionTypeNamespace, dbTermAttribute, dbLabelAttribute, dbLabelAbbrevAttribute)
			mapping.addMapper(DbImportExtensionMapper.NewInstance("RefEdition", GlobisTransformer.uuidEdition, "Edition", "Edition", "Ed."));
			mapping.addMapper(DbImportExtensionMapper.NewInstance("RefEdition", GlobisTransformer.uuidEditor, "Editor", "Editor", "Editor"));
			mapping.addMapper(DbImportExtensionMapper.NewInstance("RefGeneralKeywords", GlobisTransformer.uuidGeneralKeywords, "General Keywords", "General Keywords", "gen. keyw."));
			mapping.addMapper(DbImportExtensionMapper.NewInstance("RefGeoKeywords", GlobisTransformer.uuidGeoKeywords, "Geographic Keywords", "Geo Keywords", "geo. keyw."));
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("RefIll only"));
			mapping.addMapper(DbImportStringMapper.NewInstance("ISSN", "issn", false));
			mapping.addMapper(DbImportExtensionMapper.NewInstance("RefLibrary", GlobisTransformer.uuidLibrary, "Library", "Library", "Libr."));
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("RefMarker"));
			mapping.addMapper(DbImportStringMapper.NewInstance("RefPages", "pages"));
			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("RefPages only"));
			
			
			
			Reference ref = null;
//			ref.setP
			
		
////			mapping.addMapper(DbImportExtensionMapper.NewInstance("imis_id", GlobisTransformer.IMIS_UUID, "imis", "imis", "imis"));
//						
//			mapping.addMapper(DbImportTruncatedStringMapper.NewInstance("source_name", "titleCache", "title"));
//			mapping.addMapper(DbImportStringMapper.NewInstance("source_abstract", "referenceAbstract"));
//			mapping.addMapper(DbImportAnnotationMapper.NewInstance("source_note", AnnotationType.EDITORIAL(), Language.DEFAULT()));
//			
//			//or as Extension?
//			mapping.addMapper(DbImportExtensionMapper.NewInstance("source_link", ExtensionType.URL()));
//			
//			//not yet implemented
//			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("source_type"));
//			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("source_orig_fn"));

		}
		return mapping;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IMappingImport#createObject(java.sql.ResultSet, eu.etaxonomy.cdm.io.common.ImportStateBase)
	 */
	public Reference createObject(ResultSet rs, GlobisImportState state)
			throws SQLException {
		Reference ref;
		String refType = rs.getString("RefType");
		if (refType == null){
			ref = ReferenceFactory.newGeneric();
		}else if (refType == "book"){
			ref = ReferenceFactory.newBook();
		}else if (refType == "paper in journal"){
			ref = ReferenceFactory.newArticle();
		}else if (refType.startsWith("unpublished") ){
			ref = ReferenceFactory.newGeneric();
		}else if (refType.endsWith("paper in journal")){
			ref = ReferenceFactory.newArticle();
		}else if (refType == "paper in book"){
			ref = ReferenceFactory.newBookSection();
		}else if (refType == "paper in journalwebsite"){
			ref = ReferenceFactory.newArticle();
		}else{
			logger.warn("Unknown reference type: " + refType);
			ref = ReferenceFactory.newGeneric();
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
	protected boolean doCheck(GlobisImportState state){
		IOValidator<GlobisImportState> validator = new GlobisReferenceImportValidator();
		return validator.validate(state);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(GlobisImportState state){
		//TODO
		return state.getConfig().getDoReferences() != IImportConfigurator.DO_REFERENCES.ALL;
	}





}
