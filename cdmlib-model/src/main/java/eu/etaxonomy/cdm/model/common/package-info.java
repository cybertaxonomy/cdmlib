/* Package Annotations*/

@javax.xml.bind.annotation.XmlSchema(namespace = "http://etaxonomy.eu/cdm/model/common/1.0", elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED)
@com.sun.xml.bind.XmlAccessorFactory(eu.etaxonomy.cdm.jaxb.CdmAccessorFactoryImpl.class)
@org.hibernate.annotations.GenericGenerators(
	{
		/* Steve Ebersole of Hibernate highly recommends the use of the two new generators
		 * http://relation.to/2082.lace
		 * Also see: http://docs.jboss.org/hibernate/core/3.3/reference/en/html/mapping.html#mapping-declaration-id-enhanced
		 */
		/* new table generator
		 * always stores sequences in a table. May be configured to return a sequence on a per table basis
		 * RECOMMENDED WHEN RUNNING A CDM DATASOURCE IN A MULTI CLIENT ENVIRONMENT 
		 */
		@GenericGenerator(
				name="enhanced-table", 
				strategy = "org.hibernate.id.enhanced.TableGenerator",
				parameters = {
				    @Parameter(name="optimizer", value = "pooled"),
				    /* initial_value = increment_size as proposed to fix an issue with pooled optimizer
				     * http://opensource.atlassian.com/projects/hibernate/browse/HHH-3608?focusedCommentId=37112&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#action_37112
				     */
				    @Parameter(name="initial_value", value= "10"),
				    @Parameter(name="increment_size", value = "10"),
				    /* we want to have a sequence per table */
				    @Parameter(name="prefer_entity_table_as_segment_value", value="true")
				}
		),
		/* new sequence generator
		 * Using sequence when the dialect supports it, otherwise it will emulate a sequence using a table in the db
		 * This method will result in database wide unique id's */
		@GenericGenerator(
				name="enhanced-sequence", 
				strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
				parameters = {
			        @Parameter(name="optimizer", value = "pooled"),
			        /* initial_value = increment_size as proposed to fix an issue with pooled optimizer
				     * http://opensource.atlassian.com/projects/hibernate/browse/HHH-3608?focusedCommentId=37112&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#action_37112
				     */
				    @Parameter(name="initial_value", value= "10"),
				    @Parameter(name="increment_size", value = "10")
			    }
		),
		/* A couple of old style generators */
		/* generates identifiers of type long, short or int that are unique only when no other process 
		 * is inserting data into the same table. 
		 * DO NOT USE IN A CLUSTER OR MULTIPLE CLIENT ENVIRONMENT */
		@GenericGenerator(		
				name="system-increment", 
				strategy = "increment"
		),
		/* supports identity columns in DB2, MySQL, MS SQL Server, Sybase and HypersonicSQL. 
		 * The returned identifier is of type long, short or int. */
		@GenericGenerator(
				name="system-identity", 
				strategy = "identity"
					
		),
		/* uses a hi/lo algorithm to efficiently generate identifiers of type long, short or int, 
		 * given a table and column (by default hibernate_unique_key and next_hi respectively) as 
		 * a source of hi values. The hi/lo algorithm generates identifiers that are unique only 
		 * for a particular database. */
		@GenericGenerator(
				name="system-hilo", 
				strategy = "hilo"
		),
		/* uses a sequence in DB2, PostgreSQL, Oracle, SAP DB, McKoi or a generator in Interbase. 
		 * The returned identifier is of type long, short or int */
		@GenericGenerator(
				name="system-sequence", 
				strategy = "sequence"
		),
		/* selects identity, sequence or hilo depending upon the capabilities of the underlying database. */
		@GenericGenerator(
				name="system-native", 
				strategy = "native"
		)
	}
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
		@MetaValue(value = "eu.etaxonomy.cdm.model.description.CategoricalData", targetEntity = CategoricalData.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.description.CommonTaxonName", targetEntity = CommonTaxonName.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.description.Distribution", targetEntity = Distribution.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.description.IndividualsAssociation", targetEntity = IndividualsAssociation.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.description.QuantitativeData", targetEntity = QuantitativeData.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.description.TaxonInteraction", targetEntity = TaxonInteraction.class),
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
		@MetaValue(value = "eu.etaxonomy.cdm.model.reference.Reference", targetEntity = Reference.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.reference.Journal", targetEntity = Reference.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.reference.Article", targetEntity = Reference.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.reference.Book", targetEntity = Reference.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.reference.Generic", targetEntity = Reference.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.reference.Database", targetEntity = Reference.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.reference.BookSection", targetEntity = Reference.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.reference.Reference", targetEntity = Reference.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.reference.PrintSeries", targetEntity = Reference.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.reference.Proceedings", targetEntity = Reference.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.reference.Thesis", targetEntity = Reference.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.reference.WebPage", targetEntity = Reference.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.taxon.Synonym", targetEntity = Synonym.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.taxon.Taxon", targetEntity = Taxon.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.taxon.Classification", targetEntity = Classification.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.description.TextData", targetEntity = TextData.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.agent.TeamOrPersonBase", targetEntity = TeamOrPersonBase.class),
		@MetaValue(value = "eu.etaxonomy.cdm.model.common.User", targetEntity = User.class)
})
package eu.etaxonomy.cdm.model.common;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.MetaValue;
import org.hibernate.annotations.Parameter;

import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.description.CategoricalData;
import eu.etaxonomy.cdm.model.description.CommonTaxonName;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.IndividualsAssociation;
import eu.etaxonomy.cdm.model.description.MediaKey;
import eu.etaxonomy.cdm.model.description.QuantitativeData;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonInteraction;
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
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.Classification;
	