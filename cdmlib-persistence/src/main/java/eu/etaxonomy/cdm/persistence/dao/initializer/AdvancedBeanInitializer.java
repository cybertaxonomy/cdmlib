/**
 *
 */
package eu.etaxonomy.cdm.persistence.dao.initializer;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.collection.internal.AbstractPersistentCollection;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.factory.annotation.Autowired;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.persistence.dao.hibernate.HibernateBeanInitializer;

/**
 * For now this is a test if we can improve performance for bean initializing
 * @author a.mueller
 * @date 2013-10-25
 *
 */
public class AdvancedBeanInitializer extends HibernateBeanInitializer {

        public static final Logger logger = Logger.getLogger(AdvancedBeanInitializer.class);

        @Autowired
        ICdmGenericDao genericDao;

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

            //autoinitialize
            for (Object bean : beanList){
                autoinitializeBean(bean);
            }

            if(propertyPaths == null){
                return beanList;
            }


            //new
             BeanInitNode rootPath = BeanInitNode.createInitTree(propertyPaths);
            if (logger.isTraceEnabled()){logger.trace(rootPath.toStringTree());}


            if(logger.isDebugEnabled()){ logger.debug(">> starting to initialize beanlist ; class(e.g.):" + beanList.iterator().next().getClass().getSimpleName());}
            rootPath.addBeans(beanList);
            initializeNodeRecursive(rootPath);


            //old - keep for safety (this may help to initialize those beans that are not yet correctly initialized by the AdvancedBeanInitializer
            if(logger.isTraceEnabled()){logger.trace("Start old initalizer ... ");};
            for (Object bean :beanList){
                Collections.sort(propertyPaths);
                for(String propPath : propertyPaths){
//		            initializePropertyPath(bean, propPath);
                }
            }

