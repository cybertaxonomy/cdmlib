/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.eflora.centralAfrica.ferns;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.io.common.CdmImportBase;
import eu.etaxonomy.cdm.io.common.ICdmIO;
import eu.etaxonomy.cdm.io.common.IPartitionedIO;
import eu.etaxonomy.cdm.io.common.ImportHelper;
import eu.etaxonomy.cdm.io.common.ResultSetPartitioner;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.EDITOR;
import eu.etaxonomy.cdm.io.common.mapping.DbImportMapping;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.AnnotationType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.MarkerType;
import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
public abstract class CentralAfricaFernsImportBase<CDM_BASE extends CdmBase> extends CdmImportBase<CentralAfricaFernsImportConfigurator, CentralAfricaFernsImportState> implements ICdmIO<CentralAfricaFernsImportState>, IPartitionedIO<CentralAfricaFernsImportState> {
	private static final Logger logger = Logger.getLogger(CentralAfricaFernsImportBase.class);
	
	public static final UUID ID_IN_SOURCE_EXT_UUID = UUID.fromString("23dac094-e793-40a4-bad9-649fc4fcfd44");
	
	protected static final String TAXON_NAMESPACE = "African_pteridophytes_Taxon";
	protected static final String NAME_NAMESPACE = "African_pteridophytes_Name";
	protected static final String HIGHER_TAXON_NAMESPACE = "African_pteridophytes_Higher_Taxon";
	
	private NonViralNameParserImpl parser = NonViralNameParserImpl.NewInstance();
	

	private String pluralString;
	private String dbTableName;
	//TODO needed?
	private Class cdmTargetClass;
	

	
	
	/**
	 * @param dbTableName
	 * @param dbTableName2 
	 */
	public CentralAfricaFernsImportBase(String pluralString, String dbTableName, Class cdmTargetClass) {
		this.pluralString = pluralString;
		this.dbTableName = dbTableName;
		this.cdmTargetClass = cdmTargetClass;
	}

	protected void doInvoke(CentralAfricaFernsImportState state){
		logger.info("start make " + getPluralString() + " ...");
		CentralAfricaFernsImportConfigurator config = state.getConfig();
		Source source = config.getSource();
			
		String strIdQuery = getIdQuery();
		String strRecordQuery = getRecordQuery(config);

		int recordsPerTransaction = config.getRecordsPerTransaction();
		try{
			ResultSetPartitioner partitioner = ResultSetPartitioner.NewInstance(source, strIdQuery, strRecordQuery, recordsPerTransaction);
			while (partitioner.nextPartition()){
				partitioner.doPartition(this, state);
			}
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			state.setUnsuccessfull();
			return;
		}
		
		logger.info("end make " + getPluralString() + " ... " + getSuccessString(true));
		return;
	}
	
	public boolean doPartition(ResultSetPartitioner partitioner, CentralAfricaFernsImportState state) {
		boolean success = true ;
		Set objectsToSave = new HashSet();
		
 		DbImportMapping<?, ?> mapping = getMapping();
		mapping.initialize(state, cdmTargetClass);
		
		ResultSet rs = partitioner.getResultSet();
		try{
			while (rs.next()){
				success &= mapping.invoke(rs,objectsToSave);
			}
		} catch (SQLException e) {
			logger.error("SQLException:" +  e);
			return false;
		}
	
		partitioner.startDoSave();
		getCommonService().save(objectsToSave);
		return success;
	}


	
	/**
	 * @return
	 */
	protected abstract DbImportMapping<?, ?> getMapping();
	
	/**
	 * @return
	 */
	protected abstract String getRecordQuery(CentralAfricaFernsImportConfigurator config);

