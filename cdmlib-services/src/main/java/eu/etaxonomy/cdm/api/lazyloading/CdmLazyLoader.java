package eu.etaxonomy.cdm.api.lazyloading;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.proxy.LazyInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.ICommonService;
import eu.etaxonomy.cdm.api.service.INameService;
import eu.etaxonomy.cdm.api.service.IReferenceService;
import eu.etaxonomy.cdm.model.common.CdmBase;


@Aspect
@Component
@Configurable(autowire = Autowire.BY_TYPE)
public class CdmLazyLoader {


	@Autowired
	private INameService nameService;

	@Autowired
	private IReferenceService referenceService;
	
	@Autowired
	private ICommonService commonService;

	@Pointcut("execution(* org.hibernate.proxy.AbstractLazyInitializer.initialize())")
	public void possibleLazyInitializationException() {
	}


	@Around(value = "possibleLazyInitializationException()")
	public Object preloadOnDemand(ProceedingJoinPoint pjp) throws Throwable {		 
		LazyInitializer ll = (LazyInitializer)pjp.getTarget();		  
		System.out.println("WOOORRRKKSSSS!!");
//		if(ll.getEntityName().equals("eu.etaxonomy.cdm.model.name.TaxonNameBase")) {
//			ll.setImplementation(nameService.find(((Integer)ll.getIdentifier()).intValue()));
//		} else if(ll.getEntityName().equals("eu.etaxonomy.cdm.model.reference.Reference")) {
//			ll.setImplementation(referenceService.find(((Integer)ll.getIdentifier()).intValue()));
//		}
		int classid = ((Integer)ll.getIdentifier()).intValue();
		System.out.print("AspectJ Loat-Time Waeaving " + ll.getEntityName() + " with id " + classid);
		ll.setImplementation(commonService.find((Class<? extends CdmBase>) Class.forName(ll.getEntityName()),classid));
		System.out.println("....Done");
		return pjp.proceed();		  	    	  
	}



}
