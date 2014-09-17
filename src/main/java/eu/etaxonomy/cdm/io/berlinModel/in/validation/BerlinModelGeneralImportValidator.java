/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.berlinModel.in.validation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator;
import eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportState;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;


/**
 * For validating general consistencies like existence of tables, etc.
 * @author a.mueller
 * @created 10.06.2009
 */
@Component
public class BerlinModelGeneralImportValidator extends BerlinModelImportBase implements IOValidator<BerlinModelImportState> {
	private static final Logger logger = Logger.getLogger(BerlinModelGeneralImportValidator.class);

	public BerlinModelGeneralImportValidator(){
		super(null, null);
	}
	
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		return validate(state);
	}
	

	@Override
	public boolean validate(BerlinModelImportState state) {
		boolean result = true;
		BerlinModelImportConfigurator bmiConfig = state.getConfig();
		result &= checkRelAuthorsExist(bmiConfig);
		result &= checkRelReferenceExist(bmiConfig);
		
		return result;
	}
	
	protected void doInvoke(BerlinModelImportState state){
		//do nothing
		return;
		
	}
	
	private boolean checkRelAuthorsExist(BerlinModelImportConfigurator config){
		
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
	
	private boolean checkRelReferenceExist(BerlinModelImportConfigurator config){
		
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

	
	@Override
	protected boolean isIgnore(BerlinModelImportState state){
		return false;
	}

	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState state) {
		return true;  // not needed
	}


	@Override
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs, BerlinModelImportState state) {
		return null;  // not needed
	}


}
