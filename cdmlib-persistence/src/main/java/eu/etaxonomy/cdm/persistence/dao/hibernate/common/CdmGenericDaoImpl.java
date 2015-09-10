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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.SessionImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.BooleanType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.DoubleType;
import org.hibernate.type.EntityType;
import org.hibernate.type.EnumType;
import org.hibernate.type.FloatType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.MaterializedClobType;
import org.hibernate.type.SerializableType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.jadira.usertype.dateandtime.joda.PersistentDateTime;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.util.ReflectionUtils;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.common.DoubleResult;
import eu.etaxonomy.cdm.database.data.FullCoverageDataGenerator;
import eu.etaxonomy.cdm.hibernate.DOIUserType;
import eu.etaxonomy.cdm.hibernate.EnumUserType;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.hibernate.PartialUserType;
import eu.etaxonomy.cdm.hibernate.URIUserType;
import eu.etaxonomy.cdm.hibernate.UUIDUserType;
import eu.etaxonomy.cdm.hibernate.WSDLDefinitionUserType;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.strategy.match.CacheMatcher;
import eu.etaxonomy.cdm.strategy.match.DefaultMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.FieldMatcher;
import eu.etaxonomy.cdm.strategy.match.IMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.match.MatchException;
import eu.etaxonomy.cdm.strategy.match.MatchMode;
import eu.etaxonomy.cdm.strategy.match.Matching;
import eu.etaxonomy.cdm.strategy.merge.IMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.MergeException;

@Repository
public class CdmGenericDaoImpl extends CdmEntityDaoBase<CdmBase> implements ICdmGenericDao{
	private static final Logger logger = Logger.getLogger(CdmGenericDaoImpl.class);


	private Set<Class<? extends CdmBase>> allCdmClasses = null;
	private final Map<Class<? extends CdmBase>, Set<ReferenceHolder>> referenceMap = new HashMap<Class<? extends CdmBase>, Set<ReferenceHolder>>();


	protected class ReferenceHolder{
		String propertyName;
		Class<? extends CdmBase> otherClass;
		Class<?> itemClass;
		Class<?> targetClass;  //new as item class is used for isCollection we have a duplicate here
		public boolean isCollection(){return itemClass != null;}
		@Override
        public String toString(){return otherClass.getSimpleName() + "." + propertyName ;};
	}

	public CdmGenericDaoImpl() {
		super(CdmBase.class);
	}

	@Override
	public List<CdmBase> getCdmBasesByFieldAndClass(Class clazz, String propertyName, CdmBase referencedCdmBase){
		Session session = super.getSession();

		Criteria criteria = session.createCriteria(clazz);
		criteria.add(Restrictions.eq(propertyName, referencedCdmBase));
		return criteria.list();
	}

	@Override
	public List<CdmBase> getCdmBasesWithItemInCollection(Class itemClass, Class clazz, String propertyName, CdmBase item){
		Session session = super.getSession();
		String thisClassStr = itemClass.getSimpleName();
		String otherClassStr = clazz.getSimpleName();
		String queryStr = " SELECT other FROM "+ thisClassStr + " this, " + otherClassStr + " other " +
			" WHERE this = :referencedObject AND this member of other."+propertyName ;
		Query query = session.createQuery(queryStr).setEntity("referencedObject", item);
		@SuppressWarnings("unchecked")
		List<CdmBase> result = query.list();
		return result;
	}

