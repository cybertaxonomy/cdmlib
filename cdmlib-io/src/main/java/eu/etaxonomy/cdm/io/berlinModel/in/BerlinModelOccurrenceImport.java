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
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
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

	public BerlinModelOccurrenceImport(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		boolean result = true;
		BerlinModelImportConfigurator bmiConfig = state.getConfig();
		result &= checkTaxonIsAccepted(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		logger.warn("Checking for Occurrence not yet fully implemented");
		return result;
	}
	
//******************************** CHECK *************************************************
	
	private static boolean checkTaxonIsAccepted(BerlinModelImportConfigurator bmiConfig){
		try {
			boolean result = true;
			Source source = bmiConfig.getSource();
			String strQuery = "SELECT emOccurrence.OccurrenceId, PTaxon.StatusFk, Name.FullNameCache, Status.Status " + 
						" FROM emOccurrence INNER JOIN " +
							" PTaxon ON emOccurrence.PTNameFk = PTaxon.PTNameFk AND emOccurrence.PTRefFk = PTaxon.PTRefFk INNER JOIN " + 
			                " Name ON PTaxon.PTNameFk = Name.NameId INNER JOIN " +
			                " Status ON PTaxon.StatusFk = Status.StatusId " + 
						" WHERE (PTaxon.StatusFk <> 1)  ";
			
			ResultSet resulSet = source.getResultSet(strQuery);
			boolean firstRow = true;
			while (resulSet.next()){
				if (firstRow){
					System.out.println("========================================================");
					logger.warn("There are Occurrences for a taxon that is not accepted!");
					System.out.println("========================================================");
				}
				int occurrenceId = resulSet.getInt("OccurrenceId");
				int statusFk = resulSet.getInt("StatusFk");
				String status = resulSet.getString("Status");
				String fullNameCache = resulSet.getString("FullNameCache");
				
				System.out.println("OccurrenceId:" + occurrenceId + "\n  Status: " + status + 
						"\n  FullNameCache: " + fullNameCache );
				result = firstRow = false;
			}
			
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	

	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(BerlinModelImportState state){
		boolean success = true;
		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)state.getStore(ICdmIO.TAXON_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)state.getStore(ICdmIO.REFERENCE_STORE);
		MapWrapper<Distribution> distributionMap = new MapWrapper<Distribution>(null);
		BerlinModelImportConfigurator config = state.getConfig();
		Source source = config.getSource();
		
		Set<TaxonBase> taxonStore = new HashSet<TaxonBase>();
		
		logger.info("start make occurrences ...");
		
		try {
			//get data from database
			String strQuery =   //DISTINCT because otherwise emOccurrenceSource creates multiple records for a single distribution 
                " SELECT DISTINCT PTaxon.RIdentifier, emOccurrence.OccurrenceId, emOccurSumCat.emOccurSumCatId, emOccurSumCat.Short, emOccurSumCat.Description, " +  
                	" emOccurSumCat.OutputCode, emArea.AreaId, emArea.EMCode, emArea.ISOCode, emArea.TDWGCode, emArea.Unit, " +  
                	" emArea.Status, emArea.OutputOrder, emArea.eur, emArea.EuroMedArea " + 
                " FROM emOccurrence INNER JOIN " +  
                	" emArea ON emOccurrence.AreaFk = emArea.AreaId INNER JOIN " + 
                	" PTaxon ON emOccurrence.PTNameFk = PTaxon.PTNameFk AND emOccurrence.PTRefFk = PTaxon.PTRefFk LEFT OUTER JOIN " + 
                	" emOccurSumCat ON emOccurrence.SummaryStatus = emOccurSumCat.emOccurSumCatId LEFT OUTER JOIN " +  
                	" emOccurrenceSource ON emOccurrence.OccurrenceId = emOccurrenceSource.OccurrenceFk " +  
                " WHERE (1=1)" +  
                " ORDER BY PTaxon.RIdentifier";
			ResultSet rs = source.getResultSet(strQuery) ;
			
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
                int newTaxonId = rs.getInt("RIdentifier");
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
	                            	   taxonStore.add(taxonDescription.getTaxon()); 
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
			logger.info("Taxa to save: " + taxonStore.size());
			getTaxonService().save(taxonStore);	
			
			logger.info("end make occurrences ..." + getSuccessString(success));
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

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
	private Map<String, ReferenceBase<?>> makeSourceIdMap(
			BerlinModelImportState state) throws SQLException {
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
	private TaxonDescription getTaxonDescription(int newTaxonId, int oldTaxonId, TaxonDescription oldDescription, MapWrapper<TaxonBase> taxonMap, int occurrenceId, ReferenceBase<?> sourceSec){
		TaxonDescription result = null;
		if (oldDescription == null || newTaxonId != oldTaxonId){
			TaxonBase taxonBase = taxonMap.get(newTaxonId);
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
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelImportState state){
		return ! state.getConfig().isDoOccurrence();
	}
	
}
