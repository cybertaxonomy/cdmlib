package eu.etaxonomy.cdm.database;

import org.hibernate.cache.CacheProvider;
import org.springframework.beans.factory.config.BeanDefinition;

import eu.etaxonomy.cdm.database.CdmPersistentDataSource.HBM2DDL;

public interface ICdmDataSource {

	/**
	 * Returns a BeanDefinition object of type  DriverManagerDataSource that contains
	 * datsource properties (url, username, password, ...)
	 * @return
	 */
	public BeanDefinition getDatasourceBean();
	
	/**
	 * @param hbm2dll
	 * @param showSql
	 * @return
	 */
	public BeanDefinition getHibernatePropertiesBean(HBM2DDL hbm2dll);
	
	/**
	 * @param hbm2dll
	 * @param showSql
	 * @return
	 */
	public BeanDefinition getHibernatePropertiesBean(HBM2DDL hbm2dll, Boolean showSql, Boolean formatSql, Class<? extends CacheProvider> cacheProviderClass);

}