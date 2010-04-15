package eu.etaxonomy.cdm.remote.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.criteria.AuditCriterion;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IAuditEventService;
import eu.etaxonomy.cdm.api.service.IIdentifiableEntityService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.model.view.AuditEventRecord;
import eu.etaxonomy.cdm.persistence.dao.common.AuditEventSort;
import eu.etaxonomy.cdm.remote.dto.oaipmh.DeletedRecord;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Granularity;
import eu.etaxonomy.cdm.remote.dto.oaipmh.MetadataPrefix;
import eu.etaxonomy.cdm.remote.dto.oaipmh.ResumptionToken;
import eu.etaxonomy.cdm.remote.dto.oaipmh.SetSpec;
import eu.etaxonomy.cdm.remote.editor.DateTimeEditor;
import eu.etaxonomy.cdm.remote.editor.LSIDPropertyEditor;

public abstract class AbstractOaiPmhController<T extends IdentifiableEntity, SERVICE extends IIdentifiableEntityService<T>> {

    protected SERVICE service;

    @Autowired
    protected IAuditEventService auditEventService;

	private String repositoryName;

	private String baseURL;

	private String protocolVersion;

	private String adminEmail;

	private String description;

	private Integer pageSize;

    public abstract void setService(SERVICE service);
    
    private Cache cache;

    /**
     * sets cache name to be used
     */
    public void setCache(Cache cache) {
      this.cache = cache;
    }


    
    /**
     * Subclasses should override this method to return a list of property
     * paths that should be initialized for the getRecord, listRecords methods
     * @return
     */
    protected List<String> getPropertyPaths() {
    	return new ArrayList<String>();
    }
    
    /**
     * Subclasses should override this method and add a collection of 
     * eu.etaxonomy.cdm.remote.dto.oaipmh.Set objects  called "sets" that
     * will be returned in the response
     * @param modelAndView
     */
    protected void addSets(ModelAndView modelAndView) {
    	modelAndView.addObject("sets",new HashSet<SetSpec>());
    }

    public void setAuditEventService(IAuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }    

    public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	public void setProtocolVersion(String protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	
	@InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(DateTime.class, new DateTimeEditor());
        binder.registerCustomEditor(LSID.class, new LSIDPropertyEditor());
    }


	/**
     *  CannotDisseminateFormatException thrown by MetadataPrefixEditor
     * @throws IdDoesNotExistException 
     */
    @RequestMapping(method = RequestMethod.GET,params = "verb=GetRecord")
    public ModelAndView getRecord(@RequestParam(value = "identifier", required = true) LSID identifier,@RequestParam(value = "metadataPrefix", defaultValue = "rdf") MetadataPrefix metadataPrefix) throws IdDoesNotExistException {
 
        ModelAndView modelAndView = new ModelAndView();
        T object = service.find(identifier);
        Pager<AuditEventRecord<T>> results = service.pageAuditEvents(object,1,0,AuditEventSort.BACKWARDS,null); 

        if(results.getCount() == 0) {
	        throw new IdDoesNotExistException(identifier);
        }

        modelAndView.addObject("object",results.getRecords().get(0));

        switch(metadataPrefix) {
        case RDF:
            modelAndView.setViewName("oai/getRecord.rdf");
            break; 
	    case OAI_DC:
        default:
		    modelAndView.setViewName("oai/getRecord.dc");        
        }

        return modelAndView;
    }
    
    /**
     *  CannotDisseminateFormatException thrown by MetadataPrefixEditor
     * @throws IdDoesNotExistException 
     */
    @RequestMapping(method = RequestMethod.GET,params = "verb=ListMetadataFormats")
    public ModelAndView listMetadataFormats(@RequestParam(value = "identifier", required = false) LSID identifier) throws IdDoesNotExistException {
 
        ModelAndView modelAndView = new ModelAndView("oai/listMetadataFormats");
        T object = service.find(identifier);
        

        if(object == null) {
	        throw new IdDoesNotExistException(identifier);
        }

        return modelAndView;
    }
    
    /**
     *  CannotDisseminateFormatException thrown by MetadataPrefixEditor
     */
    @RequestMapping(method = RequestMethod.GET,params = "verb=ListSets")
    public ModelAndView listSets() {
 
        ModelAndView modelAndView = new ModelAndView("oai/listSets");
        
        addSets(modelAndView);

        return modelAndView;
    }

