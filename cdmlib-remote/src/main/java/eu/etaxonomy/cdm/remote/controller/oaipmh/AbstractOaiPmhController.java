package eu.etaxonomy.cdm.remote.controller.oaipmh;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.criteria.AuditCriterion;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springmodules.cache.CachingModel;
import org.springmodules.cache.provider.CacheProviderFacade;

import eu.etaxonomy.cdm.api.service.IAuditEventService;
import eu.etaxonomy.cdm.api.service.IIdentifiableEntityService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.view.AuditEvent;
import eu.etaxonomy.cdm.model.view.AuditEventRecord;
import eu.etaxonomy.cdm.persistence.dao.common.AuditEventSort;
import eu.etaxonomy.cdm.remote.controller.BadResumptionTokenException;
import eu.etaxonomy.cdm.remote.controller.IdDoesNotExistException;
import eu.etaxonomy.cdm.remote.dto.oaipmh.DeletedRecord;
import eu.etaxonomy.cdm.remote.dto.oaipmh.ErrorCode;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Granularity;
import eu.etaxonomy.cdm.remote.dto.oaipmh.MetadataPrefix;
import eu.etaxonomy.cdm.remote.dto.oaipmh.ResumptionToken;
import eu.etaxonomy.cdm.remote.dto.oaipmh.SetSpec;
import eu.etaxonomy.cdm.remote.dto.oaipmh.Verb;
import eu.etaxonomy.cdm.remote.editor.IsoDateTimeEditor;
import eu.etaxonomy.cdm.remote.editor.LSIDPropertyEditor;
import eu.etaxonomy.cdm.remote.editor.MetadataPrefixEditor;
import eu.etaxonomy.cdm.remote.editor.SetSpecEditor;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;
import eu.etaxonomy.cdm.remote.exception.CannotDisseminateFormatException;
import eu.etaxonomy.cdm.remote.exception.NoRecordsMatchException;

public abstract class AbstractOaiPmhController<T extends IdentifiableEntity, SERVICE extends IIdentifiableEntityService<T>> {

    protected SERVICE service;

    protected IAuditEventService auditEventService;

    private String repositoryName;

    private String baseURL;

    private String protocolVersion;

    private String adminEmail;

    private String description;

    private Integer pageSize;

    public abstract void setService(SERVICE service);

    private CacheProviderFacade cacheProviderFacade;

    private CachingModel cachingModel;

    private boolean onlyItemsWithLsid = false;

    public boolean isRestrictToLsid() {
        return onlyItemsWithLsid;
    }

    public void setRestrictToLsid(boolean restrictToLsid) {
        this.onlyItemsWithLsid = restrictToLsid;
    }

    /**
     * sets cache name to be used
     */
    @Autowired
    public void setCacheProviderFacade(CacheProviderFacade cacheProviderFacade) {
        this.cacheProviderFacade = cacheProviderFacade;
    }

    @Autowired
    public void setCachingModel(CachingModel cachingModel) {
        this.cachingModel = cachingModel;
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

    @Autowired
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
        binder.registerCustomEditor(ZonedDateTime.class, new IsoDateTimeEditor());
        binder.registerCustomEditor(LSID.class, new LSIDPropertyEditor());
        binder.registerCustomEditor(MetadataPrefix.class, new MetadataPrefixEditor());
        binder.registerCustomEditor(SetSpec.class, new SetSpecEditor());
        binder.registerCustomEditor(UUID.class, new UUIDPropertyEditor());
    }


    /**
     * CannotDisseminateFormatException thrown by MetadataPrefixEditor
     *
     * @throws IdDoesNotExistException
     */
// FIXME has same mapping as the other getRecord method: do we really need to support LSIDs or shall we skip this
//    @RequestMapping(method = RequestMethod.GET, params = "verb=GetRecord")
//    public ModelAndView getRecord(
//            @RequestParam(value = "identifier", required = true) LSID identifier,
//            @RequestParam(value = "metadataPrefix", required = true) MetadataPrefix metadataPrefix)
//            throws IdDoesNotExistException {
//
//        ModelAndView modelAndView = new ModelAndView();
//        modelAndView.addObject("metadataPrefix", metadataPrefix);
//
//        finishModelAndView(identifier, metadataPrefix, modelAndView);
//
//        return modelAndView;
//    }

