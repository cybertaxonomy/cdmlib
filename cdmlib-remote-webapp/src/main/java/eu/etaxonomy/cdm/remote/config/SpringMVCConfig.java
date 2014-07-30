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
import java.util.List;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
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

import eu.etaxonomy.cdm.remote.controller.interceptor.LocaleContextHandlerInterceptor;
import eu.etaxonomy.cdm.remote.controller.util.CdmAntPathMatcher;
import eu.etaxonomy.cdm.remote.view.PatternViewResolver;

/**
 * @author a.kohlbecker
 * @date Jul 1, 2014
 *
 */
//@EnableWebMvc do not add this since we are overriding WebMvcConfigurationSupport directly, see requestMappingHandlerMapping()
@Configuration
@Import(value={PreloadedBeans.class})
@ComponentScan(basePackages = {
        "eu.etaxonomy.cdm.remote"
//        "com.mangofactory.swagger.configuration.SpringSwaggerConfig",
//        "com.mangofactory.swagger.controllers"
        },
        excludeFilters={
            @Filter(type=FilterType.CUSTOM, value=VaadinPackageFilter.class) //  eu\.etaxonomy\.cdm\.remote\.vaadin\..*
        }
)
//@EnableSwagger
public class SpringMVCConfig extends WebMvcConfigurationSupport {


    /**
     * turn caching off FOR DEBUGING ONLY !!!!
     */
    private static final boolean XML_VIEW_CACHING = true;

    public static final String[] WEB_JAR_RESOURCE_PATTERNS = {"css/", "images/", "lib/", "swagger-ui.js"};
    public static final String WEB_JAR_RESOURCE_LOCATION = "classpath:META-INF/resources/";

    public static final String WEB_JAR_VIEW_RESOLVER_PREFIX = "/WEB-INF/jsp/";
    public static final String WEB_JAR_VIEW_RESOLVER_SUFFIX = ".jsp";

    @Autowired
    protected ServletContext servletContext;

    @Autowired // is initialized in PreloadedBeans.class
    private LocaleContextHandlerInterceptor localeContextHandlerInterceptor;


//    private SpringSwaggerConfig springSwaggerConfig;

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

    @Bean
    public PathMatcher pathMatcher(){
        return new CdmAntPathMatcher();
    }

    @Override
    @Bean
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        /* NOTE: this override is the only reason why this class
         * needs to extends WebMvcConfigurationSupport. We may be able to
         * remove this method once we no longer need
         * CdmAntPathMatcher. this is only needed  since the contollers need
         * absolute method level RequestMapping values in some few cases.
         */
        RequestMappingHandlerMapping handlerMapping = super.requestMappingHandlerMapping();
        handlerMapping.setPathMatcher(pathMatcher());
        return handlerMapping;
    }

    @Bean
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
    }


    @Override
    protected void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
      // DefaultServletHandlerConfigurer: delegates unhandled requests by forwarding to the Servlet container's "default"
      // servlet, since the DispatcherServlet is mapped to "/"
      configurer.enable();
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
    * /
   @Autowired
   public void setSpringSwaggerConfig(SpringSwaggerConfig springSwaggerConfig) {
      this.springSwaggerConfig = springSwaggerConfig;
   }

   /**
    * Every SwaggerSpringMvcPlugin bean is picked up by the swagger-mvc framework - allowing for multiple
    * swagger groups i.e. same code base multiple swagger resource listings.
    * /
   @Bean
   public SwaggerSpringMvcPlugin customImplementation(){
       // includePatterns: If not supplied a single pattern ".*?" is used by SwaggerSpringMvcPlugin
       // which matches anything and hence all RequestMappings. Here we define it explicitly
      return new SwaggerSpringMvcPlugin(this.springSwaggerConfig)
              .apiInfo(apiInfo()).
              includePatterns(".*?");
   }

   private ApiInfo apiInfo() {
       ApiInfo apiInfo = new ApiInfo(
               "EU-BON Utis",
               "The Unified Taxonomic Information Service (UTIS) is the taxonomic backbone for the EU-BON project",
               "UTIS API terms of service",
               "EditSupport@bgbm.org",
               "Mozilla Public License 2.0",
               "http://www.mozilla.org/MPL/2.0/"
         );
       return apiInfo;
     }
   */


}
