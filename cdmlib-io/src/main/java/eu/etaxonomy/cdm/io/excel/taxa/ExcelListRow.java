/**
* Copyright (C) 2018 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.excel.taxa;

import eu.etaxonomy.cdm.io.excel.common.ExcelRowBase;

/**
 * @author k.luther
 \* @since 21.02.2018
 *
 */
public class ExcelListRow extends ExcelRowBase {

    String name;
    String correctNameifSynonym;


    public ExcelListRow(){

    }


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }


    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * @return the correctNameifSynonym
     */
    public String getCorrectNameifSynonym() {
        return correctNameifSynonym;
    }


    /**
     * @param correctNameifSynonym the correctNameifSynonym to set
     */
    public void setCorrectNameifSynonym(String correctNameifSynonym) {
        this.correctNameifSynonym = correctNameifSynonym;
    }

}