	/**
	 * @return
	 */
	protected String getIdQuery(){
		String result = " SELECT id FROM " + getTableName();
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.in.IPartitionedIO#getPluralString()
	 */
	public String getPluralString(){
		return pluralString;
	}

	/**
	 * @return
	 */
	protected String getTableName(){
		return this.dbTableName;
	}
	
	
	/**
	 * Reads a foreign key field from the result set and adds its value to the idSet.
	 * @param rs
	 * @param teamIdSet
	 * @throws SQLException
	 */
	protected void handleForeignKey(ResultSet rs, Set<String> idSet, String attributeName)
			throws SQLException {
		Object idObj = rs.getObject(attributeName);
		if (idObj != null){
			String id  = String.valueOf(idObj);
			idSet.add(id);
		}
	}
	
	/**
	 * Returns true if i is a multiple of recordsPerTransaction
	 * @param i
	 * @param recordsPerTransaction
	 * @return
	 */
	protected boolean loopNeedsHandling(int i, int recordsPerLoop) {
		startTransaction();
		return (i % recordsPerLoop) == 0;
	}
	
	protected void doLogPerLoop(int count, int recordsPerLog, String pluralString){
		if ((count % recordsPerLog ) == 0 && count!= 0 ){ logger.info(pluralString + " handled: " + (count));}
	}
	
	
	
	protected void setAuthor(BotanicalName taxonName, ResultSet rs, String taxonNumber, boolean isHigherTaxon) throws SQLException {
		
		String authorsFull = null;
		String authorsAbbrev = null;
		if (! isHigherTaxon){
			authorsFull = rs.getString("Author/s - full");
			authorsAbbrev = rs.getString("Author/s - abbreviated");
		}

		Rank rank = taxonName.getRank();
		String authorString = null;
		if (rank != null){
			if (rank.equals(Rank.ORDER())){
				authorString =  rs.getString("Order name author");
			}else if (rank.equals(Rank.SUBORDER())){
				authorString = rs.getString("Suborder name author");
			}else if (rank.equals(Rank.FAMILY())){
				authorString = rs.getString("Family name author");
			}else if (rank.equals(Rank.SUBFAMILY())){
				authorString = rs.getString("Subfamily name author");
			}else if (rank.equals(Rank.TRIBE())){
				authorString = rs.getString("Tribus author");
			}else if (rank.equals(Rank.SUBTRIBE())){
				authorString = rs.getString("Subtribus author");
			}else if (rank.equals(Rank.SECTION_BOTANY())){
				authorString = rs.getString("Section name author");
			}else if (rank.equals(Rank.SUBSECTION_BOTANY())){
				authorString = rs.getString("Subsection author");
			}else if (rank.equals(Rank.GENUS())){
				authorString = rs.getString("Genus name author");
			}else if (rank.equals(Rank.SUBGENUS())){
				authorString = rs.getString("Subgenus name author");
			}else if (rank.equals(Rank.SERIES())){
				authorString = rs.getString("Series name author");
			}else if (rank.equals(Rank.SPECIES())){
				authorString =  rs.getString("Specific epithet author");
			}else if (rank.equals(Rank.SUBSPECIES())){
				authorString = rs.getString("Subspecies author");
			}else if (rank.equals(Rank.VARIETY())){
				authorString =  rs.getString("Variety name author");
			}else if (rank.equals(Rank.SUBVARIETY())){
				authorString = rs.getString("Subvariety author");
			}else if (rank.equals(Rank.FORM())){
				authorString = rs.getString("Forma name author");
			}else if (rank.equals(Rank.SUBFORM())){
				authorString = rs.getString("Subforma author");
			}else{
				logger.warn("Author string could not be defined");
				if (! isHigherTaxon){
					authorString = authorsAbbrev;
					if (StringUtils.isBlank(authorString)){
						logger.warn("Authors abbrev string could not be defined");
						authorString = authorsFull;	
					}
				}
			}
		}else{
			logger.warn(taxonNumber + ": Rank is null");
			authorString = authorsAbbrev;
			if (StringUtils.isBlank(authorString)){
				logger.warn(taxonNumber + ": Authors abbrev string could not be defined");
				authorString = authorsFull;	
			}
		}
		
		if (StringUtils.isNotBlank(authorString)){
			parser.handleAuthors(taxonName, taxonName.getNameCache().trim() + " " + authorString, authorString);
		}
		if (! isHigherTaxon){
			String combinationAuthor = taxonName.getCombinationAuthorTeam()==null ? "" :taxonName.getCombinationAuthorTeam().getNomenclaturalTitle();
			if (StringUtils.isNotBlank(authorsAbbrev) && ! authorsAbbrev.equalsIgnoreCase(combinationAuthor)){
				//it is expected that the fullAuthor and the abbrevAuthor are the combination authors but very often it is not
				logger.warn(taxonNumber + ": Rank author and abbrev author are not equal: " + authorString + "\t<-> " + combinationAuthor + "\t<-> " + authorsAbbrev);
			}
	//		if (StringUtils.isNotBlank(authorsFull) && ! authorsFull.equalsIgnoreCase(authorString)){
	//			logger.warn("Rank author and full author are not equal Rankauthor: " + authorString + ", full author " + authorsFull);
	//		}
		}
	}
	
	


	
}
