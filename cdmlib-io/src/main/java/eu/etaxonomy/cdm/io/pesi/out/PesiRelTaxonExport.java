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
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;

import eu.etaxonomy.cdm.io.berlinModel.out.mapper.MethodMapper;
import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationship;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;

/**
 * @author a.mueller
 * @author e.-m.lee
 * @date 23.02.2010
 *
 */
@Component
@SuppressWarnings("unchecked")
public class PesiRelTaxonExport extends PesiExportBase {
	private static final Logger logger = Logger.getLogger(PesiRelTaxonExport.class);
	private static final Class<? extends CdmBase> standardMethodParameter = RelationshipBase.class;

	private static int modCount = 1000;
	private static final String dbTableName = "RelTaxon";
	private static final String pluralString = "Relationships";

	public PesiRelTaxonExport() {
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
			logger.error("*** Started Making " + pluralString + " ...");
	
			// Get the limit for objects to save within a single transaction.
			int limit = state.getConfig().getLimitSave();

			// Stores whether this invoke was successful or not.
			boolean success = true;

			// PESI: Clear the database table RelTaxon.
			doDelete(state);
	
			// Get specific mappings: (CDM) Relationship -> (PESI) RelTaxon
			PesiExportMapping mapping = getMapping();

			// Initialize the db mapper
			mapping.initialize(state);

			// PESI: Create the RelTaxa
			int count = 0;
			int taxonCount = 0;
			int pastCount = 0;
			TransactionStatus txStatus = null;
			List<RelationshipBase> list = null;

			// Start transaction
			txStatus = startTransaction(true);
			logger.error("Started new transaction. Fetching some " + pluralString + " (max: " + limit + ") ...");
			while ((list = getTaxonService().getAllRelationships(limit, taxonCount)).size() > 0) {

				taxonCount += list.size();
				logger.error("Fetched " + list.size() + " " + pluralString + ". Exporting...");
			for (RelationshipBase<?, ?, ?> relation : list) {

					// Focus on TaxonRelationships and SynonymRelationships.
				if (relation.isInstanceOf(TaxonRelationship.class) || relation.isInstanceOf(SynonymRelationship.class)) {
					doCount(count++, modCount, pluralString);
					success &= mapping.invoke(relation);
				}
			}
	
			// Commit transaction
			commitTransaction(txStatus);
				logger.error("Committed transaction.");
				logger.error("Exported " + (count - pastCount) + " " + pluralString + ". Total: " + count);
				pastCount = count;

				// Start transaction
				txStatus = startTransaction(true);
				logger.error("Started new transaction. Fetching some " + pluralString + " (max: " + limit + ") ...");
			}
			if (list.size() == 0) {
				logger.error("No " + pluralString + " left to fetch.");
			}
			// Commit transaction
			commitTransaction(txStatus);
			logger.error("Committed transaction.");
	
			logger.error("*** Finished Making " + pluralString + " ..." + getSuccessString(success));
			
			return success;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			return false;
		}
	}

	/**
	 * Deletes all entries of database tables related to <code>RelTaxon</code>.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return Whether the delete operation was successful or not.
	 */
	protected boolean doDelete(PesiExportState state) {
		PesiExportConfigurator pesiConfig = (PesiExportConfigurator) state.getConfig();
		
		String sql;
		Source destination =  pesiConfig.getDestination();

		// Clear RelTaxon
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
		return ! state.getConfig().isDoRelTaxa();
	}

	/**
	 * Returns the <code>TaxonFk1</code> attribute. It corresponds to a CDM <code>TaxonRelationship</code>.
	 * @param relationship The {@link RelationshipBase Relationship}.
	 * @param state The {@link DbExportStateBase DbExportState}.
	 * @return The <code>TaxonFk1</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getTaxonFk1(RelationshipBase<?, ?, ?> relationship, DbExportStateBase<?> state) {
		return getObjectFk(relationship, state, true);
	}
	
	/**
	 * Returns the <code>TaxonFk2</code> attribute. It corresponds to a CDM <code>SynonymRelationship</code>.
	 * @param relationship The {@link RelationshipBase Relationship}.
	 * @param state The {@link DbExportStateBase DbExportState}.
	 * @return The <code>TaxonFk2</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getTaxonFk2(RelationshipBase<?, ?, ?> relationship, DbExportStateBase<?> state) {
		return getObjectFk(relationship, state, false);
	}
	
	/**
	 * Returns the <code>RelTaxonQualifierFk</code> attribute.
	 * @param relationship The {@link RelationshipBase Relationship}.
	 * @return The <code>RelTaxonQualifierFk</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getRelTaxonQualifierFk(RelationshipBase<?, ?, ?> relationship) {
		return PesiTransformer.taxonRelation2RelTaxonQualifierFk(relationship);
	}
	
	/**
	 * Returns the <code>RelQualifierCache</code> attribute.
	 * @param relationship The {@link RelationshipBase Relationship}.
	 * @return The <code>RelQualifierCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getRelQualifierCache(RelationshipBase<?, ?, ?> relationship) {
		return PesiTransformer.taxonRelation2RelTaxonQualifierCache(relationship);
	}
	
	/**
	 * Returns the <code>Notes</code> attribute.
	 * @param relationship The {@link RelationshipBase Relationship}.
	 * @return The <code>Notes</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNotes(RelationshipBase<?, ?, ?> relationship) {
		// TODO
		String result = null;
		if (relationship != null) {
			Set<Annotation> annotations = relationship.getAnnotations();
			if (annotations.size() == 1) {
				result = annotations.iterator().next().getText();
			} else {
				logger.warn("Relationship has more than one Annotation: " + relationship.getUuid());
	}
		}
		return result;
	}

	/**
	 * Returns the database key of an object in the given relationship.
	 * @param relationship {@link RelationshipBase RelationshipBase}.
	 * @param state {@link DbExportStateBase DbExportStateBase}.
	 * @param isFrom A boolean value indicating whether the database key of the parent or child in this relationship is searched. <code>true</code> means the child is searched. <code>false</code> means the parent is searched.
	 * @return The database key of an object in the given relationship.
	 */
	private static Integer getObjectFk(RelationshipBase<?, ?, ?> relationship, DbExportStateBase<?> state, boolean isFrom) {
		TaxonBase<?> taxon = null;
		if (relationship.isInstanceOf(TaxonRelationship.class)) {
			TaxonRelationship tr = (TaxonRelationship)relationship;
			taxon = (isFrom) ? tr.getFromTaxon():  tr.getToTaxon();
		} else if (relationship.isInstanceOf(SynonymRelationship.class)) {
			SynonymRelationship sr = (SynonymRelationship)relationship;
			taxon = (isFrom) ? sr.getSynonym() : sr.getAcceptedTaxon();
		}
		if (taxon != null) {
			return state.getDbId(taxon);
		}
		logger.warn("No taxon found for relationship: " + relationship.getUuid());
		return null;
	}

	/**
	 * Returns the CDM to PESI specific export mappings.
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbTableName);
		
//		mapping.addMapper(IdMapper.NewInstance("RelTaxonId")); // Automagically generated on database level as primary key
		mapping.addMapper(MethodMapper.NewInstance("TaxonFk1", this.getClass(), "getTaxonFk1", standardMethodParameter, DbExportStateBase.class));
		mapping.addMapper(MethodMapper.NewInstance("TaxonFk2", this.getClass(), "getTaxonFk2", standardMethodParameter, DbExportStateBase.class));
		mapping.addMapper(MethodMapper.NewInstance("RelTaxonQualifierFk", this));
		mapping.addMapper(MethodMapper.NewInstance("RelQualifierCache", this));
		mapping.addMapper(MethodMapper.NewInstance("Notes", this));
//		mapping.addMapper(CreatedAndNotesMapper.NewInstance(false));

		return mapping;
	}

}
