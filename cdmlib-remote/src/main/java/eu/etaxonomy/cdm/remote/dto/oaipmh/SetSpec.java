package eu.etaxonomy.cdm.remote.dto.oaipmh;

import javax.xml.bind.annotation.XmlEnum;

import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.Team;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.IdentifiableEntity;
import eu.etaxonomy.cdm.model.description.SpecimenDescription;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TaxonNameDescription;
import eu.etaxonomy.cdm.model.name.TaxonName;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;

@XmlEnum
public enum SetSpec {
	PERSON("person","People", Person.class,null),
	TEAM("team","Teams of People", Team.class,null),
	INSTITUTION("institution", "Institutions & Organisations", Institution.class,null),
	TEAM_OR_PERSON("teamOrPerson", "Teams of individuals and individuals", TeamOrPersonBase.class,new SetSpec[]{TEAM,PERSON}),
	TAXON("taxon","Accepted Taxon Concepts", Taxon.class,null),
	SYNONYM("synonym","Synonyms", Synonym.class,null),
	BACTERIAL_NAME("taxonName", "Scientific Taxon Names", TaxonName.class,null),
	TAXON_DESCRIPTION("taxonDescription","Descriptions of taxonomic concepts",TaxonDescription.class,null),
	TAXON_NAME_DESCRIPTION("taxonNameDescription","Descriptions of scientific names",TaxonNameDescription.class,null),
	SPECIMEN_DESCRIPTION("specimenDescription","Descriptions of specimens and occurrences",SpecimenDescription.class,null),
	REFERENCE("reference","Any kind of Reference",Reference.class,null);

	private String spec;
	private String name;
	private SetSpec[] innerSets;
	private Class<? extends IdentifiableEntity> setClass;

	private SetSpec(String spec, String name, Class<? extends IdentifiableEntity> setClass,SetSpec[] innerSets) {
		this.setClass = setClass;
		this.innerSets = innerSets;
		this.spec = spec;
		this.name = name;
	}

	public Class<? extends IdentifiableEntity> getSetClass() {
		return setClass;
	}

	public SetSpec[] getInnerSets() {
		return innerSets;
	}

    public String getName() {
    	return name;
    }

    public String getSpec() {
    	return spec;
    }

    public static SetSpec bySpec(String spec){
    	for(SetSpec setSpec : SetSpec.values()) {
			if(setSpec.getSpec().equals(spec)) {
				return setSpec;
			}
		}
    	return null;
    }
}
