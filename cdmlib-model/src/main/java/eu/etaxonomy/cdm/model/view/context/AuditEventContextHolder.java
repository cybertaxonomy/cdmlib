/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.view.context;

import java.lang.reflect.Constructor;

import org.springframework.util.ReflectionUtils;

/**
 * Class based heavily on SecurityContextHolder, part
 * of spring-security, but instead binding an AuditEvent object to the
 * context.
 *
 * @author ben
 * @author Ben Alex
 *
 */
public class AuditEventContextHolder {
	public static final String MODE_THREADLOCAL = "MODE_THREADLOCAL";
	public static final String MODE_INHERITABLETHREADLOCAL = "MODE_INHERITABLETHREADLOCAL";
    public static final String MODE_GLOBAL = "MODE_GLOBAL";
    public static final String SYSTEM_PROPERTY = "cate.view.strategy";
    private static String strategyName = System.getProperty(SYSTEM_PROPERTY);
    private static AuditEventContextHolderStrategy strategy;
    private static int initializeCount = 0;

    static {
    	initialize();
    }

    public static void clearContext() {
        strategy.clearContext();
    }

    public static AuditEventContext getContext() {
        return strategy.getContext();
    }

    public static int getInitializeCount() {
        return initializeCount;
    }

    private static void initialize() {
        if ((strategyName == null) || "".equals(strategyName)) {
            // Set default
            strategyName = MODE_THREADLOCAL;
        }

        if (strategyName.equals(MODE_THREADLOCAL)) {
            strategy = new ThreadLocalAuditEventContextHolderStrategy();
        } else if (strategyName.equals(MODE_INHERITABLETHREADLOCAL)) {
            strategy = new InheritableThreadLocalAuditEventContextHolderStrategy();
        } else if (strategyName.equals(MODE_GLOBAL)) {
            strategy = new GlobalAuditEventContextHolderStrategy();
        } else {
            // Try to load a custom strategy
            try {
                Class clazz = Class.forName(strategyName);
                Constructor customStrategy = clazz.getConstructor(new Class[] {});
                strategy = (AuditEventContextHolderStrategy) customStrategy.newInstance(new Object[] {});
            } catch (Exception ex) {
                ReflectionUtils.handleReflectionException(ex);
            }
        }

        initializeCount++;
    }

    public static void setContext(AuditEventContext context) {
        strategy.setContext(context);
    }

    public static void setStrategyName(String strategyName) {
        AuditEventContextHolder.strategyName = strategyName;
        initialize();
    }

    @Override
    public String toString() {
        return "AuditEventContextHolder[strategy='" + strategyName + "'; initializeCount=" + initializeCount + "]";
    }
}
