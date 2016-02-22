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


import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

import java.util.Collection;

import javax.persistence.Entity;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import eu.etaxonomy.cdm.model.CdmAssignableTypeFilter;
import eu.etaxonomy.cdm.model.CdmTypeScanner;
import eu.etaxonomy.cdm.model.common.CdmBase;

/**
 * <h3>NOTE:</h3>
 *  For a detailed overview on the spring MVC and application context configuration and
 *  bootstrapping of this web application see:
 *  {@link http://dev.e-taxonomy.eu/trac/wiki/cdmlib-remote-webappConfigurationAndBootstrapping}
 *
 *
 */
@Profile("swagger")
@EnableSwagger2
@Configuration
public class CdmSwaggerConfig {

    public static final Logger logger = Logger.getLogger(CdmSwaggerConfig.class);

    Collection<Class<? extends Object>> allCdmTypes = null;

    public CdmSwaggerConfig() {
        super();
        logger.debug("contructor");

    }

   /**
    * Every SwaggerSpringMvcPlugin bean is picked up by the swagger-mvc framework - allowing for multiple
    * swagger groups i.e. same code base multiple swagger resource listings.
    */
//   @Bean(name="swaggerPluginDefault")
   @Bean(name="swaggerPluginGenericAPI")
   public Docket swaggerPluginGenericAPI(){
       logger.debug("swaggerSpringMvcPlugin");
       configureModelConverters();
       return new Docket(DocumentationType.SWAGGER_2)
           .groupName(SwaggerGroupsConfig.GENERIC_REST_API.groupName())
               .select()
               .paths(not(
                        or(
                            regex("/portal/.*"),
                            regex("/taxon/oai.*"),
                            regex("/reference/oai.*"),
                            regex("/name_catalogue.*"),
                            regex("/occurrence_catalogue.*"),
                            regex("/authority.*"),
                            regex("/csv/.*"),
                            regex("/checklist/.*"),
                            regex("/manage/.*"),
                            regex("/progress/.*")
                         )
                       )
                )
               .build()
          .apiInfo(apiInfo(SwaggerGroupsConfig.GENERIC_REST_API.groupName(), ""
                  + "<p>The CDM REST API is a RESTful interface to resources stored in the CDM."
                  + " The RESTful architecture allows accessing the various resources like Taxa, "
                  + "Names, References, Media, etc by stable URIs. Due to security constraints "
                  + "and to assure the integration of data, currently only read operations "
                  + "(= HTTP GET) are permitted, write operations may be available in the future.</p>"
                  + "<p>The architecture directly exposes domain model entities, i.e. it provides "
                  + "direct serializations of the objects as they are stored in the CDM but also "
                  + "returns TDOs of the CDM entities in some cases. The API Service provides an easy to use way "
                  + "to transfer CDM data to web based clients."
                  + "</p>"
                  + "<p>For more information like usage of this service please refer to "
                  + "<a href=\"http://cybertaxonomy.eu/cdmlib/rest-api.html\">http://cybertaxonomy.eu/cdmlib/rest-api.html</a>"))
          .ignoredParameterTypes(allCdmTpyes())
//          .excludeAnnotations(Deprecated.class) // TODO
          ;
   }

   @Bean(name="swaggerPluginPortal")
   public Docket swaggerPluginPortal(){
       logger.debug("swaggerSpringMvcPlugin");
       configureModelConverters();
       return new Docket(DocumentationType.SWAGGER_2)
           .groupName(SwaggerGroupsConfig.WEB_PORTAL_SERVICES.groupName())
               .select()
               .paths(regex("/portal/.*"))
               .build()
          .apiInfo(apiInfo(SwaggerGroupsConfig.WEB_PORTAL_SERVICES.groupName(), "<p>The Portal Service is a specialization to the "
                  + "<a href=\"?group=Generic+REST+API\">Generic  REST API</a> as needed by CDM Dataportal "
                  + " that adds some fields like localized representations to the pure CDM entities. Another important difference "
                  + " is the initialization depth of the CDM entities. The Portal Service enpoints provide far bigger parts of the "
                  + " object graph.</p>"))
          .ignoredParameterTypes(allCdmTpyes());
   }

