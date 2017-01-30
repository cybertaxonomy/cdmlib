/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.datagenerator;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
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

	public static Taxon getTestTaxon(){
		Person deCandolle = Person.NewInstance();
		deCandolle.setTitleCache("DC.", true);
		Reference sec = ReferenceFactory.newDatabase();
		sec.setTitleCache("Flora lunaea", true);
		Reference citationRef = ReferenceFactory.newBook();
		citationRef.setTitleCache("Sp. lunarum", true);

        //genus taxon with Name, combinationAuthor,
		BotanicalName botName = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
        botName.setTitleCache("Hieracium L.", true);
        botName.setGenusOrUninomial("Hieracium");
        botName.setCombinationAuthorship(Person.NewInstance());
        botName.getCombinationAuthorship().setNomenclaturalTitle("L.");
        Taxon genusTaxon = Taxon.NewInstance(botName, sec);

        //a name that is the basionym of genusTaxon's name
        BotanicalName basionym = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
        basionym.setTitleCache("Hieracilla DC.", true);
        basionym.setGenusOrUninomial("Hieracilla");
        basionym.setCombinationAuthorship(deCandolle);
        botName.addBasionym(basionym, null, null, "216");

        //species taxon that is the child of genus taxon
        BotanicalName botSpecies = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        botSpecies.setTitleCache("Hieracium asturianum Pau", true);
        botSpecies.setGenusOrUninomial("Hieracium");
        botSpecies.setSpecificEpithet("asturianum");
        botSpecies.setCombinationAuthorship(Person.NewInstance());
        botSpecies.getCombinationAuthorship().setNomenclaturalTitle("Pau");
        Taxon childTaxon = Taxon.NewInstance(botSpecies, sec);

        //homotypic synonym of childTaxon1
        BotanicalName botSpecies4= TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        botSpecies4.setTitleCache("Hieracium gueri DC.", true);
        botSpecies4.setGenusOrUninomial("Hieracium");
        botSpecies4.setSpecificEpithet("gueri");
        botSpecies4.setCombinationAuthorship(deCandolle);
        Synonym homoSynonym = Synonym.NewInstance(botSpecies4, sec);
        childTaxon.addSynonym(homoSynonym, SynonymType.HOMOTYPIC_SYNONYM_OF());


        //2nd child species taxon that is the child of genus taxon
        BotanicalName botSpecies2= TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        botSpecies2.setTitleCache("Hieracium wolffii Zahn", true);
        botSpecies2.setGenusOrUninomial("Hieracium");
        botSpecies2.setSpecificEpithet("wolffii");
        botSpecies2.setCombinationAuthorship(Person.NewInstance());
        botSpecies2.getCombinationAuthorship().setNomenclaturalTitle("Zahn");
        Taxon childTaxon2 = Taxon.NewInstance(botSpecies2, sec);

        //heterotypic synonym of childTaxon2
        BotanicalName botSpecies3= TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        botSpecies3.setTitleCache("Hieracium lupium DC.", true);
        botSpecies3.setGenusOrUninomial("Hieracium");
        botSpecies3.setSpecificEpithet("lupium");
        botSpecies3.setCombinationAuthorship(deCandolle);
        Synonym heteroSynonym = Synonym.NewInstance(botSpecies3, sec);
        childTaxon2.addSynonym(heteroSynonym, SynonymType.HETEROTYPIC_SYNONYM_OF());

        //missaplied Name for childTaxon2
        BotanicalName missName= TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
        missName.setTitleCache("Hieracium lupium DC.", true);
        missName.setGenusOrUninomial("Hieracium");
        missName.setSpecificEpithet("lupium");
        missName.setCombinationAuthorship(deCandolle);
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
