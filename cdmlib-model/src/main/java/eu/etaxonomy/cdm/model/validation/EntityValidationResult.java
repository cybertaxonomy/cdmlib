package eu.etaxonomy.cdm.model.validation;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.model.common.CdmBase;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EntityValidationResult", propOrder = { "entityId", "entityUuid", "entityValidationTrigger" })
@XmlRootElement(name = "EntityValidationResult")
@Entity
public class EntityValidationResult extends CdmBase {

	private static final long serialVersionUID = 9120571815593117363L;
	
	private int entityId;
	private int entityUuid;


	public int getEntityId()
	{
		return entityId;
	}


	public void setEntityId(int entityId)
	{
		this.entityId = entityId;
	}


	public int getEntityUuid()
	{
		return entityUuid;
	}


	public void setEntityUuid(int entityUuid)
	{
		this.entityUuid = entityUuid;
	}



}
