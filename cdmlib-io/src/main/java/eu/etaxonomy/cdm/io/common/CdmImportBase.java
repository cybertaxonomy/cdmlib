/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common;

import java.util.UUID;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.common.CdmUtils;
import eu.etaxonomy.cdm.model.common.ExtensionType;
import eu.etaxonomy.cdm.model.common.MarkerType;
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
		

		// use defined uuid for first tree
		CONFIG config = (CONFIG)state.getConfig();
		if (state.countTrees() < 1 ){
			tree.setUuid(config.getTreeUuid());
		}
		getTaxonTreeService().save(tree);
		state.putTree(ref, tree);
		return tree;
	}
	
	
	/**
	 * Alternative memory saving method variant of
	 * {@link #makeTree(STATE state, ReferenceBase ref)} which stores only the
	 * UUID instead of the full tree in the <code>ImportStateBase</code> by 
	 * using <code>state.putTreeUuid(ref, tree);</code>
	 * 
	 * @param state
	 * @param ref
	 * @return
	 */
	protected TaxonomicTree makeTreeMemSave(STATE state, ReferenceBase ref){
		String treeName = "TaxonTree (Import)";
		if (ref != null && CdmUtils.isNotEmpty(ref.getTitleCache())){
			treeName = ref.getTitleCache();
		}
		TaxonomicTree tree = TaxonomicTree.NewInstance(treeName);
		tree.setReference(ref);
		

		// use defined uuid for first tree
		CONFIG config = (CONFIG)state.getConfig();
		if (state.countTrees() < 1 ){
			tree.setUuid(config.getTreeUuid());
		}
		getTaxonTreeService().save(tree);
		state.putTreeUuid(ref, tree);
		return tree;
	}
	
	
	protected ExtensionType getExtensionType(STATE state, UUID uuid, String label, String text, String labelAbbrev){
		ExtensionType extensionType = state.getExtensionType(uuid);
		if (extensionType == null){
			extensionType = (ExtensionType)getTermService().find(uuid);
			if (extensionType == null){
				extensionType = new ExtensionType(label, text, labelAbbrev);
				extensionType.setUuid(uuid);
				getTermService().save(extensionType);
				state.putExtensionType(extensionType);
			}
		}
		return extensionType;
	}
	
	protected MarkerType getMarkerType(STATE state, UUID uuid, String label, String text, String labelAbbrev){
		MarkerType markerType = state.getMarkerType(uuid);
		if (markerType == null){
			markerType = (MarkerType)getTermService().find(uuid);
			if (markerType == null){
				markerType = MarkerType.NewInstance(label, text, labelAbbrev);
				markerType.setUuid(uuid);
				getTermService().save(markerType);
				state.putMarkerType(markerType);
			}
		}
		return markerType;
	}
	
}
