// $Id$
/**
 * Copyright (C) 2013 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.config;

import org.apache.lucene.util.Version;

/**
 * @author a.kohlbecker
 * @date Mar 26, 2013
 *
 */
public class Configuration {

    /**
     * This should be set via the hibernate properties but at the time being it
     * is quite complex to get <code>hibernate.search.lucene_version</code> from
     * the configuration and to pass it to all classes which require this
     * version As a preliminary and unobtrusive solution the luceneVersion is
     * now provided by this.
     * <p>
     * TODO A better solution for the future would be to provide all Lucene
     * related instances of <code>LuceneSearch</code>, etc via a special
     * factory. This factors would be a spring bean and thus could have access
     * to the hibernate configuration. see #3369 (Lucene search factory or builder implemented)
     *
     */
    @Deprecated // so we now it is not 100% save to use this
    public static Version luceneVersion = Version.LUCENE_4_10_4;

    /**
     * Login name for the first user 'admin'
     */
    public static String adminLogin = "admin";

    /**
     * Default password for the first user 'admin'
     */
    public static String adminPassword = "00000";

}
