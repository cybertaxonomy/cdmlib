/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.api.config;

/**
 * @author a.kohlbecker
 * @since Feb 16, 2018
 */
public class ApplicationConfigurationFile {

    private String defaultContet =
            "#   application configuration file    #\n" +
            "######################################";

    private String fileName;

    public ApplicationConfigurationFile(String fileName, String defaultContet) {
        this.defaultContet = defaultContet;
        this.fileName = fileName;
    }

    public String getDefaultContet() {
        return defaultContet;
    }

    public String getFileName() {
        return fileName;
    }
}