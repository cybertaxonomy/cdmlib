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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.mapping.Map;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.etaxonomy.cdm.api.service.IPublishableService;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.hibernate.HibernateProxyHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IPublishable;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;
import eu.etaxonomy.cdm.remote.editor.UUIDPropertyEditor;

/**
 * based on org.cateproject.controller.common
 * @author b.clark
 * @author a.kohlbecker
 *
 * @param <T>
 * @param <SERVICE>
 */

public abstract class BaseController<T extends CdmBase, SERVICE extends IService<T>> extends AbstractController<T, SERVICE> {

/*	protected SERVICE service;

    public abstract void setService(SERVICE service);*/

    protected Class<T> baseClass;

    @SuppressWarnings("unchecked")
    public BaseController (){

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
            // doPage request parametes
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            // doList request parametes
            @RequestParam(value = "start", required = false) Integer start,
            @RequestParam(value = "limit", required = false) Integer limit,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        String servletPath = request.getServletPath();
        String baseName = FilenameUtils.getBaseName(servletPath);

        if(request != null) {
            logger.info("doGetMethod()[doGet" + StringUtils.capitalize(baseName) + "] " + requestPathAndQuery(request));
        }

        // <CUT
//		T instance = getCdmBaseInstance(uuid, response, (List<String>)null);
        //Class<?> propertyClass = propertyClass(instance, baseName);
        Object objectFromProperty = getCdmBaseProperty(uuid, baseName, response);//   invokeProperty(instance, baseName, response);
        // CUT>
        if(objectFromProperty != null){
            if( Collection.class.isAssignableFrom(objectFromProperty.getClass())){
                // Map types cannot be returned as list or in a pager!
                return pageFromCollection((Collection<CdmBase>)objectFromProperty, pageNumber, pageSize, start, limit, response);
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
            java.util.Collections.sort(list, new Comparator<CdmBase>() {

                @Override
                public int compare(CdmBase o1, CdmBase o2) {
                    if (o1 == null && o2 == null){
                        return 0;
                    }else if (o1 == null){
                        return -1;
                    }else if (o2 == null){
                        return 1;
                    }
                    return Integer.compare(o1.getId(), o2.getId());
                }
            });
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
            PagerParameters pagerParameters = new PagerParameters(pageSize, pageNumber);
            pagerParameters.normalizeAndValidate(response);

            start = pagerParameters.getPageIndex() * pagerParameters.getPageSize();
            List sub_c = subCollection(c, start, pagerParameters.getPageSize());
            Pager p = new DefaultPagerImpl(pageNumber, c.size(), pagerParameters.getPageSize(), sub_c);
            return p;
        }
    }

    public Object getCdmBaseProperty(UUID uuid, String property, HttpServletResponse response) throws IOException{

        T instance = HibernateProxyHelper.deproxy(getCdmBaseInstance(uuid, response, property));

        Object objectFromProperty = invokeProperty(instance, property, response);

        return objectFromProperty;
    }

    private Class<?> propertyClass(T instance, String baseName) {
        PropertyDescriptor propertyDescriptor = null;
        Class<?> c = null;
        try {
            propertyDescriptor = PropertyUtils.getPropertyDescriptor(instance, baseName);
            if(propertyDescriptor != null){
                c =  propertyDescriptor.getClass();
            }
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return c;
    }

    /**
     * @param <SUB_T>
     * @param clazz
     * @param uuid
     * @param response
     * @param pathProperties
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    protected final <SUB_T extends T> SUB_T getCdmBaseInstance(Class<SUB_T> clazz, UUID uuid, HttpServletResponse response, List<String> pathProperties)
            throws IOException {

        CdmBase cdmBaseObject = getCdmBaseInstance(uuid, response, pathProperties);
        if(!clazz.isAssignableFrom(cdmBaseObject.getClass())){
            HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
        }
        return (SUB_T) cdmBaseObject;
    }

    /**
     * @param <SUB_T>
     * @param clazz
     * @param uuid
     * @param response
     * @param pathProperty
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    protected final <SUB_T extends T> SUB_T getCdmBaseInstance(Class<SUB_T> clazz, UUID uuid, HttpServletResponse response, String pathProperty)
            throws IOException {

        CdmBase cdmBaseObject = getCdmBaseInstance(uuid, response, pathProperty);
        if(!clazz.isAssignableFrom(cdmBaseObject.getClass())){
            HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
        }
        return (SUB_T) cdmBaseObject;
    }

    /**
     * @param uuid
     * @param response
     * @param pathProperty
     * @return
     * @throws IOException
     */
    protected final T getCdmBaseInstance(UUID uuid, HttpServletResponse response, String pathProperty)
            throws IOException {
        return getCdmBaseInstance(baseClass, uuid, response, Arrays
                .asList(new String[] { pathProperty }));
    }


    /**
     * @param uuid
     * @param response
     * @param pathProperties
     * @return
     * @throws IOException
     */
    protected final T getCdmBaseInstance(UUID uuid, HttpServletResponse response, List<String> pathProperties)
            throws IOException {
        return getCdmBaseInstance(baseClass, service, uuid, response, pathProperties);
    }

    /**
     * @param <CDM_BASE>
     * @param clazz
     * @param service
     * @param uuid
     * @param response
     * @param pathProperties
     * @return
     * @throws IOException
     */
    protected final <CDM_BASE extends CdmBase> CDM_BASE getCdmBaseInstance(Class<CDM_BASE> clazz, IService<CDM_BASE> service, UUID uuid, HttpServletResponse response, List<String> pathProperties)
            throws IOException {

        boolean includeUnpublished = NO_UNPUBLISHED;
        CDM_BASE cdmBaseObject;
        if (service instanceof IPublishableService){
            cdmBaseObject = ((IPublishableService<CDM_BASE>)service).load(uuid, includeUnpublished, pathProperties);
        }else{
            cdmBaseObject = service.load(uuid, pathProperties);
        }
        if (cdmBaseObject == null) {
            HttpStatusMessage.UUID_NOT_FOUND.send(response);
        }
        return cdmBaseObject;
    }

    /**
     * @param instance
     * @param baseName
     * @param response
     * @return
     * @throws IOException
     */
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

    private <E> List<E> subCollection(Collection<? extends E> c, Integer start, Integer length){
        List<E> sub_c = new ArrayList<E>(length);
        if(c.size() > length){
            E[] a = (E[]) c.toArray();
            for(int i = start; i < start + length; i++){
                sub_c.add(a[i]);
            }
        } else {
            sub_c.addAll(c);
        }
        return sub_c;

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
    protected <T extends IPublishable> T checkExistsAndAccess(T publishable, boolean includeUnpublished,
            HttpServletResponse response) throws IOException {
        if (publishable == null){
            HttpStatusMessage.UUID_NOT_FOUND.send(response);
        }else if (!includeUnpublished && !publishable.isPublish()){
            HttpStatusMessage.ACCESS_DENIED.send(response);
            publishable = null;
        }
        return publishable;
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
