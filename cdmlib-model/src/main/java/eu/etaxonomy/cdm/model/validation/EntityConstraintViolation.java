package eu.etaxonomy.cdm.model.validation;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.ConstraintValidator;
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
import eu.etaxonomy.cdm.validation.Severity;

/**
 * An {@code EntityConstraintViolation} represents a single error resulting from the
 * validation of an entity. It basically is a database model for the
 * {@link ConstraintValidator} class of the javax.validation framework.
 * 
 * @author admin.ayco.holleman
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
//@formatter:off
@XmlType(name = "EntityConstraintViolation", propOrder = {
		"PropertyPath",
		"InvalidValue",
		"Severity",
		"Message",
		"Validator",
		"EntityValidationResult"
})
//@formatter:on
@XmlRootElement(name = "EntityConstraintViolation")
@Entity
public class EntityConstraintViolation extends CdmBase {

	private static final long serialVersionUID = 6685798691716413950L;

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(EntityConstraintViolation.class);


	public static EntityConstraintViolation newInstance()
	{
		return new EntityConstraintViolation();
	}

	@XmlElement(name = "PropertyPath")
	private String propertyPath;

	@XmlElement(name = "InvalidValue")
	private String invalidValue;

	@XmlElement(name = "Severity")
	@Type(type = "eu.etaxonomy.cdm.model.validation.SeverityType")
	private Severity severity;

	@XmlElement(name = "Message")
	private String message;

	@XmlElement(name = "Validator")
	private String validator;

	@XmlElement(name = "EntityValidationResult")
	@ManyToOne(fetch = FetchType.LAZY)
	@Cascade({ CascadeType.ALL })
	private EntityValidationResult entityValidationResult;


	protected EntityConstraintViolation()
	{
	}


	/**
	 * Get the path from the root bean to the field with the invalid value. Ordinarily
	 * this simply is the simple name of the field of the validated entity (see
	 * {@link EntityValidationResult#getValidatedEntityClass()}). Only if you have used @Valid
	 * annotations, and the error was in a parent or child entity, will this be a
	 * dot-separated path (e.g. "addresses[0].street" or "company.name").
	 */
	public String getPropertyPath()
	{
		return propertyPath;
	}


	public void setPropertyPath(String propertyPath)
	{
		this.propertyPath = propertyPath;
	}


	/**
	 * Get the value that violated the constraint.
	 * 
	 * @return
	 */
	public String getInvalidValue()
	{
		return invalidValue;
	}


	public void setInvalidValue(String invalidValue)
	{
		this.invalidValue = invalidValue;
	}


	/**
	 * Get the severity of the constraint violation.
	 * 
	 * @return
	 */
	public Severity getSeverity()
	{
		return severity;
	}


	public void setSeverity(Severity severity)
	{
		this.severity = severity;
	}


	/**
	 * Get the error message associated with the constraint violation.
	 * 
	 * @return The error message
	 */
	public String getMessage()
	{
		return message;
	}


	public void setMessage(String message)
	{
		this.message = message;
	}


	/**
	 * Get the fully qualified class name of the {@link ConstraintValidator} responsible
	 * for invalidating the entity.
	 * 
	 * @param validator
	 */
	public String getValidator()
	{
		return validator;
	}


	public void setValidator(String validator)
	{
		this.validator = validator;
	}


	public EntityValidationResult getEntityValidationResult()
	{
		return entityValidationResult;
	}


	public void setEntityValidationResult(EntityValidationResult entityValidationResult)
	{
		this.entityValidationResult = entityValidationResult;
	}

}
