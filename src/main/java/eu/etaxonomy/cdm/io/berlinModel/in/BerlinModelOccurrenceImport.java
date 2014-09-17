/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.in;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelOccurrenceImportValidator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.TdwgAreaProvider;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.OriginalSourceType;
import eu.etaxonomy.cdm.model.common.TermType;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaLevel;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
 * @author a.mueller
 * @created 20.03.2008
 */
@Component
public class BerlinModelOccurrenceImport  extends BerlinModelImportBase {
	private static final String EM_AREA_NAMESPACE = "emArea";

	private static final Logger logger = Logger.getLogger(BerlinModelOccurrenceImport.class);

	public static final String NAMESPACE = "Occurrence";
	
	
	private static int modCount = 5000;
	private static final String pluralString = "occurrences";
	private static final String dbTableName = "emOccurrence";  //??


	public BerlinModelOccurrenceImport(){
		super(dbTableName, pluralString);
	}
	
	@Override
	protected String getIdQuery(BerlinModelImportState state) {
		String result = " SELECT occurrenceId FROM " + getTableName();
		if (StringUtils.isNotBlank(state.getConfig().getOccurrenceFilter())){
			result += " WHERE " +  state.getConfig().getOccurrenceFilter(); 
		} 
		return result;
	}

	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
			String emCode = config.isIncludesAreaEmCode()? ", emArea.EMCode" : "";
			String strQuery =   //DISTINCT because otherwise emOccurrenceSource creates multiple records for a single distribution 
            " SELECT DISTINCT PTaxon.RIdentifier AS taxonId, emOccurrence.OccurrenceId, emOccurrence.Native, emOccurrence.Introduced, " +
            		" emOccurrence.Cultivated, emOccurrence.Notes occNotes, " +
            		" emOccurSumCat.emOccurSumCatId, emOccurSumCat.Short, emOccurSumCat.Description, " +  
                	" emOccurSumCat.OutputCode, emArea.AreaId, emArea.TDWGCode " + emCode + 
                " FROM emOccurrence INNER JOIN " +  
                	" emArea ON emOccurrence.AreaFk = emArea.AreaId INNER JOIN " + 
                	" PTaxon ON emOccurrence.PTNameFk = PTaxon.PTNameFk AND emOccurrence.PTRefFk = PTaxon.PTRefFk LEFT OUTER JOIN " + 
                	" emOccurSumCat ON emOccurrence.SummaryStatus = emOccurSumCat.emOccurSumCatId LEFT OUTER JOIN " +  
                	" emOccurrenceSource ON emOccurrence.OccurrenceId = emOccurrenceSource.OccurrenceFk " +  
            " WHERE (emOccurrence.OccurrenceId IN (" + ID_LIST_TOKEN + ")  )" +  
                " ORDER BY PTaxon.RIdentifier";
		return strQuery;
	}

	private Map<Integer, NamedArea> euroMedAreas = new HashMap<Integer, NamedArea>();
	
	
	@Override
	public void doInvoke(BerlinModelImportState state) {
		if (state.getConfig().isUseEmAreaVocabulary()){
			try {
				createEuroMedAreas(state);
			} catch (Exception e) {
				logger.error("Exception occurred when trying to create euroMed Areas");
				e.printStackTrace();
				state.setSuccess(false);
			}
		}
		super.doInvoke(state);
		//reset
		euroMedAreas = new HashMap<Integer, NamedArea>();
	}
	
	private TermVocabulary<NamedArea> createEuroMedAreas(BerlinModelImportState state) throws SQLException {
		logger.warn("Start creating E+M areas");
		Source source = state.getConfig().getSource();
		Reference<?> sourceReference = state.getConfig().getSourceReference();
		
		TransactionStatus txStatus = this.startTransaction();
		
		sourceReference = getSourceReference(sourceReference);
		
		TermVocabulary<NamedArea> euroMedAreas = makeEmptyEuroMedVocabulary();
		
		MarkerType eurMarkerType = getMarkerType(state, BerlinModelTransformer.uuidEurArea, "eur", "eur Area", "eur");
		MarkerType euroMedAreaMarkerType = getMarkerType(state, BerlinModelTransformer.uuidEurMedArea, "EuroMedArea", "EuroMedArea", "EuroMedArea");
		ExtensionType isoCodeExtType = getExtensionType(state, BerlinModelTransformer.uuidIsoCode, "IsoCode", "IsoCode", "iso");
		ExtensionType tdwgCodeExtType = getExtensionType(state, BerlinModelTransformer.uuidTdwgAreaCode, "TDWG code", "TDWG Area code", "tdwg");
		ExtensionType mclCodeExtType = getExtensionType(state, BerlinModelTransformer.uuidMclCode, "MCL code", "MedCheckList code", "mcl");
		NamedAreaLevel areaLevelTop = getNamedAreaLevel(state, BerlinModelTransformer.uuidAreaLevelTop, "Euro+Med top area level", "Euro+Med top area level. This level is only to be used for the area representing the complete Euro+Med area", "e+m top", null);
		NamedAreaLevel areaLevelEm1 = getNamedAreaLevel(state, BerlinModelTransformer.uuidAreaLevelFirst, "Euro+Med 1. area level", "Euro+Med 1. area level", "e+m 1.", null);
		NamedAreaLevel areaLevelEm2 = getNamedAreaLevel(state, BerlinModelTransformer.uuidAreaLevelSecond, "Euro+Med 2. area level", "Euro+Med 2. area level", "Euro+Med 1. area level", null);
		
		
		String sql = "SELECT * , CASE WHEN EMCode = 'EM' THEN 'a' ELSE 'b' END as isEM " +
				" FROM emArea " +
				" ORDER BY isEM, EMCode"; 
		ResultSet rs = source.getResultSet(sql);
		
		NamedArea euroMedArea = null;
		NamedArea lastLevel2Area = null;
		
		//euroMedArea (EMCode = 'EM')
		rs.next();
		euroMedArea = makeSingleEuroMedArea(rs, eurMarkerType, euroMedAreaMarkerType, isoCodeExtType, tdwgCodeExtType, mclCodeExtType, 
				areaLevelTop, areaLevelEm1 , areaLevelEm2, sourceReference, euroMedArea, lastLevel2Area);
		euroMedAreas.addTerm(euroMedArea);
		
		//all other areas
		while (rs.next()){
			NamedArea newArea = makeSingleEuroMedArea(rs, eurMarkerType, euroMedAreaMarkerType,
					isoCodeExtType, tdwgCodeExtType, mclCodeExtType, 
					areaLevelTop, areaLevelEm1 , areaLevelEm2, sourceReference, euroMedArea, lastLevel2Area);
			euroMedAreas.addTerm(newArea);
			if (newArea.getPartOf().equals(euroMedArea)){
				lastLevel2Area = newArea;
			}
			getVocabularyService().saveOrUpdate(euroMedAreas);
		}	
		
		commitTransaction(txStatus);
		logger.warn("Created E+M areas");
		
		return euroMedAreas;
	}

	/**
	 * @param sourceReference
	 * @return
	 */
	private Reference<?> getSourceReference(Reference<?> sourceReference) {
		Reference<?> persistentSourceReference = getReferenceService().find(sourceReference.getUuid());  //just to be sure
		if (persistentSourceReference != null){
			sourceReference = persistentSourceReference;
		}
		return sourceReference;
	}

	/**
	 * @param eurMarkerType
	 * @param euroMedAreaMarkerType
	 * @param isoCodeExtType
	 * @param tdwgCodeExtType
	 * @param mclCodeExtType
	 * @param rs
	 * @param areaLevelEm2 
	 * @param areaLevelEm1 
	 * @param areaLevelTop 
	 * @throws SQLException
	 */
	private NamedArea makeSingleEuroMedArea(ResultSet rs, MarkerType eurMarkerType,
			MarkerType euroMedAreaMarkerType, ExtensionType isoCodeExtType,
			ExtensionType tdwgCodeExtType, ExtensionType mclCodeExtType,
			NamedAreaLevel areaLevelTop, NamedAreaLevel areaLevelEm1, NamedAreaLevel areaLevelEm2, 
			Reference<?> sourceReference, NamedArea euroMedArea, NamedArea level2Area) throws SQLException {
		Integer areaId = rs.getInt("AreaId");
		String emCode = nullSafeTrim(rs.getString("EMCode"));
		String isoCode = nullSafeTrim(rs.getString("ISOCode"));
		String tdwgCode = nullSafeTrim(rs.getString("TDWGCode"));
		String unit = nullSafeTrim(rs.getString("Unit"));
//				      ,[Status]
//				      ,[OutputOrder]
		boolean eurMarker = rs.getBoolean("eur");
		boolean euroMedAreaMarker = rs.getBoolean("EuroMedArea");
		String notes = nullSafeTrim(rs.getString("Notes"));
		String mclCode = nullSafeTrim(rs.getString("MCLCode"));
		String geoSearch = nullSafeTrim(rs.getString("NameForGeoSearch"));
		
		if (isBlank(emCode)){
			emCode = unit;
		}
		
		//uuid
		UUID uuid = BerlinModelTransformer.getEMAreaUuid(emCode);
		NamedArea area = (NamedArea)getTermService().find(uuid);
		if (area == null){
			//label
			area = NamedArea.NewInstance(geoSearch, unit, emCode);
			if (uuid != null){
				area.setUuid(uuid);
			}else{
				logger.warn("Uuuid for emCode could not be defined: " + emCode);
			}
		}
		
		
		//code
		area.setIdInVocabulary(emCode);
		//notes
		if (StringUtils.isNotEmpty(notes)){
			area.addAnnotation(Annotation.NewInstance(notes, AnnotationType.EDITORIAL(), Language.DEFAULT()));
		}
		//markers
		area.addMarker(Marker.NewInstance(eurMarkerType, eurMarker));
		area.addMarker(Marker.NewInstance(euroMedAreaMarkerType, euroMedAreaMarker));
		
		//extensions
		if (isNotBlank(isoCode)){
			area.addExtension(isoCode, isoCodeExtType);
		}
		if (isNotBlank(tdwgCode)){
			area.addExtension(tdwgCode, tdwgCodeExtType);
		}
		if (isNotBlank(mclCode)){
			area.addExtension(mclCode, mclCodeExtType);
		}
		
		//type
		area.setType(NamedAreaType.ADMINISTRATION_AREA());
		
		//source
		area.addSource(OriginalSourceType.Import, String.valueOf(areaId), EM_AREA_NAMESPACE, sourceReference, null);
		
		//parent
		if (euroMedArea != null){
			if (emCode.contains("(")){
				area.setPartOf(level2Area);
				area.setLevel(areaLevelEm2);
			}else{
				area.setPartOf(euroMedArea);
				area.setLevel(areaLevelEm1);
			}
		}else{
			area.setLevel(areaLevelTop);
		}
		this.euroMedAreas.put(areaId, area);
		
		//save
		getTermService().saveOrUpdate(area);
		
		return area;
	}

	private String nullSafeTrim(String string) {
		if (string == null){
			return null;
		}else{
			return string.trim();
		}
	}

	/**
	 * 
	 */
	private TermVocabulary<NamedArea> makeEmptyEuroMedVocabulary() {
		TermType type = TermType.NamedArea;
		String description = "Euro+Med area vocabulary";
		String label = "E+M areas";
		String abbrev = null;
		URI termSourceUri = null;
		TermVocabulary<NamedArea> result = TermVocabulary.NewInstance(type, description, label, abbrev, termSourceUri);
		getVocabularyService().save(result);
		return result;
	}

	@Override
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState state) {
		boolean success = true;
		Set<TaxonBase> taxaToSave = new HashSet<TaxonBase>();
		
		Map<String, TaxonBase<?>> taxonMap = (Map<String, TaxonBase<?>>) partitioner.getObjectMap(BerlinModelTaxonImport.NAMESPACE);
			
		ResultSet rs = partitioner.getResultSet();

		try {
			//map to store the mapping of duplicate berlin model occurrences to their real distributions
			//duplicated may occur due to area mappings from BM areas to TDWG areas
			Map<Integer, String> duplicateMap = new HashMap<Integer, String>();
			int oldTaxonId = -1;
			TaxonDescription oldDescription = null;
			int i = 0;
			int countDescriptions = 0;
			int countDistributions = 0;
			int countDuplicates = 0;
			//for each reference
            while (rs.next()){
                
            	if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("Facts handled: " + (i-1));}
                
                int occurrenceId = rs.getInt("OccurrenceId");
                int newTaxonId = rs.getInt("taxonId");
                String notes = nullSafeTrim(rs.getString("occNotes"));
                
                Integer emStatusId = nullSafeInt(rs, "emOccurSumCatId");
                
                try {
                	//status
                	PresenceAbsenceTermBase<?> status = null;
                	String alternativeStatusString = null;
					if (emStatusId != null){
						status = BerlinModelTransformer.occStatus2PresenceAbsence(emStatusId);
					}else{
						String[] stringArray = new String[]{rs.getString("Native"), rs.getString("Introduced"), rs.getString("Cultivated")};
						alternativeStatusString = CdmUtils.concat(",", stringArray);
					}
                     
					Reference<?> sourceRef = state.getTransactionalSourceReference();
                    
					List<NamedArea> areas = makeAreaList(state, rs,	occurrenceId);
                     
                    //create description(elements)
                    TaxonDescription taxonDescription = getTaxonDescription(newTaxonId, oldTaxonId, oldDescription, taxonMap, occurrenceId, sourceRef);
                    for (NamedArea area : areas){
                    	Distribution distribution = Distribution.NewInstance(area, status);
                        if (status == null){
                        	AnnotationType annotationType = AnnotationType.EDITORIAL();
                        	Annotation annotation = Annotation.NewInstance(alternativeStatusString, annotationType, null);
                        	distribution.addAnnotation(annotation);
                        	distribution.addMarker(Marker.NewInstance(MarkerType.PUBLISH(), false));
                        }
//                      distribution.setCitation(sourceRef);
                        if (taxonDescription != null) { 
                        	Distribution duplicate = checkIsNoDuplicate(taxonDescription, distribution, duplicateMap , occurrenceId);
                            if (duplicate == null){
                            	taxonDescription.addElement(distribution); 
	                            distribution.addImportSource(String.valueOf(occurrenceId), NAMESPACE, state.getTransactionalSourceReference(), null);
	                        	countDistributions++; 
	                            if (taxonDescription != oldDescription){ 
	                            	taxaToSave.add(taxonDescription.getTaxon()); 
	                                oldDescription = taxonDescription; 
	                                countDescriptions++; 
	                            } 
                            }else{                          	  
                            	countDuplicates++;
                            	duplicate.addImportSource(String.valueOf(occurrenceId), NAMESPACE, state.getTransactionalSourceReference(), null);
                            	logger.info("Distribution is duplicate");	                           }
                        } else { 
                        	logger.warn("Distribution " + area.getLabel() + " ignored. OccurrenceId = " + occurrenceId);
	                       	success = false;
	                    }
                        //notes
                        if (isNotBlank(notes)){
                        	Annotation annotation = Annotation.NewInstance(notes, Language.DEFAULT());
                        	distribution.addAnnotation(annotation);
                        }
                    }
                } catch (UnknownCdmTypeException e) {
                     logger.error("Unknown presenceAbsence status id: " + emStatusId); 
                	e.printStackTrace();
                     success = false;
                }
            }
           
            logger.info("Distributions: " + countDistributions + ", Descriptions: " + countDescriptions );
			logger.info("Duplicate occurrences: "  + (countDuplicates));

			logger.info("Taxa to save: " + taxaToSave.size());
			getTaxonService().save(taxaToSave);	
			
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}

	/**
	 * @param state
	 * @param rs
	 * @param occurrenceId
	 * @param tdwgCodeString
	 * @param emCodeString
	 * @return
	 * @throws SQLException
	 */
	//Create area list
	private List<NamedArea> makeAreaList(BerlinModelImportState state, ResultSet rs, int occurrenceId) throws SQLException {
		List<NamedArea> areas = new ArrayList<NamedArea>();
		
		if (state.getConfig().isUseEmAreaVocabulary()){
			Integer areaId = rs.getInt("AreaId");
	        NamedArea area = this.euroMedAreas.get(areaId);
			areas.add(area);
		}else{
	        String tdwgCodeString = rs.getString("TDWGCode");
	        String emCodeString = state.getConfig().isIncludesAreaEmCode() ? rs.getString("EMCode") : null;
	
			if (tdwgCodeString != null){
			
				String[] tdwgCodes = new String[]{tdwgCodeString};
				if (state.getConfig().isSplitTdwgCodes()){
					tdwgCodes = tdwgCodeString.split(";");
				}
				
				for (String tdwgCode : tdwgCodes){
					NamedArea area = TdwgAreaProvider.getAreaByTdwgAbbreviation(tdwgCode.trim());
			    	if (area == null){
			    		area = getOtherAreas(state, emCodeString, tdwgCodeString);
			    	}
			    	if (area != null){
			    		areas.add(area);
			    	}
				}
			 }
			
			 if (areas.size()== 0){
				 NamedArea area = getOtherAreas(state, emCodeString, tdwgCodeString);
				 if (area != null){
			         areas.add(area);
			   }
			 }
			 if (areas.size() == 0){
				 String areaId = rs.getString("AreaId");
				 logger.warn("No areas defined for occurrence " + occurrenceId + ". EMCode: " + CdmUtils.Nz(emCodeString).trim() + ". AreaId: " + areaId );
			 }
		}
		return areas;
	}

	@Override
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs, BerlinModelImportState state) {
		String nameSpace;
		Class<?> cdmClass;
		Set<String> idSet;
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		try{
			Set<String> taxonIdSet = new HashSet<String>();
			while (rs.next()){
				handleForeignKey(rs, taxonIdSet, "taxonId");
			}
			
			//taxon map
			nameSpace = BerlinModelTaxonImport.NAMESPACE;
			cdmClass = TaxonBase.class;
			idSet = taxonIdSet;
			Map<String, TaxonBase> objectMap = (Map<String, TaxonBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, objectMap);

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}



	/**
     * Tests if a distribution with the same tdwgArea and the same status already exists in the description. 
     * If so the old distribution is returned 
     * @param description
     * @param tdwgArea
     * @return false, if dupplicate exists. True otherwise.
     */
    private Distribution checkIsNoDuplicate(TaxonDescription description, Distribution distribution, Map<Integer, String> duplicateMap, Integer bmDistributionId){
    	for (DescriptionElementBase descElBase : description.getElements()){
    		if (descElBase.isInstanceOf(Distribution.class)){
    			Distribution oldDistr = HibernateProxyHelper.deproxy(descElBase, Distribution.class);
    			NamedArea oldArea = oldDistr.getArea();
    			if (oldArea != null && oldArea.equals(distribution.getArea())){
    				PresenceAbsenceTermBase<?> oldStatus = oldDistr.getStatus();
    				if (oldStatus != null && oldStatus.equals(distribution.getStatus())){
    					duplicateMap.put(bmDistributionId, oldDistr.getSources().iterator().next().getIdInSource());
    					return oldDistr;
    				}
    			}
    		}
    	}
    	return null;
    }
	
	/**
	 * Use same TaxonDescription if two records belong to the same taxon 
	 * @param newTaxonId
	 * @param oldTaxonId
	 * @param oldDescription
	 * @param taxonMap
	 * @return
	 */
	private TaxonDescription getTaxonDescription(int newTaxonId, int oldTaxonId, TaxonDescription oldDescription, Map<String, TaxonBase<?>> taxonMap, int occurrenceId, Reference<?> sourceSec){
		TaxonDescription result = null;
		if (oldDescription == null || newTaxonId != oldTaxonId){
			TaxonBase<?> taxonBase = taxonMap.get(String.valueOf(newTaxonId));
			//TODO for testing
			//TaxonBase taxonBase = Taxon.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null);
			Taxon taxon;
			if ( taxonBase instanceof Taxon ) {
				taxon = (Taxon) taxonBase;
			} else if (taxonBase != null) {
				logger.warn("TaxonBase for Occurrence " + occurrenceId + " was not of type Taxon but: " + taxonBase.getClass().getSimpleName());
				return null;
			} else {
				logger.warn("TaxonBase for Occurrence " + occurrenceId + " is null.");
				return null;
			}		
			Set<TaxonDescription> descriptionSet= taxon.getDescriptions();
			if (descriptionSet.size() > 0) {
				result = descriptionSet.iterator().next(); 
			}else{
				result = TaxonDescription.NewInstance();
				result.setTitleCache(sourceSec.getTitleCache(), true);
				taxon.addDescription(result);
			}
		}else{
			result = oldDescription;
		}
		return result;
	}
	
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		IOValidator<BerlinModelImportState> validator = new BerlinModelOccurrenceImportValidator();
		return validator.validate(state);
	}


	@Override
	protected boolean isIgnore(BerlinModelImportState state){
		if (! state.getConfig().isDoOccurrence()){
			return true;
		}else{
			if (!this.checkSqlServerColumnExists(state.getConfig().getSource(), "emOccurrence", "OccurrenceId")){
				logger.error("emOccurrence table or emOccurrenceId does not exist. Must ignore occurrence import");
				return true;
			}else{
				return false;
			}
		}
	}
	
}
