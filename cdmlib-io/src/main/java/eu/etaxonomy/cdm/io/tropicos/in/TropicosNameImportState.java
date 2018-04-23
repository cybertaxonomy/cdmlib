/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.tropicos.in;

import eu.etaxonomy.cdm.io.csv.in.CsvImportState;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;

/**
 * @author a.mueller
 \* @since 15.11.2017
 *
 */
public class TropicosNameImportState extends CsvImportState<TropicosNameImportConfigurator> {

    private TaxonNode parentNode;


    /**
     * @param config
     */
    protected TropicosNameImportState(TropicosNameImportConfigurator config) {
        super(config);
    }


    @Override
    public void resetSession(){
        this.parentNode = null;
        super.resetSession();
    }

    public TaxonNode getParentNode(){
        return this.parentNode;
    }
    public void setParentNode (TaxonNode parentNode){
        this.parentNode = parentNode;
    }





}
