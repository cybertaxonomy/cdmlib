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
import java.util.UUID;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Classification;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymType;
import eu.etaxonomy.cdm.model.taxon.Taxon;

public class TaxonGenerator {
	private static String[] genera = {"Carex", "Abies", "Belladonna", "Dracula", "Maria", "Calendula", "Polygala", "Vincia"};
	private static String[] epitheta = {"vulgaris", "magdalena", "officinalis", "alba", "negra", "communa", "alpina", "rotundifolia", "greutheriana", "helventica", "allemania", "franca"};
	private static String[] ranks = {"subsp", "var", "f"};

	public static UUID GENUS_NAME_UUID = UUID.fromString("8d761fc4-b509-42f4-9568-244161934336");
	public static UUID GENUS_UUID = UUID.fromString("bf4298a8-1735-4353-a210-244442e1bd62");
	public static UUID BASIONYM_UUID = UUID.fromString("7911c51d-ccb7-4708-8992-639eae58a0e3");
	public static UUID SPECIES1_UUID = UUID.fromString("f0eb77d9-76e0-47f4-813f-9b5605b78685");
	public static UUID SPECIES1_NAME_UUID = UUID.fromString("efd78713-126f-42e1-9070-a1ff83f12abf");
	public static UUID SYNONYM_NAME_UUID = UUID.fromString("b9cbaa74-dbe0-4930-8050-b7754ce85dc0");
	public static UUID SPECIES2_NAME_UUID = UUID.fromString("0267ab67-483e-4da5-b654-11013b242c22");
	public static UUID SPECIES2_UUID = UUID.fromString("e20eb549-ced6-4e79-9d74-44f0792a4929");
	public static UUID SYNONYM2_NAME_UUID = UUID.fromString("7c17c811-4201-454b-8108-7be7c91c0938");
	public static UUID SPECIES5_NAME_UUID = UUID.fromString("0c6ecaac-804d-49e5-a33f-1b7ee77439e3");
	//public static UUID DESCRIPTION1_UUID = UUID.fromString("f3e061f6-c5df-465c-a253-1e18ab4c7e50");
	//public static UUID DESCRIPTION2_UUID = UUID.fromString("1b009a40-ebff-4f7e-9f7f-75a850ba995d");



	private final Random rnd = new Random();

