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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.TdwgArea;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelOccurrenceIO  extends BerlinModelIOBase {
	private static final Logger logger = Logger.getLogger(BerlinModelOccurrenceIO.class);

	private static int modCount = 10000;

	public BerlinModelOccurrenceIO(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config){
		boolean result = true;
		BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
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
	protected boolean doInvoke(IImportConfigurator config, 
			Map<String, MapWrapper<? extends CdmBase>> stores){
		
		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)stores.get(ICdmIO.TAXON_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
		
		Set<TaxonBase> taxonStore = new HashSet<TaxonBase>();
		Source source = bmiConfig.getSource();
		
		logger.info("start make occurrences ...");
		
		try {
			//get data from database
			String strQuery = 
					" SELECT PTaxon.RIdentifier, emOccurrence.OccurrenceId, emOccurSumCat.emOccurSumCatId, emOccurSumCat.Short, emOccurSumCat.Description, " + 
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
                     
                     //create description(elements)
                     TaxonDescription taxonDescription = getTaxonDescription(newTaxonId, oldTaxonId, oldDescription, taxonMap, occurrenceId);
                     for (NamedArea tdwgArea : tdwgAreas){
                           Distribution distribution = Distribution.NewInstance(tdwgArea, status);
                           if (taxonDescription != null) { 
                               taxonDescription.addElement(distribution); 
                               countDistributions++; 
                               if (taxonDescription != oldDescription){ 
                                       taxonStore.add(taxonDescription.getTaxon()); 
                                       oldDescription = taxonDescription; 
                                       countDescriptions++; 
                               } 
	                       } else { 
	                               logger.warn("Distribution " + tdwgArea.toString() + " ignored"); 
	                     }
                     }
                } catch (UnknownCdmTypeException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                }

//              TODO
//              sources
//              references
            }

			logger.info("Distributions: " + countDistributions + ", Descriptions: " + countDescriptions );
			logger.warn("Unmatched occurrences: "  + (i - countDescriptions));
			logger.info("Taxa to save: " + taxonStore.size());
			getTaxonService().saveTaxonAll(taxonStore);	
			
			logger.info("end make occurrences ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}
	
	
	/**
	 * Use same TaxonDescription if two records belong to the same taxon 
	 * @param newTaxonId
	 * @param oldTaxonId
	 * @param oldDescription
	 * @param taxonMap
	 * @return
	 */
	private TaxonDescription getTaxonDescription(int newTaxonId, int oldTaxonId, TaxonDescription oldDescription, MapWrapper<TaxonBase> taxonMap, int occurrenceId){
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
			
			result = TaxonDescription.NewInstance();
			taxon.addDescription(result);
			//TODO add source title as title (or reference at least)
			//result.setTitleCache("");
		}else{
			result = oldDescription;
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoOccurrence();
	}
	
}
