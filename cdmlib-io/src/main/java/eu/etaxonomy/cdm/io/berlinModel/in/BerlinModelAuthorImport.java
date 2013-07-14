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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.berlinModel.in.validation.BerlinModelAuthorImportValidator;
import eu.etaxonomy.cdm.io.common.IOValidator;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.strategy.parser.TimePeriodParser;


/**
 * Supported attributes:
 * - AuthorId, Abbrev, FirstName, LastName, Dates, AreaOfInterest, NomStandard, createUpdateNotes
 * 
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelAuthorImport extends BerlinModelImportBase {
	private static final boolean BLANK_TO_NULL = true;

	private static final Logger logger = Logger.getLogger(BerlinModelAuthorImport.class);

	public static final String NAMESPACE = "Author";
	
	private static int recordsPerLog = 5000;
	private static final String dbTableName = "Author";
	private static final String pluralString = "Authors";
	
	public BerlinModelAuthorImport(){
		super(dbTableName, pluralString);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getIdQuery()
	 */
	@Override
	protected String getIdQuery(BerlinModelImportState state) {
		String result = " SELECT authorId FROM " + getTableName();
		if (StringUtils.isNotBlank(state.getConfig().getAuthorFilter())){
			result += " WHERE " +  state.getConfig().getAuthorFilter(); 
		} 
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportBase#getRecordQuery(eu.etaxonomy.cdm.io.berlinModel.in.BerlinModelImportConfigurator)
	 */
	@Override
	protected String getRecordQuery(BerlinModelImportConfigurator config) {
		String strRecordQuery = 
			" SELECT * " +
            " FROM " + dbTableName + " " + 
            " WHERE authorId IN ( " + ID_LIST_TOKEN + " )";
		return strRecordQuery;
	}


	/**
	 * @param partitioner
	 * @throws SQLException 
	 */
	//TODO public ??
	public boolean doPartition(ResultSetPartitioner partitioner, BerlinModelImportState state)  {
		String dbAttrName;
		String cdmAttrName;
		Map<Integer, Person> personMap = new HashMap<Integer, Person>();
		
		boolean success = true;
		ResultSet rs = partitioner.getResultSet();
		try{
			//for each author
			while (rs.next()){
					
			//	partitioner.doLogPerLoop(recordsPerLog, pluralString);
				
					//create Agent element
					int authorId = rs.getInt("AuthorId");
					
					Person author = Person.NewInstance();
					
					dbAttrName = "Abbrev";
					cdmAttrName = "nomenclaturalTitle";
					success &= ImportHelper.addStringValue(rs, author, dbAttrName, cdmAttrName, BLANK_TO_NULL);

					dbAttrName = "FirstName";
					cdmAttrName = "firstname";
					success &= ImportHelper.addStringValue(rs, author, dbAttrName, cdmAttrName, BLANK_TO_NULL);
					
					dbAttrName = "LastName";
					cdmAttrName = "lastname";
					success &= ImportHelper.addStringValue(rs, author, dbAttrName, cdmAttrName, BLANK_TO_NULL);
					
					String dates = rs.getString("dates");
					if (dates != null){
						dates.trim();
						TimePeriod lifespan = TimePeriodParser.parseString(dates);
						author.setLifespan(lifespan);
					}
					
//				//AreaOfInterest
					String areaOfInterest = rs.getString("AreaOfInterest");
					if (CdmUtils.isNotEmpty(areaOfInterest)){
						Extension datesExtension = Extension.NewInstance(author, areaOfInterest, ExtensionType.AREA_OF_INTREREST());
					}

					//nomStandard
					String nomStandard = rs.getString("NomStandard");
					if (CdmUtils.isNotEmpty(nomStandard)){
						Extension nomStandardExtension = Extension.NewInstance(author, nomStandard, ExtensionType.NOMENCLATURAL_STANDARD());
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
					if (StringUtils.isNotBlank(initials)){
						Extension initialsExtension = Extension.NewInstance(author, initials, ExtensionType.ABBREVIATION());
					}

					//created, notes
				doIdCreatedUpdatedNotes(state, author, rs, authorId, NAMESPACE);

				personMap.put(authorId, author);
	
			} //while rs.hasNext()
			//logger.info("save " + i + " "+pluralString + " ...");
			getAgentService().save((Collection)personMap.values());
			
				}catch(Exception ex){
					logger.error(ex.getMessage());
					ex.printStackTrace();
					success = false;
				}
		return success;
		}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getRelatedObjectsForPartition(java.sql.ResultSet)
	 */
	public Map<Object, Map<String, ? extends CdmBase>> getRelatedObjectsForPartition(ResultSet rs)  {
		Map<Object, Map<String, ? extends CdmBase>> result = new HashMap<Object, Map<String, ? extends CdmBase>>();
		// no related objects exist
		return result;
	}
			

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(BerlinModelImportState state){
		IOValidator<BerlinModelImportState> validator = new BerlinModelAuthorImportValidator();
		return validator.validate(state);
	}
	


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelImportState state){
		return ! state.getConfig().isDoAuthors();
	}

}
