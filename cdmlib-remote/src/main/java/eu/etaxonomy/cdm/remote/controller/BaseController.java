/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.mapping.Map;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IClassificationService;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.api.service.ITaxonNodeService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IPublishable;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;

/**
 * based on org.cateproject.controller.common
 * @author b.clark
 * @author a.kohlbecker
 */
public abstract class BaseController<T extends CdmBase, SERVICE extends IService<T>>
        extends AbstractController<T, SERVICE> {

    private static final Logger logger = LogManager.getLogger();

    protected Class<T> baseClass;

    @SuppressWarnings("unchecked")
    public BaseController (){

       //define base class   //TODO can't we do this more straight forward e.g.
                             //by an abstract method returning the class?
       Type superClass = this.getClass().getGenericSuperclass();
       while(true){
           if(superClass instanceof ParameterizedType){
               ParameterizedType parametrizedSuperClass = (ParameterizedType) superClass;
               Type[] typeArguments = parametrizedSuperClass.getActualTypeArguments();

               if(typeArguments.length > 1 && typeArguments[0] instanceof Class<?>){
                   baseClass = (Class<T>) typeArguments[0];
               } else {
                   logger.error("unable to find baseClass");
               }
               break;
           } else if(superClass instanceof Class<?>){
               superClass = ((Class<?>) superClass).getGenericSuperclass();
           } else {
               // no point digging deeper if neither Class or ParameterizedType
               logger.error("unable to find baseClass");
               break;
           }
       }
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(UUID.class, new UUIDPropertyEditor());
    }

    //TODO implement bulk version of this method
    @RequestMapping(method = RequestMethod.GET)
    public T doGet(@PathVariable("uuid") UUID uuid,
                HttpServletRequest request,
                HttpServletResponse response) throws IOException {
        if(request != null) {
            logger.info("doGet() " + request.getRequestURI());
        }
        T obj = getCdmBaseInstance(uuid, response, initializationStrategy);
        if (obj instanceof IPublishable){
            obj = (T)checkExistsAndAccess((IPublishable)obj, NO_UNPUBLISHED, response);
        }
        return obj;
    }

    /**
     * @param uuid
     * @param request
     * @param response
     * @return
     * @throws IOException
     *
     * TODO implement bulk version of this method
     */
    @RequestMapping(value = "*", method = RequestMethod.GET)
    public Object doGetMethod(
            @PathVariable("uuid") UUID uuid,
            // doPage request parameters
            @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            // doList request parameters
            @RequestParam(value = "start", required = false) Integer start,
            @RequestParam(value = "limit", required = false) Integer limit,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        String servletPath = request.getServletPath();
        String propertyName = FilenameUtils.getBaseName(servletPath);

        logger.info("doGetMethod()[doGet" + StringUtils.capitalize(propertyName) + "] " + requestPathAndQuery(request));

        // <CUT
//		T instance = getCdmBaseInstance(uuid, response, (List<String>)null);
        //Class<?> propertyClass = propertyClass(instance, baseName);
        Object objectFromProperty = getCdmBaseProperty(uuid, propertyName, response);//   invokeProperty(instance, baseName, response);
        // CUT>
        if(objectFromProperty != null){
            if( Collection.class.isAssignableFrom(objectFromProperty.getClass())){
                // Map types cannot be returned as list or in a pager!
                return pageFromCollection((Collection<CdmBase>)objectFromProperty, pageIndex, pageSize, start, limit, response);
            } else {
                return objectFromProperty;
            }
        }
        return null;
    }

    /**
     * Returns a sub-collection of <code>c</code>. A pager object will be returned if the <code>pageNumber</code> and
     * <code>pageSize</code> are given. Otherwise a <code>List</code> in case of <code>start</code> and <code>limit</code>.
     *
     * @param pageNumber
     * @param pageSize
     * @param start
     * @param limit
     * @param response
     * @param objectFromProperty
     * @return either a List or Pager depending on the parameter combination.
     *
     * @throws IOException
     */
    protected Object pageFromCollection(Collection<? extends CdmBase> c, Integer pageNumber, Integer pageSize, Integer start,
            Integer limit, HttpServletResponse response) throws IOException {

        if(c instanceof Set){
            // sets need to be sorted to have a defined order
            List<CdmBase> list = new ArrayList<>(c);
            java.util.Collections.sort(list, (o1, o2)-> {
                    if (o1 == null && o2 == null){
                        return 0;
                    }else if (o1 == null){
                        return -1;
                    }else if (o2 == null){
                        return 1;
                    }
                    return Integer.compare(o1.getId(), o2.getId());
                }
            );
            c = list;
        }

        if(start != null){
            // return list
            limit = (limit == null ? DEFAULT_PAGE_SIZE : limit);
            Collection<CdmBase> sub_c = subCollection(c, start, limit);
            return sub_c;
        } else {
            //FIXME use real paging mechanism of according service class instead of subCollection()
            //FIXME use BaseListController.normalizeAndValidatePagerParameters(pageNumber, pageSize, response);
            Pager<? extends CdmBase> p = pagerForSubCollectionOf(c, pageNumber, pageSize, response);
            return p;
        }
    }

    public Object getCdmBaseProperty(UUID uuid, String property, HttpServletResponse response) throws IOException{

        T instance = HibernateProxyHelper.deproxy(getCdmBaseInstance(uuid, response, property));

        Object objectFromProperty = invokeProperty(instance, property, response);

        return objectFromProperty;
    }

    @SuppressWarnings("unchecked")
    protected final <SUB_T extends T> SUB_T getCdmBaseInstance(Class<SUB_T> clazz,
            UUID uuid, HttpServletResponse response, List<String> pathProperties)
            throws IOException {

        CdmBase cdmBaseObject = getCdmBaseInstance(uuid, response, pathProperties);
        if(!clazz.isAssignableFrom(cdmBaseObject.getClass())){
            HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
            return null;
        }
        return (SUB_T) cdmBaseObject;
    }

    @SuppressWarnings("unchecked")
    protected final <SUB_T extends T> SUB_T getCdmBaseInstance(Class<SUB_T> clazz, UUID uuid, HttpServletResponse response, String pathProperty)
            throws IOException {

        CdmBase cdmBaseObject = getCdmBaseInstance(uuid, response, pathProperty);
        if(!clazz.isAssignableFrom(cdmBaseObject.getClass())){
            HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
        }
        return (SUB_T) cdmBaseObject;
    }

    protected final T getCdmBaseInstance(UUID uuid, HttpServletResponse response, String pathProperty)
            throws IOException {
        return getCdmBaseInstance(baseClass, uuid, response, Arrays
                .asList(new String[] { pathProperty }));
    }

    protected final T getCdmBaseInstance(UUID uuid, HttpServletResponse response, List<String> pathProperties)
            throws IOException {
        return getCdmBaseInstance(baseClass, service, uuid, response, pathProperties);
    }

    protected final <CDM_BASE extends CdmBase> CDM_BASE getCdmBaseInstance(
            Class<CDM_BASE> clazz, IService<CDM_BASE> service, UUID uuid,
            HttpServletResponse response, List<String> pathProperties)
            throws IOException {

        @SuppressWarnings("unused")
        boolean includeUnpublished = NO_UNPUBLISHED;
        CDM_BASE cdmBaseObject;
//        if (service instanceof IPublishableService){
//            cdmBaseObject = ((IPublishableService<CDM_BASE>)service).load(uuid, includeUnpublished, pathProperties);
//        }else{
            pathProperties = complementInitStrategy(clazz, pathProperties);
            cdmBaseObject = service.load(uuid, pathProperties);
//        }
        if (cdmBaseObject == null) {
            HttpStatusMessage.UUID_NOT_FOUND.send(response);
        }
        return cdmBaseObject;
    }

    /**
     * Implementations of the BaseController can override this method to
     * extend the <code>pathProperties</code> to for example avoid
     * <code>LazyInitializationExceptions</code> which can happen when
     * {@link #doGetMethod(UUID, Integer, Integer, Integer, Integer, HttpServletRequest, HttpServletResponse)} is being used.
     *
     * @param clazz
     * @param pathProperties
     */
    protected  <CDM_BASE extends CdmBase> List<String> complementInitStrategy(@SuppressWarnings("unused") Class<CDM_BASE> clazz, List<String> pathProperties) {
        return pathProperties;
    }

    private final Object invokeProperty(T instance,
            String baseName, HttpServletResponse response) throws IOException {

        Object result = null;
        try {
            PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(instance, baseName);
            if(propertyDescriptor == null){
                throw new NoSuchMethodException("No such method: " + instance.getClass().getSimpleName() + ".get" + baseName);
            }
            Method method = propertyDescriptor.getReadMethod();

            Class<?> returnType = method.getReturnType();

            if(CdmBase.class.isAssignableFrom(returnType)
                    || Collection.class.isAssignableFrom(returnType)
                    || Map.class.isAssignableFrom(returnType)
                    || INomenclaturalReference.class.isAssignableFrom(returnType)){

                result = method.invoke(instance, (Object[])null);

                result = HibernateProxyHelper.deproxy(result);

            }else{
                HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
            }
        } catch (SecurityException e) {
            logger.error("SecurityException: ", e);
            HttpStatusMessage.INTERNAL_ERROR.send(response);
        } catch (NoSuchMethodException e) {
            HttpStatusMessage.PROPERTY_NOT_FOUND.send(response);
        } catch (IllegalArgumentException e) {
            HttpStatusMessage.PROPERTY_NOT_FOUND.send(response);
        } catch (IllegalAccessException e) {
            HttpStatusMessage.PROPERTY_NOT_FOUND.send(response);
        } catch (InvocationTargetException e) {
            HttpStatusMessage.PROPERTY_NOT_FOUND.send(response);
        }
        return result;
    }

    /**
     * Checks if an {@link IPublishable} was found and if it is publish.
     * If not the according {@link HttpStatusMessage http messages} are added to response.
     * @param publishable
     * @param includeUnpublished
     * @param response
     * @return
     * @throws IOException
     */
    protected <S extends IPublishable> S checkExistsAndAccess(S publishable, boolean includeUnpublished,
            HttpServletResponse response) throws IOException {
        if (publishable == null){
            HttpStatusMessage.UUID_NOT_FOUND.send(response);
        }else if (!includeUnpublished && !publishable.isPublish()){
            HttpStatusMessage.ACCESS_DENIED.send(response);
            publishable = null;
        }
        return publishable;
    }

    protected <S extends IPublishable> S checkExistsAccessType(IPublishable publishable, boolean includeUnpublished,
            Class<S> clazz, HttpServletResponse response) throws IOException {
        IPublishable result = this.checkExistsAndAccess(publishable, includeUnpublished, response);
        if (clazz != null && !clazz.isAssignableFrom(result.getClass())){
            HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
            result = null;
        }
        return (S)result;
    }

    protected TaxonNode getSubtreeOrError(UUID subtreeUuid, ITaxonNodeService taxonNodeService, HttpServletResponse response) throws IOException {
        TaxonNode subtree = null;
        if (subtreeUuid != null){
            subtree = taxonNodeService.find(subtreeUuid);
            if(subtree == null) {
                response.sendError(404 , "Taxon node not found using " + subtreeUuid );
                //will not happen
                return null;
            }
        }
        return subtree;
    }

    protected Classification getClassificationOrError(UUID classificationUuid,
            IClassificationService classificationService, HttpServletResponse response) throws IOException {
        Classification classification = null;
        if (classificationUuid != null){
            classification = classificationService.find(classificationUuid);
            if(classification == null) {
                response.sendError(404 , "Classification not found: " + classificationUuid );
                //will not happen
                return null;
            }
        }
        return classification;
    }

      /* TODO implement

      private Validator validator;

      private javax.validation.Validator javaxValidator;

      @RequestMapping(method = RequestMethod.PUT, headers="content-type=multipart/form-data")
      public T doPutForm(@PathVariable(value = "uuid") UUID uuid, @ModelAttribute("object") T object, BindingResult result) {
          object.setUuid(uuid);
          validator.validate(object, result);
          if (result.hasErrors()) {
              throw new Error();
                // set http status code depending upon what happened, possibly return
                // the put object and errors so that they can be rendered into a suitable error response
          } else {
             // requires merging detached object ?gilead?
             service.save(object);
          }

            return object;
      }

      @RequestMapping(method = RequestMethod.PUT, headers="content-type=text/json")
      public T doPutJSON(@PathVariable(value = "uuid") UUID uuid, @RequestBody String jsonMessage) {
          JSONObject jsonObject = JSONObject.fromObject(jsonMessage);
          T object = (T)JSONObject.toBean(jsonObject, this.getClass());


          Set<ConstraintViolation<T>> constraintViolations = javaxValidator.validate(object);
            if (!constraintViolations.isEmpty()) {
                throw new Error();
                    // set http status code depending upon what happened, possibly return
                // the put object and errors so that they can be rendered into a suitable error response
            } else {
              // requires merging detached object ?gilead?
              service.save(object);
            }

            return object;
      }

      @RequestMapping(method = RequestMethod.PUT) // the cdm-server may not allow clients to specify the uuid for resources
      public T doPut(@PathVariable(value = "uuid") UUID uuid, @ModelAttribute("object") T object, BindingResult result) {
            validator.validate(object, result);
            if (result.hasErrors()) {
                    // set http status code depending upon what happened, possibly return
                // the put object and errors so that they can be rendered into a suitable error response
            } else {
              service.save(object);
            }
      }

       @RequestMapping(method = RequestMethod.DELETE)
       public void doDelete(@PathVariable(value = "uuid") UUID uuid) {
           T object = service.find(uuid);
           // provided the object exists
           service.delete(uuid);
           // might return 204 or 200
       }
    }
*/
}