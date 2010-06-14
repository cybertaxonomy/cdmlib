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

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelOccurrenceImportValidator;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelOccurrenceImport  extends BerlinModelImportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelOccurrenceImport.class);

	private static int modCount = 10000;
	private static final String pluralString = "occurrences";
	private static final String dbTableName = "emOccurrence";  //??


	public BerlinModelOccurrenceImport(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery() {
		return " SELECT occurrenceId FROM " + getTableName();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
			String strQuery =   //DISTINCT because otherwise emOccurrenceSource creates multiple records for a single distribution 
            " SELECT DISTINCT PTaxon.RIdentifier AS taxonId, emOccurrence.OccurrenceId, emOccurSumCat.emOccurSumCatId, emOccurSumCat.Short, emOccurSumCat.Description, " +  
                	" emOccurSumCat.OutputCode, emArea.AreaId, emArea.EMCode, emArea.ISOCode, emArea.TDWGCode, emArea.Unit, " +  
                	" emArea.Status, emArea.OutputOrder, emArea.eur, emArea.EuroMedArea " + 
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
		MapWrapper<Distribution> distributionMap = new MapWrapper<Distribution>(null);

		Map<String, TaxonBase> taxonMap = (Map<String, TaxonBase>) partitioner.getObjectMap(BerlinModelTaxonImport.NAMESPACE);
			
		BerlinModelImportConfigurator config = state.getConfig();
		ResultSet rs = partitioner.getResultSet();

		try {
			//map to store the mapping of duplicate berlin model occurrences to their real distributions
			Map<Integer, Distribution> duplicateMap = new HashMap<Integer, Distribution>();
			int oldTaxonId = -1;
			TaxonDescription oldDescription = null;
			int i = 0;
			int countDescriptions = 0;
			int countDistributions = 0;
			//for each reference
            while (rs.next()){
                
                if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("Facts handled: " + (i-1));}
                
                int occurrenceId = rs.getInt("OccurrenceId");
                int newTaxonId = rs.getInt("taxonId");
                String tdwgCodeString = rs.getString("TDWGCode");
                Integer emStatusId = (Integer)rs.getObject("emOccurSumCatId");
                
                try {
                    //status
                     PresenceAbsenceTermBase<?> status = null;
                     if (emStatusId != null){
                    	status = BerlinModelTransformer.occStatus2PresenceAbsence(emStatusId);
                     }
                     
                     //Create area list
                     List<NamedArea> tdwgAreas = new ArrayList<NamedArea>();
                     if (tdwgCodeString != null){
                           String[] tdwgCodes = tdwgCodeString.split(";");
                           for (String tdwgCode : tdwgCodes){
                                 NamedArea tdwgArea = TdwgArea.getAreaByTdwgAbbreviation(tdwgCode.trim());
                                 if (tdwgArea != null){
                                       tdwgAreas.add(tdwgArea);
                                 }
                           }
                     }
                     ReferenceBase<?> sourceRef = state.getConfig().getSourceReference();
                     //create description(elements)
                     TaxonDescription taxonDescription = getTaxonDescription(newTaxonId, oldTaxonId, oldDescription, taxonMap, occurrenceId, sourceRef);
                     for (NamedArea tdwgArea : tdwgAreas){
                           Distribution distribution = Distribution.NewInstance(tdwgArea, status);
//                         distribution.setCitation(sourceRef);
                           if (taxonDescription != null) { 
                               if (checkIsNoDuplicate(taxonDescription, distribution, duplicateMap , occurrenceId)){
                                   distributionMap.put(occurrenceId, distribution);
	                        	   taxonDescription.addElement(distribution); 
	                               countDistributions++; 
	                               if (taxonDescription != oldDescription){ 
	                            	   taxaToSave.add(taxonDescription.getTaxon()); 
	                                   oldDescription = taxonDescription; 
	                                   countDescriptions++; 
	                               	} 
                               }else{
                            	   logger.debug("Distribution is duplicate");	                           }
	                       	} else { 
	                       		logger.warn("Distribution " + tdwgArea.toString() + " ignored");
	                       		success = false;
	                       	}
                     }
                     
                } catch (UnknownCdmTypeException e) {
                     logger.error("Unknown presenceAbsence status id: " + emStatusId); 
                	e.printStackTrace();
                     success = false;
                }
                
            }
            //TODO fix: makeOccurrenceSource(distributionMap, state, duplicateMap);
			
            logger.info("Distributions: " + countDistributions + ", Descriptions: " + countDescriptions );
			logger.warn("Unmatched occurrences: "  + (i - countDescriptions));
			logger.info("Taxa to save: " + taxaToSave.size());
			getTaxonService().save(taxaToSave);	
			return success;
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
			Set<String> taxonIdSet = new HashSet<String>();
			Set<String> referenceIdSet = new HashSet<String>();
			while (rs.next()){
				handleForeignKey(rs, taxonIdSet, "taxonId");
//				handleForeignKey(rs, referenceIdSet, "PTDesignationRefFk"); falsch, kommt eigentlich aus source Tabellen
	}
			
			//taxon map
			nameSpace = BerlinModelTaxonImport.NAMESPACE;
			cdmClass = TaxonBase.class;
			idSet = taxonIdSet;
			Map<String, TaxonBase> objectMap = (Map<String, TaxonBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, objectMap);
//
//			//nom reference map
//			nameSpace = BerlinModelReferenceImport.NOM_REFERENCE_NAMESPACE;
//			cdmClass = ReferenceBase.class;
//			idSet = referenceIdSet;
//			Map<String, ReferenceBase> nomReferenceMap = (Map<String, ReferenceBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
//			result.put(nameSpace, nomReferenceMap);
//
//			//biblio reference map
//			nameSpace = BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE;
//			cdmClass = ReferenceBase.class;
//			idSet = referenceIdSet;
//			Map<String, ReferenceBase> biblioReferenceMap = (Map<String, ReferenceBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
//			result.put(nameSpace, biblioReferenceMap);


		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
			
	/**
	 * @param distributionMap
	 * @param state
	 * @throws SQLException 
	 */
	private void makeOccurrenceSource(MapWrapper<Distribution> distributionMap, BerlinModelImportState state, Map<Integer, Distribution> duplicateMap) 
				throws SQLException {
		//FIXME multiple sources for one distribution, needs model change first
		Map<String, ReferenceBase<?>> sourceIdMap = makeSourceIdMap(state); 
		Source source = state.getConfig().getSource();
		String strQuery = " SELECT OccurrenceSourceId, OccurrenceFk, SourceNumber, OldName, OldNameFk, PreferredReferenceFlag " + 
						" FROM emOccurrenceSource " +
						" ORDER BY SourceNumber DESC ";
		
		
		ResultSet rs = source.getResultSet(strQuery) ;
		while (rs.next()){
			int occurrenceSourceId = rs.getInt("OccurrenceSourceId"); //TODO make originalSourceId
			Integer occurrenceFk = (Integer)rs.getObject("OccurrenceFk");
			String sourceNumber = rs.getString("SourceNumber");
			String oldName = rs.getString("OldName");
			int oldNameFk = rs.getInt("OldNameFk");
			Distribution distribution = distributionMap.get(occurrenceFk);
			if (distribution == null){
				distribution = duplicateMap.get(occurrenceFk);
			}
			if (distribution != null){
				ReferenceBase<?> ref = sourceIdMap.get(sourceNumber);
				if (ref != null){
					DescriptionElementSource originalSource = DescriptionElementSource.NewInstance();
					originalSource.setCitation(ref);
					TaxonNameBase<?,?> taxonName = getName(state, oldName, oldNameFk);
					if (taxonName != null){
						originalSource.setNameUsedInSource(taxonName);
					}else if(CdmUtils.isNotEmpty(oldName)){
						originalSource.setOriginalNameString(oldName);
					}
					distribution.addSource(originalSource);
				}else{
					logger.warn("reference for sourceId "+sourceNumber+" could not be found." );
				}
			}else{
				logger.warn("distribution ("+occurrenceFk+") could not be found." );
			}
			
		}
	}

	
	private NonViralNameParserImpl nameParser = NonViralNameParserImpl.NewInstance();
	/**
	 * @param state
	 * @param oldName
	 * @param oldNameFk
	 * @return
	 */
	private TaxonNameBase<?, ?> getName(BerlinModelImportState state, String oldName, int oldNameFk) {
		TaxonNameBase<?,?> taxonName = null;
		MapWrapper<TaxonNameBase<?,?>> taxonNameMap = (MapWrapper<TaxonNameBase<?,?>>)state.getStore(ICdmIO.TAXONNAME_STORE);
		taxonName = taxonNameMap.get(oldNameFk);
		if (taxonName == null && oldName != null){
			List<NonViralName> names = getNameService().getNamesByNameCache(oldName);
			if (names.size() == 1){
				return names.get(0);
			}else {
				if (names.size()> 2){
					logger.info("Name has non unique NameCache: " + oldName + ".");
				}
				return null;
				//taxonName = nameParser.parseSimpleName(oldName);
			}
		}
		return taxonName;
	}

	/**
	 * @param state
	 * @return
     * @throws SQLException 
	 */
	private Map<String, ReferenceBase<?>> makeSourceIdMap(BerlinModelImportState state) throws SQLException {
		MapWrapper<ReferenceBase<?>> referenceMap = (MapWrapper<ReferenceBase<?>>)state.getStore(ICdmIO.REFERENCE_STORE);
		MapWrapper<ReferenceBase<?>> nomRefMap = (MapWrapper<ReferenceBase<?>>)state.getStore(ICdmIO.NOMREF_STORE);
		
		Map<String, ReferenceBase<?>> result = new HashMap<String, ReferenceBase<?>>();
		Source source = state.getConfig().getSource();
		String strQuery = " SELECT RefId, IdInSource " +  
						  " FROM Reference " + 
						  " WHERE     (IdInSource IS NOT NULL) AND (IdInSource NOT LIKE '') ";
		ResultSet rs = source.getResultSet(strQuery) ;
		while (rs.next()){
			int refId = rs.getInt("RefId");
			String idInSource = rs.getString("IdInSource");
			if (idInSource != null){
				String[] singleSources = idInSource.split("\\|");
				for (String singleSource : singleSources){
					singleSource = singleSource.trim();
					ReferenceBase<?> ref = getReference(refId, referenceMap, nomRefMap);
					if (ref == null){
						logger.warn("Reference ("+refId+")not found in refStore.");
					}
					result.put(singleSource, ref);
				}
			}
		}
		return result;
	}

	/**
	 * @param refId
	 * @param referenceMap
	 * @param nomRefMap
	 */
	private ReferenceBase<?> getReference(int refId, MapWrapper<ReferenceBase<?>> referenceMap, MapWrapper<ReferenceBase<?>> nomRefMap) {
		ReferenceBase<?> ref = referenceMap.get(refId);
		if (ref == null){
			ref = nomRefMap.get(refId);
		}
		return ref;
	}

	/**
     * Tests if a distribution with the same tdwgArea and the same status already exists in the description. If so 
     * the duplicate will be registered in the duplicateMap.
     * @param description
     * @param tdwgArea
     * @return false, if dupplicate exists. True otherwise.
     */
    private boolean checkIsNoDuplicate(TaxonDescription description, Distribution distribution, Map<Integer, Distribution> duplicateMap, Integer bmDistributionId){
    	for (DescriptionElementBase descElBase : description.getElements()){
    		if (descElBase.isInstanceOf(Distribution.class)){
    			Distribution oldDistr = HibernateProxyHelper.deproxy(descElBase, Distribution.class);
    			NamedArea oldArea = oldDistr.getArea();
    			if (oldArea != null && oldArea.equals(distribution.getArea())){
    				PresenceAbsenceTermBase<?> oldStatus = oldDistr.getStatus();
    				if (oldStatus != null && oldStatus.equals(distribution.getStatus())){
    					duplicateMap.put(bmDistributionId, oldDistr);
    					return false;
    				}
    			}
    		}
    	}
    	return true;
    }
	
	/**
	 * Use same TaxonDescription if two records belong to the same taxon 
	 * @param newTaxonId
	 * @param oldTaxonId
	 * @param oldDescription
	 * @param taxonMap
	 * @return
	 */
	private TaxonDescription getTaxonDescription(int newTaxonId, int oldTaxonId, TaxonDescription oldDescription, Map<String, TaxonBase> taxonMap, int occurrenceId, ReferenceBase<?> sourceSec){
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
				result.setTitleCache(sourceSec.getTitleCache());
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
