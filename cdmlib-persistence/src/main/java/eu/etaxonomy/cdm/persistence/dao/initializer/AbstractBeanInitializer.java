/**
 * Copyright (C) 2009 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.persistence.dao.initializer;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Transient;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.IMethodCache;

/**
 * @author a.kohlbecker
 \* @since 26.03.2009
 *
 */
public abstract class AbstractBeanInitializer implements IBeanInitializer{

    public static final Logger logger = Logger.getLogger(AbstractBeanInitializer.class);

    @Autowired
    private IMethodCache methodCache;

    private Map<Class<? extends CdmBase>, AutoPropertyInitializer<CdmBase>> beanAutoInitializers = null;

    /**
     * @param beanAutoInitializers the beanAutoInitializers to set
     */
    public void setBeanAutoInitializers(Map<Class<? extends CdmBase>, AutoPropertyInitializer<CdmBase>> beanAutoInitializers) {
        this.beanAutoInitializers = beanAutoInitializers;
    }

    /**
     * @return the beanAutoInitializers
     */
    public Map<Class<? extends CdmBase>, AutoPropertyInitializer<CdmBase>> getBeanAutoInitializers() {
        return beanAutoInitializers;
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.BeanInitializer#initializeInstance(java.lang.Object)
     */
    @Override
    public abstract Object initializeInstance(Object proxy);

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.BeanInitializer#load(eu.etaxonomy.cdm.model.common.CdmBase)
     */
    @Override
    public void load(Object bean) {
        initializeBean(bean, true, false);
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.BeanInitializer#loadFully(eu.etaxonomy.cdm.model.common.CdmBase)
     */
    @Override
    public void loadFully(Object bean) {
        initializeBean(bean, true, true);
    }

    /**
     * Initializes all *toOne relations of the given bean and all *toMany
     * relations, depending on the state of the boolean parameters
     * <code>cdmEntities</code> and <code>collections</code>
     *
     * @param bean
     *            the bean to initialize
     * @param cdmEntities
     *            initialize all *toOne relations to cdm entities
     * @param collections
     *            initialize all *toMany relations
     */
    public void initializeBean(Object bean, boolean cdmEntities, boolean collections){

        if(logger.isDebugEnabled()){logger.debug(">> starting wildcard initializeBean() of " + bean + " ;class:" + bean.getClass().getSimpleName()); }
        Set<Class<?>> restrictions = new HashSet<Class<?>>();
        if(cdmEntities){
            restrictions.add(CdmBase.class);
        }
        if(collections){
            restrictions.add(Collection.class);
        }
        Set<PropertyDescriptor> props = getProperties(bean, restrictions);
        for(PropertyDescriptor propertyDescriptor : props){
            try {

                invokeInitialization(bean, propertyDescriptor);

            } catch (IllegalAccessException e) {
                logger.error("Illegal access on property " + propertyDescriptor.getName());
            } catch (InvocationTargetException e) {
                logger.info("Cannot invoke property " + propertyDescriptor.getName() + " not found");
            } catch (NoSuchMethodException e) {
                logger.info("Property " + propertyDescriptor.getName() + " not found");
            }
        }
        if(logger.isDebugEnabled()){
            logger.debug("  completed initializeBean() of " + bean);
        }
    }

    /* (non-Javadoc)
     * @see eu.etaxonomy.cdm.persistence.dao.BeanInitializer#initializeProperties(java.lang.Object, java.util.List)
     */
    //TODO optimize algorithm ..
    @Override
    public void initialize(Object bean, List<String> propertyPaths) {

        invokePropertyAutoInitializers(bean);

        if(propertyPaths == null){
            return;
        }

        Collections.sort(propertyPaths);
        //long startTime1 = System.nanoTime();
        if(logger.isDebugEnabled()){
            logger.debug(">> starting to initialize " + bean + " ;class:" + bean.getClass().getSimpleName());
        }
        for(String propPath : propertyPaths){
            initializePropertyPath(bean, propPath);
        }
        //long estimatedTime1 = System.nanoTime() - startTime1;
        //System.err.println(".");
        //long startTime2 = System.nanoTime();
        //for(String propPath : propertyPaths){
        //	initializePropertyPath(bean, propPath);
        //}
        //long estimatedTime2 = System.nanoTime() - startTime2;
        //System.err.println("first pas: "+estimatedTime1+" ns; second pas: "+estimatedTime2+ " ns");
        if(logger.isDebugEnabled()){
            logger.debug("   Completed initialization of " + bean);
        }

    }

    @Override
    public <C extends Collection<?>> C initializeAll(C beanList,  List<String> propertyPaths) {
        if(propertyPaths != null){
            for(Object bean : beanList){
                initialize(bean, propertyPaths);
            }
        }
        return beanList;
    }

    /**
     * Initializes the given single <code>propPath</code> String.
     *
     * @param bean
     * @param propPath
     */
    //changed form private to protected  (AM)
    protected void initializePropertyPath(Object bean, String propPath) {
        if(logger.isDebugEnabled()){
            logger.debug("processing " + propPath);
        }


        // [1]
        // if propPath only contains a wildcard (* or $)
        // => do a batch initialization of *toOne or *toMany relations
        if(propPath.equals(LOAD_2ONE_WILDCARD)){
            if(Collection.class.isAssignableFrom(bean.getClass())){
                initializeAllEntries((Collection)bean, true, false);
            } else if(Map.class.isAssignableFrom(bean.getClass())) {
                initializeAllEntries(((Map)bean).values(), true, false);
            } else{
                initializeBean(bean, true, false);
            }
        } else if(propPath.equals(LOAD_2ONE_2MANY_WILDCARD)){
            if(Collection.class.isAssignableFrom(bean.getClass())){
                initializeAllEntries((Collection)bean, true, true);
            } else if(Map.class.isAssignableFrom(bean.getClass())) {
                initializeAllEntries(((Map)bean).values(), true, true);
            } else {
                initializeBean(bean, true, true);
            }
        } else {
            // [2]
            // propPath contains either a single field or a nested path

            // split next path token off and keep the remaining as nestedPath
            String property;
            String nestedPath = null;
            int pos;
            if((pos = propPath.indexOf('.')) > 0){
                nestedPath = propPath.substring(pos + 1);
                property = propPath.substring(0, pos);
            } else {
                property = propPath;
            }

            // is the property indexed?
            Integer index = null;
            if((pos = property.indexOf('[')) > 0){
                String indexString = property.substring(pos + 1, property.indexOf(']'));
                index = Integer.valueOf(indexString);
                property = property.substring(0, pos);
            }

            try {
                //Class targetClass = HibernateProxyHelper.getClassWithoutInitializingProxy(bean); // used for debugging

                // [2.a] initialize the bean named by property

                PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(bean, property);
                Object unwrappedPropertyBean = invokeInitialization(bean, propertyDescriptor);

                // [2.b]
                // recurse into nested properties
                if(unwrappedPropertyBean != null && nestedPath != null){
                    if (Collection.class.isAssignableFrom(unwrappedPropertyBean.getClass())) {
                        // nested collection
                        int i = 0;
                        for (Object entrybean : (Collection<?>) unwrappedPropertyBean) {
                            if(index == null){
                                initializePropertyPath(entrybean, nestedPath);
                            } else if(index.equals(i)){
                                initializePropertyPath(entrybean, nestedPath);
                                break;
                            }
                            i++;
                        }
                    } else if(Map.class.isAssignableFrom(unwrappedPropertyBean.getClass())) {
                        // nested map
                        int i = 0;
                        for (Object entrybean : ((Map) unwrappedPropertyBean).values()) {
                            if(index == null){
                                initializePropertyPath(entrybean, nestedPath);
                            } else if(index.equals(i)){
                                initializePropertyPath(entrybean, nestedPath);
                                break;
                            }
                            i++;
                        }
                    }else {
                        // nested bean
                        initializePropertyPath(unwrappedPropertyBean, nestedPath);
                        setProperty(bean, property, unwrappedPropertyBean);
                    }
                }

            } catch (IllegalAccessException e) {
                logger.error("Illegal access on property " + property);
            } catch (InvocationTargetException e) {
                logger.error("Cannot invoke property " + property + " not found");
            } catch (NoSuchMethodException e) {
                logger.info("Property " + property + " not found");
            }
        }
    }

    /**
     * Initializes the property of the given bean and returns the bean which is returned be that property.
     *
     * @param bean
     * @param propertyDescriptor
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    protected Object invokeInitialization(Object bean, PropertyDescriptor propertyDescriptor) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        if(propertyDescriptor == null || bean == null){
            return null;
        }

        // (1)
        // initialialization of the bean
        //
        Object propertyProxy = PropertyUtils.getProperty( bean, propertyDescriptor.getName());
        Object propertyBean = initializeInstance(propertyProxy);

        if(propertyBean != null){
            // (2)
            // auto initialialization of sub properties
            //
            if(CdmBase.class.isAssignableFrom(propertyBean.getClass())){

                // initialization of a single bean
                CdmBase cdmBaseBean = (CdmBase)propertyBean;
                invokePropertyAutoInitializers(cdmBaseBean);

            } else if(Collection.class.isAssignableFrom(propertyBean.getClass()) ||
                    Map.class.isAssignableFrom(propertyBean.getClass()) ) {

                // it is a collection or map
                Method readMethod = propertyDescriptor.getReadMethod();
                Type genericReturnType = readMethod.getGenericReturnType();

                if(genericReturnType instanceof ParameterizedType){
                    ParameterizedType type = (ParameterizedType) genericReturnType;
                    Type[] typeArguments = type.getActualTypeArguments();

                    if(typeArguments.length > 0
                            && typeArguments[0] instanceof Class<?>
                            && CdmBase.class.isAssignableFrom((Class<?>) typeArguments[0])){

                        if(Collection.class.isAssignableFrom((Class<?>) type.getRawType())){
                            for(CdmBase entry : ((Collection<CdmBase>)propertyBean)){
                                invokePropertyAutoInitializers(entry);
                            }
                        }
                    }

                }
            }
        }

        return propertyBean;
    }

    /**
     * @param beanClass
     * @param bean
     * @return
     */
    protected final void invokePropertyAutoInitializers(Object bean) {

        if(beanAutoInitializers == null || bean == null){
            return;
        }
        if(!CdmBase.class.isAssignableFrom(bean.getClass())){
            return;
        }
        CdmBase cdmBaseBean = (CdmBase)bean;
        for(Class<? extends CdmBase> superClass : beanAutoInitializers.keySet()){
            if(superClass.isAssignableFrom(bean.getClass())){
                beanAutoInitializers.get(superClass).initialize(cdmBaseBean);
            }
        }
    }

    protected void setProperty(Object object, String property, Object value){
        Method method = methodCache.getMethod(object.getClass(), "set" + StringUtils.capitalize(property), value.getClass());
        if(method != null){
            try {
                method.invoke(object, value);
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * @param collection of which all entities are to be initialized
     * @param cdmEntities initialize all *toOne relations to cdm entities
     * @param collections initialize all *toMany relations
     */
    protected void initializeAllEntries(Collection collection, boolean cdmEntities, boolean collections) {
        for(Object bean : collection){
            initializeBean(bean, cdmEntities, collections);
        }
    }

    /**
     * Return all public bean properties exclusive those whose return type
     * match any class defined in the parameter <code>typeRestrictions</code>
     * or which are transient properties.
     *
     *
     * @param bean
     * @param typeRestrictions
     * @return
     */
    public static Set<PropertyDescriptor> getProperties(Object bean, Set<Class<?>> typeRestrictions) {

        Set<PropertyDescriptor> properties = new HashSet<PropertyDescriptor>();
        PropertyDescriptor[] props = PropertyUtils.getPropertyDescriptors(bean);

        for (PropertyDescriptor prop : props) {
            //String propName = prop[i].getName();

            // only read methods & skip transient getters
            if( prop.getReadMethod() != null ){
                  try{
                     Class<Transient> transientClass = (Class<Transient>)Class.forName( "javax.persistence.Transient" );
                     if( prop.getReadMethod().getAnnotation( transientClass ) != null ){
                        continue;
                     }
                  }catch( ClassNotFoundException cnfe ){
                     // ignore
                  }
                  if(typeRestrictions != null){
                      for(Class<?> restrictedType : typeRestrictions){
                          if(restrictedType.isAssignableFrom(prop.getPropertyType())){
                              properties.add(prop);
                          }
                      }
                  } else {
                      properties.add(prop);
                  }
            }
        }
        return properties;
    }

}
