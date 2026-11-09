/*
 * Copyright 2008,  Unitils.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitils.orm.hibernate.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.unitils.core.UnitilsException;
import org.unitils.util.ReflectionUtils;

/**
 * Unitils SessionFactory bean. Avoids {@code setImplicitNamingStrategy(...)} on
 * {@link LocalSessionFactoryBean} because Hibernate 6.2+ changed that method to
 * return {@code Configuration}, which breaks Spring 6.1's void call site.
 * Naming is set via {@link AvailableSettings#IMPLICIT_NAMING_STRATEGY} instead.
 */
public class UnitilsHibernate5LocalSessionFactoryBean extends LocalSessionFactoryBean {

	private Object testObject;

	private Method customConfigMethod;

// ******************* CONSTRUCTOR *********************************/

	/**
     */
    public UnitilsHibernate5LocalSessionFactoryBean() {
        Properties props = new Properties();
        props.setProperty(AvailableSettings.IMPLICIT_NAMING_STRATEGY,
                "org.hibernate.boot.model.naming.ImplicitNamingStrategyComponentPathImpl");
        setHibernateProperties(props);
    }

//****************************** SETTER **********************************/

    public void setTestObject(Object testObject) {
		this.testObject = testObject;
	}

	public void setCustomConfigMethod(Method customConfigMethod) {
		this.customConfigMethod = customConfigMethod;
	}

//*************************** METHOD *******************************/

//	@Override  //Not used, kept to remind that this method existed in hibernate3
	protected void postProcessConfiguration(Configuration config) throws HibernateException {
		if (customConfigMethod != null) {
			try {
			ReflectionUtils.invokeMethod(testObject, customConfigMethod, config);
			} catch (InvocationTargetException e) {
				throw new UnitilsException("Error while invoking custom config method", e.getCause());
			}
		}
	}


}
