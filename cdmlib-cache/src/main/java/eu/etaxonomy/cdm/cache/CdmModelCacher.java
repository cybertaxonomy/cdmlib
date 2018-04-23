package eu.etaxonomy.cdm.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.property.access.spi.Getter;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;


/**
 * This class serializing and deserializing the CDM model for performance purposes.
 * To serialize it see the comments on {@link #main(String[])} and on
 * https://dev.e-taxonomy.eu/redmine/projects/edit/wiki/TaxonomicEditorDevelopersGuide#Model-Change-Actions
 *
 * @author c.mathew
 * @since 2015
 *
 */
public class CdmModelCacher {


    public static String HB_CONFIG_FILE_PATH= "/eu/etaxonomy/cdm/mappings/hibernate.cfg.xml";

    public static final String CDM_MAP_SER_FILE = "cdm.map.ser";
    public static final String CDM_MAP_SER_FOLDER = "/eu/etaxonomy/cdm/mappings/";
    public static final String CDM_MAP_SER_FILE_PATH = CDM_MAP_SER_FOLDER + CDM_MAP_SER_FILE;




    public void cacheGetterFields(Cache cache) throws IOException, ClassNotFoundException, URISyntaxException {
        Map<String, CdmModelFieldPropertyFromClass> modelClassMap = loadModelClassMap();

        cache.removeAll();

        for(Map.Entry<String, CdmModelFieldPropertyFromClass> entry : modelClassMap.entrySet()) {
            cache.put(new Element(entry.getKey(), entry.getValue()));
        }
    }

    public Map<String, CdmModelFieldPropertyFromClass> loadModelClassMap() throws URISyntaxException, IOException, ClassNotFoundException  {

        // ============== Eclpipse specific ============== //
        /*Bundle bundle = Platform.getBundle("eu.etaxonomy.taxeditor.cdmlib");

        URL modelMapFileBundleURL = bundle.getEntry(CDM_MAP_SER_FILE_PATH);
        URL modelMapFileURL = FileLocator.resolve(modelMapFileBundleURL);
        String modelMapFilePath = modelMapFileURL.getFile();
        FileInputStream fin = new FileInputStream(modelMapFilePath);
        */
        InputStream fin = this.getClass().getResourceAsStream(CDM_MAP_SER_FILE_PATH);
        // ==============000000000000000 ============== //

        ObjectInputStream ois = new ObjectInputStream(fin);
        @SuppressWarnings("unchecked")
		Map<String, CdmModelFieldPropertyFromClass> modelClassMap = (Map<String, CdmModelFieldPropertyFromClass>) ois.readObject();
        ois.close();
        return modelClassMap;
    }


    public Map<String, CdmModelFieldPropertyFromClass> generateModelClassMap() {

    	// A SessionFactory is set up once for an application!
        URL hibernateConfigFile = this.getClass().getResource(HB_CONFIG_FILE_PATH);
    	final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
    			.configure(hibernateConfigFile) // configures settings from hibernate.cfg.xml
    			.build();
    	SessionFactory sessionFactory = null;
    	Map<String, CdmModelFieldPropertyFromClass> modelClassMap = new HashMap<>();
    	try {
//    		ConnectionProvider connectionProvider = registry.getService(ConnectionProvider.class);
//    		DatasourceConnectionProviderImpl providerImpl = registry.getService(DatasourceConnectionProviderImpl.class);

    		Metadata metadata = new MetadataSources( registry ).buildMetadata();
    		sessionFactory = metadata.buildSessionFactory();
//    		Configuration configuration = buildConfiguration(HB_CONFIG_FILE_PATH);
        	Map<String, ClassMetadata> classMetaDataMap = sessionFactory.getAllClassMetadata();
//        	Metadata metadata = new MetadataSources( registry ).getMetadataBuilder().applyImplicitNamingStrategy( ImplicitNamingStrategyJpaCompliantImpl.INSTANCE ).build();


            for(ClassMetadata classMetaData :classMetaDataMap.values()) {
            	Class<?> mappedClass = classMetaData.getMappedClass();

                String mappedClassName = mappedClass.getName();

                PersistentClass persistentClass =metadata.getEntityBinding(mappedClassName);
                CdmModelFieldPropertyFromClass cmgmfc = new CdmModelFieldPropertyFromClass(mappedClassName);
                System.out.println("Adding class : " + mappedClassName + " to cache");
                addGetters(persistentClass, cmgmfc);
                modelClassMap.put(mappedClassName, cmgmfc);
            }
    	}
    	catch (Exception e) {
    		// The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
    		// so destroy it manually.
    		StandardServiceRegistryBuilder.destroy( registry );
    		e.printStackTrace();
    	}


        return modelClassMap;
    }


    public static Configuration buildConfiguration(String hibernateConfigFilePath) {
        Configuration configuration = new Configuration().configure(hibernateConfigFilePath);
        configuration.buildMappings();
        return configuration;
    }

    private void addGetters(PersistentClass persistentClass, CdmModelFieldPropertyFromClass cmgmfc) {
        if (persistentClass != null) {
            Iterator propertyIt = persistentClass.getPropertyIterator();

            while(propertyIt.hasNext())
            {
                Property property = (Property)propertyIt.next();
                Getter getter = property.getGetter(persistentClass.getMappedClass());
                if(getter != null && getter.getMember() != null) {
                    Field field = (Field)getter.getMember();

                    //logger.info(" - contains field '" + field.getName() + "' of type '" + field.getType().getName() + "'");
                    cmgmfc.addGetMethods(field.getName());
                }
            }
            addGetters(persistentClass.getSuperclass(), cmgmfc);
        }
    }



    public static void main(String argv[]) {

        // To re-create the serialised cdm map run,
        // mvn exec:java -Dexec.mainClass="eu.etaxonomy.cdm.cache.CdmModelCacher"
        // in the eu.etaxonomy.taxeditor.cdmlib project root dir
    	// See also https://dev.e-taxonomy.eu/redmine/projects/edit/wiki/TaxonomicEditorDevelopersGuide#Model-Change-Actions
    	//Note AM: does not fully work for me, but running the main from the IDE works.


        CdmModelCacher cdmModelCacher = new CdmModelCacher();
        Map<String, CdmModelFieldPropertyFromClass> modelClassMap = cdmModelCacher.generateModelClassMap();
        try{
        	if (!modelClassMap.isEmpty()){
        	    File outFile = new File("src/main/resources/" + CDM_MAP_SER_FILE_PATH);
        	    System.out.println("writing to " + outFile.getAbsolutePath());
        		FileOutputStream fout = new FileOutputStream(outFile);
        		ObjectOutputStream oos = new ObjectOutputStream(fout);
        		oos.writeObject(modelClassMap);
        		oos.close();
        		System.out.println("CDM Map serialized");
        	}else{
        		String message = "CDM Map was empty. Model cache update NOT successful";
        		System.out.println(message);
        	}

        }catch(Exception ex){
            ex.printStackTrace();
        }

    }


}
