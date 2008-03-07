package eu.etaxonomy.cdm.remote.dto.assembler;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.agent.Address;
import eu.etaxonomy.cdm.model.common.Media;
import eu.etaxonomy.cdm.model.common.MediaInstance;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.remote.dto.IReferenceSTO;
import eu.etaxonomy.cdm.remote.dto.IdentifiedString;
import eu.etaxonomy.cdm.remote.dto.NameSTO;
import eu.etaxonomy.cdm.remote.dto.ReferenceSTO;
import eu.etaxonomy.cdm.remote.dto.ReferenceTO;

@Component
public class ReferenceAssembler extends AssemblerBase {
	static Logger logger = Logger.getLogger(ReferenceAssembler.class);

	public ReferenceSTO getSTO(ReferenceBase rb){
		ReferenceSTO r = null;
		if (rb !=null){
			r = new ReferenceSTO();
			fillReferenceSTO(r,rb);
			//TODO: add STO specific mappings here
		}
		return r;
	}	
	
	public ReferenceTO getTO(ReferenceBase rb){		
		ReferenceTO r = null;
		if (rb !=null){
			r = new ReferenceTO();
			fillReferenceSTO(r,rb);
			//TODO: add TO specific mappings here
		}
		return r;
	}	
	private IReferenceSTO fillReferenceSTO(IReferenceSTO r, ReferenceBase rb){
		setIdentifiableEntity(rb, r);
		if (rb.getAuthorTeam() != null){
			r.setAuthorship(rb.getAuthorTeam().getTitleCache());
		}
		for (Media m : rb.getMedia()){
			for (MediaInstance mi : m.getInstances()){
				r.addMediaUri(mi.getUri(), m.getUuid());
			}
		}
		return null;
	}
}
