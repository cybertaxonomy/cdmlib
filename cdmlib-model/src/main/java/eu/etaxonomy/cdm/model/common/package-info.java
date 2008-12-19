/* Package Annotations*/

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
@org.hibernate.annotations.AnyMetaDef(name = "AnnotatableEntity" ,
		                              metaType="string", 
		                              idType="integer",
		                              metaValues={
		@MetaValue(value = "Institution", targetEntity = Institution.class),
		@MetaValue(value = "Person", targetEntity = Person.class),
		@MetaValue(value = "Team", targetEntity = Team.class),
		@MetaValue(value = "Annotation", targetEntity = Annotation.class),
		@MetaValue(value = "TaxonDescription", targetEntity = TaxonDescription.class),
		@MetaValue(value = "SpecimenDescription", targetEntity = SpecimenDescription.class),
		@MetaValue(value = "Synonym", targetEntity = Synonym.class),
		@MetaValue(value = "Taxon", targetEntity = Taxon.class)
		})
package eu.etaxonomy.cdm.model.common;

import org.hibernate.annotations.MetaValue;
import org.hibernate.annotations.GenericGenerator;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
	