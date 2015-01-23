// $Id$
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

import javax.persistence.Entity;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.http.MediaType;
import org.springframework.util.PathMatcher;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.XmlViewResolver;

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;
import com.wordnik.swagger.model.ApiInfo;

import eu.etaxonomy.cdm.model.CdmAssignableTypeFilter;
import eu.etaxonomy.cdm.model.CdmTypeScanner;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.remote.controller.interceptor.LocaleContextHandlerInterceptor;
import eu.etaxonomy.cdm.remote.controller.util.CdmAntPathMatcher;
import eu.etaxonomy.cdm.remote.view.PatternViewResolver;

/**
 * @author a.kohlbecker
 * @date Jul 1, 2014
 *
 */
//@EnableWebMvc do not add this since we are overriding WebMvcConfigurationSupport directly, see requestMappingHandlerMapping()
@EnableSwagger
@Configuration
@Import(value={PreloadedBeans.class})
@ComponentScan(basePackages = {
        "eu.etaxonomy.cdm.remote.l10n",
        "eu.etaxonomy.cdm.remote.controller",
        "eu.etaxonomy.cdm.remote.service",
        "eu.etaxonomy.cdm.remote.config",
        }
)
public class CdmSpringMVCConfig extends WebMvcConfigurationSupport {

    private static final String LSID_AUTHORITY_SERVICES = "LSID authority services";

    private static final String DATA_EXPORT = "Data export";

    private static final String GENERIC_REST_API = "Generic REST API";

    private static final String WEB_PORTAL_SERVICES = "Web Portal Services";

    private static final String CATALOGUE_SERVICES = "Catalogue Services";


    /**
     * turn caching off FOR DEBUGING ONLY !!!!
     */
    private static final boolean XML_VIEW_CACHING = true;

    public static final Logger logger = Logger.getLogger(CdmSpringMVCConfig.class);


    @Autowired
    protected ServletContext servletContext;

    @Autowired // is initialized in PreloadedBeans.class
    private LocaleContextHandlerInterceptor localeContextHandlerInterceptor;


    private SpringSwaggerConfig springSwaggerConfig;

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
    public PathMatcher pathMatcher(){
        return new CdmAntPathMatcher();
    }

    @Override
    @Bean
    @DependsOn({"swaggerPluginGenericAPI", "swaggerPluginNameCatalogue", "swaggerPluginPortal", "swaggerPluginOAIPMH", "swaggerPluginLSID"})
//    @DependsOn({"swaggerPluginDefault"})
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        /* NOTE: this override is the only reason why this class
         * needs to extends WebMvcConfigurationSupport. We may be able to
         * remove this method once we no longer need
         * CdmAntPathMatcher. this is only needed  since the contollers need
         * absolute method level RequestMapping values in some few cases.
         */
        RequestMappingHandlerMapping handlerMapping = super.requestMappingHandlerMapping();
        handlerMapping.setPathMatcher(pathMatcher());

        logger.debug("requestMappingHandlerMapping");
        return handlerMapping;
    }

    @Bean
    @DependsOn({"swaggerPluginGenericAPI", "swaggerPluginNameCatalogue", "swaggerPluginPortal", "swaggerPluginOAIPMH", "swaggerPluginLSID"})
//    @DependsOn({"swaggerPluginDefault"})
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
   @DependsOn({"swaggerPluginGenericAPI", "swaggerPluginNameCatalogue", "swaggerPluginPortal", "swaggerPluginOAIPMH", "swaggerPluginLSID"})
//   @DependsOn({"swaggerPluginDefault"})
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

    // -------- Swagger configuration ------------ //

   /**
    * Required to autowire SpringSwaggerConfig
    */
   @Autowired
   public void setSpringSwaggerConfig(SpringSwaggerConfig springSwaggerConfig) {
      this.springSwaggerConfig = springSwaggerConfig;
      logger.debug("setSpringSwaggerConfig");
   }

   /**
    * Every SwaggerSpringMvcPlugin bean is picked up by the swagger-mvc framework - allowing for multiple
    * swagger groups i.e. same code base multiple swagger resource listings.
    */
