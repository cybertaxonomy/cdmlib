/**
 * 
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

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.HibernateBeanInitializer;

/**
 * For now this is to test if we can improve performance for bean initializing
 * @author a.mueller
 * @date 2013-10-25
 *
 */
public class AdvancedBeanInitializer extends HibernateBeanInitializer {

	   public static final Logger logger = Logger.getLogger(AdvancedBeanInitializer.class);
	
	    @Override
	    public void load(Object bean) {
	        initializeBean(bean, true, false);
	    }

	    @Override
	    public void loadFully(Object bean) {
	        initializeBean(bean, true, true);
	    }


//	    @Override
//	    public void initializeBean(Object bean, boolean cdmEntities, boolean collections){
//
//	        if(logger.isDebugEnabled()){
//	            logger.debug(">> starting initializeBean() of " + bean + " ;class:" + bean.getClass().getSimpleName());
//	        }
//	        Set<Class> restrictions = new HashSet<Class>();
//	        if(cdmEntities){
//	            restrictions.add(CdmBase.class);
//	        }
//	        if(collections){
//	            restrictions.add(Collections.class);
//	        }
//	        Set<PropertyDescriptor> props = getProperties(bean, restrictions);
//	        for(PropertyDescriptor propertyDescriptor : props){
//	            try {
//
//	                invokeInitialization(bean, propertyDescriptor);
//
//	            } catch (IllegalAccessException e) {
//	                logger.error("Illegal access on property " + propertyDescriptor.getName());
//	            } catch (InvocationTargetException e) {
//	                logger.info("Cannot invoke property " + propertyDescriptor.getName() + " not found");
//	            } catch (NoSuchMethodException e) {
//	                logger.info("Property " + propertyDescriptor.getName() + " not found");
//	            }
//	        }
//	        if(logger.isDebugEnabled()){
//	            logger.debug("  completed initializeBean() of " + bean);
//	        }
//	    }


	    //TODO optimize algorithm ..
	    @Override
	    public void initialize(Object bean, List<String> propertyPaths) {

	        invokePropertyAutoInitializers(bean);

	        if(propertyPaths == null){
	            return;
	        }

	        if(logger.isDebugEnabled()){
	            logger.debug(">> starting to initialize " + bean + " ;class:" + bean.getClass().getSimpleName());
	        }
	        
	        //new
	        BeanInitNode rootInitializer = BeanInitNode.createInitTree(propertyPaths);
	        System.out.println(rootInitializer.toStringTree());
	        initializeBean(bean, rootInitializer);
	        
	        
//	        //old
//	        if(logger.isDebugEnabled()){logger.debug("Start old initalizer ... ");};
//	        Collections.sort(propertyPaths);
//	        for(String propPath : propertyPaths){
//	            initializePropertyPath(bean, propPath);
//	        }
	        if(logger.isDebugEnabled()){
	            logger.debug("   Completed initialization of " + bean);
	        }

	    }
	    

		//new
	    
	    public void initializeBean(Object bean, BeanInitNode rootInitializer) {
			initializePropertyPath(bean, rootInitializer);
			for (BeanInitNode child : rootInitializer.getChildrenList()){
				initializeBean(bean, child);
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
	    void initializePropertyPath(Object bean, BeanInitNode node) {
	        if(logger.isDebugEnabled()){logger.debug("processing " + node.toString());}
	        if (StringUtils.isBlank(node.getPath())){
	        	return;
	        }

	        if (node.isWildcard()){
		        initializeWildcardPropertyPath(bean, node);
	        } else {
	            initializeNoWildcardPropertyPath(bean, node);
	        }
	    }

		// if propPath only contains a wildcard (* or $)
        // => do a batch initialization of *toOne or *toMany relations
        private void initializeWildcardPropertyPath(Object bean, BeanInitNode node) {
			boolean initToMany = node.getPath().equals(LOAD_2ONE_2MANY_WILDCARD);
		    if(Collection.class.isAssignableFrom(bean.getClass())){
		        initializeAllEntries((Collection)bean, true, initToMany);
		    } else if(Map.class.isAssignableFrom(bean.getClass())) {
		        initializeAllEntries(((Map)bean).values(), true, initToMany);
		    } else{
		        initializeBean(bean, true, initToMany);
		    }
		}

    	// propPath contains either a single field or a nested path
		// split next path token off and keep the remaining as nestedPath
        private void initializeNoWildcardPropertyPath(Object bean, BeanInitNode node) {
			
        	String property = node.getPath();
			int pos;

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
			    if (logger.isDebugEnabled()){logger.debug("invokeInitialization "+node+" ... ");}
			    Object unwrappedPropertyBean = invokeInitialization(bean, propertyDescriptor);
			    if (logger.isDebugEnabled()){logger.debug("invokeInitialization "+node+" - DONE ");}
				//TODO continue
//			    node.addBean(unwrappedPropertyBean);
			    
			    // [2.b]
			    // recurse into nested properties
			    if(unwrappedPropertyBean != null ){
			    	for (BeanInitNode childNode : node.getChildrenList()){
			    		Collection<?> collection = null;
			    		if(Map.class.isAssignableFrom(unwrappedPropertyBean.getClass())) {
			    			collection = ((Map<?,?>)unwrappedPropertyBean).values();
			    		}else if (Collection.class.isAssignableFrom(unwrappedPropertyBean.getClass())) {
			                collection =  (Collection<?>) unwrappedPropertyBean;	
			    		}
			    		if (collection != null){
			                //collection or map
			    			if (logger.isDebugEnabled()){logger.debug(" initialize collection for " + childNode.toString() + " ... ");}
			 	            int i = 0;
			    			for (Object entrybean : collection) {
			                    if(index == null){
			                        initializePropertyPath(entrybean, childNode);
			                    } else if(index.equals(i)){
			                        initializePropertyPath(entrybean, childNode);
			                        break;
			                    }
			                    i++;
			                }
			    			if (logger.isDebugEnabled()){logger.debug(" initialize collection for " + childNode.toString() + " - DONE ");}
			 	            
			            }else {
			                // nested bean
			                initializePropertyPath(unwrappedPropertyBean, childNode);
			                setProperty(bean, property, unwrappedPropertyBean);
			            }
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

	    //TODO check if needed in advanced
	    private Object invokeInitialization(Object bean, PropertyDescriptor propertyDescriptor) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

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
	     * @param collection of which all entities are to be initialized
	     * @param cdmEntities initialize all *toOne relations to cdm entities
	     * @param collections initialize all *toMany relations
	     */
	    private void initializeAllEntries(Collection collection, boolean cdmEntities, boolean collections) {
	        for(Object bean : collection){
	            initializeBean(bean, cdmEntities, collections);
	        }
	    }

}
