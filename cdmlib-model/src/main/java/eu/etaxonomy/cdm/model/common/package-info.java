/* Package Annotations*/

@javax.xml.bind.annotation.XmlSchema(namespace = "http://etaxonomy.eu/cdm/model/common/1.0", elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED)
@GenericGenerator(
		name="system-increment", 
		strategy = "increment"
)
@org.hibernate.annotations.TypeDefs( { 
	@org.hibernate.annotations.TypeDef(name="persistentDuration", typeClass=org.joda.time.contrib.hibernate.PersistentDuration.class),
	@org.hibernate.annotations.TypeDef(name="dateTimeUserType", typeClass=org.joda.time.contrib.hibernate.PersistentDateTime.class),
	@org.hibernate.annotations.TypeDef(name="partialUserType", typeClass=eu.etaxonomy.cdm.model.common.PartialUserType.class),
	@org.hibernate.annotations.TypeDef(name="uuidUserType", typeClass=eu.etaxonomy.cdm.model.common.UUIDUserType.class)
})
@org.hibernate.annotations.AnyMetaDef(name = "CdmBase" ,
		                              metaType="string", 
		                              idType="integer",
		                              metaValues={
		@MetaValue(value = "eu.etaxonomy.cdm.model.agent.Institution", targetEntity = Institution.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.agent.Person", targetEntity = Person.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.agent.Team", targetEntity = Team.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.common.Annotation", targetEntity = Annotation.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.description.MediaKey", targetEntity = MediaKey.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.description.TaxonDescription", targetEntity = TaxonDescription.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.description.SpecimenDescription", targetEntity = SpecimenDescription.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.description.TaxonNameDescription", targetEntity = TaxonNameDescription.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.media.Media", targetEntity = Media.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.molecular.Sequence", targetEntity = Sequence.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.name.BacterialName", targetEntity = BacterialName.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.name.BotanicalName", targetEntity = BotanicalName.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.name.CultivarPlantName", targetEntity = CultivarPlantName.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.name.ViralName", targetEntity = ViralName.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.name.ZoologicalName", targetEntity = ZoologicalName.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.occurrence.Collection", targetEntity = Collection.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.occurrence.DerivedUnit", targetEntity = DerivedUnit.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.occurrence.FieldObservation", targetEntity = FieldObservation.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.occurrence.Fossil", targetEntity = Fossil.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.occurrence.LivingBeing", targetEntity = LivingBeing.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.occurrence.Observation", targetEntity = Observation.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.occurrence.Specimen", targetEntity = Specimen.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.reference.Journal", targetEntity = Journal.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.reference.Article", targetEntity = Article.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.reference.Book", targetEntity = Book.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.reference.Generic", targetEntity = Generic.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.reference.Database", targetEntity = Database.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.reference.BookSection", targetEntity = BookSection.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.reference.Reference", targetEntity = ReferenceBase.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.reference.PrintSeries", targetEntity = PrintSeries.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.reference.Thesis", targetEntity = Thesis.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.reference.WebPage", targetEntity = WebPage.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.taxon.Synonym", targetEntity = Synonym.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.taxon.Taxon", targetEntity = Taxon.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.description.TextData", targetEntity = TextData.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.agent.TeamOrPersonBase", targetEntity = TeamOrPersonBase.class)
})
package eu.etaxonomy.cdm.model.common;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.MetaValue;

import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.description.MediaKey;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.name.BacterialName;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.CultivarPlantName;
import eu.etaxonomy.cdm.model.name.ViralName;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.occurrence.Collection;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldObservation;
import eu.etaxonomy.cdm.model.occurrence.Fossil;
import eu.etaxonomy.cdm.model.occurrence.LivingBeing;
import eu.etaxonomy.cdm.model.occurrence.Observation;
import eu.etaxonomy.cdm.model.occurrence.Specimen;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.BookSection;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.Generic;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.PrintSeries;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.Thesis;
import eu.etaxonomy.cdm.model.reference.WebPage;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
	