package eu.etaxonomy.cdm.io.jaxb.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import eu.etaxonomy.cdm.model.common.CdmBase;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CdmListWrapper", propOrder = {})
public abstract class CdmListWrapper<T extends CdmBase> {
	
	public abstract List<T> getElements();
	
	public abstract void setElements(List<T> elements);
	

}
