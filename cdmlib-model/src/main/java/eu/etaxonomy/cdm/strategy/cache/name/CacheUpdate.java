/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.strategy.cache.name;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author a.mueller
 * @since 03.08.2009
 * @version 1.0
 */
@Target({FIELD}) 
@Retention(RUNTIME)
public @interface CacheUpdate {
	String[] value() default {};
	String[] noUpdate() default {};
	
}
