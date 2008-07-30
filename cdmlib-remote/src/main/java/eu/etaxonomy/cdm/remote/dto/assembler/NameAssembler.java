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
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.NomenclaturalStatus;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.INomenclaturalReference;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
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
	
	public NameSTO getSTO(TaxonNameBase tnb, Enumeration<Locale> locales){
		NameSTO n = null;
		if (tnb !=null){
			n = new NameSTO();
			setVersionableEntity(tnb, n);
			n.setFullname(tnb.getTitleCache());
			n.setTaggedName(getTaggedName(tnb));
			ReferenceBase nomRef = (ReferenceBase)tnb.getNomenclaturalReference();
			if(nomRef != null) {
				n.setNomenclaturalReference(refAssembler.getSTO(nomRef, true, tnb.getNomenclaturalMicroReference(), locales));				
			}
			for (NomenclaturalStatus status : (Set<NomenclaturalStatus>)tnb.getStatus()) {
				locales = prependLocale(locales, new Locale("la"));
				n.addStatus(localisedTermAssembler.getSTO(status.getType(), locales, TermType.ABBREVLABEL));
			}
		}
		return n;
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

		for (Object token : taxonNameBase.getCacheStrategy().getTaggedName(taxonNameBase)){
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
			else if (token !=null && ReferenceBase.class.isAssignableFrom(token.getClass())){
				ReferenceBase reference = (ReferenceBase) token;
				tag.setText(reference.getTitleCache());
				tag.setType(TagEnum.reference);
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
