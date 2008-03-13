package eu.etaxonomy.cdm.remote.dto.assembler;

import java.util.ArrayList;
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
public class TaxonAssembler extends AssemblerBase{
	
	@Autowired
	private NameAssembler nameAssembler;
	@Autowired
	private ReferenceAssembler refAssembler;
	
	public TaxonSTO getSTO(TaxonBase tb){		
		TaxonSTO t = null;
		if (tb!=null){
			t = new TaxonSTO();
			setIdentifiableEntity(tb, t);
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
			setIdentifiableEntity(tb, t);
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
			setIdentifiableEntity(t, tn);
			tn.setFullname(t.getName().getTitleCache());
			tn.setHasChildren(t.getTaxonomicChildren().size());
			tn.setTaggedName(nameAssembler.getTaggedName(t.getName()));
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
		Map<String, TreeNode> results = new HashMap<String, TreeNode>();
		for (Taxon t : taxa){
			results.put(t.getTitleCache()+t.getUuid().toString(), this.getTreeNode(t));
		}
		ArrayList<TreeNode> treeNodeList = new ArrayList<TreeNode>(results.values());
		return treeNodeList;
	}

}
