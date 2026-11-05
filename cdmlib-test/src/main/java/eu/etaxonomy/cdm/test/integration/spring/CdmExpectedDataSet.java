/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.integration.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Asserts DB state after the test method (Unitils {@code @ExpectedDataSet} replacement).
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CdmExpectedDataSet {

    /**
     * Explicit classpath location(s). Empty → {@code <SimpleClassName>-result.xml}
     * or {@code <SimpleClassName>-<appendix>-result.xml}.
     */
    String[] value() default {};

    String fileAppendix() default "";
}
