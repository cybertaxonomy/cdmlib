package eu.etaxonomy.cdm.api.lazyloading;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.collection.internal.PersistentList;
import org.hibernate.collection.internal.PersistentMap;
import org.hibernate.collection.internal.PersistentSet;
import org.hibernate.collection.internal.PersistentSortedMap;
import org.hibernate.collection.internal.PersistentSortedSet;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.PersistentMultiLanguageText;


@Aspect
@Component
@Configurable(autowire = Autowire.BY_TYPE)
public class CdmLazyLoader {


	private Set classes = new HashSet();
	
	public static boolean enableWeaving = true;
	private static Set<String> classesToIgnore = new HashSet<String>();
	
	
	@Autowired
	private ICommonService commonService;

	public CdmLazyLoader() {
		classesToIgnore.add("eu.etaxonomy.cdm.model.common.TermVocabulary");
		classesToIgnore.add("eu.etaxonomy.cdm.model.common.OrderedTermVocabulary");
	}
	
	/**
	 *  Point cut for the 'initialize' method of the AbstractLazyInitializer.
	 *  
	 */
	@Pointcut("execution(* org.hibernate.proxy.AbstractLazyInitializer.initialize())")
	public void possibleEntityLazyInitializationException() {
	}


	/**
	 *  'Around' advice for the initialization of CDM Entity Objects
	 *  
	 */
	@Around(value = "possibleEntityLazyInitializationException()")
	public Object preloadEntityOnDemand(ProceedingJoinPoint pjp) throws Throwable {		 
		if(enableWeaving) {
			LazyInitializer ll = (LazyInitializer)pjp.getTarget();		
			if(ll.isUninitialized()) {
				int classid = ((Integer)ll.getIdentifier()).intValue();
				System.out.print("--> AspectJ Load-Time Weaving " + ll.getEntityName() + " with id " + classid);
				Class clazz = (Class<? extends CdmBase>) Class.forName(ll.getEntityName());
				CdmBase cdmBase = CdmBase.deproxy(commonService.find(clazz,classid),clazz);
				ll.setImplementation(cdmBase);
				System.out.println("....Done");
			}
		}
		return pjp.proceed();		  	    	  
	}
	

	/**
	 *  Point cut for the 'initialize' method of the AbstractPersistentCollection.
	 *  
	 */
	@Pointcut("execution(protected final void org.hibernate.collection.internal.AbstractPersistentCollection.initialize(..))")
	public void possibleCollectionLazyInitializationException() {
	}
	
	/**
	 *  'Around' advice for the initialization of Collection objects
	 *  
	 */
	@Around(value = "possibleCollectionLazyInitializationException()")
	public Object preloadCollectionOnDemand(ProceedingJoinPoint pjp) throws Throwable {		 
		if(enableWeaving) {
			PersistentCollection ps = (PersistentCollection) pjp.getTarget();
			if (ps.getOwner() != null && !classesToIgnore.contains(ps.getOwner().getClass().getName()) && !ps.wasInitialized() &&  !classes.contains(ps.getKey())) {
				System.out.print("--> AspectJ Load-Time Weaving " + ps.getRole());                
				classes.add(ps.getKey());
				try {
					String role = ps.getRole();
					String fieldName = role.substring(role.lastIndexOf(".") + 1);
					System.out.println("field : " + fieldName);
					Object owner = ps.getOwner();

					PersistentCollection col = commonService.initializeCollection(ps); 
					ps.afterInitialize();

					Class<?> clazz = ps.getClass();
					if (clazz != null) {			      
						Field field = clazz.getDeclaredField("set");
						field.setAccessible(true);
						field.set(ps, convertToCollectionOrMap(col));			       
					}		
				} catch (Exception ex) {
					ex.printStackTrace();
					System.out.println("Error in ReattachSessionAspect : " + ex.getMessage());
				} finally {
					classes.remove(ps.getKey());
					System.out.println("....Done");
				}	
			}
		} 
		return pjp.proceed();

	}
	
	private Object convertToCollectionOrMap(PersistentCollection pc) {
		if(pc != null) {
			if(pc instanceof PersistentSet) {
				return new HashSet((Set)pc);
			}
			if(pc instanceof PersistentSortedSet) {
				return new TreeSet((Set)pc);
			}
			if(pc instanceof PersistentList) {
				return new ArrayList((List)pc);
			}
			if(pc instanceof PersistentMap || pc instanceof PersistentMultiLanguageText) {
				return new HashMap((Map)pc);
			}
			if(pc instanceof PersistentSortedMap) {
				return new TreeMap((Map)pc);
			}
		}
		return null;
	}

}
