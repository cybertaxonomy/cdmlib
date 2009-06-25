/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.TaxonomicTree;

/**
 * @author a.mueller
 * @created 01.07.2008
 * @version 1.0
 */
public abstract class CdmImportBase<CONFIG extends IImportConfigurator, STATE extends ImportStateBase> extends CdmIoBase<STATE> implements ICdmImport<CONFIG, STATE>{
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CdmImportBase.class);

	protected TaxonomicTree makeTree(STATE state, ReferenceBase ref){
		String treeName = "TaxonTree (Import)";
		if (ref != null && CdmUtils.isNotEmpty(ref.getTitleCache())){
			treeName = ref.getTitleCache();
		}
		TaxonomicTree tree = TaxonomicTree.NewInstance(treeName);
		tree.setReference(ref);
		
		getTaxonService().saveTaxonomicTree(tree);
		state.putTree(ref, tree);
		return tree;
	}
	
}
