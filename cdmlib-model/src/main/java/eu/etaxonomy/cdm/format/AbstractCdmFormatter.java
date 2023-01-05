/**
* Copyright (C) 2015 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.format;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * @author pplitzner
 * @since Nov 30, 2015
 */
public abstract class AbstractCdmFormatter implements ICdmFormatter {


    public static final String COMMA_CHAR = ",";
    public static final String SPACE_CHAR = " ";
    public static final String OPEN_BRACKET_CHAR = "(";
    public static final String CLOSE_BRACKET_CHAR = ")";

    protected FormatKey[] formatKeys;

    protected Map<FormatKey, String> formatKeyMap = new HashMap<>();

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
    	return format(object, formatKeys);
    }

    protected void initFormatKeys(Object object){
        formatKeyMap.put(FormatKey.CLOSE_BRACKET, CLOSE_BRACKET_CHAR);
        formatKeyMap.put(FormatKey.OPEN_BRACKET, OPEN_BRACKET_CHAR);
        formatKeyMap.put(FormatKey.SPACE, SPACE_CHAR);
        formatKeyMap.put(FormatKey.COMMA, COMMA_CHAR);
    }

    protected boolean isNotBlank(String str) {
        return StringUtils.isNotBlank(str);
    }

    protected boolean isBlank(String str) {
        return StringUtils.isBlank(str);
    }

}
