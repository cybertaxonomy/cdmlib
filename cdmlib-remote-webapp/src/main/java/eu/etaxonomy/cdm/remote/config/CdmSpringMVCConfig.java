/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.config;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.XmlViewResolver;

import eu.etaxonomy.cdm.remote.controller.interceptor.LocaleContextHandlerInterceptor;
import eu.etaxonomy.cdm.remote.view.PatternViewResolver;

/**
 * This is the main configuration for spring MVC application context.
 *
 *
 * <h3>NOTE:</h3>
 *  For a detailed overview on the spring MVC and application context configuration and
 *  bootstrapping of this web application see:
 *  {@link http://dev.e-taxonomy.eu/trac/wiki/cdmlib-remote-webappConfigurationAndBootstrapping}
 *
 *
 * @author a.kohlbecker
 * @since Jul 1, 2014
 *
 */
// @EnableWebMvc // do not add this since we are overriding WebMvcConfigurationSupport directly
@Import(value={PreloadedBeans.class}) // can not be replaced by @DependsOn("...") ?
//@DependsOn("objectMapperConfigurer")
public abstract class CdmSpringMVCConfig extends WebMvcConfigurationSupport  {

    /**
     * turn caching off FOR DEBUGING ONLY !!!!
     */
    private static final boolean XML_VIEW_CACHING = true;

    public static final Logger logger = Logger.getLogger(CdmSpringMVCConfig.class);


    @Autowired
    protected ServletContext servletContext;

    @Autowired // is initialized in PreloadedBeans.class
    private LocaleContextHandlerInterceptor localeContextHandlerInterceptor;

    Collection<Class<? extends Object>> allCdmTypes = null;

//    ========================== JSP =================================
//    public static final String[] WEB_JAR_RESOURCE_PATTERNS = {"css/", "images/", "lib/", "swagger-ui.js"};
//    public static final String WEB_JAR_RESOURCE_LOCATION = "classpath:META-INF/resources/";
//
//    public static final String WEB_JAR_VIEW_RESOLVER_PREFIX = "/WEB-INF/jsp/";
//    public static final String WEB_JAR_VIEW_RESOLVER_SUFFIX = ".jsp";
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//      registry.addResourceHandler(WEB_JAR_RESOURCE_PATTERNS)
//              .addResourceLocations("/")
//              .addResourceLocations(WEB_JAR_RESOURCE_LOCATION).setCachePeriod(0);
//    }

//  @Bean
//  public InternalResourceViewResolver getInternalResourceViewResolverJsp() {
//    InternalResourceViewResolver resolver = new InternalResourceViewResolver();
//    resolver.setOrder(0);
//    resolver.setPrefix(WEB_JAR_VIEW_RESOLVER_PREFIX);
//    resolver.setSuffix(WEB_JAR_VIEW_RESOLVER_SUFFIX);
//    // view names (or name patterns) that can be handled
//    resolver.setViewNames(new String[]{...});
//    return resolver;
//  }
//  ======================================================================

    public CdmSpringMVCConfig() {
        super();
        logger.debug("contructor");

    }

    @Bean
    @DependsOn({"requestMappingHandlerAdapter"})
    public XmlViewResolver getOaiXmlViewResolver() {
        XmlViewResolver resolver = new XmlViewResolver();
      resolver.setOrder(1);
      resolver.setLocation(new ServletContextResource(servletContext,"/WEB-INF/oai-views.xml"));
      resolver.setCache(XML_VIEW_CACHING);
      return resolver;
    }


    /* (non-Javadoc)
     * @see org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration#addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry)
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        // TODO does it work?
        registry.addInterceptor(localeContextHandlerInterceptor);
        logger.debug("addInterceptors");
    }

    @Override
    protected void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
      // DefaultServletHandlerConfigurer: delegates unhandled requests by forwarding to
      // the Servlet container's "default" servlet, since the DispatcherServlet is mapped to "/"
      // so static content ad welcome files are handled by the default servlet
      configurer.enable();
      logger.debug("configureDefaultServletHandling");
    }


    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
        .favorPathExtension(true)
        .favorParameter(false)
        .defaultContentType(MediaType.APPLICATION_JSON)
        .mediaType("xml", MediaType.APPLICATION_XML)
        .mediaType("dc", MediaType.APPLICATION_XML)
        .mediaType("rdf", MediaType.APPLICATION_XML)
        .mediaType("rdfxml", MediaType.APPLICATION_XML)
        .mediaType("json", MediaType.APPLICATION_JSON);

        logger.debug("configureContentNegotiation");
    }

    /**
     * Create the CNVR.  Specify the view resolvers to use explicitly.  Get Spring to inject
     * the ContentNegotiationManager created by the configurer (see previous method).
     */
   @Bean
   public ViewResolver contentNegotiatingViewResolver(ContentNegotiationManager manager) {

       List<ViewResolver> resolvers = new ArrayList<ViewResolver>();

       resolvers.add(getPatternViewResolver("xml"));
       resolvers.add(getPatternViewResolver("json"));
       resolvers.add(getPatternViewResolver("rdf"));

       ContentNegotiatingViewResolver resolver = new ContentNegotiatingViewResolver();
       resolver.setOrder(2);
       resolver.setContentNegotiationManager(manager);
       resolver.setViewResolvers(resolvers);
       logger.debug("contentNegotiatingViewResolver");
       return resolver;
       }


   private ViewResolver getPatternViewResolver(String type) {
       PatternViewResolver resolver = new PatternViewResolver();
       resolver.setLocation(new ServletContextResource(servletContext, "/WEB-INF/"+  type + "-views.xml"));
       resolver.setCache(XML_VIEW_CACHING);
       return resolver;
   }

}