	@RequestMapping(method = RequestMethod.GET,params = "verb=Identify")
    public ModelAndView identify() {
        ModelAndView modelAndView = new ModelAndView("oai/identify");
        modelAndView.addObject("repositoryName", repositoryName);
        modelAndView.addObject("baseURL",baseURL);
        modelAndView.addObject("protocolVersion",protocolVersion);
        modelAndView.addObject("deletedRecord",DeletedRecord.PERSISTENT);
        modelAndView.addObject("granularity",Granularity.YYYY_MM_DD_THH_MM_SS_Z);

        List<AuditEvent> auditEvents = auditEventService.list(1,0,AuditEventSort.FORWARDS);
        modelAndView.addObject("earliestDatestamp",auditEvents.get(0).getDate());
        modelAndView.addObject("adminEmail",adminEmail);
        modelAndView.addObject("description",description);

        return modelAndView;
    }
 
    @RequestMapping(method = RequestMethod.GET, params = {"verb=ListIdentifiers", "!resumptionToken"})
    public ModelAndView listIdentifiers(@RequestParam("from") DateTime from, @RequestParam("until") DateTime until,@RequestParam(value = "metadataPrefix", defaultValue = "oai_dc") MetadataPrefix metadataPrefix, @RequestParam(value = "set", defaultValue = "nullSet") SetSpec set) {
 
        ModelAndView modelAndView = new ModelAndView("oai/listIdentifiers");
        modelAndView.addObject("metadataPrefix",metadataPrefix);

        AuditEvent fromAuditEvent = null;
        if(from != null) { // if from is specified, use the event at that date
            modelAndView.addObject("from",from);
            fromAuditEvent = auditEventService.find(from);
        } 
    
        AuditEvent untilAuditEvent = null;
        if(until != null) {
            modelAndView.addObject("until",until);
            untilAuditEvent = auditEventService.find(until);
        } 

        modelAndView.addObject("set",set);
        List<AuditCriterion> criteria = new ArrayList<AuditCriterion>();
        criteria.add(AuditEntity.property("lsid_lsid").isNotNull());
        Pager<AuditEventRecord<T>> results = service.pageAuditEvents((Class)set.getSetClass(),fromAuditEvent,untilAuditEvent,criteria, pageSize, 0, AuditEventSort.FORWARDS,null); 
        
        modelAndView.addObject("pager",results);

        if(results.getCount() > results.getRecords().size() && cache != null) {
	        ResumptionToken resumptionToken = new ResumptionToken(results, from, until, metadataPrefix, set);
            modelAndView.addObject("resumptionToken",resumptionToken);
            cache.put(new Element(resumptionToken.getValue(), resumptionToken));
        }

        return modelAndView;
    }

    @RequestMapping(method = RequestMethod.GET, params = {"verb=ListIdentifiers", "resumptionToken"})
    public ModelAndView listIdentifiers(@RequestParam(value = "resumptionToken",required = true) String rToken) {
    	ResumptionToken resumptionToken;
    	if(cache != null && cache.get(rToken) != null) {
    	    resumptionToken = (ResumptionToken) cache.get(rToken).getObjectValue();
            ModelAndView modelAndView = new ModelAndView("oai/listIdentifiers");
            modelAndView.addObject("metadataPrefix",resumptionToken.getMetadataPrefix());

            AuditEvent fromAuditEvent = null;
            if(resumptionToken.getFrom() != null) { // if from is specified, use the event at that date
                modelAndView.addObject("from",resumptionToken.getFrom());
                fromAuditEvent = auditEventService.find(resumptionToken.getFrom());
            } 
    
            AuditEvent untilAuditEvent = null;
            if(resumptionToken.getUntil() != null) {
                modelAndView.addObject("until",resumptionToken.getUntil());
                untilAuditEvent = auditEventService.find(resumptionToken.getUntil());
            }

            modelAndView.addObject("set",resumptionToken.getSet());
            List<AuditCriterion> criteria = new ArrayList<AuditCriterion>();
            criteria.add(AuditEntity.property("lsid_lsid").isNotNull());
            Pager<AuditEventRecord<T>> results = service.pageAuditEvents((Class)resumptionToken.getSet().getSetClass(),fromAuditEvent,untilAuditEvent,criteria, pageSize, resumptionToken.getCursor().intValue() + 1, AuditEventSort.FORWARDS,null); 
        
            modelAndView.addObject("pager",results);

            if(results.getCount() > ((results.getPageSize() * results.getCurrentIndex()) + results.getRecords().size())) {
	            resumptionToken.updateResults(results);
                modelAndView.addObject("resumptionToken",resumptionToken);
                cache.put(new Element(resumptionToken.getValue(), resumptionToken));
            } else {
                resumptionToken = ResumptionToken.emptyResumptionToken();
                modelAndView.addObject("resumptionToken",resumptionToken);
                cache.remove(rToken);
            }

            return modelAndView;
    	} else {
    		throw new BadResumptionTokenException();
    	}
    }

