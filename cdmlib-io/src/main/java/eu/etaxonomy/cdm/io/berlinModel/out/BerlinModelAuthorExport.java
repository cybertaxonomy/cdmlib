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
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbStringMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbTimePeriodMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.IdMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.MethodMapper;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.common.CdmBase;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelAuthorExport extends BerlinModelExportBase<Person> {
	private static final Logger logger = Logger.getLogger(BerlinModelAuthorExport.class);

	private static int modCount = 1000;
	private static final String dbTableName = "Author";
	private static final String pluralString = "Authors";
	private static final Class<? extends CdmBase> standardMethodParameter = Person.class;
	public BerlinModelAuthorExport(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IExportConfigurator config){
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

//TODO		
//		mapping.addMapper(DbStringMapper.NewInstance("", "NomStandard"));
//		mapping.addMapper(DbStringMapper.NewInstance("", "Kürzel"));
//		mapping.addMapper(DbStringMapper.NewInstance("", "DraftKürz"));
//		mapping.addMapper(DbStringMapper.NewInstance("", "Initials"));

		mapping.addMapper(CreatedAndNotesMapper.NewInstance());
		
		return mapping;
	}
	
	
	protected boolean doInvoke(BerlinModelExportState<BerlinModelExportConfigurator> state){
		//MapWrapper<AgentBase> teamMap = (MapWrapper<AgentBase>)stores.get(ICdmIO.AUTHOR_STORE);
		//MapWrapper<AgentBase> teamMap = (MapWrapper<AgentBase>)stores.get(ICdmIO.AUTHOR_STORE);
		
		try{
			BerlinModelExportConfigurator bmeConfig = (BerlinModelExportConfigurator)state.getConfig();
			
			logger.info("start make "+pluralString+" ...");
			boolean success = true ;
			doDelete(bmeConfig);
			
			TransactionStatus txStatus = startTransaction(true);
			Class<Person> clazz = Person.class;
			List<? extends Person> persons = getAgentService().list(clazz, 100000000, 0);
			
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
			logger.info("end make "+pluralString+"  ...");
			return success;
		}catch(SQLException e){
			e.printStackTrace();
			logger.error(e.getMessage());
			return false;
		}
	}
	
	protected boolean doDelete(BerlinModelExportConfigurator config){
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
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IExportConfigurator config){
		return ! ((BerlinModelExportConfigurator)config).isDoAuthors();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportBase#getStandardMethodParameter()
	 */
	@Override
	public Class<? extends CdmBase> getStandardMethodParameter() {
		return standardMethodParameter;
	}

}
