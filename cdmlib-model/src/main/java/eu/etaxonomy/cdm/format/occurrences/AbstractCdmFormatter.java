// $Id$
/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format.occurrences;

import java.util.HashMap;
import java.util.Map;

import eu.etaxonomy.cdm.format.ICdmFormatter;

/**
 * @author pplitzner
 * @date Nov 30, 2015
 *
 */
public abstract class AbstractCdmFormatter implements ICdmFormatter {

    protected FormatKey[] formatKeys;
    
    protected Map<FormatKey, String> formatKeyMap = new HashMap<ICdmFormatter.FormatKey, String>();

    public AbstractCdmFormatter(Object object, FormatKey[] formatKeys) {
    	this.formatKeys = formatKeys;
        initFormatKeys(object);
    }

    @Override
    public String format(Object object, FormatKey... formatKeys) {
        StringBuilder builder = new StringBuilder();
        for (FormatKey formatKey : formatKeys) {
            String string = formatKeyMap.get(formatKey);
            if(string!=null){
                builder.append(string);
            }
        }
        return builder.toString().trim();
    }
    
    @Override
    public String format(Object object) {
        StringBuilder builder = new StringBuilder();
        for (FormatKey formatKey : formatKeys) {
            String string = formatKeyMap.get(formatKey);
            if(string!=null){
                builder.append(string);
            }
        }
        return builder.toString().trim();
    }

    protected void initFormatKeys(Object object){
        formatKeyMap.put(FormatKey.CLOSE_BRACKET, ")");
        formatKeyMap.put(FormatKey.OPEN_BRACKET, "(");
        formatKeyMap.put(FormatKey.SPACE, " ");
        formatKeyMap.put(FormatKey.COMMA, ",");
    }

}
