/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.property.access.spi.Getter;

import net.sf.ehcache.Cache;

/**
 * This class is serializing and deserializing the CDM model for performance purposes.
 * To serialize see comments on {@link #main(String[])} and on
 * https://dev.e-taxonomy.eu/redmine/projects/edit/wiki/TaxonomicEditorDevelopersGuide#Model-Change-Actions
 *
 * @author c.mathew
 * @since 2015
 */
public class CdmModelCacher {

    private static final Logger logger = LogManager.getLogger();

    public static String HB_CONFIG_FILE_PATH= "/eu/etaxonomy/cdm/mappings/hibernate.cfg.xml";

    public static final String CDM_MAP_SER_FILE = "cdm.map.ser";
    public static final String CDM_MAP_SER_FOLDER = "/eu/etaxonomy/cdm/mappings/";
    public static final String CDM_MAP_SER_FILE_PATH = CDM_MAP_SER_FOLDER + CDM_MAP_SER_FILE;

    public void cacheGetterFields(Cache cache) throws IOException, ClassNotFoundException {

        Map<String, CdmModelFieldPropertyFromClass> modelClassMap = loadModelClassMap();

        cache.removeAll();

        for(Map.Entry<String, CdmModelFieldPropertyFromClass> entry : modelClassMap.entrySet()) {
            cache.put(new net.sf.ehcache.Element(entry.getKey(), entry.getValue()));
        }
    }

    public Map<String, CdmModelFieldPropertyFromClass> loadModelClassMap() throws IOException, ClassNotFoundException  {

        InputStream fin = this.getClass().getResourceAsStream(CDM_MAP_SER_FILE_PATH);
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

    	Map<String, CdmModelFieldPropertyFromClass> modelClassMap = new HashMap<>();
    	try {
//    		ConnectionProvider connectionProvider = registry.getService(ConnectionProvider.class);
//    		DatasourceConnectionProviderImpl providerImpl = registry.getService(DatasourceConnectionProviderImpl.class);

    		Metadata metadata = new MetadataSources( registry ).buildMetadata();
    		Collection<PersistentClass> entityBindings = metadata.getEntityBindings();
    		for (PersistentClass persistentClass : entityBindings) {
                Class<?> mappedClass = persistentClass.getMappedClass();
                if (mappedClass != null) {
                    handleEntityClass(modelClassMap, metadata, mappedClass);
                }
            }
    	}
    	catch (Exception e) {
    		StandardServiceRegistryBuilder.destroy( registry );
    		e.printStackTrace();
    	}
        return modelClassMap;
    }

    private void handleEntityClass(Map<String, CdmModelFieldPropertyFromClass> modelClassMap, Metadata metadata,
            Class<?> mappedClass) {
        String mappedClassName = mappedClass.getName();
        PersistentClass persistentClass = metadata.getEntityBinding(mappedClassName);
        CdmModelFieldPropertyFromClass fieldProperties = new CdmModelFieldPropertyFromClass(mappedClassName);
        logger.warn("Adding class : " + mappedClassName + " to cache");
        addGetters(persistentClass, fieldProperties);
        modelClassMap.put(mappedClassName, fieldProperties);
    }

    public static Configuration buildConfiguration(String hibernateConfigFilePath) {
        Configuration configuration = new Configuration().configure(hibernateConfigFilePath);
        return configuration;
    }

    private void addGetters(PersistentClass persistentClass, CdmModelFieldPropertyFromClass cmgmfc) {
        if (persistentClass != null) {
            @SuppressWarnings("unchecked")
            Iterator<Property> propertyIt = persistentClass.getPropertyIterator();

            while(propertyIt.hasNext()){
                Property property = propertyIt.next();
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

        // To create the serialised cdm map run
        // mvn exec:exec -Dexec.mainClass="eu.etaxonomy.cdm.cache.CdmModelCacher"
        // in the cdmlib-cache project root directory.
    	// See also https://dev.e-taxonomy.eu/redmine/projects/edit/wiki/TaxonomicEditorDevelopersGuide#Model-Change-Actions

        System.out.println("Start CdmModelCacher main.");
        CdmModelCacher cdmModelCacher = new CdmModelCacher();
        Map<String, CdmModelFieldPropertyFromClass> modelClassMap = cdmModelCacher.generateModelClassMap();
        try{
            System.out.println("Model created.");
        	if (!modelClassMap.isEmpty()){
        	    String strPath = CdmModelCacher.class.getProtectionDomain().getCodeSource().getLocation().getFile();
                File outFile = new File(strPath + CDM_MAP_SER_FILE_PATH);

                System.out.println("writing to " + outFile.getAbsolutePath());
        		FileOutputStream fout = new FileOutputStream(outFile);
        		ObjectOutputStream oos = new ObjectOutputStream(fout);
        		oos.writeObject(modelClassMap);
        		oos.close();
        		System.out.println("CDM Map serialized");
                System.exit(0);
        	}else{
        		String message = "CDM Map was empty. Model cache update NOT successful";
        		System.out.println(message);
        	}
        }catch(Exception ex){
            ex.printStackTrace();
        }

        System.exit(1);
    }
}