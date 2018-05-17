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
 *
 */
public class ApplicationConfigurationFile {

    String defaultContet =
            "#   application configuration file    #\n" +
            "######################################";

    String fileName;

    /**
     * @param defaultContet
     * @param fileName
     */
    public ApplicationConfigurationFile(String fileName, String defaultContet) {
        this.defaultContet = defaultContet;
        this.fileName = fileName;
    }

    /**
     * @return the defaultContet
     */
    public String getDefaultContet() {
        return defaultContet;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

}
