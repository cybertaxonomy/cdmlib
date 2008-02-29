package eu.etaxonomy.cdm.remote.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class TestDataGenerator {
	public static Taxon getTestTaxon(){
        BotanicalName botName = new BotanicalName(Rank.GENUS()); 
        botName.setTitleCache("Hieracium L."); 
        botName.setGenusOrUninomial("Hieracium"); 
        botName.setCombinationAuthorTeam(new Person()); 
        botName.getCombinationAuthorTeam().setTitleCache("L."); 
        Taxon genusTaxon = new Taxon(); 
        genusTaxon.setName(botName); 
        genusTaxon.setSec(null); 
                        
        BotanicalName botSpecies = new BotanicalName(Rank.SPECIES()); 
        botSpecies.setTitleCache("Hieracium asturianum Pau"); 
        botSpecies.setGenusOrUninomial("Hieracium"); 
        botSpecies.setSpecificEpithet("asturianum"); 
        botSpecies.setCombinationAuthorTeam(new Person()); 
        botSpecies.getCombinationAuthorTeam().setTitleCache("Pau"); 
        Taxon childTaxon = new Taxon(); 
        childTaxon.setName(botSpecies); 
        childTaxon.setSec(null); 
        childTaxon.setTaxonomicParent(genusTaxon, null, null); 

        BotanicalName botSpecies2= new BotanicalName(Rank.SPECIES()); 
        botSpecies2.setTitleCache("Hieracium wolffii Zahn"); 
        botSpecies2.setGenusOrUninomial("Hieracium"); 
        botSpecies2.setSpecificEpithet("wolffii"); 
        botSpecies2.setCombinationAuthorTeam(new Person()); 
        botSpecies2.getCombinationAuthorTeam().setTitleCache("Zahn"); 
        Taxon childTaxon2 = new Taxon(); 
        childTaxon2.setName(botSpecies2); 
        childTaxon2.setSec(null); 
        childTaxon2.setTaxonomicParent(genusTaxon, null, null); 
        
        return genusTaxon; 
	}
}
