// $Id$
/**
* Copyright (C) 2012 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.json;

/**
 * The JsonpRedirect is used to mimic redirects when using json ajax requests.
 *
 * @author andreas
 * @date Jul 20, 2012
 *
 */
public class JsonpRedirect {

    private String redirectURL = null;

    public JsonpRedirect(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

}
