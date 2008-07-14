package eu.etaxonomy.cdm.io.tcs;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

import eu.etaxonomy.cdm.api.application.CdmApplicationController;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.common.XmlHelp;
import eu.etaxonomy.cdm.io.common.CdmIoBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.model.agent.INomenclaturalAuthor;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.name.NomenclaturalCode;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatusType;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;


public class TcsTaxonNameRelationsIO extends CdmIoBase implements ICdmIO {
	private static final Logger logger = Logger.getLogger(TcsTaxonNameRelationsIO.class);

	private static int modCount = 5000;
	private static final String ioNameLocal = "TcsTaxonNameRelationsIO";
	
	public TcsTaxonNameRelationsIO(boolean ignore){
		super(ioNameLocal, ignore);
	}
	
	@Override
	public boolean doCheck(IImportConfigurator config){
		boolean result = true;
		logger.warn("Checking for TaxonNameRelations not yet implemented");
		//result &= checkArticlesWithoutJournal(tcsConfig);
		//result &= checkPartOfJournal(tcsConfig);
		
		return result;
	}
	
	@Override
	public boolean doInvoke(IImportConfigurator config, CdmApplicationController cdmApp, Map<String, MapWrapper<? extends CdmBase>> stores){
		
		MapWrapper<TaxonNameBase> taxonNameMap = (MapWrapper<TaxonNameBase>)stores.get(ICdmIO.TAXONNAME_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)stores.get(ICdmIO.REFERENCE_STORE);
		
		Set<TaxonNameBase> nameStore = new HashSet<TaxonNameBase>();
		TcsImportConfigurator tcsConfig = (TcsImportConfigurator)config;
		Element source = tcsConfig.getSourceRoot();
		String dbAttrName;
		String cdmAttrName;
		
		logger.info("start makeNameRelationships ...");
		logger.warn("start makeNameRelationships not yet implemented !!!");
		INameService nameService = cdmApp.getNameService();

//		try {
//			//get data from database
//			String strQuery = 
//					" SELECT RelName.*, FromName.nameId as name1Id, ToName.nameId as name2Id, RefDetail.Details " + 
//					" FROM Name as FromName INNER JOIN " +
//                      	" RelName ON FromName.NameId = RelName.NameFk1 INNER JOIN " +
//                      	" Name AS ToName ON RelName.NameFk2 = ToName.NameId LEFT OUTER JOIN "+
//                      	" RefDetail ON RelName.RefDetailFK = RefDetail.RefDetailId " + 
//                    " WHERE (1=1)";
//			ResultSet rs = source.getResultSet(strQuery) ;
//			
//			int i = 0;
//			//for each reference
//			while (rs.next()){
//				
//				if ((i++ % modCount) == 0){ logger.info("RelName handled: " + (i-1));}
//				
//				int relNameId = rs.getInt("RelNameId");
//				int name1Id = rs.getInt("name1Id");
//				int name2Id = rs.getInt("name2Id");
//				int relRefFk = rs.getInt("refFk");
//				String details = rs.getString("details");
//				int relQualifierFk = rs.getInt("relNameQualifierFk");
//				
//				TaxonNameBase nameFrom = nameMap.get(name1Id);
//				TaxonNameBase nameTo = nameMap.get(name2Id);
//				
//				ReferenceBase citation = referenceMap.get(relRefFk);
//				//TODO (preliminaryFlag = true testen
//				String microcitation = details;
//
//				if (nameFrom != null && nameTo != null){
//					if (relQualifierFk == NAME_REL_IS_BASIONYM_FOR){
//						//TODO references, mikroref, etc
//						nameTo.setBasionym(nameFrom);
//					}else if (relQualifierFk == NAME_REL_IS_LATER_HOMONYM_OF){
//						String rule = null;  //TODO
//						nameFrom.addRelationshipToName(nameTo, NameRelationshipType.LATER_HOMONYM(), rule) ;
//						//TODO reference
//					}else if (relQualifierFk == NAME_REL_IS_REPLACED_SYNONYM_FOR){
//						String rule = null;  //TODO
//						nameFrom.addRelationshipToName(nameTo, NameRelationshipType.REPLACED_SYNONYM(), rule) ;
//						//TODO reference
//					}else if (relQualifierFk == NAME_REL_IS_TYPE_OF || relQualifierFk == NAME_REL_IS_REJECTED_TYPE_OF ||  relQualifierFk == NAME_REL_IS_CONSERVED_TYPE_OF ){
//						//TODO
//						String originalNameString = null;
//						boolean isRejectedType = (relQualifierFk == NAME_REL_IS_REJECTED_TYPE_OF);
//						boolean isConservedType = (relQualifierFk == NAME_REL_IS_CONSERVED_TYPE_OF);
//						//TODO nameTo.addNameTypeDesignation(nameFrom, citation, microcitation, originalNameString, isRejectedType, isConservedType);
//					}else if (relQualifierFk == NAME_REL_IS_ORTHOGRAPHIC_VARIANT_OF){
//						String rule = null;  //TODO
//						nameFrom.addRelationshipToName(nameTo, NameRelationshipType.ORTHOGRAPHIC_VARIANT(), rule) ;
//						//TODO reference
//					}else {
//						//TODO
//						logger.warn("NameRelationShipType " + relQualifierFk + " not yet implemented");
//					}
//					nameStore.add(nameFrom);
//					
//					//TODO
//					//ID
//					//etc.
//				}else{
//					//TODO
//					if (nameFrom == null){
//						 logger.warn("from TaxonName for RelName (" + relNameId + ") does not exist in store");
//					}
//					if (nameTo == null){
//						logger.warn("to TaxonNames for RelName (" + relNameId + ") does not exist in store");
//					}
//				}
//			}
//			logger.info("TaxonName to save: " + nameStore.size());
//			nameService.saveTaxonNameAll(nameStore);
//			
//			logger.info("end makeRelName ...");
//			return true;
//		} catch (SQLException e) {
//			logger.error("SQLException:" +  e);
//			return false;
//		}
		return true;
	}
}
