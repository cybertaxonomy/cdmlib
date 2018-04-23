/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.persistence.hibernate;

import java.io.Serializable;

import org.hibernate.cache.internal.NoCachingRegionFactory;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.envers.boot.internal.EnversIntegrator;

/**
 * @author a.mueller
 * @since 03.08.2017
 *
 */
public class HibernateConfiguration implements Serializable{

    private static final long serialVersionUID = -894395918847594447L;

    public final static String SHOW_SQL = "hibernate.show_sql";

    public final static String FORMAT_SQL = "hibernate.format_sql";

    public final static String REGISTER_SEARCH = "hibernate.search.autoregister_listeners";

    public final static String REGISTER_ENVERS = EnversIntegrator.AUTO_REGISTER;

    public final static String CACHE_PROVIDER_CLASS = "hibernate.cache.region.factory_class";


    public static final boolean SHOW_SQL_DEFAULT = false;
    public static final boolean FORMAT_SQL_DEFAULT = false;
    public static final boolean REGISTER_SEARCH_DEFAULT = false;
    public static final boolean REGISTER_ENVERS_DEFAULT = true;

    public static final Class<? extends RegionFactory> CACHE_PROVIDER_DEFAULT = NoCachingRegionFactory.class;

    /**
     * @return
     */
    public static HibernateConfiguration NewDefaultInstance() {
        HibernateConfiguration result = NewInstance(SHOW_SQL_DEFAULT, FORMAT_SQL_DEFAULT,
                REGISTER_SEARCH_DEFAULT, REGISTER_ENVERS_DEFAULT, CACHE_PROVIDER_DEFAULT);
        return result;
    }

    public static HibernateConfiguration NewInstance(Boolean showSql, Boolean formatSql,
            Boolean registerSearch, Boolean registerEnvers,
            Class<? extends RegionFactory> cacheProviderClass) {
        HibernateConfiguration result = new HibernateConfiguration();
        result.setShowSql(showSql);
        result.setFormatSql(formatSql);
        result.setRegisterSearch(registerSearch);
        result.setRegisterEnvers(registerEnvers);
        result.setCacheProviderClass(cacheProviderClass);
        return result;
    }


    private Boolean showSql = null;

    private Boolean formatSql = null;

    private Boolean registerSearch = null;

    private Boolean registerEnvers = null;

    private Class<? extends RegionFactory> cacheProviderClass = null;

// *********** GETTERS /SETTERS ****************************/

    public Boolean getShowSql() {
        return showSql;
    }
    public boolean getShowSql(boolean defaultValue) {
        return showSql != null ? showSql : defaultValue;
    }
    public void setShowSql(Boolean showSql) {
        this.showSql = showSql;
    }

    public Boolean getFormatSql() {
        return formatSql;
    }
    public boolean getFormatSql(boolean defaultValue) {
        return formatSql != null ? formatSql : defaultValue;
    }
    public void setFormatSql(Boolean formatSql) {
        this.formatSql = formatSql;
    }

    public Boolean getRegisterSearch() {
        return registerSearch;
    }
    public boolean getRegisterSearch(boolean defaultValue) {
        return registerSearch != null ? registerSearch : defaultValue;
    }
    public void setRegisterSearch(Boolean registerSearch) {
        this.registerSearch = registerSearch;
    }

    public Boolean getRegisterEnvers() {
        return registerEnvers;
    }
    public boolean getRegisterEnvers(boolean defaultValue) {
        return registerEnvers != null ? registerEnvers : defaultValue;
    }
    public void setRegisterEnvers(Boolean registerEnvers) {
        this.registerEnvers = registerEnvers;
    }

    public Class<? extends RegionFactory> getCacheProviderClass() {
        return cacheProviderClass;
    }
    public Class<? extends RegionFactory> getCacheProviderClass(Class<? extends RegionFactory> defaultValue) {
        return cacheProviderClass != null ? cacheProviderClass : defaultValue;
    }
    public void setCacheProviderClass(Class<? extends RegionFactory> cacheProviderClass) {
        this.cacheProviderClass = cacheProviderClass;
    }

// ******************** toString *******************************/
    @Override
    public String toString() {
        return "HibernateConfiguration [showSql=" + showSql + ", formatSql=" + formatSql + ", registerSearch="
                + registerSearch + ", registerEnvers=" + registerEnvers + ", cacheProviderClass=" + cacheProviderClass
                + "]";
    }


}
