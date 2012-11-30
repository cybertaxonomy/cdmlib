/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.datagenerator;

import java.util.Random;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
//import eu.etaxonomy.cdm.model.reference.Book;
//import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;

/**
 * This class offers functionality to create test taxa and descriptions.
 * 
 * @author m.doering
 *
 */
public class TaxonGenerator {
	private static String[] genera = {"Carex", "Abies", "Belladonna", "Dracula", "Maria", "Calendula", "Polygala", "Vincia"};
	private static String[] epitheta = {"vulgaris", "magdalena", "officinalis", "alba", "negra", "communa", "alpina", "rotundifolia", "greutheriana", "helventica", "allemania", "franca"};
	private static String[] ranks = {"subsp", "var", "f"}; 
	
	private Random rnd = new Random();

	public static Taxon getTestTaxon(){
		ReferenceFactory refFactory = ReferenceFactory.newInstance();
		Person deCandolle = Person.NewInstance();
		deCandolle.setTitleCache("DC.", true);
		Reference sec = refFactory.newDatabase();
		sec.setTitleCache("Flora lunaea", true);
		Reference citationRef = refFactory.newBook();
		citationRef.setTitleCache("Sp. lunarum", true);
		
        //genus taxon with Name, combinationAuthor, 
		BotanicalName botName = BotanicalName.NewInstance(Rank.GENUS()); 
        botName.setTitleCache("Hieracium L.", true); 
        botName.setGenusOrUninomial("Hieracium"); 
        botName.setCombinationAuthorTeam(Person.NewInstance()); 
        botName.getCombinationAuthorTeam().setNomenclaturalTitle("L."); 
        Taxon genusTaxon = Taxon.NewInstance(botName, sec); 
                        
        //a name that is the basionym of genusTaxon's name
        BotanicalName basionym = BotanicalName.NewInstance(Rank.GENUS()); 
        basionym.setTitleCache("Hieracilla DC.", true); 
        basionym.setGenusOrUninomial("Hieracilla"); 
        basionym.setCombinationAuthorTeam(deCandolle); 
        botName.addBasionym(basionym, null, null, "216");
        
        //species taxon that is the child of genus taxon
        BotanicalName botSpecies = BotanicalName.NewInstance(Rank.SPECIES()); 
        botSpecies.setTitleCache("Hieracium asturianum Pau", true); 
        botSpecies.setGenusOrUninomial("Hieracium"); 
        botSpecies.setSpecificEpithet("asturianum"); 
        botSpecies.setCombinationAuthorTeam(Person.NewInstance()); 
        botSpecies.getCombinationAuthorTeam().setNomenclaturalTitle("Pau"); 
        Taxon childTaxon = Taxon.NewInstance(botSpecies, sec); 
        childTaxon.setTaxonomicParent(genusTaxon, citationRef, "456"); 
 
        //homotypic synonym of childTaxon1
        BotanicalName botSpecies4= BotanicalName.NewInstance(Rank.SPECIES()); 
        botSpecies4.setTitleCache("Hieracium gueri DC.", true); 
        botSpecies4.setGenusOrUninomial("Hieracium"); 
        botSpecies4.setSpecificEpithet("gueri"); 
        botSpecies4.setCombinationAuthorTeam(deCandolle); 
        Synonym homoSynonym = Synonym.NewInstance(botSpecies4, sec); 
        childTaxon.addSynonym(homoSynonym, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF());


        //2nd child species taxon that is the child of genus taxon
        BotanicalName botSpecies2= BotanicalName.NewInstance(Rank.SPECIES()); 
        botSpecies2.setTitleCache("Hieracium wolffii Zahn", true); 
        botSpecies2.setGenusOrUninomial("Hieracium"); 
        botSpecies2.setSpecificEpithet("wolffii"); 
        botSpecies2.setCombinationAuthorTeam(Person.NewInstance()); 
        botSpecies2.getCombinationAuthorTeam().setNomenclaturalTitle("Zahn"); 
        Taxon childTaxon2 = Taxon.NewInstance(botSpecies2, sec); 
        childTaxon2.setTaxonomicParent(genusTaxon, citationRef, "499"); 
        
        //heterotypic synonym of childTaxon2
        BotanicalName botSpecies3= BotanicalName.NewInstance(Rank.SPECIES()); 
        botSpecies3.setTitleCache("Hieracium lupium DC.", true); 
        botSpecies3.setGenusOrUninomial("Hieracium"); 
        botSpecies3.setSpecificEpithet("lupium"); 
        botSpecies3.setCombinationAuthorTeam(deCandolle); 
        Synonym heteroSynonym = Synonym.NewInstance(botSpecies3, sec); 
        childTaxon2.addSynonym(heteroSynonym, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());

        //missaplied Name for childTaxon2
        BotanicalName missName= BotanicalName.NewInstance(Rank.SPECIES()); 
        missName.setTitleCache("Hieracium lupium DC.", true); 
        missName.setGenusOrUninomial("Hieracium"); 
        missName.setSpecificEpithet("lupium"); 
        missName.setCombinationAuthorTeam(deCandolle); 
        Taxon misappliedName = Taxon.NewInstance(missName, sec); 
        childTaxon2.addMisappliedName(misappliedName, citationRef, "125");
       
        genusTaxon.addDescription(getTestDescription());
        
        return genusTaxon; 
	}
	
	public static TaxonDescription getTestDescription(){
		TaxonDescription taxonDescription = TaxonDescription.NewInstance();
		Language language = Language.DEFAULT();
		
		//textData
		TextData textData = TextData.NewInstance();
		String descriptionText = "this is a desciption for a taxon";
		LanguageString languageString = LanguageString.NewInstance(descriptionText, language);
		textData.putText(languageString);
		taxonDescription.addElement(textData);
		
		//commonName
		String commonNameString = "Schönveilchen";
		CommonTaxonName commonName = CommonTaxonName.NewInstance(commonNameString, language);
		taxonDescription.addElement(commonName);
		
		return taxonDescription;
	}
}
