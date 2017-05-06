/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.test.function;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Set;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.model.agent.AgentBase;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.Annotation;
import eu.etaxonomy.cdm.model.common.IdentifiableSource;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.location.Country;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.location.NamedAreaType;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.media.MediaRepresentation;
import eu.etaxonomy.cdm.model.media.MediaRepresentationPart;
import eu.etaxonomy.cdm.model.name.INonViralName;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivationEvent;
import eu.etaxonomy.cdm.model.occurrence.DerivationEventType;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.DeterminationEvent;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.GatheringEvent;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.strategy.parser.NonViralNameParserImpl;

/**
 * @author AM
 * @created 09.05.2008
 * @version 1.0
 */
public class TestSpecimen {
	private static final Logger logger = Logger.getLogger(TestSpecimen.class);

	public void testSpecimen(){
		logger.info("Create test taxon ...");
		Reference sec = ReferenceFactory.newDatabase();
		String fullNameString = "Acanthostyles saucechicoensis (Hieron.) R.M. King & H. Rob.";
		INonViralName name = NonViralNameParserImpl.NewInstance().parseFullName(fullNameString);
		TaxonNameBase botanicalName = TaxonNameBase.castAndDeproxy(name);
		Taxon taxon = Taxon.NewInstance(botanicalName, sec);

		Collection collection = Collection.NewInstance();
		String collectionCode = "ABCDE";
		collection.setCode(collectionCode);
		Institution institution = Institution.NewInstance();
		String institutionCode = "Inst444";
		institution.setCode(institutionCode);
		collection.setInstitute(institution);


		logger.info("Create determination event");
		DeterminationEvent determinationEvent = DeterminationEvent.NewInstance();
		determinationEvent.setTaxon(taxon);
		Person actor = Person.NewTitledInstance("J. M�ller (JE)");
		determinationEvent.setActor(actor);

		logger.info("Create gathering event");
		GatheringEvent gatheringEvent = GatheringEvent.NewInstance();
		AgentBase<?> gatherer = Person.NewTitledInstance("Lorentz,P.G. s.n.");
		gatheringEvent.setActor(gatherer);
		Calendar gatheringDate = Calendar.getInstance();
		int year = 1922;
		int month = 10;
		int date = 05;
		gatheringDate.set(year, month, date);

		gatheringEvent.setGatheringDate(gatheringDate);

		logger.info("Create locality");
		NamedArea namedArea = NamedArea.NewInstance();
		Country country = Country.ARGENTINAARGENTINEREPUBLIC();
		namedArea.addCountry(country);
		namedArea.setType(NamedAreaType.ADMINISTRATION_AREA());
		// XX

		gatheringEvent.addCollectingArea(namedArea);
		String localityString = "Sierras Pampeanas, Sauce Chico";
		LanguageString locality = LanguageString.NewInstance(localityString, Language.DEFAULT());
		gatheringEvent.setLocality(locality);


		logger.info("Create new specimen ...");
		DerivedUnit specimen = DerivedUnit.NewPreservedSpecimenInstance();
		specimen.setCatalogNumber("JE 00004506");
		specimen.setStoredUnder(botanicalName);   //??
		specimen.setCollection(collection);
		String annotation = "originally designated as type specimen 2000, but corrected 2005-11";
		specimen.addAnnotation(Annotation.NewDefaultLanguageInstance(annotation));


		Media media = Media.NewInstance();
		URI uri = null;
		try {
			uri = new URI("http://131.130.131.9/database/img/imgBrowser.php?ID=50599");
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		String mimeType = null;
		Integer size = null;
		MediaRepresentation mediaRepresentation = MediaRepresentation.NewInstance(mimeType, "jpg");
		media.addRepresentation(mediaRepresentation);

		MediaRepresentationPart mediaRepresentationPart = MediaRepresentationPart.NewInstance(uri, size);
		mediaRepresentation.addRepresentationPart(mediaRepresentationPart);
//		specimen.addMedia(media);    #3597

		//Original ID
		String id = "22";
		IdentifiableSource source = IdentifiableSource.NewDataImportInstance(id);
		specimen.addSource(source);

		FieldUnit fieldUnit = FieldUnit.NewInstance();
		DerivationEvent derivationEvent = DerivationEvent.NewInstance(DerivationEventType.GATHERING_IN_SITU());
		derivationEvent.addDerivative(specimen);
		fieldUnit.addDerivationEvent(derivationEvent);
		fieldUnit.setGatheringEvent(gatheringEvent);


		//type information
		//typified by


		logger.warn("Specimen: " + specimen);
		DerivationEvent dEvent = specimen.getDerivedFrom();
		logger.warn("DerivationEvent: " + dEvent);
		Set<SpecimenOrObservationBase> originals = dEvent.getOriginals();
		logger.warn("Originals: " + originals);
		for (SpecimenOrObservationBase original : originals){
			if (original instanceof FieldUnit){
				FieldUnit fieldUnit2 = (FieldUnit)original;
				logger.warn("FieldUnit: " + fieldUnit2);
				GatheringEvent gatheringEvent2= fieldUnit2.getGatheringEvent();
				logger.warn("GatheringEvent: " + gatheringEvent2);
				AgentBase<?> gatherer2 = gatheringEvent2.getCollector();
				logger.warn("Gatherer: "+  gatherer2);
			}
		}
	}

	private void test(){
		System.out.println("Start ...");
		this.testSpecimen();
		System.out.println("\nEnd ...");
	}

	/**
	 * @param args
	 */
	public static void  main(String[] args) {
		TestSpecimen sc = new TestSpecimen();
    	sc.test();
	}
}
