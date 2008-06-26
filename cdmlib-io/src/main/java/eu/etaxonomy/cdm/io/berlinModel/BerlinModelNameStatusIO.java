package eu.etaxonomy.cdm.io.berlinModel;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_BASIONYM_FOR;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_CONSERVED_TYPE_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_LATER_HOMONYM_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_ORTHOGRAPHIC_VARIANT_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_REJECTED_TYPE_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_REPLACED_SYNONYM_FOR;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_TYPE_OF;
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
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


public class BerlinModelNameStatusIO extends BerlinModelIOBase {
	private static final Logger logger = Logger.getLogger(BerlinModelNameStatusIO.class);

	private static int modCount = 5000;

	public static boolean check(BerlinModelImportConfigurator bmiConfig){
		boolean result = true;
		logger.warn("Checking for NomenclaturalStatus not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	//TODO
	public static boolean invoke(BerlinModelImportConfigurator bmiConfig, CdmApplicationController cdmApp, 
			MapWrapper<TaxonNameBase> taxonNameMap,	MapWrapper<ReferenceBase> referenceMap){
		
		Set<TaxonNameBase> nameStore = new HashSet<TaxonNameBase>();
		Source source = bmiConfig.getSource();
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeNameStatus ...");
		
		INameService nameService = cdmApp.getNameService();
		
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
				
				if ((i++ % modCount) == 0){ logger.info("NomStatus handled: " + (i-1));}
				
				int nomStatusRelId = rs.getInt("RIdentifier");
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
			nameService.saveTaxonNameAll(nameStore);
			
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
	
}
