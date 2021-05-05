/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format;

import org.apache.commons.lang3.StringUtils;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author a.mueller
 * @since 03.05.2021
 */
public abstract class CdmFormatterBase<T extends CdmBase> {

    public abstract String format(T cdmBase);

    protected boolean isBlank(String str){
        return StringUtils.isBlank(str);
    }

    protected boolean isNotBlank(String str){
        return StringUtils.isNotBlank(str);
    }

    /**
     * Null safe string. Returns the given string if it is not <code>null</code>.
     * Empty string otherwise.
     * @see CdmUtils#Nz(String)
     * @return the null-safe string
     */
    protected String Nz(String str){
        return CdmUtils.Nz(str);
    }

}
