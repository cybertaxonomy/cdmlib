// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportBase;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class DbImportObjectCreationMapper<CDM_BASE extends CdmBase> extends MultipleAttributeMapperBase implements IDbImportMapper<DbImportStateBase<?>, CDM_BASE> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbImportObjectCreationMapper.class);
	
	public static DbImportObjectCreationMapper<?> NewInstance(IMappingImport mappingImport){
		return new DbImportObjectCreationMapper(mappingImport);
	}
	
	private IMappingImport<CDM_BASE> mappingImport;
	
//	public static <T extends CdmImportBase> DbImportObjectCreationMapper NewInstance(String dbAttributeString, CdmImportBase importBase){
//		String methodName = "get" + dbAttributeString;
//		return NewInstance(dbAttributeString, importBase, methodName);
//	}
	

	
	/**
	 * @param parameterTypes 
	 * @param dbIdAttributString
	 */
	protected DbImportObjectCreationMapper(IMappingImport<CDM_BASE> mappingImport) {
		//FIXME parametrisertes super
		super();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IDbImportMapper#invoke(java.sql.ResultSet, eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public CDM_BASE invoke(ResultSet rs, CDM_BASE cdmBase) throws SQLException {
		cdmBase = mappingImport.createObject(rs);
		return cdmBase;
	}
	
}