            if(logger.isDebugEnabled()){ logger.debug("   Completed initialization of beanlist "); }
            return beanList;

        }


        //new
        private void initializeNodeRecursive(BeanInitNode rootPath) {
            initializeNode(rootPath);
            for (BeanInitNode childPath : rootPath.getChildrenList()){
                initializeNodeRecursive(childPath);
            }
            rootPath.resetBeans();
        }

        /**
         * Initializes the given single <code>propPath</code> String.
         *
         * @param bean
         * @param propPath
         */
        private void initializeNode(BeanInitNode node) {
            if(logger.isDebugEnabled()){logger.debug(" processing " + node.toString());}
            if (node.isRoot()){
                return;
            }else if (node.isWildcard()){
                initializeNodeWildcard(node);
            } else {
                initializeNodeNoWildcard(node);
            }
        }

        // if propPath only contains a wildcard (* or $)
        // => do a batch initialization of *toOne or *toMany relations
        private void initializeNodeWildcard(BeanInitNode node) {
//			boolean initToMany = node.isToManyWildcard();
            Map<Class<?>, Set<Object>> parentBeans = node.getParentBeans();
            for (Class<?> clazz : parentBeans.keySet()){
                //new
                for (Object bean : parentBeans.get(clazz)){

                    if(Collection.class.isAssignableFrom(bean.getClass())){
//				        old: initializeAllEntries((Collection<?>)bean, true, initToMany);  //TODO is this a possible case at all??
                        throw new RuntimeException("Collection no longer expected in 'initializeNodeWildcard()'. Therefore an exception is thrown.");
                    } else if(Map.class.isAssignableFrom(bean.getClass())) {
//				        old: initializeAllEntries(((Map<?,?>)bean).values(), true, initToMany);  ////TODO is this a possible case at all??
                        throw new RuntimeException("Map no longer expected in 'initializeNodeWildcard()'. Therefore an exception is thrown.");
                    } else{
                        prepareBeanWildcardForBulkLoad(node, bean);
                    }
                }
                //end new

//		    	initializeNodeWildcardOld(initToMany, beans, clazz);  //if switched on move bulkLoadLazies up
            }

            //
            bulkLoadLazies(node);
        }

        /**
         * @param initToMany
         * @param beans
         * @param clazz
         */
        private void initializeNodeWildcardOld(boolean initToMany,
                Map<Class<?>, Set<Object>> beans, Class<?> clazz) {
            for (Object bean : beans.get(clazz)){

                if(Collection.class.isAssignableFrom(bean.getClass())){
                    initializeAllEntries((Collection<?>)bean, true, initToMany);
                } else if(Map.class.isAssignableFrom(bean.getClass())) {
                    initializeAllEntries(((Map<?,?>)bean).values(), true, initToMany);
                } else{
                    initializeBean(bean, true, initToMany);
                }
            }
        }

        private void prepareBeanWildcardForBulkLoad(BeanInitNode node, Object bean){

            if(logger.isTraceEnabled()){logger.trace(">> prepare bulk wildcard initialization of a bean of type " + bean.getClass().getSimpleName()); }
            Set<Class<?>> restrictions = new HashSet<Class<?>>();
            restrictions.add(CdmBase.class);
            if(node.isToManyWildcard()){
                restrictions.add(Collection.class);
            }
            Set<PropertyDescriptor> props = getProperties(bean, restrictions);
            for(PropertyDescriptor propertyDescriptor : props){
                try {
                    String property = propertyDescriptor.getName();

//                  invokeInitialization(bean, propertyDescriptor);
                    Object propertyValue = PropertyUtils.getProperty( bean, property);

                    preparePropertyValueForBulkLoadOrStore(node, bean, property,  propertyValue );

                } catch (IllegalAccessException e) {
                    logger.error("Illegal access on property " + propertyDescriptor.getName());
                } catch (InvocationTargetException e) {
                    logger.info("Cannot invoke property " + propertyDescriptor.getName() + " not found");
                } catch (NoSuchMethodException e) {
                    logger.info("Property " + propertyDescriptor.getName() + " not found");
                }
            }
            if(logger.isTraceEnabled()){logger.trace(" completed bulk wildcard initialization of a bean");}
        }



        // propPath contains either a single field or a nested path
        // split next path token off and keep the remaining as nestedPath
        private void initializeNodeNoWildcard(BeanInitNode node) {

            String property = node.getPath();
            int pos;

            // is the property indexed?
            Integer index = null;
            if((pos = property.indexOf('[')) > 0){
                String indexString = property.substring(pos + 1, property.indexOf(']'));
                index = Integer.valueOf(indexString);
                property = property.substring(0, pos);
            }

            //Class targetClass = HibernateProxyHelper.getClassWithoutInitializingProxy(bean); // used for debugging

            for (Class<?> parentClazz : node.getParentBeans().keySet()){
                if (logger.isTraceEnabled()){logger.trace(" invoke initialization on "+ node.toString()+ " beans of class " + parentClazz.getSimpleName() + " ... ");}

                Set<Object> parentBeans = node.getParentBeans().get(parentClazz);

                if (index != null){
                    logger.warn("Property path index not yet implemented for 'new'");
                }
                //new
                for (Object parentBean : parentBeans){
                    try{
                        Object propertyValue = PropertyUtils.getProperty(parentBean, property);
                        preparePropertyValueForBulkLoadOrStore(node, parentBean, property, propertyValue);
                    } catch (IllegalAccessException e) {
                        logger.error("Illegal access on property " + property);
                    } catch (InvocationTargetException e) {
                        logger.error("Cannot invoke property " + property + " not found");
                    } catch (NoSuchMethodException e) {
                        if (logger.isDebugEnabled()){logger.debug("Property " + property + " not found for class " + parentClazz);}
                    }
                }

                //end new

//			    	initializeNodeNoWildcardOld(node, property, index, parentBeans);  //move bulkLoadLazies up again, if uncomment this line
            }
            bulkLoadLazies(node);

        }

        /**
         * @param node
         * @param property
         * @param index
         * @param parentBeans
         * @throws IllegalAccessException
         * @throws InvocationTargetException
         * @throws NoSuchMethodException
         */
        private void initializeNodeNoWildcardOld(BeanInitNode node,
                String property, Integer index, Set<Object> parentBeans)
                throws IllegalAccessException, InvocationTargetException,
                NoSuchMethodException {
            for (Object bean : parentBeans){

                PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(bean, property);
                if (logger.isTraceEnabled()){logger.trace("   unwrap " + node.toStringNoWildcard() + " ... ");}
                // [1] initialize the bean named by property
                Object unwrappedPropertyBean = invokeInitialization(bean, propertyDescriptor);
                if (logger.isTraceEnabled()){logger.trace("   unwrap " + node.toStringNoWildcard() + " - DONE ");}


                // [2]
                // handle property
                if(unwrappedPropertyBean != null ){
                    initializeNodeSinglePropertyOld(node, property, index, bean, unwrappedPropertyBean);
                }
            }
        }

        /**
         * @param node
         * @param propertyValue
         * @param parentBean
         * @param param
         */
        private void preparePropertyValueForBulkLoadOrStore(BeanInitNode node, Object parentBean, String param, Object propertyValue) {
            BeanInitNode sibling = node.getSibling(param);

            if (propertyValue instanceof AbstractPersistentCollection ){
                //collections
                if (!node.hasWildcardToManySibling()){  //if wildcard sibling exists the lazies are already prepared there
                    AbstractPersistentCollection collection = (AbstractPersistentCollection)propertyValue;
                    if (collection.wasInitialized()){
                        storeInitializedCollection(collection, node, param);
                    }else{
//						Class<?> parentClass = parentBean.getClass();
//						int parentId = ((CdmBase)parentBean).getId();
                        if (sibling != null){
                            sibling.putLazyCollection(collection);
                        }else{
                            node.putLazyCollection(collection);
                        }
                    }
                }
            }else{
                //singles
                if (!node.hasWildcardToOneSibling()){  //if wildcard exists the lazies are already prepared there
                    if (! Hibernate.isInitialized(propertyValue)){
                        if (propertyValue instanceof HibernateProxy){
                            Serializable id = ((HibernateProxy)propertyValue).getHibernateLazyInitializer().getIdentifier();
                            Class<?> persistedClass = ((HibernateProxy)propertyValue).getHibernateLazyInitializer().getPersistentClass();
                            if (sibling != null){
                                sibling.putLazyBean(persistedClass, id);
                            }else{
                                node.putLazyBean(persistedClass, id);
                            }

                        }else{
                            logger.warn("Lazy value is not of type HibernateProxy. This is not yet handled.");
                        }
                    }else if (propertyValue == null){
                        // do nothing
                    }else{
                        if (propertyValue instanceof HibernateProxy){  //TODO remove hibernate dependency
                            propertyValue = initializeInstance(propertyValue);
                        }
                        autoinitializeBean(propertyValue);
                        node.addBean(propertyValue);
                    }
                }
            }
        }

        private void autoinitializeBean(Object bean) {
            invokePropertyAutoInitializers(bean);
        }

		private void storeInitializedCollection(AbstractPersistentCollection persistedCollection,
				BeanInitNode node, String param) {
			Collection<?> collection;

			if (persistedCollection  instanceof Collection) {
				collection = (Collection<?>) persistedCollection;
			}else if (persistedCollection instanceof Map) {
				collection = ((Map<?,?>)persistedCollection).values();
			}else{
				throw new RuntimeException ("Non Map and non Collection cas not handled in storeInitializedCollection()");
			}
			for (Object value : collection){
				preparePropertyValueForBulkLoadOrStore(node, null, param, value);
			}
		}

		private void bulkLoadLazies(BeanInitNode node) {

			if (logger.isTraceEnabled()){logger.trace("bulk load " +  node);}

			//beans
			for (Class<?> clazz : node.getLazyBeans().keySet()){
				Set<Serializable> idSet = node.getLazyBeans().get(clazz);
				if (idSet != null && ! idSet.isEmpty()){

					if (logger.isTraceEnabled()){logger.trace("bulk load beans of class " +  clazz.getSimpleName());}
					//TODO use entity name
					String hql = " SELECT c FROM %s as c %s WHERE c.id IN (:idSet) ";
					hql = String.format(hql, clazz.getSimpleName(), addAutoinitFetchLoading(clazz, "c"));
					if (logger.isTraceEnabled()){logger.trace(hql);}
					Query query = genericDao.getHqlQuery(hql);
					query.setParameterList("idSet", idSet);
					List<Object> list = query.list();

					if (logger.isTraceEnabled()){logger.trace("initialize bulk loaded beans of class " +  clazz.getSimpleName());}
					for (Object object : list){
						if (object instanceof HibernateProxy){  //TODO remove hibernate dependency
							object = initializeInstance(object);
						}
						autoinitializeBean(object);
						node.addBean(object);
					}
					if (logger.isTraceEnabled()){logger.trace("bulk load - DONE");}
				}
			}
			node.resetLazyBeans();

			//collections
			for (Class<?> ownerClazz : node.getLazyCollections().keySet()){
				Map<String, Set<Serializable>> lazyParams = node.getLazyCollections().get(ownerClazz);
				for (String param : lazyParams.keySet()){
					Set<Serializable> idSet = lazyParams.get(param);
					if (idSet != null && ! idSet.isEmpty()){
						if (logger.isTraceEnabled()){logger.trace("bulk load " + node + " collections ; ownerClass=" +  ownerClazz.getSimpleName() + " ; param = " + param);}

						//TODO use entity name ??
						//get from repository
						List<Object[]> list;
						String hql = "SELECT oc " +
								" FROM %s as oc JOIN FETCH oc.%s as col %s " +
								" WHERE oc.id IN (:idSet) ";

//						String hql = "SELECT oc.%s " +
//								" FROM %s as oc WHERE oc.id IN (:idSet) ";
						hql = String.format(hql, ownerClazz.getSimpleName(), param,
								"" /*addAutoinitFetchLoading(clazz, "col")*/);

						try {
							if (logger.isTraceEnabled()){logger.trace(hql);}
							Query query = genericDao.getHqlQuery(hql);
							query.setParameterList("idSet", idSet);
							list = query.list();
						} catch (HibernateException e) {
							e.printStackTrace();
							throw e;
						}

						//getTarget and add to child node
						if (logger.isTraceEnabled()){logger.trace("initialize bulk loaded " + node + " collections - DONE");}
						for (Object parentBean : list){
							Object newBean;
                            try {
							    newBean = PropertyUtils.getProperty(
							            parentBean,
							            mapFieldToPropertyName(param, parentBean.getClass().getSimpleName())
							          );

    							if (newBean == null){
    								System.out.println("Collection is null");
    							}else {
    							}
    							if (newBean instanceof HibernateProxy){
    								newBean = initializeInstance(newBean);
    							}
    							autoinitializeBean(newBean);
    							node.addBean(newBean);
                            } catch (Exception e) {
                                // IGNORE

                                logger.error("error while geting collection property", e);
                            }
						}
						if (logger.isTraceEnabled()){logger.trace("bulk load " + node + " collections - DONE");}
					}
				}
			}
			for (AbstractPersistentCollection collection : node.getUninitializedCollections()){
				if (! collection.wasInitialized()){  //should not happen anymore
					collection.forceInitialization();
				}
			}

			node.resetLazyCollections();

			if (logger.isDebugEnabled()){logger.debug("bulk load " +  node + " - DONE ");}

		}


        private String addAutoinitFetchLoading(Class<?> clazz, String beanAlias) {
            Set<AutoPropertyInitializer<CdmBase>> inits = getAutoInitializers(clazz);
            String result = "";
            for (AutoPropertyInitializer<CdmBase> init: inits){
                result +=init.hibernateFetchJoin(clazz, beanAlias);
            }
            return result;
        }

        private Set<AutoPropertyInitializer<CdmBase>> getAutoInitializers(Class<?> clazz) {
            Set<AutoPropertyInitializer<CdmBase>> result = new HashSet<AutoPropertyInitializer<CdmBase>>();
            for(Class<? extends CdmBase> superClass : getBeanAutoInitializers().keySet()){
                if(superClass.isAssignableFrom(clazz)){
                    result.add(getBeanAutoInitializers().get(superClass));
                }
            }
            return result;
        }

        /**
         * Rename bean attributes to hibernate (field) attribute, due to bean inconsistencies
         * #3841
         * @param param
         * @param ownerClass
         * @return
         */
        private String mapFieldToPropertyName(String param, String ownerClass) {
            if (ownerClass.contains("Description") && param.equals("descriptionElements")){
                //DescriptionBase.elements -> descriptionElements
                return "elements";
            }else{
                return param;
            }
        }

        /**
         * @param node
         * @param property
         * @param index
         * @param bean
         * @param unwrappedPropertyBean
         */
        private void initializeNodeSinglePropertyOld(BeanInitNode node, String property,
                Integer index, Object bean, Object unwrappedPropertyBean) {
            Collection<?> collection = null;
            if(Map.class.isAssignableFrom(unwrappedPropertyBean.getClass())) {
                collection = ((Map<?,?>)unwrappedPropertyBean).values();
            }else if (Collection.class.isAssignableFrom(unwrappedPropertyBean.getClass())) {
                collection =  (Collection<?>) unwrappedPropertyBean;
            }
            if (collection != null){
                //collection or map
                if (logger.isTraceEnabled()){logger.trace(" initialize collection for " + node.toStringNoWildcard() + " ... ");}
                int i = 0;
                for (Object entrybean : collection) {
                    if(index == null){
                        node.addBean(entrybean);
                    } else if(index.equals(i)){
                        node.addBean(entrybean);
                        break;
                    }
                    i++;
                }
                if (logger.isTraceEnabled()){logger.trace(" initialize collection for " + node.toString() + " - DONE ");}

            }else {
                // nested bean
                node.addBean(unwrappedPropertyBean);
                setProperty(bean, property, unwrappedPropertyBean);
            }
        }

}
