/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json.processor.bean;

import java.util.List;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;

/**
 * @author a.kohlbecker
 * @date 09.07.2010
 *
 */
public class TypeDesignationBaseBeanProcessor extends AbstractCdmBeanProcessor<TypeDesignationBase> {

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.json.processor.bean.AbstractCdmBeanProcessor#getIgnorePropNames()
	 */
	@Override
	public List<String> getIgnorePropNames() {
		return null;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.json.processor.bean.AbstractCdmBeanProcessor#processBeanSecondStep(eu.etaxonomy.cdm.model.common.CdmBase, net.sf.json.JSONObject, net.sf.json.JsonConfig)
	 */
	@Override
	public JSONObject processBeanSecondStep(TypeDesignationBase bean, JSONObject json, JsonConfig jsonConfig) {
		json.element("typeStatus", bean.getTypeStatus(), jsonConfig);
		if(bean.getClass().isAssignableFrom(SpecimenTypeDesignation.class)){
			json.element("typeSpecimen", ((SpecimenTypeDesignation)bean).getTypeSpecimen(), jsonConfig);
		} else if (bean.getClass().isAssignableFrom(NameTypeDesignation.class)){
			json.element("typeName", ((NameTypeDesignation)bean).getTypeName(), jsonConfig);
			json.element("citation", ((NameTypeDesignation)bean).getCitation(), jsonConfig);
		}
		return json;
	}

}
