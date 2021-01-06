/**
* Copyright (C) 2021 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.common;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * See https://dev.e-taxonomy.eu/redmine/issues/9114
 *
 * @author a.mueller
 * @since 05.01.2021
 */
public class URI
        implements Comparable<URI>, Serializable {

    private static final long serialVersionUID = -8002215586516542076L;

    private final java.net.URI javaUri;

 // ***************************** FACTORY METHODS ***************************************/

    public static URI fromString(String uri) throws URISyntaxException {
        return new URI(uri);
    }

    /**
     * Factory method.
     *
     * @see java.net.URI#create(String)
     */
    public static URI create(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static URI fromUrl(URL url) throws URISyntaxException {
        return new URI(url);
    }

    public static URI fromFile(File file) {
        return new URI(file.toURI());
    }

 // ******************************* CONSTRUCTOR ************************************/

    @SuppressWarnings("unused")
    private URI(){javaUri = null;} //empty constructor required for JAXB (not tested but copied from DOI)

    public URI(String uriString) throws URISyntaxException {
        javaUri = parseUriString(uriString);
    }

    public URI(java.net.URI javaUri) {
        this.javaUri = javaUri;
    }

    //TODO maybe we can do encoding in between
    public URI(URL url) throws URISyntaxException {
        this.javaUri = url.toURI();
    }

    /**
     * @see java.net.URI#URI(String, String, String, int, String, String, String)
     */
    public URI(String protocol, String userInfo, String host, int port, String path, String query, String ref) throws URISyntaxException {
        javaUri = new java.net.URI(protocol, userInfo, host, port, path, query, ref);
    }

    public URI(String scheme, String authority, String path, String query, String fragment) throws URISyntaxException{
        javaUri = new java.net.URI(scheme, authority, path, query, fragment);
    }

//************************************ GETTER ***********************************/

    public java.net.URI getJavaUri(){
        return javaUri;
    }

//************************************ METHODS ************************************/

    private java.net.URI parseUriString(String uriString) throws URISyntaxException {
        java.net.URI javaUri = null;
        if(uriString != null){
            try{
                javaUri = new java.net.URI(uriString);
            }catch(Exception e){
                try {
                    String encodedUri = uriString;
                    URL url = new URL(encodedUri);
                    String[] pathElements =  url.getPath().split("/");
                    for (String element: pathElements){
                        if(element.contains("\\")){
                            //TODO needs discussion if backslash should be converted to slash instead, for now we keep it more strict
                            throw new URISyntaxException(uriString, "URI path must not contain backslash ('\')");
                        }
                        String replacement = UrlUtf8Coder.encode(element);
                        encodedUri = encodedUri.replace(element, replacement);
                    }
                    if (url.getQuery() != null){
                        encodedUri = encodedUri.replace(url.getQuery(), UrlUtf8Coder.encode(url.getQuery()));
                    }
                    url = new URL(encodedUri);

                    javaUri = url.toURI();
                } catch (MalformedURLException e1) {
                    throw new URISyntaxException(uriString, e1.getMessage());
                }
            }
        }
        return javaUri;
    }

    public File toFile(){
        return new File(javaUri);
    }

//******************************** Wrapper methods *********************/

    public URL toURL() throws MalformedURLException{
        return javaUri.toURL();
    }

    public String getHost() {
        return javaUri.getHost();
    }

    public int getPort() {
        return javaUri.getPort();
    }

    public String getScheme() {
        return javaUri.getScheme();
    }

    public boolean isAbsolute() {
        return javaUri.isAbsolute();
    }

    public String getPath() {
        return javaUri.getPath();
    }

    public String getFragment() {
        return javaUri.getFragment();
    }

    public Object getQuery() {
        return javaUri.getQuery();
    }


//****************************** equals *****************************/

    @Override
    public int hashCode() {
        return javaUri.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof URI){
            return javaUri.equals(((URI)obj).javaUri);
        }
        return false;
    }

    @Override
    public int compareTo(URI that) {
        return this.javaUri.compareTo(that.javaUri);
    }

//********************** clone ***********************************/

    @Override
    protected URI clone() throws CloneNotSupportedException {
        return new URI(this.javaUri);
    }

//******************************** toString ****************************/

    @Override
    public String toString() {
        return javaUri.toString();
    }


}