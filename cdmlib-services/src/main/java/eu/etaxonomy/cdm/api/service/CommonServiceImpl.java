// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.CdmMetaData;
import eu.etaxonomy.cdm.model.common.CdmMetaData.MetaDataPropertyName;
import eu.etaxonomy.cdm.model.common.ISourceable;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.OriginalSourceBase;
import eu.etaxonomy.cdm.persistence.dao.common.ICdmGenericDao;
import eu.etaxonomy.cdm.persistence.dao.common.IOriginalSourceDao;
import eu.etaxonomy.cdm.strategy.match.DefaultMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchStrategy;
import eu.etaxonomy.cdm.strategy.match.IMatchable;
import eu.etaxonomy.cdm.strategy.match.MatchException;
import eu.etaxonomy.cdm.strategy.merge.DefaultMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.IMergable;
import eu.etaxonomy.cdm.strategy.merge.IMergeStrategy;
import eu.etaxonomy.cdm.strategy.merge.MergeException;


@Service
@Transactional(readOnly = true)
public class CommonServiceImpl extends ServiceBase<OriginalSourceBase,IOriginalSourceDao> implements ICommonService {
	private static final Logger logger = Logger.getLogger(CommonServiceImpl.class);
	
	@Autowired
	IOriginalSourceDao originalSourceDao;
	
	@Autowired
	ICdmGenericDao genericDao;


