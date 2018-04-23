/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.service.dto;

import java.io.Serializable;

import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * @author cmathew
 \* @since 24 Jun 2015
 *
 */
public class CdmEntityIdentifier implements Serializable {


    /**
     *
     */
    private static final long serialVersionUID = 1479948194282284147L;


    private final int id;
    private final Class<? extends CdmBase> cdmClass;

    public CdmEntityIdentifier(int id, Class cdmClass) {
        this.id = id;
        this.cdmClass = cdmClass;
    }


    public int getId() {
        return id;
    }

    public Class getCdmClass() {
        return cdmClass;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof CdmEntityIdentifier)) {
            return false;
        }

        if(this == obj) {
            return true;
        }
        CdmEntityIdentifier that = (CdmEntityIdentifier) obj;
        if(this.cdmClass.equals(that.cdmClass) && this.id == that.id) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (this.cdmClass.getName() + String.valueOf(this.id)).hashCode();
    }

    @Override
    public String toString() {
        return this.cdmClass.getName() + String.valueOf(this.id);
    }
}
