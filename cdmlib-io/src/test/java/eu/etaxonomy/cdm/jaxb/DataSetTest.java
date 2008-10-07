package eu.etaxonomy.cdm.io.jaxb;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import eu.etaxonomy.cdm.io.jaxb.DataSet;
import eu.etaxonomy.cdm.model.agent.Agent;
import eu.etaxonomy.cdm.model.agent.Institution;
import eu.etaxonomy.cdm.model.agent.InstitutionalMembership;
import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.AnnotatableEntity;
import eu.etaxonomy.cdm.model.common.DefinedTermBase;
import eu.etaxonomy.cdm.model.common.Keyword;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.RelationshipBase;
import eu.etaxonomy.cdm.model.common.Representation;
import eu.etaxonomy.cdm.model.common.TermBase;
import eu.etaxonomy.cdm.model.common.TermVocabulary;
import eu.etaxonomy.cdm.model.common.TimePeriod;
import eu.etaxonomy.cdm.model.common.VersionableEntity;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.Database;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.StrictReferenceBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;

public class DataSetTest {

	private DataSet dataSet;
	private List<Agent> agents;
    private List<VersionableEntity> agentData;
    //private List<TermBase> terms;
    private List<DefinedTermBase> terms;
    private List<TermVocabulary<DefinedTermBase>> termVocabularies;
    private List<ReferenceBase> references;
    private List<TaxonNameBase> taxonomicNames;
    private List<Taxon> taxa;
    private List<Synonym> synonyms;
    private List<AnnotatableEntity> homotypicalGroups;
	private Set<TaxonRelationship> taxonRelationships;
	private Set<RelationshipBase> relationshipBases;
	
	@Before
	public void onSetUp() throws Exception {
		dataSet = new DataSet();
	}
	
	@Test
	public void testCdmDocumentBuilderInit() {
		Assert.assertNotNull(dataSet);
	}

