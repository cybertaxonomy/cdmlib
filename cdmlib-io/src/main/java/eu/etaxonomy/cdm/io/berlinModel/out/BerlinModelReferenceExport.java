/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.berlinModel.out;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.CreatedAndNotesMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbBooleanMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbObjectMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbStringMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbTimePeriodMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.IdMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.MethodMapper;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.BookSection;
import eu.etaxonomy.cdm.model.reference.InProceedings;
import eu.etaxonomy.cdm.model.reference.PrintedUnitBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelReferenceExport extends BerlinModelExportBase<ReferenceBase> {
	private static final Logger logger = Logger.getLogger(BerlinModelReferenceExport.class);

	private static int modCount = 1000;
	private static final String dbTableName = "Reference";
	private static final String pluralString = "references";
	private static final Class<? extends CdmBase> standardMethodParameter = StrictReferenceBase.class;

	public BerlinModelReferenceExport(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IExportConfigurator config){
		boolean result = true;
		logger.warn("Checking for Authors not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	private BerlinModelExportMapping getMapping(){
		String tableName = dbTableName;
		BerlinModelExportMapping mapping = new BerlinModelExportMapping(tableName);
		mapping.addMapper(IdMapper.NewInstance("RefId"));
		mapping.addMapper(MethodMapper.NewInstance("RefCategoryFk", this));
		mapping.addMapper(MethodMapper.NewInstance("RefCache", this));
		mapping.addMapper(MethodMapper.NewInstance("NomRefCache", this));
		mapping.addMapper(DbBooleanMapper.NewFalseInstance("isProtectedTitleCache","PreliminaryFlag"));

		mapping.addMapper(DbObjectMapper.NewInstance("authorTeam", "NomAuthorTeamFk"));
		mapping.addMapper(MethodMapper.NewInstance("RefAuthorString", this));
		
		mapping.addMapper(DbStringMapper.NewInstance("title", "Title"));
		mapping.addMapper(DbStringMapper.NewInstance("title", "NomTitleAbbrev"));
		
		mapping.addMapper(DbStringMapper.NewFacultativeInstance("edition", "Edition"));
		mapping.addMapper(DbStringMapper.NewFacultativeInstance("volume", "Volume"));
		mapping.addMapper(DbStringMapper.NewFacultativeInstance("series", "Series"));
		mapping.addMapper(DbStringMapper.NewFacultativeInstance("pages", "PageString"));
		mapping.addMapper(DbStringMapper.NewFacultativeInstance("isbn", "ISBN"));
		mapping.addMapper(DbStringMapper.NewFacultativeInstance("issn", "ISSN"));
		mapping.addMapper(DbTimePeriodMapper.NewInstance("datePublished", "RefYear"));
		
		mapping.addMapper(CreatedAndNotesMapper.NewInstance());
          
//		        ,[Series] ??
//		        ,[DateString]
//		        ,[URL]
//		        ,[ExportDate]
//		        ,[PublicationTown]
//		        ,[Publisher]
//		        ,[ThesisFlag]
//		        ,[RefDepositedAt]
//		        ,[InformalRefCategory]
//		        ,[IsPaper]
//		        ,[RefSourceFk]
//		        ,[IdInSource]
//		        ,[NomStandard]
		
		       
		return mapping;
	}
	
	protected boolean doInvoke(BerlinModelExportState<BerlinModelExportConfigurator> state){
		try{
			logger.info("start make "+pluralString+" ...");
			boolean success = true ;
			//Prepare InRefStatement
			String inRefSql = "UPDATE Reference SET InRefFk = ? WHERE RefId = ?"; 
			Connection con = state.getConfig().getDestination().getConnection();
			PreparedStatement stmt = con.prepareStatement(inRefSql);

			doDelete(state);
			
			TransactionStatus txStatus = startTransaction(true);
			
			List<ReferenceBase> list = getReferenceService().list(100000000, 0);
			
			BerlinModelExportMapping mapping = getMapping();
			mapping.initialize(state);
			
			int count = 0;
			for (ReferenceBase<?> ref : list){
				doCount(count++, modCount, pluralString);
				success &= mapping.invoke(ref);
			}
			logger.info("start make inRefences ...");
			count = 0;
			for (ReferenceBase<?> ref : list){
				doCount(count++, modCount, "inReferences");
				success &= invokeInRef(ref, state, stmt);
			}
			
			commitTransaction(txStatus);
			logger.info("end make "+pluralString+" ...");
			
			return success;
		}catch(SQLException e){
			e.printStackTrace();
			logger.error(e.getMessage());
			return false;
		}
	}

	protected boolean invokeInRef(ReferenceBase ref, BerlinModelExportState<BerlinModelExportConfigurator> state, PreparedStatement stmt) {
		if (ref == null){
			return true;
		}else{
			ReferenceBase<?> inRef = getInRef(ref);
			if (inRef == null){
				return true;
			}else{
				Integer inRefId = state.getDbId(inRef);
				Integer refId = state.getDbId(ref);
				try {
					stmt.setInt(1, inRefId);
					stmt.setInt(2, refId);
					stmt.executeUpdate();
					return true;
				} catch (SQLException e) {
					logger.error("SQLException during inRef invoke for reference " + ref.getTitleCache() + ": " + e.getMessage());
					e.printStackTrace();
					return false;
				}
			}
		}
	}

	private ReferenceBase<?> getInRef(ReferenceBase<?> ref){
		ReferenceBase<?> inRef;
		if (ref instanceof Article){
			return ((Article)ref).getInJournal();
		}else if (ref instanceof BookSection){
			return ((BookSection)ref).getInBook();
		}else if (ref instanceof InProceedings){
			return ((InProceedings)ref).getInProceedings();
		}else if (ref instanceof PrintedUnitBase){
			return ((PrintedUnitBase)ref).getInSeries();
		}else{
			return null;
		}
		
	}
	
	protected boolean doDelete(BerlinModelExportState<BerlinModelExportConfigurator> state){
		BerlinModelExportConfigurator bmeConfig = state.getConfig();
		
		String sql;
		Source destination =  bmeConfig.getDestination();

		//RelReference
		sql = "DELETE FROM RelReference";
		destination.setQuery(sql);
		destination.update(sql);
		//References
		sql = "DELETE FROM Reference";
		destination.setQuery(sql);
		destination.update(sql);
		return true;
	}
		
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(IExportConfigurator config){
		return ! ((BerlinModelExportConfigurator)config).isDoTaxonNames();
	}
	
	//called by MethodMapper
	@SuppressWarnings("unused")
	private static Integer getRefCategoryFk(StrictReferenceBase<?> ref){
		return BerlinModelTransformer.ref2refCategoryId(ref);
	}
	
	//called by MethodMapper
	@SuppressWarnings("unused")
	private static String getRefCache(StrictReferenceBase<?> ref){
		if (ref.isProtectedTitleCache()){
			return ref.getTitleCache();
		}else{
			return null;
		}
	}

	//called by MethodMapper
	@SuppressWarnings("unused")
	private static String getNomRefCache(StrictReferenceBase<?> ref){
		if (ref.isProtectedTitleCache()){
			return ref.getTitleCache();
		}else{
			return null;
		}
	}
	
	//called by MethodMapper
	@SuppressWarnings("unused")
	private static String getRefAuthorString(StrictReferenceBase<?> ref){
		if (ref == null){
			return null;
		}else{
			return (ref.getAuthorTeam() == null)? null: ref.getAuthorTeam().getTitleCache();
		}
	}

	
	//called by MethodMapper
	@SuppressWarnings("unused")
	private static Boolean getPreliminaryFlag(StrictReferenceBase<?> ref){
		if (ref.isProtectedTitleCache()){
			return true;
		}else{
			return false;
		}
	}
	
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportBase#getStandardMethodParameter()
	 */
	@Override
	public Class<? extends CdmBase> getStandardMethodParameter() {
		return standardMethodParameter;
	}
}
