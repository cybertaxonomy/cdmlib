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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;

import org.springframework.core.type.filter.AnnotationTypeFilter;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * Enumeration of all abstract CDM base types. Each provides a set of all sub types.
 * @author a.kohlbecker
 \* @since Sep 28, 2012
 *
 */
public enum CdmBaseType {

    /**
     * refers to the baseClass {@link TaxonBase}
     */
    TAXON(TaxonBase.class),
    NONVIRALNAME(TaxonName.class),
    DESCRIPTION_ELEMENT(DescriptionElementBase.class),
    DESCRIPTION(DescriptionBase.class),
    SPECIMEN_OR_OBSERVATIONBASE(SpecimenOrObservationBase.class);
    // TODO add all others

    private Class<? extends CdmBase> baseClass;

    private Collection<Class<? extends CdmBase>> subClasses;

    static Map<Class<? extends CdmBase>,  Class<? extends CdmBase>> subTypeToBaseTypeMap;

    CdmBaseType(Class<? extends CdmBase> baseClass){
        this.baseClass = baseClass;
        try {
            subClasses = subclassesFor(baseClass);
            updateBaseTypeMap();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateBaseTypeMap(){
        if(CdmBaseType.subTypeToBaseTypeMap == null){
            CdmBaseType.subTypeToBaseTypeMap = new HashMap<Class<? extends CdmBase>,  Class<? extends CdmBase>>();
        }
        for(Class<? extends CdmBase> subClass : subClasses){
            CdmBaseType.subTypeToBaseTypeMap.put(subClass, baseClass);
        }
    }

    private Collection<Class<? extends CdmBase>> subclassesFor(Class<? extends CdmBase> clazz) throws ClassNotFoundException{

        boolean includeAbstract = true;
        boolean includeInterfaces = false;

        CdmTypeScanner<CdmBase> scanner = new CdmTypeScanner<CdmBase>(includeAbstract, includeInterfaces);
        scanner.addIncludeFilter(new CdmAssignableTypeFilter(IdentifiableEntity.class, includeAbstract, includeInterfaces));
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        Collection<Class<? extends CdmBase>> classes = scanner.scanTypesIn("eu/etaxonomy/cdm/model");

        return classes;
    }

    public static Class<? extends CdmBase> baseTypeFor(Class<? extends CdmBase> cdmType){
        return subTypeToBaseTypeMap.get(cdmType);
    }

    public Collection<Class<? extends CdmBase>> getSubClasses() {
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
