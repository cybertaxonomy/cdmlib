package eu.etaxonomy.cdm.model.validation;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
import org.hibernate.search.annotations.FieldBridge;

import eu.etaxonomy.cdm.hibernate.search.UuidBridge;
import eu.etaxonomy.cdm.jaxb.UUIDAdapter;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.validation.CRUDEventType;

/**
 * An {@code EntityValidationResult} models the result of validating one entity, that is,
 * the outcome of calling {@link Validator#validate(Object, Class...)}. More than one
 * constraint {@link EntityConstraintViolation} may be violated while validating the
 * entity.
 * 
 * @see EntityValidationResult
 * 
 * @author ayco_holleman
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
//@formatter:off
@XmlType(name = "EntityValidationResult", propOrder = {
		"ValidatedEntityId",
		"ValidatedEntityUuid",
		"ValidatedEntityClass",
		"ValidatedEntityDescription",
		"CrudEventType",
		"ConstraintViolations"
})
//@formatter:on
@XmlRootElement(name = "EntityValidationResult")
@Entity
public class EntityValidationResult extends CdmBase {

	private static final long serialVersionUID = 9120571815593117363L;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(EntityValidationResult.class);


	public static EntityValidationResult newInstance()
	{
		return new EntityValidationResult();
	}

	@XmlElement(name = "ValidatedEntityId")
	private int validatedEntityId;

	@XmlElement(name = "ValidatedEntityUuid")
	@XmlJavaTypeAdapter(UUIDAdapter.class)
	@Type(type = "uuidUserType")
	@FieldBridge(impl = UuidBridge.class)
	private UUID validatedEntityUuid;

	@XmlElement(name = "ValidatedEntityClass")
	private String validatedEntityClass;

	@XmlElement(name = "ValidatedEntityDescription")
	private String validatedEntityDescription;

	@XmlElement(name = "CrudEventType")
	@Enumerated(EnumType.STRING)
	private CRUDEventType crudEventType;

	@XmlElementWrapper(name = "EntityConstraintViolations")
	@OneToMany(mappedBy = "entityValidationResult")
	@Cascade({ CascadeType.ALL })
	@Fetch(value = FetchMode.JOIN)
	private Set<EntityConstraintViolation> entityConstraintViolations;


	protected EntityValidationResult()
	{
		super();
	}


	public int getValidatedEntityId()
	{
		return validatedEntityId;
	}


	public void setValidatedEntityId(int validatedEntityId)
	{
		this.validatedEntityId = validatedEntityId;
	}


	public UUID getValidatedEntityUuid()
	{
		return validatedEntityUuid;
	}


	public void setValidatedEntityUuid(UUID validatedEntityUuid)
	{
		this.validatedEntityUuid = validatedEntityUuid;
	}


	public String getValidatedEntityClass()
	{
		return validatedEntityClass;
	}


	public void setValidatedEntityClass(String validatedEntityClass)
	{
		this.validatedEntityClass = validatedEntityClass;
	}


	public CRUDEventType getCrudEventType()
	{
		return crudEventType;
	}


	public void setCrudEventType(CRUDEventType crudEventType)
	{
		this.crudEventType = crudEventType;
	}


	public String getValidatedEntityDescription()
	{
		return validatedEntityDescription;
	}


	public void setValidatedEntityDescription(String validatedEntityDescription)
	{
		this.validatedEntityDescription = validatedEntityDescription;
	}


	public Set<EntityConstraintViolation> getEntityConstraintViolations()
	{
		if (entityConstraintViolations == null) {
			entityConstraintViolations = new HashSet<EntityConstraintViolation>();
		}
		return entityConstraintViolations;
	}


	public void addEntityConstraintViolation(EntityConstraintViolation ecv)
	{
		if (ecv != null) {
			getEntityConstraintViolations().add(ecv);
		}
	}


	public void removeEntityConstraintViolation(EntityConstraintViolation ecv)
	{
		if (ecv != null) {
			getEntityConstraintViolations().remove(ecv);
		}
	}

	/////////////////////////////////
	// END PUBLIC INTERFACE
	/////////////////////////////////

}
