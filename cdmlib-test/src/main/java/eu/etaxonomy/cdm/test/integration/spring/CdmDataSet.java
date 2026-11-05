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
 * Loads a FlatXml DBUnit dataset before the test method (Unitils {@code @DataSet} replacement).
 * <p>
 * Default location: classpath resource next to the test class,
 * named {@code <SimpleClassName>.xml} (or {@code <SimpleClassName>-<appendix>.xml}).
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CdmDataSet {

    /**
     * Explicit classpath location(s). Empty → derive from test class name.
     */
    String[] value() default {};

    /**
     * Appendix for the default filename (Unitils {@code @DataSet} fileAppendix).
     */
    String fileAppendix() default "";

    /**
     * If true, run {@link ClearDBDataSet} CLEAN_INSERT first (term tables preserved),
     * then CLEAN_INSERT the test dataset (Unitils {@code CleanSweepInsertLoadStrategy}).
     */
    boolean cleanSweep() default false;

    /**
     * If true, use DBUnit REFRESH instead of CLEAN_INSERT
     * (Unitils {@code RefreshLoadStrategy}).
     */
    boolean refresh() default false;
}
