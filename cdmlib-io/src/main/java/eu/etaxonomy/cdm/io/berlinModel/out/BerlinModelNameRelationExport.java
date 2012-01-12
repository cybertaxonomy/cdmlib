/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.berlinModel.out;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.CreatedAndNotesMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbObjectMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.IdMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.MethodMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.RefDetailMapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.mapping.out.CdmDbExportMapping;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.name.HomotypicalGroup;
import eu.etaxonomy.cdm.model.name.HybridRelationship;
import eu.etaxonomy.cdm.model.name.NameRelationship;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Reference;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelNameRelationExport extends BerlinModelExportBase<RelationshipBase> {
	private static final Logger logger = Logger.getLogger(BerlinModelNameRelationExport.class);

	private static int modCount = 100;
	private static final String dbTableName = "RelName";
	private static final String pluralString = "NameRelationships";
	private static final Class<? extends CdmBase> standardMethodParameter = RelationshipBase.class;


	public BerlinModelNameRelationExport(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(BerlinModelExportState state){
		boolean result = true;
		logger.warn("Checking for " + pluralString + " not yet implemented");
		//result &= checkArticlesWithoutJournal(bmiConfig);
		//result &= checkPartOfJournal(bmiConfig);
		
		return result;
	}
	
	private CdmDbExportMapping<BerlinModelExportState, BerlinModelExportConfigurator> getMapping(){
		String tableName = dbTableName;
		CdmDbExportMapping<BerlinModelExportState, BerlinModelExportConfigurator> mapping = new CdmDbExportMapping<BerlinModelExportState, BerlinModelExportConfigurator>(tableName);
		mapping.addMapper(IdMapper.NewInstance("RelNameId"));

		mapping.addMapper(DbObjectMapper.NewInstance("fromName", "NameFk1"));
		mapping.addMapper(DbObjectMapper.NewInstance("toName", "NameFk2"));

		
		mapping.addMapper(MethodMapper.NewInstance("RelNameQualifierFk", this));
		
		mapping.addMapper(DbObjectMapper.NewInstance("citation", "RefFk"));
		mapping.addMapper(RefDetailMapper.NewInstance("citationMicroReference","citation", "RefDetailFk"));
		mapping.addMapper(CreatedAndNotesMapper.NewInstance());
		
		return mapping;
	}
	
	protected void doInvoke(BerlinModelExportState state){
		try{
			logger.info("start make " + pluralString + " ...");
			boolean success = true ;
			doDelete(state);
			
			TransactionStatus txStatus = startTransaction(true);
			
			List<RelationshipBase> list = getNameService().getAllRelationships(100000000, 0);
			
			CdmDbExportMapping<BerlinModelExportState, BerlinModelExportConfigurator> mapping = getMapping();
			mapping.initialize(state);
			
			int count = 0;
			for (RelationshipBase<?,?,?> rel : list){
				if (rel.isInstanceOf(NameRelationship.class) || rel.isInstanceOf(HybridRelationship.class )){
					doCount(count++, modCount, pluralString);
					success &= mapping.invoke(rel);
				}
			}
			commitTransaction(txStatus);
			
			success &= makeIsHomotypicRelation(state, mapping);
			
			logger.info("end make " + pluralString + " ..." + getSuccessString(success));
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

	
	private boolean makeIsHomotypicRelation(BerlinModelExportState state, CdmDbExportMapping<BerlinModelExportState, BerlinModelExportConfigurator> mapping){
		boolean success = true ;
		try{
			Integer homotypicId = state.getConfig().getIsHomotypicId();
			if (homotypicId == null){
				return success;
			}
			logger.info("start make IsHomotypicRelations ...");
			
			TransactionStatus txStatus = startTransaction(true);
			
			List<HomotypicalGroup> list = getNameService().getAllHomotypicalGroups(100000000, 0);
			
			int count = 0;
			modCount = 1000;
			Set<NameRelationship> basionymNameRels = new HashSet<NameRelationship>(); 
			for (HomotypicalGroup homoGroup : list){
				doCount(count++, modCount, "homotypical groups");
				Set<TaxonNameBase> allNames = homoGroup.getTypifiedNames();
				if (allNames.size() > 1){
					Set<TaxonNameBase> readyNames = new HashSet<TaxonNameBase>();
					Set<TaxonNameBase> unrelateds = homoGroup.getUnrelatedNames();
					for (TaxonNameBase unrelated : unrelateds){
						for (TaxonNameBase oneOfAllNames: allNames){
							if(!unrelated.equals(oneOfAllNames) && ! readyNames.contains(oneOfAllNames)){
								success &= invokeIsHomotypic(state, mapping, unrelated, oneOfAllNames, null, null);
							}
						}
						readyNames.add(unrelated);
					}
				}
			}
			commitTransaction(txStatus);
			
			logger.info("end make homotypical groups ... " +  getSuccessString(success));
			return success;
		}catch(SQLException e){
			logger.error(e.getMessage());
			e.printStackTrace();
			return false;
		}	
	}
	
	private boolean invokeIsHomotypic(BerlinModelExportState state, CdmDbExportMapping<BerlinModelExportState, BerlinModelExportConfigurator> mapping, TaxonNameBase fromName, TaxonNameBase toName, Reference refId, String microCitation) throws SQLException{
		try{
			logger.info(fromName.getTitleCache() + "->" + toName.getTitleCache());
			String maxQuery = " SELECT max(relNameId) as max FROM relName ";
			ResultSet rs = state.getConfig().getDestination().getResultSet(maxQuery);
			int maxId = 1;
			if (rs.next()){
				maxId = rs.getInt("max") + 1;
			}
			int fromNameId = state.getDbId(fromName);
			int toNameId = state.getDbId(toName);
			int catId = state.getConfig().getIsHomotypicId();
			String query = "INSERT INTO relName (relNameId, nameFk1, nameFk2, RelNameQualifierFk) " + 
				" VALUES ("+maxId+","+fromNameId+","+toNameId+","+catId+")";
			int ui = state.getConfig().getDestination().getConnection().createStatement().executeUpdate(query);
		}catch(SQLException e){
			throw e;
		}
		return true;
	}

	private Set<TaxonNameBase> getAllRelatedNames(Set<NameRelationship> rels){
		Set<TaxonNameBase> result = new HashSet<TaxonNameBase>();
		for (NameRelationship rel : rels){
			result.add(rel.getFromName());
			result.add(rel.getToName());
		}
		return result;
	}
	
	protected boolean doDelete(BerlinModelExportState state){
		BerlinModelExportConfigurator bmeConfig = state.getConfig();
		
		String sql;
		Source destination =  bmeConfig.getDestination();
		//RelPTaxon
		sql = "DELETE FROM RelName";
		destination.setQuery(sql);
		destination.update(sql);

		return true;
	}
		
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	protected boolean isIgnore(BerlinModelExportState state){
		return ! state.getConfig().isDoRelNames();
	}
	
	//called by MethodMapper
	@SuppressWarnings("unused")
	private static Integer getRelNameQualifierFk(RelationshipBase<?, ?, ?> rel) throws Exception {
//		if (config.getRelNameQualifierMethod() != null){
//			try {
//				return (Integer)config.getRelNameQualifierMethod().invoke(rel);
//			} catch (Exception e) {
//				logger.error(e.getMessage());
//				throw e;
//			}
//		}else{
			return BerlinModelTransformer.nameRel2RelNameQualifierFk(rel);
//		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.BerlinModelExportBase#getStandardMethodParameter()
	 */
	@Override
	public Class<? extends CdmBase> getStandardMethodParameter() {
		return standardMethodParameter;
	}
}
