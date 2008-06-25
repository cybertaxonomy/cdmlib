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
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnitBase;
import eu.etaxonomy.cdm.persistence.dao.common.IDefinedTermDao;
import eu.etaxonomy.cdm.remote.dto.BaseTO;
import eu.etaxonomy.cdm.remote.dto.SpecimenSTO;

/**
 * @author a.kohlbecker
 * @created 05.06.2008
 * @version 1.0
 */
@Component
public class SpecimenAssembler extends AssemblerBase<SpecimenSTO, BaseTO, DerivedUnitBase> {
	private static Logger logger = Logger.getLogger(SpecimenTypeDesignationAssembler.class);
	
	@Autowired
	private MediaAssembler mediaAssembler;


	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getSTO(eu.etaxonomy.cdm.model.common.CdmBase, java.util.Enumeration)
	 */
	@Override
	public SpecimenSTO getSTO(DerivedUnitBase typeSpecimen, Enumeration<Locale> locales) {
		SpecimenSTO sto = new SpecimenSTO();
		sto.setUuid(typeSpecimen.getUuid().toString());
		sto.setSpecimenLabel(typeSpecimen.getTitleCache());
		for(Media media : typeSpecimen.getMedia()){
			sto.addMedia(mediaAssembler.getSTO(media, locales));			
		}
		return sto;
	}

	/** 
	 * Method inherited from {@link AssemblerBase} but not implemented since no SpecimenTO class exists.
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getTO(eu.etaxonomy.cdm.model.common.CdmBase, java.util.Enumeration)
	 */
	@Override
	@Deprecated
	public BaseTO getTO(DerivedUnitBase cdmObj, Enumeration<Locale> locales) {
		throw new RuntimeException("unimplemented method");
	}
}
