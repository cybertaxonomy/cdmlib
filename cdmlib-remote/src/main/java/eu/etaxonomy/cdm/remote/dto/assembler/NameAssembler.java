package eu.etaxonomy.cdm.remote.dto.assembler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.remote.dto.NameSTO;
import eu.etaxonomy.cdm.remote.dto.NameTO;
import eu.etaxonomy.cdm.remote.dto.TagEnum;
import eu.etaxonomy.cdm.remote.dto.TaggedText;
import eu.etaxonomy.cdm.remote.dto.TaxonSTO;



@Component
public class NameAssembler extends AssemblerBase<NameSTO, NameTO, TaxonNameBase>{
	@Autowired
	private ReferenceAssembler refAssembler;
	
	public NameSTO getSTO(TaxonNameBase tnb){
		NameSTO n = null;
		if (tnb !=null){
			n = new NameSTO();
			setVersionableEntity(tnb, n);
			n.setFullname(tnb.getTitleCache());
			n.setTaggedName(getTaggedName(tnb));
			n.setNomenclaturalReference(refAssembler.getSTO(tnb.getNomenclaturalReference()));
		}
		return n;
	}	
	public NameTO getTO(TaxonNameBase tnb){		
		NameTO n = null;
		if (tnb !=null){
			n = new NameTO();
			setVersionableEntity(tnb, n);
			n.setFullname(tnb.getTitleCache());
			n.setTaggedName(getTaggedName(tnb));
			n.setNomenclaturalReference(refAssembler.getTO(tnb.getNomenclaturalReference()));
		}
		return n;
	}
	public List<TaggedText> getTaggedName(TaxonNameBase<TaxonNameBase> tnb){
		List<TaggedText> tags = new ArrayList<TaggedText>();
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
