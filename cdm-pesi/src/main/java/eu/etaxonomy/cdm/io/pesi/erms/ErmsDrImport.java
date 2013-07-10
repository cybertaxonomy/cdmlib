/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.pesi.erms;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.mapping.DbIgnoreMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportAnnotationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportDistributionCreationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMapping;
import eu.etaxonomy.cdm.io.common.mapping.DbImportObjectMapper;
import eu.etaxonomy.cdm.io.pesi.erms.validation.ErmsDrImportValidator;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 * @created 20.02.2010
 * @version 1.0
 */
@Component
public class ErmsDrImport  extends ErmsImportBase<Distribution> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ErmsDrImport.class);
	
	private DbImportMapping<ErmsImportState, ErmsImportConfigurator> mapping;
	
	private static final String pluralString = "distributions";
	private static final String dbTableName = "dr";
	private static final Class<?> cdmTargetClass = Distribution.class;

	public ErmsDrImport(){
		super(pluralString, dbTableName, cdmTargetClass);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(ErmsImportConfigurator config) {
		String strRecordQuery = 
			" SELECT dr.*, tu.tu_acctaxon, tu.id " + 
			" FROM dr INNER JOIN tu ON dr.tu_id = tu.id " +
			" WHERE ( dr.id IN (" + ID_LIST_TOKEN + ") )";
		return strRecordQuery;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#getMapping()
	 */
	protected DbImportMapping<ErmsImportState, ErmsImportConfigurator> getMapping() {
		if (mapping == null){
			mapping = new DbImportMapping<ErmsImportState, ErmsImportConfigurator>();
			
			PresenceTerm status = PresenceTerm.PRESENT();
			DbImportDistributionCreationMapper<?> distributionMapper = DbImportDistributionCreationMapper.NewFixedStatusInstance("id", DR_NAMESPACE, "tu_acctaxon", ErmsTaxonImport.TAXON_NAMESPACE, status);
			distributionMapper.setSource("source_id", REFERENCE_NAMESPACE, null);
			mapping.addMapper(distributionMapper);
			
			mapping.addMapper(DbImportObjectMapper.NewInstance("gu_id", "area", ErmsAreaImport.AREA_NAMESPACE));
			mapping.addMapper(DbImportAnnotationMapper.NewInstance("note", AnnotationType.EDITORIAL()));
			
			mapping.addMapper(DbIgnoreMapper.NewInstance("unacceptsource_id"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("unacceptreason"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("valid_flag"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("certain_flag"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("map_flag"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("endemic_flag"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("exotic_flag"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("typelocality_flag"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("specimenflag"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("lat"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("long"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("depthshallow"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("depthdeep"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("beginyear"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("beginmonth"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("beginday"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("endyear"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("endmonth"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("endday"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("min_abundance"));
			mapping.addMapper(DbIgnoreMapper.NewInstance("max_abundance"));

			
		}
		return mapping;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs) {
		String nameSpace;
		Class<?> cdmClass;
		Set<String> idSet;
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		try{
			Set<String> taxonIdSet = new HashSet<String>();
			Set<String> areaIdSet = new HashSet<String>();
			Set<String> sourceIdSet = new HashSet<String>();
			while (rs.next()){
				handleForeignKey(rs, taxonIdSet,"tu_acctaxon" );
				handleForeignKey(rs, areaIdSet, "gu_id");
				handleForeignKey(rs, sourceIdSet, "source_id");
			}
			
			//taxon map
			nameSpace = ErmsTaxonImport.TAXON_NAMESPACE;
			cdmClass = TaxonBase.class;
			idSet = taxonIdSet;
			Map<String, TaxonBase<?>> taxonMap = (Map<String, TaxonBase<?>>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, taxonMap);
			
			//areas
			nameSpace = ErmsAreaImport.AREA_NAMESPACE;
			cdmClass = NamedArea.class;
			idSet = areaIdSet;
			Map<String, NamedArea> areaMap = (Map<String, NamedArea>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, areaMap);
			
			//reference map
			nameSpace = ErmsReferenceImport.REFERENCE_NAMESPACE;
			cdmClass = Reference.class;
			idSet = sourceIdSet;
			Map<String, Reference<?>> referenceMap = (Map<String, Reference<?>>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, referenceMap);

			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	
	/**
	 * @param distribution
	 * @param source_id
	 * @param state 
	 */
	private void addSource(Distribution distribution, Integer source_id, ErmsImportState state) {
		Reference<?> ref = (Reference)state.getRelatedObject(ErmsReferenceImport.REFERENCE_NAMESPACE, String.valueOf(source_id));
		distribution.addSource(null, null, ref, null);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IMappingImport#createObject(java.sql.ResultSet, eu.etaxonomy.cdm.io.common.ImportStateBase)
	 */
	public Distribution createObject(ResultSet rs, ErmsImportState state)
			throws SQLException {
		return null;  //not needed
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(ErmsImportState state){
		IOValidator<ErmsImportState> validator = new ErmsDrImportValidator();
		return validator.validate(state);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(ErmsImportState state){
		return ! state.getConfig().isDoOccurrence();
	}




}
