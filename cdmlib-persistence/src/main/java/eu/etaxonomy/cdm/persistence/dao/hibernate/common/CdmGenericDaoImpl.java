/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.Reference;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.impl.SessionImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.collection.OneToManyPersister;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.stat.Statistics;
import org.hibernate.type.AnyType;
import org.hibernate.type.BooleanType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.DoubleType;
import org.hibernate.type.EntityType;
import org.hibernate.type.FloatType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.SerializableType;
import org.hibernate.type.SetType;
import org.hibernate.type.StringClobType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.joda.time.contrib.hibernate.PersistentDateTime;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.hibernate.PartialUserType;
import eu.etaxonomy.cdm.hibernate.UUIDUserType;
import eu.etaxonomy.cdm.hibernate.WSDLDefinitionUserType;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.CdmMetaData;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.strategy.match.CacheMatcher;
import eu.etaxonomy.cdm.strategy.match.DefaultMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.FieldMatcher;
import eu.etaxonomy.cdm.strategy.match.IMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.match.MatchException;
import eu.etaxonomy.cdm.strategy.match.MatchMode;
import eu.etaxonomy.cdm.strategy.match.Matching;
import eu.etaxonomy.cdm.strategy.merge.DefaultMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.IMergable;
import eu.etaxonomy.cdm.strategy.merge.IMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.MergeException;

@Repository
public class CdmGenericDaoImpl extends CdmEntityDaoBase<CdmBase> implements ICdmGenericDao{
	private static final Logger logger = Logger.getLogger(CdmGenericDaoImpl.class);

	
	private Set<Class<? extends CdmBase>> allCdmClasses = null;
	private Map<Class<? extends CdmBase>, Set<ReferenceHolder>> referenceMap = new HashMap<Class<? extends CdmBase>, Set<ReferenceHolder>>();
	
