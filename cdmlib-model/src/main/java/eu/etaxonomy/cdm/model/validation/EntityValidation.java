/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.model.validation;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.validation.Validator;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.joda.time.DateTime;

import eu.etaxonomy.cdm.hibernate.search.DateTimeBridge;
import eu.etaxonomy.cdm.hibernate.search.UuidBridge;
import eu.etaxonomy.cdm.jaxb.DateTimeAdapter;
import eu.etaxonomy.cdm.jaxb.UUIDAdapter;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.ISelfDescriptive;

/**
 * An {@code EntityValidation} models the result of validating one entity,
 * that is, the outcome of calling {@link Validator#validate(Object, Class...)}.
 * More than one constraint {@link EntityConstraintViolation} may be violated
 * while validating the entity.
 *
 * @see EntityValidation
 *
 * @author ayco_holleman
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
// @formatter:off
@XmlType(name = "EntityValidation", propOrder = { "ValidatedEntityId", "ValidatedEntityUuid",
        "ValidatedEntityClass", "ValidationCount", "LastModified", "UserFriendlyDescription", "UserFriendlyTypeName",
        "CrudEventType", "ConstraintViolations" })
// @formatter:on
@XmlRootElement(name = "EntityValidation")
@Entity
public class EntityValidation extends CdmBase {

    private static final long serialVersionUID = 9120571815593117363L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(EntityValidation.class);

    public static EntityValidation newInstance() {
        return new EntityValidation();
    }

    // See comment below
    public static EntityValidation newInstance(ICdmBase entity, CRUDEventType crudEventType) {
        EntityValidation result = newInstance();
        result.setCrudEventType(crudEventType);
        result.setValidatedEntityClass(entity.getClass().getName());
        result.setValidatedEntityId(entity.getId());
        result.setValidatedEntityUuid(entity.getUuid());
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
            ISelfDescriptive isd = (ISelfDescriptive) entity;
            result.setUserFriendlyTypeName(isd.getUserFriendlyTypeName());
            result.setUserFriendlyDescription(isd.getUserFriendlyDescription());
        } else {
            result.setUserFriendlyTypeName(entity.getClass().getSimpleName());
            result.setUserFriendlyDescription(entity.toString());
        }
        result.setValidationCount(1);
        result.setStatus(EntityValidationStatus.IN_PROGRESS);
        result.setLastModified(result.getCreated());
        return result;
    }

    @XmlElement(name = "ValidatedEntityId")
    private int validatedEntityId;

    @XmlElement(name = "ValidatedEntityUuid")
    @XmlJavaTypeAdapter(UUIDAdapter.class)
    @Type(type = "uuidUserType")
    @Column(length = 36)
    // TODO needed? Type UUID will always assure that is exactly 36
    @FieldBridge(impl = UuidBridge.class)
    // TODO required?
    private UUID validatedEntityUuid;

    @XmlElement(name = "ValidatedEntityClass")
    private String validatedEntityClass;

    @XmlElement(name = "ValidationCount")
    private int validationCount;

    @XmlElement(name = "LastModified")
    @XmlJavaTypeAdapter(DateTimeAdapter.class)
    @Type(type = "dateTimeUserType")
    @Field(analyze = Analyze.NO)
    @FieldBridge(impl = DateTimeBridge.class)
    private DateTime lastModified;

    @XmlElement(name = "UserFriendlyDescription")
    private String userFriendlyDescription;

    @XmlElement(name = "UserFriendlyTypeName")
    private String userFriendlyTypeName;

    @XmlElement(name = "CrudEventType")
    @Enumerated(EnumType.STRING)
    private CRUDEventType crudEventType;

    @XmlElement(name = "Status")
    @Enumerated(EnumType.STRING)
    private EntityValidationStatus status;

    @XmlElementWrapper(name = "EntityConstraintViolations")
    @OneToMany(mappedBy = "entityValidation")
    @Cascade({ CascadeType.ALL })
    @Fetch(value = FetchMode.JOIN)
    private Set<EntityConstraintViolation> entityConstraintViolations;

    protected EntityValidation() {
        super();
    }

    public int getValidatedEntityId() {
        return validatedEntityId;
    }

    public void setValidatedEntityId(int validatedEntityId) {
        this.validatedEntityId = validatedEntityId;
    }

    public UUID getValidatedEntityUuid() {
        return validatedEntityUuid;
    }

    public void setValidatedEntityUuid(UUID validatedEntityUuid) {
        this.validatedEntityUuid = validatedEntityUuid;
    }

    public String getValidatedEntityClass() {
        return validatedEntityClass;
    }

    public void setValidatedEntityClass(String validatedEntityClass) {
        this.validatedEntityClass = validatedEntityClass;
    }

    public int getValidationCount() {
        return validationCount;
    }

    public void setValidationCount(int validationCount) {
        this.validationCount = validationCount;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(DateTime lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String getUserFriendlyTypeName() {
        return userFriendlyTypeName;
    }

    public void setUserFriendlyTypeName(String userFriendlyTypeName) {
        this.userFriendlyTypeName = userFriendlyTypeName;
    }

    public CRUDEventType getCrudEventType() {
        return crudEventType;
    }

    public void setCrudEventType(CRUDEventType crudEventType) {
        this.crudEventType = crudEventType;
    }

	public EntityValidationStatus getStatus()
	{
		return status;
	}

	public void setStatus(EntityValidationStatus status)
	{
		this.status = status;
	}

	@Override
    public String getUserFriendlyDescription() {
        return userFriendlyDescription;
    }

    public void setUserFriendlyDescription(String userFriendlyDescription) {
        this.userFriendlyDescription = userFriendlyDescription;
    }

    public Set<EntityConstraintViolation> getEntityConstraintViolations() {
        if (entityConstraintViolations == null) {
            entityConstraintViolations = new HashSet<EntityConstraintViolation>();
        }
        return entityConstraintViolations;
    }

    public void addEntityConstraintViolation(EntityConstraintViolation ecv) {
        if (ecv != null) {
            getEntityConstraintViolations().add(ecv);
        }
    }

    public void removeEntityConstraintViolation(EntityConstraintViolation ecv) {
        if (ecv != null) {
            getEntityConstraintViolations().remove(ecv);
        }
    }

    public void setEntityConstraintViolations(Set<EntityConstraintViolation> errors) {
        this.entityConstraintViolations = errors;
    }


}
