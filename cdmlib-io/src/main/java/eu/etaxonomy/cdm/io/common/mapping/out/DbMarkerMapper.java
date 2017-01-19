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

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;

/**
 * @author a.mueller
 * @created 12.05.2009
 * @version 1.0
 */
public class DbMarkerMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbMarkerMapper.class);

	private final MarkerType markerType;

	public static DbMarkerMapper NewInstance(MarkerType markerType, String dbAttributeString){
		return new DbMarkerMapper(markerType, dbAttributeString, null);
	}

	public static DbMarkerMapper NewInstance(MarkerType markerType, String dbAttributeString, boolean defaultValue){
		return new DbMarkerMapper(markerType, dbAttributeString, defaultValue);
	}

	/**
	 * @param dbAttributeString
	 * @param cdmAttributeString
	 */
	private DbMarkerMapper(MarkerType markerType, String dbAttributeString, Boolean defaultValue) {
		super("markers", dbAttributeString, defaultValue);
		this.markerType  = markerType;

	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValue(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	@Override
	protected Object getValue(CdmBase cdmBase) {
		Boolean result = (Boolean)this.defaultValue;
		if (cdmBase.isInstanceOf(AnnotatableEntity.class)){
			AnnotatableEntity annotatableEntity = (AnnotatableEntity)cdmBase;
			for (Marker marker : annotatableEntity.getMarkers()){
				if (this.markerType != null && this.markerType.equals(marker.getMarkerType())){
					return marker.getFlag();
				}
			}
		}else{
			throw new ClassCastException("CdmBase for DbMarkerMapper must be of type AnnotatableEntity, but was " + cdmBase.getClass());
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.berlinModel.out.mapper.DbSingleAttributeExportMapperBase#getValueType()
	 */
	@Override
	protected int getSqlType() {
		return Types.BOOLEAN;
	}


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.CdmSingleAttributeMapperBase#getTypeClass()
	 */
	@Override
	public Class<?> getTypeClass() {
		return Boolean.class;
	}
}
