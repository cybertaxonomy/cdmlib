/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.ext.registration.messages.redmine;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;

import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.internal.Transport;

import eu.etaxonomy.cdm.ext.common.ExternalServiceException;

/**
 * Temporary solution to allow resetting the assignee despite the redmine-java-api issue
 * <a href="https://github.com/taskadapter/redmine-java-api/issues/306">taskadapter/redmine-java-api #306</a>
 * blocking this.
 *
 * @author a.kohlbecker
 * @since Feb 20, 2018
 *
 */
public class RedmineTransportAccessor {

    Field transportField;
    private Method sendMethod;
    Method setEntityMethod;

    Transport transport;


    public RedmineTransportAccessor(RedmineManager manager){
        initFieldsAndMethods();
        try {
            transport = (Transport) transportField.get(manager);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     *
     */
    private void initFieldsAndMethods() {
        try {
            transportField = RedmineManager.class.getDeclaredField("transport");
            transportField.setAccessible(true);

            setEntityMethod = Transport.class.getDeclaredMethod("setEntity", HttpEntityEnclosingRequest.class, String.class);
            setEntityMethod.setAccessible(true);

            sendMethod = Transport.class.getDeclaredMethod("send", HttpRequestBase.class);
            sendMethod.setAccessible(true);

        } catch (NoSuchFieldException | SecurityException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

    }

    public void httpPut(URI uri, String body) throws ExternalServiceException{
        final HttpPut http = new HttpPut(uri);
        try {
            setEntityMethod.invoke(transport, http, body);
            sendMethod.invoke(transport, http);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new ExternalServiceException(uri.toString(), e);
        }

    }


}
