package eu.etaxonomy.cdm.remote.dto.assembler;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.remote.dto.TaxonSTO;

@Component
public class TaxonSTOAssembler extends AssemblerBase{
	
	@Autowired
	private NameSTOAssembler nameSTOAssembler;
	
	public TaxonSTO getRandom(){		
		TaxonSTO t = new TaxonSTO();
		t.setUuid(getRandomUUID());
		t.setSecUuid(getRandomUUID());
		t.setName(nameSTOAssembler.getRandom());
		t.setAccepted(true);
		return t;
}

}
