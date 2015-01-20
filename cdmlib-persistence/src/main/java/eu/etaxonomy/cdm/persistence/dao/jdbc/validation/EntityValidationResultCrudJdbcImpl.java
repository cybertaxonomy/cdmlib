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
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.database.ICdmDataSource;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.EntityValidationResult;
import eu.etaxonomy.cdm.model.validation.Severity;
import eu.etaxonomy.cdm.persistence.dao.jdbc.JdbcDaoUtils;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationResultCrud;

/**
 * @author ayco_holleman
 * @date 16 jan. 2015
 *
 */
public class EntityValidationResultCrudJdbcImpl implements IEntityValidationResultCrud {

    public static final Logger logger = Logger.getLogger(EntityValidationResultCrudJdbcImpl.class);

    private static final String VR_SQL_INSERT = "INSERT INTO entityvalidationresult (id, created, uuid, "
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

    private static final String CV_SQL_INSERT = "INSERT INTO entityconstraintviolation (id, created, uuid, "
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
        deleteValidationResult(entity.getClass().getName(),entity.getId());
        Connection conn = null;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        try {
            conn = datasource.getConnection();
            datasource.startTransaction();
            stmt1 = conn.prepareStatement(VR_SQL_INSERT);
            stmt2 = conn.prepareStatement(CV_SQL_INSERT);
            int nextId = 1 + JdbcDaoUtils.fetchInt(conn, "SELECT MAX(id) FROM entityvalidationresult");
            saveValidationResult(stmt1, entity, crudEventType, nextId);
            saveErrors(stmt2, errors, entity, nextId);
            datasource.commitTransaction();
        } catch (SQLException e) {
            JdbcDaoUtils.rollback(conn);
            logger.error("Problems while saving validation result for entity " + entity, e);
        } finally {
            JdbcDaoUtils.close(stmt1);
            JdbcDaoUtils.close(stmt2);
        }
    }

    @Override
    public void deleteValidationResult(String validatedEntityClass, int validatedEntityId) {
        int resultId = getValidationResultId(validatedEntityClass,validatedEntityId);
        if (resultId == -1) {
            return;
        }
        String vrSqlDelete = "DELETE FROM entityvalidationresult WHERE id = ?";
        String cvSqlDelete = "DELETE FROM entityconstraintviolation WHERE entityvalidationresult_id = ?";
        Connection conn = null;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        try {
            conn = datasource.getConnection();
            datasource.startTransaction();
            stmt1 = conn.prepareStatement(vrSqlDelete);
            stmt2 = conn.prepareStatement(cvSqlDelete);
            stmt1.setInt(1, resultId);
            stmt1.executeUpdate();
            stmt2.setInt(1, resultId);
            stmt2.executeUpdate();
            datasource.commitTransaction();
        } catch (SQLException e) {
            JdbcDaoUtils.rollback(conn);
            logger.error("Error while deleting validation result", e);
        } finally {
            JdbcDaoUtils.close(stmt1);
            JdbcDaoUtils.close(stmt2);
        }
    }

    /**
     * @param datasource
     *            the datasource to set
     */
    public void setDatasource(ICdmDataSource datasource) {
        this.datasource = datasource;
    }

    public EntityValidationResult getValidationResult(CdmBase entity) {
        String sql1 = "SELECT * FROM entityvalidationresult WHERE validatedentityclass=? AND validatedentityid=?";
        String sql2 = "SELECT * FROM entityconstraintviolation WHERE entityvalidationresult_id=?";
        Connection connection = null;
        PreparedStatement stmt1 = null;
        PreparedStatement stmt2 = null;
        EntityValidationResult result = null;
        try {
            connection = datasource.getConnection();
            stmt1 = connection.prepareStatement(sql1);
            result = getValidationResult(stmt1, entity);
            stmt2 = connection.prepareStatement(sql2);
            Set<EntityConstraintViolation> errors = getErrors(stmt2, result.getId());
            result.setEntityConstraintViolations(errors);
        } catch (SQLException e) {
            logger.error("Error while retrieving validation result", e);
        } finally {
            JdbcDaoUtils.close(stmt1);
            JdbcDaoUtils.close(stmt2);
        }
        return result;
    }

    private EntityValidationResult getValidationResult(PreparedStatement stmt, CdmBase entity) throws SQLException {
        EntityValidationResult result = null;
        stmt.setString(1, entity.getClass().getName());
        stmt.setInt(2, entity.getId());
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            result = EntityValidationResult.newInstance();
            result.setId(rs.getInt("id"));
            result.setCreated(new DateTime(rs.getDate("created").getTime()));
            result.setUuid(UUID.fromString(rs.getString("uuid")));
            result.setCrudEventType(CRUDEventType.valueOf(rs.getString("crudeventtype")));
            result.setValidatedEntityClass(rs.getString("validatedentityclass"));
            result.setValidatedEntityId(rs.getInt("validatedentityid"));
            result.setValidatedEntityUuid(UUID.fromString(rs.getString("validatedentityuuid")));
            result.setUserFriendlyDescription(rs.getString("userfriendlydescription"));
            result.setUserFriendlyTypeName(rs.getString("userfriendlytypename"));
        }
        return result;
    }

    private Set<EntityConstraintViolation> getErrors(PreparedStatement stmt, int resultId) throws SQLException {
        Set<EntityConstraintViolation> errors = new HashSet<EntityConstraintViolation>();
        stmt.setInt(1, resultId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            EntityConstraintViolation error = EntityConstraintViolation.newInstance();
            error.setId(rs.getInt("id"));
            error.setCreated(new DateTime(rs.getDate("created").getTime()));
            error.setUuid(UUID.fromString(rs.getString("uuid")));
            error.setInvalidValue(rs.getString("invalidvalue"));
            error.setMessage(rs.getString("message"));
            error.setPropertyPath(rs.getString("propertypath"));
            error.setUserFriendlyFieldName(rs.getString("userfriendlyfieldname"));
            error.setSeverity(Severity.forName(rs.getString("severity")));
            error.setValidator(rs.getString("validator"));
            errors.add(error);
        }
        return errors;
    }

    private void saveValidationResult(PreparedStatement stmt, CdmBase entity, CRUDEventType crudEventType,
            int validationResultId) throws SQLException {
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
        } else {
            stmt.setNull(vr_createdby_id, Types.INTEGER);
        }
        stmt.executeUpdate();
    }

    private <T extends CdmBase> void saveErrors(PreparedStatement stmt, Set<ConstraintViolation<T>> errors, T entity,
            int validationResultId) throws SQLException {
        for (ConstraintViolation<T> error : errors) {
            EntityConstraintViolation ecv = EntityConstraintViolation.newInstance(entity, error);
            int maxId = JdbcDaoUtils.fetchInt(stmt.getConnection(), "SELECT MAX(id) FROM entityconstraintviolation");
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
            } else {
                stmt.setNull(cv_createdby_id, Types.INTEGER);
            }
            stmt.setInt(cv_entityvalidationresult_id, validationResultId);
            stmt.executeUpdate();
        }
    }

    private int getValidationResultId(String validatedEntityClass, int validatedEntityId) {
        String sql = "SELECT id FROM EntityValidationResult WHERE validatedEntityClass = ? AND validatedEntityId = ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = datasource.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, validatedEntityClass);
            stmt.setInt(2, validatedEntityId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error while retrieving validation result id", e);
        } finally {
            JdbcDaoUtils.close(stmt);
        }
        return -1;
    }

}
