/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model;

import org.springframework.core.type.filter.AbstractTypeHierarchyTraversingFilter;

/**
 * Special implementation for the cdm which allows to also match
 * abstract base types and interfaces in eu.etaxonomy.cdm.model.
 *
 *
 * @author a.kohlbecker
 * @date Jul 31, 2014
 *
 */
public class CdmAssignableTypeFilter extends AbstractTypeHierarchyTraversingFilter {


    private final Class targetType;

    /**
     * @param targetType
     */
    public CdmAssignableTypeFilter(Class targetType, boolean considerInherited, boolean considerInterfaces) {
        super(considerInherited, considerInterfaces);
        this.targetType = targetType;
    }

    @Override
    protected boolean matchClassName(String className) {
        return this.targetType.getName().equals(className);
    }

    @Override
    protected Boolean matchSuperClass(String superClassName) {
        return matchTargetType(superClassName);
    }

    @Override
    protected Boolean matchInterface(String interfaceName) {
        return matchTargetType(interfaceName);
    }


    protected Boolean matchTargetType(String typeName) {
        if (this.targetType.getName().equals(typeName)) {
            return true;
        }
        else if (Object.class.getName().equals(typeName)) {
            return Boolean.FALSE;
        }
        else if (typeName.startsWith("eu.etaxonomy.cdm.model.")) {
            try {
                Class clazz = getClass().getClassLoader().loadClass(typeName);
                return Boolean.valueOf(this.targetType.isAssignableFrom(clazz));
            }
            catch (ClassNotFoundException ex) {
                // Class not found - can't determine a match that way.
            }
        }
        return null;
    }

}