//   @Bean(name="swaggerPluginDefault")
   @Bean(name="swaggerPluginGenericAPI")
   public SwaggerSpringMvcPlugin swaggerPluginGenericAPI(){
       logger.debug("swaggerSpringMvcPlugin");
       configureModelConverters();
       return new SwaggerSpringMvcPlugin(this.springSwaggerConfig)
          .apiInfo(apiInfo(GENERIC_REST_API, ""))
          // below regex excludes the paths of all other groups,
          // !!! also hide the manage and progress controller
          .includePatterns("/(?!portal/)(?!taxon/oai)(?!reference/oai)(?!name_catalogue/)(?!authority/)(?!csv/)(?!checklist)(?!manage/)(?!progress/).*")
          .ignoredParameterTypes(allCdmTpyes())
          .swaggerGroup(GENERIC_REST_API)
//          .excludeAnnotations(Deprecated.class) // TODO
          ;
   }

   @Bean(name="swaggerPluginPortal")
   public SwaggerSpringMvcPlugin swaggerPluginPortal(){
       logger.debug("swaggerSpringMvcPlugin");
       configureModelConverters();
       return new SwaggerSpringMvcPlugin(this.springSwaggerConfig)
          .apiInfo(apiInfo(WEB_PORTAL_SERVICES, ""))
          .includePatterns("/portal/.*")
          .ignoredParameterTypes(allCdmTpyes())
          .swaggerGroup(WEB_PORTAL_SERVICES);
   }

   @Bean(name="swaggerPluginNameCatalogue")
   public SwaggerSpringMvcPlugin swaggerPluginNameCatalogue(){
       configureModelConverters();
       return new SwaggerSpringMvcPlugin(this.springSwaggerConfig)
           .apiInfo(apiInfo(CATALOGUE_SERVICES, ""))
           .includePatterns("/name_catalogue/.*", "/occurrence_catalogue/.*")
           .ignoredParameterTypes(allCdmTpyes())
           .swaggerGroup(CATALOGUE_SERVICES);
   }

   @Bean(name="swaggerPluginOAIPMH")
   public SwaggerSpringMvcPlugin swaggerPluginOAIPMH(){
       logger.debug("swaggerSpringMvcPlugin");
       configureModelConverters();
       return new SwaggerSpringMvcPlugin(this.springSwaggerConfig)
          .apiInfo(apiInfo("OAI-PMH", ""))
          .includePatterns("/reference/oai.*", "/taxon/oai.*")
          .ignoredParameterTypes(allCdmTpyes())
          .swaggerGroup("OAI-PMH");
   }

   @Bean(name="swaggerPluginLSID")
   public SwaggerSpringMvcPlugin swaggerPluginLSID(){
       logger.debug("swaggerSpringMvcPlugin");
       configureModelConverters();
       return new SwaggerSpringMvcPlugin(this.springSwaggerConfig)
          .apiInfo(apiInfo(LSID_AUTHORITY_SERVICES, ""))
          .includePatterns("/authority/.*")
          .ignoredParameterTypes(allCdmTpyes())
          .swaggerGroup(LSID_AUTHORITY_SERVICES);
   }

   @Bean(name="swaggerPluginDataExport")
   public SwaggerSpringMvcPlugin swaggerPluginDataExport(){
       logger.debug("swaggerSpringMvcPlugin");
       configureModelConverters();
       return new SwaggerSpringMvcPlugin(this.springSwaggerConfig)
          .apiInfo(apiInfo(DATA_EXPORT, ""))
          .includePatterns("/csv/.*", "/checklist.*")
          .ignoredParameterTypes(allCdmTpyes())
          .swaggerGroup(DATA_EXPORT);
   }


   /**
     *
     */
    private void configureModelConverters() {
        // fully skip the creation of cdm model documentation
        // since it will be too excessive to scan the huge cdm model
        // which in fact has cycles.
        // not sure if this is working !!!
//       String emptyJSON = "{}";
//       OverrideConverter sessionConverter = new OverrideConverter();
//       sessionConverter.add(Session.class.getName(), emptyJSON);
//       ModelConverters.addConverter(sessionConverter, true);
    }


       //TODO failes to convert json to model
       // "org.json4s.package$MappingException: Did not find value
       //            which can be converted into java.lang.String"


    /**
     * @return
     */
    private Class<Class<? extends Object>>[] allCdmTpyes() {
        if (allCdmTypes == null) {
            allCdmTypes = allCdmTypes();
            allCdmTypes.add(eu.etaxonomy.cdm.api.service.pager.Pager.class);
            allCdmTypes.add(eu.etaxonomy.cdm.api.facade.DerivedUnitFacade.class);
        }
        return allCdmTypes.toArray(new Class[allCdmTypes.size()]);
    }

/**
 * @return
 */
private Collection<Class<? extends Object>> allCdmTypes() {
    boolean includeAbstract = true;
    boolean includeInterfaces = false;
    Collection<Class<? extends Object>> classes = null;

    CdmTypeScanner<Object> scanner = new CdmTypeScanner<Object>(includeAbstract, includeInterfaces);
    scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
    scanner.addIncludeFilter(new CdmAssignableTypeFilter(CdmBase.class, includeAbstract, includeInterfaces));
    classes = scanner.scanTypesIn("eu/etaxonomy/cdm/model");

    return classes;
}

   private ApiInfo apiInfo(String title, String description) {
       ApiInfo apiInfo = new ApiInfo(
               title,
               description,
               null, // terms of service
               "EditSupport@bgbm.org",
               "Mozilla Public License 2.0",
               "http://www.mozilla.org/MPL/2.0/"
         );
       return apiInfo;
     }


}
