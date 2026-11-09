/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.search;

import java.util.Map;
import java.util.Properties;

/**
 * Shared Hibernate Search defaults for CDM SessionFactory / EMF bootstrap.
 * Explicit config (XML, datasource props) always wins.
 */
public final class CdmHibernateSearchSettings {

    /**
     * Keep in sync with {@code lucene.version} in the root POM.
     * Pins analyzer compatibility (avoids HSEARCH000075 warnings).
     */
    public static final String LUCENE_VERSION = "9.11.1";

    public static final String MAPPING_CONFIGURER =
            CdmHibernateSearchMappingConfigurer.class.getName();

    private static final String PROP_MAPPING_CONFIGURER = "hibernate.search.mapping.configurer";
    private static final String PROP_LUCENE_VERSION = "hibernate.search.backend.lucene_version";

    private CdmHibernateSearchSettings() {
    }

    /**
     * Apply CDM defaults where not already set (JPA property map).
     */
    public static void applyDefaults(Map<String, Object> properties) {
        properties.putIfAbsent(PROP_MAPPING_CONFIGURER, MAPPING_CONFIGURER);
        properties.putIfAbsent(PROP_LUCENE_VERSION, LUCENE_VERSION);
    }

    /**
     * Apply CDM defaults where not already set (native Configuration properties).
     */
    public static void applyDefaults(Properties properties) {
        if (!properties.containsKey(PROP_MAPPING_CONFIGURER)) {
            properties.setProperty(PROP_MAPPING_CONFIGURER, MAPPING_CONFIGURER);
        }
        if (!properties.containsKey(PROP_LUCENE_VERSION)) {
            properties.setProperty(PROP_LUCENE_VERSION, LUCENE_VERSION);
        }
    }
}
