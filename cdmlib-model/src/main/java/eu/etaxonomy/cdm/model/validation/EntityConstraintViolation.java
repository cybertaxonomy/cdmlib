package eu.etaxonomy.cdm.model.validation;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.model.common.CdmBase;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EntityConstraintViolation", propOrder = { "message" })
@XmlRootElement(name = "EntityConstraintViolation")
@Entity
public class EntityConstraintViolation extends CdmBase {

	private static final long serialVersionUID = 6685798691716413950L;
	
	private String message;


	public String getMessage()
	{
		return message;
	}


	public void setMessage(String message)
	{
		this.message = message;
	}

}
