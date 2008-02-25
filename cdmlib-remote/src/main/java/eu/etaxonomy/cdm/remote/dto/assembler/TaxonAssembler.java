package eu.etaxonomy.cdm.remote.dto.assembler;

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

	public TaxonSTO getSTO(TaxonBase taxonBase){		
		TaxonSTO t = new TaxonSTO();
		setIdentifiableEntity(taxonBase, t);
		t.setName(nameAssembler.getSTO(taxonBase.getName()));
		//TODO: add more mapppings
		t.setSecUuid(getRandomUUID());
		t.setAccepted(true);
		return t;
	}
	public TaxonTO getTO(TaxonBase taxonBase){		
		TaxonTO t = new TaxonTO();
		setIdentifiableEntity(taxonBase, t);
		t.setName(nameAssembler.getSTO(taxonBase.getName()));
		//TODO: add more mapppings
		return t;
	}
	public TreeNode getTreeNode(Taxon taxon){
		// TODO:
		TreeNode tn = new TreeNode();
		return tn;
	}
}
