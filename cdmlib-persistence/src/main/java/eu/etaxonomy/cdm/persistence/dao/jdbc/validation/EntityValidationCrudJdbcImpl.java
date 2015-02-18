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

import javax.sql.DataSource;
import javax.validation.ConstraintViolation;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.validation.CRUDEventType;
import eu.etaxonomy.cdm.model.validation.EntityConstraintViolation;
import eu.etaxonomy.cdm.model.validation.EntityValidation;
import eu.etaxonomy.cdm.model.validation.Severity;
import eu.etaxonomy.cdm.persistence.dao.jdbc.JdbcDaoUtils;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationCrud;

/**
 * @author ayco_holleman
 * @date 16 jan. 2015
 *
 */
@Repository
public class EntityValidationCrudJdbcImpl implements IEntityValidationCrud {

    public static final Logger logger = Logger.getLogger(EntityValidationCrudJdbcImpl.class);

    private static final String SQL_INSERT_VALIDATION_RESULT = "INSERT INTO entityvalidation"
            + "(id, created, uuid,  crudeventtype, validatedentityclass, validatedentityid,"
            + "validatedentityuuid, userfriendlydescription, userfriendlytypename, validationcount,"
            + "lastmodified, createdby_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final int vr_id = 1;
    private static final int vr_created = 2;
    private static final int vr_uuid = 3;
    private static final int vr_crudeventtype = 4;
    private static final int vr_validatedentityclass = 5;
    private static final int vr_validatedentityid = 6;
    private static final int vr_validatedentityuuid = 7;
    private static final int vr_userfriendlydescription = 8;
    private static final int vr_userfriendlytypename = 9;
    private static final int vr_validationcount = 10;
    private static final int vr_lastmodified = 11;
    private static final int vr_createdby_id = 12;

    private static final String SQL_INSERT_CONSTRAINT_VIOLATION = "INSERT INTO entityconstraintviolation"
            + "(id, created, uuid,  invalidvalue, message, propertypath, userfriendlyfieldname, severity,"
            + "validator, validationgroup, createdby_id, entityvalidation_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final int cv_id = 1;
    private static final int cv_created = 2;
    private static final int cv_uuid = 3;
    private static final int cv_invalidvalue = 4;
    private static final int cv_message = 5;
    private static final int cv_propertypath = 6;
    private static final int cv_userfriendlyfieldname = 7;
    private static final int cv_severity = 8;
    private static final int cv_validator = 9;
    private static final int cv_validationgroup = 10;
    private static final int cv_createdby_id = 11;
    private static final int cv_entityvalidation_id = 12;

    @Autowired
    private DataSource datasource;

    public EntityValidationCrudJdbcImpl() {

    }

    public EntityValidationCrudJdbcImpl(DataSource datasource) {
        this.datasource = datasource;
    }

    public void setDatasource(DataSource datasource) {
        this.datasource = datasource;
    }

    @Override
    public <T extends ICdmBase> void saveEntityValidation(T validatedEntity, Set<ConstraintViolation<T>> errors,
            CRUDEventType crudEventType, Class<?>[] validationGroups) {
    	Connection conn = null;
        try {
            conn = datasource.getConnection();
            JdbcDaoUtils.startTransaction(conn);
            String entityClass = validatedEntity.getClass().getName();
            int entityId = validatedEntity.getId();
            EntityValidation previousResult = getValidationResultRecord(conn, entityClass, entityId);
            if (previousResult == null) {
                /*
                 * The entity has never been validated before. We should now
                 * create an entityvalidation record whether or not the
                 * entity has errors, because the entity HAS been validated so
                 * its validationcount is now 1.
                 */
                int validationResultId = saveValidationResultRecord(conn, validatedEntity, crudEventType);
                if (errors.size() != 0) {
                    saveErrorRecords(conn, validationResultId, validatedEntity, errors);
                }
            } else {
                deletedErrorRecords(conn, previousResult.getId(), validationGroups);
                if (errors.size() != 0) {
                    updateValidationResultRecord(conn, previousResult.getId(), crudEventType);
                    saveErrorRecords(conn, previousResult.getId(), validatedEntity, errors);
                }
            }
            conn.commit();
        } catch (Throwable t) {
            logger.error("Error while saving validation result:", t);
            JdbcDaoUtils.rollback(conn);
        }
    }