    @RequestMapping(method = RequestMethod.GET, params = {"verb=ListRecords", "!resumptionToken"})
    public ModelAndView listRecords(@RequestParam("from") DateTime from, @RequestParam("until") DateTime until,@RequestParam(value = "metadataPrefix", defaultValue = "oai_dc") MetadataPrefix metadataPrefix, @RequestParam(value = "set", defaultValue = "nullSet") SetSpec set) {
 
        ModelAndView modelAndView = null;
        modelAndView.addObject("metadataPrefix",metadataPrefix);
 
        switch(metadataPrefix) {
        case RDF:
            modelAndView.setViewName("oai/listRecords.rdf");
            break; 
	    case OAI_DC:
            default:
		    modelAndView.setViewName("oai/listRecords.dc");        
        }

        AuditEvent fromAuditEvent = null;
        if(from != null) { // if from is specified, use the event at that date
            modelAndView.addObject("from",from);
            fromAuditEvent = auditEventService.find(from);
        } 
    
        AuditEvent untilAuditEvent = null;
        if(until != null) {
            modelAndView.addObject("until",until);
            untilAuditEvent = auditEventService.find(until);
        } 

        modelAndView.addObject("set",set);
        List<AuditCriterion> criteria = new ArrayList<AuditCriterion>();
        criteria.add(AuditEntity.property("lsid_lsid").isNotNull());
        Pager<AuditEventRecord<T>> results = service.pageAuditEvents((Class)set.getSetClass(),fromAuditEvent,untilAuditEvent,criteria, pageSize, 0, AuditEventSort.FORWARDS,getPropertyPaths()); 
        
        modelAndView.addObject("pager",results);

        if(results.getCount() > results.getRecords().size() && cache != null) {
	        ResumptionToken resumptionToken = new ResumptionToken(results, from, until, metadataPrefix, set);
            modelAndView.addObject("resumptionToken",resumptionToken);
            cache.put(new Element(resumptionToken.getValue(), resumptionToken));
        }

        return modelAndView;
    }

	@RequestMapping(method = RequestMethod.GET, params = {"verb=ListRecords", "resumptionToken"})
    public ModelAndView listRecords(@RequestParam("resumptionToken") String rToken) {
 
	   ResumptionToken resumptionToken;
	   if(cache != null && cache.get(rToken) != null) {
   	        resumptionToken = (ResumptionToken) cache.get(rToken).getObjectValue();
            ModelAndView modelAndView = null;
            modelAndView.addObject("metadataPrefix",resumptionToken.getMetadataPrefix());
 
            switch (resumptionToken.getMetadataPrefix()) {
            case RDF:
                modelAndView.setViewName("oai/listRecords.rdf");
                break; 
	        case OAI_DC:
                default:
		        modelAndView.setViewName("oai/listRecords.dc");        
            }

            AuditEvent fromAuditEvent = null;
            if(resumptionToken.getFrom() != null) { // if from is specified, use the event at that date
                modelAndView.addObject("from",resumptionToken.getFrom());
                fromAuditEvent = auditEventService.find(resumptionToken.getFrom());
            }
    
            AuditEvent untilAuditEvent = null;
            if(resumptionToken.getUntil() != null) {
                modelAndView.addObject("until",resumptionToken.getUntil());
                untilAuditEvent = auditEventService.find(resumptionToken.getUntil());
            }
        
            modelAndView.addObject("set",resumptionToken.getSet());
            List<AuditCriterion> criteria = new ArrayList<AuditCriterion>();
            criteria.add(AuditEntity.property("lsid_lsid").isNotNull());
            Pager<AuditEventRecord<T>> results = service.pageAuditEvents((Class)resumptionToken.getSet().getSetClass(),fromAuditEvent,untilAuditEvent,criteria, pageSize, resumptionToken.getCursor().intValue() + 1, AuditEventSort.FORWARDS,getPropertyPaths()); 
        
            modelAndView.addObject("pager",results);

            if(results.getCount() > ((results.getPageSize() * results.getCurrentIndex()) + results.getRecords().size())) {
	            resumptionToken.updateResults(results);
                modelAndView.addObject("resumptionToken",resumptionToken);
                cache.put(new Element(resumptionToken.getValue(), resumptionToken));
            } else {
                resumptionToken = ResumptionToken.emptyResumptionToken();
                modelAndView.addObject("resumptionToken",resumptionToken);
                cache.remove(rToken);
            }

            return modelAndView;
	   } else {
		   throw new BadResumptionTokenException();
	   }
    }


}