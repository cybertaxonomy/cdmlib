package eu.etaxonomy.cdm.persistence.hibernate;

import java.util.Properties;

import org.springframework.stereotype.Component;

@Component
class TableGeneratorGlobalOverride {

	public TableGeneratorGlobalOverride(){}

//	public static final ThreadLocal<Properties> threadLocalProperties = new ThreadLocal<Properties>();

	public static Properties properties;

	public static Properties getProperties() {
//		return threadLocalProperties.get();
		return TableGeneratorGlobalOverride.properties;
	}

	public void setProperties(Properties properties) {
//		threadLocalProperties.set(properties);
		TableGeneratorGlobalOverride.properties = properties;
	}

}