package eu.etaxonomy.cdm.remote.dto.assembler;

import java.util.UUID;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.remote.dto.NameSTO;

@Component
public class NameSTOAssembler extends AssemblerBase{
	
	public NameSTO getRandom(){
		NameSTO n = new NameSTO();
		n.setUuid(getRandomUUID());
		n.setFullname("Maria magdalena subsp. hebrea L.");
		n.setNomenclaturalReference(null);
		return n;
	}
}
