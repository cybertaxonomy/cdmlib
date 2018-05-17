/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbImportStateBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.common.MarkerType;


/**
 * Object creation mapper which creates a marker.
 * 
 * @author a.mueller
 * @since 11.03.2010
 * @version 1.0
 */
public class DbImportMarkerCreationMapper extends DbImportSupplementCreationMapperBase<Marker, AnnotatableEntity, DbImportStateBase<?, ?>, MarkerType> {
	private static final Logger logger = Logger.getLogger(DbImportMarkerCreationMapper.class);

//************************** FACTORY METHODS ***************************************************************/
	
	
	/**
	 * Creates a marker mapper which creates an empty marker.
	 * 
	 * @param dbMarkedObjectAttribute
	 * @param markedObjectNamespace
	 * @return
	 */
	public static DbImportMarkerCreationMapper NewInstance(String dbMarkedObjectAttribute, String markedObjectNamespace){
		return new DbImportMarkerCreationMapper(dbMarkedObjectAttribute, markedObjectNamespace, null, null, null);
	}
	
	/**
	 * Creates a marker mapper which creates a marker and sets the marker flag and the marker type and adds
	 * an Annotation holding the original source id
     *
	 * @param dbMarkedObjectAttribute
	 * @param markedObjectNamespace
	 * @param dbMarkerValueAttribute
	 * @param dbIdAttribute
	 * @param markerType
	 * @return
	 */
	public static DbImportMarkerCreationMapper NewInstance(String dbMarkedObjectAttribute, String markedObjectNamespace, String dbMarkerValueAttribute, String dbIdAttribute, MarkerType markerType){
		return new DbImportMarkerCreationMapper(dbMarkedObjectAttribute, markedObjectNamespace, dbMarkerValueAttribute, dbIdAttribute, markerType);
	}

	
//********************************* CONSTRUCTOR ****************************************/

	/**
	 * @param dbSupplementValueAttribute
	 * @param dbSupplementedObjectAttribute
	 * @param dbIdAttribute
	 * @param supplementedObjectNamespace
	 * @param supplementType
	 */
	protected DbImportMarkerCreationMapper(String dbSupplementedObjectAttribute, String supplementedObjectNamespace, String dbSupplementValueAttribute, String dbIdAttribute, MarkerType supplementType) {
		super(dbSupplementValueAttribute, dbSupplementedObjectAttribute, dbIdAttribute, supplementedObjectNamespace, supplementType);
	}

//************************************ METHODS *******************************************/

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbImportSupplementCreationMapperBase#addSupplement(eu.etaxonomy.cdm.model.common.AnnotatableEntity, java.lang.String, eu.etaxonomy.cdm.model.common.AnnotatableEntity)
	 */
	@Override
	protected boolean addSupplement(Marker marker, AnnotatableEntity annotatableEntity, String id) {
		if (annotatableEntity != null){
			annotatableEntity.addMarker(marker);
			return true;
		}else{
			String warning = "Annotatable entity (" + id + ") for marker not found. Marker not created.";
			logger.warn(warning);
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbImportSupplementCreationMapperBase#setSupplementValue(java.lang.Object)
	 */
	@Override
	protected void setSupplementValue(ResultSet rs, Marker marker) throws SQLException {
		Boolean value = rs.getBoolean(dbSupplementValueAttribute);
		marker.setFlag(value);
		
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.io.common.mapping.DbImportObjectCreationMapperBase#createObject(java.sql.ResultSet)
	 */
	@Override
	protected Marker createObject(ResultSet rs) throws SQLException {
		Marker marker = Marker.NewInstance();
		marker.setMarkerType(supplementType);
		return marker;
	}

	
}
