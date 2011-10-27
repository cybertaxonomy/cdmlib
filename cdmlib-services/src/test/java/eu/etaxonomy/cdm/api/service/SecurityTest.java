package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.ReflectionSaltSource;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.ExpectedException;


import org.unitils.database.annotations.Transactional;
import org.unitils.UnitilsJUnit4TestClassRunner;
import org.unitils.database.annotations.TestDataSource;
import org.unitils.database.util.TransactionMode;
import org.unitils.dbunit.annotation.DataSet;
import org.unitils.spring.annotation.SpringApplicationContext;
import org.unitils.spring.annotation.SpringBeanByName;


import eu.etaxonomy.cdm.api.service.config.ITaxonServiceConfigurator;
import eu.etaxonomy.cdm.api.service.config.TaxonServiceConfiguratorImpl;
import eu.etaxonomy.cdm.api.service.pager.Pager;
import eu.etaxonomy.cdm.database.EvaluationFailedException;
import eu.etaxonomy.cdm.model.common.User;


import eu.etaxonomy.cdm.model.description.DescriptionElementBase;
import eu.etaxonomy.cdm.model.description.Distribution;
import eu.etaxonomy.cdm.model.description.Feature;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;

import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Synonym;

import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.permission.CdmPermissionEvaluator;
import eu.etaxonomy.cdm.persistence.dao.BeanInitializer;




@RunWith(UnitilsJUnit4TestClassRunner.class)
@SpringApplicationContext({"/eu/etaxonomy/cdm/applicationContextSecurity.xml"})
@Transactional(TransactionMode.DISABLED)
@DataSet
public class SecurityTest {
private static final Logger logger = Logger.getLogger(TaxonServiceImplTest.class);
	
	@SpringBeanByName
	private ITaxonService taxonService;
	
	@SpringBeanByName
	private ITaxonNodeService taxonNodeService;
	
	@SpringBeanByName
	private IDescriptionService descriptionService;
	
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
		//taxonService.getSession().flush();
		TaxonBase<?> actualTaxon = taxonService.load(uuid);
		assertEquals(expectedTaxon, actualTaxon);
		
		token = new UsernamePasswordAuthenticationToken("taxonEditor", "test2");
		authentication = authenticationManager.authenticate(token);
		context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		expectedTaxon = Taxon.NewInstance(BotanicalName.NewInstance(Rank.GENUS()), null);
		taxonService.saveOrUpdate(actualTaxon);
		
		
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
		TaxonBase<?> actualTaxon = taxonService.load(uuid);
		assertEquals(expectedTaxon, actualTaxon);
		
		actualTaxon.setName(BotanicalName.NewInstance(Rank.SPECIES()));
		taxonService.saveOrUpdate(actualTaxon);
		
		token = new UsernamePasswordAuthenticationToken("taxonEditor", "test2");
		authentication = authenticationManager.authenticate(token);
		context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		actualTaxon = taxonService.load(uuid);
		actualTaxon.setName(BotanicalName.NewInstance(Rank.GENUS()));
		taxonService.saveOrUpdate(actualTaxon);
			
	}
	
	
	
	@Test
	public void testCascadingInSpringSecurityAccesDenied(){
		/*authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("partEditor", "test4"));
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		*/
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("descriptionEditor", "test"));
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		CdmPermissionEvaluator permissionEvaluator = new CdmPermissionEvaluator();
		
		Taxon taxon =(Taxon) taxonService.load(UUID.fromString("bc09aca6-06fd-4905-b1e7-cbf7cc65d783"));
		TaxonDescription description = TaxonDescription.NewInstance(taxon);
		description.setTitleCache("test");
		assertTrue(permissionEvaluator.hasPermission(authentication, description, "UPDATE"));
		
		
		//during cascading the permissions are not evaluated, but with hibernate listener every database transaction can be interrupted, but how to manage it, 
		//when someone has the rights to save descriptions, but not taxa (the editor always saves everything by saving the taxon)
		//taxonService.saveOrUpdate(taxon);
		
		
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("descriptionEditor", "test"));
		context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		
		//taxonService.saveOrUpdate(taxon);
		taxon = null;
				
		descriptionService.saveOrUpdate(description);
		taxon = (Taxon)taxonService.load(UUID.fromString("bc09aca6-06fd-4905-b1e7-cbf7cc65d783"));
		assertTrue(taxon.getDescriptions().contains(description));
		
		
		
	}
	
	@Test
	public void testCascadingInSpring(){
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("descriptionEditor", "test"));
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		Taxon taxon = (Taxon)taxonService.load(UUID.fromString("928a0167-98cd-4555-bf72-52116d067625"));
		TaxonDescription description = TaxonDescription.NewInstance(taxon);
		description.addElement(Distribution.NewInstance());
		CdmPermissionEvaluator permissionEvaluator = new CdmPermissionEvaluator();
		assertTrue(permissionEvaluator.hasPermission(authentication, description, "UPDATE"));
		
		descriptionService.saveOrUpdate(description);
		
		taxon = (Taxon)taxonService.load(UUID.fromString("928a0167-98cd-4555-bf72-52116d067625"));
		Set<TaxonDescription> descriptions = taxon.getDescriptions();
		assertTrue(descriptions.contains(description));
		
		
	}
	
	@Test
	public void testSaveSynonym(){
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("taxonomist", "test4"));
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		
		Synonym syn = Synonym.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null);
		taxonService.saveOrUpdate(syn);
		
	}
	
	@Test(expected= EvaluationFailedException.class)
	public void testEditPartOfClassification(){
		authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("partEditor", "test4"));
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		
		TaxonNode node = taxonNodeService.load(UUID.fromString("20c8f083-5870-4cbd-bf56-c5b2b98ab6a7"));
		
		node = node.addChildTaxon(Taxon.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null), null, null, null);
		taxonNodeService.saveOrUpdate(node);
		
		node = taxonNodeService.load(UUID.fromString("cecfa77f-f26a-4476-9d87-a8d993cb55d9"));
		node = node.addChildTaxon(Taxon.NewInstance(BotanicalName.NewInstance(Rank.GENUS()), null), null, null, null);
		taxonNodeService.saveOrUpdate(node);
		
	}
	
	public static void main(String[] args){
		Md5PasswordEncoder encoder =new Md5PasswordEncoder();
	
		ReflectionSaltSource saltSource = new ReflectionSaltSource();
		saltSource.setUserPropertyToUse("getUsername");
		User user = User.NewInstance("taxonomist", "test4");
		System.err.println(encoder.encodePassword("test4", saltSource.getSalt(user)));
	}
}