	private class ReferenceHolder{
		String propertyName;
		Class<? extends CdmBase> otherClass;
		Class<? extends CdmBase> itemClass;
		Field field;
		public boolean isCollection(){return itemClass != null;};
		public String toString(){return otherClass.getSimpleName() + "." + propertyName ;};
	}

	
	public CdmGenericDaoImpl() {
		super(CdmBase.class);
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao#getCdmBasesByFieldAndClass(java.lang.Class, java.lang.String, eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	
	public List<CdmBase> getCdmBasesByFieldAndClass(Class clazz, String propertyName, CdmBase referencedCdmBase){
		Session session = super.getSession();
		Criteria criteria = session.createCriteria(clazz);
		criteria.add(Restrictions.eq(propertyName, referencedCdmBase));
		return criteria.list();
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao#getCdmBasesByFieldAndClass(java.lang.Class, java.lang.String, eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public List<CdmBase> getCdmBasesWithItemInCollection(Class itemClass, Class clazz, String propertyName, CdmBase item){
		Session session = super.getSession();
		String thisClassStr = itemClass.getSimpleName();
		String otherClassStr = clazz.getSimpleName();
		String queryStr = " SELECT other FROM "+ thisClassStr + " this, " + otherClassStr + " other " + 
			" WHERE this = :referencedObject AND this member of other."+propertyName ;
		Query query = session.createQuery(queryStr).setEntity("referencedObject", item);
		List<CdmBase> result = query.list();
		return result;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao#getAllCdmClasses(boolean)
	 */
	public Set<Class<? extends CdmBase>> getAllCdmClasses(boolean includeAbstractClasses){
		Set<Class<? extends CdmBase>> result = new HashSet<Class<? extends CdmBase>>();
		
		SessionFactory sessionFactory = getSession().getSessionFactory();
		Map<?,?> allClassMetadata = sessionFactory.getAllClassMetadata();
		Collection<?> keys = allClassMetadata.keySet();
		for (Object oKey : keys){
			if (oKey instanceof String){
				String strKey = (String)oKey;
				if (! strKey.endsWith("_AUD")){
					try {
						Class clazz = Class.forName(strKey);
						boolean isAbstractClass = Modifier.isAbstract(clazz.getModifiers());
						if (! isAbstractClass || includeAbstractClasses){
							result.add(clazz);
						}
					} catch (ClassNotFoundException e) {
						logger.warn("Class not found: " + strKey);
					}
				}
			}else{
				logger.warn("key is not of type String: " +  oKey);
			}
		}
		return result;
	}
	
	

	
	public Set<CdmBase> getReferencingObjects(CdmBase referencedCdmBase){
		Set<CdmBase> result = new HashSet<CdmBase>();
		try {
			referencedCdmBase = (CdmBase)HibernateProxyHelper.deproxy(referencedCdmBase);
			Class<? extends CdmBase> referencedClass = referencedCdmBase.getClass();
			
			Set<ReferenceHolder> holderSet = referenceMap.get(referencedClass);
			if (holderSet == null){
				holderSet = makeHolderSet(referencedClass);
				referenceMap.put(referencedClass, holderSet);
			}
			for (ReferenceHolder refHolder: holderSet){
				handleReferenceHolder(referencedCdmBase, result, refHolder);
			}
			return result;	
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
	}

	/**
	 * @param referencedCdmBase
	 * @param result
	 * @param refHolder
	 */
	private void handleReferenceHolder(CdmBase referencedCdmBase,
			Set<CdmBase> result, ReferenceHolder refHolder) {
		boolean isCollection = refHolder.isCollection();
		if (isCollection){
			result.addAll(getCdmBasesWithItemInCollection(refHolder.itemClass, refHolder.otherClass, refHolder.propertyName, referencedCdmBase));
		}else{
			result.addAll(getCdmBasesByFieldAndClass(refHolder.otherClass, refHolder.propertyName, referencedCdmBase));
		}
	}

	
	/**
	 * @param referencedClass
	 * @return
	 * @throws NoSuchFieldException 
	 * @throws ClassNotFoundException 
	 */
	private Set<ReferenceHolder> makeHolderSet(Class referencedClass) throws ClassNotFoundException, NoSuchFieldException {
		Set<ReferenceHolder> result = new HashSet<ReferenceHolder>();
		
		//init
		if (allCdmClasses == null){
			allCdmClasses = getAllCdmClasses(false); //findAllCdmClasses();
		}
		//referencedCdmBase = (CdmBase)HibernateProxyHelper.deproxy(referencedCdmBase);
		SessionFactory sessionFactory = getSession().getSessionFactory();
		
		
		for (Class<? extends CdmBase> cdmClass : allCdmClasses){
			ClassMetadata classMetadata = sessionFactory.getClassMetadata(cdmClass);
			Type[] propertyTypes = classMetadata.getPropertyTypes();
			int propertyNr = 0;
			for (Type propertyType: propertyTypes){
				String propertyName = classMetadata.getPropertyNames()[propertyNr];
				makePropertyType(result, referencedClass, sessionFactory, cdmClass, propertyType, propertyName, false);
				propertyNr++;
			}
			
		}
		return result;
	}

	/**
	 * @param referencedCdmBase
	 * @param result
	 * @param referencedClass
	 * @param sessionFactory
	 * @param cdmClass
	 * @param propertyType
	 * @param propertyName
	 * @throws ClassNotFoundException
	 * @throws NoSuchFieldException
	 */
	private void makePropertyType(
//			CdmBase referencedCdmBase,
			Set<ReferenceHolder> result,
			Class referencedClass,
			SessionFactory sessionFactory, Class<? extends CdmBase> cdmClass,
			Type propertyType, String propertyName, boolean isCollection)
				throws ClassNotFoundException, NoSuchFieldException {
		
		
		if (propertyType.isEntityType()){
			EntityType entityType = (EntityType)propertyType;
			String associatedEntityName = entityType.getAssociatedEntityName();
			Class entityClass = Class.forName(associatedEntityName);
			if (entityClass.isInterface()){
				logger.debug("There is an interface");
			}
			if (entityClass.isAssignableFrom(referencedClass)){
				makeSingleProperty(referencedClass, entityClass, propertyName, cdmClass, result, isCollection);
			}
		}else if (propertyType.isCollectionType()){
			CollectionType collectionType = (CollectionType)propertyType;
			//String role = collectionType.getRole();
			Type elType = collectionType.getElementType((SessionFactoryImpl)sessionFactory);
			makePropertyType(result, referencedClass, sessionFactory, cdmClass, elType, propertyName, true);
		}else if (propertyType.isAnyType()){
			AnyType anyType = (AnyType)propertyType;
			Field field = cdmClass.getDeclaredField(propertyName);
			Class returnType = field.getType();
			if (returnType.isInterface()){
				logger.debug("There is an interface");
			}
			if (returnType.isAssignableFrom(referencedClass)){
				makeSingleProperty(referencedClass, returnType, propertyName, cdmClass, result, isCollection);
			}
		}else if (propertyType.isComponentType()){
			ComponentType componentType = (ComponentType)propertyType;
			Type[] subTypes = componentType.getSubtypes();
//			Field field = cdmClass.getDeclaredField(propertyName);
//			Class returnType = field.getType();
			int propertyNr = 0;
			for (Type subType: subTypes){
				String subPropertyName = componentType.getPropertyNames()[propertyNr];
				if (!isNoDoType(subType)){
					logger.warn("SubType not yet handled: " + subType);
				}
//				handlePropertyType(referencedCdmBase, result, referencedClass, 
//						sessionFactory, cdmClass, subType, subPropertyName, isCollection);
				propertyNr++;
			}
		}else if (isNoDoType(propertyType)){
			//do nothing
		}else{
			logger.warn("propertyType not yet handled: " + propertyType.getName());
		}
		//OLD: 
				//		if (! type.isInterface()){
		//		if (referencedClass.isAssignableFrom(type)|| 
		//				type.isAssignableFrom(referencedClass) && CdmBase.class.isAssignableFrom(type)){
		//			handleSingleClass(referencedClass, type, field, cdmClass, result, referencedCdmBase, false);
		//		}
		//	//interface
		//	}else if (type.isAssignableFrom(referencedClass)){
		//			handleSingleClass(referencedClass, type, field, cdmClass, result, referencedCdmBase, false);

	}
	
	private boolean makeSingleProperty(Class itemClass, Class type, String propertyName, Class cdmClass, Set<ReferenceHolder> result,/*CdmBase item,*/ boolean isCollection){
			String fieldName = StringUtils.rightPad(propertyName, 30);
			String className = StringUtils.rightPad(cdmClass.getSimpleName(), 30);
			String returnTypeName = StringUtils.rightPad(type.getSimpleName(), 30);
			
//			logger.debug(fieldName +   "\t\t" + className + "\t\t" + returnTypeName);
			ReferenceHolder refHolder = new ReferenceHolder();
			refHolder.propertyName = propertyName;
			refHolder.otherClass = cdmClass;
			refHolder.itemClass = (isCollection ? itemClass : null) ;
			result.add(refHolder);
		return true;
	}

	/**
	 * @param propertyType
	 * @return
	 */
	private boolean isNoDoType(Type propertyType) {
		boolean result = false;
		Class[] classes = new Class[]{
				PersistentDateTime.class, 
				WSDLDefinitionUserType.class,
				UUIDUserType.class, 
				PartialUserType.class,
				StringType.class,
				BooleanType.class, 
				IntegerType.class, 
				StringClobType.class,
				LongType.class,
				FloatType.class,
				SerializableType.class,
				DoubleType.class
				};
		Set<String> classNames = new HashSet<String>();
		for (Class clazz: classes){
			classNames.add(clazz.getCanonicalName());
			if (clazz == propertyType.getClass()){
				return true;
			}
		}
		String propertyTypeClassName = propertyType.getName();
		if (classNames.contains(propertyTypeClassName)){
			return true;
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao#getHqlResult(java.lang.String)
	 */
	public List<CdmBase> getHqlResult(String hqlQuery){
		Query query = getSession().createQuery(hqlQuery);
		List<CdmBase> result = query.list();
		return result;
	}
	
	public <T extends CdmBase> void   merge(T cdmBase1, T cdmBase2, IMergeStrategy mergeStrategy) throws MergeException {
		Class<T> clazz = (Class<T>)cdmBase1.getClass();
		SessionImpl session = (SessionImpl) getSession();
		SessionFactory sessionFactory = session.getSessionFactory();
		if (mergeStrategy == null){
			mergeStrategy = DefaultMergeStrategy.NewInstance(cdmBase1.getClass());
		}
		try {
			//test null and types
			testMergeValid(cdmBase1, cdmBase2);
			
			//merge objects
			//externel impl
			//internal impl
			session.flush();
			Set<ICdmBase> deleteSet = new HashSet<ICdmBase>();
			Set<ICdmBase> cloneSet = new HashSet<ICdmBase>();
			if (cdmBase1 instanceof IMergable){
				IMergable mergable1 = (IMergable)cdmBase1;
				IMergable mergable2 = (IMergable)cdmBase2;
				deleteSet = mergeStrategy.invoke(mergable1, mergable2, cloneSet);
				//session.saveOrUpdate(mergable1);

				session.flush();
				//((IMergable)cdmBase1).mergeInto(cdmBase2, DefaultMergeStrategy.NewInstance(cdmBase1.getClass()));
			}else{
				mergeExternal(cdmBase1, cdmBase2, clazz, session);
			}
			
			
			if (cdmBase2.getId() > 0){
				session.saveOrUpdate(cdmBase2);
				//rearrange references to cdmBase2
				reallocateReferences(cdmBase1, cdmBase2, sessionFactory, clazz, cloneSet);
			}
			
			//remove deleted objects 
			
			//session.delete(null, mergable2, true, null);
			session.delete(cdmBase2);
			for (ICdmBase toBeDeleted : deleteSet){
				logger.debug("Delete " + toBeDeleted);
				if (toBeDeleted != cdmBase2){
					session.delete(toBeDeleted);
				}
				
			}
			
			//flush
			session.flush();
			
		} catch (Exception e) {
			throw new MergeException(e);
		} 
	}

	
	/**
	 * @param <T>
	 * @param cdmBase1
	 * @param cdmBase2
	 * @param clazz
	 * @param sessionFactory
	 * @throws MergeException 
	 * @throws ClassNotFoundException
	 * @throws NoSuchFieldException
	 */
	private <T extends CdmBase> void mergeExternal(T cdmBase1, T cdmBase2, Class<T> clazz,
			Session session) throws MergeException {
//		handleAnnotations
		logger.warn("Merge external");
		handleAnnotationsEtc(cdmBase1, cdmBase2, session);
		
		SessionFactoryImpl sessionFactory = (SessionFactoryImpl) session.getSessionFactory();
		
		Map allClassMetadata = sessionFactory.getAllClassMetadata();
		
		//TODO cast
		getCollectionRoles(clazz, sessionFactory);
		
		TaxonNameBase name1 = BotanicalName.NewInstance(null);
		name1.getTaxonBases();
		
		Type propType = sessionFactory.getReferencedPropertyType(BotanicalName.class.getCanonicalName(), "taxonBases");
		Map collMetadata = sessionFactory.getAllCollectionMetadata();
		//roles = sessionFactory.getCollectionRolesByEntityParticipant("eu.etaxonomy.cdm.model.name.BotanicalName");
		CollectionPersister collPersister;
		try {
			collPersister = sessionFactory.getCollectionPersister(TaxonNameBase.class.getCanonicalName()+".annotations");
		} catch (MappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Statistics statistics = sessionFactory.getStatistics();
		logger.debug("");
		ClassMetadata taxonMetaData = sessionFactory.getClassMetadata(Taxon.class);
		String ename = taxonMetaData.getEntityName();
		try {
			Reference ref = sessionFactory.getReference();
			logger.debug("");
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//sessionFactory.get
		ClassMetadata classMetadata = getSession().getSessionFactory().getClassMetadata(clazz);
		Type[] propertyTypes = classMetadata.getPropertyTypes();
		int propertyNr = 0;
		for (Type propertyType: propertyTypes){
			String propertyName = classMetadata.getPropertyNames()[propertyNr];
			logger.debug(propertyName);
			makeMergeProperty(cdmBase1, cdmBase2, propertyType, propertyName, sessionFactory, false);
			propertyNr++;
		}
		Set<String> collectionRoles;
		if (classMetadata instanceof AbstractEntityPersister){
			AbstractEntityPersister persister = (AbstractEntityPersister)classMetadata;
			String rootName = persister.getRootEntityName();
			collectionRoles = sessionFactory.getCollectionRolesByEntityParticipant(rootName);
			for (String collectionRole : collectionRoles){
				CollectionMetadata collMetadata2 = sessionFactory.getCollectionMetadata(collectionRole);
				String role = collMetadata2.getRole();
				Type elType = collMetadata2.getElementType();
				logger.debug(role);
			}
		}
	}

	/**
	 * @param <T>
	 * @param clazz
	 * @param sessionFactory
	 */
	private <T> Set<String> getCollectionRoles(Class<T> clazz,
			SessionFactoryImpl sessionFactory) {
		Set<String> collectionRoles = null;
		ClassMetadata classMetaData = sessionFactory.getClassMetadata(clazz);
		if (classMetaData instanceof AbstractEntityPersister){
			AbstractEntityPersister persister = (AbstractEntityPersister)classMetaData;
			String rootName = persister.getRootEntityName();
			collectionRoles = sessionFactory.getCollectionRolesByEntityParticipant(rootName);
			for (String collectionRole : collectionRoles){
				CollectionMetadata collMetadata = sessionFactory.getCollectionMetadata(collectionRole);
				CollectionPersister collPersister = sessionFactory.getCollectionPersister(collectionRole);
				logger.debug("");
			}
		}else{
			logger.warn("Class metadata is not of type AbstractEntityPersister");
			throw new UnhandledException("Class metadata is not of type AbstractEntityPersister", null);
		}
		return collectionRoles;
	}
	
	
	private <T extends CdmBase> void makeMergeProperty(T cdmBase1, T cdmBase2, Type propertyType, String propertyName, SessionFactoryImpl sessionFactory, boolean isCollection) throws MergeException
			 {
	
		try {
			Class<T> clazz = (Class<T>)cdmBase1.getClass();
			if (isNoDoType(propertyType)){
						//do nothing 
			}else if (propertyType.isEntityType()){
				//Field field = clazz.getField(propertyName);	
				EntityType entityType = (EntityType)propertyType;
				String associatedEntityName = entityType.getAssociatedEntityName();
				Class entityClass = Class.forName(associatedEntityName);
//				 Type refPropType = sessionFactory.getReferencedPropertyType(entityClass.getCanonicalName(), propertyName);
				Set<String> collectionRoles = getCollectionRoles(clazz, sessionFactory);
				for (String collectionRole : collectionRoles){
					CollectionMetadata collMetadata = sessionFactory.getCollectionMetadata(collectionRole);
					String role = collMetadata.getRole();
					logger.debug(role);
					
				}
				
//				if (entityClass.isInterface()){
//					logger.debug("So ein interface");
//				}
//				if (entityClass.isAssignableFrom(clazz)){
//					makeSingleProperty(referencedClass, entityClass, propertyName, cdmClass, result, isCollection);
//				}
			}else if (propertyType.isCollectionType()){
				CollectionType collectionType = (CollectionType)propertyType;
				String role = collectionType.getRole();
				Type elType = collectionType.getElementType((SessionFactoryImpl)sessionFactory);
				String n = collectionType.getAssociatedEntityName(sessionFactory);
				CollectionMetadata collMetadata = sessionFactory.getCollectionMetadata(role);
				if (collMetadata instanceof OneToManyPersister){
					OneToManyPersister oneManyPersister = (OneToManyPersister)collMetadata;
					String className = oneManyPersister.getOwnerEntityName();
					Class<?> myClass = Class.forName(className);
					Field field = myClass.getDeclaredField(propertyName);
					field.setAccessible(true);
					try {
						if (collectionType instanceof SetType){
							Set set2 = (Set)field.get(cdmBase2);
							Set<Object> set1 = (Set<Object>)field.get(cdmBase1);
							for (Object obj2: set2){
								set1.add(obj2);
							}
						}
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				logger.debug("");
				
//			makePropertyType(result, referencedClass, sessionFactory, cdmClass, elType, propertyName, true);
			}else if (propertyType.isAnyType()){
				AnyType anyType = (AnyType)propertyType;
				Field field = clazz.getDeclaredField(propertyName);
				Class returnType = field.getType();
//			if (returnType.isInterface()){
//				logger.debug("So ein interface");
//			}
//			if (returnType.isAssignableFrom(referencedClass)){
//				makeSingleProperty(referencedClass, returnType, propertyName, cdmClass, result, isCollection);
//			}
			}else if (propertyType.isComponentType()){
				ComponentType componentType = (ComponentType)propertyType;
				Type[] subTypes = componentType.getSubtypes();
//		Field field = cdmClass.getDeclaredField(propertyName);
//		Class returnType = field.getType();
				int propertyNr = 0;
				for (Type subType: subTypes){
					String subPropertyName = componentType.getPropertyNames()[propertyNr];
					if (!isNoDoType(subType)){
						logger.warn("SubType not yet handled: " + subType);
					}
//					handlePropertyType(referencedCdmBase, result, referencedClass, 
//					sessionFactory, cdmClass, subType, subPropertyName, isCollection);
					propertyNr++;
				}
			}else{
				logger.warn("propertyType not yet handled: " + propertyType.getName());
			}
			//OLD: 
					//		if (! type.isInterface()){
			//		if (referencedClass.isAssignableFrom(type)|| 
			//				type.isAssignableFrom(referencedClass) && CdmBase.class.isAssignableFrom(type)){
			//			handleSingleClass(referencedClass, type, field, cdmClass, result, referencedCdmBase, false);
			//		}
			//	//interface
			//	}else if (type.isAssignableFrom(referencedClass)){
			//			handleSingleClass(referencedClass, type, field, cdmClass, result, referencedCdmBase, false);
		} catch (Exception e) {
			throw new MergeException(e);
		}
	}


		
		
	private void reallocateReferences(CdmBase cdmBase1, CdmBase cdmBase2, SessionFactory sessionFactory, Class clazz, Set<ICdmBase> cloneSet){
		try {
			Set<ReferenceHolder> holderSet = referenceMap.get(clazz);
			if (holderSet == null){
				holderSet = makeHolderSet(clazz);
				referenceMap.put(clazz, holderSet);
			}
			for (ReferenceHolder refHolder: holderSet){
				reallocateByHolder(cdmBase1, cdmBase2, refHolder, cloneSet);
			}
			return;	
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @param cdmBase1
	 * @param cdmBase2
	 * @param refHolder
	 * @throws MergeException 
	 */
	private void reallocateByHolder(CdmBase cdmBase1, CdmBase cdmBase2, ReferenceHolder refHolder, Set<ICdmBase> cloneSet) throws MergeException {
		try {
			if (refHolder.isCollection()){
				reallocateCollection(cdmBase1, cdmBase2, refHolder, cloneSet);
			}else{
				reallocateSingleItem(cdmBase1, cdmBase2, refHolder, cloneSet);
			}
		} catch (Exception e) {
			throw new MergeException("Error during reallocation of references to merge object: " + cdmBase2, e);
		}
		
	}

	/**
	 * @param cdmBase1
	 * @param cdmBase2
	 * @param refHolder
	 * @param cloneSet
	 * @throws MergeException 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws InvocationTargetException 
	 */
	private void reallocateCollection(CdmBase cdmBase1, CdmBase cdmBase2,
			ReferenceHolder refHolder, Set<ICdmBase> cloneSet) throws MergeException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		List<CdmBase> list = getCdmBasesWithItemInCollection(refHolder.itemClass, refHolder.otherClass, refHolder.propertyName, cdmBase2);
		for (CdmBase referencingObject : list){
			Field referencingField = getFieldRecursive(refHolder.otherClass, refHolder.propertyName);
			referencingField.setAccessible(true);
			Object collection = referencingField.get(referencingObject);
			if (! (collection instanceof Collection)){
				throw new MergeException ("Reallocation of collections for collection other than set and list not yet implemented");
			}
			Method addMethod = DefaultMergeStrategy.getAddMethod(referencingField, false);
			Method removeMethod = DefaultMergeStrategy.getAddMethod(referencingField, true);
			addMethod.invoke(referencingObject, cdmBase1);
			removeMethod.invoke(referencingObject, cdmBase2);
		}
	}
	
	private Field getFieldRecursive(Class clazz, String propertyName) throws NoSuchFieldException{
		try {
			return clazz.getDeclaredField(propertyName);
		} catch (NoSuchFieldException e) {
			Class superClass = clazz.getSuperclass();
			if (CdmBase.class.isAssignableFrom(superClass)){
				return getFieldRecursive(superClass, propertyName);
			}else{
				throw e;
			}
		}
	}

	/**
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * 
	 */
	private void reallocateSingleItem_Old(CdmBase cdmBase1, CdmBase cdmBase2, ReferenceHolder refHolder) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		List<CdmBase> referencingObjects = getCdmBasesByFieldAndClass(refHolder.otherClass, refHolder.propertyName, cdmBase2);
		for (CdmBase referencingObject : referencingObjects){
			Field referencingField = refHolder.otherClass.getDeclaredField(refHolder.propertyName);
			referencingField.setAccessible(true);
			Object test = referencingField.get(referencingObject);
			assert(test.equals(cdmBase2));
			referencingField.set(referencingObject, cdmBase1);
		}
	}

	private void reallocateSingleItem(CdmBase cdmBase1, CdmBase cdmBase2, ReferenceHolder refHolder, Set<ICdmBase> cloneSet) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		List<CdmBase> referencingObjects = getCdmBasesByFieldAndClass(refHolder.otherClass, refHolder.propertyName, cdmBase2);
		Session session = getSession(); 
		for (CdmBase referencingObject : referencingObjects){
			if (!cloneSet.contains(referencingObject)){
		        String className = refHolder.otherClass.getSimpleName();
	            String propertyName = refHolder.propertyName;
		        String hql = "update " + className + " c set c."+propertyName+" = :newValue where c.id = :id";
		        Query query = session.createQuery(hql);
		        query.setEntity("newValue", cdmBase1);
		        query.setInteger("id",referencingObject.getId());
		        int rowCount = query.executeUpdate();
		        logger.debug("Rows affected: " + rowCount);
		        session.refresh(referencingObject);
	        }
	    }
		session.flush();
	}
	


	/**
	 * @param <T>
	 * @param cdmBase1
	 * @param cdmBase2
	 * @param session
	 */
	private <T> void handleAnnotationsEtc(T cdmBase1, T cdmBase2, Session session) {
		//when handling annotations and other elements linked via @Any an JDBC errors occurs
		//due to the unique column constraint in the association table on the column referencing
		//the annotation.
		//For some reason not delete command is executed for the old collection
		// Hibernate bug ??
		session.flush();  //for debugging
		if (cdmBase1 instanceof AnnotatableEntity){
			AnnotatableEntity annotatableEntity1 = (AnnotatableEntity)cdmBase1;
			AnnotatableEntity annotatableEntity2 = (AnnotatableEntity)cdmBase2;
			//annotations
			List<Annotation> removeListAnnotation = new ArrayList<Annotation>();
			for (Annotation annotation : annotatableEntity2.getAnnotations()){
				Annotation clone = null;
				try {
					clone = annotation.clone(annotatableEntity1);
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				annotatableEntity1.addAnnotation(clone);
				removeListAnnotation.add(annotation);
			}
			for (Annotation annotation : removeListAnnotation){
				annotatableEntity2.removeAnnotation(annotation);
				getSession().delete(annotation);
			}
			//marker
			List<Marker> removeListMarker = new ArrayList<Marker>();
			for (Marker marker : annotatableEntity2.getMarkers()){
				Marker clone = null;
				try {
					clone = marker.clone(annotatableEntity1);
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				annotatableEntity1.addMarker(clone);
				removeListMarker.add(marker);
			}
			for (Marker marker : removeListMarker){
				annotatableEntity2.removeMarker(marker);
				getSession().delete(marker);
			}
		}
		if (cdmBase1 instanceof IdentifiableEntity){
			IdentifiableEntity identifiableEntity1 = (IdentifiableEntity)cdmBase1;
			IdentifiableEntity identifiableEntity2 = (IdentifiableEntity)cdmBase2;
			//annotations
			List<Extension> removeListExtension = new ArrayList<Extension>();
			for (Extension changeObject : (Set<Extension>)identifiableEntity2.getExtensions()){
				try {
					Extension clone = changeObject.clone(identifiableEntity1);
					identifiableEntity1.addExtension(clone);
					removeListExtension.add(changeObject);
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
				
			}
			for (Extension removeObject : removeListExtension){
				identifiableEntity2.removeExtension(removeObject);
				getSession().delete(removeObject);
			}
		}
		
		session.saveOrUpdate(cdmBase1);
		session.saveOrUpdate(cdmBase2);
		session.flush();
	}
	
	private <T extends CdmBase> void testMergeValid(T cdmBase1, T cdmBase2)throws IllegalArgumentException, NullPointerException{
		if (cdmBase1 == null || cdmBase2 == null){
			throw new NullPointerException("Merge arguments must not be (null)");
		}
		cdmBase1 = (T)HibernateProxyHelper.deproxy(cdmBase1);
		cdmBase2 = (T)HibernateProxyHelper.deproxy(cdmBase2);
		
		if (cdmBase1.getClass() != cdmBase2.getClass()){
			throw new IllegalArgumentException("Merge arguments must be of same type");
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao#test()
	 */
	public void test() {
		SessionFactoryImpl factory = (SessionFactoryImpl)getSession().getSessionFactory();
		Type propType = factory.getReferencedPropertyType(BotanicalName.class.getCanonicalName(), "titleCache");
		Map collMetadata = factory.getAllCollectionMetadata();
		Object roles = factory.getCollectionRolesByEntityParticipant("eu.etaxonomy.cdm.model.name.BotanicalName");
		CollectionPersister collPersister;
		try {
			collPersister = factory.getCollectionPersister(TaxonNameBase.class.getCanonicalName()+".annotations");
		} catch (MappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Statistics statistics = factory.getStatistics();
		Map allClassMetadata = factory.getAllClassMetadata();
		logger.debug("");
		
	}

	public <T extends CdmBase> T find(Class<T> clazz, int id){
		Session session;
		session =  getSession();
		//session = getSession().getSessionFactory().getCurrentSession();
		return (T)session.get(clazz, id);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao#findMatching(eu.etaxonomy.cdm.strategy.match.IMatchable, eu.etaxonomy.cdm.strategy.match.IMatchStrategy)
	 */
	public <T extends IMatchable> List<T> findMatching(T objectToMatch,
			IMatchStrategy matchStrategy) throws MatchException {
		try {
			List<T> result = new ArrayList<T>();
			if(objectToMatch == null){
				return result;
			}
			if (matchStrategy == null){
				matchStrategy = DefaultMatchStrategy.NewInstance(objectToMatch.getClass());
			}
			result.addAll(findMatchingNullSafe(objectToMatch, matchStrategy));
			return result;
		} catch (IllegalArgumentException e) {
			throw new MatchException(e);
		} catch (IllegalAccessException e) {
			throw new MatchException(e);
		}
	}
	
	public <T extends IMatchable> List<T> findMatchingNullSafe(T objectToMatch,	IMatchStrategy matchStrategy) throws IllegalArgumentException, IllegalAccessException, MatchException {
		List<T> result = new ArrayList<T>();
		Session session = getSession();
		Class matchClass = objectToMatch.getClass();
		ClassMetadata classMetaData = session.getSessionFactory().getClassMetadata(matchClass.getCanonicalName());
		Criteria criteria = session.createCriteria(matchClass);
		boolean noMatch = makeCriteria(objectToMatch, matchStrategy, classMetaData, criteria);
		logger.debug(criteria);
		//session.flush();
		if (noMatch == false){
			List<T> matchCandidates = criteria.list();
			matchCandidates.remove(objectToMatch);
			for (T matchCandidate : matchCandidates ){
				if (matchStrategy.invoke(objectToMatch, matchCandidate)){
					result.add(matchCandidate);
				}else{
					logger.warn("Match candidate did not match: " + matchCandidate);
				}
			}
		}
		return result;
	}

	/**
	 * @param <T>
	 * @param objectToMatch
	 * @param matchStrategy
	 * @param classMetaData
	 * @param criteria
	 * @return
	 * @throws IllegalAccessException
	 * @throws MatchException
	 */
	private <T> boolean makeCriteria(T objectToMatch,
			IMatchStrategy matchStrategy, ClassMetadata classMetaData,
			Criteria criteria) throws IllegalAccessException, MatchException {
		Matching matching = matchStrategy.getMatching();
		boolean noMatch = false;
		Map<String, List<MatchMode>> replaceMatchers = new HashMap<String, List<MatchMode>>();
		for (CacheMatcher cacheMatcher: matching.getCacheMatchers()){
			boolean cacheProtected = (Boolean)cacheMatcher.getProtectedField(matching).get(objectToMatch);
			if (cacheProtected == true){
				String cacheValue = (String)cacheMatcher.getField().get(objectToMatch);
				if (CdmUtils.isEmpty(cacheValue)){
					return true;  //no match
				}else{
					criteria.add(Restrictions.eq(cacheMatcher.getPropertyName(), cacheValue));
					criteria.add(Restrictions.eq(cacheMatcher.getProtectedPropertyName(), cacheProtected));
					
					List<DoubleResult<String, MatchMode>> replacementModes = cacheMatcher.getReplaceMatchModes(matching);
					for (DoubleResult<String, MatchMode> replacementMode: replacementModes ){
						String propertyName = replacementMode.getFirstResult();
						List<MatchMode> replaceMatcherList = replaceMatchers.get(propertyName);
						if (replaceMatcherList == null){
							replaceMatcherList = new ArrayList<MatchMode>();
							replaceMatchers.put(propertyName, replaceMatcherList);
						}
						replaceMatcherList.add(replacementMode.getSecondResult());
					}

				}
			}
		}
		for (FieldMatcher fieldMatcher : matching.getFieldMatchers(false)){
			String propertyName = fieldMatcher.getPropertyName();
			Type propertyType = classMetaData.getPropertyType(propertyName);
			Object value = fieldMatcher.getField().get(objectToMatch);
			List<MatchMode> matchModes= new ArrayList<MatchMode>();
			matchModes.add(fieldMatcher.getMatchMode());
			if (replaceMatchers.get(propertyName) != null){
				matchModes.addAll(replaceMatchers.get(propertyName));
			}
			
			boolean isIgnore = false;
			for (MatchMode matchMode : matchModes){
				isIgnore |= matchMode.isIgnore(value);
			}
			if (! isIgnore ){
				if (propertyType.isComponentType()){
					matchComponentType(criteria, fieldMatcher, propertyName, value, matchModes);
				}else{
					noMatch = matchNonComponentType(criteria, fieldMatcher, propertyName, value, matchModes, propertyType);
				}
			}
			if (noMatch){
				return noMatch;
			}
		}
		return noMatch;
	}

	/**
	 * @param criteria
	 * @param fieldMatcher
	 * @param propertyName
	 * @param value
	 * @param matchMode
	 * @throws MatchException
	 * @throws IllegalAccessException
	 */
	private void matchComponentType(Criteria criteria,
			FieldMatcher fieldMatcher, String propertyName, Object value,
			List<MatchMode> matchModes) throws MatchException, IllegalAccessException {
		if (value == null){
			boolean requiresSecondNull = requiresSecondNull(matchModes, value);
			if (requiresSecondNull){
				criteria.add(Restrictions.isNull(propertyName));
			}else{
				//TODO 
				logger.warn("Component type not yet implemented for (null) value: " + propertyName);
				throw new MatchException("Component type not yet fully implemented for (null) value. Property: " + propertyName);
			}
		}else{
			Class<?> componentClass = fieldMatcher.getField().getType();
			Map<String, Field> fields = CdmUtils.getAllFields(componentClass, Object.class, false, false, true, false);
			for (String fieldName : fields.keySet()){
				String restrictionPath = propertyName +"."+fieldName;
				Object componentValue = fields.get(fieldName).get(value);
				//TODO diffentiate matchMode
				createCriterion(criteria, restrictionPath, componentValue, matchModes);
			}
		}
	}

	private boolean matchNonComponentType(Criteria criteria,
			FieldMatcher fieldMatcher, String propertyName, Object value,
			List<MatchMode> matchModes, Type propertyType) throws HibernateException, DataAccessException, MatchException, IllegalAccessException{
		boolean noMatch = false;
		if (isRequired(matchModes) && value == null){
			noMatch = true;
			return noMatch;
		}else if (requiresSecondNull(matchModes,value)){
			criteria.add(Restrictions.isNull(propertyName));
		}else{
			if (isMatch(matchModes)){
				if (propertyType.isCollectionType()){
					//TODO collection not yet handled for match	
				}else{
					int joinType = CriteriaSpecification.INNER_JOIN;
					if (! requiresSecondValue(matchModes,value)){
						joinType = CriteriaSpecification.LEFT_JOIN;
					}
					Criteria matchCriteria = criteria.createCriteria(propertyName, joinType).add(Restrictions.isNotNull("id"));
					Class matchClass = value.getClass();
					if (IMatchable.class.isAssignableFrom(matchClass)){
						IMatchStrategy valueMatchStrategy = DefaultMatchStrategy.NewInstance((Class<IMatchable>)matchClass);
						ClassMetadata valueClassMetaData = getSession().getSessionFactory().getClassMetadata(matchClass.getCanonicalName());;
						noMatch = makeCriteria(value, valueMatchStrategy, valueClassMetaData, matchCriteria); 
					}else{
						logger.error("Class to match (" + matchClass + ") is not of type IMatchable");
						throw new MatchException("Class to match (" + matchClass + ") is not of type IMatchable");
					}
				}
			}else if (isEqual(matchModes)){
				createCriterion(criteria, propertyName, value, matchModes);
			}else {
				logger.warn("Unhandled match mode: " + matchModes + ", value: " + (value==null?"null":value));
			}
		}
		return noMatch;
	}
	
	/**
	 * @param criteria
	 * @param propertyName
	 * @param value
	 * @param matchMode
	 * @throws MatchException
	 */
	private void createCriterion(Criteria criteria, String propertyName,
			Object value, List<MatchMode> matchModes) throws MatchException {
		Criterion finalRestriction = null;
		Criterion equalRestriction = Restrictions.eq(propertyName, value);
		Criterion nullRestriction = Restrictions.isNull(propertyName);
		if (this.requiresSecondValue(matchModes, value)){
			finalRestriction = equalRestriction;
		}else if (requiresSecondNull(matchModes, value) ){
			finalRestriction = nullRestriction;
		}else{
			finalRestriction = Restrictions.or(equalRestriction, nullRestriction);
		}
		//return finalRestriction;
		criteria.add(finalRestriction);
	}
	
	/**
	 * @param matchModes
	 * @param value
	 * @return
	 * @throws MatchException 
	 */
	private boolean requiresSecondNull(List<MatchMode> matchModes, Object value) throws MatchException {
		boolean result = true;
		for (MatchMode matchMode: matchModes){
			result &= matchMode.requiresSecondNull(value);
		}
		return result;
	}
	
	/**
	 * @param matchModes
	 * @param value
	 * @return
	 * @throws MatchException 
	 */
	private boolean requiresSecondValue(List<MatchMode> matchModes, Object value) throws MatchException {
		boolean result = true;
		for (MatchMode matchMode: matchModes){
			result &= matchMode.requiresSecondValue(value);
		}
		return result;
	}
	
	/**
	 * @param matchModes
	 * @param value
	 * @return
	 * @throws MatchException 
	 */
	private boolean isRequired(List<MatchMode> matchModes) throws MatchException {
		boolean result = true;
		for (MatchMode matchMode: matchModes){
			result &= matchMode.isRequired();
		}
		return result;
	}
	
	/**
	 * Returns true if at least one match mode is of typ MATCH_XXX
	 * @param matchModes
	 * @param value
	 * @return
	 * @throws MatchException 
	 */
	private boolean isMatch(List<MatchMode> matchModes) throws MatchException {
		boolean result = false;
		for (MatchMode matchMode: matchModes){
			result |= matchMode.isMatch();
		}
		return result;
	}

	/**
	 * Returns true if at least one match mode is of typ EQUAL_XXX
	 * @param matchModes
	 * @param value
	 * @return
	 * @throws MatchException 
	 */
	private boolean isEqual(List<MatchMode> matchModes) throws MatchException {
		boolean result = false;
		for (MatchMode matchMode: matchModes){
			result |= matchMode.isEqual();
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao#saveMetaData(eu.etaxonomy.cdm.model.common.CdmMetaData)
	 */
	public void saveMetaData(CdmMetaData cdmMetaData) {
		getSession().saveOrUpdate(cdmMetaData);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao#getMetaData()
	 */
	public List<CdmMetaData> getMetaData() {
		Session session = getSession();
		Criteria crit = session.createCriteria(CdmMetaData.class);
		List<CdmMetaData> results = crit.list();
		return results;
	}
}


