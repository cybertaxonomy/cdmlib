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
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelTaxonImport;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * Import for AlgaTerra images from table 
 * @author a.mueller
 * @created 18.01.2013
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
		String result = " SELECT p.PictureId "  
				+ " FROM Picture p  INNER JOIN Fact f ON p.PictureId = f.ExtensionFk LEFT OUTER JOIN PTaxon pt ON f.PTNameFk = pt.PTNameFk AND f.PTRefFk = pt.PTRefFk " 
				+ " WHERE f.FactCategoryFk = 205 "
				+ " ORDER BY p.PictureId ";
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
			String strQuery =    
						
				" SELECT p.*, f.*, pt.RIdentifier, p.PicturePhrase as FigurePhrase, p.PictureFile as fileName, p.PicturePath as filePath " +
	            " FROM Picture p INNER JOIN Fact f ON p.PictureId = f.ExtensionFk LEFT OUTER JOIN PTaxon pt ON f.PTNameFk = pt.PTNameFk AND f.PTRefFk = pt.PTRefFk " 
	            + 	" WHERE f.FactCategoryFk = 205 AND ( p.PictureID IN (" + ID_LIST_TOKEN + ")     )"  
	            + " ORDER BY p.PictureId, f.factId, pt.RIdentifier ";
            ;
		return strQuery;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.berlinModel.in.ResultSetPartitioner, eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState bmState) {
		boolean success = true;
		
		AlgaTerraImportState state = (AlgaTerraImportState)bmState;
		
//		Set<SpecimenOrObservationBase> specimenToSave = new HashSet<SpecimenOrObservationBase>();
		Set<TaxonBase> taxaToSave = new HashSet<TaxonBase>();
		
//		Map<String, DerivedUnitBase> ecoFactMap = (Map<String, DerivedUnitBase>) partitioner.getObjectMap(AlgaTerraSpecimenImportBase.ECO_FACT_DERIVED_UNIT_NAMESPACE);
		Map<String, TaxonBase> taxonMap = (Map<String, TaxonBase>) partitioner.getObjectMap(BerlinModelTaxonImport.NAMESPACE);
		
		ResultSet rs = partitioner.getResultSet();

		try {
			
			int i = 0;

			//for each reference
            while (rs.next()){
                
        		if ((i++ % modCount) == 0 && i!= 1 ){ logger.info(pluralString + " handled: " + (i-1));}
				
				int pictureId = rs.getInt("PictureID");
				int taxonId = rs.getInt("RIdentifier");
				int factId = rs.getInt("FactId");
				
				
				//TODO etc. Created, Notes, Copyright, TermsOfUse etc.
				
				try {
					
					TaxonBase<?> taxonBase = taxonMap.get(String.valueOf(taxonId));
					if (taxonBase == null){
						logger.warn("Could not find taxon (" + taxonId +") for picture fact " +  factId);
					}else if (! taxonBase.isInstanceOf(Taxon.class)){
						logger.warn("Taxon is not of class Taxon but " + taxonBase.getClass() + ". RIdentifier: " + taxonId + " PictureId: " +  pictureId + ", FactId: "+  factId);
						
					}else{
						Taxon taxon = CdmBase.deproxy(taxonBase, Taxon.class);
						
						Media media = handleSingleImage(rs, taxon, state, partitioner);
						
						handlePictureSpecificFields(rs, media, state);
						
						taxaToSave.add(taxon); 
					}
	

				} catch (Exception e) {
					logger.warn("Exception in " + getTableName() + ": PictureId " + pictureId + ". " + e.getMessage());
					e.printStackTrace();
				} 
                
            }
           
//            logger.warn("Specimen: " + countSpecimen + ", Descriptions: " + countDescriptions );

			logger.warn(pluralString + " to save: " + taxaToSave.size());
//			getOccurrenceService().save(taxaToSave);	
			getTaxonService().save(taxaToSave);	
			
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}



	private void handlePictureSpecificFields(ResultSet rs, Media media, AlgaTerraImportState state) throws SQLException {
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
			Set<String> taxonIdSet = new HashSet<String>();
			
			while (rs.next()){
				handleForeignKey(rs, taxonIdSet, "RIdentifier");
			}
			
			//taxon map
			nameSpace = BerlinModelTaxonImport.NAMESPACE;
			cdmClass = TaxonBase.class;
			idSet = taxonIdSet;
			Map<String, TaxonBase> taxonMap = (Map<String,TaxonBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, taxonMap);

			
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
	}
	
}
