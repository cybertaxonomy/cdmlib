/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.jaxb;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import eu.etaxonomy.cdm.model.common.IIdentifiableEntity;

/**
 * @author a.babadshanjan
 * @version 1.0
 */
public class NamespacesAdapter extends XmlAdapter<NamespacesElement, Map<String,Class<? extends IIdentifiableEntity>>> {

	@Override
	public NamespacesElement marshal(Map<String,Class<? extends IIdentifiableEntity>> value)
			throws Exception {
		
		NamespacesElement namespacesElement = new NamespacesElement();
		
		for(String s : value.keySet()) {
			Namespace namespace = new Namespace();
			namespace.setNSpace(s);
			namespace.setClazz(value.get(s));
			namespacesElement.getNamespace().add(namespace);
		}
		return namespacesElement;
	}

    @Override
	public Map<String,Class<? extends IIdentifiableEntity>> unmarshal(NamespacesElement value)
			throws Exception {
		
        Map<String, Class<? extends IIdentifiableEntity>> map = new ConcurrentHashMap<String, Class<? extends IIdentifiableEntity>>();
		
		for(Namespace n : value.getNamespace()) {
			map.put(n.getNSpace(),n.getClazz());
		}
		return map;
	}
}