   @Bean(name="swaggerPluginNameCatalogue")
   public Docket swaggerPluginNameCatalogue(){
       configureModelConverters();
       return new Docket(DocumentationType.SWAGGER_2)
           .groupName(SwaggerGroupsConfig.CATALOGUE_SERVICES.groupName())
               .select()
               .paths(or(regex("/name_catalogue.*"),regex("/occurrence_catalogue.*")))
               .build()
           .apiInfo(apiInfo(
                   SwaggerGroupsConfig.CATALOGUE_SERVICES.groupName(),
                   "<p>These web services are optimized for using names taxonomic information and occurence data in workflow environments "
                   + " but are suitabale for all applicatoins in which fast response times are crucial."
                   + " Additional detailed documentation of these services can also be found at:</p>"
                   + "<ul>"
                   + "<li><a href=\"http://cybertaxonomy.eu/cdmlib/rest-api-name-catalogue.html\">Name Catalogue REST API</a>"
                   + "<li><a href=\"http://cybertaxonomy.eu/cdmlib/rest-api-occurrence-catalogue.html\">Occurrence Catalogue REST API</a>"
                   + "</ul>"
                   ))
           .ignoredParameterTypes(allCdmTpyes());
   }

   @Bean(name="swaggerPluginOAIPMH")
   public Docket swaggerPluginOAIPMH(){
       logger.debug("swaggerSpringMvcPlugin");
       configureModelConverters();
       return new Docket(DocumentationType.SWAGGER_2)
           .groupName("OAI-PMH")
               .select()
               .paths(or(regex("/reference/oai.*"), regex("/taxon/oai.*")))
               .build()
          .apiInfo(apiInfo(
                  "OAI-PMH",
                  "<p>This is an automatcially created documentation on the OAI-PMH service which is atually dedicated to REST services."
                  + " Since OAI-PMH is not a REST service in the original sense, you may want to refer to"
                  + " the more specific <a href=\"http://cybertaxonomy.eu/cdmlib/oai-pmh.html\">OAI-PMH documentation</a> or"
                  + " to the service endpoints them self, which also provide a good, comprehensive and selfexplanatory"
                  + " level of documentation:</p>"
                  + "<ul>"
                  + "<li><a href=\"../reference/oai?verb=Identify\">OAI-PMH for References</a></li>"
                  + "<li><a href=\"../taxon/oai?verb=Identify\">OAI-PMH for Taxa</a></li>"
                  + "</ul>"))
          .ignoredParameterTypes(allCdmTpyes());
   }

   @Bean(name="swaggerPluginLSID")
   public Docket swaggerPluginLSID(){
       logger.debug("swaggerSpringMvcPlugin");
       configureModelConverters();
       return new Docket(DocumentationType.SWAGGER_2)
           .groupName(SwaggerGroupsConfig.LSID_AUTHORITY_SERVICES.groupName())
               .select()
               .paths(regex("/authority/.*"))
               .build()
          .apiInfo(apiInfo(SwaggerGroupsConfig.LSID_AUTHORITY_SERVICES.groupName(), ""))
          .ignoredParameterTypes(allCdmTpyes());
   }

   @Bean(name="swaggerPluginDataExport")
   public Docket swaggerPluginDataExport(){
       logger.debug("swaggerSpringMvcPlugin");
       configureModelConverters();
       return new Docket(DocumentationType.SWAGGER_2)
           .groupName(SwaggerGroupsConfig.DATA_EXPORT.groupName())
               .select()
               .paths(or(regex("/csv/.*"), regex("/checklist.*")))
               .build()
          .apiInfo(apiInfo(SwaggerGroupsConfig.DATA_EXPORT.groupName(), ""))
          .ignoredParameterTypes(allCdmTpyes());
   }


   /**
    * FIXME remove or implement?
     * Disabled during the trassition to springfox,
     * the old code could not be compiled but seems
     * no longer to be nessescary
     */
    private void configureModelConverters() {
        // fully skip the creation of cdm model documentation
        // since it will be too excessive to scan the huge cdm model
        // which in fact has cycles.
        // not sure if this is working !!!
//       String emptyJSON = "{}";
//       OverrideConverter sessionConverter = new OverrideConverter();
//       sessionConverter.add(Session.class.getName(), emptyJSON);
//       ModelContext.addConverter(sessionConverter, true);
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
               title, // title
               description, //description
               null, //version
               null, // terms of service URL
               "EditSupport@bgbm.org", // contact
               "Mozilla Public License 2.0", // license
               "http://www.mozilla.org/MPL/2.0/" // licenseUrl
         );
       return apiInfo;
     }


}
