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
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.config.Configuration;
import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelUserImportValidator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.User;


/**
 * @author a.mueller
 * @created 20.03.2008
 */
@Component
public class BerlinModelUserImport extends BerlinModelImportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelUserImport.class);

	public static final String NAMESPACE = "User";
	
	private static int modCount = 100;
	private static final String dbTableName = "webAuthorisation";
	private static final String pluralString = "Users";
	
	public BerlinModelUserImport(){
		super(dbTableName, pluralString);
	}
	
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		IOValidator<BerlinModelImportState> validator = new BerlinModelUserImportValidator();
		return validator.validate(state);
	}
	
	@Override
	protected void doInvoke(BerlinModelImportState state){
		boolean success = true;
		
		BerlinModelImportConfigurator config = state.getConfig();
		Source source = config.getSource();
		String dbAttrName;
		String cdmAttrName;

		logger.info("start make "+pluralString+" ...");
		
		//get data from database
		String strQuery = 
				" SELECT *  " +
                " FROM "+dbTableName+" " ;
		ResultSet rs = source.getResultSet(strQuery) ;
		Collection<User> users = new ArrayList<User>();
		
		int i = 0;
		//for each reference
		try{
			while (rs.next()){
				try{
					if ((i++ % modCount ) == 0 && i!= 1 ){ logger.info(""+pluralString+" handled: " + (i-1));}
					
					//
					String username = rs.getString("Username");
					String pwd = rs.getString("Password");
					
					if (username != null){
						username = username.trim();
					}
					User user = User.NewInstance(username, pwd);
					
					Person person = Person.NewInstance();
					user.setPerson(person);
					
					/* 
					 * this is a crucial call, otherwise the password will not be set correctly
					 * and the whole authentication will not work 
					 */
					authenticate(Configuration.adminLogin, Configuration.adminPassword);
					getUserService().createUser(user);
					
					
					dbAttrName = "RealName";
					cdmAttrName = "TitleCache";
					success &= ImportHelper.addStringValue(rs, person, dbAttrName, cdmAttrName, false);
	
					users.add(user);
					state.putUser(username, user);
				}catch(Exception ex){
					logger.error(ex.getMessage());
					ex.printStackTrace();
					state.setUnsuccessfull();
					success = false;
				}
			} //while rs.hasNext()
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			state.setUnsuccessfull();
			return;
		}
			
		logger.info("save " + i + " "+pluralString + " ...");
		getUserService().save(users);

		logger.info("end make "+pluralString+" ..." + getSuccessString(success));;
		if (!success){
			state.setUnsuccessfull();
		}
		return;
	}

	
	@Override
	protected boolean isIgnore(BerlinModelImportState state){
		return ! state.getConfig().isDoUser();
	}

	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
		return null; // not needed at the moment
	}

	@Override
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState state) {
		return true;  // not needed at the moment
	}

	@Override
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs, BerlinModelImportState state) {
		return null; //not needed at the moment
	}

}
