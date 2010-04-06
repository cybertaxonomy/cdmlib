package eu.etaxonomy.cdm.remote.dto.assembler.lsid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.dozer.util.mapping.MapperIF;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.LazyInitializationException;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.unitils.UnitilsJUnit4;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.DefaultTermInitializer;
import eu.etaxonomy.cdm.model.common.LSID;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.PresenceTerm;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.NonViralName;
import eu.etaxonomy.cdm.model.reference.IBook;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonRelationship;
import eu.etaxonomy.cdm.remote.dto.tdwg.DefinedTerm;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.InfoItem;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.Relationship;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.SpeciesProfileModel;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.StringType;
import eu.etaxonomy.cdm.remote.dto.tdwg.voc.TaxonConcept;

@SpringApplicationContext("file:./target/test-classes/eu/etaxonomy/cdm/applicationContext-test.xml")
public class AssemblerTest extends UnitilsJUnit4 {
	
	@SpringBeanByType
	private MapperIF mapper;
	
	private Taxon taxon;
	private IBook sec;
	private TeamOrPersonBase authorTeam;
	private NonViralName name;
	private LSID lsid;
	private TaxonDescription taxonDescription;
	ReferenceFactory refFactory = ReferenceFactory.newInstance();
	
	@BeforeClass
	public static void onSetUp() {
		DefaultTermInitializer defaultTermInitializer = new DefaultTermInitializer();
		defaultTermInitializer.initialize();
	}
	
	@Before
	public void setUp() throws Exception {   
	    lsid = new LSID("urn:lsid:example.org:taxonconcepts:1");
	    
	    authorTeam = Person.NewInstance();
	    authorTeam.setTitleCache("authorTeam.titleCache");
	    authorTeam.setLsid(new LSID("urn:lsid:dagg.org:agents:2"));
	    
	    name = BotanicalName.NewInstance(null);
	    name.setNameCache("nameCache");
	    name.setAuthorshipCache("authorshipCache");
	    
	    sec = refFactory.newBook();
	    sec.setAuthorTeam(authorTeam);
	    sec.setTitleCache("sec.titleCache");
	    sec.setLsid(new LSID("urn:lsid:example.org:references:1"));
	    
		taxon = Taxon.NewInstance(name, (ReferenceBase)sec);
		taxon.setCreated(new DateTime(2004, 12, 25, 12, 0, 0, 0));
		taxon.setTitleCache("titleCache");
		taxon.setLsid(lsid);

		for(int i = 0; i < 10; i++) {
			Taxon child = Taxon.NewInstance(name, (ReferenceBase)sec);
			child.setLsid(new LSID("urn:lsid:example.org:taxonconcepts:" + (2 + i )));
			taxon.addTaxonomicChild(child, null,null);
		}

		
		taxonDescription = TaxonDescription.NewInstance();
		taxon.addDescription(taxonDescription);
		
		TextData textData = TextData.NewInstance();
		Language english = Language.NewInstance();
		english.setIso639_1("en");
		Language french = Language.NewInstance();
		french.setIso639_1("fr");
		textData.getMultilanguageText().put(english, LanguageString.NewInstance("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce justo leo, tempus ultricies bibendum eu, interdum sit amet ipsum. Suspendisse eu odio in sem iaculis euismod. Suspendisse sed metus ante, in sodales risus. In sit amet magna sit amet risus elementum dapibus. Nullam in magna at mauris placerat sagittis. Proin urna nisl, porta at venenatis in, tristique tempus nisl. Proin vel arcu blandit velit vestibulum blandit. In at diam libero, vel malesuada mauris. Duis a enim diam. Donec urna dui, dictum at suscipit vel, consectetur quis est. In venenatis bibendum diam nec laoreet. ", english));
		textData.getMultilanguageText().put(french, LanguageString.NewInstance("Mauris elementum malesuada orci, non eleifend metus placerat ut. Aenean ornare felis sed lectus cursus id cursus nulla consectetur. Mauris magna justo, placerat id pretium posuere, ultrices et libero. Aliquam erat volutpat. Ut libero diam, interdum commodo fringilla sollicitudin, faucibus vel nisl. Fusce mattis justo interdum enim rhoncus eget sollicitudin dolor lobortis. Morbi mauris odio, tempus eget egestas eu, ornare vel ante. In quis placerat mi. Aliquam blandit tristique dictum. Donec pretium dui lacinia magna ornare eu venenatis ante dignissim. Integer ullamcorper tempus nisl et tincidunt. Curabitur vel nulla eu dolor faucibus porta. Mauris pulvinar est at est porta molestie. Nam varius nunc nec ipsum lacinia non egestas turpis congue. In at ipsum augue. Nulla mollis lobortis mauris ac sagittis. Nullam et facilisis lacus. Nam euismod sapien pellentesque lacus hendrerit dapibus. Aenean blandit rhoncus feugiat.", french));		
		taxonDescription.addElement(textData);
		
		Distribution distribution = Distribution.NewInstance();
		NamedArea namedArea = NamedArea.NewInstance("Africa", "Africa", "Africa");
		namedArea.setTitleCache("Africa");
		distribution.setArea(namedArea);
		distribution.setStatus(PresenceTerm.NATIVE());
		
		taxonDescription.addElement(distribution);
	}
	
