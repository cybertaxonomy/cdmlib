/**
 *
 */
package eu.etaxonomy.cdm.persistence.dao.hibernate.common;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Reference;

import org.apache.commons.lang.UnhandledException;
import org.apache.log4j.Logger;
import org.hibernate.MappingException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.internal.SessionImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.collection.OneToManyPersister;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.type.AnyType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.EntityType;
import org.hibernate.type.SetType;
import org.hibernate.type.Type;

import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Extension;
import eu.etaxonomy.cdm.model.common.ICdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.Identifier;
import eu.etaxonomy.cdm.model.common.Marker;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.CdmGenericDaoImpl.ReferenceHolder;
import eu.etaxonomy.cdm.strategy.merge.ConvertMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.DefaultMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.IMergable;
import eu.etaxonomy.cdm.strategy.merge.IMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.MergeException;

/**
 * @author a.mueller
 *
 */
public class DeduplicationHelper {
	private static final Logger logger = Logger.getLogger(DeduplicationHelper.class);

	private final SessionImpl session;
	private final CdmGenericDaoImpl genericDao;

	protected DeduplicationHelper(SessionImpl session, CdmGenericDaoImpl genericDao){
		this.session = session;
		this.genericDao = genericDao;
	}

	public <T extends CdmBase>  boolean isMergeable(T cdmBase1, T cdmBase2, IMergeStrategy mergeStrategy) throws MergeException {
		Class<T> clazz = (Class<T>)cdmBase1.getClass();
		Class<T> clazz2 = (Class<T>)cdmBase2.getClass();

		SessionFactory sessionFactory = session.getSessionFactory();
		if (mergeStrategy == null){
			mergeStrategy = DefaultMergeStrategy.NewInstance(cdmBase1.getClass());
		}
		try {
			testMergeValid(cdmBase1, cdmBase2, mergeStrategy);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		} catch (NullPointerException e) {
			return false;
		}
	}


	public <T extends CdmBase>  void merge(T cdmBase1, T cdmBase2, IMergeStrategy mergeStrategy) throws MergeException {
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>)cdmBase1.getClass();
		@SuppressWarnings("unchecked")
		Class<T> clazz2 = (Class<T>)cdmBase2.getClass();

		SessionFactory sessionFactory = session.getSessionFactory();
		if (mergeStrategy == null){
			mergeStrategy = DefaultMergeStrategy.NewInstance(cdmBase1.getClass());
		}
		try {
			//test null and types
			testMergeValid(cdmBase1, cdmBase2, mergeStrategy);

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
				//TODO should we better use clazz2 here?
				mergeExternal(cdmBase1, cdmBase2, clazz, session);
			}


			if (cdmBase2.getId() > 0){
				session.saveOrUpdate(cdmBase2);
				//rearrange references pointing to cdmBase2 to cdmBase1 in future
				reallocateReferences(cdmBase1, cdmBase2, sessionFactory, clazz2, cloneSet);
			}

			//remove deleted objects

			//session.delete(null, mergable2, true, null);
			boolean deleteSecond = true;
			if (mergeStrategy instanceof ConvertMergeStrategy) {
			    deleteSecond = ((ConvertMergeStrategy)mergeStrategy).isDeleteSecondObject();
			}
			if(deleteSecond){
			    session.delete(cdmBase2);
			    for (ICdmBase toBeDeleted : deleteSet){
	                logger.debug("Delete " + toBeDeleted);
	                if (toBeDeleted != cdmBase2){
	                    session.delete(toBeDeleted);
	                }
	            }
			}

