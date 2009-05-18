// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.berlinModel.out.mapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.hsqldb.Types;

import eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportConfigurator;
import eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportMapping;
import eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportState;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class NomStatusMapper extends DbSingleAttributeExportMapperBase<BerlinModelExportState<?>> implements IDbExportMapper<BerlinModelExportState<?>>{
	private static final Logger logger = Logger.getLogger(NomStatusMapper.class);
	
	private BerlinModelExportMapping mapping = null; 
	private String dbTableName = "NomStatusRel";
	
		
	public static NomStatusMapper NewInstance(String cdmAttributeString, String dbAttributeString){
		return new NomStatusMapper(cdmAttributeString, dbAttributeString);
	}
	
	
	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 */
	private NomStatusMapper(String cdmAttributeString, String dbAttributeString) {
		super(cdmAttributeString, dbAttributeString, null);
	}


	private BerlinModelExportMapping getMapping(){
		String tableName = dbTableName;
		BerlinModelExportMapping mapping = new BerlinModelExportMapping(tableName);
//		NameFk
//		NomStatusFk
//		NomStatusRefFk
//		NomStatusRefDetailFk
//		DoubtfulFlag
		
		mapping.addMapper(DbObjectMapper.NewInstance("fromName", "NameFk"));
		mapping.addMapper(DbObjectMapper.NewInstance("toName", "NameFk1"));

		
		mapping.addMapper(MethodMapper.NewInstance("NomStatusFk", this.getClass(), "getNomStatusFk", NomenclaturalStatus.class));
		
		mapping.addMapper(DbObjectMapper.NewInstance("citation", "NomStatusRefFk"));
		mapping.addMapper(RefDetailMapper.NewInstance("citationMicroReference","citation", "NomStatusRefDetailFk"));
		
		mapping.addMapper(CreatedAndNotesMapper.NewInstance());
		
		NomenclaturalStatus r = null;
		
		
		return mapping;
	}

	
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#initialize(java.sql.PreparedStatement, eu.etaxonomy.cdm.io.berlinModel.out.mapper.IndexCounter, eu.etaxonomy.cdm.io.berlinModel.out.DbExportState)
	 */
	@Override
	public void initialize(PreparedStatement stmt, IndexCounter index,BerlinModelExportState<?> state, String tableName) {
		mapping = getMapping();
		try {
			mapping.initialize(getState());
		} catch (SQLException e) {
			logger.error("SQLException in NomStatusMapper.initialize()");
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValue()
	 */
	@Override
	protected Object getValue(CdmBase cdmBase) {
		try {
			mapping.invoke(cdmBase);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		String value = (String)super.getValue(cdmBase);
		return null;
	}

	protected Integer getId(CdmBase cdmBase){
		BerlinModelExportConfigurator config = getState().getConfig();
		if (false && config.getIdType() == BerlinModelExportConfigurator.IdType.CDM_ID){
			return cdmBase.getId();
		}else{
			Integer id = getState().getDbId(cdmBase);
			return id;
		}
	}	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValueType()
	 */
	@Override
	protected int getSqlType() {
		return Types.INTEGER;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
	public Class<?> getTypeClass() {
		return String.class;
	}

}
