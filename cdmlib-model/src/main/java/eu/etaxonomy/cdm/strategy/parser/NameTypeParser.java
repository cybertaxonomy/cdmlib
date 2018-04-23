/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.strategy.parser;

import org.apache.commons.lang.StringUtils;

import eu.etaxonomy.cdm.model.name.NameTypeDesignationStatus;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 \* @since 02.08.2011
 *
 */
public class NameTypeParser {

	private static final String desigPattern =  "\\sdesig(\\.|nation)?";

	/**
	 * see also CentralAfricaFernsTaxonParser#handleTypeLocationPart
	 */
	public static final String typeTypePattern = "("+
			"lecto(\\.|type)?+" +
			"(original|present|subsequent)"+ desigPattern +
			"(subsequent monotypy|tautonomy)" +
			")";



	public static NameTypeDesignationStatus parseNameTypeStatus(String type) throws UnknownCdmTypeException {
		if (StringUtils.isBlank(type)){
			return null;
		}
		if (type.matches("(?i).*" + desigPattern)){
			if (type.matches("(?i)original" + desigPattern)){
				return NameTypeDesignationStatus.ORIGINAL_DESIGNATION();
			}else if (type.matches("(?i)present" + desigPattern)){
				return NameTypeDesignationStatus.PRESENT_DESIGNATION();
			}else if (type.matches("(?i)subsequent" + desigPattern)){
				return NameTypeDesignationStatus.SUBSEQUENT_DESIGNATION();
			}

		}else if(type.matches("(?i)subsequent monotypy")){
			return NameTypeDesignationStatus.SUBSEQUENT_MONOTYPY();
		}else if(type.matches("(?i)monotypy")){
			return NameTypeDesignationStatus.MONOTYPY();
		}else if(type.matches("(?i)tautonomy")){
			return NameTypeDesignationStatus.TAUTONYMY();
		}else if(type.matches("(?i)lectotype")){
			return NameTypeDesignationStatus.LECTOTYPE();
		}else if(type.matches("(?i)automatic")){
			return NameTypeDesignationStatus.AUTOMATIC();
		}else if(type.matches("(?i)not applicable")){
			return NameTypeDesignationStatus.NOT_APPLICABLE();
		}
		String message = "Type Status not supported: " + type;
		throw new UnknownCdmTypeException(message);

	}
}
