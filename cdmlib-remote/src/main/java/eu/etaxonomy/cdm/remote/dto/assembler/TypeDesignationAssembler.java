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
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.name.ITypeDesignation;
import eu.etaxonomy.cdm.model.name.NameTypeDesignation;
import eu.etaxonomy.cdm.model.name.SpecimenTypeDesignation;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationBase;
import eu.etaxonomy.cdm.model.name.TypeDesignationStatus;
import eu.etaxonomy.cdm.remote.dto.BaseTO;
import eu.etaxonomy.cdm.remote.dto.IdentifiedString;
import eu.etaxonomy.cdm.remote.dto.NameTypeDesignationSTO;
import eu.etaxonomy.cdm.remote.dto.SpecimenTypeDesignationSTO;
import eu.etaxonomy.cdm.remote.dto.TypeDesignationSTO;

/**
 * @author a.kohlbecker
 * @created 05.06.2008
 * @version 1.0
 */
@Component
public class TypeDesignationAssembler extends AssemblerBase<TypeDesignationSTO, BaseTO, TypeDesignationBase> {
	private static Logger logger = Logger
			.getLogger(TypeDesignationAssembler.class);
	
	@Autowired
	private SpecimenAssembler specimenAssembler;
	@Autowired
	private NameAssembler nameAssembler;
	@Autowired
	private ReferenceAssembler referenceAssembler;
	
	
	public SpecimenTypeDesignationSTO getSTO(SpecimenTypeDesignation typeDesignation, Enumeration<Locale> locales) {
		SpecimenTypeDesignationSTO sto = new SpecimenTypeDesignationSTO();
		sto.setUuid(typeDesignation.getUuid().toString());
		TypeDesignationStatus status = typeDesignation.getTypeStatus();
		
		if (status == null){
			sto.setStatus(new IdentifiedString(
					null,
					null)
			);
		}else{
			sto.setStatus(new IdentifiedString(
						status.getLabel(),
						status.getUuid().toString())
				);
			// append reference only if there is a status
			sto.setReference(referenceAssembler.getSTO(typeDesignation.getCitation(), false, typeDesignation.getCitationMicroReference(), locales));
		}
		sto.setTypeSpecimen(specimenAssembler.getSTO(typeDesignation.getTypeSpecimen(), locales));
		return sto;
	}
	
	/**
	 * 
	 * @param typeDesignation
	 * @param locales
	 * @return
	 */
	public NameTypeDesignationSTO getSTO(NameTypeDesignation typeDesignation
			, Enumeration<Locale> locales) 
	{
		NameTypeDesignationSTO sto = new NameTypeDesignationSTO();
		sto.setUuid(typeDesignation.getUuid().toString());
		
		sto.setConservedType(typeDesignation.isConservedType());
		sto.setRejectedType(typeDesignation.isRejectedType());
		sto.setTypeSpeciesName(nameAssembler.getSTO(typeDesignation.getTypeSpecies(), locales));
		
		for (TaxonNameBase typifiedName : typeDesignation.getTypifiedNames()){
			sto.addTypifiedName(nameAssembler.getSTO(typifiedName, locales));
		}
		
		return sto;
	}
	
	/**
	 * @param specimenTypeDesignations
	 * @param locales
	 * @return
	 */
	public List<TypeDesignationSTO> getSTOs(Set<TypeDesignationBase> typeDesignations, Enumeration<Locale> locales){
		List<TypeDesignationSTO> stoList = new ArrayList<TypeDesignationSTO>(typeDesignations.size());
		
		for(TypeDesignationBase typeDesignation : typeDesignations){ 
			stoList.add(getSTO(typeDesignation, locales));
		}
		return stoList;
	}
	
	/** 
	 * Method inherited from {@link AssemblerBase} but not implemented since no SpecimenTypeDesignationTO class exists.
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getTO(eu.etaxonomy.cdm.model.common.CdmBase, java.util.Enumeration)
	 */
	@Override
	BaseTO getTO(TypeDesignationBase cdmObj, Enumeration<Locale> locales) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.dto.assembler.AssemblerBase#getSTO(eu.etaxonomy.cdm.model.common.CdmBase, java.util.Enumeration)
	 */
	@Override
	TypeDesignationSTO getSTO(TypeDesignationBase typeDesignation,
			Enumeration<Locale> locales) {
		if (typeDesignation instanceof SpecimenTypeDesignation){
			return getSTO((SpecimenTypeDesignation) typeDesignation, locales);
		}
		if (typeDesignation instanceof NameTypeDesignation){
			return getSTO((NameTypeDesignation) typeDesignation, locales);
		}
		
		return null;
	}
}
