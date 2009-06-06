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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
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
public class BerlinModelCommonNamesImport  extends BerlinModelImportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelCommonNamesImport.class);

	private static int modCount = 10000;

	public BerlinModelCommonNamesImport(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config){
		boolean result = true;
		logger.warn("Checking for Occurrence not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(BerlinModelImportState<BerlinModelImportConfigurator> state){
		
		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)state.getStore(ICdmIO.TAXON_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)state.getStore(ICdmIO.REFERENCE_STORE);
		BerlinModelImportConfigurator config = state.getConfig();
		
		if (true){
			return false;
		}
		
		Set<TaxonBase> taxonStore = new HashSet<TaxonBase>();
		Source source = config.getSource();
		
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start make occurrences ...");
		
		boolean delete = config.isDeleteAll();

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
                    " WHERE (1=1)";
			ResultSet rs = source.getResultSet(strQuery) ;

			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("Facts handled: " + (i-1));}
				
				int occurrenceId = rs.getInt("OccurrenceId");
				int taxonId = rs.getInt("RIdentifier");
				String tdwgCode = rs.getString("TDWGCode");
				Integer emStatusId = (Integer)rs.getObject("emOccurSumCatId");
				
					
				TaxonBase taxonBase = taxonMap.get(taxonId);
				if (taxonBase != null){
					try {
					
						PresenceAbsenceTermBase<?> status = null;
						if (emStatusId != null){
								status = BerlinModelTransformer.occStatus2PresenceAbsence(emStatusId);
						}
						
						NamedArea tdwgArea = TdwgArea.getAreaByTdwgLabel(tdwgCode);
						
							Taxon taxon;
							if ( taxonBase instanceof Taxon ) {
								taxon = (Taxon) taxonBase;
							}else{
								logger.warn("TaxonBase for Occurrence " + occurrenceId + " was not of type Taxon but: " + taxonBase.getClass().getSimpleName());
								continue;
							}
							
							if (tdwgArea != null){
								Distribution distribution = Distribution.NewInstance(tdwgArea, status);
								//TODO only one description per taxon (for all EM distributions)
								TaxonDescription taxonDescription = TaxonDescription.NewInstance();
								
								taxon.addDescription(taxonDescription);
								taxonStore.add(taxon);
							}
							
						} catch (UnknownCdmTypeException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

//						TODO
//						sources
//						references
				
					
						
//					//commonNames -> TODO move to separate IO
//					String commonNameString;
//					if (taxon.getName() != null){
//						commonNameString = "Common " + taxon.getName().getTitleCache(); 
//					}else{
//						commonNameString = "Common (null)";
//					}
//					Language language = bmiConfig.getFactLanguage();
//					language = null;
//					CommonTaxonName commonName = CommonTaxonName.NewInstance(commonNameString, language);
//					taxonDescription.addElement(commonName);
					
					
					
				}else{
					//TODO
					logger.warn("Taxon for Fact " + occurrenceId + " does not exist in store");
				}
				//put
			}
			logger.info("Taxa to save: " + taxonStore.size());
			getTaxonService().saveTaxonAll(taxonStore);	
			
			logger.info("end make occurrences ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoOccurrence();
	}
	
}
