/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.match;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author a.mueller
 * @created 03.08.2009
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface Match {
	MatchMode value();

	ReplaceMode cacheReplaceMode() default ReplaceMode.ALL;
	MatchMode replaceMatchMode() default MatchMode.EQUAL_OR_ONE_NULL;
	String[] cacheReplacedProperties() default {};

	public enum ReplaceMode{
		ALL,  //Selects all properties
		NONE,  //Selects no properties
		DEFINED,  //sel
		DEFINED_REVERSE
	}
//	IMatchStrategy matchStrategy();

}
