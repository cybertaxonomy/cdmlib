// $Id$
/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.batch.validation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Validator;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.ICdmApplicationConfiguration;
import eu.etaxonomy.cdm.api.service.IService;
import eu.etaxonomy.cdm.model.common.ICdmBase;

/**
 * @author ayco_holleman
 * @date 28 jan. 2015
 *
 */
class BatchValidationUtil {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(BatchValidationUtil.class);

    public static List<IService<?>> getAvailableServices(ICdmApplicationConfiguration appConfig)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        List<IService<?>> services = new ArrayList<IService<?>>();
        Method[] methods = appConfig.getClass().getMethods();
        for (Method method : methods) {
            if (isGetterForService(method)) {
                IService<?> service = (IService<?>) method.invoke(appConfig);
                services.add(service);
            }
        }
        return services;
    }

    public static Class<?> getServicedEntity(Class<? extends IService<?>> serviceClass) {
        Type type = ((ParameterizedType) serviceClass.getGenericInterfaces()[0]).getActualTypeArguments()[0];
        return (Class<?>) type;
    }

    public static boolean isConstrainedEntityClass(Validator validator, Class<? extends ICdmBase> entityClass) {
        return validator.getConstraintsForClass(entityClass).hasConstraints();
    }

    private static boolean isGetterForService(Method method) {
        if (method.getParameterTypes().length != 0) {
            return false;
        }
        if (!method.getName().startsWith("get")) {
            return false;
        }
        if (!IService.class.isAssignableFrom(method.getReturnType())) {
            return false;
        }
        return true;
    }

    // @SuppressWarnings("unchecked")
    // private static ArrayList<Class<? extends ICdmBase>> getEntityClasses(File
    // f, String packageName) {
    // ArrayList<Class<? extends ICdmBase>> result = new ArrayList<Class<?
    // extends ICdmBase>>();
    // if (f == null) {
    // URL url = CdmBaseType.class.getResource("CdmBaseType.class");
    // f = new File(URI.create(url.toString())).getParentFile();
    // packageName = "eu.etaxonomy.cdm.";
    // }
    // if (f.isDirectory()) {
    // packageName += f.getName() + ".";
    // File[] dirContents = f.listFiles();
    // for (File file : dirContents) {
    // result.addAll(getEntityClasses(file, packageName));
    // }
    // } else if (f.getName().endsWith(".class")) {
    // String className = packageName + f.getName().substring(0,
    // f.getName().length() - 6);
    // try {
    // Class<?> cls = Class.forName(className);
    // if (ICdmBase.class.isAssignableFrom(cls)) {
    // logger.debug("Loading entity class: " + className);
    // result.add((Class<? extends ICdmBase>) cls);
    // }
    // } catch (ClassNotFoundException e) {
    // // Huh?
    // logger.error("Class file found but could not be loaded with system class loader: "
    // + className);
    // }
    // }
    // return result;
    // }

}
