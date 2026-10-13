/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.config;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.facade.DerivedUnitFacade;
import eu.etaxonomy.cdm.model.CdmAssignableTypeFilter;
import eu.etaxonomy.cdm.model.CdmTypeScanner;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.reference.IReference;
import eu.etaxonomy.cdm.remote.controller.ClassificationListController;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;

/**
 * springdoc-openapi configuration (replaces springfox {@code CdmSwaggerConfig}).
 * <p>
 * Non-Boot Spring MVC: auto-config classes must be imported explicitly.
 * Activate with {@code spring.profiles.active=...swagger}.
 *
 * @see <a href="https://springdoc.org/#spring-webmvc-support-without-spring-boot">springdoc non-Boot FAQ</a>
 */
@Profile("swagger")
@Configuration
@Import({
    // API docs only — swagger-ui is served manually (see SwaggerUiWebjarController).
    // Importing SwaggerConfig registers SwaggerWebMvcConfigurer which maps /swagger-ui*/**
    // to classpath:/META-INF/resources/webjars/ without a working version resolver here → 404.
    org.springdoc.core.configuration.SpringDocConfiguration.class,
    org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration.class,
    org.springdoc.webmvc.core.configuration.MultipleOpenApiSupportConfiguration.class,
    org.springdoc.core.properties.SpringDocConfigProperties.class
})
public class CdmOpenApiConfig {

    private static final Logger logger = LogManager.getLogger();

    private static final String CONTACT =
            "Research and Development Group Biodiversity Informatics and Scientific "
            + "Information Systems (BDI) at the Botanic Garden and Botanical Museum Berlin-Dahlem (BGBM)";

    public CdmOpenApiConfig() {
        logger.debug("CdmOpenApiConfig");
    }

    /**
     * CDM endpoints use {@code @Controller} + view negotiation (no class-level
     * {@code @ResponseBody}), so springdoc ignores them by default. Register
     * concrete controller classes here (one-by-one or as varargs) to include
     * their mapped methods — including inherited ones such as
     * {@code BaseListController#doList} — in the OpenAPI document.
     * <p>
     * Static block: springdoc FAQ pattern; runs before OpenAPI resource scanning.
     */
    static {
        SpringDocUtils.getConfig().addRestControllers(
                ClassificationListController.class
                // further controllers: FooListController.class, ...
        );
    }

    /**
     * Skip huge / cyclic CDM entity graphs in the OpenAPI model (same intent as
     * springfox {@code ignoredParameterTypes}).
     * <p>
     * {@code addJavaTypeToIgnore} alone is not enough: springdoc's
     * {@code JavaTypeToIgnoreConverter} only skips nested schema properties, not
     * controller return types. {@code replaceWithSchema} short-circuits Jackson
     * introspection (avoids conflicts like {@code cacheStrategy} on Reference).
     */
    @PostConstruct
    public void ignoreCdmModelTypes() {
        Collection<Class<? extends Object>> types = allCdmTypes();
        types.add(CdmBase.class);
        types.add(IdentifiableEntity.class);
        types.add(IReference.class);
        types.add(Pager.class);
        types.add(DerivedUnitFacade.class);
        Schema<?> stub = cdmTypeStubSchema();
        SpringDocUtils utils = SpringDocUtils.getConfig();
        for (Class<?> type : types) {
            utils.replaceWithSchema(type, stub);
            utils.addJavaTypeToIgnore(type);
            utils.addResponseTypeToIgnore(type);
        }
        logger.info("springdoc: stubbed/ignored " + types.size() + " CDM model types");
    }

    /**
     * Package-level safety net for CDM types not covered by the entity scanner
     * (interfaces, DTOs under model.*, etc.).
     */
    @Bean
    public ModelConverter cdmModelPackageStubConverter() {
        return new ModelConverter() {
            @Override
            public Schema<?> resolve(AnnotatedType annotatedType, ModelConverterContext context,
                    Iterator<ModelConverter> chain) {
                Class<?> raw = rawClass(annotatedType.getType());
                if (isCdmDomainType(raw)) {
                    return cdmTypeStubSchema().name(raw.getSimpleName());
                }
                return chain.hasNext() ? chain.next().resolve(annotatedType, context, chain) : null;
            }
        };
    }