    @RequestMapping(method = RequestMethod.GET, params = "verb=GetRecord")
    public ModelAndView getRecord(
            @RequestParam(value = "identifier", required = true) UUID identifier,
            @RequestParam(value = "metadataPrefix", required = true) MetadataPrefix metadataPrefix)
            throws IdDoesNotExistException {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("metadataPrefix", metadataPrefix);

        return modelAndView;
    }

    /**
     * @param identifier
     * @param metadataPrefix
     * @param modelAndView
     * @throws IdDoesNotExistException
     */
    protected void finishModelAndView(LSID identifier,
            MetadataPrefix metadataPrefix, ModelAndView modelAndView)
            throws IdDoesNotExistException {

        switch (metadataPrefix) {
            case RDF:
                modelAndView.addObject("object", obtainCdmEntity(identifier));
                modelAndView.setViewName("oai/getRecord.rdf");
                break;
            case OAI_DC:
            default:
                modelAndView.addObject("object", obtainCdmEntity(identifier));
                modelAndView.setViewName("oai/getRecord.dc");
        }
    }

    /**
     * @param identifier
     * @return
     * @throws IdDoesNotExistException
     */
    protected AuditEventRecord<T> obtainCdmEntity(LSID identifier)
            throws IdDoesNotExistException {
        T object = service.find(identifier);
        if(object == null){
            throw new IdDoesNotExistException(identifier);
        }

        Pager<AuditEventRecord<T>> results = service.pageAuditEvents(object, 1,
                0, AuditEventSort.BACKWARDS, getPropertyPaths());

        if (results.getCount() == 0) {
            throw new IdDoesNotExistException(identifier);
        }
        return results.getRecords().get(0);
    }


