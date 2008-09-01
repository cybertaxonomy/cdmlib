/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.berlinModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatus;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;



/**
 * @author a.mueller
 *
 */
public class BerlinModelTypesIO extends BerlinModelIOBase /*implements IIO<BerlinModelImportConfigurator>*/ {
	private static final Logger logger = Logger.getLogger(BerlinModelTypesIO.class);

	private static int modCount = 10000;
	
	public BerlinModelTypesIO(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config){
		boolean result = true;
		logger.warn("Checking for Types not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(IImportConfigurator config, CdmApplicationController cdmApp, 
			Map<String, MapWrapper<? extends CdmBase>> stores){
		
		MapWrapper<TaxonNameBase> taxonNameMap = (MapWrapper<TaxonNameBase>)stores.get(ICdmIO.TAXONNAME_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		
		boolean result = true;
		Set<TaxonNameBase> taxonNameStore = new HashSet<TaxonNameBase>();
		BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
		Source source = bmiConfig.getSource();
		INameService nameService = cdmApp.getNameService();
		
		Map<Integer, Specimen> typeMap = new HashMap<Integer, Specimen>();
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeTypes ...");
		
		try {
			//get data from database
			String strQuery = 
					" SELECT TypeDesignation.*, TypeStatus.Status " + 
					" FROM TypeDesignation LEFT OUTER JOIN " +
                      	" TypeStatus ON TypeDesignation.TypeStatusFk = TypeStatus.TypeStatusId " + 
                    " WHERE (1=1) ";
			ResultSet rs = source.getResultSet(strQuery) ;

			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("Types handled: " + (i-1));}
				
				int typeDesignationId = rs.getInt("typeDesignationId");
				int nameId = rs.getInt("nameFk");
				int typeStatusFk = rs.getInt("typeStatusFk");
				int refFk = rs.getInt("refFk");
				String refDetail = rs.getString("refDetail");
				String status = rs.getString("Status");
				String typePhrase = rs.getString("typePhrase");
				
				//TODO 
				boolean isNotDesignated = false;
				
				
				//TODO
				//TypeCache leer
				//RejectedFlag false
				//PublishFlag xxx
				
				
				TaxonNameBase taxonNameBase = taxonNameMap.get(nameId);
				
				if (taxonNameBase != null){
					try{
						TypeDesignationStatus typeDesignationStatus = BerlinModelTransformer.typeStatusId2TypeStatus(typeStatusFk);
						ReferenceBase citation = referenceMap.get(refFk);
						
						Specimen specimen = Specimen.NewInstance();
						specimen.setTitleCache(typePhrase);
						boolean addToAllNames = true;
						String originalNameString = null;
						taxonNameBase.addSpecimenTypeDesignation(specimen, typeDesignationStatus, citation, refDetail, originalNameString, isNotDesignated, addToAllNames);
												
						typeMap.put(typeDesignationId, specimen);
						taxonNameStore.add(taxonNameBase);
						
						//TODO
						//Update, Created, Notes, origId
						//doIdCreatedUpdatedNotes(bmiConfig, media, rs, nameFactId);

					}catch (UnknownCdmTypeException e) {
						logger.warn("TypeStatus '" + status + "' not yet implemented");
					}
				}else{
					//TODO
					logger.warn("TaxonName for TypeDesignation " + typeDesignationId + " does not exist in store");
				}
				//put
			}
			
			result &= makeFigures(typeMap, source);
			
			
			logger.info("Names to save: " + taxonNameStore.size());
			nameService.saveTaxonNameAll(taxonNameStore);	
			
			logger.info("end makeTypes ...");
			return result;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}
	
	private static boolean makeFigures(Map<Integer, Specimen> typeMap, Source source){
		try {
			//get data from database
			String strQuery = 
					" SELECT * " +
					" FROM TypeFigure " + 
                    " WHERE (1=1) ";
			ResultSet rs = source.getResultSet(strQuery) ;

			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0){ logger.info("TypesFigures handled: " + (i-1));}
				
				Integer typeFigureId = rs.getInt("typeFigureId");
				Integer typeDesignationFk = rs.getInt("typeDesignationFk");
				Integer collectionFk = rs.getInt("collectionFk");
				String filename = rs.getString("filename");
				String figurePhrase = rs.getString("figurePhrase");
				
				String mimeType = null; //"image/jpg";
				String suffix = null; //"jpg";
				Media media = ImageFile.NewMediaInstance(null, null, filename, mimeType, suffix, null, null, null);
				if (figurePhrase != null) {
					media.addAnnotation(Annotation.NewDefaultLanguageInstance(figurePhrase));
				}
				Specimen typeSpecimen = typeMap.get(typeDesignationFk);
				if (typeSpecimen != null) {
					typeSpecimen.addMedia(media);
				}
				
				//mimeType + suffix
				//TODO
				//RefFk
				//RefDetail
				//VerifiedBy
				//VerifiedWhen
				//PrefFigureFlag
				//PublishedFlag
				//etc.
			}
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
			
		return true;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoTypes();
	}

	
}
