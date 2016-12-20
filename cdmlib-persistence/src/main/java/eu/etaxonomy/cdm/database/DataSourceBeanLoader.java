package eu.etaxonomy.cdm.database;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.common.CdmUtils;

@Component
public class DataSourceBeanLoader {

    private static final Logger logger = Logger.getLogger(DataSourceBeanLoader.class);

    private static final String DATASOURCE_BEANDEF_FILE = "datasources.xml";
    private static final String DATASOURCE_BEANDEF_PATH = CdmUtils.getCdmHomeDir().getPath();

    private static String userdefinedBeanDefinitionFile = null;

    public void setBeanDefinitionFile(String filename){
        userdefinedBeanDefinitionFile = filename;
    }


    /**
     * @return
     */
    public static <T> Map<String, T> loadDataSources(final Class<T> requiredType) {

        Map<String, T> dataSources = new HashMap<String, T>();
        String path = DATASOURCE_BEANDEF_PATH + (userdefinedBeanDefinitionFile == null ? DATASOURCE_BEANDEF_FILE : userdefinedBeanDefinitionFile);

        logger.info("loading DataSourceBeans from: " + path);
        FileSystemResource file = new FileSystemResource(path);
        XmlBeanFactory beanFactory  = new XmlBeanFactory(file);

        for(String beanName : beanFactory.getBeanDefinitionNames()){
            T datasource = beanFactory.getBean(beanName, requiredType);
            dataSources.put(beanName, datasource);
        }
        return (Map<String, T>) dataSources;
    }

}
