/**
 *
 */
package eu.etaxonomy.cdm.test.unitils;


import java.io.File;
import java.io.IOException;

import org.hibernate.HibernateException;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.H2CorrectedDialect;
import org.hibernate.envers.boot.internal.EnversService;
import org.hibernate.envers.boot.internal.EnversServiceImpl;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.core.io.ClassPathResource;

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
 *
 */
public class DdlCreator {

	public static void main(String[] args) {
		try {
			new DdlCreator().execute(H2CorrectedDialect.class, "h2");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void execute(Class<?> dialect, String lowerCaseDialectName, Class<?>... classes) throws IOException, HibernateException, InstantiationException, IllegalAccessException {
		String classPath = "eu/etaxonomy/cdm/hibernate.cfg.xml";
		ClassPathResource resource = new ClassPathResource(classPath);
		File file = resource.getFile();

//		File file = new File("C:\\Users\\pesiimport\\Documents\\cdm-3.3\\cdmlib-persistence\\src\\main\\resources\\eu\\etaxonomy\\cdm\\hibernate.cfg.xml");
		System.out.println(file.exists());

		Configuration config = new Configuration().addFile(file);
		config.setProperty(AvailableSettings.DIALECT, dialect.getCanonicalName());
//		NamingStrategyDelegator;
		PhysicalNamingStrategy namingStrategy = new PhysicalNamingStrategyStandardImpl();
//		        new DefaultComponentSafeNamingStrategy(); //; = new ImprovedNamingStrategy();
		config.setPhysicalNamingStrategy(namingStrategy);

		config.configure(file);
//		String[] schema = config.generateSchemaCreationScript((Dialect)dialect.newInstance());
//		for (String s : schema){
//			System.out.println(s);
//		}

		//FIXME #4716
		EnversService enversService = new EnversServiceImpl();
//		. .getFor(config.);
		SchemaExport schemaExport = new SchemaExport(config);
		schemaExport.setDelimiter(";");
		schemaExport.drop(false, false);
		schemaExport.setOutputFile(String.format("%s.%s.%s ", new Object[] {"new-cdm", lowerCaseDialectName, "sql" }));
		boolean consolePrint = true;
		boolean exportInDatabase = false;
		schemaExport.create(consolePrint, exportInDatabase);

		schemaExport.execute(consolePrint, exportInDatabase, false, true);

	}
}