    /**
     *  CannotDisseminateFormatException thrown by MetadataPrefixEditor
     * @throws IdDoesNotExistException
     */
    @RequestMapping(method = RequestMethod.GET,params = "verb=ListMetadataFormats")
    public ModelAndView listMetadataFormats(@RequestParam(value = "identifier", required = false) LSID identifier) throws IdDoesNotExistException {

        ModelAndView modelAndView = new ModelAndView("oai/listMetadataFormats");

        if(identifier != null) {
            T  object = service.find(identifier);
            if(object == null) {
                throw new IdDoesNotExistException(identifier);
            }
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

        Pager<AuditEvent> auditEvents = auditEventService.list(0,1,AuditEventSort.FORWARDS);
        modelAndView.addObject("earliestDatestamp",auditEvents.getRecords().get(0).getDate());
        modelAndView.addObject("adminEmail",adminEmail);
        modelAndView.addObject("description",description);

        return modelAndView;
    }

    @RequestMapping(method = RequestMethod.GET, params = {"verb=ListIdentifiers", "!resumptionToken"})
    public ModelAndView listIdentifiers(
            @RequestParam(value = "from", required = false) ZonedDateTime from,
            @RequestParam(value = "until", required = false) ZonedDateTime until,
            @RequestParam(value = "metadataPrefix",required = true) MetadataPrefix metadataPrefix,
            @RequestParam(value = "set", required = false) SetSpec set) {

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

        Class clazz = null;
        if(set != null) {
            modelAndView.addObject("set",set);
            clazz = set.getSetClass();
        }

        List<AuditCriterion> criteria = new ArrayList<>();
        if(onlyItemsWithLsid){
            //criteria.add(AuditEntity.property("lsid_lsid").isNotNull());
            //TODO this isNotNull criterion did not work with mysql, so using a like statement as interim solution
            criteria.add(AuditEntity.property("lsid_lsid").like("urn:lsid:%"));
        }
        Pager<AuditEventRecord<T>> results = service.pageAuditEvents(clazz, fromAuditEvent, untilAuditEvent, criteria, pageSize, 0, AuditEventSort.FORWARDS, null);

        if(results.getCount() == 0) {
            throw new NoRecordsMatchException("No records match");
        }

        modelAndView.addObject("pager",results);

        if(results.getCount() > results.getRecords().size() && cacheProviderFacade != null) {
            ResumptionToken resumptionToken = new ResumptionToken(results, from, until, metadataPrefix, set);
            modelAndView.addObject("resumptionToken",resumptionToken);
            cacheProviderFacade.putInCache(resumptionToken.getValue(), cachingModel, resumptionToken);
        }

        return modelAndView;
    }

    @RequestMapping(method = RequestMethod.GET, params = {"verb=ListIdentifiers", "resumptionToken"})
    public ModelAndView listIdentifiers(@RequestParam(value = "resumptionToken",required = true) String rToken) {
        ResumptionToken resumptionToken;
        if(cacheProviderFacade != null && cacheProviderFacade.getFromCache(rToken, cachingModel) != null) {
            resumptionToken = (ResumptionToken) cacheProviderFacade.getFromCache(rToken, cachingModel);
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

            Class clazz = null;
            if(resumptionToken.getSet() != null) {
                modelAndView.addObject("set",resumptionToken.getSet());
                clazz = resumptionToken.getSet().getSetClass();
            }

            List<AuditCriterion> criteria = new ArrayList<AuditCriterion>();
            if(onlyItemsWithLsid){
                //criteria.add(AuditEntity.property("lsid_lsid").isNotNull());
                //TODO this isNotNull criterion did not work with mysql, so using a like statement as interim solution
                criteria.add(AuditEntity.property("lsid_lsid").like("urn:lsid:%"));
            }
            Pager<AuditEventRecord<T>> results = service.pageAuditEvents(clazz,fromAuditEvent,untilAuditEvent,criteria, pageSize, (resumptionToken.getCursor().intValue() / pageSize) + 1, AuditEventSort.FORWARDS,null);

            if(results.getCount() == 0) {
                throw new NoRecordsMatchException("No records match");
            }

            modelAndView.addObject("pager",results);

            if(results.getCount() > ((results.getPageSize() * results.getCurrentIndex()) + results.getRecords().size())) {
                resumptionToken.updateResults(results);
                modelAndView.addObject("resumptionToken",resumptionToken);
                cacheProviderFacade.putInCache(resumptionToken.getValue(),cachingModel, resumptionToken);
            } else {
                resumptionToken = ResumptionToken.emptyResumptionToken();
                modelAndView.addObject("resumptionToken",resumptionToken);
                cacheProviderFacade.removeFromCache(rToken,cachingModel);
            }

            return modelAndView;
        } else {
            throw new BadResumptionTokenException();
        }
    }

    @RequestMapping(method = RequestMethod.GET, params = {"verb=ListRecords", "!resumptionToken"})
    public ModelAndView listRecords(@RequestParam(value = "from", required = false) ZonedDateTime from,
            @RequestParam(value = "until", required = false) ZonedDateTime until,
            @RequestParam(value = "metadataPrefix", required = true) MetadataPrefix metadataPrefix,
            @RequestParam(value = "set", required = false) SetSpec set) {

        ModelAndView modelAndView = new ModelAndView();
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

        Class clazz = null;
        if(set != null) {
            modelAndView.addObject("set",set);
            clazz = set.getSetClass();
        }

        List<AuditCriterion> criteria = new ArrayList<AuditCriterion>();
        if(onlyItemsWithLsid){
            //criteria.add(AuditEntity.property("lsid_lsid").isNotNull());
            //TODO this isNotNull criterion did not work with mysql, so using a like statement as interim solution
            criteria.add(AuditEntity.property("lsid_lsid").like("urn:lsid:%"));
        }
        Pager<AuditEventRecord<T>> results = service.pageAuditEvents(clazz, fromAuditEvent, untilAuditEvent, criteria, pageSize, 0, AuditEventSort.FORWARDS, getPropertyPaths());

        if(results.getCount() == 0) {
            throw new NoRecordsMatchException("No records match");
        }

        modelAndView.addObject("pager",results);

        if(results.getCount() > results.getRecords().size() && cacheProviderFacade != null) {
            ResumptionToken resumptionToken = new ResumptionToken(results, from, until, metadataPrefix, set);
            modelAndView.addObject("resumptionToken",resumptionToken);
            cacheProviderFacade.putInCache(resumptionToken.getValue(), cachingModel, resumptionToken);
        }

        return modelAndView;
    }

    @RequestMapping(method = RequestMethod.GET, params = {"verb=ListRecords", "resumptionToken"})
    public ModelAndView listRecords(@RequestParam("resumptionToken") String rToken) {

       ResumptionToken resumptionToken;
       if(cacheProviderFacade != null && cacheProviderFacade.getFromCache(rToken,cachingModel) != null) {
               resumptionToken = (ResumptionToken) cacheProviderFacade.getFromCache(rToken,cachingModel);
            ModelAndView modelAndView = new ModelAndView();
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

            Class clazz = null;
            if(resumptionToken.getSet() != null) {
              modelAndView.addObject("set",resumptionToken.getSet());
              clazz = resumptionToken.getSet().getSetClass();
            }
            List<AuditCriterion> criteria = new ArrayList<AuditCriterion>();
            if(onlyItemsWithLsid){
                //criteria.add(AuditEntity.property("lsid_lsid").isNotNull());
                //TODO this isNotNull criterion did not work with mysql, so using a like statement as interim solution
                criteria.add(AuditEntity.property("lsid_lsid").like("urn:lsid:%"));
            }
            Pager<AuditEventRecord<T>> results = service.pageAuditEvents(clazz,fromAuditEvent,untilAuditEvent,criteria, pageSize, (resumptionToken.getCursor().intValue()  / pageSize) + 1, AuditEventSort.FORWARDS,getPropertyPaths());

            if(results.getCount() == 0) {
                throw new NoRecordsMatchException("No records match");
            }

            modelAndView.addObject("pager",results);

            if(results.getCount() > ((results.getPageSize() * results.getCurrentIndex()) + results.getRecords().size())) {
                resumptionToken.updateResults(results);
                modelAndView.addObject("resumptionToken",resumptionToken);
                cacheProviderFacade.putInCache(resumptionToken.getValue(),cachingModel,resumptionToken);
            } else {
                resumptionToken = ResumptionToken.emptyResumptionToken();
                modelAndView.addObject("resumptionToken",resumptionToken);
                cacheProviderFacade.removeFromCache(rToken,cachingModel);
            }

            return modelAndView;
       } else {
           throw new BadResumptionTokenException();
       }
    }

    private ModelAndView doException(Exception ex, HttpServletRequest request, ErrorCode code) {
        ModelAndView modelAndView = new ModelAndView("oai/exception");
        modelAndView.addObject("message", ex.getMessage());
        if(request.getParameter("verb") != null) {
            try {
              modelAndView.addObject("verb", Verb.fromValue(request.getParameter("verb")));
            } catch(Exception e) {// prevent endless recursion

            }
        }
        modelAndView.addObject("code",code);
        return modelAndView;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class,TypeMismatchException.class,MissingServletRequestParameterException.class})
    public ModelAndView handleBadArgument(Exception ex, HttpServletRequest request) {
        return doException(ex,request,ErrorCode.BAD_ARGUMENT);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CannotDisseminateFormatException.class)
    public ModelAndView handleCannotDisseminateFormat(Exception ex, HttpServletRequest request) {
        return doException(ex,request,ErrorCode.CANNOT_DISSEMINATE_FORMAT);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadResumptionTokenException.class)
    public ModelAndView handleBadResumptionToken(Exception ex, HttpServletRequest request) {
        return doException(ex,request,ErrorCode.BAD_RESUMPTION_TOKEN);
    }

    @ExceptionHandler(NoRecordsMatchException.class)
    public ModelAndView handleNoRecordsMatch(Exception ex, HttpServletRequest request) {
        return doException(ex,request,ErrorCode.NO_RECORDS_MATCH);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(IdDoesNotExistException.class)
    public ModelAndView handleIdDoesNotExist(Exception ex, HttpServletRequest request) {
        return doException(ex,request,ErrorCode.ID_DOES_NOT_EXIST);
    }


}