    private static Class<?> rawClass(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz;
        }
        JavaType javaType = TypeFactory.defaultInstance().constructType(type);
        return javaType.getRawClass();
    }

    private static boolean isCdmDomainType(Class<?> clazz) {
        if (clazz == null || clazz.isPrimitive()) {
            return false;
        }
        if (CdmBase.class.isAssignableFrom(clazz)) {
            return true;
        }
        String name = clazz.getName();
        return name.startsWith("eu.etaxonomy.cdm.model.")
                || name.startsWith("eu.etaxonomy.cdm.facade.")
                || name.startsWith("eu.etaxonomy.cdm.api.service.pager.");
    }

    @SuppressWarnings("rawtypes")
    private static Schema cdmTypeStubSchema() {
        return new ObjectSchema().description("CDM domain type omitted from OpenAPI (cyclic / conflicting model)");
    }

    @Bean
    public GroupedOpenApi genericRestApi() {
        return GroupedOpenApi.builder()
                .group(SwaggerGroupsConfig.GENERIC_REST_API.groupName())
                .pathsToExclude(
                        "/portal/**",
                        "/taxon/oai/**",
                        "/reference/oai/**",
                        "/name_catalogue/**",
                        "/occurrence_catalogue/**",
                        "/authority/**",
                        "/csv/**",
                        "/checklist/**",
                        "/manage/**",
                        "/progress/**")
                .addOpenApiCustomizer(openApi -> openApi.info(apiInfo(
                        SwaggerGroupsConfig.GENERIC_REST_API.groupName(),
                        "<p>The CDM REST API is a RESTful interface to resources stored in the CDM."
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
                        + "<a href=\"https://cybertaxonomy.org/cdmlib/rest-api.html\">https://cybertaxonomy.org/cdmlib/rest-api.html</a>")))
                .build();
    }

    @Bean
    public GroupedOpenApi portalApi() {
        return GroupedOpenApi.builder()
                .group(SwaggerGroupsConfig.WEB_PORTAL_SERVICES.groupName())
                .pathsToMatch("/portal/**")
                .addOpenApiCustomizer(openApi -> openApi.info(apiInfo(
                        SwaggerGroupsConfig.WEB_PORTAL_SERVICES.groupName(),
                        "<p>The Portal Service is a specialization to the "
                        + "Generic REST API as needed by CDM Dataportal "
                        + " that adds some fields like localized representations to the pure CDM entities. Another important difference "
                        + " is the initialization depth of the CDM entities. The Portal Service endpoints provide far bigger parts of the "
                        + " object graph.</p>")))
                .build();
    }

    @Bean
    public GroupedOpenApi catalogueApi() {
        return GroupedOpenApi.builder()
                .group(SwaggerGroupsConfig.CATALOGUE_SERVICES.groupName())
                .pathsToMatch("/name_catalogue/**", "/occurrence_catalogue/**")
                .addOpenApiCustomizer(openApi -> openApi.info(apiInfo(
                        SwaggerGroupsConfig.CATALOGUE_SERVICES.groupName(),
                        "<p>These web services are optimized for using names taxonomic information and occurence data in workflow environments "
                        + " but are suitabale for all applicatoins in which fast response times are crucial."
                        + " Additional detailed documentation of these services can also be found at:</p>"
                        + "<ul>"
                        + "<li><a href=\"https://cybertaxonomy.org/cdmlib/rest-api-name-catalogue.html\">Name Catalogue REST API</a>"
                        + "<li><a href=\"https://cybertaxonomy.org/cdmlib/rest-api-occurrence-catalogue.html\">Occurrence Catalogue REST API</a>"
                        + "</ul>")))
                .build();
    }

    @Bean
    public GroupedOpenApi oaiPmhApi() {
        return GroupedOpenApi.builder()
                .group(SwaggerGroupsConfig.OAIPMH.groupName())
                .pathsToMatch("/reference/oai/**", "/taxon/oai/**")
                .addOpenApiCustomizer(openApi -> openApi.info(apiInfo(
                        SwaggerGroupsConfig.OAIPMH.groupName(),
                        "<p>This is an automatically created documentation on the OAI-PMH service which is actually dedicated to REST services."
                        + " Since OAI-PMH is not a REST service in the original sense, you may want to refer to"
                        + " the more specific <a href=\"https://cybertaxonomy.org/cdmlib/oai-pmh.html\">OAI-PMH documentation</a> or"
                        + " to the service endpoints them self, which also provide a good, comprehensive and selfexplanatory"
                        + " level of documentation:</p>"
                        + "<ul>"
                        + "<li><a href=\"../reference/oai?verb=Identify\">OAI-PMH for References</a></li>"
                        + "<li><a href=\"../taxon/oai?verb=Identify\">OAI-PMH for Taxa</a></li>"
                        + "</ul>")))
                .build();
    }

    @Bean
    public GroupedOpenApi lsidApi() {
        return GroupedOpenApi.builder()
                .group(SwaggerGroupsConfig.LSID_AUTHORITY_SERVICES.groupName())
                .pathsToMatch("/authority/**")
                .addOpenApiCustomizer(openApi -> openApi.info(apiInfo(
                        SwaggerGroupsConfig.LSID_AUTHORITY_SERVICES.groupName(), "")))
                .build();
    }

    @Bean
    public GroupedOpenApi dataExportApi() {
        return GroupedOpenApi.builder()
                .group(SwaggerGroupsConfig.DATA_EXPORT.groupName())
                .pathsToMatch("/csv/**", "/checklist/**")
                .addOpenApiCustomizer(openApi -> openApi.info(apiInfo(
                        SwaggerGroupsConfig.DATA_EXPORT.groupName(), "")))
                .build();
    }

    /** Default OpenAPI bean (used when no group is selected). */
    @Bean
    public OpenAPI cdmOpenAPI() {
        return new OpenAPI().info(apiInfo("CDM REST API", "EDIT CDM Library remote services"));
    }

    private Collection<Class<? extends Object>> allCdmTypes() {
        boolean includeAbstract = true;
        boolean includeInterfaces = false;
        CdmTypeScanner<Object> scanner = new CdmTypeScanner<>(includeAbstract, includeInterfaces);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        scanner.addIncludeFilter(new CdmAssignableTypeFilter(CdmBase.class, includeAbstract, includeInterfaces));
        return scanner.scanTypesIn("eu/etaxonomy/cdm/model");
    }

    private Info apiInfo(String title, String description) {
        return new Info()
                .title(title)
                .description(description)
                .contact(new Contact().name(CONTACT))
                .license(new License().name("Mozilla Public License 2.0")
                        .url("http://www.mozilla.org/MPL/2.0/"));
    }
}
