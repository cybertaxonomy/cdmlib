/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.berlinModel.in;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelWebMarkerImportValidator;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelWebMarkerImport extends BerlinModelImportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelWebMarkerImport.class);

	private static int modCount = 2000;
	private static final String dbTableName = "webMarker";
	private static final String pluralString = "markers";
	
	public BerlinModelWebMarkerImport(){
		super();
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery() {
		return " SELECT markerId FROM " + getTableName();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
		String strQuery = 
			" SELECT *  " +
            " FROM webMarker INNER JOIN webTableName ON webMarker.TableNameFk = webTableName.TableNameId " +
            " WHERE (markerId IN ("+ ID_LIST_TOKEN + ") )";
		return strQuery;

	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.berlinModel.in.ResultSetPartitioner, eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState state) {
		boolean success = true ;
	
		MapWrapper<Taxon> taxonMap = (MapWrapper<Taxon>)state.getStore(ICdmIO.TAXON_STORE);
		Set<TaxonBase> taxaToBeSaved = new HashSet<TaxonBase>(); 
		
		Map<String, DefinedTermBase> definedTermMap = state.getDbCdmDefinedTermMap();
		ResultSet rs = partitioner.getResultSet();
		
		int i = 0;
		//for each reference
		try{
			while (rs.next()){
				try{
					if ((i++ % modCount ) == 0 && i!= 1 ){ logger.info(""+pluralString+" handled: " + (i-1));}
					//
					int markerId = rs.getInt("MarkerId");
					int markerCategoryFk = rs.getInt("MarkerCategoryFk");
					int rIdentifierFk = rs.getInt("RIdentifierFk");
					String tableName = rs.getString("TableName");
					Boolean activeFlag = rs.getBoolean("ActiveFlag");
					
					AnnotatableEntity annotatableEntity;
					if ("PTaxon".equals(tableName)){
						TaxonBase<?> taxon = taxonMap.get(String.valueOf(rIdentifierFk));
						if (taxon != null){
							annotatableEntity = taxon;
							taxaToBeSaved.add(taxon);
							addMarker(annotatableEntity, activeFlag, markerCategoryFk, definedTermMap);
						}else{
							logger.warn("TaxonBase (RIdentifier " + rIdentifierFk + ") could not be found for marker " + markerId);
						}
					}else{
						logger.warn("Marker for table " + tableName + " not yet implemented.");
						success = false;
					}
					
					
				}catch(Exception ex){
					logger.error(ex.getMessage());
					ex.printStackTrace();
					success = false;
				}
			} //while rs.hasNext()
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

		logger.info("save " + i + " "+pluralString + " ...");
		getTaxonService().save(taxaToBeSaved);
		return success;
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
			while (rs.next()){
				int tableNameId = rs.getInt("TableNameFk");
				if (tableNameId != 500){
					//TODO
					logger.warn("A marker is not related to table PTaxon. This case is not handled yet!");
				}else{
					handleForeignKey(rs, taxonIdSet, "RIdentifierFk");
	}
			}
	
			//taxon map
			nameSpace = BerlinModelTaxonImport.NAMESPACE;
			cdmClass = TaxonBase.class;
			idSet = taxonIdSet;
			Map<String, TaxonBase> taxonMap = (Map<String, TaxonBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, taxonMap);
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	
	private boolean addMarker(AnnotatableEntity annotatableEntity, boolean activeFlag, int markerCategoryFk, Map<String, DefinedTermBase> map ){
		MarkerType markerType = (MarkerType)map.get("webMarkerCategory_" + markerCategoryFk);
		if (markerType == null){
			logger.warn("MarkerType not found: " + markerCategoryFk);
		}
		Marker marker = Marker.NewInstance(markerType, activeFlag);
		annotatableEntity.addMarker(marker);
		return true;

	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		IOValidator<BerlinModelImportState> validator = new BerlinModelWebMarkerImportValidator();
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
	protected boolean isIgnore(BerlinModelImportState state){
		return ! state.getConfig().isDoMarker();
	}


}
