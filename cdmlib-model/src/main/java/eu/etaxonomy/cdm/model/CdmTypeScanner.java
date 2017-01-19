/**
* Copyright (C) 2014 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.util.ClassUtils;

/**
 * @author a.kohlbecker
 * @date Jul 31, 2014
 *
 */
public class CdmTypeScanner<T> extends ClassPathScanningCandidateComponentProvider {

    static  final String defaultBasePackage = "eu/etaxonomy/cdm/";

    boolean includeAbstract;
    boolean includeInterfaces;

    public CdmTypeScanner(boolean considerAbstract, boolean considerInterfaces) {
        super(false);
        this.includeAbstract = considerAbstract;
        this.includeInterfaces = considerInterfaces;
    }

    public final Collection<Class<? extends T>> scanTypesIn(String basePackage) {
        String _basePackage = basePackage == null ? defaultBasePackage : basePackage;
        List<Class<? extends T>> classes = new ArrayList<Class<? extends T>>();
        for (BeanDefinition candidate : findCandidateComponents(_basePackage)) {
                Class cls = ClassUtils.resolveClassName(candidate.getBeanClassName(),
                        ClassUtils.getDefaultClassLoader());
                classes.add(cls);
        }
        return classes;
    }

    /**
     * Determine whether the given bean definition qualifies as candidate.
     * <p>The special implementation checks whether the class is concrete
     * or abstract or an interface. The latter two conditions depend on
     * the state of the two boolean fields includeAnstract, includeInterface.
     *
     * @param beanDefinition the bean definition to check
     * @return whether the bean definition qualifies as a candidate component
     */
    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return (beanDefinition.getMetadata().isIndependent()
                && (beanDefinition.getMetadata().isConcrete()
                  || (includeAbstract && beanDefinition.getMetadata().isAbstract())
                  || (includeInterfaces && beanDefinition.getMetadata().isInterface())
                 )
                );
    }

    }
