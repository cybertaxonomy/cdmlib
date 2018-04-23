/**
* Copyright (C) 2017 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.common;

/**
 * @author a.mueller
 \* @since 14.03.2017
 *
 */
public class EmptyImportState<CONFIG extends ImportConfiguratorBase, IO extends CdmImportBase>
        extends ImportStateBase<CONFIG, IO> {

    /**
     * @param config
     */
    protected EmptyImportState(CONFIG config) {
        super(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CONFIG getConfig() {
        return super.getConfig();
    }



}
