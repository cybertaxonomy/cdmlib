/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.pesi.erms.validation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMapping;
import eu.etaxonomy.cdm.io.pesi.erms.ErmsImportBase;
import eu.etaxonomy.cdm.io.pesi.erms.ErmsImportConfigurator;
import eu.etaxonomy.cdm.io.pesi.erms.ErmsImportState;
import eu.etaxonomy.cdm.model.common.CdmBase;


/**
 * For validating general consistencies like existence of tables, etc.
 * @author a.mueller
 * @created 10.06.2009
 * @version 1.0
 */
@Component
public class ErmsGeneralImportValidator extends ErmsImportBase<CdmBase> implements IOValidator<ErmsImportState> {
	private static final Logger logger = Logger.getLogger(ErmsGeneralImportValidator.class);

	public ErmsGeneralImportValidator(){
		super(null, null, null);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(ErmsImportState state){
		return validate(state);
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.IOValidator#validate(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	public boolean validate(ErmsImportState state) {
		boolean result = true;
		ErmsImportConfigurator config = state.getConfig();
//		result &= checkRelAuthorsExist(config);
//		result &= checkRelReferenceExist(config);
		
		return result;
	}
	
	protected void doInvoke(ErmsImportState state){
		//do nothing
		return;
		
	}
	
	private boolean checkRelAuthorsExist(ErmsImportConfigurator config){
		
		try {
			boolean result = true;
			Source source = config.getSource();
			String strQuery = "SELECT Count(*) as n " +
					" FROM RelAuthor "
					;
			ResultSet rs = source.getResultSet(strQuery);
			rs.next();
			int count = rs.getInt("n");
			if (count > 0){
				System.out.println("========================================================");
				logger.warn("There are "+count+" RelAuthors, but RelAuthors are not implemented for CDM yet.");
				System.out.println("========================================================");
			}
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

	}
	
	private boolean checkRelReferenceExist(ErmsImportConfigurator config){
		
		try {
			boolean result = true;
			Source source = config.getSource();
			String strQuery = "SELECT Count(*) as n " +
					" FROM RelReference "
					;
			ResultSet rs = source.getResultSet(strQuery);
			rs.next();
			int count = rs.getInt("n");
			if (count > 0){
				System.out.println("========================================================");
				logger.warn("There are "+count+" RelReferences, but RelReferences are not implemented for CDM yet.");
				System.out.println("========================================================");
			}
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}

	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getTableName()
	 */
	@Override
	protected String getTableName() {
		return null;  //not needed
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getPluralString()
	 */
	@Override
	public String getPluralString() {
		return null; //not needed
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(ErmsImportState state){
		return false;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(ErmsImportConfigurator config) {
		return null;  // not needed
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#doPartition(eu.etaxonomy.cdm.io.berlinModel.in.ResultSetPartitioner, eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState)
	 */
	public boolean doPartition(ResultSetPartitioner partitioner, ErmsImportState state) {
		return true;  // not needed
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs) {
		return null;  // not needed
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.IMappingImport#createObject(java.sql.ResultSet)
	 */
	public CdmBase createObject(ResultSet rs, ErmsImportState state) throws SQLException {
		return null;  //not needed
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.erms.ErmsImportBase#getMapping()
	 */
	@Override
	protected DbImportMapping<?, ?> getMapping() {
		// TODO Auto-generated method stub
		return null;
	}



}
