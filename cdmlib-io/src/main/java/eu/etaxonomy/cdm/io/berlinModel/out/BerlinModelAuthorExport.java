/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.berlinModel.out;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.berlinModel.out.mapper.CreatedAndNotesMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbExtensionMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbStringMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbTimePeriodMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.IdMapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ExtensionType;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelAuthorExport extends BerlinModelExportBase<Person> {
	private static final Logger logger = Logger.getLogger(BerlinModelAuthorExport.class);

	private static int modCount = 5000;
	private static final String dbTableName = "Author";
	private static final String pluralString = "Authors";
	private static final Class<? extends CdmBase> standardMethodParameter = Person.class;
	public BerlinModelAuthorExport(){
		super();
	}
	

	@Override
	protected boolean doCheck(BerlinModelExportState state){
		boolean result = true;
		logger.warn("Checking for "+pluralString+" not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	private BerlinModelExportMapping getMapping(){
		String tableName = dbTableName;
		BerlinModelExportMapping mapping = new BerlinModelExportMapping(tableName);
		mapping.addMapper(IdMapper.NewInstance("AuthorId"));
		mapping.addMapper(DbStringMapper.NewInstance("nomenclaturalTitle", "Abbrev"));
		mapping.addMapper(DbStringMapper.NewInstance("firstname", "FirstName"));
		mapping.addMapper(DbStringMapper.NewInstance("lastname", "LastName"));
		mapping.addMapper(DbTimePeriodMapper.NewInstance("lifespan", "Dates"));
		mapping.addMapper(DbExtensionMapper.NewInstance(ExtensionType.NOMENCLATURAL_STANDARD(), "NomStandard"));
		mapping.addMapper(DbExtensionMapper.NewInstance(ExtensionType.AREA_OF_INTREREST(), "AreaOfInterest"));
		mapping.addMapper(DbExtensionMapper.NewInstance(ExtensionType.ABBREVIATION(), "Initials"));
//		mapping.addMapper(DbExtensionMapper.NewInstance(ExtensionType.ABBREVIATION(),Kürzel")); //Initials used instead
//		mapping.addMapper(DbExtensionMapper.NewInstance(ExtensionType.ABBREVIATION(), "DraftKürz")); //Initials used instead
		mapping.addMapper(CreatedAndNotesMapper.NewInstance());
		
		return mapping;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportBase#doInvoke(eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportState)
	 */
	@Override
	protected boolean doInvoke(BerlinModelExportState state) {
		try{
			BerlinModelExportConfigurator bmeConfig = (BerlinModelExportConfigurator)state.getConfig();
			
			logger.info("start make "+pluralString+" ...");
			boolean success = true ;
			doDelete(bmeConfig);
			
			TransactionStatus txStatus = startTransaction(true);
			Class<Person> clazz = Person.class;
			List<AgentBase> persons = getAgentService().list(clazz, 100000000, 0, null, null);
			
			BerlinModelExportMapping mapping = getMapping();
			mapping.initialize(state);
			
			logger.info("save "+pluralString+" ...");
			int count = 0;
			for (AgentBase<?> agent : persons){
				doCount(count++, modCount, pluralString);
				if (agent instanceof Person){
					success &= mapping.invoke(agent);
				}
			}
			
			commitTransaction(txStatus);
			logger.info("end make "+pluralString+"  ..." + getSuccessString(success));
			return success;
		}catch(SQLException e){
			e.printStackTrace();
			logger.error(e.getMessage());
			return false;
		}
	}
	
	protected boolean doDelete(BerlinModelExportConfigurator config){
		
		//TODO make more generic for all BerlinModelExport classes
		String sql;
		Source destination =  config.getDestination();
		//RelPTaxon
		sql = "DELETE FROM RelPTaxon";
		destination.setQuery(sql);
		destination.update(sql);
		//Fact
		sql = "DELETE FROM Fact";
		destination.setQuery(sql);
		destination.update(sql);
		//PTaxon
		sql = "DELETE FROM PTaxon";
		destination.setQuery(sql);
		destination.update(sql);
		
		//NameHistory
		sql = "DELETE FROM NameHistory";
		destination.setQuery(sql);
		destination.update(sql);
		//RelName
		sql = "DELETE FROM RelName";
		destination.setQuery(sql);
		destination.update(sql);
		//NomStatusRel
		sql = "DELETE FROM NomStatusRel";
		destination.setQuery(sql);
		destination.update(sql);
		//Name
		sql = "DELETE FROM Name";
		destination.setQuery(sql);
		destination.update(sql);
		//RefDetail
		sql = "DELETE FROM RefDetail";
		destination.setQuery(sql);
		destination.update(sql);
		//Reference
		sql = "DELETE FROM Reference";
		destination.setQuery(sql);
		destination.update(sql);
		//AuthorTeamSequence
		sql = "DELETE FROM AuthorTeamSequence";
		destination.setQuery(sql);
		destination.update(sql);
		//AuthorTeam
		sql = "DELETE FROM AuthorTeam";
		destination.setQuery(sql);
		destination.update(sql);
		//Author
		sql = "DELETE FROM Author";
		destination.setQuery(sql);
		destination.update(sql);
		return true;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IIoConfigurator)
	 */
	@Override
	protected boolean isIgnore(BerlinModelExportState state) {
		return ! state.getConfig().isDoAuthors();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportBase#getStandardMethodParameter()
	 */
	@Override
	public Class<? extends CdmBase> getStandardMethodParameter() {
		return standardMethodParameter;
	}



}
