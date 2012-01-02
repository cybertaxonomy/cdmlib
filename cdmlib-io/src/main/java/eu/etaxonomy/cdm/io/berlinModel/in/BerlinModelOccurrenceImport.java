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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelOccurrenceImportValidator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.location.WaterbodyOrCountry;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelOccurrenceImport  extends BerlinModelImportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelOccurrenceImport.class);

	public static final String NAMESPACE = "Occurrence";
	
	
	private static int modCount = 5000;
	private static final String pluralString = "occurrences";
	private static final String dbTableName = "emOccurrence";  //??


	public BerlinModelOccurrenceImport(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery(BerlinModelImportState state) {
		String result = " SELECT occurrenceId FROM " + getTableName();
		if (StringUtils.isNotBlank(state.getConfig().getOccurrenceFilter())){
			result += " WHERE " +  state.getConfig().getOccurrenceFilter(); 
		} 
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
			String strQuery =   //DISTINCT because otherwise emOccurrenceSource creates multiple records for a single distribution 
            " SELECT DISTINCT PTaxon.RIdentifier AS taxonId, emOccurrence.OccurrenceId, emOccurrence.Native, emOccurrence.Introduced, " +
            		" emOccurrence.Cultivated, emOccurSumCat.emOccurSumCatId, emOccurSumCat.Short, emOccurSumCat.Description, " +  
                	" emOccurSumCat.OutputCode, emArea.AreaId, emArea.TDWGCode, emArea.EMCode " + 
                " FROM emOccurrence INNER JOIN " +  
                	" emArea ON emOccurrence.AreaFk = emArea.AreaId INNER JOIN " + 
                	" PTaxon ON emOccurrence.PTNameFk = PTaxon.PTNameFk AND emOccurrence.PTRefFk = PTaxon.PTRefFk LEFT OUTER JOIN " + 
                	" emOccurSumCat ON emOccurrence.SummaryStatus = emOccurSumCat.emOccurSumCatId LEFT OUTER JOIN " +  
                	" emOccurrenceSource ON emOccurrence.OccurrenceId = emOccurrenceSource.OccurrenceFk " +  
            " WHERE (emOccurrence.OccurrenceId IN (" + ID_LIST_TOKEN + ")  )" +  
                " ORDER BY PTaxon.RIdentifier";
		return strQuery;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.berlinModel.in.ResultSetPartitioner, eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState state) {
		boolean success = true;
		Set<TaxonBase> taxaToSave = new HashSet<TaxonBase>();
		
		Map<String, TaxonBase> taxonMap = (Map<String, TaxonBase>) partitioner.getObjectMap(BerlinModelTaxonImport.NAMESPACE);
			
		ResultSet rs = partitioner.getResultSet();

		try {
			//map to store the mapping of duplicate berlin model occurrences to their real distributions
			//duplicated may occurr due to area mappings from BM areas to TDWG areas
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
                String tdwgCodeString = rs.getString("TDWGCode");
                String emCodeString = rs.getString("EMCode");
                Integer emStatusId = (Integer)rs.getObject("emOccurSumCatId");
                
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
                     
                     //Create area list
                     List<NamedArea> areas = new ArrayList<NamedArea>();
                     if (tdwgCodeString != null){
                           String[] tdwgCodes = tdwgCodeString.split(";");
                           for (String tdwgCode : tdwgCodes){
                                 NamedArea area = TdwgArea.getAreaByTdwgAbbreviation(tdwgCode.trim());
                            	 if (area == null){
                            		 area = getOtherAreas(state, emCodeString, tdwgCodeString);
                            	 }
                                 if (area != null){
                                       areas.add(area);
                                 }
                           }
                     }
                     Reference<?> sourceRef = state.getConfig().getSourceReference();
                     //create description(elements)
                     TaxonDescription taxonDescription = getTaxonDescription(newTaxonId, oldTaxonId, oldDescription, taxonMap, occurrenceId, sourceRef);
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
                     for (NamedArea area : areas){
                           Distribution distribution = Distribution.NewInstance(area, status);
                           if (status == null){
                        	   AnnotationType annotationType = AnnotationType.EDITORIAL();
                        	   Annotation annotation = Annotation.NewInstance(alternativeStatusString, annotationType, null);
                        	   distribution.addAnnotation(annotation);
                        	   distribution.addMarker(Marker.NewInstance(MarkerType.PUBLISH(), false));
                           }
//                         distribution.setCitation(sourceRef);
                           if (taxonDescription != null) { 
                        	   Distribution duplicate = checkIsNoDuplicate(taxonDescription, distribution, duplicateMap , occurrenceId);
                               if (duplicate == null){
	                        	   taxonDescription.addElement(distribution); 
	                               distribution.addSource(String.valueOf(occurrenceId), NAMESPACE, state.getConfig().getSourceReference(), null);
	                        	   countDistributions++; 
	                               if (taxonDescription != oldDescription){ 
	                            	   taxaToSave.add(taxonDescription.getTaxon()); 
	                                   oldDescription = taxonDescription; 
	                                   countDescriptions++; 
	                               	} 
                               }else{                          	  
                            	   countDuplicates++;
                            	   duplicate.addSource(String.valueOf(occurrenceId), NAMESPACE, state.getConfig().getSourceReference(), null);
                            	   logger.info("Distribution is duplicate");	                           }
	                       	} else { 
	                       		logger.warn("Distribution " + area.getLabel() + " ignored. OccurrenceId = " + occurrenceId);
	                       		success = false;
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

	private NamedArea getOtherAreas(BerlinModelImportState state, String emCodeString, String tdwgCodeString) {
		String em = CdmUtils.Nz(emCodeString).trim();
		String tdwg = CdmUtils.Nz(tdwgCodeString).trim();
		//Cichorieae + E+M
		if ("EM".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.euroMedUuid, "Euro+Med", "Euro+Med area", "EM", null, null);
		}else if("Rf".equals(em)){
			return WaterbodyOrCountry.RUSSIANFEDERATION();
		
		//Cichorieae
		}else if("KRY-OO + UKR-UK".equals(tdwg)){
			return WaterbodyOrCountry.UKRAINE();
		}else if("TCS-AZ + TCS-NA".equals(tdwg)){
			return WaterbodyOrCountry.AZERBAIJANREPUBLICOF();
		}else if("TCS-AB + TCS-AD + TCS-GR".equals(tdwg)){
			return WaterbodyOrCountry.GEORGIA();
		}
		//E+M
		else if("EUR".equals(em)){
			return TdwgArea.getAreaByTdwgAbbreviation("1");
		}else if("14".equals(em)){
			return TdwgArea.getAreaByTdwgAbbreviation("14");
		}else if("21".equals(em)){
			return TdwgArea.getAreaByTdwgAbbreviation("21");
		}else if("33".equals(em)){
			return TdwgArea.getAreaByTdwgAbbreviation("33");
		
		//TODO Azores, Canary Is. 
		
		}else if("Ir".equals(em)){
			return WaterbodyOrCountry.ISRAELSTATEOF();
		}else if("Md(D)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidDesertas, "Desertas", "Euro+Med area 'Desertas'", "Md(D)", null, null);
		}else if("Md(M)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidMadeira, "Madeira", "Euro+Med area 'Madeira'", "Md(M)", null, null);
		}else if("Md(P)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidPortoSanto, "Porto Santo", "Euro+Med area 'Porto Santo'", "Md(P)", null, null);
		}else if("Rf(E)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidEasternEuropeanRussia, "Eastern European Russia", "Euro+Med area 'Eastern European Russia'", "Rf(E)", null, null);
		}else if("SM".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidSerbiaMontenegro, "Serbia & Montenegro", "Euro+Med area 'Serbia & Montenegro'", "SM", NamedAreaType.ADMINISTRATION_AREA(), null);
		}else if("LS".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidLebanonSyria, "Lebanon-Syria", "Euro+Med area 'Lebanon-Syria'", "LS", null, null);
		}else if("Rs".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidUssr, "Former USSR", "Euro+Med area 'Former USSR'", "Rs", NamedAreaType.ADMINISTRATION_AREA(), null);
		}else if("Si".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidSicilyMalta, "Sicily with Malta", "Euro+Med area 'Sicily with Malta'", "Si", null, null);
		}else if("Az(L)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidFlores, "Flores", "Euro+Med area 'Flores'", "Az(L)", null, null);
		}else if("Rs(N)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidRussiaNorthern, "Russia Northern", "Euro+Med area 'Russia Northern'", "Rs(N)", null, null);
		}else if("Rs(B)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidRussiaBaltic, "Russia Baltic", "Euro+Med area 'Russia Baltic'", "Rs(B)", null, null);
		}else if("Rs(C)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidRussiaCentral, "Russia Central", "Euro+Med area 'Russia Central'", "Rs(C)", null, null);
		}else if("Rs(W)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidRussiaSouthWest, "Russia Southwest", "Euro+Med area 'Russia Southwest'", "Rs(W)", null, null);
		}else if("Rs(E)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidRussiaSouthEast, "Russia Southeast", "Euro+Med area 'Russia Southeast'", "Rs(E)", null, null);
		}else if("AE".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidEastAegeanIslands, "East Aegean Islands", "Euro+Med area 'East Aegean Islands'", "AE", null, null);
		}else if("AE(T)".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidTurkishEastAegeanIslands, "Turkish East Aegean Islands", "Euro+Med area 'Turkish East Aegean Islands'", "AE(T)", null, null);
		}else if("Bt".equals(em)){
			return getNamedArea(state, BerlinModelTransformer.uuidBalticStates, "Baltic states (Estonia, Latvia, Lithuania) and Kaliningrad region", "Euro+Med area 'Baltic states (Estonia, Latvia, Lithuania) and Kaliningrad region'", "Bt", null, null);
		}
		
		return null;
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
	private TaxonDescription getTaxonDescription(int newTaxonId, int oldTaxonId, TaxonDescription oldDescription, Map<String, TaxonBase> taxonMap, int occurrenceId, Reference<?> sourceSec){
		TaxonDescription result = null;
		if (oldDescription == null || newTaxonId != oldTaxonId){
			TaxonBase taxonBase = taxonMap.get(String.valueOf(newTaxonId));
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
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		IOValidator<BerlinModelImportState> validator = new BerlinModelOccurrenceImportValidator();
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
		return ! state.getConfig().isDoOccurrence();
	}
	
}
