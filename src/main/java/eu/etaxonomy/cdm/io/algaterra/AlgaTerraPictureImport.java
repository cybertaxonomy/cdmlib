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
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class AlgaTerraPictureImport  extends AlgaTerraImageImportBase {
	private static final Logger logger = Logger.getLogger(AlgaTerraPictureImport.class);

	
	private static int modCount = 5000;
	private static final String pluralString = "pictures";
	private static final String dbTableName = "Picture";  //??  
	
	public AlgaTerraPictureImport(){
		super(dbTableName, pluralString);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery(BerlinModelImportState state) {
		String result = " SELECT PictureId "  
				+ " FROM Picture p " 
				+ " WHERE p.PictureFile IS NOT NULL "
				+ " ORDER BY PictureId ";
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
			String strQuery =    
						
				" SELECT p.*, p.PicturePhrase as FigurePhrase, p.PictureFile as fileName " +
	            " FROM Picture p  " 
	            + 	" WHERE ( p.PictureID IN (" + ID_LIST_TOKEN + ")    )"  
	            + " ORDER BY PictureId ";
            ;
		return strQuery;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.berlinModel.in.ResultSetPartitioner, eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState bmState) {
		boolean success = true;
		
		AlgaTerraImportState state = (AlgaTerraImportState)bmState;
		
		Set<SpecimenOrObservationBase> specimenToSave = new HashSet<SpecimenOrObservationBase>();
		Set<TaxonBase> taxaToSave = new HashSet<TaxonBase>();
		
		Map<String, DerivedUnitBase> ecoFactMap = (Map<String, DerivedUnitBase>) partitioner.getObjectMap(AlgaTerraSpecimenImportBase.ECO_FACT_DERIVED_UNIT_NAMESPACE);
		
		ResultSet rs = partitioner.getResultSet();

		try {
			
			int i = 0;

			//for each reference
            while (rs.next()){
                
        		if ((i++ % modCount) == 0 && i!= 1 ){ logger.info(pluralString + " handled: " + (i-1));}
				
				int pictureId = rs.getInt("PictureID");
				
				
				//TODO etc. Created, Notes, Copyright, TermsOfUse etc.
				
				try {
					
//					DerivedUnitBase<?> derivedUnit = ecoFactMap.get(ecoFactFk);
//					
//					if (derivedUnit == null){
//						logger.warn("Could not find eco fact specimen (" + ecoFactFk +") for voucher image " +  figureId);
//					}else{
//						
//					}
//					
//					//field observation
//					Media media = handleSingleImage(rs, derivedUnit, state, partitioner);
//					
//					handleTypeImageSpecificFields(rs, media, state);
//					
//					specimenToSave.add(derivedUnit); 
					

				} catch (Exception e) {
					logger.warn("Exception in " + getTableName() + ": PictureId " + pictureId + ". " + e.getMessage());
					e.printStackTrace();
				} 
                
            }
           
//            logger.warn("Specimen: " + countSpecimen + ", Descriptions: " + countDescriptions );

			logger.warn(pluralString + " to save: " + specimenToSave.size());
			getOccurrenceService().save(specimenToSave);	
			getTaxonService().save(taxaToSave);	
			
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}



	private void handleTypeImageSpecificFields(ResultSet rs, Media media, AlgaTerraImportState state) throws SQLException {
		//TODO
		
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
			Set<String>voucherIdSet = new HashSet<String>();
			
			while (rs.next()){
				handleForeignKey(rs, voucherIdSet, "VoucherFk");
			}
			
			//voucher map
			nameSpace = AlgaTerraSpecimenImportBase.ECO_FACT_DERIVED_UNIT_NAMESPACE;
			cdmClass = DerivedUnitBase.class;
			idSet = voucherIdSet;
			Map<String, DerivedUnitBase> voucherMap = (Map<String,DerivedUnitBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, voucherMap);

			
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
//		return !  ( config.isDoTypes() && config.isDoImages()) ;
		return false;
	}
	
}
