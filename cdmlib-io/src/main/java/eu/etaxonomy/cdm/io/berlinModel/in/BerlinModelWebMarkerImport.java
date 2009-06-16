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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
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
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config){
		boolean result = true;
		logger.warn("Checking for "+pluralString+" not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	protected boolean doInvoke(BerlinModelImportState state){
		
		MapWrapper<Taxon> taxonMap = (MapWrapper<Taxon>)state.getStore(ICdmIO.TAXON_STORE);
		Set<TaxonBase> taxaToBeSaved = new HashSet<TaxonBase>(); 
		
		BerlinModelImportConfigurator config = state.getConfig();
		Source source = config.getSource();
		
		logger.info("start make "+pluralString+" ...");
		boolean success = true ;
		
		//get data from database
		String strQuery = 
				" SELECT *  " +
                " FROM webMarker INNER JOIN webTableName ON webMarker.TableNameFk = webTableName.TableNameId ";
		ResultSet rs = source.getResultSet(strQuery) ;
		String namespace = dbTableName;
		Map<String, DefinedTermBase> map = state.getDbCdmDefinedTermMap();
		
		int i = 0;
		//for each reference
		try{
			while (rs.next()){
				try{
					if ((i++ % modCount ) == 0 && i!= 1 ){ logger.info(""+pluralString+" handled: " + (i-1));}
					//
					int markerId = rs.getInt("MarkerId");
					int markerCategoryFk = rs.getInt("MarkerCategoryFk");
					int rIdentifier = rs.getInt("RIdentifierFk");
					String tableName = rs.getString("TableName");
					Boolean activeFlag = rs.getBoolean("ActiveFlag");
					
					AnnotatableEntity annotatableEntity;
					if ("PTaxon".equals(tableName)){
						TaxonBase<?> taxon = taxonMap.get(rIdentifier);
						if (taxon != null){
							annotatableEntity = taxon;
							taxaToBeSaved.add(taxon);
							addMarker(annotatableEntity, activeFlag, markerCategoryFk, map);
						}else{
							logger.warn("TaxonBase (RIdentifier " + rIdentifier + ") could not be found for marker " + markerId);
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
		getTaxonService().saveAll(taxaToBeSaved);
		logger.info("end make "+pluralString+" ..." + getSuccessString(success));;
		return success;
		
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
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
		return ! bmiConfig.isDoMarker();
	}

}
