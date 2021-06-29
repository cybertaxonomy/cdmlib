/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.unitils;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyComponentPathImpl;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.registry.internal.StandardServiceRegistryImpl;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.H2CorrectedDialectTest;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.orm.hibernate5.LocalSessionFactoryBuilder;

import eu.etaxonomy.cdm.persistence.hibernate.UpperCasePhysicalNamingStrategyStandardImpl;

/**
 *
 * This class may help to create your DDL file.
 * However, it does not support Auditing table yet as they are (maybe) not supported
 * by Hibernate 4 hbm2dll.
 * It is also unclear if the antrun plugin supports envers in hibernate 4. I wasn't successful with it.
 * http://docs.jboss.org/hibernate/orm/4.2/devguide/en-US/html/ch15.html#envers-generateschema
 *
 * Also the result needs to be changed to uppercase and some _uniquekey statements need to be replaced as they are not
 * unique themselves.
 *
 * The result is stored in a file "new-cdm.h2.sql" in the root directory and is written to the console.
 *
 * @author a.mueller
 */
public class DdlCreator {

	public static void main(String[] args) {
		try {
			new DdlCreator().execute(H2CorrectedDialectTest.class, "h2");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public void execute(Class<?> dialect, String lowerCaseDialectName){

        try {
            String outputFileName = String.format("%s.%s.%s", new Object[] {"001-cdm", lowerCaseDialectName, "sql" });
            String outputFileClassPath = "dbscripts/" + outputFileName;
//            String templateFileName = outputFileName + "-template";
//            String templateFileClassPath = "dbscripts/" + templateFileName;

            ClassPathResource resource = new ClassPathResource(outputFileClassPath);
            File folder = resource.getFile().getParentFile();
            String outputPath = folder.getCanonicalPath()+File.separator + outputFileName;
//            String templatePath = folder.getCanonicalPath()+File.separator + templateFileName;
            System.out.println(outputPath);
//            System.out.println(templatePath);

            StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DIALECT, dialect.getCanonicalName())  // dialect
//                .applySetting(AvailableSettings.HBM2DDL_CREATE_SCRIPT_SOURCE, resource.getURL())
                ;

            StandardServiceRegistry serviceRegistry = registryBuilder.build();

            MetadataSources metadataSources = new MetadataSources(serviceRegistry);

            //model scan
            PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
            new LocalSessionFactoryBuilder(null, resourceLoader, metadataSources).scanPackages("eu.etaxonomy.cdm.model");

            //metadata
            ImplicitNamingStrategyComponentPathImpl namingStrategy = new ImplicitNamingStrategyComponentPathImpl();
            PhysicalNamingStrategy physicalNamingStrategy = new UpperCasePhysicalNamingStrategyStandardImpl();
            Metadata metadata = metadataSources.getMetadataBuilder(serviceRegistry)
                    .applyImplicitSchemaName("public")
                    .applyImplicitNamingStrategy(namingStrategy)
                    .applyPhysicalNamingStrategy(physicalNamingStrategy)
                    .build();

            //export
            EnumSet<TargetType> targetTypes = EnumSet.of(/*TargetType.STDOUT, */TargetType.SCRIPT);
            new SchemaExport()
                .setFormat(true)
                .setDelimiter(";")
//                .setImportFiles(templatePath)
                .setOutputFile(outputPath)
                .createOnly(targetTypes, metadata);

            ((StandardServiceRegistryImpl) serviceRegistry).destroy();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        //approaches for JPA and eclipselink can be found here: https://stackoverflow.com/questions/297438/auto-generate-data-schema-from-jpa-annotated-entity-classes;
    }
}