	public static Taxon getTestTaxon(){
		int descrIndex = 6000;
		Person deCandolle = Person.NewInstance();
		deCandolle.setTitleCache("DC.", true);
		Reference sec = ReferenceFactory.newDatabase();
		sec.setTitleCache("Flora lunaea", true);
		Reference citationRef = ReferenceFactory.newBook();
		citationRef.setTitleCache("Sp. lunarum", true);

        //genus taxon with Name, combinationAuthor,
		BotanicalName botName = BotanicalName.NewInstance(Rank.GENUS());
        botName.setTitleCache("Hieracium L.", true);
        botName.setGenusOrUninomial("Hieracium");
        botName.setCombinationAuthorship(Person.NewInstance());
        botName.getCombinationAuthorship().setNomenclaturalTitle("L.");
        botName.setUuid(GENUS_NAME_UUID);
        Taxon genusTaxon = Taxon.NewInstance(botName, sec);
        genusTaxon.setUuid(GENUS_UUID);

        //a name that is the basionym of genusTaxon's name
        BotanicalName basionym = BotanicalName.NewInstance(Rank.GENUS());
        basionym.setTitleCache("Hieracilla DC.", true);
        basionym.setGenusOrUninomial("Hieracilla");
        basionym.setCombinationAuthorship(deCandolle);
        basionym.setUuid(BASIONYM_UUID);
        botName.addBasionym(basionym, null, null,"216");

        //species taxon that is the child of genus taxon
        BotanicalName botSpecies = BotanicalName.NewInstance(Rank.SPECIES());
        botSpecies.setTitleCache("Hieracium asturianum Pau", true);
        botSpecies.setGenusOrUninomial("Hieracium");
        botSpecies.setSpecificEpithet("asturianum");
        botSpecies.setCombinationAuthorship(Person.NewInstance());
        botSpecies.getCombinationAuthorship().setNomenclaturalTitle("Pau");
        botSpecies.setUuid(SPECIES1_NAME_UUID);
        Taxon childTaxon = Taxon.NewInstance(botSpecies, sec);
        childTaxon.setUuid(SPECIES1_UUID);
        TaxonDescription taxDesc = getTestDescription(descrIndex++);
        //taxDesc.setUuid(DESCRIPTION1_UUID);
        childTaxon.addDescription(taxDesc);
        Classification classification = getTestClassification("TestClassification");
        classification.addParentChild(genusTaxon, childTaxon, citationRef, "456");

        //homotypic synonym of childTaxon1
        BotanicalName botSpecies4= BotanicalName.NewInstance(Rank.SPECIES());
        botSpecies4.setTitleCache("Hieracium gueri DC.", true);
        botSpecies4.setGenusOrUninomial("Hieracium");
        botSpecies4.setSpecificEpithet("gueri");
        botSpecies4.setCombinationAuthorship(deCandolle);
        botSpecies4.setUuid(SYNONYM_NAME_UUID);
        Synonym homoSynonym = Synonym.NewInstance(botSpecies4, sec);
        childTaxon.addSynonym(homoSynonym, SynonymType.HOMOTYPIC_SYNONYM_OF());


        //2nd child species taxon that is the child of genus taxon
        BotanicalName botSpecies2= BotanicalName.NewInstance(Rank.SPECIES());
        botSpecies2.setTitleCache("Hieracium wolffii Zahn", true);
        botSpecies2.setGenusOrUninomial("Hieracium");
        botSpecies2.setSpecificEpithet("wolffii");
        botSpecies2.setCombinationAuthorship(Person.NewInstance());
        botSpecies2.getCombinationAuthorship().setNomenclaturalTitle("Zahn");
        botSpecies2.setUuid(SPECIES2_NAME_UUID);
        Taxon childTaxon2 = Taxon.NewInstance(botSpecies2, sec);
        childTaxon2.setUuid(SPECIES2_UUID);
        classification.addParentChild(genusTaxon, childTaxon2, citationRef, "499");

        //heterotypic synonym of childTaxon2
        BotanicalName botSpecies3= BotanicalName.NewInstance(Rank.SPECIES());
        botSpecies3.setTitleCache("Hieracium lupium DC.", true);
        botSpecies3.setGenusOrUninomial("Hieracium");
        botSpecies3.setSpecificEpithet("lupium");
        botSpecies3.setCombinationAuthorship(deCandolle);
        botSpecies3.setUuid(SYNONYM2_NAME_UUID);
        Synonym heteroSynonym = Synonym.NewInstance(botSpecies3, sec);
        childTaxon2.addSynonym(heteroSynonym, SynonymType.HETEROTYPIC_SYNONYM_OF());

        //missaplied Name for childTaxon2
        BotanicalName missName= BotanicalName.NewInstance(Rank.SPECIES());
        missName.setTitleCache("Hieracium lupium DC.", true);
        missName.setGenusOrUninomial("Hieracium");
        missName.setSpecificEpithet("lupium");
        missName.setCombinationAuthorship(deCandolle);
        missName.setUuid(SPECIES5_NAME_UUID);
        Taxon misappliedName = Taxon.NewInstance(missName, sec);
        childTaxon2.addMisappliedName(misappliedName, citationRef, "125");
        taxDesc = getTestDescription(descrIndex++);
       // taxDesc.setUuid(DESCRIPTION2_UUID);
        genusTaxon.addDescription(taxDesc);


        return genusTaxon;
	}

	public static TaxonDescription getTestDescription(int index){
		TaxonDescription taxonDescription = TaxonDescription.NewInstance();
		Language language = Language.DEFAULT();
		//taxonDescription.setId(index);

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

	public static Classification getTestClassification(String name){
		return Classification.NewInstance(name);
	}
}
