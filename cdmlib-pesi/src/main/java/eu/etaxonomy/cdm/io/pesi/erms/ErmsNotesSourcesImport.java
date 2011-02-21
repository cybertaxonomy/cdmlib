// $Id$
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
import eu.etaxonomy.cdm.io.common.mapping.DbImportDescriptionElementSourceCreationMapper;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMapping;
import eu.etaxonomy.cdm.io.pesi.erms.validation.ErmsVernacularSourceImportValidator;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.DescriptionElementSource;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;

/**
 * @author a.mueller
 * @created 12.03.2010
 * @version 1.0
 */
@Component
public class ErmsNotesSourcesImport extends ErmsImportBase<CommonTaxonName> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ErmsNotesSourcesImport.class);
	
	
//************************** VARIABLES ********************************************
	
	private static String pluralString = "note sources";
	private static String dbTableName = "notes_sources";
	private static final Class cdmTargetClass = DescriptionElementSource.class;

	private DbImportMapping mapping;

	
//******************************************* CONSTRUCTOR *******************************	
	
	/**
	 * @param dbTableName 
	 * @param pluralString
	 * @param dbTableName
	 */
	public ErmsNotesSourcesImport() {
		super(pluralString, dbTableName, cdmTargetClass);
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#getRecordQuery(eu.etaxonomy.cdm.io.erms.ErmsImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(ErmsImportConfigurator config) {
		String strQuery = 
			" SELECT * " + 
			" FROM vernaculars_sources " +
			" WHERE vernacular_id IN (" + ID_LIST_TOKEN + ") AND " +
					" source_id IN (" + ID_LIST_TOKEN + ")";
		return strQuery;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery() {
		String strQuery = 
			" SELECT vernacular_id, source_id " + 
			" FROM vernaculars_sources " 
			;
		return strQuery;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#getMapping()
	 */
	protected DbImportMapping getMapping() {
		if (mapping == null){
			mapping = new DbImportMapping();
			String vernacularNamespace = ErmsVernacularImport.VERNACULAR_NAMESPACE;
			String referenceNamespace = ErmsReferenceImport.REFERENCE_NAMESPACE;
			mapping.addMapper(DbImportDescriptionElementSourceCreationMapper.NewInstance("vernacular_id", vernacularNamespace, "source_id", referenceNamespace ));
		}
		return mapping;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map getRelatedObjectsForPartition(ResultSet rs) {
		String nameSpace;
		Class cdmClass;
		Set<String> idSet;
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		
		try{
			Set<String> vernacularIdSet = new HashSet<String>();
			Set<String> sourceIdSet = new HashSet<String>();
			while (rs.next()){
				handleForeignKey(rs, vernacularIdSet, "vernacular_id");
				handleForeignKey(rs, sourceIdSet, "source_id");
			}
			
			//vernacular map
			nameSpace = ErmsVernacularImport.VERNACULAR_NAMESPACE;
			cdmClass = CommonTaxonName.class;
			idSet = vernacularIdSet;
			Map<String, CommonTaxonName> vernacularMap = (Map<String, CommonTaxonName>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, vernacularMap);
			
			
			//reference map
			nameSpace = ErmsReferenceImport.REFERENCE_NAMESPACE;
			cdmClass = Reference.class;
			idSet = sourceIdSet;
			Map<String, Reference> referenceMap = (Map<String, Reference>)getCommonService().getSourcedObjectsByIdInSource(cdmClass, idSet, nameSpace);
			result.put(nameSpace, referenceMap);
	
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(ErmsImportState state) {
		IOValidator<ErmsImportState> validator = new ErmsVernacularSourceImportValidator();
		return validator.validate(state);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean isIgnore(ErmsImportState state) {
		boolean isDo = state.getConfig().isDoVernaculars() && state.getConfig().isDoVernaculars();
		return ! isDo ;
	}
}
