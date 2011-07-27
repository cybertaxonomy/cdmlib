package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.ReflectionSaltSource;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;
import org.unitils.database.util.TransactionMode;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.database.annotations.TestDataSource;
import org.unitils.database.util.TransactionMode;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;
import org.unitils.spring.annotation.SpringBeanByType;

import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;

import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;

import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.SynonymRelationshipType;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.permission.CdmPermissionEvaluator;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;

import org.springframework.security.access.AccessDeniedException;

@RunWith(UnitilsJUnit4TestClassRunner.class)
@SpringApplicationContext({"/eu/etaxonomy/cdm/applicationContextSecurity.xml"})
@Transactional
@DataSet
public class SecurityTest {
private static final Logger logger = Logger.getLogger(TaxonServiceImplTest.class);
	
	@SpringBeanByName
	private ITaxonService taxonService;
	
	@SpringBeanByName
	private IDescriptionService descriptionService;
	
	@SpringBeanByName
	private ITaxonNodeService taxonNodeService;
	
	@SpringBeanByName
	private IUserService userService;
	
	
	@TestDataSource
	protected DataSource dataSource;
	
	private Authentication authentication;
	
	@SpringBeanByName
	private AuthenticationManager authenticationManager;
	
	private UsernamePasswordAuthenticationToken token;
	
	@Before
	public void setUp(){
		token = new UsernamePasswordAuthenticationToken("ben", "sPePhAz6");
	}
	
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.api.service.TaxonServiceImpl#saveTaxon(eu.etaxonomy.cdm.model.taxon.TaxonBase)}.
	 */
	@Test
	public final void testSaveTaxon() {
		/*
		Md5PasswordEncoder encoder =new Md5PasswordEncoder();
		ReflectionSaltSource saltSource = new ReflectionSaltSource();
		saltSource.setUserPropertyToUse("getUsername");
		User user = User.NewInstance("partEditor", "test4");
		System.err.println(encoder.encodePassword("test4", saltSource.getSalt(user)));
		
		*/
		authentication = authenticationManager.authenticate(token);
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		
		Taxon expectedTaxon = Taxon.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null);
		UUID uuid = taxonService.save(expectedTaxon);
		TaxonBase<?> actualTaxon = taxonService.find(uuid);
		assertEquals(expectedTaxon, actualTaxon);
		