    @Override
    public void deleteEntityValidation(String validatedEntityClass, int validatedEntityId) {
    	Connection conn = null;
        try {
            conn = datasource.getConnection();
            JdbcDaoUtils.startTransaction(conn);
            int validationResultId = getValidationResultId(conn, validatedEntityClass, validatedEntityId);
            if (validationResultId == -1) {
                return;
            }
            deleteValidationResultRecord(conn, validationResultId);
            deletedErrorRecords(conn, validationResultId, null);
            conn.commit();
        } catch (Throwable t) {
            JdbcDaoUtils.rollback(conn);
        }
    }

    private static void deletedErrorRecords(Connection conn, int validationResultId, Class<?>[] validationGroups)
            throws SQLException {
        StringBuilder sql = new StringBuilder(127);
        sql.append("DELETE FROM entityconstraintviolation WHERE entityvalidation_id = ?");
        if (validationGroups != null && validationGroups.length != 0) {
            sql.append(" AND (");
            for (int i = 0; i < validationGroups.length; ++i) {
                if (i != 0) {
                    sql.append(" OR ");
                }
                sql.append("validationgroup = ?");
            }
            sql.append(")");
        }
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql.toString());
            stmt.setInt(1, validationResultId);
            if (validationGroups != null && validationGroups.length != 0) {
                for (int i = 0; i < validationGroups.length; ++i) {
                    stmt.setString(i + 2, validationGroups[i].getName());
                }
            }
            stmt.executeUpdate();
        } finally {
            JdbcDaoUtils.close(stmt);
        }
    }

    private static <T extends ICdmBase> int saveValidationResultRecord(Connection conn, T entity,
            CRUDEventType crudEventType) throws SQLException {
        PreparedStatement stmt = null;
        int entityValidationId;
        try {
            stmt = conn.prepareStatement(SQL_INSERT_VALIDATION_RESULT);
            entityValidationId = 10 + JdbcDaoUtils.fetchInt(conn, "SELECT MAX(id) FROM entityvalidation");
            EntityValidation validationResult = EntityValidation.newInstance(entity, crudEventType);
            stmt.setInt(vr_id, entityValidationId);
            stmt.setDate(vr_created, new Date(validationResult.getCreated().getMillis()));
            stmt.setString(vr_uuid, validationResult.getUuid().toString());
            stmt.setString(vr_crudeventtype, validationResult.getCrudEventType().toString());
            stmt.setString(vr_validatedentityclass, validationResult.getValidatedEntityClass());
            stmt.setInt(vr_validatedentityid, validationResult.getValidatedEntityId());
            stmt.setString(vr_validatedentityuuid, validationResult.getValidatedEntityUuid().toString());
            stmt.setString(vr_userfriendlydescription, validationResult.getUserFriendlyDescription());
            stmt.setString(vr_userfriendlytypename, validationResult.getUserFriendlyTypeName());
            stmt.setInt(vr_validationcount, 1);
            stmt.setDate(vr_lastmodified, new Date(validationResult.getCreated().getMillis()));
            if (validationResult.getCreatedBy() != null) {
                stmt.setInt(vr_createdby_id, validationResult.getCreatedBy().getId());
            } else {
                stmt.setNull(vr_createdby_id, Types.INTEGER);
            }
            stmt.executeUpdate();
        } finally {
            JdbcDaoUtils.close(stmt);
        }
        return entityValidationId;
    }

    private static void updateValidationResultRecord(Connection conn, int validationResultId,
            CRUDEventType crudEventType) throws SQLException {
        String sql = "UPDATE entityvalidation SET crudeventtype=?, "
                + " validationcount = validationcount + 1, lastmodified=? WHERE id=?";
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, crudEventType.toString());
            stmt.setDate(2, new Date(new java.util.Date().getTime()));
            stmt.setInt(3, validationResultId);
            stmt.executeUpdate();
        } finally {
            JdbcDaoUtils.close(stmt);
        }
    }

    private static void deleteValidationResultRecord(Connection conn, int validationResultId) throws SQLException {
        String sql = "DELETE FROM entityvalidation WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, validationResultId);
        stmt.executeUpdate();
    }

    private static <T extends ICdmBase> void saveErrorRecords(Connection conn, int validationResultId, T entity,
            Set<ConstraintViolation<T>> errors) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(SQL_INSERT_CONSTRAINT_VIOLATION);
            for (ConstraintViolation<T> error : errors) {
                EntityConstraintViolation ecv = EntityConstraintViolation.newInstance(entity, error);
                int maxId = JdbcDaoUtils.fetchInt(conn, "SELECT MAX(id) FROM entityconstraintviolation");
                stmt.setInt(cv_id, maxId + 1);
                stmt.setDate(cv_created, new Date(ecv.getCreated().getMillis()));
                stmt.setString(cv_uuid, ecv.getUuid().toString());
                stmt.setString(cv_invalidvalue, ecv.getInvalidValue());
                stmt.setString(cv_message, ecv.getMessage());
                stmt.setString(cv_propertypath, ecv.getPropertyPath());
                stmt.setString(cv_userfriendlyfieldname, ecv.getUserFriendlyFieldName());
                stmt.setString(cv_severity, ecv.getSeverity().toString());
                stmt.setString(cv_validator, ecv.getValidator());
                stmt.setString(cv_validationgroup, ecv.getValidationGroup());
                if (ecv.getCreatedBy() != null) {
                    stmt.setInt(cv_createdby_id, ecv.getCreatedBy().getId());
                } else {
                    stmt.setNull(cv_createdby_id, Types.INTEGER);
                }
                stmt.setInt(cv_entityvalidation_id, validationResultId);
                stmt.executeUpdate();
            }
        } finally {
            JdbcDaoUtils.close(stmt);
        }
    }

    // Called by unit test
    EntityValidation getValidationResult(String validatedEntityClass, int validatedEntityId) {
    	Connection conn = null;
        try {
            conn = datasource.getConnection();
            JdbcDaoUtils.startTransaction(conn);
            EntityValidation result = getValidationResultRecord(conn, validatedEntityClass, validatedEntityId);
            if (result != null) {
                result.setEntityConstraintViolations(getErrorRecords(conn, result.getId()));
            }
            conn.commit();
            return result;
        } catch (Throwable t) {
            logger.error("Error while retrieving validation result", t);
            JdbcDaoUtils.rollback(conn);
            return null;
        }
    }

    private static <T extends ICdmBase> EntityValidation getValidationResultRecord(Connection conn,
            String validatedEntityClass, int validatedEntityId) throws SQLException {
        String sql = "SELECT * FROM entityvalidation WHERE validatedentityclass=? AND validatedentityid=?";
        EntityValidation result = null;
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, validatedEntityClass);
            stmt.setInt(2, validatedEntityId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                result = EntityValidation.newInstance();
                result.setId(rs.getInt("id"));
                result.setCreated(new DateTime(rs.getDate("created").getTime()));
                result.setUuid(UUID.fromString(rs.getString("uuid")));
                String s = rs.getString("crudeventtype");
                if (!rs.wasNull()) {
                    result.setCrudEventType(CRUDEventType.valueOf(s));
                }
                result.setValidatedEntityClass(rs.getString("validatedentityclass"));
                result.setValidatedEntityId(rs.getInt("validatedentityid"));
                result.setValidatedEntityUuid(UUID.fromString(rs.getString("validatedentityuuid")));
                result.setUserFriendlyDescription(rs.getString("userfriendlydescription"));
                result.setUserFriendlyTypeName(rs.getString("userfriendlytypename"));
            }
            rs.close();
            return result;
        } finally {
            JdbcDaoUtils.close(stmt);
        }
    }

    private static int getValidationResultId(Connection conn, String validatedEntityClass, int validatedEntityId)
            throws SQLException {
        String sql = "SELECT id FROM entityvalidation WHERE validatedentityclass = ? AND validatedentityid = ?";
        PreparedStatement stmt = null;
        int result = -1;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, validatedEntityClass);
            stmt.setInt(2, validatedEntityId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                result = rs.getInt(1);
            }
            rs.close();
        } finally {
            JdbcDaoUtils.close(stmt);
        }
        return result;
    }

    private static Set<EntityConstraintViolation> getErrorRecords(Connection conn, int validationResultId)
            throws SQLException {
        String sql = "SELECT * FROM entityconstraintviolation WHERE entityvalidation_id=?";
        PreparedStatement stmt = null;
        Set<EntityConstraintViolation> errors = new HashSet<EntityConstraintViolation>();
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, validationResultId);
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
                error.setValidationGroup(rs.getString("validationgroup"));
                errors.add(error);
            }
            rs.close();
        } finally {
            JdbcDaoUtils.close(stmt);
        }
        return errors;
    }
}
