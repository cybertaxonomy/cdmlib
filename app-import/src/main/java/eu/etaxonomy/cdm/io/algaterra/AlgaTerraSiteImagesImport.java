/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.algaterra;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.algaterra.validation.AlgaTerraTypeImportValidator;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class AlgaTerraSiteImagesImport  extends AlgaTerraImageImportBase {
	private static final Logger logger = Logger.getLogger(AlgaTerraSiteImagesImport.class);

	
	private static int modCount = 5000;
	private static final String pluralString = "site images";
	private static final String dbTableName = "SiteImages";  //??  
	
	public AlgaTerraSiteImagesImport(){
		super(dbTableName, pluralString);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery(BerlinModelImportState state) {
		String result = " SELECT SiteId "  
				+ " FROM SiteImages " 
				+ " ORDER BY EcoFactFk ";
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
			String strQuery =    
						
				" SELECT si.*, si.Comment as FigurePhrase, si.PictureFile as fileName, si.Path as filePath " +
	            " FROM SiteImages si  " 
	            + 	" WHERE (si.SiteID IN (" + ID_LIST_TOKEN + ")  )"  
	            + " ORDER BY EcoFactFk ";
            ;
		return strQuery;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.berlinModel.in.ResultSetPartitioner, eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState bmState) {
		boolean success = true;
		
		AlgaTerraImportState state = (AlgaTerraImportState)bmState;
		
		Set<SpecimenOrObservationBase> unitsToSave = new HashSet<SpecimenOrObservationBase>();
		
		Map<String, FieldObservation> ecoFactFieldObservationMap = (Map<String, FieldObservation>) partitioner.getObjectMap(AlgaTerraSpecimenImportBase.ECO_FACT_FIELD_OBSERVATION_NAMESPACE);
		
		ResultSet rs = partitioner.getResultSet();

		try {
			
			int i = 0;

			//for each reference
            while (rs.next()){
                
        		if ((i++ % modCount) == 0 && i!= 1 ){ logger.info(pluralString + " handled: " + (i-1));}
				
				int figureId = rs.getInt("SiteId");
				int ecoFactFk = rs.getInt("EcoFactFk");
				
				
				//TODO etc. Created, Notes, Copyright, TermsOfUse etc.
				
				try {
					
					//TODO use deduplicated ecofact
					FieldObservation fieldObservation = ecoFactFieldObservationMap.get(String.valueOf(ecoFactFk));
					
					if (fieldObservation == null){
						logger.warn("Could not find eco fact field observation (" + ecoFactFk +") for site image " +  figureId);
					}else{
						
					}
					
					//field observation
					Media media = handleSingleImage(rs, fieldObservation, state, partitioner);
					
					handleSiteImageSpecificFields(rs, media, state);
					
					unitsToSave.add(fieldObservation); 
					

				} catch (Exception e) {
					logger.warn("Exception in " + getTableName() + ": SiteId " + figureId + ". " + e.getMessage());
					e.printStackTrace();
				} 
                
            }
           
//            logger.warn("Specimen: " + countSpecimen + ", Descriptions: " + countDescriptions );

			logger.warn(pluralString + " to save: " + unitsToSave.size());
			getOccurrenceService().saveOrUpdate(unitsToSave);	
			
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}



	private void handleSiteImageSpecificFields(ResultSet rs, Media media, AlgaTerraImportState state) throws SQLException {
		//TODO
		
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs) {
		String nameSpace;
		Class<?> cdmClass;
		Set<String> idSet;
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		try{
			Set<String> ecoFactIdSet = new HashSet<String>();
			
			while (rs.next()){
				handleForeignKey(rs, ecoFactIdSet, "EcoFactFk");
			}
			
			//field observation map
			nameSpace = AlgaTerraSpecimenImportBase.ECO_FACT_FIELD_OBSERVATION_NAMESPACE;
			cdmClass = FieldObservation.class;
			idSet = ecoFactIdSet;
			Map<String, FieldObservation> fieldObservationMap = (Map<String,FieldObservation>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, fieldObservationMap);

			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		IOValidator<BerlinModelImportState> validator = new AlgaTerraTypeImportValidator();
		return validator.validate(state);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelImportState bmState){
		AlgaTerraImportConfigurator config = ((AlgaTerraImportState) bmState).getAlgaTerraConfigurator();
		return !  ( config.isDoEcoFacts() && config.isDoImages()) ;
	}
	
}
