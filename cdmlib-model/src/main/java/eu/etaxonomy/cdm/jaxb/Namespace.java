/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Namespace", namespace="http://etaxonomy.eu/cdm/model/common/1.0", propOrder = {
    "nspace",
	"clazz"
})
public class Namespace {

	@XmlElement(name = "NSpace", namespace="http://etaxonomy.eu/cdm/model/common/1.0", required = true)
	private String nspace;
	
	@XmlElement(name = "Class", namespace="http://etaxonomy.eu/cdm/model/common/1.0", required = true)
	@XmlJavaTypeAdapter(ClassAdapter.class)
	private Class<? extends IIdentifiableEntity> clazz;

	public String getNSpace() {
		return nspace;
	}

	public void setNSpace(String nspace) {
		this.nspace = nspace;
	}

	public Class<? extends IIdentifiableEntity> getClazz() {
		return clazz;
	}

	public void setClazz(Class<? extends IIdentifiableEntity> clazz) {
		this.clazz = clazz;
	}
}
