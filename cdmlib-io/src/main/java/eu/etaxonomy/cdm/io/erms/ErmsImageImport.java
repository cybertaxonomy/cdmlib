/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.erms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.mapping.DbImportImageGalleryMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMapping;
import eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapper;
import eu.etaxonomy.cdm.io.erms.validation.ErmsImageImportValidator;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 * @created 20.02.2010
 * @version 1.0
 */
@Component
public class ErmsImageImport  extends ErmsImportBase<TaxonBase> {

	private static final Logger logger = Logger.getLogger(ErmsImageImport.class);

	private static final String IMAGE_NAMESPACE = "Images";
	
	private DbImportMapping mapping;
	
	
	private int modCount = 10000;
	private static final String pluralString = "images";
	private String dbTableName = "images";
	//TODO needed?
	private Class cdmTargetClass = Media.class;

	public ErmsImageImport(){
		super();
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery() {
		String strIdQuery = 
			" SELECT tu_id, img_thumb " +   //tu_id is not a key
			" FROM images " + 
			" ORDER BY tu_id, img_thumb, img_url ";
		return strIdQuery;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(ErmsImportConfigurator config) {
		String strRecordQuery = 
			" SELECT * " + 
			" FROM images " +
			" WHERE ( images.tu_id IN (" + ID_LIST_TOKEN + ") AND " +
				"  images.img_thumb IN (" + ID_LIST_TOKEN + ")  )";
		return strRecordQuery;
	}

	/**
	 * @return
	 */
	private DbImportMapping getMapping() {
		if (mapping == null){
			mapping = new DbImportMapping();
			//TODO do we need to add to TaxonNameBase too?
			mapping.addMapper(DbImportObjectCreationMapper.NewInstance(this, "tu_id", IMAGE_NAMESPACE)); //id
			//TODO put into one media
			mapping.addMapper(DbImportImageGalleryMapper.NewInstance("img_url"));
			mapping.addMapper(DbImportImageGalleryMapper.NewInstance("img_thumb"));
		}
		return mapping;
	}
	
	
	public boolean doPartition(ResultSetPartitioner partitioner, ErmsImportState state) {
		boolean success = true ;
		ErmsImportConfigurator config = state.getConfig();
		Set referencesToSave = new HashSet<TaxonBase>();
		
 		DbImportMapping<?, ?> mapping = getMapping();
		mapping.initialize(state, cdmTargetClass);
		
		ResultSet rs = partitioner.getResultSet();
		try{
			while (rs.next()){
				success &= mapping.invoke(rs,referencesToSave);
			}
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	
		partitioner.startDoSave();
		getReferenceService().save(referencesToSave);
		return success;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IMappingImport#createObject(java.sql.ResultSet, eu.etaxonomy.cdm.io.common.ImportStateBase)
	 */
	public TaxonBase createObject(ResultSet rs, ErmsImportState state)throws SQLException {
		TaxonBase result = null;
		Integer tu_id = rs.getInt("tu_id");
		if (tu_id != null){
			result = (TaxonBase)state.getRelatedObject(ErmsTaxonImport.TAXON_NAMESPACE, String.valueOf(tu_id));		
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs) {
		String nameSpace;
		Class cdmClass;
		Set<String> idSet;
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		try{
			Set<String> taxonIdSet = new HashSet<String>();
			Set<String> languageIdSet = new HashSet<String>();
			while (rs.next()){
				handleForeignKey(rs, taxonIdSet, "tu_id");
			}
			
			//taxon map
			nameSpace = ErmsTaxonImport.TAXON_NAMESPACE;
			cdmClass = TaxonBase.class;
			idSet = taxonIdSet;
			Map<String, TaxonBase> taxonMap = (Map<String, TaxonBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, taxonMap);
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(ErmsImportState state){
		IOValidator<ErmsImportState> validator = new ErmsImageImportValidator();
		return validator.validate(state);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getTableName()
	 */
	@Override
	protected String getTableName() {
		return dbTableName;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getPluralString()
	 */
	@Override
	public String getPluralString() {
		return pluralString;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(ErmsImportState state){
		return ! state.getConfig().isDoImages();
	}





}