	/**
	 * This method constructs a small sample taxonomic tree to test JAXB marshaling.
	 * The sample tree contains four taxa. The root taxon has two children taxa, and
	 * there is one "free" taxon without a parent and children.
	 */
	public void buildData(boolean persistentContext) {

		agents = new ArrayList<Agent>();
		agentData = new ArrayList<VersionableEntity>();
		//terms = new ArrayList<TermBase>();
		terms = new ArrayList<DefinedTermBase>();
		termVocabularies = new ArrayList<TermVocabulary<DefinedTermBase>>();
	    references = new ArrayList<ReferenceBase>();
		taxonomicNames = new ArrayList<TaxonNameBase>();
		taxa = new ArrayList<Taxon>();
		synonyms = new ArrayList<Synonym>();
    	taxonRelationships = new HashSet();
		
		StrictReferenceBase citRef, sec;
		BotanicalName name1, name2, nameRoot, nameFree, synName11, synName12, synName2, synNameFree;
		Taxon child1, child2, rootT, freeT;
		Synonym syn11, syn12, syn2, synFree;
		Rank rankSpecies, rankSubspecies, rankGenus;

		// agents 
		// - persons, institutions 

		Person linne = new Person("Carl", "Linné", "L.");
		GregorianCalendar birth = new GregorianCalendar(1707, 4, 23);
		GregorianCalendar death = new GregorianCalendar(1778, 0, 10);
		TimePeriod period = new TimePeriod(birth, death);
		linne.setLifespan(period);

		Language lang = Language.ENGLISH();
		TermVocabulary termvoc = new TermVocabulary();
		lang.setVocabulary(termvoc);
		Keyword keyword = Keyword.NewInstance("plantarum", lang.getLabel(), "");
		Representation rep = keyword.getRepresentation(lang);
		rep.setLanguage(lang);
		
		linne.addKeyword(keyword);

		Institution institute = Institution.NewInstance();

		agents.add(linne);
		agents.add(institute);

		// agent data
		// - contacts, addresses, memberships

		//Contact contact1 = new Contact();
		//contact1.setEmail("someone@somewhere.org");
		InstitutionalMembership membership 
		= new InstitutionalMembership(institute, linne, period, "Biodiversity", "Head");
		//agentData.add(contact1);

		agentData.add(membership);

		// terms
		// - ranks, keywords

		if (persistentContext == true) {
			
		rankSpecies = Rank.SPECIES();
		rankSubspecies = Rank.SUBSPECIES();
		rankGenus = Rank.GENUS();
		
		} else {
		rankSpecies = new Rank ();
		rankSubspecies = new Rank();
		rankGenus = new Rank();
		}
		
//      Do something like this? If yes, FIXME: Stack overflow.
//		try {
//			rankSpecies = Rank.getRankByName("Species");
//			rankSubspecies = Rank.getRankByName("Subspecies");
//			rankGenus = Rank.getRankByName("Genus");
//			
//		} catch (UnknownCdmTypeException ex) {
//			ex.printStackTrace();
//		}
		
		terms.add(rankSpecies);
		terms.add(rankSubspecies);
		terms.add(rankGenus);
		terms.add(lang);
		
		terms.add(keyword);
		
		termVocabularies.add(termvoc);
		
        // taxonomic names
		
		nameRoot = BotanicalName.NewInstance(rankGenus,"Calendula",null,null,null,linne,null,"p.100", null);
		
		name1 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"arvensis",null,linne,null,"p.1", null);
		synName11 = BotanicalName.NewInstance(rankSpecies,"Caltha",null,"arvensis",null,linne,null,"p.11", null);
		synName12 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"sancta",null,linne,null,"p.12", null);
		
		name2 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"lanzae",null,linne,null,"p.2", null);
		synName2 = BotanicalName.NewInstance(rankSpecies,"Calendula",null,"echinata",null,linne,null,"p.2", null);
		
		nameFree = BotanicalName.NewInstance(rankSpecies,"Cichorium",null,"intybus",null,linne,null,"p.200", null);
		synNameFree = BotanicalName.NewInstance(rankSpecies,"Cichorium",null,"balearicum",null,linne,null,"p.2", null);

		taxonomicNames.add(nameRoot);
		taxonomicNames.add(name1);
		taxonomicNames.add(synName11);
		taxonomicNames.add(synName12);
		taxonomicNames.add(name2);
		taxonomicNames.add(synName2);
		taxonomicNames.add(nameFree);
		taxonomicNames.add(synNameFree);
		
        // references
		
		sec = Book.NewInstance();
		sec.setAuthorTeam(linne);
		sec.setTitleCache("Plant Speciation");
		references.add(sec);
		
		citRef = Database.NewInstance();
		citRef.setAuthorTeam(linne);
		citRef.setTitleCache("BioCASE");
		references.add(citRef);

		// taxa
		
		rootT = Taxon.NewInstance(nameRoot, sec);
		freeT = Taxon.NewInstance(nameFree, sec);
		child1 = Taxon.NewInstance(name1, sec);
		child2 = Taxon.NewInstance(name2, sec);
		
		// synonyms
		
		synFree = Synonym.NewInstance(synNameFree, sec);
		syn11 = Synonym.NewInstance(synName11, sec);
		syn12 = Synonym.NewInstance(synName12, sec);
		syn2 = Synonym.NewInstance(synName2, sec);
		
		child1.addSynonym(syn11, SynonymRelationshipType.HOMOTYPIC_SYNONYM_OF());
		child1.addSynonym(syn12, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		child2.addSynonym(syn2, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());
		freeT.addSynonym(synFree, SynonymRelationshipType.HETEROTYPIC_SYNONYM_OF());

		synonyms.add(synFree);
		synonyms.add(syn11);
		synonyms.add(syn12);
		synonyms.add(syn2);
		
		// relationships
		
		
		// taxonomic children
		
		rootT.addTaxonomicChild(child1, sec, "p.998");
		rootT.addTaxonomicChild(child2, sec, "p.987");
				
		taxa.add(rootT);
		taxa.add(freeT);
		taxa.add(child1);
		taxa.add(child2);
		
//	    List<Taxon> taxa;
//    	Object obj = (Object)taxa;
//    	Collection<TaxonBase> taxonBases = (Collection<TaxonBase>)obj;

//		Set<TaxonRelationship> taxonRelationships;
    	taxonRelationships = rootT.getTaxonRelations();
    	Object obj = taxonRelationships;
    	relationshipBases = (Set<RelationshipBase>)obj;
		
	}
		
	public DataSet buildDataSet(boolean persistentContext) {

		buildData(persistentContext);
		
		dataSet.setAgents(agents);
		dataSet.setAgentData(agentData);
		dataSet.setTerms(terms);
		dataSet.setTermVocabularies(termVocabularies);
		dataSet.setReferences(references);
		dataSet.setTaxonomicNames(taxonomicNames);
		dataSet.setTaxa(taxa);
		dataSet.setSynonyms(synonyms);
		dataSet.setRelationships(relationshipBases);

		return dataSet;
	}

	public DataSet buildDataSet(DataSet dataSet, boolean persistentContext) {

		buildData(persistentContext);
		
		dataSet.setAgents(agents);
		dataSet.setAgentData(agentData);
		dataSet.setTerms(terms);
		dataSet.setTermVocabularies(termVocabularies);
		dataSet.setReferences(references);
		dataSet.setTaxonomicNames(taxonomicNames);
		dataSet.setTaxa(taxa);
		dataSet.setSynonyms(synonyms);

		return dataSet;
	}
}
