package eu.etaxonomy.cdm.remote.dto.assembler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.remote.dto.TaxonSTO;
import eu.etaxonomy.cdm.remote.dto.TaxonTO;
import eu.etaxonomy.cdm.remote.dto.TreeNode;

@Component
public class TaxonAssembler extends AssemblerBase<TaxonSTO, TaxonTO, TaxonBase>{
	
	@Autowired
	private NameAssembler nameAssembler;
	@Autowired
	private ReferenceAssembler refAssembler;
	
	public TaxonSTO getSTO(TaxonBase tb){		
		TaxonSTO t = null;
		if (tb!=null){
			t = new TaxonSTO();
			setVersionableEntity(tb, t);
			t.setName(nameAssembler.getSTO(tb.getName()));
			t.setSecUuid(tb.getSec().getUuid().toString());
			if (Taxon.class.isInstance(tb)){
				t.setAccepted(true);
			}else{
				t.setAccepted(false);
			}
			//TODO: add more mapppings
		}
		return t;
	}
	public TaxonTO getTO(TaxonBase tb){
		TaxonTO t = null;
		if(tb!=null){
			t = new TaxonTO();
			setVersionableEntity(tb, t);
			t.setName(nameAssembler.getSTO(tb.getName()));
			t.setSec(refAssembler.getTO(tb.getSec()));
			//TODO: add more mapppings
		}
		return t;
	}
	public TreeNode getTreeNode(Taxon t){
		TreeNode tn = null;
		if(t!=null){
			tn = new TreeNode();
			setVersionableEntity(t, tn);
			tn.setFullname(t.getName().getTitleCache());
			tn.setHasChildren(t.getTaxonomicChildren().size());
			tn.setTaggedName(nameAssembler.getTaggedName(t.getName()));
			tn.setSecUuid(t.getSec().getUuid().toString());
		}
		return tn;
	}
	public List<TreeNode> getTreeNodeList(Taxon[] taxa){
		ArrayList<TreeNode> result = new ArrayList<TreeNode>();
		for (Taxon t : taxa){
			result.add(this.getTreeNode(t));
		}
		return result;
	}

	public List<TreeNode> getTreeNodeListSortedByName(Iterable<Taxon> taxa){
		Map<String, TreeNode> nameMap = new HashMap<String, TreeNode>();
		for (Taxon t : taxa){
			nameMap.put(t.getTitleCache()+"  "+t.getUuid().toString(), this.getTreeNode(t));
		}
		ArrayList<TreeNode> treeNodeList = new ArrayList<TreeNode>();
		ArrayList<String> keys = new ArrayList<String>(nameMap.keySet());
		Collections.sort(keys);
		for (String name : keys){
			treeNodeList.add(nameMap.get(name));
		}
		return treeNodeList;
	}

}
