/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.database;

import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.cache.internal.NoCachingRegionFactory;
import org.hibernate.cache.spi.RegionFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.persistence.hibernate.HibernateConfiguration;

/**
 * @author a.mueller
 *
 */
public class CdmDataSource extends CdmDataSourceBase {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(CdmDataSource.class);

	private DatabaseTypeEnum dbType;
	private String database;
	private int port = -1;
	private String username;
	private String password;

	private String filePath;
	private H2Mode mode;

	private boolean isLazy = true;
	private String initMethodName = null;
	private String destroyMethodName = null;
	private DbSchemaValidation hbm2dll = DbSchemaValidation.VALIDATE;
	private boolean showSql = false;
	private boolean formatSql = false;
	private boolean registerSearchListener = false;
	private boolean registerAuditing = true;
	private Class<? extends RegionFactory> cacheProviderClass = NoCachingRegionFactory.class;

	public static CdmDataSource NewInstance(DatabaseTypeEnum dbType, String server, String database, String username, String password){
		return new CdmDataSource(dbType, server, database, -1, username, password, null, null);
	}

	public static CdmDataSource NewInstance(DatabaseTypeEnum dbType, String server, String database, int port, String username, String password){
		return new CdmDataSource(dbType, server, database, port, username, password, null, null);
	}


	static public CdmDataSource  NewMySqlInstance(String server, String database, String username, String password ){
		return new CdmDataSource(DatabaseTypeEnum.MySQL, server, database, -1, username, password, null, null);
	}

	static public CdmDataSource  NewMySqlInstance(String server, String database, int port, String username, String password){
		return new CdmDataSource(DatabaseTypeEnum.MySQL, server, database, port, username, password, null, null);
	}

	static public CdmDataSource  NewPostgreSQLInstance(String server, String database, int port, String username, String password){
		return new CdmDataSource(DatabaseTypeEnum.PostgreSQL, server, database, port, username, password, null, null);
	}

	static public CdmDataSource  NewSqlServer2005Instance(String server, String database, int port, String username, String password){
		return new CdmDataSource(DatabaseTypeEnum.SqlServer2005, server, database, port, username, password, null, null);
	}


	/** in work
	 * @param code TODO*/
	static public CdmDataSource  NewH2EmbeddedInstance(String database, String username, String password){
		return NewH2EmbeddedInstance(database, username, password, null);
	}

	/** in work
	 * @param code TODO*/
	static public CdmDataSource  NewH2EmbeddedInstance(String database, String username, String password, String filePath){
		//FIXME in work
		int port = -1;
		H2Mode mode = H2Mode.EMBEDDED;
		CdmDataSource dataSource = new CdmDataSource(DatabaseTypeEnum.H2, null, database, port, username, password, filePath, mode);
		return dataSource;
	}

	/** in work */
	static public CdmDataSource  NewH2InMemoryInstance(){
		//FIXME in work
		int port = -1;
		H2Mode mode = H2Mode.IN_MEMORY;
		String username = "sa";
		String password = "";
		CdmDataSource dataSource = new CdmDataSource(DatabaseTypeEnum.H2, null, null, port, username, password, null, mode);
		return dataSource;
	}


	public static CdmDataSource NewInstance(ICdmDataSource dataSource) {
		return new CdmDataSource(dataSource.getDatabaseType(),
				dataSource.getServer(),
				dataSource.getDatabase(),
				dataSource.getPort(),
				dataSource.getUsername(),
				dataSource.getPassword(),
				dataSource.getFilePath(),
				dataSource.getMode());
	}
	/**
	 * @param server
	 * @param database
	 * @param port
	 */
	protected CdmDataSource(DatabaseTypeEnum dbType, String server, String database, int port, String username, String password, String filePath, H2Mode mode) {
		super();
		this.dbType = dbType;
		this.server = server;
		this.database = database;
		this.port = port;
		this.username = username;
		this.password = password;
		this.initMethodName = dbType.getInitMethod();
		this.destroyMethodName = dbType.getDestroyMethod();
		this.filePath = filePath;
		this.mode = mode;
	}

	@Override
	public String getName() {
		return database;
	}

