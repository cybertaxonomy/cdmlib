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

import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.SessionImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.query.Query;
import org.hibernate.type.BooleanType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.DoubleType;
import org.hibernate.type.EntityType;
import org.hibernate.type.EnumType;
import org.hibernate.type.FloatType;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.hibernate.type.MaterializedClobType;
import org.hibernate.type.OneToOneType;
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
import eu.etaxonomy.cdm.hibernate.BigDecimalUserType;
import eu.etaxonomy.cdm.hibernate.DOIUserType;
import eu.etaxonomy.cdm.hibernate.EnumSetUserType;
import eu.etaxonomy.cdm.hibernate.EnumUserType;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.hibernate.OrcidUserType;
import eu.etaxonomy.cdm.hibernate.PartialUserType;
import eu.etaxonomy.cdm.hibernate.SeverityUserType;
import eu.etaxonomy.cdm.hibernate.ShiftUserType;
import eu.etaxonomy.cdm.hibernate.URIUserType;
import eu.etaxonomy.cdm.hibernate.UUIDUserType;
import eu.etaxonomy.cdm.hibernate.WSDLDefinitionUserType;
import eu.etaxonomy.cdm.hibernate.WikiDataItemIdUserType;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.metadata.CdmMetaData;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.persistence.dto.ReferencingObjectDto;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
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
public class CdmGenericDaoImpl
          extends CdmBaseDaoImpl
          implements ICdmGenericDao {

    private static final Logger logger = LogManager.getLogger();

	private Set<Class<? extends CdmBase>> allCdmClasses = null;
	private final Map<Class<? extends CdmBase>, Set<ReferenceHolder>> referenceMap = new HashMap<>();


	protected class ReferenceHolder{
		String propertyName;
		Class<? extends CdmBase> otherClass;
		Class<?> itemClass;
		Class<?> targetClass;  //new as item class is used for isCollection we have a duplicate here
		public boolean isCollection(){return itemClass != null;}
		@Override
        public String toString(){return otherClass.getSimpleName() + "." + propertyName ;}
	}

	public CdmGenericDaoImpl() {
	}

//    @Override
    private List<ReferencingObjectDto> getCdmBasesByFieldAndClassDto(Class<? extends CdmBase> clazz, String propertyName,
            CdmBase referencedCdmBase, Integer limit){

        Query<ReferencingObjectDto> query = getSession().createQuery("SELECT new eu.etaxonomy.cdm.persistence.dto.ReferencingObjectDto(this.uuid, this.id) "
                    + "FROM "+ clazz.getSimpleName() + " this "
                    + "WHERE this." + propertyName +" = :referencedObject",
                    ReferencingObjectDto.class)
                .setParameter("referencedObject", referencedCdmBase);

        if (limit != null){
            query.setMaxResults(limit);
        }
        List<ReferencingObjectDto> result = query.list();
        result.forEach(dto->dto.setType((Class<CdmBase>)clazz));
        return result;
    }

    @Override
    public List<CdmBase> getCdmBasesByFieldAndClass(Class<? extends CdmBase> clazz, String propertyName,
            CdmBase referencedCdmBase, Integer limit){

        //FIXME replace CdmBase by T extends CdmBase, but this created compile errors
        //for now on jenkins

	    Session session = super.getSession();

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<CdmBase> cq = (CriteriaQuery)cb.createQuery(clazz);
        Root<CdmBase> root = (Root)cq.from(clazz);
        cq.select(root)
          .where(predicateEqual(cb, root, propertyName, referencedCdmBase));
        TypedQuery<CdmBase> query = session.createQuery(cq);
        if (limit != null){
            query.setMaxResults(limit);
        }
        List<CdmBase> results = query.getResultList();

        return results;
	}

	@Override
    public long getCountByFieldAndClass(Class<? extends CdmBase> clazz, String propertyName, CdmBase referencedCdmBase){
        Query<Long> query = getSession().createQuery("SELECT count(this) "
                + " FROM "+ clazz.getSimpleName() + " this "
                + " WHERE this." + propertyName +" = :referencedObject",
                Long.class)
                .setParameter("referencedObject", referencedCdmBase);

        long result =query.uniqueResult();
        return result;
    }

    @Override
    public List<ReferencingObjectDto> getCdmBasesWithItemInCollectionDto(Class<?> itemClass,
            Class<? extends CdmBase> clazz, String propertyName, CdmBase item, Integer limit){

        String queryStr = withItemInCollectionHql(itemClass, clazz, propertyName,
                "new eu.etaxonomy.cdm.persistence.dto.ReferencingObjectDto(other.uuid, other.id)");
        Query<ReferencingObjectDto> query = getSession().createQuery(queryStr, ReferencingObjectDto.class)
                .setParameter("referencedObject", item);
        if (limit != null){
            query.setMaxResults(limit);
        }
        List<ReferencingObjectDto> result = query.list();
        result.forEach(dto->dto.setType((Class)clazz));
        return result;
    }

	@Override
	public List<CdmBase> getCdmBasesWithItemInCollection(Class<?> itemClass,
	        Class<?> clazz, String propertyName, CdmBase item, Integer limit){

		String queryStr = withItemInCollectionHql(itemClass, clazz, propertyName, "other");
		Query<CdmBase> query = getSession().createQuery(queryStr, CdmBase.class)
		        .setParameter("referencedObject", item);
		if (limit != null){
		    query.setMaxResults(limit);
		}
		List<CdmBase> result = query.list();
		return result;
	}

    private String withItemInCollectionHql(Class<?> itemClass, Class<?> clazz, String propertyName, String select) {
        String thisClassStr = itemClass.getSimpleName();
        String otherClassStr = clazz.getSimpleName();
        String result =  "SELECT "+select+" FROM "+ thisClassStr + " this, " + otherClassStr + " other " +
                " WHERE this = :referencedObject AND this member of other." + propertyName ;
        return result;
    }

    @Override
    public long getCountWithItemInCollection(Class<?> itemClass, Class<?> clazz, String propertyName,
            CdmBase item){

        String queryStr = withItemInCollectionHql(itemClass, clazz, propertyName, "count(this)");

        Query<Long> query = getSession().createQuery(queryStr, Long.class)
                .setParameter("referencedObject", item);
        long result =query.uniqueResult();
        return result;
    }

    @Override
    public Set<Class<? extends CdmBase>> getAllPersistedClasses(boolean includeAbstractClasses){
        Set<Class<? extends CdmBase>> result = new HashSet<>();

        EntityManagerFactory sessionFactory = getSession().getSessionFactory();
        Set<javax.persistence.metamodel.EntityType<?>> entities = sessionFactory.getMetamodel().getEntities();
        for (javax.persistence.metamodel.EntityType<?> entity : entities){
            if (! entity.getName().endsWith("_AUD") && !entity.getName().endsWith("_AUD1")){
                Class<?> clazz = entity.getBindableJavaType();
                boolean isAbstractClass = Modifier.isAbstract(clazz.getModifiers());
                if (! isAbstractClass || includeAbstractClasses){
                    result.add((Class)clazz);
                }
            }
        }
        return result;
    }

    @Override
    public Set<ReferencingObjectDto> getReferencingObjectsDto(CdmBase referencedCdmBase){
        Set<ReferencingObjectDto> result = new HashSet<>();
        if (referencedCdmBase == null) {
            return null;
        }
        try {

            referencedCdmBase = HibernateProxyHelper.deproxy(referencedCdmBase);
            Class<? extends CdmBase> referencedClass = referencedCdmBase.getClass();

            Set<ReferenceHolder> holderSet = getOrMakeHolderSet(referencedClass);
            for (ReferenceHolder refHolder: holderSet){
                handleReferenceHolderDto(referencedCdmBase, result, refHolder, false);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

	@Override
	public Set<CdmBase> getReferencingObjects(CdmBase referencedCdmBase){
		Set<CdmBase> result = new HashSet<>();
		if (referencedCdmBase == null) {
			return null;
		}
		try {

			referencedCdmBase = HibernateProxyHelper.deproxy(referencedCdmBase);
			Class<? extends CdmBase> referencedClass = referencedCdmBase.getClass();

			Set<ReferenceHolder> holderSet = getOrMakeHolderSet(referencedClass);
			//Integer count = getReferencingObjectsCount(referencedCdmBase);
			for (ReferenceHolder refHolder: holderSet){
//			    if (count > 100000) {
//                    handleReferenceHolder(referencedCdmBase, result, refHolder, true);
//                }else{
                    handleReferenceHolder(referencedCdmBase, result, refHolder, false);
//                }
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
    public long getReferencingObjectsCount(CdmBase referencedCdmBase){
        long result = 0;
        if (referencedCdmBase == null) {
            return 0;
        }
        try {

            referencedCdmBase = HibernateProxyHelper.deproxy(referencedCdmBase);
            Class<? extends CdmBase> referencedClass = referencedCdmBase.getClass();

            Set<ReferenceHolder> holderSet = getOrMakeHolderSet(referencedClass);
            for (ReferenceHolder refHolder: holderSet){
                result =+ handleReferenceHolderForCount(referencedCdmBase, result, refHolder);
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
	    if (referencedCdmBase == null){
	        return null;
	    }
		Set<CdmBase> result = getReferencingObjects(referencedCdmBase);
		Set<ReferenceHolder> holderSet = referenceMap.get(IdentifiableEntity.class);
		try {
			if (holderSet == null){
				holderSet = makeHolderSet(IdentifiableEntity.class);
				referenceMap.put(IdentifiableEntity.class, holderSet);
			}
			Set<CdmBase> resultIdentifiableEntity = new HashSet<>();
			for (ReferenceHolder refHolder: holderSet){
				handleReferenceHolder(referencedCdmBase, resultIdentifiableEntity, refHolder, false);
			}
			result.removeAll(resultIdentifiableEntity);

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

    private void handleReferenceHolderDto(CdmBase referencedCdmBase,
            Set<ReferencingObjectDto> result, ReferenceHolder refHolder, boolean limited) {

        boolean isCollection = refHolder.isCollection();
        if (isCollection){
            if (limited){
                result.addAll(getCdmBasesWithItemInCollectionDto(refHolder.itemClass, refHolder.otherClass, refHolder.propertyName, referencedCdmBase, 100));
            }else{
                result.addAll(getCdmBasesWithItemInCollectionDto(refHolder.itemClass, refHolder.otherClass, refHolder.propertyName, referencedCdmBase, null));
            }
        }else{
            if (limited){
                result.addAll(getCdmBasesByFieldAndClassDto(refHolder.otherClass, refHolder.propertyName, referencedCdmBase, 100));
            }else{
                result.addAll(getCdmBasesByFieldAndClassDto(refHolder.otherClass, refHolder.propertyName, referencedCdmBase, null));
            }
        }
    }

	private void handleReferenceHolder(CdmBase referencedCdmBase,
			Set<CdmBase> result, ReferenceHolder refHolder, boolean limited) {

	    boolean isCollection = refHolder.isCollection();
		if (isCollection){
		    if (limited){
		        result.addAll(getCdmBasesWithItemInCollection(refHolder.itemClass, refHolder.otherClass, refHolder.propertyName, referencedCdmBase, 100));
		    }else{
		        result.addAll(getCdmBasesWithItemInCollection(refHolder.itemClass, refHolder.otherClass, refHolder.propertyName, referencedCdmBase, null));
		    }
		}else{
		    if (limited){
		        result.addAll(getCdmBasesByFieldAndClass(refHolder.otherClass, refHolder.propertyName, referencedCdmBase, 100));
		    }else{
		        result.addAll(getCdmBasesByFieldAndClass(refHolder.otherClass, refHolder.propertyName, referencedCdmBase, null));
		    }
		}
	}

    private Long handleReferenceHolderForCount(CdmBase referencedCdmBase,
            Long result, ReferenceHolder refHolder) {
        boolean isCollection = refHolder.isCollection();
        if (isCollection){
            result += getCountWithItemInCollection(refHolder.itemClass, refHolder.otherClass, refHolder.propertyName, referencedCdmBase);
        }else{
            result += getCountByFieldAndClass(refHolder.otherClass, refHolder.propertyName, referencedCdmBase);
        }
        return result;
    }

    //Dev Note: the soon to be removed class referringObjectMetadataFactoryImpl used properties in
    //   entityType.getAttributes(), e.g. isAssociation(). This might be considered for xxx, too
	private Set<ReferenceHolder> makeHolderSet(Class<?> referencedClass) throws ClassNotFoundException, NoSuchFieldException {
		Set<ReferenceHolder> result = new HashSet<>();

		//init
		if (allCdmClasses == null){
			allCdmClasses = getAllPersistedClasses(false); //findAllCdmClasses();
		}
		SessionFactory sessionFactory = getSession().getSessionFactory();
//        EntityManagerFactory sessionFactory = getSession().getSessionFactory();

		for (Class<? extends CdmBase> cdmClass : allCdmClasses){
			ClassMetadata classMetadata = sessionFactory.getClassMetadata(cdmClass);
//	        javax.persistence.metamodel.EntityType<? extends CdmBase> classMetadata = sessionFactory.getMetamodel().entity(cdmClass);
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
			if (entityType instanceof OneToOneType){
			    OneToOneType oneToOneType = (OneToOneType)entityType;
			    ForeignKeyDirection direction = oneToOneType.getForeignKeyDirection();
			    if (direction == ForeignKeyDirection.TO_PARENT){  //this
			        return;
			    }
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

	private boolean makeSingleProperty(Class<?> itemClass, Class<?> type, String propertyName, Class<? extends CdmBase> cdmClass, Set<ReferenceHolder> result,/*CdmBase item,*/ boolean isCollection){
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
				DOIUserType.class,
				OrcidUserType.class,
				WikiDataItemIdUserType.class,
                ShiftUserType.class,
				EnumSetUserType.class,
				SeverityUserType.class,
				BigDecimalUserType.class,
				};
		Set<String> classNames = new HashSet<>();
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
    public <T> List<T> getHqlResult(String hqlQuery, Class<T> clazz){
        return getHqlResult(hqlQuery, new Object[0], clazz);
    }


	@Override
	public <T> List<T> getHqlResult(String hqlQuery, Object[] params, Class<T> clazz){
		params = params == null ? new Object[0] : params;
		Query<T> query = getSession().createQuery(hqlQuery, clazz);
		for(int i = 0; i<params.length; i++){
		    query.setParameter(i+1, params[i]); //param numbering in query should start with 1
		}
        List<T> result = query.list();
		return result;
	}

	@Override
    public <T> List<T> getHqlResult(String hqlQuery, Map<String,Object> params, Class<T> clazz){
        params = params == null ? new HashMap<>() : params;
        Query<T> query = getSession().createQuery(hqlQuery, clazz);
        for(Map.Entry<String,Object> entry : params.entrySet()){
            query.setParameter(entry.getKey(), entry.getValue());
        }
        List<T> result = query.list();
        return result;
    }

    @Override
    public <T> List<Map<String,T>> getHqlMapResult(String hqlQuery, Map<String,Object> params, Class<T> clazz) throws UnsupportedOperationException{
        @SuppressWarnings({ "rawtypes", "unchecked" })
        List<Map<String,T>> result = (List)getHqlResult(hqlQuery, params, Map.class);
        return result;
    }

    @Override
    public <T> List<Map<String,T>> getHqlMapResult(String hqlQuery, Class<T> clazz) throws UnsupportedOperationException{
        return getHqlMapResult(hqlQuery, new Object[] {}, clazz);
    }


	@Override
    public <T> List<Map<String,T>> getHqlMapResult(String hqlQuery, Object[] params, Class<T> clazz) throws UnsupportedOperationException{
	    @SuppressWarnings({ "rawtypes", "unchecked" })
        List<Map<String,T>> result = (List)getHqlResult(hqlQuery, params, Map.class);
	    return result;
	}

	@Override
	public Query<?> getHqlQuery(String hqlQuery){
		Query<?> query = getSession().createQuery(hqlQuery);
		return query;
	}

    @Override
    public <T> Query<T> getHqlQuery(String hqlQuery, Class<T> clazz) throws UnsupportedOperationException{
        Query<T> query = getSession().createQuery(hqlQuery, clazz);
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
		T o = session.get(clazz, id);
		return o;
	}

	@Override
	public <T extends CdmBase> T find(Class<T> clazz, int id, List<String> propertyPaths){
	    Session session;
	    session =  getSession();
	    T bean = session.get(clazz, id);
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

        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(clazz);
        Root<T> root = cq.from(clazz);
        cq.select(root)
          .where(predicateUuid(cb, root, uuid))
          .orderBy(cb.desc(root.get("created")));
        List<T> results = session.createQuery(cq).getResultList();

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

    @SuppressWarnings("unchecked")
    @Override
    public <T extends CdmBase> T findWithoutFlush(Class<T> clazz, UUID uuid) throws DataAccessException {
        return (T)this.findByUuidWithoutFlush((Class<CdmBase>)clazz, uuid);
    }

    /**
     * This is a copy of see (see also)
     * @see CdmEntityDaoBase#findByUuidWithoutFlush(Class, UUID)
     */
    private <T extends CdmBase> T findByUuidWithoutFlush(Class<T> clazz, UUID uuid) throws DataAccessException {
        Session session = getSession();
        FlushMode currentFlushMode = session.getHibernateFlushMode();
        try {
            // set flush mode to manual so that the session does not flush
            // when before performing the query
            session.setHibernateFlushMode(FlushMode.MANUAL);

            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(clazz);
            Root<T> root = cq.from(clazz);
            cq.select(root)
              .where(predicateUuid(cb, root, uuid))
              .orderBy(cb.desc(root.get("created")));
            List<T> results = session.createQuery(cq).getResultList();

            results = deduplicateResult(results);
            if (results.isEmpty()) {
                return null;
            } else {
                if (results.size() > 1) {
                    logger.error("findByUuid() delivers more than one result for UUID: " + uuid);
                }
                return results.get(0);
            }
        } finally {
            // set back the session flush mode
            if (currentFlushMode != null) {
                session.setHibernateFlushMode(currentFlushMode);
            }
        }
    }


    @Override
    public <T extends IMatchable> List<T> findMatching(
            T objectToMatch, IMatchStrategy matchStrategy) throws MatchException{
        return findMatching(objectToMatch, matchStrategy, false);
    }

    /**
     * Like {@link #findMatching(IMatchable, IMatchStrategy)} but with additional parameter
     * for debugging.
     *
     * @param includeCandidates if <code>true</code> the list of match candidates (objects matching the hql query used) are included in the result.
     *                         The parameter is mostly for debugging.
     */
	public <T extends IMatchable> List<T> findMatching(T objectToMatch,
			IMatchStrategy matchStrategy, boolean includeCandidates) throws MatchException {

		getSession().flush();
		try {
			List<T> result = new ArrayList<>();
			if(objectToMatch == null){
				return result;
			}
			if (matchStrategy == null){
				matchStrategy = DefaultMatchStrategy.NewInstance(objectToMatch.getClass());
			}
			result.addAll(findMatchingNullSafe(objectToMatch, matchStrategy, false));
			return result;
		} catch (IllegalArgumentException|IllegalAccessException e) {
			throw new MatchException(e);
		}
	}

	private <T extends IMatchable> List<T> findMatchingNullSafe(T objectToMatch,
	        IMatchStrategy matchStrategy, boolean includeCandidates) throws IllegalArgumentException, IllegalAccessException, MatchException {

	    List<T> result = new ArrayList<>();
		Session session = getSession();
		Class<CdmBase> matchClass = (Class)objectToMatch.getClass();
		ClassMetadata classMetaData = session.getSessionFactory().getClassMetadata(matchClass.getCanonicalName());

		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<CdmBase> query = cb.createQuery(matchClass);
		List<Predicate> finalAndPredicates = new ArrayList<>();
		Root<CdmBase> root = query.from(matchClass);
		root.alias("root");
		boolean noMatch = makeCriteria(objectToMatch, matchStrategy, classMetaData,
		        cb, finalAndPredicates, root, 1);
		Predicate finalPredicate = cb.and(finalAndPredicates.toArray(new Predicate[0]));
		query.select(root)
		     .where(finalPredicate);

//		Criteria criteria = session.createCriteria(matchClass);
//        boolean noMatch = makeCriteria(objectToMatch, matchStrategy, classMetaData, criteria, 1);
		if (logger.isDebugEnabled()){logger.warn(query);}

		//session.flush();
		if (noMatch == false){

			@SuppressWarnings({ "rawtypes", "unchecked" })
            //deduplicate (for some reason the match candidate list return matching teams >1x if they have >1 member, so we deduplicate to be on the safe site
			List<T> matchCandidates = deduplicateResult((List)session.createQuery(query).getResultList());
//            List<T> matchCandidates = deduplicateResult(criteria.list());
			matchCandidates.remove(objectToMatch);
			for (T matchCandidate : matchCandidates ){
				if (includeCandidates || matchStrategy.invoke(objectToMatch, matchCandidate).isSuccessful()){
					result.add(matchCandidate);
				}else{
					logger.info("Match candidate did not match: " + matchCandidate);
				}
			}
		}
		return result;
	}

	/**
	 * Fills the criteria according to the object to match, the match strategy
	 * and the classMetaData.
	 *
	 * @param objectToMatch the object to match
	 * @param matchStrategy the match strategy used
	 * @param classMetaData the precomputed class metadata
	 * @param finalAndPredicates
	 * @param criteria the criteria to fill
	 * @param level recursion level
	 * @return <code>true</code> if definitely no matching object will be found,
	 *         <code>false</code> if nothing is known on the existence of a matching result
	 */
	private boolean makeCriteria(Object objectToMatch,
			IMatchStrategy matchStrategy, ClassMetadata classMetaData,
			CriteriaBuilder cb, List<Predicate> finalAndPredicates,
			From<CdmBase, CdmBase> from, int level) throws IllegalAccessException, MatchException {

	    Matching matching = matchStrategy.getMatching((IMatchable)objectToMatch);
		boolean noMatch = false;
		Map<String, List<MatchMode>> replaceMatchers = new HashMap<>();
		for (CacheMatcher cacheMatcher: matching.getCacheMatchers()){
		    Field protectedField = cacheMatcher.getProtectedField(matching);
			boolean cacheProtected = protectedField == null ? false : (Boolean)protectedField.get(objectToMatch);
			if (cacheProtected == true){
				String cacheValue = (String)cacheMatcher.getField().get(objectToMatch);
				if (StringUtils.isBlank(cacheValue)){
					return true;  //no match
				}else{
//				    cacheMatcher.getField().getDeclaringClass();
				    Predicate p = predicateStrNotNull(cb, from, cacheMatcher.getPropertyName(), cacheValue);
				    finalAndPredicates.add(p);
				    Predicate p2 = predicateBoolean(cb, from, cacheMatcher.getProtectedPropertyName(), cacheProtected);
				    finalAndPredicates.add(p2);

//					criteria.add(Restrictions.eq(cacheMatcher.getPropertyName(), cacheValue));
//					criteria.add(Restrictions.eq(cacheMatcher.getProtectedPropertyName(), cacheProtected));

					List<DoubleResult<String, MatchMode>> replacementModes = cacheMatcher.getReplaceMatchModes(matching);
					for (DoubleResult<String, MatchMode> replacementMode: replacementModes ){
						String propertyName = replacementMode.getFirstResult();
						List<MatchMode> replaceMatcherList = replaceMatchers.get(propertyName);
						if (replaceMatcherList == null){
							replaceMatcherList = new ArrayList<>();
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
			List<MatchMode> matchModes = new ArrayList<>();
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
					matchComponentType(cb, finalAndPredicates, from, fieldMatcher, propertyName, value, matchModes);
				}else{
					noMatch = matchNonComponentType(cb, finalAndPredicates, from, fieldMatcher, propertyName, value, matchModes, propertyType, level);
				}
			}
			if (noMatch){
				return noMatch;
			}
		}
		return noMatch;
	}

	private void matchComponentType(CriteriaBuilder cb, List<Predicate> andPredicates, Path<CdmBase> root,
			FieldMatcher fieldMatcher, String propertyName, Object value,
			List<MatchMode> matchModes) throws MatchException, IllegalAccessException {

	    if (value == null){
			boolean requiresSecondNull = requiresSecondNull(matchModes, null);
			if (requiresSecondNull){
			    Predicate p = predicateIsNull(cb, root, propertyName);
			    andPredicates.add(p);
//				criteria.add(Restrictions.isNull(propertyName));
			}else{
				//this should not happen, should be handled as ignore before
				logger.warn("Component type not yet implemented for (null) value: " + propertyName);
				throw new MatchException("Component type not yet fully implemented for (null) value. Property: " + propertyName);
			}
		}else{
			Class<?> componentClass = fieldMatcher.getField().getType();
			Map<String, Field> fields = CdmUtils.getAllFields(componentClass, Object.class, false, false, true, false);
			for (String fieldName : fields.keySet()){
//			    String restrictionPath = propertyName +"."+fieldName;
				Object componentValue = fields.get(fieldName).get(value);
				Path<Object> path = root.get(propertyName).get(fieldName);
				//TODO differentiate matchMode
				Predicate predicate = createCriterion(cb, path, componentValue, matchModes);
				andPredicates.add(predicate);
			}
		}
	}

    /**
     * @param level the recursion level
     */
    private boolean matchNonComponentType(CriteriaBuilder cb, List<Predicate> finalAndPredicates, From<CdmBase,CdmBase> root,
			FieldMatcher fieldMatcher,
			String propertyName,
			Object value,
			List<MatchMode> matchModes,
			Type propertyType, int level)
			throws HibernateException, DataAccessException, MatchException, IllegalAccessException{

	    boolean noMatch = false;
		if (isRequired(matchModes) && value == null){
			noMatch = true;
			return noMatch;
		}else if (requiresSecondNull(matchModes,value)){
		    finalAndPredicates.add(predicateIsNull(cb, root, propertyName));
//			criteria.add(Restrictions.isNull(propertyName));
		}else{
			if (isMatch(matchModes)){
				if (propertyType.isCollectionType()){
				    if (value instanceof Collection) {
				        //TODO fieldMatcher?
	                    matchCollection(cb, finalAndPredicates, root, propertyName, (Collection<?>)value, level);

				    }else if (value instanceof Map) {
				        //TODO map not yet handled for match
				    }else {
				        //TODO not yet handled
				    }
				}else{
//					JoinType joinTypeHib = JoinType.INNER_JOIN;
//					if (! requiresSecondValue(matchModes, value)){
//						joinType = JoinType.LEFT_OUTER_JOIN;
//					}
					JoinType joinType = JoinType.INNER;
                    if (! requiresSecondValue(matchModes, value)){
                        joinType = JoinType.LEFT;
                    }

					//TODO Object
//					Join<CdmBase, CdmBase> join = root.join(propertyName, joinType);
					Join<CdmBase, CdmBase> join = root.join(propertyName, joinType);
//					join.on(cb.isNotNull(join.get("id")));

//					Criteria matchCriteria = criteria.createCriteria(propertyName, joinTypeHib).add(Restrictions.isNotNull("id"));
					@SuppressWarnings("rawtypes")
                    Class matchClass = value.getClass();
                    if (IMatchable.class.isAssignableFrom(matchClass)){
                        @SuppressWarnings({ "rawtypes", "unchecked" })
                        Join specificJoin = cb.treat(join, matchClass);
                        //TODO do we want an explicit type check here like cb.equal(join.type(), matchClass);
                        //     maybe not needed as we want to allow 2 objects to match even if they are of different subtypes
                        //     but they have only attributes in use that are available also in the superclass
						IMatchStrategy valueMatchStrategy = fieldMatcher.getMatchStrategy() != null? fieldMatcher.getMatchStrategy() : DefaultMatchStrategy.NewInstance(matchClass);
						ClassMetadata valueClassMetaData = getSession().getSessionFactory().getClassMetadata(matchClass.getCanonicalName());
						noMatch = makeCriteria(value, valueMatchStrategy, valueClassMetaData, cb, finalAndPredicates, specificJoin, level+1);
//						noMatch = makeCriteria(value, valueMatchStrategy, valueClassMetaData, matchCriteria, level+1);
					}else{
						logger.error("Class to match (" + matchClass + ") is not of type IMatchable");
						throw new MatchException("Class to match (" + matchClass + ") is not of type IMatchable");
					}
				}
			}else if (isEqual(matchModes)){
				Predicate p = createCriterion(cb, root, propertyName, value, matchModes);
				finalAndPredicates.add(p);
			}else {
				logger.warn("Unhandled match mode: " + matchModes + ", value: " + (value==null?"null":value));
			}
		}
		return noMatch;
	}

    /**
     * Add restrictions for collections matched by matchMode {@value MatchMode#MATCH}
     *
     * NOTE: current implementation is only a work around to handle #9905 by
     * checking the collection size and if the collection contains instances of
     * class Person it checks that there is at least 1 person matching in
     * nomenclaturalTitle, no matter at which position.
     * See #9905 and #9964
     *
     * @param level recursion level
     */
    private void matchCollection(CriteriaBuilder cb, List<Predicate> andPredicates, From<CdmBase,CdmBase> root, String propertyName, Collection<?> collection, int level) {
        int i = 0;
        //this is a workaround to avoid handling TeamOrPersonBase e.g. in references.
        //TeamOrPersonBase does not have a property 'teamMembers' and therefore an
        //according restriction can not be added
        if (level > 1) {
            return;
        }

        andPredicates.add(predicateCollectionSize(cb, root, propertyName, collection.size()));
//        criteria.add(Restrictions.sizeEq(propertyName, collection.size()));

//        String propertyAlias = propertyName+"Alias";
//        criteria.createAlias(propertyName, propertyAlias);
        //In future (hibernate >5.1 JPA will allow using index joins: https://www.logicbig.com/tutorials/java-ee-tutorial/jpa/criteria-api-collection-operations.html
//        Criteria subCriteria = criteria.createCriteria(propertyName+"[1]");

        Join<CdmBase,CdmBase> join = root.join(propertyName, JoinType.INNER);
        //join.alias(propertyName); only for debugging
//        Criteria subCriteria = criteria.createCriteria(propertyName);

        for (Object single : collection) {
            Class<?> classOfSingle = CdmBase.deproxy(single).getClass();
            if (classOfSingle.equals(Person.class)) {
                Person person = ((Person)single);
//	            DetachedCriteria subCriteria = DetachedCriteria.forClass(classOfSingle);
//	            subCriteria.add(Property.forName("familyName").eq(person.getFamilyName()));
//	            subCriteria.setProjection(Projections.property("id"));
//	            Subqueries.propertyIn(propertyName, subCriteria);
//	            criteria.add(detCrit);
//              Criterion criterion = Restrictions.eqOrIsNull(propertyAlias+".familyName", person.getFamilyName());
//              criteria.add(criterion);
//              Criterion criterion2 = Restrictions.eqOrIsNull(propertyAlias+".nomenclaturalTitle", person.getNomenclaturalTitle());
//              criteria.add(criterion2);

                if (StringUtils.isNotBlank(person.getNomenclaturalTitle())) {
                    Predicate nomTitlePred = cb.equal(join.get("nomenclaturalTitle"), person.getNomenclaturalTitle());
                    andPredicates.add(nomTitlePred);
                    //subCriteria.add(Restrictions.eq("nomenclaturalTitle", person.getNomenclaturalTitle()));
//                    subCriteria.add(Restrictions.eq("index()", i));
                    i++;
                }else {
//                    subCriteria.add(Restrictions.isNull("nomenclaturalTitle"));
                }
            }
            if (i>0) {
                break;
            }
        }
    }

    private Predicate createCriterion(CriteriaBuilder cb, Path<CdmBase> root, String propertyName,
            Object value, List<MatchMode> matchModes) throws MatchException {
        return createCriterion(cb, root.get(propertyName), value, matchModes);
    }

    private Predicate createCriterion(CriteriaBuilder cb, Path<Object> path,
			Object value, List<MatchMode> matchModes) throws MatchException {

	    Predicate eqPred = cb.equal(path, value);
	    Predicate nullPred = cb.isNull(path);
        Predicate finalPredicate = null;
	    if (this.requiresSecondValue(matchModes, value)){
	        finalPredicate = eqPred;
        }else if (requiresSecondNull(matchModes, value) ){
            finalPredicate = nullPred;
        }else{
            finalPredicate = cb.or(eqPred, nullPred);
        }
	    return finalPredicate;

//	    Criterion finalRestriction = null;
//		Criterion equalRestriction = Restrictions.eq(propertyName, value);
//		Criterion nullRestriction = Restrictions.isNull(propertyName);
//		if (this.requiresSecondValue(matchModes, value)){
//			finalRestriction = equalRestriction;
//		}else if (requiresSecondNull(matchModes, value) ){
//			finalRestriction = nullRestriction;
//		}else{
//			finalRestriction = Restrictions.or(equalRestriction, nullRestriction);
//		}
		//return finalRestriction;
//		criteria.add(finalRestriction);
	}

	private boolean requiresSecondNull(List<MatchMode> matchModes, Object value) throws MatchException {
		boolean result = true;
		for (MatchMode matchMode: matchModes){
			result &= matchMode.requiresSecondNull(value);
		}
		return result;
	}

	private boolean requiresSecondValue(List<MatchMode> matchModes, Object value) {
		boolean result = true;
		for (MatchMode matchMode: matchModes){
			result &= matchMode.requiresSecondValue(value);
		}
		return result;
	}

	private boolean isRequired(List<MatchMode> matchModes) {
		boolean result = true;
		for (MatchMode matchMode: matchModes){
			result &= matchMode.isRequired();
		}
		return result;
	}

	/**
	 * Returns true if at least one match mode is of type MATCH_XXX
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

	@Override
    public void saveMetaData(CdmMetaData cdmMetaData) {
		getSession().saveOrUpdate(cdmMetaData);
	}

	@Override
    public List<CdmMetaData> listCdmMetaData() {
	    return super.list(CdmMetaData.class);
	}

    @Override
    public <S extends CdmBase> Object initializeCollection(Class<S> clazz, UUID ownerUuid, String fieldName, List<String> appendedPropertyPaths)  {
        List<String> propertyPaths = new ArrayList<>();
        propertyPaths.add(fieldName);
        if(appendedPropertyPaths != null && !appendedPropertyPaths.isEmpty()) {
            for(String app : appendedPropertyPaths) {
                propertyPaths.add(fieldName + "." + app);
            }
        }
        S cdmBase = find(clazz, ownerUuid, propertyPaths);
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
    public <S extends CdmBase> Object initializeCollection(Class<S> clazz, UUID ownerUuid, String fieldName)  {
        return initializeCollection(clazz, ownerUuid, fieldName, null);
    }

    @Override
    public <S extends CdmBase> boolean isEmpty(Class<S> clazz, UUID ownerUuid, String fieldName) {
        Object col = initializeCollection(clazz, ownerUuid, fieldName);
        if(col instanceof Collection) {
            return ((Collection<?>)col).isEmpty();
        } else if(col instanceof Map){
            return ((Map<?,?>)col).isEmpty();
        }

        return false;
    }

    @Override
    public <S extends CdmBase> int size(Class<S> clazz, UUID ownerUuid, String fieldName) {
        Object col = initializeCollection(clazz, ownerUuid, fieldName);
        if(col instanceof Collection) {
            return ((Collection<?>)col).size();
        } else if(col instanceof Map){
            return ((Map<?,?>)col).size();
        }
        return 0;
    }

    @Override
    public <S extends CdmBase> Object get(Class<S> clazz, UUID ownerUuid, String fieldName, int index) {
        Object col = initializeCollection(clazz, ownerUuid, fieldName);
        if(col instanceof List) {
            return ((List<?>)col).get(index);
        } else {
            throw new IllegalArgumentException("Field name provided does not correspond to a list");
        }
    }

    @Override
    public <S extends CdmBase> boolean contains(Class<S> clazz, UUID ownerUuid, String fieldName, Object element) {
        Object col = initializeCollection(clazz, ownerUuid, fieldName);
        if(col instanceof Collection) {
            return ((Collection<?>)col).contains(element);
        } else {
            throw new IllegalArgumentException("Field name provided does not correspond to a collection");
        }
    }

    @Override
    public <S extends CdmBase> boolean containsKey(Class<S> clazz, UUID ownerUuid, String fieldName, Object key) {
        Object col = initializeCollection(clazz, ownerUuid, fieldName);
        if(col instanceof Map) {
            return ((Map<?,?>)col).containsKey(key);
        } else {
            throw new IllegalArgumentException("Field name provided does not correspond to a map");
        }
    }

    @Override
    public <S extends CdmBase> boolean containsValue(Class<S> clazz, UUID ownerUuid, String fieldName, Object value) {
        Object col = initializeCollection(clazz, ownerUuid, fieldName);
        if(col instanceof Map) {
            return ((Map<?,?>)col).containsValue(value);
        } else {
            throw new IllegalArgumentException("Field name provided does not correspond to a map");
        }
    }

    @Override
	public void createFullSampleData() {
		FullCoverageDataGenerator dataGenerator = new FullCoverageDataGenerator();
		dataGenerator.fillWithData(getSession());
	}

    @Override
    public List<UUID> listUuid(Class<? extends CdmBase> clazz) {
        String queryString = "SELECT uuid FROM " + clazz.getSimpleName();
        Query<UUID> query = getSession().createQuery(queryString, UUID.class);
        List<UUID> list = query.list();
        return list;
    }

    @Override
    public Set<CdmBase> saveAll(Set<CdmBase> cdmBases){
        Set<CdmBase> result = new HashSet<>();
        for (CdmBase cdmBase: cdmBases) {
            result.add(save(cdmBase));
        }
        return result;
    }


    @Override
    public UUID delete(CdmBase objectToDelete) throws DataAccessException {
        return super.delete_(objectToDelete);
    }

    @Override
    public <S extends CdmBase> CdmBase save(S newInstance) throws DataAccessException {
        return super.save_(newInstance);
    }

    @Override
    public UUID saveOrUpdate(CdmBase transientObject) throws DataAccessException {
        return super.saveOrUpdate_(transientObject);
    }

    @Override
    public UUID update(CdmBase transientObject) throws DataAccessException {
        return super.update_(transientObject);
    }

    @Override
    public long count(Class<? extends CdmBase> type) {
        return super.count_(type);
    }

    @Override
    public <S extends CdmBase> List<S> list(Class<S> clazz, Integer limit, Integer start, List<OrderHint> orderHints,
            List<String> propertyPaths) {
        return super.list_(clazz, limit, start, orderHints, propertyPaths);
    }

    @Override
    public UUID refresh(CdmBase persistentObject) throws DataAccessException {
        return super.refresh_(persistentObject);
    }
}