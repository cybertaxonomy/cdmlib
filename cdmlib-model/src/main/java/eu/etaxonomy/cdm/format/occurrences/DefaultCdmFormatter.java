/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.occurrences;


/**
 * @author pplitzner
 \* @since Nov 30, 2015
 *
 */
public class DefaultCdmFormatter extends AbstractCdmFormatter {

    public DefaultCdmFormatter(Object object, FormatKey[] formatKeys) {
        super(object, formatKeys);
    }

    @Override
    public String format(Object object, FormatKey... formatKeys) {
        return object.toString();
    }
    
    @Override
    public String format(Object object) {
    	return object.toString();
    }
}
