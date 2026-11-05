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

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Applies {@link CdmDataSet} / {@link CdmExpectedDataSet} around each test method.
 */
public class CdmDbUnitExtension implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Optional<CdmDataSet> annotation = findDataSet(context);
        if (annotation.isEmpty()) {
            return;
        }
        DataSource dataSource = dataSource(context);
        List<Resource> resources = resolveResources(context, annotation.get().value(),
                annotation.get().fileAppendix(), false);
        if (annotation.get().cleanSweep()) {
            for (Resource resource : resources) {
                CdmDbUnitSupport.cleanSweepInsert(dataSource, resource);
            }
        } else {
            CdmDbUnitSupport.cleanInsert(dataSource, resources.toArray(Resource[]::new));
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        Optional<CdmExpectedDataSet> annotation = findExpectedDataSet(context);
        if (annotation.isEmpty()) {
            return;
        }
        DataSource dataSource = dataSource(context);
        List<Resource> resources = resolveResources(context, annotation.get().value(),
                annotation.get().fileAppendix(), true);
        for (Resource resource : resources) {
            CdmDbUnitSupport.assertEquals(dataSource, resource);
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
        List<Resource> resources = new ArrayList<>();
        if (explicit != null && explicit.length > 0) {
            for (String location : explicit) {
                resources.add(new ClassPathResource(location));
            }
            return resources;
        }
        Class<?> testClass = context.getRequiredTestClass();
        String packagePath = testClass.getPackageName().replace('.', '/');
        String appendix = fileAppendix == null || fileAppendix.isEmpty() ? "" : "-" + fileAppendix;
        String suffix = expected ? "-result.xml" : ".xml";
        String path = packagePath + "/" + testClass.getSimpleName() + appendix + suffix;
        resources.add(new ClassPathResource(path));
        return resources;
    }
}
