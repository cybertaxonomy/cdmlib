/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.eflora.centralAfrica.checklist;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMapping;
import eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapper;
import eu.etaxonomy.cdm.io.common.mapping.IMappingImport;
import eu.etaxonomy.cdm.io.eflora.centralAfrica.checklist.validation.CentralAfricaChecklistTaxonImportValidator;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;


/**
 * @author a.mueller
 * @created 20.02.2010
 */
@Component
public class CentralAfricaChecklistReferenceImport  extends CentralAfricaChecklistImportBase<Reference> implements IMappingImport<Reference, CentralAfricaChecklistImportState>{
	private static final Logger logger = Logger.getLogger(CentralAfricaChecklistReferenceImport.class);
	
	private DbImportMapping<?,?> mapping;
	
//	private int modCount = 10000;
	private static final String pluralString = "references";
	private static final String dbTableName = "checklist";
	private static final Class<?> cdmTargetClass = TaxonBase.class;
	private static final String strOrderBy = " ORDER BY source ";

	public CentralAfricaChecklistReferenceImport(){
		super(pluralString, dbTableName, cdmTargetClass);
	}
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery() {
		String strQuery = " SELECT DISTINCT source FROM " + dbTableName +
						strOrderBy;
		return strQuery;
	}

	@Override
	protected DbImportMapping<?,?> getMapping() {
		if (mapping == null){
			mapping = new DbImportMapping();
				mapping.addMapper(DbImportObjectCreationMapper.NewInstance(this, "source", REFERENCE_NAMESPACE)); 
		}
		
		return mapping;
	}

	@Override
	protected String getRecordQuery(CentralAfricaChecklistImportConfigurator config) {
		String strSelect = " SELECT DISTINCT source ";
		String strFrom = " FROM checklist";
		String strWhere = " WHERE ( source IN (" + ID_LIST_TOKEN + ") )";
		String strRecordQuery = strSelect + strFrom + strWhere + strOrderBy;
		return strRecordQuery;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs) {
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		return result;
	}
	
	@Override
	public Reference<?> createObject(ResultSet rs, CentralAfricaChecklistImportState state) throws SQLException {
		Reference<?> ref = ReferenceFactory.newGeneric();
		String sourceString = rs.getString("source");
		ref.setTitle(sourceString);
		return ref;
	}

	@Override
	protected boolean doCheck(CentralAfricaChecklistImportState state){
		IOValidator<CentralAfricaChecklistImportState> validator = new CentralAfricaChecklistTaxonImportValidator();
		return validator.validate(state);
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(CentralAfricaChecklistImportState state){
		return state.getConfig().getDoReferences().equals(IImportConfigurator.DO_REFERENCES.NONE);
	}



}
