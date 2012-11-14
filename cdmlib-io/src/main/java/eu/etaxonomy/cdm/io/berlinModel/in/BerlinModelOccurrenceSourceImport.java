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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelOccurrenceSourceImportValidator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelOccurrenceSourceImport  extends BerlinModelImportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelOccurrenceSourceImport.class);

	private static int modCount = 5000;
	private static final String pluralString = "occurrence sources";
	private static final String dbTableName = "emOccurrenceSource";  //??
	
	
	private Map<String, Integer> sourceNumberRefIdMap;
	private Set<String> unfoundReferences = new HashSet<String>();
	

	public BerlinModelOccurrenceSourceImport(){
		super(dbTableName, pluralString);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery(BerlinModelImportState state) {
		String result = "SELECT occurrenceSourceId FROM " + getTableName();
		if (state.getConfig().getOccurrenceSourceFilter() != null){
			result += " WHERE " +  state.getConfig().getOccurrenceSourceFilter();
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
			String strQuery =   //DISTINCT because otherwise emOccurrenceSource creates multiple records for a single distribution 
            " SELECT * " + 
                " FROM emOccurrenceSource " +  
            " WHERE (OccurrenceSourceId IN (" + ID_LIST_TOKEN + ")  )" +  
             "";
		return strQuery;
	}
	
	

	@Override
	protected void doInvoke(BerlinModelImportState state) {
		unfoundReferences = new HashSet<String>();
		
		try {
			sourceNumberRefIdMap = makeSourceNumberReferenceIdMap(state);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		super.doInvoke(state);
		sourceNumberRefIdMap = null;
		if (unfoundReferences.size()>0){
			String unfound = "'" + CdmUtils.concat("','", unfoundReferences.toArray(new String[]{})) + "'"; 
			logger.warn("Not found references: " + unfound);
		}
		return;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.berlinModel.in.ResultSetPartitioner, eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState state) {
		boolean success = true;
		ResultSet rs = partitioner.getResultSet();

		Set<DescriptionElementBase> objectsToSave = new HashSet<DescriptionElementBase>();
		try {
			int i = 0;
			//for each reference
            while (rs.next()){
                
                if ((i++ % modCount) == 0 && i!= 1 ){ logger.info("occurrence sources handled: " + (i-1));}
                
                Integer occurrenceSourceId = rs.getInt("OccurrenceSourceId");
                Integer occurrenceFk = (Integer)rs.getObject("OccurrenceFk");
    			String sourceNumber = rs.getString("SourceNumber");
    			String oldName = rs.getString("OldName");
    			Integer oldNameFk = (Integer)rs.getObject("OldNameFk");
    			
    			Distribution distribution = (Distribution)state.getRelatedObject(BerlinModelOccurrenceImport.NAMESPACE, String.valueOf(occurrenceFk));
                
    			if (distribution == null){
    				//distribution = duplicateMap.get(occurrenceFk);
    			}
    			if (distribution != null){
    				Integer refId = sourceNumberRefIdMap.get(sourceNumber);
    				Reference<?> ref = getReference(refId, state);

    				if (ref != null){
    					DescriptionElementSource originalSource = DescriptionElementSource.NewInstance();
    					originalSource.setCitation(ref);
    					TaxonNameBase<?, ?> taxonName;
						taxonName = getName(state, oldName, oldNameFk);
						if (taxonName != null){
    						originalSource.setNameUsedInSource(taxonName);
    					}else if(CdmUtils.isNotEmpty(oldName)){
    						originalSource.setOriginalNameString(oldName);
    					}
    					distribution.addSource(originalSource);
    				}else{
    					logger.warn("reference for sourceNumber "+sourceNumber+" could not be found. OccurrenceSourceId: " + occurrenceSourceId );
    					unfoundReferences.add(sourceNumber);
    				}
    			}else{
    				logger.warn("distribution ("+occurrenceFk+") for occurrence source (" + occurrenceSourceId + ") could not be found." );
    			}
                
            }
			logger.info("Distributions to save: " + objectsToSave.size());
			getDescriptionService().saveDescriptionElement(objectsToSave);	
			
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
			Set<String> occurrenceIdSet = new HashSet<String>();
			Set<String> referenceIdSet = new HashSet<String>();
			Set<String> nameIdSet = new HashSet<String>();
			Set<String> sourceNumberSet = new HashSet<String>();
			while (rs.next()){
				handleForeignKey(rs, occurrenceIdSet, "occurrenceFk");
				handleForeignKey(rs, nameIdSet, "oldNameFk");
				sourceNumberSet.add(CdmUtils.NzTrim(rs.getString("SourceNumber")));
			}
			
			sourceNumberSet.remove("");
			referenceIdSet = handleSourceNumber(rs, sourceNumberSet, result);
			
			
			//occurrence map
			nameSpace = BerlinModelOccurrenceImport.NAMESPACE;
			cdmClass = Distribution.class;
			idSet = occurrenceIdSet;
			Map<String, Distribution> occurrenceMap = (Map<String, Distribution>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, occurrenceMap);

			//name map
			nameSpace = BerlinModelTaxonNameImport.NAMESPACE;
			cdmClass = TaxonNameBase.class;
			idSet =nameIdSet;
			Map<String, TaxonNameBase> nameMap = (Map<String, TaxonNameBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
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


		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	private Set<String> handleSourceNumber(ResultSet rs, Set<String> sourceNumberSet, Map<Object, Map<String, ? extends CdmBase>> result) {
		Map<String, Integer> sourceNumberReferenceIdMap = this.sourceNumberRefIdMap;
		Set<String> referenceIdSet = new HashSet<String>();
		
		for(String sourceNumber : sourceNumberSet){
			Integer refId = sourceNumberReferenceIdMap.get(sourceNumber);
			referenceIdSet.add(String.valueOf(refId));		
		}
		return referenceIdSet;
	}

	
	
	/**
	 * @param state
	 * @param oldName
	 * @param oldNameFk
	 * @return
	 */
	boolean isFirstTimeNoNameByService = true;
	private TaxonNameBase<?, ?> getName(BerlinModelImportState state, String oldName, Integer oldNameFk) {
		TaxonNameBase<?,?> taxonName = (TaxonNameBase)state.getRelatedObject(BerlinModelTaxonNameImport.NAMESPACE, String.valueOf(oldNameFk));
		if (taxonName == null && oldName != null){
			if (isFirstTimeNoNameByService){
				logger.warn("oldName not checked against names in BerlinModel. Just take it as a string");
				isFirstTimeNoNameByService = false;
			}
			List<NonViralName> names = new ArrayList<NonViralName>();
//			names = getNameService().getNamesByNameCache(oldName);
			if (names.size() == 1){
				return names.get(0);
			}else {
				if (names.size()> 2){
					logger.info("There is more than one name matching oldName: " + oldName + ".");
				}
				return null;
				//taxonName = nameParser.parseSimpleName(oldName);
			}
		}
		return taxonName;
	}

	/**
	 * Creates a map which maps source numbers on references
	 * @param state
	 * @return
     * @throws SQLException 
	 */
	private Map<String, Integer> makeSourceNumberReferenceIdMap(BerlinModelImportState state) throws SQLException {
		Map<String, Integer> result = new HashMap<String, Integer>();
		
		Source source = state.getConfig().getSource();
		String strQuery = " SELECT RefId, IdInSource " +  
						  " FROM Reference " + 
						  " WHERE     (IdInSource IS NOT NULL) AND (IdInSource NOT LIKE '') ";
		
		ResultSet rs = source.getResultSet(strQuery) ;
		while (rs.next()){
			int refId = rs.getInt("RefId");
			String idInSource = rs.getString("IdInSource");
			if (idInSource != null){
				String[] singleSources = idInSource.split("\\|");
				for (String singleSource : singleSources){
					singleSource = singleSource.trim();
					result.put(singleSource, refId);
				}
			}
		}
		return result;
	}

	

	private Reference getReference(Integer refId, BerlinModelImportState state) {
		Reference<?> ref = (Reference)state.getRelatedObject(BerlinModelReferenceImport.BIBLIO_REFERENCE_NAMESPACE, String.valueOf(refId));
		if (ref == null){
			ref = (Reference)state.getRelatedObject(BerlinModelReferenceImport.NOM_REFERENCE_NAMESPACE, String.valueOf(refId));;
		}
		return ref;
	}

	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		IOValidator<BerlinModelImportState> validator = new BerlinModelOccurrenceSourceImportValidator();
		return validator.validate(state);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelImportState state){
		if (! state.getConfig().isDoOccurrence()){
			return true;
		}else{
			if (!this.checkSqlServerColumnExists(state.getConfig().getSource(), "emOccurrenceSource", "OccurrenceSourceId")){
				logger.error("emOccurrenceSource table or emOccurrenceSourceId does not exist. Must ignore occurrence import");
				return true;
			}else{
				return false;
			}
		}
	}
	
}
