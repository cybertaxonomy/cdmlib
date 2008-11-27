/**
 * 
 */
package eu.etaxonomy.cdm.io.berlinModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 *
 */
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
		logger.warn("Checking for Occurrence not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
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
		
		if (true){
			return false;
		}
		
		Set<TaxonBase> taxonStore = new HashSet<TaxonBase>();
		Source source = bmiConfig.getSource();
		ITaxonService taxonService = config.getCdmAppController().getTaxonService();
		
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeFacts ...");
		
		boolean delete = bmiConfig.isDeleteAll();

		try {
			//get data from database
			String strQuery = 
					" SELECT xxx.*, PTaxon.RIdentifier as taxonId " + 
					" FROM Fact INNER JOIN " +
                      	" dbo.PTaxon ON Fact.PTNameFk = PTaxon.PTNameFk AND Fact.PTRefFk = PTaxon.PTRefFk "+
                    " WHERE (1=1)";
			ResultSet rs = source.getResultSet(strQuery) ;

			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("Facts handled: " + (i-1));}
				
				int occurrenceId = rs.getInt("occId");
				int taxonId = rs.getInt("taxonId");
				int factRefFk = rs.getInt("factRefFk");
				int categoryFk = rs.getInt("factCategoryFk");
				
				TaxonBase taxonBase = taxonMap.get(taxonId);
				
				if (taxonBase != null){
					Taxon taxon;
					if ( taxonBase instanceof Taxon ) {
						taxon = (Taxon) taxonBase;
					}else{
						logger.warn("TaxonBase for Occurrence " + occurrenceId + " was not of type Taxon but: " + taxonBase.getClass().getSimpleName());
						continue;
					}
					
					TaxonDescription taxonDescription = TaxonDescription.NewInstance();
					
					taxon.addDescription(taxonDescription);
					
					//commonNames
					String commonNameString;
					if (taxon.getName() != null){
						commonNameString = "Common " + taxon.getName().getTitleCache(); 
					}else{
						commonNameString = "Common (null)";
					}
					Language language = bmiConfig.getFactLanguage();
					language = null;
					CommonTaxonName commonName = CommonTaxonName.NewInstance(commonNameString, language);
					taxonDescription.addElement(commonName);
					
					
					//TODO
					//etc.
					
					taxonStore.add(taxon);
				}else{
					//TODO
					logger.warn("Taxon for Fact " + occurrenceId + " does not exist in store");
				}
				//put
			}
			logger.info("Taxa to save: " + taxonStore.size());
			taxonService.saveTaxonAll(taxonStore);	
			
			logger.info("end makeFacts ...");
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
