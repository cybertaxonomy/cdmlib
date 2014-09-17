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
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelWebMarkerCategoryImportValidator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.MarkerType;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelWebMarkerCategoryImport extends BerlinModelImportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelWebMarkerCategoryImport.class);

	private static int modCount = 100;
	private static final String dbTableName = "webMarkerCategory";
	private static final String pluralString = "markerCategories";
	
	public BerlinModelWebMarkerCategoryImport(){
		super(dbTableName, pluralString);
	}
	
	private static Map<String, MarkerType> generalCategoryMap;
	
	private static Map<String, MarkerType> getGeneralCategoryMap(){
		if (generalCategoryMap == null){
			generalCategoryMap = new HashMap<String, MarkerType>();
			String tableName = "webMarkerCategory_";
			generalCategoryMap.put(tableName + 1, MarkerType.COMPLETE());
		}
		return generalCategoryMap;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#doInvoke(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	protected void doInvoke(BerlinModelImportState state){
		boolean success = true ;

		BerlinModelImportConfigurator config = state.getConfig();
		Source source = config.getSource();

		logger.info("start make "+pluralString+" ...");
		
		ResultSet rs = source.getResultSet(getRecordQuery(config)) ;
		
		int i = 0;
		//for each reference
		try{
			while (rs.next()){
				try{
					if ((i++ % modCount ) == 0 && i!= 1 ){ logger.info(""+pluralString+" handled: " + (i-1));}
					
					//
					int markerCategoryId = rs.getInt("MarkerCategoryID");
					String fullDbId = dbTableName + "_" + markerCategoryId;
					MarkerType markerType = getGeneralCategoryMap().get(fullDbId);
					if (markerType == null){
						String markerDescription = rs.getString("MarkerDescription");
						String markerCategory = rs.getString("MarkerCategory");
						UUID uuid = BerlinModelTransformer.getWebMarkerUuid(markerCategoryId);
						markerType = getMarkerType(state, uuid, markerDescription, markerCategory, null);
						getTermService().saveOrUpdate(markerType);
					}
					state.putDefinedTermToMap(dbTableName, markerCategoryId, markerType);
//					state.putMarkerType(markerType);

				}catch(Exception ex){
					logger.error(ex.getMessage());
					ex.printStackTrace();
					success = false;
				}
			} //while rs.hasNext()
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			state.setUnsuccessfull();
			return;
		}

			
		logger.info("save " + i + " "+pluralString + " ...");

		logger.info("end make "+pluralString+" ..." + getSuccessString(success));;
		if (!success){
			state.setUnsuccessfull();
		}
		return;
		
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
		String strQuery = 
			" SELECT *  " +
            " FROM "+dbTableName+" " ;
		return strQuery;
	}

	@Override
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState state) {
		return false; // not needed
	}

	@Override
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs, BerlinModelImportState state) {
		return null; // not needed
	}

	@Override
	protected boolean doCheck(BerlinModelImportState state){
		IOValidator<BerlinModelImportState> validator = new BerlinModelWebMarkerCategoryImportValidator();
		return validator.validate(state);
	}
	
	@Override
	protected boolean isIgnore(BerlinModelImportState state){
		return ! state.getConfig().isDoMarker();
	}

}
