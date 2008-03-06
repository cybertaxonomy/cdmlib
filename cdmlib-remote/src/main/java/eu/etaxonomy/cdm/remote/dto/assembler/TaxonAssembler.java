package eu.etaxonomy.cdm.remote.dto.assembler;

import java.util.ArrayList;
import java.util.List;
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
	
	public TaxonSTO getRandom(){		
		TaxonSTO t = new TaxonSTO();
		t.setUuid(getRandomUUID());
		t.setSecUuid(getRandomUUID());
		t.setName(nameAssembler.getRandom());
		t.setAccepted(true);
		return t;
	}

	public TaxonSTO getSTO(TaxonBase tb){		
		TaxonSTO t = null;
		if (tb!=null){
			t = new TaxonSTO();
			setIdentifiableEntity(tb, t);
			t.setName(nameAssembler.getSTO(tb.getName()));
			//TODO: add more mapppings
			t.setSecUuid(getRandomUUID());
			t.setAccepted(true);
		}
		return t;
	}
	public TaxonTO getTO(TaxonBase tb){
		TaxonTO t = null;
		if(tb!=null){
			t = new TaxonTO();
			setIdentifiableEntity(tb, t);
			t.setName(nameAssembler.getSTO(tb.getName()));
			//TODO: add more mapppings
		}
		return t;
	}
	public TreeNode getTreeNode(Taxon t){
		TreeNode tn = null;
		if(t!=null){
			tn = new TreeNode();
			setIdentifiableEntity(t, tn);
			tn.setFullname(t.getTitleCache());
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
}
