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
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.common.mapping.out.CdmDbExportMapping;
import eu.etaxonomy.cdm.io.common.mapping.out.CreatedAndNotesMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbBooleanMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbObjectMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbStringMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.DbTimePeriodMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.IExportTransformer;
import eu.etaxonomy.cdm.io.common.mapping.out.IdMapper;
import eu.etaxonomy.cdm.io.common.mapping.out.MethodMapper;
import eu.etaxonomy.cdm.model.common.CdmBase;
/*import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.BookSection;*/
import eu.etaxonomy.cdm.model.reference.IArticle;
import eu.etaxonomy.cdm.model.reference.IBookSection;
import eu.etaxonomy.cdm.model.reference.IInProceedings;
import eu.etaxonomy.cdm.model.reference.IPrintedUnitBase;
/*import eu.etaxonomy.cdm.model.reference.InProceedings;
import eu.etaxonomy.cdm.model.reference.PrintedUnitBase;*/
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceType;
//import eu.etaxonomy.cdm.model.reference.Thesis;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelReferenceExport extends BerlinModelExportBase<Reference> {
	private static final Logger logger = Logger.getLogger(BerlinModelReferenceExport.class);

	private static int modCount = 1000;
	private static final String dbTableName = "Reference";
	private static final String pluralString = "references";
	private static final Class<? extends CdmBase> standardMethodParameter = Reference.class;

	public BerlinModelReferenceExport(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(BerlinModelExportState state){
		boolean result = true;
		logger.warn("Checking for References not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	private CdmDbExportMapping<BerlinModelExportState, BerlinModelExportConfigurator, IExportTransformer> getMapping(){
		String tableName = dbTableName;
		CdmDbExportMapping<BerlinModelExportState, BerlinModelExportConfigurator, IExportTransformer> mapping = new CdmDbExportMapping<BerlinModelExportState, BerlinModelExportConfigurator, IExportTransformer>(tableName);
		mapping.addMapper(IdMapper.NewInstance("RefId"));
		mapping.addMapper(MethodMapper.NewInstance("RefCategoryFk", this));
		mapping.addMapper(MethodMapper.NewInstance("RefCache", this));
		mapping.addMapper(MethodMapper.NewInstance("NomRefCache", this));
		mapping.addMapper(DbBooleanMapper.NewFalseInstance("isProtectedTitleCache","PreliminaryFlag"));

		mapping.addMapper(DbObjectMapper.NewInstance("authorTeam", "NomAuthorTeamFk"));
		mapping.addMapper(MethodMapper.NewInstance("RefAuthorString", this));
		
		mapping.addMapper(DbStringMapper.NewInstance("title", "Title"));
//		mapping.addMapper(MethodMapper.NewInstance("Title", this));

//		mapping.addMapper(DbStringMapper.NewInstance("title", "NomTitleAbbrev"));
		mapping.addMapper(MethodMapper.NewInstance("NomTitleAbbrev", this));
		
		
		
		mapping.addMapper(DbStringMapper.NewFacultativeInstance("edition", "Edition"));
		mapping.addMapper(DbStringMapper.NewFacultativeInstance("volume", "Volume"));
		mapping.addMapper(DbStringMapper.NewFacultativeInstance("series", "Series"));
		mapping.addMapper(DbStringMapper.NewFacultativeInstance("pages", "PageString"));
		mapping.addMapper(DbStringMapper.NewFacultativeInstance("isbn", "ISBN"));
		mapping.addMapper(DbStringMapper.NewFacultativeInstance("issn", "ISSN"));
		mapping.addMapper(DbStringMapper.NewFacultativeInstance("publisher", "Publisher"));
		mapping.addMapper(DbStringMapper.NewFacultativeInstance("placePublished", "PublicationTown"));
		mapping.addMapper(DbTimePeriodMapper.NewInstance("datePublished", "RefYear"));
		mapping.addMapper(MethodMapper.NewInstance("ThesisFlag", this));
		
		mapping.addMapper(CreatedAndNotesMapper.NewInstance());
          
//		        ,[Series] ??
//		        ,[URL]
//		        ,[ExportDate]
//		        ,[InformalRefCategory]
//		        ,[IsPaper]
//		        ,[RefSourceFk]
//		        ,[IdInSource] 
		
		       
		return mapping;
	}
	
	protected void doInvoke(BerlinModelExportState state){
		try{
			logger.info("start make "+pluralString+" ...");
			boolean success = true ;

			doDelete(state);
			
			TransactionStatus txStatus = startTransaction(true);
			
			List<Reference> list = getReferenceService().list(null,100000000, 0,null,null);
			
			CdmDbExportMapping<BerlinModelExportState, BerlinModelExportConfigurator, IExportTransformer> mapping = getMapping();
			mapping.initialize(state);
			
			int count = 0;
			for (Reference<?> ref : list){
				doCount(count++, modCount, pluralString);
				success &= mapping.invoke(ref);
			}
			//Prepare InRefStatement
			logger.info("start make inRefences ...");
			String inRefSql = "UPDATE Reference SET InRefFk = ? WHERE RefId = ?"; 
			Connection con = state.getConfig().getDestination().getConnection();
			PreparedStatement stmt = con.prepareStatement(inRefSql);
			count = 0;
			for (Reference<?> ref : list){
				doCount(count++, modCount, "inReferences");
				success &= invokeInRef(ref, state, stmt);
			}
			
			commitTransaction(txStatus);
			logger.info("end make "+pluralString+" ..." + getSuccessString(success));
			if (!success){
				state.setUnsuccessfull();
			}
			return;
		}catch(SQLException e){
			e.printStackTrace();
			logger.error(e.getMessage());
			state.setUnsuccessfull();
			return;
		}
	}

	protected boolean invokeInRef(Reference ref, BerlinModelExportState state, PreparedStatement stmt) {
		if (ref == null){
			return true;
		}else{
			Reference<?> inRef = getInRef(ref);
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

	private Reference<?> getInRef(Reference<?> ref){
		Reference<?> inRef;
		if (ref.getType().equals(ReferenceType.Article)){
			return (Reference)((IArticle)ref).getInJournal();
		}else if (ref.getType().equals(ReferenceType.BookSection)){
			return (Reference)((IBookSection)ref).getInBook();
		}else if (ref.getType().equals(ReferenceType.InProceedings)){
			return (Reference) ((IInProceedings)ref).getInProceedings();
		}else if (ref.getType().isPrintedUnit()){
			return (Reference)((IPrintedUnitBase)ref).getInSeries();
		}else{
			return null;
		}
		
	}
	
	protected boolean doDelete(BerlinModelExportState state){
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
	protected boolean isIgnore(BerlinModelExportState state){
		if (state.getConfig().getDoReferences().equals(DO_REFERENCES.ALL)){
			return false;
		}else{
			return true;
		}
	}
	
	//called by MethodMapper
	@SuppressWarnings("unused")
	private static Integer getRefCategoryFk(Reference<?> ref){
		return BerlinModelTransformer.ref2refCategoryId(ref);
	}
	
	//called by MethodMapper
	@SuppressWarnings("unused")
	private static String getRefCache(Reference<?> ref){
		if (ref.isProtectedTitleCache()){
			return ref.getTitleCache();
		}else{
			return null;
		}
	}

	//called by MethodMapper
	@SuppressWarnings("unused")
	private static String getNomRefCache(Reference<?> ref){
		if (ref.isProtectedTitleCache()){
			return ref.getTitleCache();
		}else{
			return null;
		}
	}

//	//called by MethodMapper
//	@SuppressWarnings("unused")
//	private static String getTtile(StrictReferenceBase<?> ref){
////		if (ref.isProtectedTitleCache()){
////			return ref.getTitleCache();
////		}else{
////			return null;
////		}
//	}
	

	//called by MethodMapper
	@SuppressWarnings("unused")
	private static String getNomTitleAbbrev(Reference<?> ref){
		
		if (/*ref.isNomenclaturallyRelevant() &&*/ ref.getTitle() != null && ref.getTitle().length() <=80){
			return ref.getTitle();
		}else{
			return null;
		}
	}

	
	
	//called by MethodMapper
	@SuppressWarnings("unused")
	private static String getRefAuthorString(Reference<?> ref){
		if (ref == null){
			return null;
		}else{
			return (ref.getAuthorTeam() == null)? null: ref.getAuthorTeam().getTitleCache();
		}
	}

	
	//called by MethodMapper
	@SuppressWarnings("unused")
	private static Boolean getPreliminaryFlag(Reference<?> ref){
		if (ref.isProtectedTitleCache()){
			return true;
		}else{
			return false;
		}
	}

	//called by MethodMapper
	@SuppressWarnings("unused")
	private static Boolean getThesisFlag(Reference<?> ref){
		if (ref.getType().equals(ReferenceType.Thesis)){
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
