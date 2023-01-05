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

import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.term.IEnumTerm;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.TaxonRelationshipTerm;

/**
 * @author a.mueller
 * @since 04.01.2023
 */
public class EnumConverter extends DozerConverter<IEnumTerm<?>,Object> {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public EnumConverter() {
        super((Class)IEnumTerm.class, Object.class);
    }

    @Override
    public Object convertTo(IEnumTerm<?> source, Object destination) {
        if (source instanceof SynonymType){
            TaxonRelationshipTerm result = new TaxonRelationshipTerm();
            result.setTitle(source.getLabel());
            return result;
        }else{
            return this.getParameter();
        }
    }

    @Override
    public IEnumTerm<?> convertFrom(Object destination, IEnumTerm<?> source) {
        throw new RuntimeException("EnumConverter should be used one-way only");
    }
}