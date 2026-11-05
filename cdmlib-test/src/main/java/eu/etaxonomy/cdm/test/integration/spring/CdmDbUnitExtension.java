/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.integration.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Applies {@link CdmDataSet} / {@link CdmExpectedDataSet} around each test method.
 * <p>
 * Expected datasets are checked in {@link AfterTestExecutionCallback} (before {@code @AfterEach}),
 * so transactional tests still see uncommitted changes / committed state before rollback.
 */
public class CdmDbUnitExtension implements BeforeEachCallback, AfterTestExecutionCallback {

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Optional<CdmDataSet> annotation = findDataSet(context);
        if (annotation.isEmpty()) {
            return;
        }
        DataSource dataSource = dataSource(context);
        List<Resource> resources = resolveResources(context, annotation.get().value(),
                annotation.get().fileAppendix(), false);
        Resource[] resourceArray = resources.toArray(Resource[]::new);
        if (annotation.get().refresh()) {
            CdmDbUnitSupport.refresh(dataSource, resourceArray);
        } else if (annotation.get().cleanSweep()) {
            CdmDbUnitSupport.cleanSweepInsert(dataSource, resourceArray);
        } else {
            CdmDbUnitSupport.cleanInsert(dataSource, resourceArray);
        }
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        Optional<CdmExpectedDataSet> annotation = findExpectedDataSet(context);
        if (annotation.isEmpty()) {
            return;
        }
        // Unitils flushed the ORM session before ExpectedDataSet so JDBC sees writes
        flushHibernateSession(context);
        DataSource dataSource = dataSource(context);
        List<Resource> resources = resolveResources(context, annotation.get().value(),
                annotation.get().fileAppendix(), true);
        for (Resource resource : resources) {
            CdmDbUnitSupport.assertEquals(dataSource, resource);
        }
    }

    private static void flushHibernateSession(ExtensionContext context) {
        try {
            org.hibernate.SessionFactory sessionFactory = SpringExtension.getApplicationContext(context)
                    .getBean(org.hibernate.SessionFactory.class);
            org.hibernate.Session session = sessionFactory.getCurrentSession();
            if (session != null && session.isOpen()) {
                session.flush();
            }
        } catch (Exception ignored) {
            // no open session / no SessionFactory — ExpectedDataSet still runs on JDBC
        }
    }

    private static DataSource dataSource(ExtensionContext context) {
        return SpringExtension.getApplicationContext(context).getBean(DataSource.class);
    }

    private static Optional<CdmDataSet> findDataSet(ExtensionContext context) {
        Optional<CdmDataSet> onMethod = context.getTestMethod()
                .map(m -> AnnotatedElementUtils.findMergedAnnotation(m, CdmDataSet.class));
        if (onMethod.isPresent()) {
            return onMethod;
        }
        return context.getTestClass()
                .map(c -> AnnotatedElementUtils.findMergedAnnotation(c, CdmDataSet.class));
    }

    private static Optional<CdmExpectedDataSet> findExpectedDataSet(ExtensionContext context) {
        Optional<CdmExpectedDataSet> onMethod = context.getTestMethod()
                .map(m -> AnnotatedElementUtils.findMergedAnnotation(m, CdmExpectedDataSet.class));
        if (onMethod.isPresent()) {
            return onMethod;
        }
        return context.getTestClass()
                .map(c -> AnnotatedElementUtils.findMergedAnnotation(c, CdmExpectedDataSet.class));
    }

    private static List<Resource> resolveResources(ExtensionContext context, String[] explicit,
            String fileAppendix, boolean expected) {
        Class<?> testClass = context.getRequiredTestClass();
        String packagePath = testClass.getPackageName().replace('.', '/');
        List<Resource> resources = new ArrayList<>();
        if (explicit != null && explicit.length > 0) {
            for (String location : explicit) {
                resources.add(toResource(packagePath, location));
            }
            return resources;
        }
        // Unitils defaults:
        //   @DataSet → ClassName.xml (method-specific ClassName.method.xml first, then class)
        //   @ExpectedDataSet → ClassName.methodName-result.xml
        String appendix = fileAppendix == null || fileAppendix.isEmpty() ? "" : "-" + fileAppendix;
        if (expected) {
            String method = context.getRequiredTestMethod().getName();
            resources.add(new ClassPathResource(
                    packagePath + "/" + testClass.getSimpleName() + "." + method + appendix + "-result.xml"));
            return resources;
        }
        String methodSpecific = packagePath + "/" + testClass.getSimpleName() + "."
                + context.getRequiredTestMethod().getName() + appendix + ".xml";
        ClassPathResource methodResource = new ClassPathResource(methodSpecific);
        if (methodResource.exists()) {
            resources.add(methodResource);
            return resources;
        }
        resources.add(new ClassPathResource(packagePath + "/" + testClass.getSimpleName() + appendix + ".xml"));
        return resources;
    }

    private static Resource toResource(String packagePath, String location) {
        String normalized = location.startsWith("/") ? location.substring(1) : location;
        // Unitils: bare filenames are relative to the test class package
        if (!normalized.contains("/")) {
            normalized = packagePath + "/" + normalized;
        }
        return new ClassPathResource(normalized);
    }
}
