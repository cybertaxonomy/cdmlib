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
 * cdm types be their simple class name
 *
 *
 * @author a.kohlbecker
 * @date Jul 31, 2014
 *
 */
public class CdmSimpleNameTypeFilter extends AbstractTypeHierarchyTraversingFilter {


    private final String simpleName;

    /**
     * @param targetType
     */
    public CdmSimpleNameTypeFilter(String simpleName) {
        super(false, false);
        this.simpleName = simpleName;
    }

    @Override
    protected boolean matchClassName(String className) {
        return className.endsWith("." + simpleName);
    }

}
