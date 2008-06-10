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

import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.remote.dto.BaseTO;
import eu.etaxonomy.cdm.remote.dto.SpecimenSTO;

/**
 * @author a.kohlbecker
 * @created 05.06.2008
 * @version 1.0
 */
@Component
public class SpecimenAssembler extends AssemblerBase<SpecimenSTO, BaseTO, SpecimenTypeDesignation> {
	private static Logger logger = Logger.getLogger(SpecimenTypeDesignationAssembler.class);
	
//	@Autowired
//	private MediaAssembler mediaAssembler;

	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getSTO(eu.etaxonomy.cdm.model.common.CdmBase, java.util.Enumeration)
	 */
	@Override
	public SpecimenSTO getSTO(SpecimenTypeDesignation specimenTypeDesignation, Enumeration<Locale> locales) {
		SpecimenSTO specimenSTO = new SpecimenSTO();
		specimenSTO.setUuid(specimenTypeDesignation.getUuid().toString());
		
		//FIXME: 
//		for(Media media : specimenTypeDesignation.getTypeSpecimen().getMedia()){
//			String uuid = media.getUuid().toString();
//			for(MediaInstance instance : media.getRepresentations()){
//				specimenSTO.getMediaUri().add(new IdentifiedString(uuid, instance.getUri().toString()));				
//			}
//		}
		return specimenSTO;
	}

	/** 
	 * Method inherited from {@link AssemblerBase} but not implemented since no SpecimenTO class exists.
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getTO(eu.etaxonomy.cdm.model.common.CdmBase, java.util.Enumeration)
	 */
	@Override
	@Deprecated
	public BaseTO getTO(SpecimenTypeDesignation cdmObj, Enumeration<Locale> locales) {
		throw new RuntimeException("unimplemented method");
	}
}
