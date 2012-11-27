// $Id$
/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * Enumeration of all abstract CDM base types. Each provides a set of all sub types.
 * @author a.kohlbecker
 * @date Sep 28, 2012
 *
 */
public enum CdmBaseType {

    TAXON(TaxonBase.class),
    DESCRIPTION_ELEMENT(DescriptionElementBase.class),
    DESCRIPTION(DescriptionBase.class);
    // TODO add all others

    private Class<? extends CdmBase> baseClass;

    private Set<Class<? extends CdmBase>> subClasses;

    CdmBaseType(Class<? extends CdmBase> baseClass){
        this.baseClass = baseClass;
        try {
            subClasses = subclassesFor(baseClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Set<Class<? extends CdmBase>> subclassesFor(Class<? extends CdmBase> clazz) throws ClassNotFoundException{

        Set<Class<? extends CdmBase>> subClasses = new HashSet<Class<? extends CdmBase>>();
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true);
        provider.addIncludeFilter(new AssignableTypeFilter(clazz));

        // scan only in eu.etaxonomy.cdm.model
        Set<BeanDefinition> components = provider.findCandidateComponents("eu/etaxonomy/cdm/model");
        for (BeanDefinition component : components)
        {
            subClasses.add((Class<? extends CdmBase>) Class.forName(component.getBeanClassName()));
        }
        return subClasses;
    }

    public Set<Class<? extends CdmBase>> getSubClasses() {
        return subClasses;
    }

    public Class<? extends CdmBase> getBaseClass() {
        return baseClass;
    }

    public List<String> getSubClassNames() {
        List<String> names = new ArrayList<String>(subClasses.size());
        for(Class<? extends CdmBase> clazz : subClasses){
            names.add(clazz.getName());
        }
        return names;
    }

}
