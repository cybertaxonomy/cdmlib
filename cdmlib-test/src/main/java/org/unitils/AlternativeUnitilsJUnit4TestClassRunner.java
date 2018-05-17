/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package org.unitils;

import java.io.IOException;
import java.io.InputStream;

import org.junit.internal.runners.InitializationError;
import org.junit.runner.notification.RunNotifier;
import org.unitils.UnitilsJUnit4TestClassRunner;

/**
 * The CDM integration tests can not yet use the {@link org.springframework.test.context.junit4.SpringJUnit4ClassRunner SpringJUnit4ClassRunner}
 * provided by the spring test framework. Due to this it is not possible to use
 * {@code @TestPropertySource(properties = {"myproperty = foo"})} to inject properties into the spring environment which are
 * needed to configure the tests. This <code>ClassRunner</code> is simple alternative which also allows to inject
 * properties into the spring environments. In contrast to the {@link org.springframework.test.context.junit4.SpringJUnit4ClassRunner SpringJUnit4ClassRunner}
 * it loads the properties from a test resource file named <code>spring-environment.mock.properties</code>
 *
 *
 * @author a.kohlbecker
 * @since Nov 23, 2017
 *
 */
public class AlternativeUnitilsJUnit4TestClassRunner extends UnitilsJUnit4TestClassRunner {

    /**
     * @param testClass
     * @throws InitializationError
     */
    public AlternativeUnitilsJUnit4TestClassRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(RunNotifier notifier) {

        loadSystemPropertiesFrom("spring-environment.mock.properties");
        super.run(notifier);
    }

    /**
     * @param propFile
     */
    protected void loadSystemPropertiesFrom(String propFile) {
        InputStream inStream = this.getClass().getClassLoader().getResourceAsStream(propFile);
        //Properties props = new Properties();
        try {
            // props.load(inStream);
            System.getProperties().load(inStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}
