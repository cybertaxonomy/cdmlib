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

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.Source;
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
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		boolean result = true;
		logger.warn("Checking for "+pluralString+" not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
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
	
	protected boolean doInvoke(BerlinModelImportState state){
		boolean result = true;
		
		BerlinModelImportConfigurator config = state.getConfig();
		Source source = config.getSource();

		logger.info("start make "+pluralString+" ...");
		boolean success = true ;

		//get data from database
		String strQuery = 
				" SELECT *  " +
                " FROM "+dbTableName+" " ;
		ResultSet rs = source.getResultSet(strQuery) ;
		
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
						markerType = MarkerType.NewInstance(markerDescription, markerDescription, markerCategory);
						getTermService().saveOrUpdate(markerType);
					}
					state.putDefinedTermToMap(dbTableName, markerCategoryId, markerType);

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

		logger.info("end make "+pluralString+" ..." + getSuccessString(success));;
		return result;
		
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelImportState state){
		return ! state.getConfig().isDoMarker();
	}

}
