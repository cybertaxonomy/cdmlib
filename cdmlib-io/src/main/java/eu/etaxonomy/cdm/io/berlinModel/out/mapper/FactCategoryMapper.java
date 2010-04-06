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

import eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportMapping;
import eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportState;
import eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class FactCategoryMapper extends DbSingleAttributeExportMapperBase<BerlinModelExportState> implements IDbExportMapper<BerlinModelExportState>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FactCategoryMapper.class);
	
	private PreparedStatement preparedStatement;
	private BerlinModelExportMapping mapping = null; 
	private String dbTableName = "NomStatusRel";
//	protected BerlinModelExportState<?> state;

	public static FactCategoryMapper NewInstance(String cdmAttributeString, String dbAttributeString){
		return new FactCategoryMapper(cdmAttributeString, dbAttributeString);
	}

	private BerlinModelExportMapping getMapping(){
		boolean doExecute = false;
		String tableName = dbTableName;
		BerlinModelExportMapping mapping = new BerlinModelExportMapping(tableName);

		mapping.addMapper(MethodMapper.NewInstance("NomStatusFk", this.getClass(), "getNomStatusFk", NomenclaturalStatus.class));
		mapping.addMapper(DbObjectMapper.NewInstance("citation", "NomStatusRefFk"));
		mapping.addMapper(RefDetailMapper.NewInstance("citationMicroReference","citation", "NomStatusRefDetailFk"));
		
		mapping.addMapper(CreatedAndNotesMapper.NewInstance());
		//TODO
//		DoubtfulFlag
		
		return mapping;
	}
	
	
//	public static RefDetailMapper NewInstance(String cdmAttributeString, String dbAttributeString){
//		return new RefDetailMapper();
//	}

	
	/**
	 * @param dbAttributString
	 * @param cdmAttributeString
	 */
	private FactCategoryMapper(String cdmAttributeString, String dbAttributeString) {
		super(cdmAttributeString, dbAttributeString, null);
	}

	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#initialize(java.sql.PreparedStatement, eu.etaxonomy.cdm.io.berlinModel.out.mapper.IndexCounter, eu.etaxonomy.cdm.io.berlinModel.out.DbExportState)
	 */
	@Override
	public void initialize(PreparedStatement stmt, IndexCounter index,BerlinModelExportState state, String tableName) {
		super.initialize(stmt, index, state, tableName);
		mapping = getMapping();

//		String inRefSql = "INSERT INTO FactCategory (FactCategoryId, FactCategory , " + 
//	 		" MaxFactNumber , RankRestrictionFk" +
//	 		" VALUES (?,?,?,?)";    
//		Connection con = getState().getConfig().getDestination().getConnection();
		try {
			mapping.initialize(state);
			mapping.initialize(state);
//			preparedStatement = con.prepareStatement(inRefSql);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValue()
	 */
	//@Override
	public Object invoke(CdmBase cdmBase, boolean xxx) {
		String value = (String)super.getValue(cdmBase);
		boolean isBoolean = false;
		Feature feature = (Feature)ImportHelper.getValue(cdmBase, getSourceAttribute(), isBoolean, true);
		Object result = makeRow(feature);
//		getState().getConfig().getCdmAppController().commitTransaction(tx);
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValue()
	 */
	@Override
	protected Object getValue(CdmBase cdmBase) {
		String value = (String)super.getValue(cdmBase);
		boolean isBoolean = false;
		Feature feature = (Feature)ImportHelper.getValue(cdmBase, getSourceAttribute(), isBoolean, true);
		Object result = makeRow(feature);
//		getState().getConfig().getCdmAppController().commitTransaction(tx);
		return result;
	}

	
	protected Integer makeRow(Feature feature){
		if (feature == null){
			return null;		
		}
		Integer factCategoryId = getState().getNextFactCategoryId();
		String factCategory = feature.getLabel();
		Integer maxFactNumber = null;
		Integer RankRestrictionFk = null;
		
		try {
			preparedStatement.setInt(1, factCategoryId);
			preparedStatement.setString(2, factCategory);
			preparedStatement.setNull(3, Types.INTEGER) ;//.setString(3, maxFactNumber);
			preparedStatement.setNull(4, Types.INTEGER) ;//.setString(4, RankRestrictionFk);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return factCategoryId;
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