	@Override
	public String getServer() {
		return server;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public BeanDefinition getDatasourceBean(){
		AbstractBeanDefinition bd = new RootBeanDefinition(dbType.getDataSourceClass());
		//attributes
		bd.setLazyInit(isLazy);
		if (! CdmUtils.Nz(initMethodName).trim().equals("") ){
			bd.setInitMethodName(initMethodName);
		}
		if (! CdmUtils.Nz(destroyMethodName).trim().equals("") ){
			bd.setInitMethodName(destroyMethodName);
		}

		//properties
		MutablePropertyValues props = new MutablePropertyValues();
		Properties persistentProperties = getDatasourceProperties();
		Enumeration<Object> keys = persistentProperties.keys();
		while (keys.hasMoreElements()){
			String key = (String)keys.nextElement();
			props.addPropertyValue(key, persistentProperties.getProperty(key));
//			Properties a = Environment.getProperties();
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
		String connectionString = dbType.getConnectionString(this);
		result.put("url", connectionString);
		result.put("username", username);
		result.put("password", password);
		return result;
	}


	@Override
	public BeanDefinition getHibernatePropertiesBean(DbSchemaValidation hbm2dll){
        HibernateConfiguration hibernateConfig = HibernateConfiguration.NewDefaultInstance();
		return getHibernatePropertiesBean(hbm2dll, hibernateConfig);
	}

    @Override
    @Deprecated
    public BeanDefinition getHibernatePropertiesBean(DbSchemaValidation hbm2dll, Boolean showSql, Boolean formatSql,
            Boolean registerSearchListener, Class<? extends RegionFactory> cacheProviderClass){
        HibernateConfiguration hibernateConfig = HibernateConfiguration.NewInstance(showSql, formatSql,
                registerSearchListener, null, cacheProviderClass);
        return getHibernatePropertiesBean(hbm2dll, hibernateConfig);
    }

    @Override
	public BeanDefinition getHibernatePropertiesBean(DbSchemaValidation hbm2dll,
	        HibernateConfiguration hibernateConfig){
        if (hibernateConfig == null){
            hibernateConfig = new HibernateConfiguration();  //empty
        }

        boolean showSql = hibernateConfig.getShowSql(this.showSql);
        boolean formatSql = hibernateConfig.getFormatSql(this.formatSql);
        boolean registerAuditing = hibernateConfig.getRegisterEnvers(this.registerAuditing);
        boolean registerSearchListener = hibernateConfig.getRegisterSearch(this.registerSearchListener);
        Class<? extends RegionFactory> cacheProviderClass = hibernateConfig.getCacheProviderClass(this.cacheProviderClass);

		//Hibernate default values
		if (hbm2dll == null){
			hbm2dll = this.hbm2dll;
		}

		AbstractBeanDefinition bd = makeHibernatePropertiesBean(dbType, hbm2dll, showSql, formatSql, registerAuditing,
                registerSearchListener, cacheProviderClass);
		return bd;
	}

	public String getInitMethodName() {
		return initMethodName;
	}

	public void setInitMethodName(String initMethodName) {
		this.initMethodName = initMethodName;
	}

	public String getDestroyMethodName() {
		return destroyMethodName;
	}

	public void setDestroyMethodName(String destroyMethodName) {
		this.destroyMethodName = destroyMethodName;
	}

	@Override
	public String getDatabase() {
		return database;
	}

	@Override
	public void setDatabase(String database) {
		this.database = database;
	}

	@Override
	public DatabaseTypeEnum getDatabaseType() {
		return dbType;
	}

	@Override
	public String getFilePath() {
		return filePath;
	}

	@Override
	public H2Mode getMode() {
		return mode;
	}

	@Override
	public void setMode(H2Mode h2Mode) {
		this.mode = h2Mode;

	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public void setPassword(String password) {
		this.password = password;

	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public void setUsername(String username) {
		this.username = username;

	}

//********************* TO STRING() **********************/

	@Override
	public String toString() {
		if (StringUtils.isBlank(this.database)){
			return super.toString();
		}else{
			String result = "DataSource<" + dbType.getConnectionString(this).replace(CdmUtils.Nz(password), "") + ">";
			return result;
		}
	}

}

