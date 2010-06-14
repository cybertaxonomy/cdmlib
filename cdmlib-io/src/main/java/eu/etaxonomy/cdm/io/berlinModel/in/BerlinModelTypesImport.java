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

import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelTypesImportValidator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.media.ImageFile;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelTypesImport extends BerlinModelImportBase /*implements IIO<BerlinModelImportConfigurator>*/ {
	private static final Logger logger = Logger.getLogger(BerlinModelTypesImport.class);

	private static int modCount = 10000;
	private static final String pluralString = "types";
	private static final String dbTableName = "TypeDesignation";

	
	public BerlinModelTypesImport(){
		super();
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
		String strRecordQuery = 
			" SELECT TypeDesignation.*, TypeStatus.Status " + 
			" FROM TypeDesignation LEFT OUTER JOIN " +
			" TypeStatus ON TypeDesignation.TypeStatusFk = TypeStatus.TypeStatusId " + 
			" WHERE (TypeDesignationId IN ("+ ID_LIST_TOKEN + ") )";
		return strRecordQuery;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.berlinModel.in.ResultSetPartitioner, eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState state) {
		boolean result = true;
		Set<TaxonNameBase> namesToSave = new HashSet<TaxonNameBase>();
		Map<Integer, Specimen> typeMap = new HashMap<Integer, Specimen>();
		
		Map<String, TaxonNameBase> nameMap = (Map<String, TaxonNameBase>) partitioner.getObjectMap(BerlinModelTaxonNameImport.NAMESPACE);
		Map<String, ReferenceBase> biblioRefMap = partitioner.getObjectMap(BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE);
		Map<String, ReferenceBase> nomRefMap = partitioner.getObjectMap(BerlinModelReferenceImport.NOM_REFERENCE_NAMESPACE);

		BerlinModelImportConfigurator config = state.getConfig();
		Source source = config.getSource();
		
		ResultSet rs = partitioner.getResultSet();

		try {

			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("Types handled: " + (i-1));}
				
				int typeDesignationId = rs.getInt("typeDesignationId");
				int nameId = rs.getInt("nameFk");
				int typeStatusFk = rs.getInt("typeStatusFk");
				Object refFkObj = rs.getObject("refFk");
				String refDetail = rs.getString("refDetail");
				String status = rs.getString("Status");
				String typePhrase = rs.getString("typePhrase");
				
				//TODO 
				boolean isNotDesignated = false;
				
				
				//TODO
				//TypeCache leer
				//RejectedFlag false
				//PublishFlag xxx
				
				TaxonNameBase<?,?> taxonNameBase = nameMap.get(String.valueOf(nameId));
				
				if (taxonNameBase != null){
					try{
						SpecimenTypeDesignationStatus typeDesignationStatus = BerlinModelTransformer.typeStatusId2TypeStatus(typeStatusFk);
						ReferenceBase<?> citation = null;
						if (refFkObj != null){
							String relRefFk = String.valueOf(refFkObj);
							//get nomRef
							citation = getReferenceOnlyFromMaps(biblioRefMap, nomRefMap, 
									relRefFk);
							}
						
						Specimen specimen = Specimen.NewInstance();
						specimen.setTitleCache(typePhrase);
						boolean addToAllNames = true;
						String originalNameString = null;
						taxonNameBase.addSpecimenTypeDesignation(specimen, typeDesignationStatus, citation, refDetail, originalNameString, isNotDesignated, addToAllNames);
												
						typeMap.put(typeDesignationId, specimen);
						namesToSave.add(taxonNameBase);
						
						//TODO
						//Update, Created, Notes, origId
						//doIdCreatedUpdatedNotes(bmiConfig, media, rs, nameFactId);

					}catch (UnknownCdmTypeException e) {
						logger.warn("TypeStatus '" + status + "' not yet implemented");
						result = false;
					}
				}else{
					//TODO
					logger.warn("TaxonName for TypeDesignation " + typeDesignationId + " does not exist in store");
					result = false;
				}
				//put
			}
			
			result &= makeFigures(typeMap, source);
			logger.info("Names to save: " + namesToSave.size());
			getNameService().save(namesToSave);	
			return result;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
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
			Set<String> nameIdSet = new HashSet<String>();
			Set<String> referenceIdSet = new HashSet<String>();
			while (rs.next()){
				handleForeignKey(rs, nameIdSet, "NameFk");
				handleForeignKey(rs, referenceIdSet, "RefFk");
	}
	
			//name map
			nameSpace = BerlinModelTaxonNameImport.NAMESPACE;
			cdmClass = TaxonNameBase.class;
			idSet = nameIdSet;
			Map<String, Person> objectMap = (Map<String, Person>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, objectMap);

			//nom reference map
			nameSpace = BerlinModelReferenceImport.NOM_REFERENCE_NAMESPACE;
			cdmClass = ReferenceBase.class;
			idSet = referenceIdSet;
			Map<String, ReferenceBase> nomReferenceMap = (Map<String, ReferenceBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, nomReferenceMap);

			//biblio reference map
			nameSpace = BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE;
			cdmClass = ReferenceBase.class;
			idSet = referenceIdSet;
			Map<String, ReferenceBase> biblioReferenceMap = (Map<String, ReferenceBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, biblioReferenceMap);
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	private static boolean makeFigures(Map<Integer, Specimen> typeMap, Source source){
		boolean success = true;
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
			
		return success;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		IOValidator<BerlinModelImportState> validator = new BerlinModelTypesImportValidator();
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
		return ! state.getConfig().isDoTypes();
	}

}
