/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.unitils;


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
			new DdlCreator().execute2(H2CorrectedDialectTest.class, "h2");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public void execute2(Class<?> dialect, String lowerCaseDialectName){

        String outputFileName = String.format("%s.%s.%s ", new Object[] {"001-cdm", lowerCaseDialectName, "sql" });
        String templateFile = "dbscripts/" + outputFileName + "-template";
        String outputPath = "src/main/resources/dbscripts/" + outputFileName;

//      String classPath = "eu/etaxonomy/cdm/hibernate.cfg.xml";
//      ClassPathResource resource = new ClassPathResource(classPath);
//      File configurationFile = resource.getFile();

        StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DIALECT, dialect.getCanonicalName())  // dialect
                //alternative: .loadProperties(propertiesFile)
                //alternative2: .configure("hibernate.cfg.xml")
                //alternative3: .configure(configurationFile)
                //SCRIPT_THEN_METADATA does not work with SchemaExport:
//                .applySetting(AvailableSettings.HBM2DDL_CREATE_SCRIPT_SOURCE, templateFile)
//                .applySetting(AvailableSettings.HBM2DDL_CREATE_SOURCE, SourceType.SCRIPT_THEN_METADATA);
                ;

        StandardServiceRegistry serviceRegistry = registryBuilder.build();

        MetadataSources metadataSources = new MetadataSources(serviceRegistry);


        PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
        new LocalSessionFactoryBuilder(null, resourceLoader, metadataSources).scanPackages("eu.etaxonomy.cdm.model");

//        PhysicalNamingStrategy namingStrategy = new PhysicalNamingStrategyStandardImpl();
        ImplicitNamingStrategyComponentPathImpl namingStrategy = new ImplicitNamingStrategyComponentPathImpl();
        PhysicalNamingStrategy physicalNamingStrategy = new UpperCasePhysicalNamingStrategyStandardImpl();

        Metadata metadata = metadataSources.getMetadataBuilder(serviceRegistry)
                .applyImplicitSchemaName("public")
                .applyImplicitNamingStrategy(namingStrategy)
                .applyPhysicalNamingStrategy(physicalNamingStrategy)
                .build();

        EnumSet<TargetType> targetTypes = EnumSet.of(/*TargetType.STDOUT, */TargetType.SCRIPT);
        new SchemaExport()
            .setFormat(true)
            .setDelimiter(";")
//            .setImportFiles(templateFile)
            .setOutputFile(outputPath)
            .createOnly(targetTypes, metadata);

        ((StandardServiceRegistryImpl) serviceRegistry).destroy();

        //approaches for JPA and eclipselink can be found here: https://stackoverflow.com/questions/297438/auto-generate-data-schema-from-jpa-annotated-entity-classes;
    }

}

