/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto.assembler;

import java.util.Enumeration;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.remote.dto.IReferenceSTO;
import eu.etaxonomy.cdm.remote.dto.ReferenceSTO;
import eu.etaxonomy.cdm.remote.dto.ReferenceTO;

@Component
public class ReferenceAssembler extends AssemblerBase<ReferenceSTO, ReferenceTO, ReferenceBase> {
	static Logger logger = Logger.getLogger(ReferenceAssembler.class);
	
	@Autowired
	private MediaAssembler mediaAssembler;

	public ReferenceSTO getSTO(ReferenceBase rb, Enumeration<Locale> locales){
		ReferenceSTO r = null;
		if (rb !=null){
			r = new ReferenceSTO();
			fillReferenceSTO(r,rb, locales);
			//TODO: add STO specific mappings here
		}
		return r;
	}	
	
	public ReferenceTO getTO(ReferenceBase rb, Enumeration<Locale> locales){		
		ReferenceTO r = null;
		if (rb !=null){
			r = new ReferenceTO();
			fillReferenceSTO(r,rb, locales);
			r.setCitation(rb.getCitation());
			//TODO: add TO specific mappings here
		}
		return r;
	}
	
	private IReferenceSTO fillReferenceSTO(IReferenceSTO r, ReferenceBase rb, Enumeration<Locale> locales){
		setVersionableEntity(rb, r);
		if (rb.getAuthorTeam() != null){
			r.setAuthorship(rb.getAuthorTeam().getTitleCache());
		}
		for (Media m : rb.getMedia()){
			r.addMedia(mediaAssembler.getSTO(m, locales));
		}
		String fullCitation = rb.getTitleCache();
		//TODO compile fullCitation using a formatter
		r.setFullCitation(fullCitation);
		return r;
	}
}
