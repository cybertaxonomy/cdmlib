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
import eu.etaxonomy.cdm.remote.dto.LocalisedTermSTO;
import eu.etaxonomy.cdm.remote.dto.NameSTO;
import eu.etaxonomy.cdm.remote.dto.NameTO;
import eu.etaxonomy.cdm.remote.dto.TagEnum;
import eu.etaxonomy.cdm.remote.dto.TaggedText;
import eu.etaxonomy.cdm.strategy.cache.name.INameCacheStrategy;



@Component
public class NameAssembler extends AssemblerBase<NameSTO, NameTO, TaxonNameBase>{
	
	@Autowired
	private ReferenceAssembler refAssembler;
	@Autowired
	private LocalisedTermAssembler localisedTermAssembler;
	
	public NameSTO getSTO(TaxonNameBase taxonNameBase, Enumeration<Locale> locales){
		NameSTO name = null;
		if (taxonNameBase !=null){
			name = new NameSTO();
			setVersionableEntity(taxonNameBase, name);
			name.setFullname(taxonNameBase.getTitleCache());
			name.setTaggedName(getTaggedName(taxonNameBase));
			//TODO RUDE HACK
			name.setNomenclaturalReference(refAssembler.getSTO((ReferenceBase)taxonNameBase.getNomenclaturalReference(), locales));
			
			
			for( NomenclaturalStatus status : (Set<NomenclaturalStatus>) taxonNameBase.getStatus()){
				name.addStatus(localisedTermAssembler.getSTO(status.getType(), locales, LocalisedTermAssembler.ABBREVIATED_LABEL));
			}		
		}
		return name;
	}	
	public NameTO getTO(TaxonNameBase taxonNameBase, Enumeration<Locale> locales){		
		NameTO name = null;
		if (taxonNameBase !=null){
			name = new NameTO();
			setVersionableEntity(taxonNameBase, name);
			name.setFullname(taxonNameBase.getTitleCache());
			name.setTaggedName(getTaggedName(taxonNameBase));
			name.setNomenclaturalReference(refAssembler.getTO((ReferenceBase)taxonNameBase.getNomenclaturalReference(), locales));
			for( NomenclaturalStatus status : (Set<NomenclaturalStatus>) taxonNameBase.getStatus()){
				name.addStatus(localisedTermAssembler.getSTO(status.getType(), locales, LocalisedTermAssembler.ABBREVIATED_LABEL));
			}
		}
		return name;
	}
	public List<TaggedText> getTaggedName(TaxonNameBase<TaxonNameBase, INameCacheStrategy> taxonNameBase){
		List<TaggedText> tags = new ArrayList<TaggedText>();
		//FIXME rude hack:
		if(!(taxonNameBase instanceof NonViralName)){
			return tags;
		}
		taxonNameBase = (NonViralName)taxonNameBase;
		// --- end of rude hack
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
			else if (token !=null && INomenclaturalReference.class.isAssignableFrom(token.getClass())){
				INomenclaturalReference reference = (INomenclaturalReference) token;
				tag.setText(reference.getNomenclaturalCitation(taxonNameBase.getNomenclaturalMicroReference()));
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
