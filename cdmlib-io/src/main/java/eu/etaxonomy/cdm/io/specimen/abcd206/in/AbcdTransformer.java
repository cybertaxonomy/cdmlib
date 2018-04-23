/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.specimen.abcd206.in;

import org.apache.commons.lang.StringUtils;

import eu.etaxonomy.cdm.io.common.mapping.InputTransformerBase;
import eu.etaxonomy.cdm.io.common.mapping.UndefinedTransformerMethodException;
import eu.etaxonomy.cdm.model.location.ReferenceSystem;

/**
 * @author pplitzner
 \* @since Nov 30, 2015
 *
 */
public class AbcdTransformer extends InputTransformerBase{

    private static final long serialVersionUID = 5946317528955718151L;

    /**
     * {@inheritDoc}
     */
    @Override
    public ReferenceSystem getReferenceSystemByKey(String key) throws UndefinedTransformerMethodException {
        if (StringUtils.isBlank(key)){
            return null;
        }else{
            ReferenceSystem result = null;
            try {
                result = super.getReferenceSystemByKey(key);
            } catch (UndefinedTransformerMethodException e) {
                //do nothing
            }
            return result;
        }
    }

}
