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

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.TimePeriod;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelAuthorImport extends BerlinModelImportBase {
	private static final Logger logger = Logger.getLogger(BerlinModelAuthorImport.class);

	private static int modCount = 5000;
	private static final String dbTableName = "Author";
	private static final String pluralString = "Authors";
	
	public BerlinModelAuthorImport(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IImportConfigurator config){
		boolean result = true;
		logger.warn("Checking for "+pluralString+" not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	protected boolean doInvoke(BerlinModelImportState state){
		
		MapWrapper<Person> personMap = (MapWrapper<Person>)state.getStore(ICdmIO.PERSON_STORE);
		
		BerlinModelImportConfigurator config = state.getConfig();
		Source source = config.getSource();
		String dbAttrName;
		String cdmAttrName;

		logger.info("start make "+pluralString+" ...");
		boolean success = true ;
		
		
		
		//get data from database
		String strQuery = 
				" SELECT *  " +
                " FROM "+dbTableName+" " ;
		ResultSet rs = source.getResultSet(strQuery) ;
		String namespace = dbTableName;
		
		int i = 0;
		//for each reference
		try{
			while (rs.next()){
				
				if ((i++ % modCount ) == 0 && i!= 1 ){ logger.info(""+pluralString+" handled: " + (i-1));}
				
				//create Agent element
				int authorId = rs.getInt("AuthorId");
				
				Person author = Person.NewInstance();
				
				dbAttrName = "Abbrev";
				cdmAttrName = "nomenclaturalTitle";
				success &= ImportHelper.addStringValue(rs, author, dbAttrName, cdmAttrName);

				dbAttrName = "FirstName";
				cdmAttrName = "firstname";
				success &= ImportHelper.addStringValue(rs, author, dbAttrName, cdmAttrName);
				
				dbAttrName = "LastName";
				cdmAttrName = "lastname";
				success &= ImportHelper.addStringValue(rs, author, dbAttrName, cdmAttrName);
				
				String dates = rs.getString("dates");
				if (dates != null){
					//dates = dates.replace("fl.", "");
					//dates = dates.replace("c.", "");
					dates.trim();
					TimePeriod lifespan = TimePeriod.parseString(dates);
					author.setLifespan(lifespan);
				}
				
//				//AreaOfInterest
				String areaOfInterest = rs.getString("AreaOfInterest");
				Extension datesExtension = Extension.NewInstance(author, areaOfInterest, ExtensionType.AREA_OF_INTREREST());

				//nomStandard
				String nomStandard = rs.getString("NomStandard");
				Extension nomStandardExtension = Extension.NewInstance(author, nomStandard, ExtensionType.NOMENCLATURAL_STANDARD());
				
				//initials
				String initials = null;
				for (int j = 1; j <= rs.getMetaData().getColumnCount(); j++){
					String label = rs.getMetaData().getColumnLabel(j);
					if (label.equalsIgnoreCase("Initials") || label.equalsIgnoreCase("Kürzel")){
						initials = rs.getString(j);
						break;
					}
				}
				Extension initialsExtension = Extension.NewInstance(author, initials, ExtensionType.ABBREVIATION());


				//created, notes
				doIdCreatedUpdatedNotes(config, author, rs, authorId, namespace);

				personMap.put(authorId, author);
			} //while rs.hasNext()
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}

			
		logger.info("save " + i + " "+pluralString + " ...");
		getAgentService().saveAgentAll(personMap.objects());

		logger.info("end make "+pluralString+" ...");
		return success;
		
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IImportConfigurator config){
		return ! config.isDoAuthors();
	}

}
