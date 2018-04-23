/**
* Copyright (C) 2016 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto.common;

/**
 * @author a.mueller
 * @since 25.11.2016
 *
 */
public class StringResultDTO {

    private String result;

    /**
     * @param result
     */
    public StringResultDTO(String result) {
        this.result = result;
    }

    /**
     * @return the result
     */
    public String getResult() {
        return result;
    }

}
