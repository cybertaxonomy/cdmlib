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
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.remote.dto.NameSTO;
import eu.etaxonomy.cdm.remote.dto.NameTO;
import eu.etaxonomy.cdm.remote.dto.TagEnum;
import eu.etaxonomy.cdm.remote.dto.TaggedText;
import eu.etaxonomy.cdm.remote.dto.TaxonSTO;
import eu.etaxonomy.cdm.strategy.cache.INameCacheStrategy;



@Component
public class NameAssembler extends AssemblerBase<NameSTO, NameTO, TaxonNameBase>{
	
	@Autowired
	private ReferenceAssembler refAssembler;
	
	public NameSTO getSTO(TaxonNameBase tnb, Enumeration<Locale> locales){
		NameSTO n = null;
		if (tnb !=null){
			n = new NameSTO();
			setVersionableEntity(tnb, n);
			n.setFullname(tnb.getTitleCache());
			n.setTaggedName(getTaggedName(tnb));
			//TODO RUDE HACK
			n.setNomenclaturalReference(refAssembler.getSTO((ReferenceBase)tnb.getNomenclaturalReference(), locales));
		}
		return n;
	}	
	public NameTO getTO(TaxonNameBase tnb, Enumeration<Locale> locales){		
		NameTO n = null;
		if (tnb !=null){
			n = new NameTO();
			setVersionableEntity(tnb, n);
			n.setFullname(tnb.getTitleCache());
			n.setTaggedName(getTaggedName(tnb));
			n.setNomenclaturalReference(refAssembler.getTO((ReferenceBase)tnb.getNomenclaturalReference(), locales));
		}
		return n;
	}
	public List<TaggedText> getTaggedName(TaxonNameBase<TaxonNameBase, INameCacheStrategy> tnb){
		List<TaggedText> tags = new ArrayList<TaggedText>();
		//FIXME rude hack:
		if(!(tnb instanceof NonViralName)){
			return tags;
		}
		tnb = (NonViralName)tnb;
		// --- end of rude hack
		for (Object token : tnb.getCacheStrategy().getTaggedName(tnb)){
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
				ReferenceBase ref = (ReferenceBase)token;
				tag.setText(ref.getTitleCache());
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
