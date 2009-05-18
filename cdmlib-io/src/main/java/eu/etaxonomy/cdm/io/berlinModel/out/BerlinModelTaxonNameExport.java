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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.berlinModel.BerlinModelTransformer;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.CreatedAndNotesMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbBooleanMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbObjectMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbStringMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.IdMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.MethodMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.NomStatusMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.RefDetailMapper;
import eu.etaxonomy.cdm.io.common.IExportConfigurator;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;


/**
 * @author a.mueller
 * @created 20.03.2008
 * @version 1.0
 */
@Component
public class BerlinModelTaxonNameExport extends BerlinModelExportBase<TaxonNameBase> {
	private static final Logger logger = Logger.getLogger(BerlinModelTaxonNameExport.class);

	private static int modCount = 2500;
	private static final String dbTableName = "Name";
	private static final String pluralString = "TaxonNames";
	private static final Class<? extends CdmBase> standardMethodParameter = NonViralName.class;

	public BerlinModelTaxonNameExport(){
		super();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IImportConfigurator)
	 */
	@Override
	protected boolean doCheck(IExportConfigurator config){
		boolean result = true;
		logger.warn("Checking for " + pluralString + " not yet fully implemented");
		List<TaxonNameBase> list = getObjectList();
		checkRank(list);
		
		//result &= checkRank(config);
		
		return result;
	}
	
	private boolean checkRank(List<TaxonNameBase> list){
		List<TaxonNameBase> errorNames = new ArrayList<TaxonNameBase>();
		for (TaxonNameBase<?,?> name : list){
			if (name.getRank() == null);
			errorNames.add(name);
		}
		if (errorNames.size() >0){
			System.out.println("The following names have no Rank:\n=======================");
			for (TaxonNameBase<?,?> name : errorNames){
				System.out.println("  " + name.toString());
				System.out.println("  " + name.getUuid());		
				System.out.println("  " + name.getTitleCache());		
			}
			return false;
		}else{
			return true;
		}
	}
	
	private BerlinModelExportMapping getMapping(){
		String tableName = dbTableName;
		BerlinModelExportMapping mapping = new BerlinModelExportMapping(tableName);
		mapping.addMapper(IdMapper.NewInstance("NameId"));
		mapping.addMapper(MethodMapper.NewInstance("RankFk", this));
		mapping.addMapper(MethodMapper.NewInstance("SupraGenericName", this));
		mapping.addMapper(MethodMapper.NewInstance("Genus", this));
		mapping.addMapper(MethodMapper.NewInstance("NameCache", this));
		mapping.addMapper(MethodMapper.NewInstance("FullNameCache", this));
		mapping.addMapper(MethodMapper.NewInstance("PreliminaryFlag", this));
		mapping.addMapper(DbStringMapper.NewInstance("infraGenericEpithet", "GenusSubDivisionEpi"));
		mapping.addMapper(DbStringMapper.NewInstance("SpecificEpithet", "SpeciesEpi"));
		mapping.addMapper(DbStringMapper.NewInstance("infraSpecificEpithet", "InfraSpeciesEpi"));
		mapping.addMapper(DbStringMapper.NewInstance("appendedPhrase", "UnnamedNamePhrase"));
		mapping.addMapper(DbBooleanMapper.NewInstance("isHybridFormula", "HybridFormulaFlag", false, false));
		mapping.addMapper(DbBooleanMapper.NewInstance("isMonomHybrid", "MonomHybFlag", false, false));
		mapping.addMapper(DbBooleanMapper.NewInstance("isBinomHybrid", "BinomHybFlag", false, false));
		mapping.addMapper(DbBooleanMapper.NewInstance("isTrinomHybrid", "TrinomHybFlag", false, false));
		mapping.addMapper(DbStringMapper.NewFacultativeInstance("cultivarName", "CultivarName"));
		
		mapping.addMapper(DbObjectMapper.NewInstance("combinationAuthorTeam", "AuthorTeamFk"));
		mapping.addMapper(DbObjectMapper.NewInstance("exCombinationAuthorTeam", "ExAuthorTeamFk"));
		mapping.addMapper(DbObjectMapper.NewInstance("basionymAuthorTeam", "BasAuthorTeamFk"));
		mapping.addMapper(DbObjectMapper.NewInstance("exBasionymAuthorTeam", "ExBasAuthorTeamFk"));
		
		mapping.addMapper(DbObjectMapper.NewInstance("nomenclaturalReference", "NomRefFk"));
		mapping.addMapper(RefDetailMapper.NewInstance("nomenclaturalMicroReference","nomenclaturalReference", "NomRefDetailFk"));
		mapping.addMapper(CreatedAndNotesMapper.NewInstance(false));
		
		//NomRef (must be last)
//		mapping.addMapper(NomStatusMapper.NewInstance());
		
		//TODO
		//CultivarGroupName
		//NameSourceRefFk
		//     ,[Source_ACC]
		
		//publicationYear
		//originalPublicationYear
		//breed
		NonViralName<?> n = null;
		//n.getNomenclaturalMicroReference()
		return mapping;
	}
	
