/**
* Copyright (C) 2020 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.remoting;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.search.util.common.SearchException;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationExecutor;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import eu.etaxonomy.cdm.api.service.UpdateResult;
import eu.etaxonomy.cdm.model.description.DescriptionBase;
import eu.etaxonomy.cdm.model.description.Distribution;

/**
 * Alternative implementation of the {@link RemoteInvocationExecutor} interface which behaves exactly
 * as the {@link org.springframework.remoting.support.DefaultRemoteInvocationExecutor} with the
 * additional capability of measuring the execution of the method invocation.
 * <p>
 * The execution duration in milliseconds will be written to the log when the log level for this
 * class is set to <code>DEBUG</code>.
 * <p>
 * Also converts Hibernate Search {@link SearchException}s (which embed a non-serializable
 * {@code EventContext}) into plain {@link RuntimeException}s so HttpInvoker can marshal them
 * to remoting clients (e.g. TaxEditor).
 *
 * @author a.kohlbecker
 * @since Feb 17, 2020
 */
public class DebuggingRemoteInvocationExecutor implements RemoteInvocationExecutor {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public Object invoke(RemoteInvocation invocation, Object targetObject)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException{

        Assert.notNull(invocation, "RemoteInvocation must not be null");
        Assert.notNull(targetObject, "Target object must not be null");

        boolean doMeasure = logger.isDebugEnabled();
        String targetInvocationStr = null;
        long start = 0;
        if(doMeasure) {
            targetInvocationStr = targetCdmServiceInterfaces(targetObject) + "#" + invocation.getMethodName() + "(" +
                    ClassUtils.classNamesToString(invocation.getParameterTypes()) + ")";
            Object[] attributes =  invocation.getArguments();

            if (attributes != null) {
                String attributeListString = "";
                for (Object o:attributes) {
                    if (o instanceof ArrayList) {
                        for (Object object: (ArrayList<?>)o) {
                            if (object instanceof DescriptionBase) {
                                DescriptionBase<?> desc = (DescriptionBase<?>)object;
//                                String descLabel = desc.getTitleCache();
                                attributeListString += " Description UUID: " + desc.getUuid() + " \n ";
                                for (Object element: desc.getElements()) {
                                    if (element instanceof Distribution) {
                                        Distribution dist = (Distribution)element;
                                        String area = dist.getArea()!= null? dist.getArea().getLabel(): "";
                                        String status = dist.getStatus()!= null? dist.getStatus().getLabel(): "";
                                        attributeListString += "  Distribution UUID: " + dist.getUuid() + ", Area: " + area + ", Status: " + status + "\n";
                                    }
                                }
                            }
                        }
                    }
                }

                targetInvocationStr = targetInvocationStr + attributeListString;
            }
            logger.debug("invoking: " + targetInvocationStr);
            start = System.currentTimeMillis();
        }
        try {
            Object invocationResult = invocation.invoke(targetObject);
            if (doMeasure) {
                logger.debug("invocation: " + targetInvocationStr + " completed [" + (System.currentTimeMillis() - start) + " ms]");
            }
            sanitizeReturnValue(invocationResult);
            return invocationResult;
        } catch (InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (containsSearchException(target)) {
                RuntimeException remotingSafe = toRemotingSafeException(target);
                logger.warn("Replacing non-serializable Hibernate Search exception for remoting: "
                        + remotingSafe.getMessage());
                throw new InvocationTargetException(remotingSafe);
            }
            throw e;
        }
    }

    /**
     * {@link UpdateResult} may carry {@link SearchException}s that later fail HttpInvoker
     * serialization. Replace them in place.
     */
    private void sanitizeReturnValue(Object invocationResult) {
        if (invocationResult instanceof UpdateResult) {
            UpdateResult updateResult = (UpdateResult) invocationResult;
            ArrayList<Exception> original = new ArrayList<>(updateResult.getExceptions());
            updateResult.getExceptions().clear();
            for (Exception exception : original) {
                if (containsSearchException(exception)) {
                    updateResult.addException(toRemotingSafeException(exception));
                } else {
                    updateResult.addException(exception);
                }
            }
        }
    }

    /**
     * Recursively replaces {@link SearchException} (and subclasses) with a plain
     * {@link RuntimeException} carrying the same message and stack trace. SearchException
     * holds a non-{@link java.io.Serializable} {@code EventContext} field which breaks
     * Java serialization used by Spring HttpInvoker.
     */
    static RuntimeException toRemotingSafeException(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        Throwable cause = throwable.getCause();
        Throwable safeCause = null;
        if (cause != null && cause != throwable) {
            safeCause = containsSearchException(cause) ? toRemotingSafeException(cause) : cause;
        }
        if (isSearchException(throwable)) {
            RuntimeException replacement = new RuntimeException(
                    throwable.getClass().getName() + ": " + throwable.getMessage(),
                    safeCause);
            replacement.setStackTrace(throwable.getStackTrace());
            return replacement;
        }
        if (safeCause != null && safeCause != cause) {
            RuntimeException replacement = new RuntimeException(
                    throwable.getClass().getName() + ": " + throwable.getMessage(),
                    safeCause);
            replacement.setStackTrace(throwable.getStackTrace());
            return replacement;
        }
        if (throwable instanceof RuntimeException) {
            return (RuntimeException) throwable;
        }
        RuntimeException replacement = new RuntimeException(throwable.getMessage(), throwable);
        replacement.setStackTrace(throwable.getStackTrace());
        return replacement;
    }

    static boolean containsSearchException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (isSearchException(current)) {
                return true;
            }
            Throwable next = current.getCause();
            if (next == current) {
                break;
            }
            current = next;
        }
        return false;
    }

    private static boolean isSearchException(Throwable throwable) {
        return throwable instanceof SearchException;
    }

    private String targetCdmServiceInterfaces(Object targetObject) {
        String interfacesStr = targetObject.getClass().getName();
        if(interfacesStr.contains("$Proxy")){
            Class<?>[] intfs = targetObject.getClass().getInterfaces();
            interfacesStr = Arrays.stream(intfs).map(c -> c.getName())
                    .filter(n -> n.startsWith("eu.etaxonomy.cdm.api.service."))
                    .collect( Collectors.joining( "," ) );
        }
        return interfacesStr;
    }
}
