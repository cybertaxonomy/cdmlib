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
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.remote.dto.DescriptionTO;
import eu.etaxonomy.cdm.remote.dto.NameSTO;
import eu.etaxonomy.cdm.remote.dto.NameTO;
import eu.etaxonomy.cdm.remote.dto.TagEnum;
import eu.etaxonomy.cdm.remote.dto.TaggedText;
import eu.etaxonomy.cdm.remote.dto.assembler.LocalisedTermAssembler.TermType;
import eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy;



@Component
public class NameAssembler extends AssemblerBase<NameSTO, NameTO, TaxonNameBase>{
	
	@Autowired
	private ReferenceAssembler refAssembler;
	@Autowired
	private LocalisedTermAssembler localisedTermAssembler;
	@Autowired
	private DescriptionAssembler descriptionAssembler;
	
	public NameSTO getSTO(TaxonNameBase taxonNameBase, Enumeration<Locale> locales){
		NameSTO name = null; 
		if (taxonNameBase !=null){
			name = new NameSTO();
			setVersionableEntity(taxonNameBase, name);
			name.setFullname(taxonNameBase.getTitleCache());
			name.setTaggedName(getTaggedName(taxonNameBase));
			ReferenceBase nomRef = (ReferenceBase)taxonNameBase.getNomenclaturalReference();
			if(nomRef != null) {
				name.setNomenclaturalReference(refAssembler.getSTO(nomRef, true, taxonNameBase.getNomenclaturalMicroReference(), locales));				
			}
			for (NomenclaturalStatus status : (Set<NomenclaturalStatus>)taxonNameBase.getStatus()) {
				locales = prependLocale(locales, new Locale("la"));
				name.addStatus(localisedTermAssembler.getSTO(status.getType(), locales, TermType.ABBREVLABEL));
			}
			name.setDescriptions(this.getDescriptions(taxonNameBase, locales));		
		}
		return name;
	}
	private Enumeration<Locale> prependLocale(Enumeration<Locale> locales,
			Locale firstLocale) {
		List<Locale> localeList = Collections.list(locales);
		localeList.add(0, firstLocale);
		locales = Collections.enumeration(localeList);
		return locales;
	}	
	public NameTO getTO(TaxonNameBase tnb, Enumeration<Locale> locales){		
		NameTO n = null;
		if (tnb !=null){
			n = new NameTO();
			setVersionableEntity(tnb, n);
			n.setFullname(tnb.getTitleCache());
			n.setTaggedName(getTaggedName(tnb));
			ReferenceBase nomRef = (ReferenceBase)tnb.getNomenclaturalReference();
			if(nomRef != null) {
				n.setNomenclaturalReference(refAssembler.getTO(nomRef, true ,tnb.getNomenclaturalMicroReference(), locales));
			}
			for (NomenclaturalStatus status : (Set<NomenclaturalStatus>)tnb.getStatus()) {
				locales = prependLocale(locales, new Locale("la"));
				n.addStatus(localisedTermAssembler.getSTO(status.getType(), locales));
			}
		}
		return n;
	}
	
	public Set<DescriptionTO> getDescriptions(TaxonNameBase<TaxonNameBase, INameCacheStrategy> taxonNameBase, Enumeration<Locale> locales){
		Set<DescriptionTO> descriptions = new HashSet<DescriptionTO>();

		for(TaxonNameDescription nameDescription : (Set<TaxonNameDescription>)taxonNameBase.getDescriptions()){
			descriptions.add(descriptionAssembler.getTO(nameDescription, locales));
		}
		
		return descriptions;
	}
	
	
	public List<TaggedText> getTaggedName(TaxonNameBase<TaxonNameBase, INameCacheStrategy> taxonNameBase){
		List<TaggedText> tags = new ArrayList<TaggedText>();
		//FIXME rude hack:
		if(!(taxonNameBase instanceof NonViralName)){
			return tags;
		}
		taxonNameBase = (NonViralName)taxonNameBase;
		// --- end of rude hack
		//FIXME infrageneric epithets are not jet handled!
		//   - infraGenericEpithet	"Cicerbita"	
        //   - infraSpecificEpithet	null	

		List<Object> taggedName = taxonNameBase.getCacheStrategy().getTaggedName(taxonNameBase);
		
		for (Object token : taggedName){
			TaggedText tag = new TaggedText();
			if (String.class.isInstance(token)){
				tag.setText((String)token);
				tag.setType(TagEnum.name);
			}
			else if (Rank.class.isInstance(token)){
				Rank r = (Rank)token;
				tag.setText(r.getAbbreviation());
				tag.setType(TagEnum.rank);
			}
			else if (Date.class.isInstance(token)){
				Date d = (Date)token;
				tag.setText(String.valueOf(d.getYear()));
				tag.setType(TagEnum.year);
			}
			else if (Team.class.isInstance(token)){
				Team t = (Team)token;
				tag.setText(String.valueOf(t.getTitleCache()));
				tag.setType(TagEnum.authors);
			}

			if (tag!=null){
				tags.add(tag);
			}
		}
		return tags;
	}
}
