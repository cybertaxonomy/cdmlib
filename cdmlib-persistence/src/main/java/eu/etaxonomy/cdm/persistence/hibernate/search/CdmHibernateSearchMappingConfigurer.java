/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate.search;

import org.hibernate.search.mapper.orm.mapping.HibernateOrmMappingConfigurationContext;
import org.hibernate.search.mapper.orm.mapping.HibernateOrmSearchMappingConfigurer;

/**
 * Avoids Hibernate Search building a Jandex index by opening JAR filesystems.
 * <p>
 * Under Tomcat / remote-webapp the JAR is often already opened as a
 * {@link java.nio.file.FileSystem}; a second {@code FileSystems.newFileSystem}
 * then fails with {@code FileSystemAlreadyExistsException} (message {@code null}),
 * surfaced as {@code HSEARCH900016}. CDM {@code @Indexed} types are already
 * registered as Hibernate entities, so classpath/Jandex discovery is not required.
 */
public class CdmHibernateSearchMappingConfigurer implements HibernateOrmSearchMappingConfigurer {

    @Override
    public void configure(HibernateOrmMappingConfigurationContext context) {
        context.annotationMapping()
                .discoverJandexIndexesFromAddedTypes(false)
                .discoverAnnotatedTypesFromRootMappingAnnotations(false);
    }
}
