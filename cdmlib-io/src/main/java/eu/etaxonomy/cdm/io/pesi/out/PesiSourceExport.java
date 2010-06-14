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

import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbStringMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbTimePeriodMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.IdMapper;
import eu.etaxonomy.cdm.io.berlinModel.out.mapper.MethodMapper;
import eu.etaxonomy.cdm.io.common.Source;
import eu.etaxonomy.cdm.io.common.IImportConfigurator.DO_REFERENCES;
import eu.etaxonomy.cdm.io.erms.ErmsTransformer;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.mueller
 * @author e.-m.lee
 * @date 11.02.2010
 *
 */
@Component
@SuppressWarnings("unchecked")
public class PesiSourceExport extends PesiExportBase {
	private static final Logger logger = Logger.getLogger(PesiSourceExport.class);
	private static final Class<? extends CdmBase> standardMethodParameter = ReferenceBase.class;

	private static int modCount = 1000;
	private static final String dbTableName = "Source";
	private static final String pluralString = "Sources";

	public PesiSourceExport() {
		super();
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.pesi.out.PesiExportBase#getStandardMethodParameter()
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
		try{
			logger.error("*** Started Making " + pluralString + " ...");

			// Get the limit for objects to save within a single transaction.
			int limit = state.getConfig().getLimitSave();

			// Stores whether this invoke was successful or not.
			boolean success = true ;

			// PESI: Clear the database table Source.
			doDelete(state);

			// Get specific mappings: (CDM) Reference -> (PESI) Source
			PesiExportMapping mapping = getMapping();

			// Initialize the db mapper
			mapping.initialize(state);

			// PESI: Create the Sources
			int count = 0;
			int pastCount = 0;
			TransactionStatus txStatus = null;
			List<ReferenceBase> list = null;

			// Start transaction
			txStatus = startTransaction(true);
			logger.error("Started new transaction. Fetching some " + pluralString + " (max: " + limit + ") ...");
			while ((list = getReferenceService().list(null, limit, count, null, null)).size() > 0) {

				logger.error("Fetched " + list.size() + " " + pluralString + ". Exporting...");
			for (ReferenceBase<?> reference : list) {
				doCount(count++, modCount, pluralString);
				success &= mapping.invoke(reference);
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
	 * Deletes all entries of database tables related to <code>Source</code>.
	 * @param state The {@link PesiExportState PesiExportState}.
	 * @return Whether the delete operation was successful or not.
	 */
	protected boolean doDelete(PesiExportState state) {
		PesiExportConfigurator pesiConfig = (PesiExportConfigurator) state.getConfig();
		
		String sql;
		Source destination =  pesiConfig.getDestination();

		// Clear Occurrences
		sql = "DELETE FROM Occurrence";
		destination.setQuery(sql);
		destination.update(sql);

		// Clear Taxa
		sql = "DELETE FROM Taxon";
		destination.setQuery(sql);
		destination.update(sql);

		// Clear Sources
		sql = "DELETE FROM " + dbTableName;
		destination.setQuery(sql);
		destination.update(sql);
		
		return true;
	}
	
	/**
	 * Returns the <code>IMIS_Id</code> attribute.
	 * @param reference The {@link ReferenceBase Reference}.
	 * @return The <code>IMIS_Id</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getIMIS_Id(ReferenceBase<?> reference) {
		Integer result = null;
		if (reference != null) {
			Set<Extension> extensions = reference.getExtensions();
			for (Extension extension : extensions) {
				try {
					if (extension.getType().getUuid().equals(ErmsTransformer.IMIS_UUID)) {
						// IMIS_ID found
						result = Integer.parseInt(extension.getValue().trim());
	}
				} catch (NumberFormatException e) {
					logger.warn("String could not be parsed to int: " + extension.getValue() + " / Reference: " + reference.getUuid() + " (" + reference.getTitleCache() + ")");
					result = null;
				}
			}
		}
		return result;
	}
	
	/**
	 * Returns the <code>SourceCategoryFK</code> attribute.
	 * @param reference The {@link ReferenceBase Reference}.
	 * @return The <code>SourceCategoryFK</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static Integer getSourceCategoryFK(ReferenceBase<?> reference) {
		return PesiTransformer.reference2SourceCategoryFK(reference);
	}
	
	/**
	 * Returns the <code>SourceCategoryCache</code> attribute.
	 * @param reference The {@link ReferenceBase Reference}.
	 * @return The <code>SourceCategoryCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getSourceCategoryCache(ReferenceBase<?> reference) {
		return PesiTransformer.getSourceCategoryCache(reference);
	}

	/**
	 * Returns the <code>Name</code> attribute. The corresponding CDM attribute is <code>title</code>.
	 * @param reference The {@link ReferenceBase Reference}.
	 * @return The <code>Name</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getName(ReferenceBase<?> reference) {
		return reference.getTitle();
	}

	/**
	 * Returns the <code>AuthorString</code> attribute. The corresponding CDM attribute is the <code>titleCache</code> of an <code>authorTeam</code>.
	 * @param reference The {@link ReferenceBase Reference}.
	 * @return The <code>AuthorString</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getAuthorString(ReferenceBase<?> reference) {
		TeamOrPersonBase team = reference.getAuthorTeam();
		if (team != null) {
//			return team.getTitleCache();
			return team.getNomenclaturalTitle();
		} else {
			return null;
		}
	}

	/**
	 * Returns the <code>NomRefCache</code> attribute. The corresponding CDM attribute is <code>titleCache</code>.
	 * @param reference The {@link ReferenceBase Reference}.
	 * @return The <code>NomRefCache</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNomRefCache(ReferenceBase<?> reference) {
		return reference.getTitleCache();
	}

	/**
	 * Returns the <code>Notes</code> attribute.
	 * @param reference The {@link ReferenceBase Reference}.
	 * @return The <code>Notes</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getNotes(ReferenceBase<?> reference) {
		// TODO
		String result = null;
		if (reference != null) {
			Set<Annotation> annotations = reference.getAnnotations();
			if (annotations.size() == 1) {
				result = annotations.iterator().next().getText();
			} else {
				logger.warn("Reference has more than one Annotation: " + reference.getUuid() + " (" + reference.getTitleCache() + ")");
	}
		}
		return result;
	}

	/**
	 * Returns the <code>RefIdInSource</code> attribute.
	 * @param reference The {@link ReferenceBase Reference}.
	 * @return The <code>RefIdInSource</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getRefIdInSource(ReferenceBase<?> reference) {
		String result = null;
		Set<IdentifiableSource> sources = reference.getSources();
		if (sources.size() == 1) {
			result = sources.iterator().next().getIdInSource();
		} else {
			logger.warn("Reference has more than one source: " + reference.getUuid() + " (" + reference.getTitleCache() + ")");
			}
		return result;
	}

	/**
	 * Returns the <code>OriginalDB</code> attribute. The corresponding CDM attribute is the <code>titleCache</code> of a <code>citation</code>.
	 * @param reference The {@link ReferenceBase Reference}.
	 * @return The <code>OriginalDB</code> attribute.
	 * @see MethodMapper
	 */
	@SuppressWarnings("unused")
	private static String getOriginalDB(ReferenceBase<?> reference) {
		String result = null;
		if (reference != null) {
		for (IdentifiableSource source : reference.getSources()) {
				ReferenceBase citation = source.getCitation();
				if (source != null && citation != null) {
					result = citation.getTitleCache();  //or just title
			}
		}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmIoBase#isIgnore(eu.etaxonomy.cdm.io.common.IoStateBase)
	 */
	@Override
	protected boolean isIgnore(PesiExportState state) {
		return ! (((PesiExportConfigurator) state.getConfig()).getDoReferences().equals(DO_REFERENCES.ALL));
	}

	/**
	 * Returns the CDM to PESI specific export mappings.
	 * @return The {@link PesiExportMapping PesiExportMapping}.
	 */
	private PesiExportMapping getMapping() {
		PesiExportMapping mapping = new PesiExportMapping(dbTableName);
		
		mapping.addMapper(IdMapper.NewInstance("SourceId"));
		mapping.addMapper(MethodMapper.NewInstance("IMIS_Id", this));
		mapping.addMapper(MethodMapper.NewInstance("SourceCategoryFK", this));
		mapping.addMapper(MethodMapper.NewInstance("SourceCategoryCache", this));
		mapping.addMapper(MethodMapper.NewInstance("Name", this));
		mapping.addMapper(DbStringMapper.NewInstance("referenceAbstract", "Abstract"));
		mapping.addMapper(DbStringMapper.NewInstance("title", "Title"));
		mapping.addMapper(MethodMapper.NewInstance("AuthorString", this));
		mapping.addMapper(DbTimePeriodMapper.NewInstance("datePublished", "RefYear"));
		mapping.addMapper(MethodMapper.NewInstance("NomRefCache", this));
		mapping.addMapper(DbStringMapper.NewInstance("uri", "Link"));
		mapping.addMapper(MethodMapper.NewInstance("Notes", this));
		mapping.addMapper(MethodMapper.NewInstance("RefIdInSource", this));
		mapping.addMapper(MethodMapper.NewInstance("OriginalDB", this));

		return mapping;
	}

}