	protected boolean doInvoke(BerlinModelExportState<BerlinModelExportConfigurator> state){
		try{
			logger.info("start make "+pluralString+" ...");
			boolean success = true ;
			doDelete(state);
			
			TransactionStatus txStatus = startTransaction(true);
			logger.info("load "+pluralString+" ...");
			List<TaxonNameBase> names = getObjectList();
			
			BerlinModelExportMapping mapping = getMapping();
			mapping.initialize(state);
			logger.info("save "+pluralString+" ...");
			int count = 0;
			for (TaxonNameBase<?,?> name : names){
				doCount(count++, modCount, pluralString);
				success &= mapping.invoke(name);
				//TODO rank = null or rank < genus and genusOrUninomial != null
			}
			commitTransaction(txStatus);
			logger.info("end make " + pluralString+ " ...");
			
			return success;
		}catch(SQLException e){
			e.printStackTrace();
			logger.error(e.getMessage());
			return false;
		}
	}

	protected List<TaxonNameBase> getObjectList(){
		List<TaxonNameBase> list = getNameService().list(100000000, 0);
		return list;
	}

	
	protected boolean doDelete(BerlinModelExportState<BerlinModelExportConfigurator> state){
		BerlinModelExportConfigurator bmeConfig = state.getConfig();
		
		String sql;
		Source destination =  bmeConfig.getDestination();
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
	private static Integer getRankFk(NonViralName<?> name){
		Integer result = BerlinModelTransformer.rank2RankId(name.getRank());
		if (result == null){
			logger.warn ("Rank = null is not allowed in Berlin Model. Rank was changed to KINGDOM: " + name);
			result = 1;
		}
		return result;
	}
	
	//called by MethodMapper
	@SuppressWarnings("unused")
	private static String getSupraGenericName(NonViralName<?> name){
		if (name.isSupraGeneric()){
			return name.getGenusOrUninomial();
		}else{
			return null;
		}
	}
	
	//called by MethodMapper
	@SuppressWarnings("unused")
	private static String getGenus(NonViralName<?> name){
		if (! name.isSupraGeneric()){
			return name.getGenusOrUninomial();
		}else{
			return null;
		}
	}

	//called by MethodMapper
	@SuppressWarnings("unused")
	private static String getNameCache(NonViralName<?> name){
		if (name.isProtectedNameCache()){
			return name.getNameCache();
		}else{
			return null;
		}
	}

	//called by MethodMapper
	@SuppressWarnings("unused")
	private static String getFullNameCache(NonViralName<?> name){
		if (name.isProtectedTitleCache()){
			return name.getTitleCache();
		}else{
			return null;
		}
	}
	
	//called by MethodMapper
	@SuppressWarnings("unused")
	private static Boolean getPreliminaryFlag(NonViralName<?> name){
		if (name.isProtectedTitleCache() || name.isProtectedNameCache()){
			if (name.isProtectedTitleCache() && name.isProtectedNameCache()){
				logger.warn("protectedTitleCache and protectedNameCache do not have the same value for name " + name.getTitleCache() + ". This can not be mapped appropriately to the Berlin Model ");
			}
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
