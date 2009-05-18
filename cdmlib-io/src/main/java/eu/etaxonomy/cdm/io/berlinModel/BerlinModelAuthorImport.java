/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.berlinModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IImportConfigurator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.MapWrapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
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
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IImportConfigurator, eu.etaxonomy.cdm.api.application.CdmApplicationController, java.util.Map)
	 */
	@Override
	protected boolean doInvoke(IImportConfigurator config, 
			Map<String, MapWrapper<? extends CdmBase>> stores){ 

		MapWrapper<Person> personMap = (MapWrapper<Person>)stores.get(ICdmIO.PERSON_STORE);
		
		BerlinModelImportConfigurator bmiConfig = (BerlinModelImportConfigurator)config;
		Source source = bmiConfig.getSource();
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
					try {
						TimePeriod lifespan = TimePeriod.parseString(dates);
						author.setLifespan(lifespan);
					} catch (IllegalArgumentException e) {
						logger.warn("Lifespan could not be parsed: " + dates);
						Annotation annotation = Annotation.NewInstance("Dates: " + dates,Language.DEFAULT());
						author.addAnnotation(annotation);
					}
				}
				
				String areaOfInterest = rs.getString("AreaOfInterest");
				//AreaOfInterest
				if (! CdmUtils.Nz(areaOfInterest).equals("")){
					Annotation annotation = Annotation.NewInstance("Area of Interest: " + areaOfInterest,Language.DEFAULT());
					author.addAnnotation(annotation);
				}
				//nomStandard
				String nomStandard = rs.getString("NomStandard");
				if (! CdmUtils.Nz(nomStandard).equals("")){
					Annotation annotation = Annotation.NewInstance("NomStandard: " + nomStandard,Language.DEFAULT());
					author.addAnnotation(annotation);
				}
				
				//initials
				String initials = null;
				for (int j = 1; j <= rs.getMetaData().getColumnCount(); j++){
					String label = rs.getMetaData().getColumnLabel(j);
					if (label.equalsIgnoreCase("Initials") || label.equalsIgnoreCase("Kürzel")){
						initials = rs.getString(j);
						break;
					}
				}
				if (! CdmUtils.Nz(initials).equals("")){
					Annotation annotation = Annotation.NewInstance("Initials: " + initials,Language.DEFAULT());
					author.addAnnotation(annotation);
				}

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
