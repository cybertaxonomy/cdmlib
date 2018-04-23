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

import eu.etaxonomy.cdm.model.common.DefinedTerm;
import eu.etaxonomy.cdm.strategy.exceptions.UnknownCdmTypeException;

/**
 * @author a.mueller
 \* @since 28.10.2017
 */
public class DeterminationModifierParser {


//	public static final String qualifierPattern = "(?i)(aff|cf|vel. aff)\\.?";



	public static DefinedTerm parseDeterminationQualifier(String qualifier) throws UnknownCdmTypeException {

		if (StringUtils.isBlank(qualifier)){
			return null;
		}
		DefinedTerm term;
		if (qualifier.matches("aff(.?|inis)")){
			term = DefinedTerm.DETERMINATION_MODIFIER_AFFINIS();
		}else if (qualifier.matches("(cf.?|confer)")){
		    term = DefinedTerm.DETERMINATION_MODIFIER_CONFER();
		}else{
			String message = "Determination qualifier not supported: " + qualifier;
			throw new UnknownCdmTypeException(message);
		}
		return term;
	}
}