	@Override
	public Set<Class<? extends CdmBase>> getAllPersistedClasses(boolean includeAbstractClasses){
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

	@Override
	public Set<CdmBase> getReferencingObjects(CdmBase referencedCdmBase){
		Set<CdmBase> result = new HashSet<CdmBase>();
		if (referencedCdmBase == null) {
			return null;
		}
		try {

			referencedCdmBase = (CdmBase)HibernateProxyHelper.deproxy(referencedCdmBase);
			Class<? extends CdmBase> referencedClass = referencedCdmBase.getClass();

			Set<ReferenceHolder> holderSet = getOrMakeHolderSet(referencedClass);
			for (ReferenceHolder refHolder: holderSet){
				handleReferenceHolder(referencedCdmBase, result, refHolder);
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	protected Set<ReferenceHolder> getOrMakeHolderSet(
			Class<? extends CdmBase> referencedClass)
			throws ClassNotFoundException, NoSuchFieldException {
		Set<ReferenceHolder> holderSet = referenceMap.get(referencedClass);
		if (holderSet == null){
			holderSet = makeHolderSet(referencedClass);
			referenceMap.put(referencedClass, holderSet);
		}
		return holderSet;
	}

	@Override
	public Set<CdmBase> getReferencingObjectsForDeletion(CdmBase referencedCdmBase){
		Set<CdmBase> result = getReferencingObjects(referencedCdmBase);
		Set<ReferenceHolder> holderSet = referenceMap.get(IdentifiableEntity.class);
		try {
			if (holderSet == null){
				holderSet = makeHolderSet(IdentifiableEntity.class);
				referenceMap.put(IdentifiableEntity.class, holderSet);
			}
			Set<CdmBase> resultIdentifiableEntity = new HashSet<CdmBase>();
			for (ReferenceHolder refHolder: holderSet){
				handleReferenceHolder(referencedCdmBase, resultIdentifiableEntity, refHolder);
			}
			result.removeAll(resultIdentifiableEntity);

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
	protected Set<ReferenceHolder> makeHolderSet(Class<?> referencedClass) throws ClassNotFoundException, NoSuchFieldException {
		Set<ReferenceHolder> result = new HashSet<ReferenceHolder>();

		//init
		if (allCdmClasses == null){
			allCdmClasses = getAllPersistedClasses(false); //findAllCdmClasses();
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
			Class<?> referencedClass,
			SessionFactory sessionFactory, Class<? extends CdmBase> cdmClass,
			Type propertyType, String propertyName, boolean isCollection)
				throws ClassNotFoundException, NoSuchFieldException {


		if (propertyType.isEntityType()){
			EntityType entityType = (EntityType)propertyType;
			String associatedEntityName = entityType.getAssociatedEntityName();
			Class<?> entityClass = Class.forName(associatedEntityName);
			if (entityClass.isInterface()){
				logger.debug("There is an interface");
			}
			if (entityClass.isAssignableFrom(referencedClass)){
				makeSingleProperty(referencedClass, entityClass, propertyName, cdmClass, result, isCollection);
			}
		}else if (propertyType.isCollectionType()){
			CollectionType collectionType = (CollectionType)propertyType;
			//String role = collectionType.getRole();
			Type elType = collectionType.getElementType((SessionFactoryImplementor)sessionFactory);
			makePropertyType(result, referencedClass, sessionFactory, cdmClass, elType, propertyName, true);
		}else if (propertyType.isAnyType()){
//			AnyType anyType = (AnyType)propertyType;
			Field field = cdmClass.getDeclaredField(propertyName);
			Class<?> returnType = field.getType();
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

	private boolean makeSingleProperty(Class<?> itemClass, Class<?> type, String propertyName, Class cdmClass, Set<ReferenceHolder> result,/*CdmBase item,*/ boolean isCollection){
//			String fieldName = StringUtils.rightPad(propertyName, 30);
//			String className = StringUtils.rightPad(cdmClass.getSimpleName(), 30);
//			String returnTypeName = StringUtils.rightPad(type.getSimpleName(), 30);

//			logger.debug(fieldName +   "\t\t" + className + "\t\t" + returnTypeName);
			ReferenceHolder refHolder = new ReferenceHolder();
			refHolder.propertyName = propertyName;
			refHolder.otherClass = cdmClass;
			refHolder.itemClass = (isCollection ? itemClass : null) ;
			refHolder.targetClass = type ;

			result.add(refHolder);
		return true;
	}

	/**
	 * @param propertyType
	 * @return
	 */
	protected static boolean isNoDoType(Type propertyType) {
		boolean result = false;
		Class<?>[] classes = new Class[]{
				PersistentDateTime.class,
				WSDLDefinitionUserType.class,
				UUIDUserType.class,
				PartialUserType.class,
				StringType.class,
				BooleanType.class,
				IntegerType.class,
				MaterializedClobType.class,
				LongType.class,
				FloatType.class,
				SerializableType.class,
				DoubleType.class,
				URIUserType.class,
				EnumType.class,
				EnumUserType.class,
				DOIUserType.class
				};
		Set<String> classNames = new HashSet<String>();
		for (Class<?> clazz: classes){
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

	@Override
	public List<CdmBase> getHqlResult(String hqlQuery){
		Query query = getSession().createQuery(hqlQuery);
		List<CdmBase> result = query.list();
		return result;
	}

	@Override
	public Query getHqlQuery(String hqlQuery){
		Query query = getSession().createQuery(hqlQuery);
		return query;
	}

	@Override
	public <T extends CdmBase> void   merge(T cdmBase1, T cdmBase2, IMergeStrategy mergeStrategy) throws MergeException {
		SessionImpl session = (SessionImpl) getSession();
		DeduplicationHelper helper = new DeduplicationHelper(session, this);
		helper.merge(cdmBase1, cdmBase2, mergeStrategy);
	}


	@Override
	public <T extends CdmBase> boolean isMergeable(T cdmBase1, T cdmBase2, IMergeStrategy mergeStrategy) throws MergeException {
		SessionImpl session = (SessionImpl) getSession();
		DeduplicationHelper helper = new DeduplicationHelper(session, this);
		return helper.isMergeable(cdmBase1, cdmBase2, mergeStrategy);
	}


	@Override
	public <T extends CdmBase> T find(Class<T> clazz, int id){
		Session session;
		session =  getSession();
		//session = getSession().getSessionFactory().getCurrentSession();
		Object o = session.get(clazz, id);
		return (T)o;
	}

	@Override
	public <T extends CdmBase> T find(Class<T> clazz, int id, List<String> propertyPaths){
	    Session session;
	    session =  getSession();
	    T bean = (T)session.get(clazz, id);
	    if(bean == null){
            return bean;
        }
        defaultBeanInitializer.initialize(bean, propertyPaths);
        return bean;
	}

	@Override
    public <T extends CdmBase> T find(Class<T> clazz, UUID uuid){
	    return find(clazz, uuid, null);
	}

    @Override
    public <T extends CdmBase> T find(Class<T> clazz, UUID uuid, List<String> propertyPaths){
        Session session = getSession();
        Criteria crit = session.createCriteria(type);
        crit.add(Restrictions.eq("uuid", uuid));
        crit.addOrder(Order.desc("created"));
        @SuppressWarnings("unchecked")
        List<T> results = crit.list();
        if (results.isEmpty()){
            return null;
        }else{
            if(results.size() > 1){
                logger.error("findByUuid() delivers more than one result for UUID: " + uuid);
            }
            T result = results.get(0);
            if (result == null || propertyPaths == null){
                return result;
            }else{
                defaultBeanInitializer.initialize(result, propertyPaths);
                return result;
            }
        }
    }


	@Override
	public <T extends IMatchable> List<T> findMatching(T objectToMatch,
			IMatchStrategy matchStrategy) throws MatchException {

		getSession().flush();
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

	private <T extends IMatchable> List<T> findMatchingNullSafe(T objectToMatch,	IMatchStrategy matchStrategy) throws IllegalArgumentException, IllegalAccessException, MatchException {
		List<T> result = new ArrayList<T>();
		Session session = getSession();
		Class<?> matchClass = objectToMatch.getClass();
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
				if (StringUtils.isBlank(cacheValue)){
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
						IMatchStrategy valueMatchStrategy = DefaultMatchStrategy.NewInstance(matchClass);
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
	@Override
    public void saveMetaData(CdmMetaData cdmMetaData) {
		getSession().saveOrUpdate(cdmMetaData);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao#getMetaData()
	 */
	@Override
    public List<CdmMetaData> getMetaData() {
		Session session = getSession();
		Criteria crit = session.createCriteria(CdmMetaData.class);
		List<CdmMetaData> results = crit.list();
		return results;
	}

    @Override
    public Object initializeCollection(UUID ownerUuid, String fieldName, List<String> appendedPropertyPaths)  {
        List<String> propertyPaths = new ArrayList<String>();
        propertyPaths.add(fieldName);
        if(appendedPropertyPaths != null && !appendedPropertyPaths.isEmpty()) {
            for(String app : appendedPropertyPaths) {
                propertyPaths.add(fieldName + "." + app);
            }
        }
        CdmBase cdmBase = load(ownerUuid, propertyPaths);
        Field field = ReflectionUtils.findField(cdmBase.getClass(), fieldName);
        field.setAccessible(true);
        Object obj;
        try {
            obj = field.get(cdmBase);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Requested object is not accessible");
        }
        if(obj instanceof Collection || obj instanceof Map) {
            return obj;
        } else {
            throw new IllegalArgumentException("Field name provided does not correspond to a collection or map");
        }
    }

    @Override
    public Object initializeCollection(UUID ownerUuid, String fieldName)  {
        return initializeCollection(ownerUuid, fieldName, null);
    }

    @Override
    public boolean isEmpty(UUID ownerUuid, String fieldName) {
        Object col = initializeCollection(ownerUuid, fieldName);
        if(col instanceof Collection) {
            return ((Collection)col).isEmpty();
        } else if(col instanceof Map){
            return ((Map)col).isEmpty();
        }

        return false;
    }

    @Override
    public int size(UUID ownerUuid, String fieldName) {
        Object col = initializeCollection(ownerUuid, fieldName);
        if(col instanceof Collection) {
            return ((Collection)col).size();
        } else if(col instanceof Map){
            return ((Map)col).size();
        }
        return 0;
    }


    @Override
    public Object get(UUID ownerUuid, String fieldName, int index) {
        Object col = initializeCollection(ownerUuid, fieldName);
        if(col instanceof List) {
            return ((List)col).get(index);
        } else {
            throw new IllegalArgumentException("Field name provided does not correspond to a list");
        }
    }

    @Override
    public boolean contains(UUID ownerUuid, String fieldName, Object element) {
        Object col = initializeCollection(ownerUuid, fieldName);
        if(col instanceof Collection) {
            return ((Collection)col).contains(element);
        } else {
            throw new IllegalArgumentException("Field name provided does not correspond to a collection");
        }
    }

    @Override
    public boolean containsKey(UUID ownerUuid, String fieldName, Object key) {
        Object col = initializeCollection(ownerUuid, fieldName);
        if(col instanceof Map) {
            return ((Map)col).containsKey(key);
        } else {
            throw new IllegalArgumentException("Field name provided does not correspond to a map");
        }
    }



    @Override
    public boolean containsValue(UUID ownerUuid, String fieldName, Object value) {
        Object col = initializeCollection(ownerUuid, fieldName);
        if(col instanceof Map) {
            return ((Map)col).containsValue(value);
        } else {
            throw new IllegalArgumentException("Field name provided does not correspond to a map");
        }
    }

    @Override
	public void createFullSampleData() {
		FullCoverageDataGenerator dataGenerator = new FullCoverageDataGenerator();
		dataGenerator.fillWithData(getSession());
	}



}