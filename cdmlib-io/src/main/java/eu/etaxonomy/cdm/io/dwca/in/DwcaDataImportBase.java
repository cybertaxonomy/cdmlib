/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.io.stream.IItemStream;
import eu.etaxonomy.cdm.io.stream.StreamImportBase;

/**
 *
 * @author a.mueller
 *
 */
public abstract class DwcaDataImportBase<CONFIG extends DwcaDataImportConfiguratorBase<STATE>, STATE extends DwcaDataImportStateBase<CONFIG>> extends StreamImportBase<CONFIG, STATE>{

    private static final long serialVersionUID = 8816075241549849925L;
    @SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DwcaDataImportBase.class);


	@Override
	protected void finalizeStream(IItemStream itemStream, STATE state) {
		fireWarningEvent("Stream finished", itemStream.getStreamLocation(), 0);
		if (itemStream.getTerm().equals(TermUri.DWC_TAXON)){
			if (state.isTaxaCreated() == false){
				state.setTaxaCreated(true);
			}
		}
	}

}
