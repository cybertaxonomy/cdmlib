// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.api.application.eclipse;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.osgi.framework.internal.core.*;
import org.springframework.core.*;
import org.springframework.core.io.*;
import org.springframework.core.io.support.*;

public class EclipseRcpSavePathMatchingResourcePatternResolver extends PathMatchingResourcePatternResolver 
{
    public EclipseRcpSavePathMatchingResourcePatternResolver(ResourceLoader resourceLoader) 
    {
        super(resourceLoader);
    }
    
    protected Resource[] findAllClassPathResources(String location) throws IOException 
    {
        String path = location;
        
        if (path.startsWith("/")) 
        {
            path = path.substring(1) ;
        }
        
        Enumeration<URL> resourceUrls = getClassLoader().getResources(path);
        Set result = CollectionFactory.createLinkedSetIfPossible(16);
        while (resourceUrls.hasMoreElements())  
        {
            URL url = (URL) resourceUrls.nextElement();
            
            if (url.getProtocol().startsWith("bundleresource")){
                //handle eclipse bundleresource:
            	URLConnection con = url.openConnection();
            	BundleURLConnection bundleCon = (BundleURLConnection) con;
            	// Convert the bundle URL into a file system URL.
            	result.add(new UrlResource(bundleCon.getFileURL()));
            }else{
            	result.add(convertClassLoaderURL(url));
            }
        }
        
        return (Resource[]) result.toArray(new Resource[result.size()]);
    }
}