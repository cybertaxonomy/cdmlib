package eu.etaxonomy.cdm.model.validation;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.validation.Severity;

@XmlAccessorType(XmlAccessType.FIELD)
//@formatter:off
@XmlType(name = "EntityConstraintViolation", propOrder = {
		"PropertyPath",
		"InvalidValue",
		"Severity",
		"Message",
		"ConstraintValidatorClass",
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
	private Severity severity;

	@XmlElement(name = "Message")
	private String message;

	@XmlElement(name = "ConstraintValidatorClass")
	private String constraintValidatorClass;

	@XmlElement(name = "EntityValidationResult")
	@ManyToOne(fetch = FetchType.LAZY)
	private EntityValidationResult entityValidationResult;


	public String getPropertyPath()
	{
		return propertyPath;
	}


	public void setPropertyPath(String propertyPath)
	{
		this.propertyPath = propertyPath;
	}


	public String getInvalidValue()
	{
		return invalidValue;
	}


	public void setInvalidValue(String invalidValue)
	{
		this.invalidValue = invalidValue;
	}


	public Severity getSeverity()
	{
		return severity;
	}


	public void setSeverity(Severity severity)
	{
		this.severity = severity;
	}


	public String getMessage()
	{
		return message;
	}


	public void setMessage(String message)
	{
		this.message = message;
	}


	public String getConstraintValidatorClass()
	{
		return constraintValidatorClass;
	}


	public void setConstraintValidatorClass(String constraintValidatorClass)
	{
		this.constraintValidatorClass = constraintValidatorClass;
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
