/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.in;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.T_STATUS_ACCEPTED;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.T_STATUS_PARTIAL_SYN;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.T_STATUS_PRO_PARTE_SYN;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.T_STATUS_SYNONYM;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelTaxonImport  extends BerlinModelImportBase  {
	private static final Logger logger = Logger.getLogger(BerlinModelTaxonImport.class);

	public static final UUID DETAIL_EXT_UUID = UUID.fromString("c3959b4f-d876-4b7a-a739-9260f4cafd1c");
	public static final UUID APPENDED_TITLE_PHRASE = UUID.fromString("b121f3b6-89bb-48e1-a010-7d3148d2caba");
	public static final UUID USE_NAME_CACHE = UUID.fromString("1b959a0d-230b-4b03-b7b6-2bd46056a22d");
	
	
	/**
	 * How should the publish flag in table PTaxon be interpreted
	 * NO_MARKER: No marker is set
	 * ONLY_FALSE: 
	 */
	public enum PublishMarkerChooser{
		NO_MARKER,
		ONLY_FALSE,
		ONLY_TRUE,
		ALL;
		
		boolean doMark(boolean value){
			if (value == true){
				return this == ALL || this == ONLY_TRUE;
			}else{
				return this == ALL || this == ONLY_FALSE;
			}
		}
	}
	
	
	private int modCount = 10000;
	
	public BerlinModelTaxonImport(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		boolean result = true;
		BerlinModelImportConfigurator bmiConfig = state.getConfig();
		logger.warn("Checking for Taxa not yet fully implemented");
		result &= checkTaxonStatus(bmiConfig);
		result &= checkInactivated(bmiConfig);
		
		return result;
	}
	
	private boolean checkTaxonStatus(BerlinModelImportConfigurator bmiConfig){
		try {
			boolean result = true;
			Source source = bmiConfig.getSource();
			String strSQL = " SELECT RelPTaxon.RelQualifierFk, RelPTaxon.relPTaxonId, PTaxon.PTNameFk, PTaxon.PTRefFk, PTaxon_1.PTNameFk AS Expr1, PTaxon.RIdentifier, PTaxon_1.RIdentifier AS Expr3, Name.FullNameCache "  +
				" FROM RelPTaxon " + 
					" INNER JOIN PTaxon ON RelPTaxon.PTNameFk1 = PTaxon.PTNameFk AND RelPTaxon.PTRefFk1 = PTaxon.PTRefFk " + 
					" INNER JOIN PTaxon AS PTaxon_1 ON RelPTaxon.PTNameFk2 = PTaxon_1.PTNameFk AND RelPTaxon.PTRefFk2 = PTaxon_1.PTRefFk  " + 
					" INNER JOIN Name ON PTaxon.PTNameFk = Name.NameId " +
				" WHERE (dbo.PTaxon.StatusFk = 1) AND ((RelPTaxon.RelQualifierFk = 7) OR (RelPTaxon.RelQualifierFk = 6) OR (RelPTaxon.RelQualifierFk = 2)) ";
			ResultSet rs = source.getResultSet(strSQL);
			boolean firstRow = true;
			int i = 0;
			while (rs.next()){
				i++;
				if (firstRow){
					System.out.println("========================================================");
					logger.warn("There are taxa that have a 'is synonym of' - relationship but having taxon status 'accepted'!");
					System.out.println("========================================================");
				}
				int rIdentifier = rs.getInt("RIdentifier");
				int nameFk = rs.getInt("PTNameFk");
				int refFk = rs.getInt("PTRefFk");
				int relPTaxonId = rs.getInt("relPTaxonId");
				String taxonName = rs.getString("FullNameCache");
				
				System.out.println("RIdentifier:" + rIdentifier + "\n  name: " + nameFk + 
						"\n  taxonName: " + taxonName + "\n  refId: " + refFk + "\n  RelPTaxonId: " + relPTaxonId );
				result = firstRow = false;
			}
			if (i > 0){
				System.out.println(" ");
			}
			
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean checkInactivated(BerlinModelImportConfigurator bmiConfig){
		try {
			boolean result = true;
			Source source = bmiConfig.getSource();
			String strSQL = " SELECT * "  +
				" FROM PTaxon " +
					" INNER JOIN Name ON PTaxon.PTNameFk = Name.NameId " +
				" WHERE (PTaxon.DoubtfulFlag = 'i') ";
			ResultSet rs = source.getResultSet(strSQL);
			boolean firstRow = true;
			int i = 0;
			while (rs.next()){
				i++;
				if (firstRow){
					System.out.println("========================================================");
					logger.warn("There are taxa that have a doubtful flag 'i'(inactivated). Inactivated is not supported by CDM!");
					System.out.println("========================================================");
				}
				int rIdentifier = rs.getInt("RIdentifier");
				int nameFk = rs.getInt("PTNameFk");
				int refFk = rs.getInt("PTRefFk");
				String taxonName = rs.getString("FullNameCache");
				
				System.out.println("RIdentifier:" + rIdentifier + "\n  nameId: " + nameFk + 
						"\n  taxonName: " + taxonName + "\n  refId: " + refFk  );
				result = firstRow = false;
			}
			if (i > 0){
				System.out.println(" ");
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
		
		//make not needed maps empty
		String teamStore = ICdmIO.TEAM_STORE;
		MapWrapper<? extends CdmBase> store = state.getStore(teamStore);
		MapWrapper<TeamOrPersonBase> teamMap = (MapWrapper<TeamOrPersonBase>)store;
		teamMap.makeEmpty();

		
		MapWrapper<TaxonNameBase<?,?>> taxonNameMap = (MapWrapper<TaxonNameBase<?,?>>)state.getStore(ICdmIO.TAXONNAME_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)state.getStore(ICdmIO.REFERENCE_STORE);
		MapWrapper<ReferenceBase> nomRefMap = (MapWrapper<ReferenceBase>)state.getStore(ICdmIO.NOMREF_STORE);
		MapWrapper<TaxonBase> taxonMap = (MapWrapper<TaxonBase>)state.getStore(ICdmIO.TAXON_STORE);
		
		BerlinModelImportConfigurator config = state.getConfig();
		Source source = config.getSource();
		
		logger.info("start makeTaxa ...");
		
		String namespace = "PTaxon";
		
		try {
			//get data from database
			String strQuery = 
					" SELECT * " + 
					" FROM PTaxon " +
					" WHERE (1=1)";
			
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("PTaxa handled: " + (i-1));}
				
				//create TaxonName element
				int taxonId = rs.getInt("RIdentifier");
				int statusFk = rs.getInt("statusFk");
				
				int nameFk = rs.getInt("PTNameFk");
				int refFk = rs.getInt("PTRefFk");
				String doubtful = rs.getString("DoubtfulFlag");
				String uuid = null;
				if (resultSetHasColumn(rs,"UUID")){
					uuid = rs.getString("UUID");
				}
				
				TaxonNameBase<?,?> taxonName = null;
				if (taxonNameMap != null){
					taxonName  = taxonNameMap.get(nameFk);
				}
								
				ReferenceBase<?> reference = null;
				if (referenceMap != null){
					reference = referenceMap.get(refFk);
					if (reference == null){
						reference = nomRefMap.get(refFk);
					}
				}
				
				if(! config.isIgnoreNull()){
					if (taxonName == null ){
						logger.warn("TaxonName belonging to taxon (RIdentifier = " + taxonId + ") could not be found in store. Taxon will not be transported");
						success = false;
						continue; //next taxon
					}else if (reference == null ){
						logger.warn("Reference belonging to taxon could not be found in store. Taxon will not be imported");
						success = false;
						continue; //next taxon
					}
				}
				TaxonBase<?> taxonBase;
				Synonym synonym;
				Taxon taxon;
				try {
					logger.debug(statusFk);
					if (statusFk == T_STATUS_ACCEPTED){
						taxon = Taxon.NewInstance(taxonName, reference);
						taxonBase = taxon;
					}else if (statusFk == T_STATUS_SYNONYM || statusFk == T_STATUS_PRO_PARTE_SYN || statusFk == T_STATUS_PARTIAL_SYN){
						synonym = Synonym.NewInstance(taxonName, reference);
						taxonBase = synonym;
						if (statusFk == T_STATUS_PRO_PARTE_SYN){
							config.addProParteSynonym(synonym);
						}
						if (statusFk == T_STATUS_PARTIAL_SYN){
							config.addPartialSynonym(synonym);
						}
					}else{
						logger.warn("TaxonStatus " + statusFk + " not yet implemented. Taxon (RIdentifier = " + taxonId + ") left out.");
						success = false;
						continue;
					}
					if (uuid != null){
						taxonBase.setUuid(UUID.fromString(uuid));
					}
					

					if (doubtful.equals("a")){
						taxonBase.setDoubtful(false);
					}else if(doubtful.equals("d")){
						taxonBase.setDoubtful(true);
					}else if(doubtful.equals("i")){
						taxonBase.setDoubtful(false);
						logger.warn("Doubtful = i (inactivated) does not exist in CDM. Doubtful set to false");
					}
					
					//nameId
					ImportHelper.setOriginalSource(taxonBase, config.getSourceReference(), taxonId, namespace);

					//detail
					String detail = rs.getString("Detail");
					if (CdmUtils.isNotEmpty(detail)){
						ExtensionType detailExtensionType = getExtensionType(DETAIL_EXT_UUID, "micro reference","micro reference","micro ref.");
						Extension.NewInstance(taxonBase, detail, detailExtensionType);
					}
					//idInSource
					String idInSource = rs.getString("IdInSource");
					if (CdmUtils.isNotEmpty(idInSource)){
						ExtensionType detailExtensionType = getExtensionType(ID_IN_SOURCE_EXT_UUID, "Berlin Model IdInSource","Berlin Model IdInSource","BM source id");
						Extension.NewInstance(taxonBase, idInSource, detailExtensionType);
					}
					//namePhrase
					String namePhrase = rs.getString("NamePhrase");
					if (CdmUtils.isNotEmpty(namePhrase)){
						taxonBase.setAppendedPhrase(namePhrase);
					}
					//useNameCache
					Boolean useNameCacheFlag = rs.getBoolean("UseNameCacheFlag");
					if (useNameCacheFlag){
						taxonBase.setUseNameCache(true);
					}
					//publisheFlag
					Boolean publishFlag = rs.getBoolean("PublishFlag");
					if (config.getTaxonPublishMarker().doMark(publishFlag)){
						taxonBase.addMarker(Marker.NewInstance(MarkerType.PUBLISH(), publishFlag));
					}
					//Notes
					doIdCreatedUpdatedNotes(state, taxonBase, rs, taxonId, namespace);
					
					taxonMap.put(taxonId, taxonBase);
				} catch (Exception e) {
					logger.warn("An exception occurred when creating taxon with id " + taxonId + ". Taxon could not be saved.");
					success = false;
				}
			}
			//invokeRelations(source, cdmApp, deleteAll, taxonMap, referenceMap);
			logger.info("saving "+i+" taxa ...");
			getTaxonService().saveTaxonAll(taxonMap.objects());
			
			logger.info("end makeTaxa ..." + getSuccessString(success));
			
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}

	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelImportState state){
		return ! state.getConfig().isDoTaxa();
	}

}
