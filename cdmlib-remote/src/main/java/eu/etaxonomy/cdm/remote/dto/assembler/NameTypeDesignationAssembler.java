/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.remote.dto.assembler;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.remote.dto.BaseTO;
import eu.etaxonomy.cdm.remote.dto.NameTypeDesignationSTO;

/**
 * @author n.hoffmann
 * @created 14.07.2008
 * @version 1.0
 */
@Component
public class NameTypeDesignationAssembler extends AssemblerBase<NameTypeDesignationSTO, BaseTO, NameTypeDesignation> 
{
	private static Logger logger = Logger
			.getLogger(SpecimenTypeDesignationAssembler.class);
	
	@Autowired
	private SpecimenTypeDesignationAssembler specimenTypeDesignationAssembler;
	@Autowired
	private NameAssembler nameAssembler;
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getSTO(eu.etaxonomy.cdm.model.common.CdmBase, 
	 * 		java.util.Enumeration)
	 */
	@Override
	public NameTypeDesignationSTO getSTO(NameTypeDesignation nameTypeDesignation
											, Enumeration<Locale> locales) 
	{
		NameTypeDesignationSTO sto = new NameTypeDesignationSTO();
		sto.setUuid(nameTypeDesignation.getUuid().toString());
		
		sto.setConservedType(nameTypeDesignation.isConservedType());
		sto.setRejectedType(nameTypeDesignation.isRejectedType());
		sto.setTypeSpeciesName(nameAssembler.getSTO(nameTypeDesignation.getTypeSpecies(), locales));
		sto.setTypifiedName(nameAssembler.getSTO(nameTypeDesignation.getTypifiedName(), locales));
		
		return sto;
	}
	
	/**
	 * @param nameTypeDesignations
	 * @param locales
	 * @return
	 */
	public List<NameTypeDesignationSTO> getSTOs(Set<NameTypeDesignation> nameTypeDesignations
												, Enumeration<Locale> locales)
	{
		List<NameTypeDesignationSTO> stoList = new ArrayList<NameTypeDesignationSTO>(nameTypeDesignations.size());
		for(NameTypeDesignation nameTypeDesignation : nameTypeDesignations){
			stoList.add(getSTO(nameTypeDesignation, locales));
		}
		return stoList;
	}

	/** 
	 * Method inherited from {@link AssemblerBase} but not implemented since no SpecimenTypeDesignationTO class exists.
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getTO(eu.etaxonomy.cdm.model.common.CdmBase, java.util.Enumeration)
	 */
	@Override
	@Deprecated
	public BaseTO getTO(NameTypeDesignation cdmObj, Enumeration<Locale> locales) {
		throw new RuntimeException("unimplemented method");
	}
}
