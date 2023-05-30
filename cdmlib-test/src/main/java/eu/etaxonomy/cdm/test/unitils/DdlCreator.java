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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
 * This class may help to create your DDL file.
 *
 * TODO: Not sure if the following notes are still correct:
 *
 * Also the result needs to be changed to uppercase and some _uniquekey statements need to be replaced as they are not
 * unique themselves.
 *
 * The result is stored in a file "new-cdm.h2.sql" in the root directory and is written to the console.
 *
 * @author a.mueller
 */
public class DdlCreator {

    protected static final Logger logger = LogManager.getLogger();

	public static void main(String[] args) {
	    try {
            System.out.println("Java version: " + System.getProperty("java.version"));
            System.setSecurityManager(null); //avoids security exception when started by ant (problem is the jmx server registration by log4j2, similar issue is described at https://stackoverflow.com/questions/12195868/java-security-accesscontrolexception-when-using-ant-but-runs-ok-when-invoking-j )
        } catch (Exception e1) {
            e1.printStackTrace();
        }

	    try {
		    new DdlCreator().execute(H2CorrectedDialectTest.class, "h2");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public void execute(Class<?> dialect, String lowerCaseDialectName){

        try {
            String fileName = String.format("%s.%s.%s", new Object[] {"001-cdm", lowerCaseDialectName, "sql" });
            String outputFileClassPath = "dbscripts/" + fileName;

            ClassPathResource resource = new ClassPathResource(outputFileClassPath);
            File folder = resource.getFile().getParentFile();
            String outputPath = folder.getCanonicalPath()+File.separator + fileName;

            StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DIALECT, dialect.getCanonicalName())  // dialect
//                .applySetting(AvailableSettings.HBM2DDL_CREATE_SCRIPT_SOURCE, resource.getURL())  //does not have the expected effect
                ;

            StandardServiceRegistry serviceRegistry = registryBuilder.build();

            MetadataSources metadataSources = new MetadataSources(serviceRegistry);

            //model scan
            PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
            new LocalSessionFactoryBuilder(null, resourceLoader, metadataSources).scanPackages("eu.etaxonomy.cdm.model");

            //metadata
            ImplicitNamingStrategyComponentPathImpl namingStrategy = new ImplicitNamingStrategyComponentPathImpl();
            PhysicalNamingStrategy physicalNamingStrategy = new UpperCasePhysicalNamingStrategyStandardImpl();
            Metadata metadata = metadataSources.getMetadataBuilder()
                    .applyImplicitSchemaName("public")
                    .applyImplicitNamingStrategy(namingStrategy)
                    .applyPhysicalNamingStrategy(physicalNamingStrategy)
                    .build();

            //export
            EnumSet<TargetType> targetTypes = EnumSet.of(TargetType.SCRIPT);
            new SchemaExport()
                .setFormat(true)
                .setDelimiter(";")
//                .setImportFiles(templatePath)  //does not have the expected effect
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