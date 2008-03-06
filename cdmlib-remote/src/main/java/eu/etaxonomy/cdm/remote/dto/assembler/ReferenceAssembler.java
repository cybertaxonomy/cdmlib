package eu.etaxonomy.cdm.remote.dto.assembler;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.common.Media;
import eu.etaxonomy.cdm.model.common.MediaInstance;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.remote.dto.IdentifiedString;
import eu.etaxonomy.cdm.remote.dto.NameSTO;
import eu.etaxonomy.cdm.remote.dto.ReferenceSTO;
import eu.etaxonomy.cdm.remote.dto.ReferenceTO;

@Component
public class ReferenceAssembler extends AssemblerBase {
	public ReferenceSTO getSTO(ReferenceBase rb){
		ReferenceSTO r = null;
		if (rb !=null){
			r = new ReferenceSTO();
			setIdentifiableEntity(rb, r);
			r.setAuthorship(rb.getAuthorTeam().getTitleCache());
			r.setFullCitation(rb.getTitleCache());
			for (Media m : rb.getMedia()){
				for (MediaInstance mi : m.getInstances()){
					r.addMediaUri(mi.getUri(), m.getUuid());
				}
			}
		}
		return r;
	}	
	
	public ReferenceTO getTO(ReferenceBase rb){		
		ReferenceTO r = null;
		if (rb !=null){
			new ReferenceTO();
			setIdentifiableEntity(rb, r);
			r.setAuthorship(rb.getAuthorTeam().getTitleCache());
			for (Media m : rb.getMedia()){
				for (MediaInstance mi : m.getInstances()){
					r.addMediaUri(mi.getUri(), m.getUuid());
				}
			}
		}
		return r;
	}	

}
