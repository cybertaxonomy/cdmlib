// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import java.sql.Types;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementSource;
import eu.etaxonomy.cdm.model.reference.Reference;


/**
 * Maps distribution to a database foreign key or cache field.
 * Requires according transformer implementation.
 * @author a.mueller
 * @created 06.02.2012
 */
public class DbSingleSourceMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer>{
	private static final Logger logger = Logger.getLogger(DbSingleSourceMapper.class);

	public enum EXCLUDE{
		NONE,
		WITH_ID,
		WITH_NAMESPACE
	}
	public static int EXCLUDE_NONE = 0;
	public static int EXCLUDE_WITH_ID = 1;
	public static int EXCLUDE_WITH_NAMESPACE = 2;

	private boolean isCache = false;
	private final EnumSet<EXCLUDE> exclude;

	public static DbSingleSourceMapper NewInstance(String dbAttributeString, EnumSet<EXCLUDE> exclude, boolean isCache){
		return new DbSingleSourceMapper(dbAttributeString, exclude, isCache, null);
	}

	/**
	 * @param dbAttributeString
	 * @param cdmAttributeString
	 */
	protected DbSingleSourceMapper(String dbAttributeString, EnumSet<EXCLUDE> exclude, boolean isCache, Object defaultValue) {
		super("sources", dbAttributeString, defaultValue);
		this.isCache = isCache;
		this.exclude = exclude;
	}

	@Override
	protected Object getValue(CdmBase cdmBase) {
		//TODO implement also for Identifiable sources
		if (cdmBase.isInstanceOf(DescriptionElementBase.class)){
			//find source candidates
			DescriptionElementBase el = CdmBase.deproxy(cdmBase, DescriptionElementBase.class);
			Set<DescriptionElementSource> sourceCandidates = el.getSources();
			Set<DescriptionElementSource> filteredSources = new HashSet<DescriptionElementSource>();
			for (DescriptionElementSource sourceCandidate : sourceCandidates){
				if (isPesiSource(sourceCandidate)){  //TODO pesi should not appear here
					filteredSources.add(sourceCandidate);
				}
			}
			//filter
			if (filteredSources.size() == 0 ){
				return null;
			}else if (filteredSources.size() > 1){
				logger.warn("There is more than 1 accepted source for description element " + el.getUuid() + ". Arbitrary first source is used.");
			}
			DescriptionElementSource source = filteredSources.iterator().next();
			Reference<?> ref = source.getCitation();
			if (ref == null){
				logger.warn("Citation is missing for description element (" + el.getUuid() + ") source.");
				return null;
			}
			if (isCache){
				return ref.getTitleCache();
			}else{
				return getState().getDbId(ref);
			}
		}else{
			throw new ClassCastException("CdmBase for "+this.getClass().getName() +" must be of type DescriptionElementBase, but was " + cdmBase.getClass());
		}
	}

	private boolean isPesiSource(DescriptionElementSource source) {
		boolean result = true;
		if ( exclude.contains(EXCLUDE.NONE)){
			return true;
		}
		if ( exclude.contains(EXCLUDE.WITH_ID)){
			result &= StringUtils.isBlank(source.getIdInSource());
		}else if  ( exclude.contains(EXCLUDE.WITH_NAMESPACE)){
			result &= StringUtils.isBlank(source.getIdNamespace());
		}else{
			logger.warn("isPesiSource case not yet handled for " + this.exclude);
		}
		return result;
	}

	@Override
	protected int getSqlType() {
		if (isCache){
			return Types.VARCHAR;
		}else{
			return Types.INTEGER;
		}
	}

	@Override
	public Class<?> getTypeClass() {
		if (isCache){
			return String.class;
		}else{
			return Integer.class;
		}
	}
}
