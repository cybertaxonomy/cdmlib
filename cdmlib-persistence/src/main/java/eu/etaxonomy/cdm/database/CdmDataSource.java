package eu.etaxonomy.cdm.database;

import java.util.Enumeration;
import java.util.Properties;

import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.NoCacheProvider;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

import eu.etaxonomy.cdm.database.DatabaseTypeEnum;
import eu.etaxonomy.cdm.database.CdmPersistentDataSource.HBM2DDL;

/**
 * @author a.mueller
 *
 */
public class CdmDataSource implements ICdmDataSource {
	
	private DatabaseTypeEnum dbType;
	private String server;
	private String database; 
	private int port = -1;
	private String username;
	private String password;
	private boolean isLazy = true;
	private String initMethodName = null;
	private String destroyMethodName = null;
	private HBM2DDL hbm2dll = HBM2DDL.VALIDATE;
	private boolean showSql = false;
	private boolean formatSql;
	private Class<? extends CacheProvider> cacheProviderClass = NoCacheProvider.class;;

	static public ICdmDataSource  NewMySqlInstance(String server, String database, String username, String password){
		return new CdmDataSource(DatabaseTypeEnum.MySQL, server, database, -1, username, password);
	}
	
	static public ICdmDataSource  NewMySqlInstance(String server, String database, int port, String username, String password){
		return new CdmDataSource(DatabaseTypeEnum.MySQL, server, database, port, username, password);
	}

	/**
	 * @param server
	 * @param database
	 * @param port
	 */
	private CdmDataSource(DatabaseTypeEnum dbType, String server, String database, int port, String username, String password) {
		super();
		this.dbType = dbType;
		this.server = server;
		this.database = database;
		this.port = port;
		this.username = username;
		this.password = password;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmDataSource#getDatasourceBean()
	 */
	public BeanDefinition getDatasourceBean(){
		AbstractBeanDefinition bd = new RootBeanDefinition(dbType.getDriverManagerDataSourceClass());
		//attributes
		bd.setLazyInit(isLazy);
		if (initMethodName != null && ! initMethodName.trim().equals("") ){
			bd.setInitMethodName(initMethodName);
		}
		if (destroyMethodName != null  && ! destroyMethodName.trim().equals("") ){
			bd.setInitMethodName(destroyMethodName);
		}
		
		//properties
		MutablePropertyValues props = new MutablePropertyValues();
		Properties persistentProperties = getDatasourceProperties();
		Enumeration<String> keys = (Enumeration)persistentProperties.keys(); 
		while (keys.hasMoreElements()){
			String key = (String)keys.nextElement();
			props.addPropertyValue(key, persistentProperties.getProperty(key));
		}

		bd.setPropertyValues(props);
		return bd;
	}
	
	/**
	 * Returns the list of properties that are defined in the datasource    
	 * @return 
	 */
	private Properties getDatasourceProperties(){
		Properties result = new Properties();
		result.put("driverClassName", dbType.getDriverClassName());
		if (port > 1){
			result.put("url", dbType.getConnectionString(server, database, port));
		}
		result.put("username", username);
		result.put("password", password);
		return result;
	}
	

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmDataSource#getHibernatePropertiesBean(eu.etaxonomy.cdm.database.CdmPersistentDataSource.HBM2DDL)
	 */
	public BeanDefinition getHibernatePropertiesBean(HBM2DDL hbm2dll){
		boolean showSql = false;
		boolean formatSql = false;
		Class<? extends CacheProvider> cacheProviderClass = NoCacheProvider.class;
		return getHibernatePropertiesBean(hbm2dll, showSql, formatSql, cacheProviderClass);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.application.ICdmDataSource#getHibernatePropertiesBean(eu.etaxonomy.cdm.database.CdmPersistentDataSource.HBM2DDL, java.lang.Boolean, java.lang.Boolean, java.lang.Class)
	 */
	public BeanDefinition getHibernatePropertiesBean(HBM2DDL hbm2dll, Boolean showSql, Boolean formatSql, Class<? extends CacheProvider> cacheProviderClass){
		//Hibernate default values
		if (hbm2dll == null){
			hbm2dll = this.hbm2dll;
		}
		if (showSql == null){
			showSql = this.showSql;
		}
		if (formatSql == null){
			formatSql = this.formatSql;
		}
		if (cacheProviderClass == null){
			cacheProviderClass = this.cacheProviderClass;
		}
		
		DatabaseTypeEnum dbtype = dbType;
		AbstractBeanDefinition bd = new RootBeanDefinition(PropertiesFactoryBean.class);
		MutablePropertyValues hibernateProps = new MutablePropertyValues();

		Properties props = new Properties();
		props.setProperty("hibernate.hbm2ddl.auto", hbm2dll.toString());
		props.setProperty("hibernate.dialect", dbtype.getHibernateDialect());
		props.setProperty("hibernate.cache.provider_class", cacheProviderClass.getName());
		props.setProperty("hibernate.show_sql", String.valueOf(showSql));
		props.setProperty("hibernate.format_sql", String.valueOf(formatSql));

		hibernateProps.addPropertyValue("properties",props);
		bd.setPropertyValues(hibernateProps);
		return bd;
	}
	

	
	
	
	
}
