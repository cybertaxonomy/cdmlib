package eu.etaxonomy.cdm.model.validation;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.validation.CRUDEventType;

@XmlAccessorType(XmlAccessType.FIELD)
//@formatter:off
@XmlType(name = "EntityValidationResult", propOrder = {
		"ValidatedEntityId",
		"ValidatedEntityUuid",
		"ValidatedEntityClass",
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
	private int validatedEntityUuid;

	@XmlElement(name = "ValidatedEntityClass")
	private String validatedEntityClass;

	@XmlElement(name = "CrudEventType")
	private CRUDEventType crudEventType;

	@XmlElementWrapper(name = "ConstraintViolations")
	@OneToMany(mappedBy = "entityValidationResult")
	@Cascade({ CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.DELETE, CascadeType.REFRESH })
	private Set<EntityConstraintViolation> constraintViolations;


	public int getValidatedEntityId()
	{
		return validatedEntityId;
	}


	public void setValidatedEntityId(int validatedEntityId)
	{
		this.validatedEntityId = validatedEntityId;
	}


	public int getValidatedEntityUuid()
	{
		return validatedEntityUuid;
	}


	public void setValidatedEntityUuid(int validatedEntityUuid)
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


	public Set<EntityConstraintViolation> getConstraintViolations()
	{
		if (constraintViolations == null) {
			constraintViolations = new HashSet<EntityConstraintViolation>();
		}
		return constraintViolations;
	}


	public void addConstraintViolation(EntityConstraintViolation ecv)
	{
		if (ecv != null) {
			getConstraintViolations().add(ecv);
		}
	}


	public void removeConstraintViolation(EntityConstraintViolation ecv)
	{
		if (ecv != null) {
			getConstraintViolations().remove(ecv);
		}
	}


	/////////////////////////////////
	// END PUBLIC INTERFACE
	/////////////////////////////////

	protected EntityValidationResult()
	{
		super();
	}

}
