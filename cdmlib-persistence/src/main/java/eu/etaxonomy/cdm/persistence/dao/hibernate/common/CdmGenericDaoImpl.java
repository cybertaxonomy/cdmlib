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
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.metadata.ClassMetadata;
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
import org.hibernate.type.StringClobType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.joda.time.contrib.hibernate.PersistentDateTime;
import org.springframework.stereotype.Repository;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.PartialUserType;
import eu.etaxonomy.cdm.model.common.UUIDUserType;
import eu.etaxonomy.cdm.model.common.WSDLDefinitionUserType;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;

@Repository
public class CdmGenericDaoImpl extends CdmEntityDaoBase<CdmBase> implements ICdmGenericDao{
	private static final Logger logger = Logger.getLogger(CdmGenericDaoImpl.class);

	
	private Set<Class<? extends CdmBase>> allCdmClasses = null;
	private Map<Class<? extends CdmBase>, Set<ReferenceHolder>> referenceMap = new HashMap<Class<? extends CdmBase>, Set<ReferenceHolder>>();
	
	private class ReferenceHolder{
		String propertyName;
		Class<? extends CdmBase> otherClass;
		Class<? extends CdmBase> itemClass;
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
		List result = query.list();
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
		boolean isCollection = (refHolder.itemClass != null);
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
				System.out.println("So ein interface");
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
				System.out.println("So ein interface");
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
			
//			System.out.println(fieldName +   "\t\t" + className + "\t\t" + returnTypeName);
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

	public List getHqlResult(String hqlQuery){
		Query query = getSession().createQuery(hqlQuery);
		List result = query.list();
		return result;
	}
	

}


