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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelNameStatusImportValidator;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;
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
	private static final String pluralString = "nomenclatural status";
	private static final String dbTableName = "NomStatusRel";

	
	public BerlinModelNameStatusImport(){
		super();
	}


	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery(BerlinModelImportState state) {
		String result =  " SELECT RIdentifier FROM " + getTableName();
		
		if (StringUtils.isNotEmpty(state.getConfig().getNameIdTable())){
			result += " WHERE nameFk IN (SELECT NameId FROM " + state.getConfig().getNameIdTable() + ")";
		}
		return result;
	}



	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
		String strQuery = 
			" SELECT NomStatusRel.*, NomStatus.NomStatus, RefDetail.Details " + 
			" FROM NomStatusRel INNER JOIN " +
              	" NomStatus ON NomStatusRel.NomStatusFk = NomStatus.NomStatusId " +
              	" LEFT OUTER JOIN RefDetail ON NomStatusRel.NomStatusRefDetailFk = RefDetail.RefDetailId AND " + 
              	" NomStatusRel.NomStatusRefFk = RefDetail.RefFk " +
            " WHERE (RIdentifier IN (" + ID_LIST_TOKEN + "))";
		return strQuery;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.berlinModel.in.ResultSetPartitioner, eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner,BerlinModelImportState state) {
		boolean success = true;	
		String dbAttrName;
		String cdmAttrName;
		
		Set<TaxonNameBase> namesToSave = new HashSet<TaxonNameBase>();
		BerlinModelImportConfigurator config = state.getConfig();
		Map<String, TaxonNameBase> nameMap = (Map<String, TaxonNameBase>) partitioner.getObjectMap(BerlinModelTaxonNameImport.NAMESPACE);

		ResultSet rs = partitioner.getResultSet();
		try {
			//get data from database
			
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
				String nomStatusLabel = rs.getString("NomStatus");
				
				TaxonNameBase taxonName = nameMap.get(String.valueOf(nameId));
				//TODO doubtful
				
				if (taxonName != null ){
					try{
						NomenclaturalStatus nomStatus = BerlinModelTransformer.nomStatusFkToNomStatus(nomStatusFk, nomStatusLabel);
						if (nomStatus == null){
							String message = "Nomenclatural status could not be defined for %s ; %s";
							message = String.format(message, nomStatusFk, nomStatusLabel);
							logger.warn(message);
							success = false;
							continue;
						}else{
							if (nomStatus.getType() == null){
								String message = "Nomenclatural status type could not be defined for %s ; %s";
								message = String.format(message, nomStatusFk, nomStatusLabel);
								logger.warn(message);
								success = false;
								continue;
							}else if(nomStatus.getType().getId() == 0){
								getTermService().save(nomStatus.getType());
							}
						}
						
						//reference
						makeReference(config, nomStatus, nameId, rs, partitioner);
						
						//Details
						dbAttrName = "details";
						cdmAttrName = "citationMicroReference";
						success &= ImportHelper.addStringValue(rs, nomStatus, dbAttrName, cdmAttrName);
						
						//doubtful
						if (doubtful){
							nomStatus.addMarker(Marker.NewInstance(MarkerType.IS_DOUBTFUL(), true));
						}
						taxonName.addStatus(nomStatus);
						namesToSave.add(taxonName);
					}catch (UnknownCdmTypeException e) {
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
			logger.info("TaxonNames to save: " + namesToSave.size());
			getNameService().save(namesToSave);
			
			return success;
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

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
				handleForeignKey(rs, nameIdSet, "nameFk");
				handleForeignKey(rs, referenceIdSet, "NomStatusRefFk");
				handleForeignKey(rs, refDetailIdSet, "NomStatusRefDetailFk");
			}
			
			//name map
			nameSpace = BerlinModelTaxonNameImport.NAMESPACE;
			cdmClass = TaxonNameBase.class;
			idSet = nameIdSet;
			Map<String, Person> nameMap = (Map<String, Person>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, nameMap);

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
	
	private boolean makeReference(IImportConfigurator config, NomenclaturalStatus nomStatus, 
			int nameId, ResultSet rs, ResultSetPartitioner partitioner) 
			throws SQLException{
		
		Map<String, Reference> biblioRefMap = partitioner.getObjectMap(BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE);
		Map<String, Reference> nomRefMap = partitioner.getObjectMap(BerlinModelReferenceImport.NOM_REFERENCE_NAMESPACE);
		Map<String, Reference> biblioDetailMap = partitioner.getObjectMap(BerlinModelRefDetailImport.BIBLIO_REFDETAIL_NAMESPACE);
		Map<String, Reference> nomRefDetailMap = partitioner.getObjectMap(BerlinModelRefDetailImport.NOM_REFDETAIL_NAMESPACE);
		
		Object nomRefFkObj = rs.getObject("NomStatusRefFk");
		Object nomRefDetailFkObj = rs.getObject("NomStatusRefDetailFk");
		//TODO
//		boolean refDetailPrelim = rs.getBoolean("RefDetailPrelim");
		
		boolean success = true;
		//nomenclatural Reference
		if (biblioRefMap != null){
			if (nomRefFkObj != null){
				String nomRefFk = String.valueOf(nomRefFkObj);
				String nomRefDetailFk = String.valueOf(nomRefDetailFkObj);
				Reference ref = getReferenceFromMaps(biblioRefMap,
						nomRefMap, biblioDetailMap, nomRefDetailMap,
						nomRefDetailFk, nomRefFk);									
				
				//setRef
				if (ref == null ){
					//TODO
					if (! config.isIgnoreNull()){logger.warn("Reference (refFk = " + nomRefFk + ") for NomStatus of TaxonName (nameId = " + nameId + ")"+
						" was not found in reference store. Nomenclatural status reference was not set!!");}
				}else{
					nomStatus.setCitation(ref);
				}
			}
		}
		return success;
	}



	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		IOValidator<BerlinModelImportState> validator = new BerlinModelNameStatusImportValidator();
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
		return ! state.getConfig().isDoNameStatus();
	}


}
