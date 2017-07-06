/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.database.update.v31_33;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.monitor.IProgressMonitor;
import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.database.update.CaseType;
import eu.etaxonomy.cdm.database.update.SchemaUpdateResult;
import eu.etaxonomy.cdm.database.update.SchemaUpdaterStepBase;

/**
 * @author a.mueller
 * @date 15.12.2013
 */
public class SpecimenMediaMoverUpdater
            extends SchemaUpdaterStepBase{

    private static final Logger logger = Logger.getLogger(SpecimenMediaMoverUpdater.class);

	private static final String stepName = "Update rank class values";

// **************************** STATIC METHODS ********************************/

	public static final SpecimenMediaMoverUpdater NewInstance(){
		return new SpecimenMediaMoverUpdater(stepName);
	}

	protected SpecimenMediaMoverUpdater(String stepName) {
		super(stepName);
	}

    @Override
    public void invoke(ICdmDataSource datasource, IProgressMonitor monitor,
            CaseType caseType, SchemaUpdateResult result) throws SQLException {

		try {
			Integer featureId = null;

			//get existing media
			String sql = caseType.replaceTableNames(
					" SELECT SpecimenOrObservationBase_id, media_id " +
					" FROM @@SpecimenOrObservationBase_Media@@");
			ResultSet rs = datasource.executeQuery(sql);
			while (rs.next()){
				if (featureId == null){
					featureId = getFeatureId(datasource, caseType);
				}

				Integer specimenId = rs.getInt("SpecimenOrObservationBase_id");
				Integer mediaId = rs.getInt("media_id");

				//image gallery
				Number galleryId = getOrCreateImageGallery(datasource, monitor, specimenId, caseType);

				//textData
				Number textDataId = getOrCreateTextData(datasource, monitor, galleryId, featureId, caseType);

				//sortIndex
				Number sortIndex = getSortIndex(datasource, monitor, textDataId, mediaId, caseType);

				//insert
				sql = caseType.replaceTableNames(
						" INSERT INTO @@DescriptionElementBase_Media@@" +
								" (DescriptionElementBase_id, media_id, sortIndex) " +
						" VALUES (%d, %d, %d)");
				sql = String.format(sql, textDataId, mediaId, sortIndex);
				datasource.executeUpdate(sql);
			}

			return;
		} catch (Exception e) {
			String message = e.getMessage();
		    monitor.warning(message, e);
			logger.warn(message);
			result.addException(e, message, this, "invoke");
			return;
		}
	}

	private Integer getFeatureId(ICdmDataSource datasource, CaseType caseType) throws SQLException {
		String sql = caseType.replaceTableNames(
				" SELECT id " +
				" FROM @@DefinedTermBase@@ " +
				" WHERE uuid = '84193b2c-327f-4cce-90ef-c8da18fd5bb5'");
		return (Integer)datasource.getSingleValue(sql);
	}

	private Number getSortIndex(ICdmDataSource datasource, IProgressMonitor monitor, Number textDataId, Integer mediaId, CaseType caseType) throws SQLException {
		String sql = caseType.replaceTableNames(
				" SELECT max(sortIndex) " +
				" FROM @@DescriptionElementBase_Media@@ MN " +
				" WHERE MN.DescriptionElementBase_id = "+textDataId+" AND MN.media_id = " + mediaId);
		Number sortIndex = (Long)datasource.getSingleValue(sql);
		if (sortIndex == null){
			sortIndex = 0;
		}

		return sortIndex;
	}

	private Number getOrCreateTextData(ICdmDataSource datasource, IProgressMonitor monitor, Number galleryId, Integer featureId, CaseType caseType) throws SQLException {

		String sql = caseType.replaceTableNames(
				" SELECT deb.id " +
				" FROM @@DescriptionElementBase@@ deb " +
				" WHERE deb.DTYPE = 'TextData' AND feature_id = "+ featureId +" AND deb.indescription_id = " + galleryId);
		Number textDataId = (Integer)datasource.getSingleValue(sql);

		if (textDataId == null){
			sql = caseType.replaceTableNames(
					" SELECT max(id)+1 FROM @@DescriptionElementBase@@ ");
			textDataId = (Long)datasource.getSingleValue(sql);

			sql = caseType.replaceTableNames(
					" INSERT INTO @@DescriptionElementBase@@ (DTYPE, id, created, uuid, feature_id, indescription_id)  " +
					" VALUES  ('TextData', %d, '%s', '%s', %d, %d) ");
			sql = String.format(sql, textDataId, this.getNowString(), UUID.randomUUID().toString(), featureId, galleryId);
			datasource.executeUpdate(sql);
		}
		return textDataId;

	}

	private Number getOrCreateImageGallery(ICdmDataSource datasource, IProgressMonitor monitor, Integer specimenId, CaseType caseType) throws SQLException {
		String sql = caseType.replaceTableNames(
				" SELECT  db.id " +
				" FROM @@DescriptionBase@@ db " +
				" WHERE db.imagegallery = True AND db.specimen_id = " + specimenId);
		Number descriptionId = (Number)datasource.getSingleValue(sql);

		if (descriptionId == null){
			sql = caseType.replaceTableNames(
					" SELECT max(id)+1 FROM @@DescriptionBase@@ ");
			descriptionId = (Long)datasource.getSingleValue(sql);

			sql = caseType.replaceTableNames(
					" INSERT INTO @@DescriptionBase@@ (DTYPE, id, created, uuid, protectedtitlecache, titleCache, imagegallery, specimen_id) " +
					" VALUES ('SpecimenDescription',  %d, '%s', '%s', 1, 'Specimenimage(s) moved for schema update', 1, %d) ");
			sql = String.format(sql, descriptionId, this.getNowString(), UUID.randomUUID().toString(), specimenId);
			datasource.executeUpdate(sql);
		}
		return descriptionId;
	}

}