	@Test
	public void testDeepMapping() {
		for(int i = 0; i < 3; i++) {
			Synonym synonym = Synonym.NewInstance(name,(ReferenceBase)sec);
			taxon.addSynonym(synonym,new SynonymRelationshipType());
		}
		TaxonConcept taxonConcept = (TaxonConcept)mapper.map(taxon, TaxonConcept.class);
		
		assertNotNull("map() should return an object", taxonConcept);
		assertTrue("map() should return a TaxonConcept",taxonConcept instanceof TaxonConcept);

		assertEquals("IdentifiableEntity.titleCache should be copied into BaseThing.title",taxon.getTitleCache(),taxonConcept.getTitle());
		assertEquals("IdentifiableEntity.lsid should be copied into BaseThing.identifier",taxon.getLsid().toString(),taxonConcept.getIdentifier().toString());
		assertEquals("BaseThing.sameAs should refer to the proxy version of the lsid","http://lsid.example.org/" + taxon.getLsid().toString(),taxonConcept.getSameAs());
		assertEquals("CdmBase.created should be copied into BaseThing.created",new DateTime(2004, 12, 25, 12, 0, 0, 0),taxonConcept.getCreated());
		assertNotNull("TaxonBase.sec should be mapped into BaseThing.publishedInCitation",taxonConcept.getPublishedInCitation());
		assertEquals("TaxonBase.sec.titleCache should be mapped into BaseThing.publishedInCitation.title",sec.getTitleCache(),taxonConcept.getPublishedInCitation().getTitle());
		assertNotNull("TaxonBase.sec.authorTeam should be mapped into TaxonConcept.accordingTo",taxonConcept.getAccordingTo());
		assertEquals("TaxonBase.sec.authorTeam.titleCache should be mapped into TaxonConcept.accordingTo.title",authorTeam.getTitleCache(),taxonConcept.getAccordingTo().getTitle());
		assertNotNull("TaxonBase.name should be mapped to TaxonConcept.hasName",taxonConcept.getHasName());
		assertEquals("NonViralName.nameCache should be mapped to TaxonName.nameComplete",name.getNameCache(),taxonConcept.getHasName().getNameComplete());
		assertNotNull("Taxon.relationsToThisTaxon should be copied into TaxonConcept.hasRelationship",taxonConcept.getHasRelationship());
		assertEquals("There should be 13 relations in TaxonConcept.hasRelationship",13,taxonConcept.getHasRelationship().size());
	}
	
	@Test
	public void testLazyInitializationExceptionWithProxy() throws Exception {
		IBook proxy = getUninitializedDetachedProxy(ReferenceBase.class,(ReferenceBase)sec);
		assert !Hibernate.isInitialized(proxy);
		Field secField = TaxonBase.class.getDeclaredField("sec");
		secField.setAccessible(true);
		secField.set(taxon, proxy);
		
		TaxonConcept taxonConcept = (TaxonConcept)mapper.map(taxon, TaxonConcept.class);
		assertNull("TaxonBase.sec was uninitialized, so TaxonConcept.publishedInCitation should be null",taxonConcept.getPublishedInCitation());
		assertNull("TaxonBase.sec was uninitialized, so TaxonConcept.accordingTo should be null",taxonConcept.getAccordingTo());
	}
	
	@Test 
	public void testLazyInitializationExceptionWithPersistentCollection() throws Exception {
		Set<TaxonRelationship> proxy = (Set<TaxonRelationship>)getUninitializedPersistentCollection(HashSet.class,(HashSet<TaxonRelationship>)taxon.getRelationsToThisTaxon());
		assert !Hibernate.isInitialized(proxy);
		Field relationsToThisTaxonField = Taxon.class.getDeclaredField("relationsToThisTaxon");
		relationsToThisTaxonField.setAccessible(true);
		relationsToThisTaxonField.set(taxon, proxy);
		
		TaxonConcept taxonConcept = (TaxonConcept)mapper.map(taxon, TaxonConcept.class);
		assertTrue("TaxonBase.relationsToThisTaxon was uninitialized, so TaxonConcept.hasRelationship should be null",taxonConcept.getHasRelationship().isEmpty());
	}
	
