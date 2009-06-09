/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.in;

import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_HAS_SAME_TYPE_AS;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_BASIONYM_FOR;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_CONSERVED_TYPE_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_FEMALE_PARENT_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_FIRST_PARENT_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_LATER_HOMONYM_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_LECTOTYPE_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_MALE_PARENT_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_ORTHOGRAPHIC_VARIANT_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_REJECTED_TYPE_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_REPLACED_SYNONYM_FOR;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_SECOND_PARENT_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_TYPE_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_TYPE_NOT_DESIGNATED;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelTaxonNameRelationImport extends BerlinModelImportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelTaxonNameRelationImport.class);

	private static int modCount = 5000;
	
	public BerlinModelTaxonNameRelationImport(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config){
		boolean result = true;
		logger.warn("Checking for TaxonNameRelations not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(BerlinModelImportState state){				
			
		MapWrapper<TaxonNameBase> taxonNameMap = (MapWrapper<TaxonNameBase>)state.getStore(ICdmIO.TAXONNAME_STORE);
		MapWrapper<ReferenceBase> referenceMap = (MapWrapper<ReferenceBase>)state.getStore(ICdmIO.REFERENCE_STORE);
		
		Set<TaxonNameBase> nameStore = new HashSet<TaxonNameBase>();
		BerlinModelImportConfigurator config = state.getConfig();
		Source source = config.getSource();
		
		logger.info("start makeNameRelationships ...");
		
		try {
			//get data from database
			String strQuery = 
					" SELECT RelName.*, FromName.nameId as name1Id, ToName.nameId as name2Id, RefDetail.Details " + 
					" FROM Name as FromName INNER JOIN " +
                      	" RelName ON FromName.NameId = RelName.NameFk1 INNER JOIN " +
                      	" Name AS ToName ON RelName.NameFk2 = ToName.NameId LEFT OUTER JOIN "+
                      	" RefDetail ON RelName.RefDetailFK = RefDetail.RefDetailId " + 
                    " WHERE (1=1)";
			ResultSet rs = source.getResultSet(strQuery) ;
			
			int i = 0;
			//for each reference
			while (rs.next()){
				
				if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("RelName handled: " + (i-1));}
				
				int relNameId = rs.getInt("RelNameId");
				int name1Id = rs.getInt("name1Id");
				int name2Id = rs.getInt("name2Id");
				int relRefFk = rs.getInt("refFk");
				String details = rs.getString("details");
				int relQualifierFk = rs.getInt("relNameQualifierFk");
				
				TaxonNameBase nameFrom = taxonNameMap.get(name1Id);
				TaxonNameBase nameTo = taxonNameMap.get(name2Id);
				
				ReferenceBase citation = referenceMap.get(relRefFk);
				//TODO (preliminaryFlag = true testen
				String microcitation = details;
				String rule = null;  //TODO
				
				if (nameFrom != null && nameTo != null){
					if (relQualifierFk == NAME_REL_IS_BASIONYM_FOR){
						nameTo.addBasionym(nameFrom, citation, microcitation, rule);
					}else if (relQualifierFk == NAME_REL_IS_LATER_HOMONYM_OF){
						nameFrom.addRelationshipToName(nameTo, NameRelationshipType.LATER_HOMONYM(), citation, microcitation, rule) ;
					}else if (relQualifierFk == NAME_REL_IS_REPLACED_SYNONYM_FOR){
						nameFrom.addRelationshipToName(nameTo, NameRelationshipType.REPLACED_SYNONYM(), citation, microcitation, rule) ;
					}else if (relQualifierFk == NAME_REL_HAS_SAME_TYPE_AS){
						nameTo.getHomotypicalGroup().merge(nameFrom.getHomotypicalGroup());//nameFrom.addRelationshipToName(nameTo, NameRelationshipType.REPLACED_SYNONYM(), rule) ;
					}else if (relQualifierFk == NAME_REL_IS_TYPE_OF || relQualifierFk == NAME_REL_IS_REJECTED_TYPE_OF ||  relQualifierFk == NAME_REL_IS_CONSERVED_TYPE_OF || relQualifierFk == NAME_REL_IS_LECTOTYPE_OF || relQualifierFk == NAME_REL_TYPE_NOT_DESIGNATED ){
						//TODO
						String originalNameString = null;
						boolean isRejectedType = (relQualifierFk == NAME_REL_IS_REJECTED_TYPE_OF);
						boolean isConservedType = (relQualifierFk == NAME_REL_IS_CONSERVED_TYPE_OF);
						boolean isLectoType = (relQualifierFk == NAME_REL_IS_LECTOTYPE_OF);
						boolean isNotDesignated = (relQualifierFk == NAME_REL_TYPE_NOT_DESIGNATED);
						
						//TODO addToAllNames true or false?
						nameTo.addNameTypeDesignation(nameFrom, citation, microcitation, originalNameString, isRejectedType, isConservedType, isLectoType, isNotDesignated, false);
						
					}else if (relQualifierFk == NAME_REL_IS_ORTHOGRAPHIC_VARIANT_OF){
						nameFrom.addRelationshipToName(nameTo, NameRelationshipType.ORTHOGRAPHIC_VARIANT(), citation, microcitation, rule) ;
					}else if (relQualifierFk == NAME_REL_IS_FIRST_PARENT_OF || relQualifierFk == NAME_REL_IS_SECOND_PARENT_OF || relQualifierFk == NAME_REL_IS_FEMALE_PARENT_OF || relQualifierFk == NAME_REL_IS_MALE_PARENT_OF){
						//HybridRelationships
						if (! (nameTo instanceof BotanicalName) || ! (nameFrom instanceof BotanicalName)){
							logger.warn("HybridrelationshipNames ("+name1Id +"," + name2Id +") must be of type BotanicalName but are not");
						}
						try {
							HybridRelationshipType hybridRelType = BerlinModelTransformer.relNameId2HybridRel(relQualifierFk);
							BotanicalName parent = (BotanicalName)nameFrom;
							BotanicalName child = (BotanicalName)nameTo;
							
							//TODO bug when trying to persist
							//parent.addHybridChild(child, hybridRelType, rule);
							logger.warn("HybridRelationships not yet implemented");
							
							//TODO reference
						} catch (UnknownCdmTypeException e) {
							//TODO
							logger.warn(e);
						}
					}else {
						//TODO
						Method method = config.getNamerelationshipTypeMethod();
						if (method != null){
							try {
								method.invoke(null, relQualifierFk, nameTo, nameFrom);
							} catch (Exception e) {
								logger.error(e.getMessage());
								logger.warn("NameRelationship could not be imported");
								return false;
							} 
						}
						
						logger.warn("NameRelationShipType " + relQualifierFk + " not yet implemented");
					}
					nameStore.add(nameFrom);
					
					//TODO
					//ID
					//etc.
				}else{
					//TODO
					if (nameFrom == null){
						 logger.warn("from TaxonName for RelName (" + relNameId + ") does not exist in store");
					}
					if (nameTo == null){
						logger.warn("to TaxonNames for RelName (" + relNameId + ") does not exist in store");
					}
				}
			}
			logger.info("TaxonName to save: " + nameStore.size());
			getNameService().saveTaxonNameAll(nameStore);
			
			logger.info("end makeNameRelationships ...");
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
		return ! config.isDoRelNames();
	}
	
}
