/**
 * Copyright (C) 2026 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.remote.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Serves swagger-ui webjar assets under {@code /swagger-ui/**}.
 * <p>
 * Controllers take precedence over {@code ResourceHttpRequestHandler}, avoiding the
 * springdoc/Boot resource-handler setup that fails in this non-Boot WAR
 * ({@code No static resource swagger-ui/index.html}).
 */
@Profile("swagger")
@Controller
public class SwaggerUiWebjarController {

    private final String webjarVersion = resolveSwaggerUiVersion();

    @GetMapping("/swagger-ui.html")
    public ResponseEntity<Void> redirectHtml() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "/swagger-ui/index.html")
                .build();
    }

    @GetMapping("/swagger-ui/")
    public ResponseEntity<Void> redirectTrailingSlash() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "/swagger-ui/index.html")
                .build();
    }

    @GetMapping("/swagger-ui/{*resourcePath}")
    public ResponseEntity<?> swaggerUiAsset(@PathVariable("resourcePath") String resourcePath)
            throws IOException {
        String relative = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        if (relative.isBlank()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "/swagger-ui/index.html")
                    .build();
        }
        if ("swagger-initializer.js".equals(relative)) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/javascript"))
                    .body(swaggerInitializerJs().getBytes(StandardCharsets.UTF_8));
        }
        String classpath = "META-INF/resources/webjars/swagger-ui/" + webjarVersion + "/" + relative;
        Resource resource = new ClassPathResource(classpath);
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }
        byte[] body;
        try (InputStream in = resource.getInputStream()) {
            body = StreamUtils.copyToByteArray(in);
        }
        return ResponseEntity.ok()
                .contentType(guessMediaType(relative))
                .body(body);
    }

    private static String swaggerInitializerJs() {
        return """
                window.onload = function() {
                  window.ui = SwaggerUIBundle({
                    url: "/v3/api-docs",
                    dom_id: '#swagger-ui',
                    deepLinking: true,
                    presets: [
                      SwaggerUIBundle.presets.apis,
                      SwaggerUIStandalonePreset
                    ],
                    layout: "StandaloneLayout"
                  });
                };
                """;
    }

    private static MediaType guessMediaType(String path) {
        if (path.endsWith(".html")) {
            return MediaType.TEXT_HTML;
        }
        if (path.endsWith(".js")) {
            return MediaType.parseMediaType("application/javascript");
        }
        if (path.endsWith(".css")) {
            return MediaType.parseMediaType("text/css");
        }
        if (path.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (path.endsWith(".map")) {
            return MediaType.APPLICATION_JSON;
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private static String resolveSwaggerUiVersion() {
        String resource = "META-INF/maven/org.webjars/swagger-ui/pom.properties";
        try (InputStream in = SwaggerUiWebjarController.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Missing " + resource);
            }
            Properties props = new Properties();
            props.load(in);
            String version = props.getProperty("version");
            if (version == null || version.isBlank()) {
                throw new IllegalStateException("No version in " + resource);
            }
            return version;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read swagger-ui webjar version", e);
        }
    }
}
