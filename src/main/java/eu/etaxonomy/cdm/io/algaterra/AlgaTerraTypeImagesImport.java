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
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelReferenceImport;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class AlgaTerraTypeImagesImport  extends AlgaTerraImageImportBase {
	private static final Logger logger = Logger.getLogger(AlgaTerraTypeImagesImport.class);

	
	private static int modCount = 5000;
	private static final String pluralString = "type images";
	private static final String dbTableName = "SpecimenFigure";  //??  
	
	public AlgaTerraTypeImagesImport(){
		super(dbTableName, pluralString);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery(BerlinModelImportState state) {
		String result = " SELECT SpecimenFigureId "  
				+ " FROM SpecimenFigure " 
				+ " WHERE TypeSpecimenFk is NOT NULL "
				+ " ORDER BY TypeSpecimenFk ";
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
			String strQuery =    
					
			" SELECT sf.*, sf.SpecimenFigurePhrase as FigurePhrase, sf.SpecimenFigure as fileName, sf.PicturePath as filePath" +
            " FROM SpecimenFigure sf  " 
     	+ 	" WHERE (sf.SpecimenFigureId IN (" + ID_LIST_TOKEN + ")  )"  
     	+ " ORDER BY TypeSpecimenFk ";
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
		
		Map<String, DerivedUnit> typeSpecimenMap = (Map<String, DerivedUnit>) partitioner.getObjectMap(AlgaTerraSpecimenImportBase.TYPE_SPECIMEN_DERIVED_UNIT_NAMESPACE);
		Map<String, Reference> biblioReference = (Map<String, Reference>) partitioner.getObjectMap(BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE);
		
		
		ResultSet rs = partitioner.getResultSet();

		try {
			
			int i = 0;

			//for each reference
            while (rs.next()){
                
        		if ((i++ % modCount) == 0 && i!= 1 ){ logger.info(pluralString + " handled: " + (i-1));}
				
				int figureId = rs.getInt("SpecimenFigureId");
				int typeSpecimenFk = rs.getInt("typeSpecimenFk");
				
				
				//TODO etc. Created, Notes, Copyright, TermsOfUse etc.
				
				try {
					
					//source ref
					Reference<?> sourceRef = state.getTransactionalSourceReference();
				
					DerivedUnitBase<?> derivedUnit = typeSpecimenMap.get(String.valueOf(typeSpecimenFk));
					
					if (derivedUnit == null){
						logger.warn("Could not find type specimen (" + typeSpecimenFk +") for specimen figure " +  figureId);
					}else{
						
						//field observation
						Media media = handleSingleImage(rs, derivedUnit, state, partitioner);
						
						handleTypeImageSpecificFields(rs, media, state);
						
						unitsToSave.add(derivedUnit); 
					}


				} catch (Exception e) {
					logger.warn("Exception in " + getTableName() + ": SpecimenFigureId " + figureId + ". " + e.getMessage());
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



	private void handleTypeImageSpecificFields(ResultSet rs, Media media, AlgaTerraImportState state) throws SQLException {
		//TODO refFk, refDetailFk, publishFlag
		Integer refFk = nullSafeInt(rs, "refFk");
		Integer refDetailFk = nullSafeInt(rs, "refDetailFk");
		
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
			Set<String> typeSpecimenIdSet = new HashSet<String>();
			
			while (rs.next()){
				handleForeignKey(rs, typeSpecimenIdSet, "TypeSpecimenFk");
			}
			
			//type specimen map
			nameSpace = AlgaTerraSpecimenImportBase.TYPE_SPECIMEN_DERIVED_UNIT_NAMESPACE;
			cdmClass = SpecimenOrObservationBase.class;
			idSet = typeSpecimenIdSet;
			Map<String, DerivedUnit> typeSpecimenMap = (Map<String, DerivedUnit>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, typeSpecimenMap);

			
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
		return !  ( config.isDoTypes() && config.isDoImages()) ;
//		return false;
	}
	
}
