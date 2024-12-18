/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.api.service.pager.impl.DefaultPagerImpl;
import eu.etaxonomy.cdm.api.util.UserHelper;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.persistence.dao.hibernate.common.DaoBase;
import eu.etaxonomy.cdm.remote.controller.util.PagerParameters;

/**
 * Abstract controller class for CDM Rest service controllers which return entities or DTOs
 * from the underlying data base. Implementations of this class are primarily bound to a
 * specific cdm service class and thus are operating primarily on a specific cdm base type.
 *
 * This class guarantees consistent use of init-strategies and harmonizes the logging of full
 * request urls with query parameters.
 *
 * @author a.kohlbecker
 * @since 23.06.2009
 *
 * @param <T>
 * @param <SERVICE>
 */
public abstract class AbstractController<T extends CdmBase, SERVICE extends IService<T>> {

    protected static final List<String> DEFAULT_INIT_STRATEGY = Arrays.asList(new String []{
            "$"
    });
    protected static final Integer DEFAULT_PAGE_SIZE = PagerParameters.DEFAULT_PAGESIZE;

    /**
     * Default thread priority for long term processes which are running in
     * separate threads. These batch processes are usually monitored with the
     * {@link ProgressMonitorController}. This value must be lower than
     * {@link Thread#NORM_PRIORITY}
     */
    public static final int DEFAULT_BATCH_THREAD_PRIORITY = 3;

    protected static final boolean NO_UNPUBLISHED = DaoBase.NO_UNPUBLISHED;
    protected static final boolean INCLUDE_UNPUBLISHED = DaoBase.INCLUDE_UNPUBLISHED;

    protected SERVICE service;

    @Autowired
    protected UserHelper userHelper;

    public abstract void setService(SERVICE service);

    protected List<String> initializationStrategy = DEFAULT_INIT_STRATEGY;

    /**
     * Set the default initialization strategy for this controller.
     *
     * @param initializationStrategy
     */
    public final void setInitializationStrategy(List<String> initializationStrategy) {
        this.initializationStrategy = initializationStrategy;
    }

    /**
     * Provides access to the default initialization strategy.
     * The default initialization strategy is predefined for all controllers in
     * {@link #DEFAULT_INIT_STRATEGY} but can be altered by
     * concrete implementations by utilizing {@link #setInitializationStrategy(List)}
     * in the constructor of the specific controller.
     *
     * @return the default initialization strategy
     */
    public final List<String> getInitializationStrategy() {
        return this.initializationStrategy;
    }

    /**
     * Returns the HTTP request path and query parameters as string
     *
     * @param request
     * @return request path and query parameters as string.
     */
    public static String requestPathAndQuery(HttpServletRequest request) {
        if(request == null) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        b.append(request.getMethod()).append(": ");
        b.append(request.getRequestURI());
        String query = request.getQueryString();
        if(query != null) {
            b.append("?").append(query);
        }

        return b.toString();
    }

    /**
     * This method is useful to read path parameters from request urls in methods where the method has been annotated with a
     * {@link RequestMapping} having wildcards as trailing characters like in <code>@RequestMapping("identifier/**")</code>.
     * <p>
     * Reads the path part following pattern passed as <code>basePath</code> and returns it is urldecoded String.
     * The <code>basepath</code> usually is the combination of the class level and method level RequestMappings e.g.:
     * <code>"/registration/identifier/"</code>
     *
     * @param basePath
     *      The base path of the controller method.
     * @param request
     * @return
     */
    protected String readPathParameter(HttpServletRequest request, String basePath) {
        String pathParameter = request.getRequestURI().replaceFirst("^(?:.*)" + basePath , "");
        if(pathParameter != null){
            try {
                pathParameter = java.net.URLDecoder.decode(pathParameter, "UTF-8");
                pathParameter = pathParameter.replaceAll("\\.json$|\\.xml$", "");
            } catch (UnsupportedEncodingException e) {
                // should never happen
                throw new RuntimeException(e);
            }
        }
        return pathParameter;
    }

    protected <T extends CdmBase> Pager<T> pagerForSubCollectionOf(Collection<T> c, Integer pageNumber, Integer pageSize, HttpServletResponse response) throws IOException {
        PagerParameters pagerParameters = new PagerParameters(pageSize, pageNumber);
        pagerParameters.normalizeAndValidate(response);

        int subCollectionStart = pagerParameters.getPageIndex() * pagerParameters.getPageSize();
        List<T> sub_c = subCollection(c, subCollectionStart, pagerParameters.getPageSize());
        Pager<T> p = new DefaultPagerImpl<>(pageNumber, c.size(), pagerParameters.getPageSize(), sub_c);
        return p;
    }

    protected <E> List<E> subCollection(Collection<? extends E> c, Integer start, Integer length) {
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

    //as we do not have a common base controller for both single and list controllers
    //I put this method here though it might be more specific then other methods here.
    //Maybe better use a method like BaseController.getCdmBaseInstance(class, ...)
    protected Taxon getAcceptedTaxonOrError(UUID taxonUuid, ITaxonService taxonService,
            HttpServletResponse response) throws IOException {
        TaxonBase<?> taxon = null;
        if (taxonUuid != null){
            taxon = taxonService.find(taxonUuid);
            if(taxon == null) {
                response.sendError(404 , "Accepted taxon not found using " + taxonUuid );
                //will not happen
                return null;
            }
            if (!taxon.isInstanceOf(Taxon.class)) {
                HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
                //will not happen
                return null;
            }
        }
        return CdmBase.deproxy(taxon, Taxon.class);
    }
}