	@Test
	public void testSpeciesProfileModelMapping() {
		SpeciesProfileModel speciesProfileModel = (SpeciesProfileModel)mapper.map(taxonDescription, SpeciesProfileModel.class);
		assertEquals(speciesProfileModel.getHasInformation().size(),2);
	}
	
	private <T extends Collection> T getUninitializedPersistentCollection(final Class<T> clazz,final T wrappedCollection) {
		final Enhancer enhancer = new Enhancer();
		List<Class> interfaces = new ArrayList<Class>();
		interfaces.addAll(Arrays.asList(clazz.getInterfaces()));
		interfaces.add(PersistentCollection.class);
		enhancer.setSuperclass(clazz);
		enhancer.setInterfaces(interfaces.toArray(new Class[interfaces.size()]));
		enhancer.setCallback( new MethodInterceptor() {
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				if("wasInitialized".equals(method.getName())) {
				  return false;
				} else if(clazz.getDeclaredConstructor().equals(method)){
					 return proxy.invoke(obj, args);
				} else if("finalize".equals(method.getName())){
					 return proxy.invoke(obj, args);
				} else if("toString".equals(method.getName())) {
					return wrappedCollection.toString();
				} else if("getClass".equals(method.getName())) {
					return proxy.invoke(obj, args);
				} else if("hashCode".equals(method.getName())) {
					return wrappedCollection.hashCode();
				}else if("initListener".equals(method.getName())) {
					return null;
				} else {

					throw new LazyInitializationException(null);
				}
				
			}
		});
		
		T proxy = (T)enhancer.create();
		return proxy;
	}
	
	private <T> T getUninitializedDetachedProxy(final Class<T> clazz,final T wrappedClass) {
		final Enhancer enhancer = new Enhancer();
		List<Class> interfaces = new ArrayList<Class>();
		interfaces.addAll(Arrays.asList(clazz.getInterfaces()));
		interfaces.add(HibernateProxy.class);
		enhancer.setSuperclass(clazz);
		enhancer.setInterfaces(interfaces.toArray(new Class[interfaces.size()]));
		enhancer.setCallback( new MethodInterceptor() {
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				if("getHibernateLazyInitializer".equals(method.getName())) {
				  return new UninitializedLazyInitializer();
				} else if(clazz.getDeclaredConstructor().equals(method)){
					 return proxy.invoke(obj, args);
				} else if("finalize".equals(method.getName())){
					 return proxy.invoke(obj, args);
				} else if("toString".equals(method.getName())) {
					return wrappedClass.toString();
				} else if("getClass".equals(method.getName())) {
					return proxy.invoke(obj, args);
				} else if("hashCode".equals(method.getName())) {
					return wrappedClass.hashCode();
				} else if("initListener".equals(method.getName())) {
					return null;
				} else {
					throw new LazyInitializationException(null);
				}
				
			}
		});
		
		T proxy = (T)enhancer.create();
		return proxy;
	}
	
	class UninitializedLazyInitializer implements LazyInitializer {

		public  boolean isUninitialized() {
			return true;
		}

		public String getEntityName() {
			// TODO Auto-generated method stub
			return null;
		}

		public Serializable getIdentifier() {
			// TODO Auto-generated method stub
			return null;
		}

		public Object getImplementation() {
			// TODO Auto-generated method stub
			return null;
		}

		public Object getImplementation(SessionImplementor arg0)
				throws HibernateException {
			// TODO Auto-generated method stub
			return null;
		}

		public Class getPersistentClass() {
			// TODO Auto-generated method stub
			return null;
		}

		public SessionImplementor getSession() {
			// TODO Auto-generated method stub
			return null;
		}

		public void initialize() throws HibernateException {
			// TODO Auto-generated method stub
			
		}

		public boolean isUnwrap() {
			// TODO Auto-generated method stub
			return false;
		}

		public void setIdentifier(Serializable arg0) {
			// TODO Auto-generated method stub
			
		}

		public void setImplementation(Object arg0) {
			// TODO Auto-generated method stub
			
		}

		public void setSession(SessionImplementor arg0)
				throws HibernateException {
			// TODO Auto-generated method stub
			
		}

		public void setUnwrap(boolean arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
	

}
