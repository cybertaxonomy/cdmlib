/**
 * 
 */
package eu.etaxonomy.cdm.persistence.dao.initializer;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.persistence.dao.hibernate.HibernateBeanInitializer;

/**
 * For now this is a test if we can improve performance for bean initializing
 * @author a.mueller
 * @date 2013-10-25
 *
 */
public class AdvancedBeanInitializer extends HibernateBeanInitializer {

	   public static final Logger logger = Logger.getLogger(AdvancedBeanInitializer.class);

	    @Override
	    public void initialize(Object bean, List<String> propertyPaths) {
	        List<Object> beanList = new ArrayList<Object>(1);
	        beanList.add(bean);
	        initializeAll(beanList, propertyPaths);
	    }
	    
	    //TODO optimize algorithm ..
	    @Override
	    public <C extends Collection<?>> C initializeAll(C beanList,  List<String> propertyPaths) {

	    	if (beanList == null || beanList.isEmpty()){
	    		return beanList;
	    	}
	    	
	    	//TODO new required?
//	        invokePropertyAutoInitializers(bean);

	        if(propertyPaths == null){  //TODO if AutoInitializer is not requiredfor top level bean, this can be merged with previous "if"
	            return beanList;
	        }

	        
	        //new
 	        BeanInitNode rootPath = BeanInitNode.createInitTree(propertyPaths);
	        System.out.println(rootPath.toStringTree());
	        

	        if(logger.isDebugEnabled()){
	            logger.debug(">> starting to initialize beanlist ; class(e.g.):" + beanList.iterator().next().getClass().getSimpleName());
	        }
	        rootPath.addBeans(beanList);
	        initializeBean(rootPath);
	        
	        
//	        //old
//	        if(logger.isDebugEnabled()){logger.debug("Start old initalizer ... ");};
//	        Collections.sort(propertyPaths);
//	        for(String propPath : propertyPaths){
//	            initializePropertyPath(bean, propPath);
//	        }
	        if(logger.isDebugEnabled()){
	            logger.debug("   Completed initialization of beanlist ");
	        }
	        return beanList;

	    }
	    

		//new
	    private void initializeBean(BeanInitNode rootPath) {
			initializePropertyPath(rootPath);
			for (BeanInitNode childPath : rootPath.getChildrenList()){
				initializeBean(childPath);
			}
			rootPath.resetBeans();
		}
	    
	    /**
	     * Initializes the given single <code>propPath</code> String.
	     *
	     * @param bean
	     * @param propPath
	     */
	    private void initializePropertyPath(BeanInitNode node) {
	        if(logger.isDebugEnabled()){logger.debug("processing " + node.toString());}
	        if (StringUtils.isBlank(node.getPath())){
	        	return;
	        }

	        if (node.isWildcard()){
		        initializeWildcardPropertyPath(node);
	        } else {
	            initializeNoWildcardPropertyPath(node);
	        }
	    }

		// if propPath only contains a wildcard (* or $)
        // => do a batch initialization of *toOne or *toMany relations
        private void initializeWildcardPropertyPath(BeanInitNode node) {
			boolean initToMany = node.getPath().equals(LOAD_2ONE_2MANY_WILDCARD);
		    for (Class<?> clazz : node.getParentBeans().keySet()){
				for (Object bean : node.getParentBeans().get(clazz)){
				
					if(Collection.class.isAssignableFrom(bean.getClass())){
				        initializeAllEntries((Collection<?>)bean, true, initToMany);
				    } else if(Map.class.isAssignableFrom(bean.getClass())) {
				        initializeAllEntries(((Map<?,?>)bean).values(), true, initToMany);
				    } else{
				        initializeBean(bean, true, initToMany);
				    }
			    }
		    }
		}

    	// propPath contains either a single field or a nested path
		// split next path token off and keep the remaining as nestedPath
        private void initializeNoWildcardPropertyPath(BeanInitNode node) {
			
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

			    // [1] initialize the bean named by property
			    for (Class<?> clazz : node.getParentBeans().keySet()){
					for (Object bean : node.getParentBeans().get(clazz)){
		
					    PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(bean, property);
					    if (logger.isDebugEnabled()){logger.debug("invokeInitialization "+node+" ... ");}
					    Object unwrappedPropertyBean = invokeInitialization(bean, propertyDescriptor);
					    if (logger.isDebugEnabled()){logger.debug("invokeInitialization "+node+" - DONE ");}
		
	
					    // [2]
					    // recurse into nested properties
					    if(unwrappedPropertyBean != null ){
	//				    	for (BeanInitNode childNode : node.getChildrenList()){
					    		Collection<?> collection = null;
					    		if(Map.class.isAssignableFrom(unwrappedPropertyBean.getClass())) {
					    			collection = ((Map<?,?>)unwrappedPropertyBean).values();
					    		}else if (Collection.class.isAssignableFrom(unwrappedPropertyBean.getClass())) {
					                collection =  (Collection<?>) unwrappedPropertyBean;	
					    		}
					    		if (collection != null){
					                //collection or map
					    			if (logger.isDebugEnabled()){logger.debug(" initialize collection for " + node.toString() + " ... ");}
					 	            int i = 0;
					    			for (Object entrybean : collection) {
					                    if(index == null){
					    				    node.addBean(entrybean);
	//				                    	initializePropertyPath(entrybean, childNode);
					                    } else if(index.equals(i)){
					                    	node.addBean(entrybean);
	//				                    	initializePropertyPath(entrybean, childNode);
					                        break;
					                    }
					                    i++;
					                }
					    			if (logger.isDebugEnabled()){logger.debug(" initialize collection for " + node.toString() + " - DONE ");}
					 	            
					            }else {
					                // nested bean
					            	node.addBean(unwrappedPropertyBean);
	//				            	initializePropertyPath(unwrappedPropertyBean, childNode);
					                setProperty(bean, property, unwrappedPropertyBean);
					            }
					    	}
						}
//				    }
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
