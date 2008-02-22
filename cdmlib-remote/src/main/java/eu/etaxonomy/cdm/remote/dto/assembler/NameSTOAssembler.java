package eu.etaxonomy.cdm.remote.dto.assembler;

import java.util.Random;
import java.util.UUID;

import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.remote.dto.NameSTO;
import eu.etaxonomy.cdm.remote.dto.TagEnum;
import eu.etaxonomy.cdm.remote.dto.TaggedText;



@Component
public class NameSTOAssembler extends AssemblerBase{
	
	private String[] genera = {"Carex", "Abies", "Belladonna", "Dracula", "Maria", "Calendula", "Polygala", "Vincia"};
	private String[] epitheta = {"vulgaris", "magdalena", "officinalis", "alba", "negra", "communa", "alpina", "rotundifolia", "greutheriana", "helventica", "allemania", "franca"};
	private String[] ranks = {"subsp", "var", "f"}; 
	
	private Random rnd = new Random();

	private String getRandomToken(String[] en){
		return en[rnd.nextInt(en.length)];
	}
	
	public NameSTO getRandom(){
		NameSTO n = new NameSTO();
		n.setUuid(getRandomUUID());
		n.setNomenclaturalReference(null);
		String tmp = getRandomToken(genera);
		n.setFullname(tmp);
		n.addNameToken(new TaggedText(TagEnum.name, tmp));
		tmp = getRandomToken(epitheta);
		n.setFullname(n.getFullname() + " " + tmp);
		n.addNameToken(new TaggedText(TagEnum.name, tmp));
		if (rnd.nextInt(5)<4){
			tmp = getRandomToken(ranks);
			n.setFullname(n.getFullname() + " " + tmp+".");
			n.addNameToken(new TaggedText(TagEnum.name, tmp));
			tmp = getRandomToken(epitheta);
			n.setFullname(n.getFullname() + " " + tmp);
			n.addNameToken(new TaggedText(TagEnum.name, tmp));
		}
		return n;
	}
}
