/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto.assembler.converter;

import com.github.dozermapper.core.DozerConverter;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.TaxonRelationshipTerm;

/**
 * @author a.mueller
 * @since 05.02.2021
 */
public class ConstantConverter extends DozerConverter<CdmBase,Object> {

    final static String TAX_INCLUDED = "taxIncluded";

    public ConstantConverter() {
        super(CdmBase.class, Object.class);
    }

    @Override
    public Object convertTo(CdmBase source, Object destination) {
        if (TAX_INCLUDED.equals(this.getParameter())){
            TaxonRelationshipTerm result = new TaxonRelationshipTerm();
            result.setTitle("is taxonomically included in");
            return result;
        }else{
            return this.getParameter();
        }
    }

    @Override
    public CdmBase convertFrom(Object source, CdmBase destination) {
        throw new RuntimeException("ConstantConverter should be used one-way only");
    }


}
