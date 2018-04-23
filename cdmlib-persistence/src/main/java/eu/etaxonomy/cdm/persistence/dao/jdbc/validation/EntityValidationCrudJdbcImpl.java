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
import java.util.Iterator;
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
import eu.etaxonomy.cdm.model.validation.EntityValidationStatus;
import eu.etaxonomy.cdm.model.validation.Severity;
import eu.etaxonomy.cdm.persistence.dao.jdbc.JdbcDaoUtils;
import eu.etaxonomy.cdm.persistence.dao.validation.IEntityValidationCrud;

/**
 * @author ayco_holleman
 * @since 16 jan. 2015
 *
 */
@Repository
public class EntityValidationCrudJdbcImpl implements IEntityValidationCrud {

    public static final Logger logger = Logger.getLogger(EntityValidationCrudJdbcImpl.class);

    private static final String SQL_INSERT_VALIDATION_RESULT = "INSERT INTO entityvalidation"
            + "(id, created, uuid,  crudeventtype, validatedentityclass, validatedentityid,"
            + "validatedentityuuid, userfriendlydescription, userfriendlytypename, validationcount,"
            + "updated, status, createdby_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

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
    private static final int vr_updated = 11;
    private static final int vr_status = 12;
    private static final int vr_createdby_id = 13;

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
        saveEntityValidation(createEntityValidation(validatedEntity, errors, crudEventType), validationGroups);
    }

    // This is the method that's tested by the unit tests
    // rather than the interface method above, because it
    // is almost impossible to create a mock instance of
    // ConstraintViolation<T>
    void saveEntityValidation(EntityValidation newValidation, Class<?>[] validationGroups) {
        Connection conn = null;
        EntityValidation tmp = null;
        try {
            conn = datasource.getConnection();
            JdbcDaoUtils.startTransaction(conn);
            String entityClass = newValidation.getValidatedEntityClass();
            int entityId = newValidation.getValidatedEntityId();
            EntityValidation oldValidation = getEntityValidation(conn, entityClass, entityId);
            if (oldValidation == null) {
                tmp = newValidation;
                /*
                 * The entity has never been validated before. We should now
                 * create an entityvalidation record whether or not the entity
                 * has errors, because the entity HAS been validated so its
                 * validationcount is now 1.
                 */
                saveEntityValidationRecord(conn, newValidation);
                Set<EntityConstraintViolation> errors = newValidation.getEntityConstraintViolations();
                if (errors != null && errors.size() != 0) {
                    saveErrorRecords(conn, newValidation);
                }

            } else {
                tmp = oldValidation;
                // Increase validation counter
                increaseValidationCounter(conn, oldValidation);

                // Delete obsolete errors, that is, errors from the previous
                // validation that have disappeared from the new validation
                // even though they belong to the same validation group
                dontDeleteErrorsInOtherValidationGroups(oldValidation, validationGroups);
                // Now all errors have been removed from the previous validation
                // that don't belong to the validation group(s) applied by the
                // current validation. Set them apart because we need them
                HashSet<EntityConstraintViolation> oldErrors = new HashSet<EntityConstraintViolation>(
                        oldValidation.getEntityConstraintViolations());
                oldValidation.getEntityConstraintViolations().removeAll(newValidation.getEntityConstraintViolations());
                // Now we're left with previous errors that have disappeared
                // from the current validation (they have become obsolete)
                deleteObsoleteErrors(conn, oldValidation);

                // From the new errors delete all that are identical to
                // errors from a previous validation (identical as per the
                // equals() method of EntityConstraintViolation). These
                // errors will not replace the old ones in order to limit
                // the number of INSERTs.
                newValidation.getEntityConstraintViolations().removeAll(oldErrors);
                saveErrorRecords(conn, newValidation);
            }
            conn.commit();
            setStatus(conn, tmp, EntityValidationStatus.OK);
        } catch (Throwable t) {
            logger.error("Error while saving validation result:", t);
            setStatus(conn, tmp, EntityValidationStatus.ERROR);
            JdbcDaoUtils.rollback(conn);
        } finally {
            JdbcDaoUtils.close(conn);
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
        JdbcDaoUtils.close(conn);
    }

    private static <T extends ICdmBase> EntityValidation createEntityValidation(T validatedEntity,
            Set<ConstraintViolation<T>> errors, CRUDEventType crudEventType) {
        EntityValidation entityValidation = EntityValidation.newInstance(validatedEntity, crudEventType);
        Set<EntityConstraintViolation> errorEntities = new HashSet<EntityConstraintViolation>(errors.size());
        for (ConstraintViolation<T> error : errors) {
            EntityConstraintViolation errorEntity = EntityConstraintViolation.newInstance(validatedEntity, error);
            errorEntities.add(errorEntity);
        }
        entityValidation.setEntityConstraintViolations(errorEntities);
        return entityValidation;
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

    private static void deleteObsoleteErrors(Connection conn, EntityValidation previousValidation) throws SQLException {
        Set<EntityConstraintViolation> obsoleteErrors = previousValidation.getEntityConstraintViolations();
        if (obsoleteErrors == null || obsoleteErrors.size() == 0) {
            return;
        }
        String sql = "DELETE FROM entityconstraintviolation WHERE id = ?";
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql.toString());
            for (EntityConstraintViolation error : obsoleteErrors) {
                stmt.setInt(1, error.getId());
                stmt.executeUpdate();
            }
        } finally {
            JdbcDaoUtils.close(stmt);
        }
    }

    // Save EntityValidation entity to database. As a side effect
    // the database id assigned to the entity will be set on the
    // EntityValidation instance
    private static void saveEntityValidationRecord(Connection conn, EntityValidation newValidation) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(SQL_INSERT_VALIDATION_RESULT);
            if (newValidation.getId() <= 0) {
                int id = 10 + JdbcDaoUtils.fetchInt(conn, "SELECT MAX(id) FROM entityvalidation");
                newValidation.setId(id);
            }
            stmt.setInt(vr_id, newValidation.getId());
            stmt.setDate(vr_created, new Date(newValidation.getCreated().getMillis()));
            stmt.setString(vr_uuid, newValidation.getUuid().toString());
            stmt.setString(vr_crudeventtype, newValidation.getCrudEventType().toString());
            stmt.setString(vr_validatedentityclass, newValidation.getValidatedEntityClass());
            stmt.setInt(vr_validatedentityid, newValidation.getValidatedEntityId());
            stmt.setString(vr_validatedentityuuid, newValidation.getValidatedEntityUuid().toString());
            stmt.setString(vr_userfriendlydescription, newValidation.getUserFriendlyDescription());
            stmt.setString(vr_userfriendlytypename, newValidation.getUserFriendlyTypeName());
            stmt.setInt(vr_validationcount, 1);
            stmt.setDate(vr_updated, new Date(newValidation.getCreated().getMillis()));
            stmt.setString(vr_status, EntityValidationStatus.IN_PROGRESS.toString());
            if (newValidation.getCreatedBy() != null) {
                stmt.setInt(vr_createdby_id, newValidation.getCreatedBy().getId());
            } else {
                stmt.setNull(vr_createdby_id, Types.INTEGER);
            }
            stmt.executeUpdate();
        } finally {
            JdbcDaoUtils.close(stmt);
        }
    }

    private static void increaseValidationCounter(Connection conn, EntityValidation entityValidation)
            throws SQLException {
        String sql = "UPDATE entityvalidation SET crudeventtype=?, validationcount = validationcount + 1, "
                + "updated = ?, status = ? WHERE id=?";
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(sql);
            if (entityValidation.getCrudEventType() == null) {
                stmt.setString(1, null);
            } else {
                stmt.setString(1, entityValidation.getCrudEventType().toString());
            }
            stmt.setDate(2, new Date(new java.util.Date().getTime()));
            stmt.setString(3, EntityValidationStatus.IN_PROGRESS.toString());
            stmt.setInt(4, entityValidation.getId());
            stmt.executeUpdate();
        } finally {
            JdbcDaoUtils.close(stmt);
        }
    }

    private static <T extends ICdmBase> void saveErrorRecords(Connection conn, EntityValidation entityValidation)
            throws SQLException {
        Set<EntityConstraintViolation> errors = entityValidation.getEntityConstraintViolations();
        if (errors == null || errors.size() == 0) {
            return;
        }
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(SQL_INSERT_CONSTRAINT_VIOLATION);
            for (EntityConstraintViolation error : errors) {
                if (error.getId() <= 0) {
                    int id = 10 + JdbcDaoUtils.fetchInt(conn, "SELECT MAX(id) FROM entityconstraintviolation");
                    error.setId(id);
                }
                stmt.setInt(cv_id, error.getId());
                stmt.setDate(cv_created, new Date(error.getCreated().getMillis()));
                stmt.setString(cv_uuid, error.getUuid().toString());
                stmt.setString(cv_invalidvalue, error.getInvalidValue());
                stmt.setString(cv_message, error.getMessage());
                stmt.setString(cv_propertypath, error.getPropertyPath());
                stmt.setString(cv_userfriendlyfieldname, error.getUserFriendlyFieldName());
                stmt.setString(cv_severity, error.getSeverity().toString());
                stmt.setString(cv_validator, error.getValidator());
                stmt.setString(cv_validationgroup, error.getValidationGroup());
                if (error.getCreatedBy() != null) {
                    stmt.setInt(cv_createdby_id, error.getCreatedBy().getId());
                } else {
                    stmt.setNull(cv_createdby_id, Types.INTEGER);
                }
                stmt.setInt(cv_entityvalidation_id, entityValidation.getId());
                stmt.executeUpdate();
            }
        } finally {
            JdbcDaoUtils.close(stmt);
        }
    }

    // Called by unit test
    EntityValidation getEntityValidation(String validatedEntityClass, int validatedEntityId) {
        Connection conn = null;
        try {
            conn = datasource.getConnection();
            JdbcDaoUtils.startTransaction(conn);
            EntityValidation result = getEntityValidation(conn, validatedEntityClass, validatedEntityId);
            conn.commit();
            return result;
        } catch (Throwable t) {
            logger.error("Error while retrieving validation result", t);
            JdbcDaoUtils.rollback(conn);
            return null;
        }
    }

    private static EntityValidation getEntityValidation(Connection conn, String validatedEntityClass,
            int validatedEntityId) throws SQLException {
        EntityValidation entityValidation = getEntityValidationRecord(conn, validatedEntityClass, validatedEntityId);
        if (entityValidation != null) {
            entityValidation.setEntityConstraintViolations(getErrorRecords(conn, entityValidation.getId()));
        }
        return entityValidation;
    }

    private static void deleteValidationResultRecord(Connection conn, int validationResultId) throws SQLException {
        String sql = "DELETE FROM entityvalidation WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, validationResultId);
        stmt.executeUpdate();
    }

    private static void setStatus(Connection conn, EntityValidation entityValidation, EntityValidationStatus status) {
        if (conn == null || entityValidation == null || entityValidation.getId() <= 0) {
            logger.warn("Failed to save entity validation status to database");
            return;
        }
        String sql = "UPDATE entityvalidation SET status = ? WHERE id = ?";
        PreparedStatement stmt = null;
        try {
            JdbcDaoUtils.startTransaction(conn);
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status.toString());
            stmt.setInt(2, entityValidation.getId());
            stmt.executeUpdate();
            conn.commit();
        } catch (Throwable t) {
            logger.error("Failed to set validation status", t);
        } finally {
            JdbcDaoUtils.close(stmt);
        }
    }

    private static <T extends ICdmBase> EntityValidation getEntityValidationRecord(Connection conn,
            String validatedEntityClass, int validatedEntityId) throws SQLException {
        String sqlCount = "SELECT count(*) as n FROM entityvalidation";
        PreparedStatement stmtCount = conn.prepareStatement(sqlCount);
        ResultSet rsCount = stmtCount.executeQuery();
        if (rsCount.next()) {
            int n = rsCount.getInt("n");
            System.out.println("count=" + n);
        }

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
                Date d = rs.getDate("created");
                if (!rs.wasNull()) {
                    result.setCreated(new DateTime(d.getTime()));
                }
                String s = rs.getString("uuid");
                if (!rs.wasNull()) {
                    result.setUuid(UUID.fromString(rs.getString("uuid")));
                }
                s = rs.getString("crudeventtype");
                if (!rs.wasNull()) {
                    result.setCrudEventType(CRUDEventType.valueOf(s));
                }
                result.setValidatedEntityClass(rs.getString("validatedentityclass"));
                result.setValidatedEntityId(rs.getInt("validatedentityid"));
                s = rs.getString("validatedentityuuid");
                if (!rs.wasNull()) {
                    result.setValidatedEntityUuid(UUID.fromString(s));
                }
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

    private static Set<EntityConstraintViolation> getErrorRecords(Connection conn, int entityValidationId)
            throws SQLException {
        return getErrorRecordsForValidationGroup(conn, entityValidationId, null);
    }

    private static Set<EntityConstraintViolation> getErrorRecordsForValidationGroup(Connection conn,
            int entityValidationId, Class<?>[] validationGroups) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM entityconstraintviolation WHERE entityvalidation_id=?");
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
        Set<EntityConstraintViolation> errors = new HashSet<EntityConstraintViolation>();
        try {
            stmt = conn.prepareStatement(sql.toString());
            stmt.setInt(1, entityValidationId);
            if (validationGroups != null && validationGroups.length != 0) {
                for (int i = 0; i < validationGroups.length; ++i) {
                    stmt.setString(i + 2, validationGroups[i].getName());
                }
            }
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

    private static void dontDeleteErrorsInOtherValidationGroups(EntityValidation previousValidation,
            Class<?>[] validationGroups) {
        Set<String> classNames = new HashSet<String>(validationGroups.length);
        for (Class<?> c : validationGroups) {
            classNames.add(c.getName());
        }
        Iterator<EntityConstraintViolation> iterator = previousValidation.getEntityConstraintViolations().iterator();
        while (iterator.hasNext()) {
            if (!classNames.contains(iterator.next().getValidationGroup())) {
                iterator.remove();
            }
        }
    }

}