			//flush
			session.flush();

		} catch (Exception e) {
			e.printStackTrace();
		    throw new MergeException(e);
		}
	}

	/**
	 * Throws an exception if merge is not possible.
	 * @param cdmBase1
	 * @param cdmBase2
	 * @param strategy
	 * @throws IllegalArgumentException
	 * @throws NullPointerException
	 */

	private <T extends CdmBase> void testMergeValid(T cdmBase1, T cdmBase2, IMergeStrategy strategy)throws IllegalArgumentException, NullPointerException{
		if (cdmBase1 == null || cdmBase2 == null){
			throw new NullPointerException("Merge arguments must not be (null)");
		}
		cdmBase1 = HibernateProxyHelper.deproxy(cdmBase1);
		cdmBase2 = HibernateProxyHelper.deproxy(cdmBase2);


		if (cdmBase1.getClass() != cdmBase2.getClass()){
			boolean reallocationPossible = testOnlyReallocationAllowed(cdmBase1.getClass(), cdmBase2.getClass());
			if (! reallocationPossible){
				String msg = "Merge not possible for objects of type %s and type %s";
				msg = String.format(msg, cdmBase1.getClass().getSimpleName(), cdmBase2.getClass().getSimpleName());
				throw new IllegalArgumentException("Merge not possible for objects of type %s and type %s");
			}else{
				if (! testInstancesMergeable(cdmBase1, cdmBase2, strategy)){
					throw new IllegalArgumentException("Object can not be merged into new object as it is referenced in a way that does not allow merging");
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends CdmBase> boolean testInstancesMergeable(T cdmBase1, T cdmBase2, IMergeStrategy strategy){
		Class<T> clazz2 = (Class<T>)cdmBase2.getClass();
		//FIXME do we need to compute the cloneSet? See #reallocateReferences for comparison
		//this is only a copy from there. Maybe we do not need the cloneSet at all here
		Set<ICdmBase> cloneSet = new HashSet<ICdmBase>();
		Set<ReferenceHolder> holderSet;
		try {
			boolean result = true;
			holderSet = genericDao.getOrMakeHolderSet(clazz2);
			for (ReferenceHolder refHolder: holderSet){
				if (! reallocateByHolderPossible(cdmBase1, cdmBase2, refHolder, cloneSet)){
					return false;
				}
			}
			return result;
		} catch (ClassNotFoundException e) {
			return false;
		} catch (NoSuchFieldException e) {
			return false;
		} catch (MergeException e) {
			return false;
		}
	}

	/**
	 * Test if 2 classes
	 * @param class1
	 * @param class2
	 * @return
	 */
	private boolean  testOnlyReallocationAllowed(Class<? extends CdmBase> class1,
			Class<? extends CdmBase> class2) {
		if (class1 == class2){
			return true;
		}else{
			if (classesAssignableFrom(TeamOrPersonBase.class, class1, class2)){
				return true;
			}else if (classesAssignableFrom(TaxonNameBase.class, class1, class2)){
				return true;
			}else{
				return false;
			}
		}
	}

	private boolean classesAssignableFrom(Class<? extends CdmBase> superClass,
			Class<? extends CdmBase> class1, Class<? extends CdmBase> class2) {
		return superClass.isAssignableFrom(class1) && superClass.isAssignableFrom(class2);
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

		Map<String, ClassMetadata> allClassMetadata = sessionFactory.getAllClassMetadata();

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
//		Statistics statistics = sessionFactory.getStatistics();
		logger.debug("");
		ClassMetadata taxonMetaData = sessionFactory.getClassMetadata(Taxon.class);
		String ename = taxonMetaData.getEntityName();
		try {
			Reference ref = sessionFactory.getReference();
			logger.debug("");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//sessionFactory.get
		ClassMetadata classMetadata = session.getSessionFactory().getClassMetadata(clazz);
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
			copyAnnotatableExtensions(cdmBase1, cdmBase2, session);
		}
		if (cdmBase1 instanceof IdentifiableEntity){
			copyIdentifiableExtensions(cdmBase1, cdmBase2, session);
		}

		session.saveOrUpdate(cdmBase1);
		session.saveOrUpdate(cdmBase2);
		session.flush();
	}


	/**
	 * Clones all annotations and markers of cdmBase2 and
	 * attaches the clones to cdmBase1.
	 * Finally removes all annotations and markers from cdmBase2.
	 * @param cdmBase1
	 * @param cdmBase2
	 * @param session
	 */
	private <T> void copyAnnotatableExtensions(T cdmBase1, T cdmBase2,
			Session session) {
		AnnotatableEntity annotatableEntity1 = (AnnotatableEntity)cdmBase1;
		AnnotatableEntity annotatableEntity2 = (AnnotatableEntity)cdmBase2;
		//annotations
		List<Annotation> removeListAnnotation = new ArrayList<Annotation>();
		for (Annotation annotation : annotatableEntity2.getAnnotations()){
			Annotation clone = null;
			try {
				clone = (Annotation)annotation.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			annotatableEntity1.addAnnotation(clone);
			removeListAnnotation.add(annotation);
		}
		for (Annotation annotation : removeListAnnotation){
			annotatableEntity2.removeAnnotation(annotation);
			session.delete(annotation);
		}

		//marker
		List<Marker> removeListMarker = new ArrayList<Marker>();
		for (Marker marker : annotatableEntity2.getMarkers()){
			Marker clone = null;
			try {
				clone = (Marker)marker.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			annotatableEntity1.addMarker(clone);
			removeListMarker.add(marker);
		}
		for (Marker marker : removeListMarker){
			annotatableEntity2.removeMarker(marker);
			session.delete(marker);
		}
	}

	/**
	 * Clones all extensions   of cdmBase2 and
	 * attaches the clones to cdmBase1.
	 * Finally removes all annotations and markers from cdmBase2.
	 * @param cdmBase1
	 * @param cdmBase2
	 * @param session
	 */
	//TODO Why do we not handle credits (credits are reusable), rights and sources here
	private <T> void copyIdentifiableExtensions(T cdmBase1, T cdmBase2,
			Session session) {
		IdentifiableEntity identifiableEntity1 = (IdentifiableEntity)cdmBase1;
		IdentifiableEntity identifiableEntity2 = (IdentifiableEntity)cdmBase2;

		//extensions
		List<Extension> removeListExtension = new ArrayList<Extension>();
		for (Extension changeObject : (Set<Extension>)identifiableEntity2.getExtensions()){
			try {
				Extension clone = (Extension)changeObject.clone();
				identifiableEntity1.addExtension(clone);
				removeListExtension.add(changeObject);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}

		}
		for (Extension removeObject : removeListExtension){
			identifiableEntity2.removeExtension(removeObject);
			session.delete(removeObject);
		}

		//identifiers
		List<Identifier> removeListIdentifier = new ArrayList<Identifier>();
		for (Identifier<?> changeObject : (List<Identifier>)identifiableEntity2.getIdentifiers()){
			try {
				Identifier<?> clone = (Identifier)changeObject.clone();
				identifiableEntity1.addIdentifier(clone);
				removeListIdentifier.add(changeObject);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
		for (Identifier removeObject : removeListIdentifier){
			identifiableEntity2.removeIdentifier(removeObject);
			session.delete(removeObject);
		}

	}

	private void reallocateReferences(CdmBase cdmBase1, CdmBase cdmBase2, SessionFactory sessionFactory, Class clazz, Set<ICdmBase> cloneSet){
		try {
			Set<ReferenceHolder> holderSet = genericDao.getOrMakeHolderSet(clazz);
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
	private boolean reallocateByHolderPossible(CdmBase cdmBase1, CdmBase cdmBase2, ReferenceHolder refHolder, Set<ICdmBase> cloneSet) throws MergeException {
		try {
			if (refHolder.isCollection()){
				return reallocateCollectionPossible(cdmBase1, cdmBase2, refHolder, cloneSet);
			}else{
				return reallocateSingleItemPossible(cdmBase1, cdmBase2, refHolder, cloneSet);
			}
		} catch (Exception e) {
			throw new MergeException("Error during reallocation of references to merge object: " + cdmBase2, e);
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

	private boolean reallocateCollectionPossible(CdmBase cdmBase1, CdmBase cdmBase2,
			ReferenceHolder refHolder, Set<ICdmBase> cloneSet) throws MergeException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Class<?> targetClass = refHolder.targetClass;
		Class<?> clazz1 = cdmBase1.getClass();
		if (! targetClass.isAssignableFrom(clazz1)){
			//FIXME only do count or hasXXX, we do not need to instantiate objects here
			List<CdmBase> referencingObjects = genericDao.getCdmBasesWithItemInCollection(refHolder.itemClass, refHolder.otherClass, refHolder.propertyName, cdmBase2);
			if (! referencingObjects.isEmpty()){
				return false;
			}
		}
		return true;
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
		List<CdmBase> list = genericDao.getCdmBasesWithItemInCollection(refHolder.itemClass, refHolder.otherClass, refHolder.propertyName, cdmBase2);
		for (CdmBase referencingObject : list){
			Field referencingField = getFieldRecursive(refHolder.otherClass, refHolder.propertyName);
			referencingField.setAccessible(true);
			Object collection = referencingField.get(referencingObject);
			if (! (collection instanceof Collection)){
				throw new MergeException ("Reallocation of collections for collection other than set and list not yet implemented");
			}else if (collection instanceof List){
			    Method replaceMethod = DefaultMergeStrategy.getReplaceMethod(referencingField);
			    replaceMethod.invoke(referencingObject, cdmBase1, cdmBase2);
			}else{
			    Method addMethod = DefaultMergeStrategy.getAddMethod(referencingField, false);
			    Method removeMethod = DefaultMergeStrategy.getAddMethod(referencingField, true);
			    addMethod.invoke(referencingObject, cdmBase1);
			    removeMethod.invoke(referencingObject, cdmBase2);
			}
		}
	}

	private boolean reallocateSingleItemPossible(CdmBase cdmBase1, CdmBase cdmBase2, ReferenceHolder refHolder, Set<ICdmBase> cloneSet) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Class<?> targetClass = refHolder.targetClass;
		Class<?> clazz1 = cdmBase1.getClass();
		if (! targetClass.isAssignableFrom(clazz1)){
			//FIXME only do count or hasXXX, we do not need to instantiate objects here
			List<CdmBase> referencingObjects = genericDao.getCdmBasesByFieldAndClass(refHolder.otherClass, refHolder.propertyName, cdmBase2);
			if (! referencingObjects.isEmpty()){
				return false;
			}
		}
		return true;
	}

	private void reallocateSingleItem(CdmBase cdmBase1, CdmBase cdmBase2, ReferenceHolder refHolder, Set<ICdmBase> cloneSet) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		List<CdmBase> referencingObjects = genericDao.getCdmBasesByFieldAndClass(refHolder.otherClass, refHolder.propertyName, cdmBase2);
		for (CdmBase referencingObject : referencingObjects){
			if (!cloneSet.contains(referencingObject)){
		        String className = refHolder.otherClass.getSimpleName();
	            String propertyName = refHolder.propertyName;
		        String hql = "UPDATE " + className + " c SET c."+propertyName+" = :newValue WHERE c.id = :id";
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
		List<CdmBase> referencingObjects = genericDao.getCdmBasesByFieldAndClass(refHolder.otherClass, refHolder.propertyName, cdmBase2);
		for (CdmBase referencingObject : referencingObjects){
			Field referencingField = refHolder.otherClass.getDeclaredField(refHolder.propertyName);
			referencingField.setAccessible(true);
			Object test = referencingField.get(referencingObject);
			assert(test.equals(cdmBase2));
			referencingField.set(referencingObject, cdmBase1);
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


	private <T extends CdmBase> void makeMergeProperty(T cdmBase1, T cdmBase2, Type propertyType, String propertyName, SessionFactoryImpl sessionFactory, boolean isCollection) throws MergeException{

		try {
			Class<T> clazz = (Class<T>)cdmBase1.getClass();
			if (CdmGenericDaoImpl.isNoDoType(propertyType)){
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
				Type elType = collectionType.getElementType(sessionFactory);
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
					if (! CdmGenericDaoImpl.isNoDoType(subType)){
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



}
