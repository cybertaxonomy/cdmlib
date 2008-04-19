package eu.etaxonomy.cdm.datagenerator;

import java.util.Random;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class TaxonGenerator {
	private static String[] genera = {"Carex", "Abies", "Belladonna", "Dracula", "Maria", "Calendula", "Polygala", "Vincia"};
	private static String[] epitheta = {"vulgaris", "magdalena", "officinalis", "alba", "negra", "communa", "alpina", "rotundifolia", "greutheriana", "helventica", "allemania", "franca"};
	private static String[] ranks = {"subsp", "var", "f"}; 
	
	private Random rnd = new Random();

	public static Taxon getTestTaxon(){
		Person deCandolle = Person.NewInstance();
		deCandolle.setTitleCache("DC.");
		
        BotanicalName botName = new BotanicalName(Rank.GENUS()); 
        botName.setTitleCache("Hieracium L."); 
        botName.setGenusOrUninomial("Hieracium"); 
        botName.setCombinationAuthorTeam(Person.NewInstance()); 
        botName.getCombinationAuthorTeam().setNomenclaturalTitle("L."); 
        Taxon genusTaxon = new Taxon(); 
        genusTaxon.setName(botName); 
        genusTaxon.setSec(null); 
                        
        BotanicalName basionym = new BotanicalName(Rank.GENUS()); 
        basionym.setTitleCache("Hieracilla DC."); 
        basionym.setGenusOrUninomial("Hieracilla"); 
        basionym.setCombinationAuthorTeam(deCandolle); 
        botName.setBasionym(basionym, "¤216");
        
        BotanicalName botSpecies = new BotanicalName(Rank.SPECIES()); 
        botSpecies.setTitleCache("Hieracium asturianum Pau"); 
        botSpecies.setGenusOrUninomial("Hieracium"); 
        botSpecies.setSpecificEpithet("asturianum"); 
        botSpecies.setCombinationAuthorTeam(Person.NewInstance()); 
        botSpecies.getCombinationAuthorTeam().setNomenclaturalTitle("Pau"); 
        Taxon childTaxon = new Taxon(); 
        childTaxon.setName(botSpecies); 
        childTaxon.setSec(null); 
        childTaxon.setTaxonomicParent(genusTaxon, null, null); 

        BotanicalName botSpecies2= new BotanicalName(Rank.SPECIES()); 
        botSpecies2.setTitleCache("Hieracium wolffii Zahn"); 
        botSpecies2.setGenusOrUninomial("Hieracium"); 
        botSpecies2.setSpecificEpithet("wolffii"); 
        botSpecies2.setCombinationAuthorTeam(Person.NewInstance()); 
        botSpecies2.getCombinationAuthorTeam().setNomenclaturalTitle("Zahn"); 
        Taxon childTaxon2 = new Taxon(); 
        childTaxon2.setName(botSpecies2); 
        childTaxon2.setSec(null); 
        childTaxon2.setTaxonomicParent(genusTaxon, null, null); 
        
        BotanicalName botSpecies3= new BotanicalName(Rank.SPECIES()); 
        botSpecies3.setTitleCache("Hieracium lupium DC."); 
        botSpecies3.setGenusOrUninomial("Hieracium"); 
        botSpecies3.setSpecificEpithet("lupium"); 
        botSpecies3.setCombinationAuthorTeam(deCandolle); 
        Synonym synonym = new Synonym(); 
        synonym.setName(botSpecies3); 
        synonym.setSec(null);
        childTaxon2.addSynonym(synonym, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());

        return genusTaxon; 
	}
}