	@Autowired
	protected void setDao(IOriginalSourceDao dao) {
		this.dao = dao;
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ICommonService#getSourcedObjectsByIdInSource(java.lang.Class, java.util.List, java.lang.String)
	 */
	public Map<String, ? extends ISourceable> getSourcedObjectsByIdInSource(Class clazz, Set<String> idInSourceSet, String idNamespace) {
		Map<String, ? extends ISourceable> list = originalSourceDao.findOriginalSourcesByIdInSource(clazz, idInSourceSet, idNamespace);
		return list;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ICommonService#getSourcedObjectById(java.lang.String, java.lang.String)
	 */
	public ISourceable getSourcedObjectByIdInSource(Class clazz, String idInSource, String idNamespace) {
		ISourceable result = null;
		List<IdentifiableEntity> list = originalSourceDao.findOriginalSourceByIdInSource(clazz, idInSource, idNamespace);
		if (! list.isEmpty()){
			result = list.get(0);
		}return result;
	}
	
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ICommonService#getReferencingObjects(eu.etaxonomy.cdm.model.common.CdmBase)
	 */
	public Set<CdmBase> getReferencingObjects(CdmBase referencedCdmBase){
		return this.genericDao.getReferencingObjects(referencedCdmBase);
	}	
//		try {
//			Set<Class<? extends CdmBase>> allCdmClasses = genericDao.getAllCdmClasses(false); //findAllCdmClasses();
//			
//			referencedCdmBase = (CdmBase)HibernateProxyHelper.deproxy(referencedCdmBase);
//			Class referencedClass = referencedCdmBase.getClass();
//			Set<CdmBase> result = new HashSet<CdmBase>();
//			logger.debug("Referenced Class: " + referencedClass.getName());
//			
//			for (Class<? extends CdmBase> cdmClass : allCdmClasses){
//				Set<Field> fields = getFields(cdmClass);
//				for (Field field: fields){
//					Class<?> type = field.getType();
//					//class
//					if (! type.isInterface()){
//						if (referencedClass.isAssignableFrom(type)|| 
//								type.isAssignableFrom(referencedClass) && CdmBase.class.isAssignableFrom(type)){
//							handleSingleClass(referencedClass, type, field, cdmClass, result, referencedCdmBase, false);
//						}
//					//interface
//					}else if (type.isAssignableFrom(referencedClass)){
//							handleSingleClass(referencedClass, type, field, cdmClass, result, referencedCdmBase, false);
//					}else if (Collection.class.isAssignableFrom(type)){
//						
//						if (checkIsSetOfType(field, referencedClass, type) == true){
//							handleSingleClass(referencedClass, type, field, cdmClass, result, referencedCdmBase, true);	
//						}
//					}
////				Class[] interfaces = referencedClass.getInterfaces();
////				for (Class interfaze: interfaces){
////					if (interfaze == type){
//////					if(interfaze.isAssignableFrom(returnType)){
////						handleSingleClass(interfaze, type, field, cdmClass, result, referencedCdmBase);
////					}
////				}
//				}	
//			}
//			return result;
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new RuntimeException(e);
//		}
//		
//	}
//	
//	private boolean checkIsSetOfType(Field field, Class referencedClass, Class<?> type){
//		Type genericType = (ParameterizedTypeImpl)field.getGenericType();
//		if (genericType instanceof ParameterizedTypeImpl){
//			ParameterizedTypeImpl paraType = (ParameterizedTypeImpl)genericType;
//			paraType.getRawType();
//			Type[] arguments = paraType.getActualTypeArguments();
//			//logger.debug(arguments.length);
//			if (arguments.length == 1){
//				Class collectionClass;
//				try {
//					if (arguments[0] instanceof Class){
//						collectionClass = (Class)arguments[0];
//					}else if(arguments[0] instanceof TypeVariableImpl){
//						TypeVariableImpl typeVariable = (TypeVariableImpl)arguments[0];
//						GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
//						collectionClass = (Class)genericDeclaration;
//					}else{
//						logger.warn("Unknown Type");
//						return false;
//					}
//					if (CdmBase.class.isAssignableFrom(collectionClass) && collectionClass.isAssignableFrom(referencedClass)  ){
//						return true;
//					}
//				} catch (Exception e) {
//					logger.warn(e.getMessage());
//				}
//			}else{
//				logger.warn("Length of arguments <> 1");
//			}
//		}else{
//			logger.warn("Not a generic type of type ParameterizedTypeImpl");
//		}
//		return false;
//	}
//	
//	
//	
//	
//	private boolean handleSingleClass(Class itemClass, Class type, Field field, Class cdmClass, Set<CdmBase> result,CdmBase value, boolean isCollection){
//		if (! Modifier.isStatic(field.getModifiers())){
//			String methodName = StringUtils.rightPad(field.getName(), 30);
//			String className = StringUtils.rightPad(cdmClass.getSimpleName(), 30);
//			String returnTypeName = StringUtils.rightPad(type.getSimpleName(), 30);
//			
//			logger.debug(methodName +   "\t\t" + className + "\t\t" + returnTypeName);
////			result_old.add(method);
//			result.addAll(getCdmBasesByFieldAndClass(field, itemClass, cdmClass, value, isCollection));
//		}
//		return true;
//	}
//	
//	private Set<Field> getFields(Class clazz){
//		Set<Field> result = new HashSet<Field>();
//		for (Field field: clazz.getDeclaredFields()){
//			if (!Modifier.isStatic(field.getModifiers())){
//				result.add(field);	
//			}
//		}
//		Class superclass = clazz.getSuperclass();
//		if (CdmBase.class.isAssignableFrom(superclass)){
//			result.addAll(getFields(superclass));
//		}
//		return result;
//	}
//	
//	private Set<CdmBase> getCdmBasesByFieldAndClass(Field field, Class itemClass, Class otherClazz, CdmBase item, boolean isCollection){
//		Set<CdmBase> result = new HashSet<CdmBase>();
//		if (isCollection){
//			result.addAll(genericDao.getCdmBasesWithItemInCollection(itemClass, otherClazz, field.getName(), item));
//		}else{
//			result.addAll(genericDao.getCdmBasesByFieldAndClass(otherClazz, field.getName(), item));
//		}
//		return result;
//	}
	
	public List getHqlResult(String hqlQuery){
		return genericDao.getHqlResult(hqlQuery);
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ICommonService#merge(eu.etaxonomy.cdm.strategy.merge.IMergable, eu.etaxonomy.cdm.strategy.merge.IMergable, eu.etaxonomy.cdm.strategy.merge.IMergeStragegy)
	 */
	public <T extends IMergable> void merge(T mergeFirst, T mergeSecond, IMergeStrategy mergeStrategy) throws MergeException {
		if (mergeStrategy == null){
			mergeStrategy = DefaultMergeStrategy.NewInstance(((CdmBase)mergeFirst).getClass());
		}
		genericDao.merge((CdmBase)mergeFirst, (CdmBase)mergeSecond, mergeStrategy);
	}


	public <T extends IMatchable> List<T> findMatching(T objectToMatch, IMatchStrategy matchStrategy) throws MatchException {
		if (matchStrategy == null){
			matchStrategy = DefaultMatchStrategy.NewInstance(((objectToMatch).getClass()));
		}
		return genericDao.findMatching(objectToMatch, matchStrategy);
	}
	
	
	
//	/* (non-Javadoc)
//	 * @see eu.etaxonomy.cdm.api.service.IService#list(java.lang.Class, java.lang.Integer, java.lang.Integer, java.util.List, java.util.List)
//	 */
//	public <TYPE extends OriginalSourceBase> Pager<TYPE> list(Class<TYPE> type,
//			Integer pageSize, Integer pageNumber, List<OrderHint> orderHints,
//			List<String> propertyPaths) {
//		logger.warn("Not yet implemented");
//		return null;
//	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ICommonService#saveAll(java.util.Collection)
	 */
	@Transactional(readOnly = false)
	public void saveAllMetaData(Collection<CdmMetaData> metaData) {
		Iterator<CdmMetaData> iterator = metaData.iterator();
		while(iterator.hasNext()){
			CdmMetaData cdmMetaData = iterator.next();
			genericDao.saveMetaData(cdmMetaData);
		}
	}

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.api.service.ICommonService#getCdmMetaData()
	 */
	public Map<MetaDataPropertyName, CdmMetaData> getCdmMetaData() {
		Map<MetaDataPropertyName, CdmMetaData> result = new HashMap<MetaDataPropertyName, CdmMetaData>();
		List<CdmMetaData> metaDataList = genericDao.getMetaData();
		for (CdmMetaData metaData : metaDataList){
			MetaDataPropertyName propertyName = metaData.getPropertyName();
			result.put(propertyName, metaData);
		}
		return result;
	}

}
