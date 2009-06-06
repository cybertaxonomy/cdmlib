/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.in;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_COMB_INVAL;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_ALTERN;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_AMBIG;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_CONFUS;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_CONS;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_CONS_PROP;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_DUB;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_ILLEG;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_INVAL;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_NOV;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_NUD;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_PROVIS;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_REJ;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_REJ_PROP;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_SUPERFL;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_UTIQUE_REJ;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_NOM_UTIQUE_REJ_PROP;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_ORTH_CONS;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_ST_ORTH_CONS_PROP;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

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
	protected boolean doInvoke(BerlinModelImportState<BerlinModelImportConfigurator> state){
			
		MapWrapper<TaxonNameBase> taxonNameMap = (MapWrapper<TaxonNameBase>)state.getStore(ICdmIO.TAXONNAME_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)state.getStore(ICdmIO.REFERENCE_STORE);
				
		Set<TaxonNameBase> nameStore = new HashSet<TaxonNameBase>();
		BerlinModelImportConfigurator config = state.getConfig();
		Source source = config.getSource();
		
		logger.info("start makeNameStatus ...");
		
		try {
			//get data from database
			String strQuery = 
					" SELECT NomStatusRel.*, NomStatus.NomStatus " + 
					" FROM NomStatusRel INNER JOIN " +
                      	" NomStatus ON NomStatusRel.NomStatusFk = NomStatus.NomStatusId " +
                    " WHERE (1=1)";
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("NomStatus handled: " + (i-1));}
				
				int nomStatusRelId;
				try {
					nomStatusRelId = rs.getInt("RIdentifier");
				} catch (Exception e) {
					nomStatusRelId = -1;
				}
				int nomStatusFk = rs.getInt("NomStatusFk");
				int nameId = rs.getInt("nameFk");
				int refFk = rs.getInt("nomStatusRefFk");
				int detailFk = rs.getInt("nomStatusRefDetailFk");
				
				TaxonNameBase taxonName = taxonNameMap.get(nameId);
				
				//TODO
				ReferenceBase citation = null;
				String microcitation = null;
				//TODO doubtful
				
				if (taxonName != null ){
					if (nomStatusFk == NAME_ST_NOM_INVAL){
						//TODO references, mikroref, etc �berall
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.INVALID()));
					}else if (nomStatusFk == NAME_ST_NOM_ILLEG){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ILLEGITIMATE()));
					}else if (nomStatusFk == NAME_ST_NOM_NUD){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.NUDUM()));
					}else if (nomStatusFk == NAME_ST_NOM_REJ){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.REJECTED()));
					}else if (nomStatusFk == NAME_ST_NOM_REJ_PROP){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.REJECTED_PROP()));
					}else if (nomStatusFk == NAME_ST_NOM_UTIQUE_REJ){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.UTIQUE_REJECTED()));
					}else if (nomStatusFk == NAME_ST_NOM_UTIQUE_REJ_PROP){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.UTIQUE_REJECTED_PROP()));
					}else if (nomStatusFk == NAME_ST_NOM_CONS){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.CONSERVED()));
					}else if (nomStatusFk == NAME_ST_NOM_CONS_PROP){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.CONSERVED_PROP()));
					}else if (nomStatusFk == NAME_ST_ORTH_CONS){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED()));
					}else if (nomStatusFk == NAME_ST_ORTH_CONS_PROP){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ORTHOGRAPHY_CONSERVED_PROP()));
					}else if (nomStatusFk == NAME_ST_NOM_SUPERFL){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.SUPERFLUOUS()));
					}else if (nomStatusFk == NAME_ST_NOM_AMBIG){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.AMBIGUOUS()));
					}else if (nomStatusFk == NAME_ST_NOM_PROVIS){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.PROVISIONAL()));
					}else if (nomStatusFk == NAME_ST_NOM_DUB){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.DOUBTFUL()));
					}else if (nomStatusFk == NAME_ST_NOM_NOV){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.NOVUM()));
					}else if (nomStatusFk == NAME_ST_NOM_CONFUS){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.CONFUSUM()));
					}else if (nomStatusFk == NAME_ST_NOM_ALTERN){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.ALTERNATIVE()));
					}else if (nomStatusFk == NAME_ST_COMB_INVAL){
						taxonName.addStatus(NomenclaturalStatus.NewInstance(NomenclaturalStatusType.COMBINATION_INVALID()));
					}else {
						//TODO
						logger.warn("NomStatusType " + nomStatusFk + " not yet implemented");
					}
					nameStore.add(taxonName);
					//TODO
					//Reference
					//ID
					//etc.
				}else{
					logger.warn("TaxonName for NomStatus (" + nomStatusRelId + ") does not exist in store");
				}
			}
			logger.info("TaxonNames to save: " + nameStore.size());
			getNameService().saveTaxonNameAll(nameStore);
			
			logger.info("end makeNameStatus ...");
			return true;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

	}
	
	private static TeamOrPersonBase getAuthorTeam(MapWrapper<TeamOrPersonBase> authorMap, Object teamIdObject, int nameId){
		if (teamIdObject == null){
			return null;
		}else {
			int teamId = (Integer)teamIdObject;
			TeamOrPersonBase author = authorMap.get(teamId);
			if (author == null){
				//TODO
				logger.warn("AuthorTeam (teamId = " + teamId + ") for TaxonName (nameId = " + nameId + ")"+
				" was not found in authorTeam store. Relation was not set!!");
				return null;
			}else{
				return author;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoNameStatus();
	}

}
