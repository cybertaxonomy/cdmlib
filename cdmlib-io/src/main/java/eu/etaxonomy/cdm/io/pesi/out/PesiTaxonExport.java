// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.pesi.out;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbStringMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbTimePeriodMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.MethodMapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @author e.-m.lee
 * @date 23.02.2010
 *
 */
@Component
@SuppressWarnings("unchecked")
public class PesiTaxonExport extends PesiExportBase<TaxonBase> {
	private static final Logger logger = Logger.getLogger(PesiTaxonExport.class);
	private static final Class<? extends CdmBase> standardMethodParameter = TaxonBase.class;

	private static int modCount = 1000;
	private static final String dbTableName = "Taxon";
	private static final String pluralString = "Taxa";

	public PesiTaxonExport() {
		super();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.DbExportBase#getStandardMethodParameter()
	 */
	@Override
	public Class<? extends CdmBase> getStandardMethodParameter() {
		return standardMethodParameter;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doCheck(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doCheck(PesiExportState state) {
		boolean result = true;
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#doInvoke(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean doInvoke(PesiExportState state) {
		try {
			logger.info("Start: Make " + pluralString + " ...");
	
			// Stores whether this invoke was successful or not.
			boolean success = true ;
	
			// PESI: Clear the database table Taxon.
			doDelete(state);
	
			// Start transaction
			TransactionStatus txStatus = startTransaction(true);
	
			// CDM: Get all Taxa
			List<TaxonBase> list = getTaxonService().list(null, 100000000, 0, null, null);
	
			// Get specific mappings: (CDM) Taxon -> (PESI) Taxon
			PesiExportMapping mapping = getMapping();
	
			// Initialize the db mapper
			mapping.initialize(state);
	
			// PESI: Create the Taxa
			int count = 0;
			for (TaxonBase<?> taxon : list) {
				doCount(count++, modCount, pluralString);
				success &= mapping.invoke(taxon);
			}
	
			// Commit transaction
			commitTransaction(txStatus);
			logger.info("End: Make " + pluralString + " ..." + getSuccessString(success));
	
			return success;
		} catch(SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			return false;
		}
	}

	/**
	 * Deletes all entries of database tables related to <code>Taxon</code>.
	 * @param state The PesiExportState
	 * @return Whether the delete operation was successful or not.
	 */
	protected boolean doDelete(PesiExportState state) {
		PesiExportConfigurator pesiConfig = (PesiExportConfigurator) state.getConfig();
		
		String sql;
		Source destination =  pesiConfig.getDestination();

		// Clear Taxon
		sql = "DELETE FROM " + dbTableName;
		destination.setQuery(sql);
		destination.update(sql);
		return true;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean isIgnore(PesiExportState state) {
		return ! state.getConfig().isDoTaxa();
	}

	/**
	 * Returns the <code>SourceFK</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>SourceFK</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceFK(TaxonBase<?> taxon) {
		// TODO
		// Determine Reference (CDM: taxon.getSec();) and lookup identifier in Source (PESI) using helper map created during Source Export.
		return null;
	}

	/**
	 * Returns the <code>KingdomFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>KingdomFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getKingdomFk(TaxonBase<?> taxon) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>RankFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>RankFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getRankFk(TaxonBase<?> taxon) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>RankCache</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>RankCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getRankCache(TaxonBase<?> taxon) {
		// TODO
		return null;
	}

	/**
	 * 
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return Whether it's genus or uninomial.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getGenusOrUninomial(TaxonBase<?> taxon) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>InfraGenericEpithet</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>InfraGenericEpithet</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getInfraGenericEpithet(TaxonBase<?> taxon) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>SpecificEpithet</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>SpecificEpithet</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSpecificEpithet(TaxonBase<?> taxon) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>InfraSpecificEpithet</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>InfraSpecificEpithet</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getInfraSpecificEpithet(TaxonBase<?> taxon) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>WebSearchName</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>WebSearchName</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getWebSearchName(TaxonBase<?> taxon) {
		// TODO
		return null;
	}
	
	/**
	 * Returns the <code>WebShowName</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>WebShowName</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getWebShowName(TaxonBase<?> taxon) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>AuthorString</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>AuthorString</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getAuthorString(TaxonBase<?> taxon) {
		TeamOrPersonBase team = taxon.getSec().getAuthorTeam();
		if (team != null) {
			return team.getTitleCache();
		} else {
			return null;
		}
	}

	/**
	 * Returns the <code>NomRefString</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>NomRefString</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNomRefString(TaxonBase<?> taxon) {
		return taxon.getSec().getTitleCache();
	}
	
	/**
	 * Returns the <code>DisplayName</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>DisplayName</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getDisplayName(TaxonBase<?> taxon) {
		// TODO: extension - taxon.getExtensions();
		return null;
	}
	
	/**
	 * Returns the <code>FuzzyName</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>FuzzyName</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getFuzzyName(TaxonBase<?> taxon) {
		// TODO: extension
		return null;
	}

	/**
	 * Returns the <code>NameStatusFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>NameStatusFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNameStatusFk(TaxonBase<?> taxon) {
		// TODO: extension
		return null;
	}
	
	/**
	 * Returns the <code>NameStatusCache</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>NameStatusCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNameStatusCache(TaxonBase<?> taxon) {
		// TODO: extension
		return null;
	}
	
	/**
	 * Returns the <code>TaxonStatusFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>TaxonStatusFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getTaxonStatusFk(TaxonBase<?> taxon) {
		// TODO
		return null;
	}
	
	/**
	 * Returns the <code>TaxonStatusCache</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>TaxonStatusCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getTaxonStatusCache(TaxonBase<?> taxon) {
		// TODO
		return null;
	}
	
	/**
	 * Returns the <code>ParentTaxonFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>ParentTaxonFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getParentTaxonFk(TaxonBase<?> taxon) {
		// TODO
		return null;
	}
	
	/**
	 * Returns the <code>TypeNameFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>TypeNameFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getTypeNameFk(TaxonBase<?> taxon) {
		// TODO
		return null;
	}
	
	/**
	 * Returns the <code>TypeFullnameCache</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>TypeFullnameCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getTypeFullnameCache(TaxonBase<?> taxon) {
		// TODO
		return null;
	}
	
	/**
	 * Returns the <code>QualityStatusFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>QualityStatusFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getQualityStatusFk(TaxonBase<?> taxon) {
		// TODO
		return null;
	}
	
	/**
	 * Returns the <code>QualityStatusCache</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>QualityStatusCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getQualityStatusCache(TaxonBase<?> taxon) {
		// TODO
		return null;
	}
	
	/**
	 * Returns the <code>TypeDesignationStatusFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>TypeDesignationStatusFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getTypeDesignationStatusFk(TaxonBase<?> taxon) {
		// TODO
		return null;
	}

	/**
	 * Returns the <code>TypeDesignationStatusCache</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>TypeDesignationStatusCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getTypeDesignationStatusCache(TaxonBase<?> taxon) {
		// TODO
		return null;
	}
	
	/**
	 * returns the <code>TreeIndex</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>TreeIndex</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getTreeIndex(TaxonBase<?> taxon) {
		// TODO
		return null;
	}
	
	/**
	 * Returns the <code>FossilStatusFk</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>FossilStatusFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getFossilStatusFk(TaxonBase<?> taxon) {
		// TODO
		return null;
	}
	
	/**
	 * Returns the <code>FossilStatusCache</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>FossilStatusCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getFossilStatusCache(TaxonBase<?> taxon) {
		// TODO
		return null;
	}
	
	/**
	 * Returns the <code>IdInSource</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>IdInSource</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getIdInSource(TaxonBase<?> taxon) {
		// TODO
		return null;
	}
	
	/**
	 * Returns the <code>OriginalDB</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>OriginalDB</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getOriginalDB(TaxonBase<?> taxon) {
		// TODO
		return null;
	}
	
	/**
	 * Returns the <code>LastAction</code> attribute.
	 * @param taxon The {@link TaxonBase Taxon}.
	 * @return The <code>LastAction</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getLastAction(TaxonBase<?> taxon) {
		// TODO
		return null;
	}
	
	/**
	 * Returns the CDM to PESI specific export mappings.
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbTableName);
		
	//	mapping.addMapper(IdMapper.NewInstance("TaxonId"));
		mapping.addMapper(MethodMapper.NewInstance("SourceFK", this));
		mapping.addMapper(MethodMapper.NewInstance("KingdomFk", this));
		mapping.addMapper(MethodMapper.NewInstance("RankFk", this));
		mapping.addMapper(MethodMapper.NewInstance("RankCache", this));
		mapping.addMapper(MethodMapper.NewInstance("GenusOrUninomial", this));
		mapping.addMapper(MethodMapper.NewInstance("InfraGenericEpithet", this));
		mapping.addMapper(MethodMapper.NewInstance("SpecificEpithet", this));
		mapping.addMapper(MethodMapper.NewInstance("InfraSpecificEpithet", this));
		mapping.addMapper(MethodMapper.NewInstance("WebSearchName", this));
		mapping.addMapper(MethodMapper.NewInstance("WebShowName", this));
		mapping.addMapper(MethodMapper.NewInstance("AuthorString", this));
		mapping.addMapper(DbStringMapper.NewInstance("name", "FullName"));
		mapping.addMapper(MethodMapper.NewInstance("NomRefString", this));
		mapping.addMapper(MethodMapper.NewInstance("DisplayName", this));
		mapping.addMapper(MethodMapper.NewInstance("FuzzyName", this));
		mapping.addMapper(MethodMapper.NewInstance("NameStatusFk", this));
		mapping.addMapper(MethodMapper.NewInstance("NameStatusCache", this));
		mapping.addMapper(MethodMapper.NewInstance("TaxonStatusFk", this));
		mapping.addMapper(MethodMapper.NewInstance("TaxonStatusCache", this));
		mapping.addMapper(MethodMapper.NewInstance("ParentTaxonFk", this));
		mapping.addMapper(MethodMapper.NewInstance("TypeNameFk", this));
		mapping.addMapper(MethodMapper.NewInstance("TypeFullnameCache", this));
		mapping.addMapper(MethodMapper.NewInstance("QualityStatusFk", this));
		mapping.addMapper(MethodMapper.NewInstance("QualityStatusCache", this));
		mapping.addMapper(MethodMapper.NewInstance("TypeDesignationStatusFk", this));
		mapping.addMapper(MethodMapper.NewInstance("TypeDesignationStatusCache", this));
		mapping.addMapper(MethodMapper.NewInstance("TreeIndex", this));
		mapping.addMapper(MethodMapper.NewInstance("FossilStatusFk", this));
		mapping.addMapper(MethodMapper.NewInstance("FossilStatusCache", this));
		mapping.addMapper(MethodMapper.NewInstance("IdInSource", this));
		mapping.addMapper(MethodMapper.NewInstance("OriginalDB", this));
		mapping.addMapper(MethodMapper.NewInstance("LastAction", this));
		mapping.addMapper(DbTimePeriodMapper.NewInstance("updated", "LastActionDate"));

		return mapping;
	}

}