		token = new UsernamePasswordAuthenticationToken("taxonEditor", "test2");
		authentication = authenticationManager.authenticate(token);
		context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		expectedTaxon = Taxon.NewInstance(BotanicalName.NewInstance(Rank.GENUS()), null);
		taxonService.save(actualTaxon);
		
		
	}
	@Test
	public void testUpdateUser(){
		
		authentication = authenticationManager.authenticate(token);
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		String username = "standardUser";
		String password = "pw";
		User user = User.NewInstance(username, password);
		
		userService.createUser(user);
		user.setEmailAddress("test@bgbm.org");
		
		userService.updateUser(user);
		userService.update(user);
		userService.saveOrUpdate(user);
	}
	
	@Test
	public final void testSaveOrUpdateTaxon() {
		authentication = authenticationManager.authenticate(token);
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		Taxon expectedTaxon = Taxon.NewInstance(null, null);
		UUID uuid = taxonService.save(expectedTaxon);
		TaxonBase<?> actualTaxon = taxonService.find(uuid);
		assertEquals(expectedTaxon, actualTaxon);
		
		actualTaxon.setName(BotanicalName.NewInstance(Rank.SPECIES()));
		taxonService.saveOrUpdate(actualTaxon);
		
		token = new UsernamePasswordAuthenticationToken("taxonEditor", "test2");
		authentication = authenticationManager.authenticate(token);
		context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		actualTaxon = taxonService.find(uuid);
		actualTaxon.setName(BotanicalName.NewInstance(Rank.GENUS()));
		taxonService.saveOrUpdate(actualTaxon);
			
	}
	
	@Test
	public void testDeleteTaxon(){
		token = new UsernamePasswordAuthenticationToken("taxonomist", "test3");
		authentication = authenticationManager.authenticate(token);
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		Taxon actualTaxon = (Taxon)taxonService.find(UUID.fromString("7b8b5cb3-37ba-4dba-91ac-4c6ffd6ac331"));
		
		taxonService.delete(actualTaxon);
	}
	
	
	@Test
	public void testSaveOrUpdateDescription(){
		
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("descriptionEditor", "test"));
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		Taxon taxon = (Taxon) taxonService.find(UUID.fromString("7b8b5cb3-37ba-4dba-91ac-4c6ffd6ac331"));
		
		Set<TaxonDescription> descriptions = taxon.getDescriptions();
		
		Iterator<TaxonDescription> iterator = descriptions.iterator();
		
		TaxonDescription description = iterator.next();
		description = (TaxonDescription) descriptionService.find(description.getUuid());
		Iterator<DescriptionElementBase> elements = description.getElements().iterator();
		TextData textData = new TextData();
		textData.setFeature(Feature.ECOLOGY());
		Media media = Media.NewInstance();
		textData.addMedia(media);
		
		
		
		descriptionService.saveDescriptionElement(textData);
		description.addElement(textData);
		
		descriptionService.saveOrUpdate(description);
		
		taxon = (Taxon) taxonService.find(UUID.fromString("7b8b5cb3-37ba-4dba-91ac-4c6ffd6ac331"));
		descriptions = taxon.getDescriptions();
		
		iterator = descriptions.iterator();
		
		description = iterator.next();
		assertEquals(1,description.getElements().iterator().next().getMedia().size());
		
	}
	
	@Test
	public void testAllowOnlyAccessToPartOfTree(){
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("partEditor", "test4"));
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		
		Taxon tribe = (Taxon)taxonService.find(UUID.fromString("928a0167-98cd-4555-bf72-52116d067625"));
		Taxon taxon = (Taxon)taxonService.find(UUID.fromString("bc09aca6-06fd-4905-b1e7-cbf7cc65d783"));
		Iterator<TaxonNode> it = tribe.getTaxonNodes().iterator();
		TaxonNode node = it.next();
		
		CdmPermissionEvaluator permissionEvaluator = new CdmPermissionEvaluator();
		assertFalse(permissionEvaluator.hasPermission(authentication, node, "UPDATE"));
		node = node.getChildNodes().iterator().next();
		System.err.println(node.getUuid()); 
		assertTrue(permissionEvaluator.hasPermission(authentication, node, "UPDATE"));
		node = node.getChildNodes().iterator().next();
		assertTrue(permissionEvaluator.hasPermission(authentication, node, "UPDATE"));
		TaxonDescription description = TaxonDescription.NewInstance(taxon);
		
		taxonNodeService.saveOrUpdate(node);
		assertFalse(permissionEvaluator.hasPermission(authentication, description, "UPDATE"));
		
		
	}
	
	@Test
	public void testCascadingInSpringSecurity(){
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("partEditor", "test4"));
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		CdmPermissionEvaluator permissionEvaluator = new CdmPermissionEvaluator();
		Taxon taxon = (Taxon)taxonService.find(UUID.fromString("bc09aca6-06fd-4905-b1e7-cbf7cc65d783"));
		TaxonDescription description = TaxonDescription.NewInstance(taxon);
		assertFalse(permissionEvaluator.hasPermission(authentication, description, "UPDATE"));
		//during cascading the permissions are not evaluated
		taxonService.saveOrUpdate(taxon);
		
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("descriptionEditor", "test"));
		context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		taxon = (Taxon)taxonService.find(UUID.fromString("928a0167-98cd-4555-bf72-52116d067625"));
		description = TaxonDescription.NewInstance(taxon);
		assertTrue(permissionEvaluator.hasPermission(authentication, description, "UPDATE"));
		
	}
	
	@Test
	public void testSaveSynonym(){
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("partEditor", "test4"));
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		Synonym syn = Synonym.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null);
		taxonService.saveOrUpdate(syn);
		
	}
}
