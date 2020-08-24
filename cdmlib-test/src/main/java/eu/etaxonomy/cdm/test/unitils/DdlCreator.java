/**
 *
 */
package eu.etaxonomy.cdm.test.unitils;


import java.util.EnumSet;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.dialect.H2CorrectedDialectTest;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.orm.hibernate5.LocalSessionFactoryBuilder;

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

//	private void execute(Class<?> dialect, String lowerCaseDialectName, Class<?>... classes) throws IOException, HibernateException, InstantiationException, IllegalAccessException {
//		String classPath = "eu/etaxonomy/cdm/hibernate.cfg.xml";
//		ClassPathResource resource = new ClassPathResource(classPath);
//		File file = resource.getFile();
//
////		File file = new File("C:\\Users\\pesiimport\\Documents\\cdm-3.3\\cdmlib-persistence\\src\\main\\resources\\eu\\etaxonomy\\cdm\\hibernate.cfg.xml");
//		System.out.println(file.exists());
//
//		Configuration config = new Configuration().addFile(file);
//		config.setProperty(AvailableSettings.DIALECT, dialect.getCanonicalName());
////		NamingStrategyDelegator;
//		PhysicalNamingStrategy namingStrategy = new PhysicalNamingStrategyStandardImpl();
////		        new DefaultComponentSafeNamingStrategy(); //; = new ImprovedNamingStrategy();
//		config.setPhysicalNamingStrategy(namingStrategy);
//
//		config.configure(file);
////		String[] schema = config.generateSchemaCreationScript((Dialect)dialect.newInstance());
////		for (String s : schema){
////			System.out.println(s);
////		}
//
//		//FIXME #4716
//		EnversService enversService = new EnversServiceImpl();
////		. .getFor(config.);
//		SchemaExport schemaExport = new SchemaExport(config);
//		schemaExport.setDelimiter(";");
//		schemaExport.drop(false, false);
//		schemaExport.setOutputFile(String.format("%s.%s.%s ", new Object[] {"new-cdm", lowerCaseDialectName, "sql" }));
//		boolean consolePrint = true;
//		boolean exportInDatabase = false;
//		schemaExport.create(consolePrint, exportInDatabase);
//
//		schemaExport.execute(consolePrint, exportInDatabase, false, true);
//
//	}


    public void execute2(Class<?> dialect, String lowerCaseDialectName){
        StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.dialect", dialect.getName()); // dialect

        StandardServiceRegistry serviceRegistry = registryBuilder.build();

        MetadataSources metadataSources = new MetadataSources(serviceRegistry);


        PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
        new LocalSessionFactoryBuilder(null, resourceLoader, metadataSources).scanPackages("eu.etaxonomy.cdm.model");

        PhysicalNamingStrategy namingStrategy = new PhysicalNamingStrategyStandardImpl();

        Metadata metadata = metadataSources.buildMetadata();

        new SchemaExport().setFormat(true).setDelimiter(";").setOutputFile("export.sql")
            .createOnly(EnumSet.of(TargetType.STDOUT, TargetType.SCRIPT), metadata);
    }

//
//	private void test5_1(){
//	    String packageName;
//	    String propertiesFile;
//	    ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
//                .loadProperties(propertiesFile)
//                .build();
//	    MetadataSources metadata = new MetadataSources(serviceRegistry);
//
//	    new org.reflections.Reflections(packageName)
//	            .getTypesAnnotatedWith(Entity.class)
//	            .forEach(metadata::addAnnotatedClass);
//
//        //STDOUT will export to output window, but other `TargetType` values are available to export to file or to the db.
//        EnumSet<TargetType> targetTypes = EnumSet.of(TargetType.STDOUT);
//
//        SchemaExport export = new SchemaExport();
//
//        export.setDelimiter(";");
//        export.setFormat(true);
//
//        export.createOnly(targetTypes, metadata.buildMetadata());
//	}

//
//	public void test5_1_2(){
//	    ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
//                .configure("hibernate.cfg.xml")
//                .build();
//	    MetadataImplementor metadata = (MetadataImplementor) new  MetadataSources(serviceRegistry)
//	            .buildMetadata();
//	    SchemaExport schemaExport = new SchemaExport(metadata);
//	    schemaExport.setOutputFile("hbm2schema.sql");
//	    schemaExport.create(true, true);
//	    ( (StandardServiceRegistryImpl) serviceRegistry ).destroy();
//	}

}

