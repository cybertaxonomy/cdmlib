/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.erms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.mapping.DbImportAnnotationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMapping;
import eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportStringMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbNotYetImplementedMapper;
import eu.etaxonomy.cdm.io.erms.validation.ErmsReferenceImportValidator;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 * @created 20.02.2010
 * @version 1.0
 */
@Component
public class ErmsDrImport  extends ErmsImportBase<TaxonBase> {
	private static final Logger logger = Logger.getLogger(ErmsDrImport.class);

	private static final String DR_NAMESPACE = "dr";
	
	
	public static final UUID IMIS_UUID = UUID.fromString("ee2ac2ca-b60c-4e6f-9cad-720fcdb0a6ae");
	
	private DbImportMapping mapping;
	
	
	private int modCount = 10000;
	private static final String pluralString = "distributions";
	private String dbTableName = "dr";
	private Class cdmTargetClass = Distribution.class;

	public ErmsDrImport(){
		super();
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(ErmsImportConfigurator config) {
		String strRecordQuery = 
			" SELECT * " + 
			" FROM " + dbTableName +
			" WHERE ( dr.id IN (" + ID_LIST_TOKEN + ") )";
		return strRecordQuery;
	}

	/**
	 * @return
	 */
	private DbImportMapping getMapping() {
		if (mapping == null){
			mapping = new DbImportMapping();
			
			mapping.addMapper(DbImportObjectCreationMapper.NewInstance(this, "id", DR_NAMESPACE)); //id
			
			mapping.addMapper(DbImportAnnotationMapper.NewInstance("note", AnnotationType.EDITORIAL()));
			//not yet implemented
//			mapping.addMapper(DbNotYetImplementedMapper.NewInstance("source_type"));
			
		}
		return mapping;
	}
	
	
	public boolean doPartition(ResultSetPartitioner partitioner, ErmsImportState state) {
		boolean success = true ;
		ErmsImportConfigurator config = state.getConfig();
		Set referencesToSave = new HashSet<TaxonBase>();
		
 		DbImportMapping<?, ?> mapping = getMapping();
		mapping.initialize(state, cdmTargetClass);
		
		ResultSet rs = partitioner.getResultSet();
		try{
			while (rs.next()){
				success &= mapping.invoke(rs,referencesToSave);
			}
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	
		partitioner.startDoSave();
		getReferenceService().save(referencesToSave);
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
			Set<String> taxonIdSet = new HashSet<String>();
			Set<String> areaIdSet = new HashSet<String>();
			while (rs.next()){
				handleForeignKey(rs, taxonIdSet, "tu_id");
				handleForeignKey(rs, areaIdSet, "gu_id");
			}
			
			//taxon map
			nameSpace = ErmsTaxonImport.TAXON_NAMESPACE;
			cdmClass = TaxonBase.class;
			idSet = taxonIdSet;
			Map<String, TaxonBase> taxonMap = (Map<String, TaxonBase>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, taxonMap);
			
			//areas
			nameSpace = ErmsAreaImport.AREA_NAMESPACE;
			cdmClass = NamedArea.class;
			idSet = areaIdSet;
			Map<String, NamedArea> areaMap = (Map<String, NamedArea>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, areaMap);
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IMappingImport#createObject(java.sql.ResultSet, eu.etaxonomy.cdm.io.common.ImportStateBase)
	 */
	public TaxonBase createObject(ResultSet rs, ErmsImportState state) throws SQLException {
		Integer gu_id = rs.getInt("gu_id");
		Integer source_id = rs.getInt("source_id");
		Integer tu_id = rs.getInt("tu_id");
		
		UUID areaUuid = ErmsTransformer.uuidFromGuId(gu_id);
		NamedArea area = state.getNamedArea(areaUuid);
		PresenceTerm status = PresenceTerm.PRESENT();
		Distribution distribution = Distribution.NewInstance(area, status);
		addSource(distribution, source_id, state);
		//TODO check for multiple sources
		
		TaxonBase taxonBase = (TaxonBase)state.getRelatedObject(ErmsTaxonImport.TAXON_NAMESPACE, String.valueOf(tu_id));
		Taxon taxon = null;
		if (taxonBase instanceof Taxon){
			taxon = (Taxon)taxonBase;
			addDistribution(taxon, distribution);
		}else if (taxonBase instanceof Synonym){
			logger.warn("Distributions not yet implemented for synonyms: " + taxonBase.getName() +"("+ tu_id + ")");
		}else{ //null
			logger.warn("TaxonBase not found: " + tu_id);
		}
		return taxon;
	}

	/**
	 * @param distribution
	 * @param source_id
	 * @param state 
	 */
	private void addSource(Distribution distribution, Integer source_id, ErmsImportState state) {
		ReferenceBase ref = (ReferenceBase)state.getRelatedObject(ErmsReferenceImport.REFERENCE_NAMESPACE, String.valueOf(source_id));
		distribution.addSource(null, null, ref, null);
	}


	/**
	 * @param taxon
	 * @param distribution
	 */
	private void addDistribution(Taxon taxon, Distribution distribution) {
		Set<TaxonDescription> descriptions = taxon.getDescriptions();
		TaxonDescription description = null;
		if (descriptions.size() > 0){
			for (TaxonDescription desc : descriptions){
				if (! desc.isImageGallery()){
					description = desc;
					break;
				}
			}
		}
		if (description == null){
			description = TaxonDescription.NewInstance(taxon);
		}
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(ErmsImportState state){
		IOValidator<ErmsImportState> validator = new ErmsReferenceImportValidator();
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
	protected boolean isIgnore(ErmsImportState state){
		return ! state.getConfig().isDoOccurrence();
	}




}
