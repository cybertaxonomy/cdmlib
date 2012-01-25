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
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_ALTERNATIVE_NAME_FOR;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_BASIONYM_FOR;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_CONSERVED_TYPE_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_FEMALE_PARENT_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_FIRST_PARENT_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_LATER_HOMONYM_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_LATER_VALIDATION_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_LECTOTYPE_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_MALE_PARENT_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_ORTHOGRAPHIC_VARIANT_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_REJECTED_TYPE_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_REPLACED_SYNONYM_FOR;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_SECOND_PARENT_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_TYPE_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_IS_VALIDATION_OF;
import static eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer.NAME_REL_TYPE_NOT_DESIGNATED;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelTaxonNameRelationImportValidator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.HybridRelationshipType;
import eu.etaxonomy.cdm.model.name.NameRelationshipType;
import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
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
	private static final String pluralString = "name relations";
	private static final String dbTableName = "RelName";

	
	public BerlinModelTaxonNameRelationImport(){
		super();
	}

	

	@Override
	protected String getIdQuery(BerlinModelImportState state) {
		if (StringUtils.isNotBlank(state.getConfig().getNameIdTable())){
			String result = super.getIdQuery(state);
			result += " WHERE nameFk1 IN (SELECT NameId FROM %s) OR ";
			result += "       nameFk2 IN (SELECT NameId FROM %s)";
			result = String.format(result, state.getConfig().getNameIdTable(),state.getConfig().getNameIdTable() );
			return result;
		}else{
			return super.getIdQuery(state);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
			String strQuery = 
					" SELECT RelName.*, FromName.nameId as name1Id, ToName.nameId as name2Id, RefDetail.Details " + 
					" FROM Name as FromName INNER JOIN " +
                      	" RelName ON FromName.NameId = RelName.NameFk1 INNER JOIN " +
                      	" Name AS ToName ON RelName.NameFk2 = ToName.NameId LEFT OUTER JOIN "+
                      	" RefDetail ON RelName.RefDetailFK = RefDetail.RefDetailId " + 
            " WHERE (RelNameId IN ("+ID_LIST_TOKEN +"))";
		return strQuery;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.berlinModel.in.ResultSetPartitioner, eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState state) {
		boolean success = true ;
		BerlinModelImportConfigurator config = state.getConfig();
		Set<TaxonNameBase> nameToSave = new HashSet<TaxonNameBase>();
		Map<String, TaxonNameBase> nameMap = (Map<String, TaxonNameBase>) partitioner.getObjectMap(BerlinModelTaxonNameImport.NAMESPACE);
		Map<String, Reference> biblioRefMap = partitioner.getObjectMap(BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE);
		Map<String, Reference> nomRefMap = partitioner.getObjectMap(BerlinModelReferenceImport.NOM_REFERENCE_NAMESPACE);

			
		ResultSet rs = partitioner.getResultSet();
		try {
			
			int i = 0;
			//for each name relation
			while (rs.next()){
				
				if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("RelName handled: " + (i-1));}
				
				int relNameId = rs.getInt("RelNameId");
				int name1Id = rs.getInt("name1Id");
				int name2Id = rs.getInt("name2Id");
				Object relRefFkObj = rs.getObject("refFk");
				String details = rs.getString("details");
				int relQualifierFk = rs.getInt("relNameQualifierFk");
				String notes = rs.getString("notes");
				
				TaxonNameBase nameFrom = nameMap.get(String.valueOf(name1Id));
				TaxonNameBase nameTo = nameMap.get(String.valueOf(name2Id));
				
				
				Reference<?> citation = null;
				if (relRefFkObj != null){
					String relRefFk = String.valueOf(relRefFkObj);
					//get nomRef
					citation = getReferenceOnlyFromMaps(biblioRefMap, nomRefMap, 
							relRefFk);
					}
				
				//TODO (preliminaryFlag = true testen
				String microcitation = details;
				String rule = null;  
				
				if (nameFrom != null && nameTo != null){
					success = handleNameRelationship(success, config, name1Id, name2Id,	relQualifierFk, 
							notes, nameFrom, nameTo, citation, microcitation, rule);
					nameFrom.setTitleCache(null);
					nameTo.setTitleCache(null);
					nameFrom.getTitleCache();
					nameTo.getTitleCache();
					nameToSave.add(nameFrom);
					
					//TODO
					//ID
					//etc.
				}else{
					//TODO
					if (nameFrom == null){
						 logger.warn("from TaxonName " + name1Id + "  for RelName (" + relNameId + " , type: " + relQualifierFk + ") does not exist in store. ToName is: " + (nameTo == null ? "" : nameTo.getTitleCache()));
					}
					if (nameTo == null){
						logger.warn("to TaxonName " + name2Id + " for RelName (" + relNameId + " , type: " + relQualifierFk + ") does not exist in store. FromName is: "  + (nameFrom == null ? "" : nameFrom.getTitleCache()));
					}
					success = false;
				}
			}
			
			
			partitioner.startDoSave();
			getNameService().save(nameToSave);
			
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	}

	/**
	 * @param success
	 * @param config
	 * @param name1Id
	 * @param name2Id
	 * @param relQualifierFk
	 * @param notes
	 * @param nameFrom
	 * @param nameTo
	 * @param citation
	 * @param microcitation
	 * @param rule
	 * @return
	 */
	private boolean handleNameRelationship(boolean success,
				BerlinModelImportConfigurator config, int name1Id, int name2Id,
				int relQualifierFk, String notes, TaxonNameBase nameFrom,
				TaxonNameBase nameTo, Reference<?> citation,
				String microcitation, String rule) {
		AnnotatableEntity nameRelationship = null;
		if (relQualifierFk == NAME_REL_IS_BASIONYM_FOR){
			nameRelationship = nameTo.addBasionym(nameFrom, citation, microcitation, rule);
		}else if (relQualifierFk == NAME_REL_IS_LATER_HOMONYM_OF){
			nameRelationship = nameFrom.addRelationshipToName(nameTo, NameRelationshipType.LATER_HOMONYM(), citation, microcitation, rule) ;
		}else if (relQualifierFk == NAME_REL_IS_REPLACED_SYNONYM_FOR){
			nameRelationship = nameFrom.addRelationshipToName(nameTo, NameRelationshipType.REPLACED_SYNONYM(), citation, microcitation, rule) ;
		}else if (relQualifierFk == NAME_REL_HAS_SAME_TYPE_AS){
			nameTo.getHomotypicalGroup().merge(nameFrom.getHomotypicalGroup());
		}else if (relQualifierFk == NAME_REL_IS_VALIDATION_OF){
			nameRelationship = nameTo.addRelationshipToName(nameFrom, NameRelationshipType.VALIDATED_BY_NAME(), citation, microcitation, rule) ;
		}else if (relQualifierFk == NAME_REL_IS_LATER_VALIDATION_OF){
			nameRelationship = nameTo.addRelationshipToName(nameFrom, NameRelationshipType.LATER_VALIDATED_BY_NAME(), citation, microcitation, rule) ;
		}else if (relQualifierFk == NAME_REL_IS_TYPE_OF || relQualifierFk == NAME_REL_IS_REJECTED_TYPE_OF ||  relQualifierFk == NAME_REL_IS_CONSERVED_TYPE_OF || relQualifierFk == NAME_REL_IS_LECTOTYPE_OF || relQualifierFk == NAME_REL_TYPE_NOT_DESIGNATED ){
			boolean isRejectedType = (relQualifierFk == NAME_REL_IS_REJECTED_TYPE_OF);
			boolean isConservedType = (relQualifierFk == NAME_REL_IS_CONSERVED_TYPE_OF);
			boolean isLectoType = (relQualifierFk == NAME_REL_IS_LECTOTYPE_OF);
			boolean isNotDesignated = (relQualifierFk == NAME_REL_TYPE_NOT_DESIGNATED);
			
			NameTypeDesignationStatus status = null;
			String originalNameString = null;
			//TODO addToAllNames true or false?
			boolean addToAllNames = false;
			if (config.getNameTypeDesignationStatusMethod() != null){
				Method method = config.getNameTypeDesignationStatusMethod();
				method.setAccessible(true);
				try {
					status = (NameTypeDesignationStatus)method.invoke(null, notes);
					nameRelationship = nameTo.addNameTypeDesignation(nameFrom, citation, microcitation, originalNameString, status, addToAllNames);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}else{
				if (isLectoType){
					status = NameTypeDesignationStatus.LECTOTYPE();
				}
				nameRelationship = nameTo.addNameTypeDesignation(nameFrom, citation, microcitation, originalNameString, status, isRejectedType, isConservedType, /*isLectoType,*/ isNotDesignated, addToAllNames);
			}
			
		}else if (relQualifierFk == NAME_REL_IS_ORTHOGRAPHIC_VARIANT_OF){
			nameRelationship = nameFrom.addRelationshipToName(nameTo, NameRelationshipType.ORTHOGRAPHIC_VARIANT(), citation, microcitation, rule) ;
		}else if (relQualifierFk == NAME_REL_IS_ALTERNATIVE_NAME_FOR){
			nameRelationship = nameFrom.addRelationshipToName(nameTo, NameRelationshipType.ALTERNATIVE_NAME(), citation, microcitation, rule) ;
		}else if (relQualifierFk == NAME_REL_IS_FIRST_PARENT_OF || relQualifierFk == NAME_REL_IS_SECOND_PARENT_OF || relQualifierFk == NAME_REL_IS_FEMALE_PARENT_OF || relQualifierFk == NAME_REL_IS_MALE_PARENT_OF){
			//HybridRelationships
			if (! (nameTo instanceof NonViralName ) || ! (nameFrom instanceof NonViralName)){
				logger.warn("HybridrelationshipNames ("+name1Id +"," + name2Id +") must be of type NonViralNameName but are not");
				success = false;
			}
			try {
				HybridRelationshipType hybridRelType = BerlinModelTransformer.relNameId2HybridRel(relQualifierFk);
				BotanicalName parent = (BotanicalName)nameFrom;
				BotanicalName child = (BotanicalName)nameTo;
				
				nameRelationship = parent.addHybridChild(child, hybridRelType, rule);
				
			} catch (UnknownCdmTypeException e) {
				logger.warn(e);
				success = false;
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
					success = false;
				} 
			}else{
				logger.warn("NameRelationShipType " + relQualifierFk + " not yet implemented");
				success = false;
			}
		}
		doNotes(nameRelationship, notes);
		return success;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs) {
		String nameSpace;
		Class cdmClass;
		Set<String> idSet;
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		try{
			Set<String> nameIdSet = new HashSet<String>();
			Set<String> referenceIdSet = new HashSet<String>();
			Set<String> refDetailIdSet = new HashSet<String>();
			while (rs.next()){
				handleForeignKey(rs, nameIdSet, "name1Id");
				handleForeignKey(rs, nameIdSet, "name2Id");
				handleForeignKey(rs, referenceIdSet, "RefFk");
				handleForeignKey(rs, refDetailIdSet, "RefDetailFk");
	}
	
			//name map
			nameSpace = BerlinModelTaxonNameImport.NAMESPACE;
			cdmClass = TaxonNameBase.class;
			idSet = nameIdSet;
			Map<String, Person> objectMap = (Map<String, Person>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, objectMap);

			//nom reference map
			nameSpace = BerlinModelReferenceImport.NOM_REFERENCE_NAMESPACE;
			cdmClass = Reference.class;
			idSet = referenceIdSet;
			Map<String, Reference> nomReferenceMap = (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, nomReferenceMap);

			//biblio reference map
			nameSpace = BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE;
			cdmClass = Reference.class;
			idSet = referenceIdSet;
			Map<String, Reference> biblioReferenceMap = (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, biblioReferenceMap);
	
			//nom refDetail map
			nameSpace = BerlinModelRefDetailImport.NOM_REFDETAIL_NAMESPACE;
			cdmClass = Reference.class;
			idSet = refDetailIdSet;
			Map<String, Reference> nomRefDetailMap= (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, nomRefDetailMap);
			
			//biblio refDetail map
			nameSpace = BerlinModelRefDetailImport.BIBLIO_REFDETAIL_NAMESPACE;
			cdmClass = Reference.class;
			idSet = refDetailIdSet;
			Map<String, Reference> biblioRefDetailMap= (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, biblioRefDetailMap);

		} catch (SQLException e) {
			throw new RuntimeException(e);
				}
		return result;
	}
				
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		IOValidator<BerlinModelImportState> validator = new BerlinModelTaxonNameRelationImportValidator();
		return validator.validate(state);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getTableName()
	 */
	@Override
	protected String getTableName() {
		return dbTableName;
	}

	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getPluralString()
	 */
	@Override
	public String getPluralString() {
		return pluralString;
			}
			
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelImportState state){
		return ! state.getConfig().isDoRelNames();
		}

	
}
