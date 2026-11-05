/**
* Copyright (C) 2026 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.test.integration.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Ensures {@code sessionFactory} is created only after {@link CdmTestSchemaInitializer}.
 */
public class CdmTestSchemaDependsOnPostProcessor implements BeanFactoryPostProcessor {

    public static final String SCHEMA_INITIALIZER_BEAN = "cdmTestSchemaInitializer";
    public static final String SESSION_FACTORY_BEAN = "sessionFactory";

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (!beanFactory.containsBeanDefinition(SESSION_FACTORY_BEAN)
                || !beanFactory.containsBeanDefinition(SCHEMA_INITIALIZER_BEAN)) {
            return;
        }
        String[] dependsOn = beanFactory.getBeanDefinition(SESSION_FACTORY_BEAN).getDependsOn();
        if (dependsOn == null) {
            beanFactory.getBeanDefinition(SESSION_FACTORY_BEAN)
                    .setDependsOn(SCHEMA_INITIALIZER_BEAN);
            return;
        }
        String[] merged = new String[dependsOn.length + 1];
        System.arraycopy(dependsOn, 0, merged, 0, dependsOn.length);
        merged[dependsOn.length] = SCHEMA_INITIALIZER_BEAN;
        beanFactory.getBeanDefinition(SESSION_FACTORY_BEAN).setDependsOn(merged);
    }
}
