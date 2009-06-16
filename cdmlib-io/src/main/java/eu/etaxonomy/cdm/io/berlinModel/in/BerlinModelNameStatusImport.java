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
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelNameStatusImport extends BerlinModelImportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelNameStatusImport.class);

	private int modCount = 5000;
	
	public BerlinModelNameStatusImport(){
		super();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator bmiConfig){
		boolean result = true;
		logger.warn("Checking for NomenclaturalStatus not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	@Override
	protected boolean doInvoke(BerlinModelImportState state){
		boolean success = true;	
		String dbAttrName;
		String cdmAttrName;
		
		MapWrapper<TaxonNameBase> taxonNameMap = (MapWrapper<TaxonNameBase>)state.getStore(ICdmIO.TAXONNAME_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)state.getStore(ICdmIO.REFERENCE_STORE);
				
		Set<TaxonNameBase> nameStore = new HashSet<TaxonNameBase>();
		BerlinModelImportConfigurator config = state.getConfig();
		Source source = config.getSource();

		logger.info("start makeNameStatus ...");
		
		try {
			//get data from database
			String strQuery = 
					" SELECT NomStatusRel.*, NomStatus.NomStatus, RefDetail.Details " + 
					" FROM NomStatusRel INNER JOIN " +
                      	" NomStatus ON NomStatusRel.NomStatusFk = NomStatus.NomStatusId " +
                      	" LEFT OUTER JOIN RefDetail ON NomStatusRel.NomStatusRefDetailFk = RefDetail.RefDetailId AND " + 
                      	" NomStatusRel.NomStatusRefFk = RefDetail.RefFk " +
                    " WHERE (1=1)";
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("NomStatus handled: " + (i-1));}
				
				int nomStatusRelId;
				try {
					nomStatusRelId = rs.getInt("RIdentifier");
				} catch (Exception e) {  //RIdentifier does not exist in BM database
					nomStatusRelId = -1;
				}
				int nomStatusFk = rs.getInt("NomStatusFk");
				int nameId = rs.getInt("nameFk");
				
				boolean doubtful = rs.getBoolean("DoubtfulFlag");
//				ReferenceBase citation = rs.getString("");
//				String microcitation = null;
				
				TaxonNameBase taxonName = taxonNameMap.get(nameId);
				//TODO doubtful
				
				if (taxonName != null ){
					try{
						NomenclaturalStatus nomStatus = BerlinModelTransformer.nomStatusFkToNomStatus(nomStatusFk);
						
						//reference
						makeReference(config, nomStatus, nameId, rs, state.getStores());
						
						//Details
						dbAttrName = "details";
						cdmAttrName = "citationMicroReference";
						success &= ImportHelper.addStringValue(rs, nomStatus, dbAttrName, cdmAttrName);
						
						//doubtful
						if (doubtful){
							nomStatus.addMarker(Marker.NewInstance(MarkerType.IS_DOUBTFUL(), true));
						}
						taxonName.addStatus(nomStatus);
						nameStore.add(taxonName);
					}catch (UnknownCdmTypeException e) {
						//TODO
						logger.warn("NomStatusType " + nomStatusFk + " not yet implemented");
						success = false;
					}
					//TODO
					//ID
					//etc.
				}else{
					logger.warn("TaxonName for NomStatus (" + nomStatusRelId + ") does not exist in store");
					success = false;
				}
			}
			logger.info("TaxonNames to save: " + nameStore.size());
			getNameService().saveTaxonNameAll(nameStore);
			
			logger.info("end makeNameStatus ..." + getSuccessString(success));
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}
	
	private boolean makeReference(IImportConfigurator config, NomenclaturalStatus nomStatus, 
			int nameId, ResultSet rs, Map<String, MapWrapper<? extends CdmBase>> stores) 
			throws SQLException{
		
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		MapWrapper<ReferenceBase> nomRefMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.NOMREF_STORE);
		MapWrapper<ReferenceBase> refDetailMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REF_DETAIL_STORE);
		MapWrapper<ReferenceBase> nomRefDetailMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.NOMREF_DETAIL_STORE);
		
		Object nomRefFk = rs.getObject("NomStatusRefFk");
		int nomRefDetailFk = rs.getInt("NomStatusRefDetailFk");
		//TODO
//		boolean refDetailPrelim = rs.getBoolean("RefDetailPrelim");
		
		boolean success = true;
		//nomenclatural Reference
		if (referenceMap != null){
			if (nomRefFk != null){
				int nomRefFkInt = (Integer)nomRefFk;
				
				//get ref
				ReferenceBase ref = nomRefDetailMap.get(nomRefDetailFk);
				if (ref == null){
					ref = refDetailMap.get(nomRefDetailFk);
				}	
				if (ref == null){
					ref = referenceMap.get(nomRefFkInt);
				}
				if (ref == null){
					ref = nomRefMap.get(nomRefFkInt);
				}									
				
				//setRef
				if (ref == null ){
					//TODO
					if (! config.isIgnoreNull()){logger.warn("Reference (refFk = " + nomRefFkInt + ") for NomStatus of TaxonName (nameId = " + nameId + ")"+
						" was not found in reference store. Nomenclatural reference was not set!!");}
				}else{
					nomStatus.setCitation(ref);
				}
			}
		}
		return success;
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoNameStatus();
	}

}
