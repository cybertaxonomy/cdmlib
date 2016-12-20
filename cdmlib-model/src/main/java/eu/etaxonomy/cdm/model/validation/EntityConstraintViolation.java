/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.model.validation;

import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintViolation;
import javax.validation.metadata.ConstraintDescriptor;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.ISelfDescriptive;

/**
 * An {@code EntityConstraintViolation} represents a single error resulting from
 * the validation of an entity. It basically is a database model for the
 * {@link ConstraintValidator} class of the javax.validation framework.
 *
 * @author ayco_holleman
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EntityConstraintViolation", propOrder = { "PropertyPath", "UserFriendlyFieldName", "InvalidValue",
        "Severity", "Message", "Validator", "ValidationGroup", "EntityValidation" })
@XmlRootElement(name = "EntityConstraintViolation")
@Entity
public class EntityConstraintViolation extends CdmBase {
    private static final long serialVersionUID = 6685798691716413950L;

    private static final Logger logger = Logger.getLogger(EntityConstraintViolation.class);

    public static EntityConstraintViolation newInstance() {
        return new EntityConstraintViolation();
    }

    public static <T extends ICdmBase> EntityConstraintViolation newInstance(T entity, ConstraintViolation<T> error) {
        EntityConstraintViolation violation = newInstance();
        violation.setSeverity(Severity.getSeverity(error));
        String propPath = error.getPropertyPath() == null ? "-" : error.getPropertyPath().toString();
        violation.setPropertyPath(propPath);
        violation.setInvalidValue(error.getInvalidValue() == null ? "NULL" : error.getInvalidValue().toString());
        violation.setMessage(error.getMessage());
        /*
         * Since I have changed CdmBase to implement ISelfDescriptive, this is a
         * redundant check, since only instances of CdmBase can be validated
         * using the validation infrastructure. However, until Andreas Mueller
         * decides that it is actually useful and appropriate that CdmBase
         * should implement this interface, this check should be made, so that
         * nothing breaks if the "implements ISelfDescriptive" is removed from
         * the class declaration of CdmBase.
         */
        if (entity instanceof ISelfDescriptive) {
            ISelfDescriptive selfDescriptive = (ISelfDescriptive) entity;
            violation.setUserFriendlyFieldName(selfDescriptive.getUserFriendlyFieldName(propPath));
        } else {
            violation.setUserFriendlyFieldName(propPath);
        }
        ConstraintDescriptor<?> metadata = error.getConstraintDescriptor();
        List<?> validators = metadata.getConstraintValidatorClasses();
        violation.setValidator(validators.isEmpty() ? null : ((Class<?>) validators.iterator().next()).getName());
        Set<Class<?>> validationGroups = metadata.getGroups();

        // See spec for getGroups(): The set of groups the constraint is applied
        // on. If the constraint declares no group, a set with only the Default
        // group is returned.
        assert (validationGroups != null && validationGroups.size() > 0);

        String validationGroup = validationGroups.iterator().next().getName();
        if (validationGroups.size() > 1) {
            if (logger.isDebugEnabled()) {
                String fmt = "Constraint %s belongs to multiple validation groups. Will use %s to create instance";
                String msg = String.format(fmt, violation.getValidator(), validationGroup);
                logger.debug(msg);
            }
        }
        violation.setValidationGroup(validationGroup);
        return violation;
    }

    @XmlElement(name = "PropertyPath")
    private String propertyPath;

    @XmlElement(name = "UserFriendlyFieldName")
    private String userFriendlyFieldName;

    @XmlElement(name = "InvalidValue")
    private String invalidValue;

    @XmlElement(name = "Severity")
    @Type(type = "eu.etaxonomy.cdm.hibernate.SeverityUserType")
    private Severity severity = Severity.ERROR;

    @XmlElement(name = "Message")
    private String message;

    @XmlElement(name = "Validator")
    private String validator;

    @XmlElement(name = "ValidationGroup")
    private String validationGroup;

    @XmlElement(name = "EntityValidation")
    @ManyToOne(fetch = FetchType.LAZY)
    @Cascade({ CascadeType.SAVE_UPDATE })
    private EntityValidation entityValidation;

    protected EntityConstraintViolation() {
    }

    /**
     * Get the path from the root bean to the field with the invalid value.
     * Ordinarily this simply is the simple name of the field of the validated
     * entity (see {@link EntityValidation#getValidatedEntityClass()}). Only if
     * you have used @Valid annotations, and the error was in a parent or child
     * entity, will this be a dot-separated path (e.g. "addresses[0].street" or
     * "company.name").
     */
    public String getPropertyPath() {
        return propertyPath;
    }

    public void setPropertyPath(String propertyPath) {
        this.propertyPath = propertyPath;
    }

    /**
     * A user-friendly name for the property path.
     */
    public String getUserFriendlyFieldName() {
        return userFriendlyFieldName;
    }

    public void setUserFriendlyFieldName(String userFriendlyFieldName) {
        this.userFriendlyFieldName = userFriendlyFieldName;
    }

    /**
     * Get the value that violated the constraint.
     *
     * @return
     */
    public String getInvalidValue() {
        return invalidValue;
    }

    public void setInvalidValue(String invalidValue) {
        this.invalidValue = invalidValue;
    }

    /**
     * Get the severity of the constraint violation.
     *
     * @return
     */
    public Severity getSeverity() {
        return severity == null ? Severity.ERROR : severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    /**
     * Get the error message associated with the constraint violation.
     *
     * @return The error message
     */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get the fully qualified class name of the {@link ConstraintValidator}
     * responsible for invalidating the entity.
     *
     * @param validator
     */
    public String getValidator() {
        return validator;
    }

    public void setValidator(String validator) {
        this.validator = validator;
    }

    /**
     * @return the validationGroup
     */
    public String getValidationGroup() {
        return validationGroup;
    }

    /**
     * @param validationGroup
     *            the validationGroup to set
     */
    public void setValidationGroup(String validationGroup) {
        this.validationGroup = validationGroup;
    }

    public EntityValidation getEntityValidation() {
        return entityValidation;
    }

    public void setEntityValidation(EntityValidation entityValidation) {
        this.entityValidation = entityValidation;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        EntityConstraintViolation other = (EntityConstraintViolation) obj;
        if (!equals(invalidValue, other.invalidValue)) {
            return false;
        }
        if (!equals(propertyPath, other.propertyPath)) {
            return false;
        }
        if (!equals(validator, other.validator)) {
            return false;
        }
        if (!equals(validationGroup, other.validationGroup)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = (hash * 31) + (invalidValue == null ? 0 : invalidValue.hashCode());
        hash = (hash * 31) + (propertyPath == null ? 0 : propertyPath.hashCode());
        hash = (hash * 31) + (validator == null ? 0 : validator.hashCode());
        hash = (hash * 31) + (validationGroup == null ? 0 : validationGroup.hashCode());
        return hash | super.hashCode();
    }

    private static boolean equals(Object o1, Object o2) {
        if (o1 == null) {
            if (o2 == null) {
                return true;
            }
            return false;
        }
        return o2 != null && o1.equals(o1);
    }

}
