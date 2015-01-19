// $Id$
/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.dao.jdbc.validation;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.EntityValidationResult;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationResultCrud;

/**
 * @author ayco_holleman
 * @date 16 jan. 2015
 *
 */
public class EntityValidationResultCrudJdbcImpl implements IEntityValidationResultCrud {

    private static final Logger logger = Logger.getLogger(EntityValidationResultCrudJdbcImpl.class);

    private static final String vr_sql = "INSERT INTO entityvalidationresult (id, created, uuid, "
            + "crudeventtype, validatedentityclass,  validatedentityid, validatedentityuuid, "
            + "userfriendlydescription, userfriendlytypename, createdby_id) VALUES (?,?,?,?,?,?,?,?,?,?)";

    private static final int vr_id = 1;
    private static final int vr_created = 2;
    private static final int vr_uuid = 3;
    private static final int vr_crudeventtype = 4;
    private static final int vr_validatedentityclass = 5;
    private static final int vr_validatedentityid = 6;
    private static final int vr_validatedentityuuid = 7;
    private static final int vr_userfriendlydescription = 8;
    private static final int vr_userfriendlytypename = 9;
    private static final int vr_createdby_id = 10;

    private static final String cv_sql = "INSERT INTO entityconstraintviolation (id, created, uuid, "
            + "invalidvalue, message, propertypath, userfriendlyfieldname, severity, validator, "
            + "createdby_id, entityvalidationresult_id) VALUES (?,?,?,?,?,?,?,?,?,?,?)";

    private static final int cv_id = 1;
    private static final int cv_created = 2;
    private static final int cv_uuid = 3;
    private static final int cv_invalidvalue = 4;
    private static final int cv_message = 5;
    private static final int cv_propertypath = 6;
    private static final int cv_userfriendlyfieldname = 7;
    private static final int cv_severity = 8;
    private static final int cv_validator = 9;
    private static final int cv_createdby_id = 10;
    private static final int cv_entityvalidationresult_id = 11;

    private ICdmDataSource datasource;

    public EntityValidationResultCrudJdbcImpl() {

    }

    public EntityValidationResultCrudJdbcImpl(ICdmDataSource datasource) {
        this.datasource = datasource;
    }

    @Override
    public <T extends CdmBase> void saveValidationResult(Set<ConstraintViolation<T>> errors, T entity,
            CRUDEventType crudEventType) {

        deleteValidationResult(entity.getClass().getName(), entity.getId());

        Connection connection = null;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;

        try {
            connection = datasource.getConnection();
            datasource.startTransaction();
            stmt1 = connection.prepareStatement(vr_sql);
            stmt2 = connection.prepareStatement(cv_sql);
            int id = 1 + fetchInt(connection, "SELECT MAX(id) FROM entityvalidationresult");
            saveValidationResult(stmt1, entity, crudEventType, id);
            saveErrors(stmt2, errors, entity, id);
            datasource.commitTransaction();
        } catch (SQLException e) {
            try {
                if (connection != null && !connection.getAutoCommit()) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                logger.error("Error during rollback", ex);
            }
            logger.error("Problems while saving validation result for entity " + entity, e);
        } finally {
            close(stmt1);
            close(stmt2);
        }
    }

    private <T extends CdmBase> void saveValidationResult(PreparedStatement stmt, T entity,
            CRUDEventType crudEventType, int validationResultId) throws SQLException {
        EntityValidationResult validationResult = EntityValidationResult.newInstance(entity, crudEventType);
        stmt.setInt(vr_id, validationResultId);
        stmt.setDate(vr_created, new Date(validationResult.getCreated().getMillis()));
        stmt.setString(vr_uuid, validationResult.getUuid().toString());
        stmt.setString(vr_crudeventtype, validationResult.getCrudEventType().toString());
        stmt.setString(vr_validatedentityclass, validationResult.getValidatedEntityClass());
        stmt.setInt(vr_validatedentityid, validationResult.getValidatedEntityId());
        stmt.setString(vr_validatedentityuuid, validationResult.getValidatedEntityUuid().toString());
        stmt.setString(vr_userfriendlydescription, validationResult.getUserFriendlyDescription());
        stmt.setString(vr_userfriendlytypename, validationResult.getUserFriendlyTypeName());
        if (validationResult.getCreatedBy() != null) {
            stmt.setInt(vr_createdby_id, validationResult.getCreatedBy().getId());
        }
        stmt.executeUpdate();
    }

    private <T extends CdmBase> void saveErrors(PreparedStatement stmt, Set<ConstraintViolation<T>> errors, T entity,
            int validationResultId) throws SQLException {
        for (ConstraintViolation<T> error : errors) {
            EntityConstraintViolation ecv = EntityConstraintViolation.newInstance(entity, error);
            int maxId = fetchInt(stmt.getConnection(), "SELECT MAX(id) FROM entityconstraintviolation");
            stmt.setInt(cv_id, maxId + 1);
            stmt.setDate(cv_created, new Date(ecv.getCreated().getMillis()));
            stmt.setString(cv_uuid, ecv.getUuid().toString());
            stmt.setString(cv_invalidvalue, ecv.getInvalidValue());
            stmt.setString(cv_message, ecv.getMessage());
            stmt.setString(cv_propertypath, ecv.getPropertyPath());
            stmt.setString(cv_userfriendlyfieldname, ecv.getUserFriendlyFieldName());
            stmt.setString(cv_severity, ecv.getSeverity().toString());
            stmt.setString(cv_validator, ecv.getValidator());
            if (ecv.getCreatedBy() != null) {
                stmt.setInt(cv_createdby_id, ecv.getCreatedBy().getId());
            }
            stmt.setInt(cv_entityvalidationresult_id, validationResultId);
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteValidationResult(String validatedEntityClass, int validatedEntityId) {

        int resultId = getValidationResultId(validatedEntityClass, validatedEntityId);

        if (resultId == -1) {
            return;
        }

        String sql = "DELETE FROM EntityValidationResult vr WHERE vr.id = ?";
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = datasource.getConnection();
            datasource.startTransaction();
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, resultId);
            stmt.executeUpdate();
            stmt.close();
            sql = "DELETE FROM EntityConstraintViolation cv WHERE cv.entityvalidationresult_id = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, resultId);
            stmt.executeUpdate();
            datasource.commitTransaction();
        } catch (SQLException e) {
            try {
                if (connection != null && !connection.getAutoCommit()) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                logger.warn("Error during rollback");
            }
            logger.error("Problems when executing SQL \n:" + sql + "\nException: ", e);
        } finally {
            close(stmt);
        }
    }

    private int getValidationResultId(String validatedEntityClass, int validatedEntityId) {
        String sql = "SELECT id FROM EntityValidationResult vr "
                + "WHERE vr.validatedEntityClass = ? AND vr.validatedEntityId = ?";
        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = datasource.getConnection();
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, validatedEntityClass);
            stmt.setInt(2, validatedEntityId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            try {
                if (connection != null && !connection.getAutoCommit()) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                logger.warn("Error during rollback");
            }
            logger.error("Problems while retrieving validation result id", e);
        } finally {
            close(stmt);
        }
        return -1;
    }

    /**
     * @param datasource
     *            the datasource to set
     */
    public void setDatasource(ICdmDataSource datasource) {
        this.datasource = datasource;
    }

    private static int fetchInt(Connection connection, String sql) throws SQLException {
        int result = -1;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                result = rs.getInt(1);
            }
        } finally {
            close(stmt);
        }
        return result;
    }

    private static void close(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.error("Error closing JDBC Statement", e);
            }
        }
    }

}
