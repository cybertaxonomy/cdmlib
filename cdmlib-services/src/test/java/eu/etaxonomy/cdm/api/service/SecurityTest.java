package eu.etaxonomy.cdm.api.service;

import static org.junit.Assert.assertEquals;


import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

import eu.etaxonomy.cdm.model.common.User;
import eu.etaxonomy.cdm.model.description.DescriptionElementBase;

import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.description.TextData;

import eu.etaxonomy.cdm.model.media.Media;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

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
		
		authentication = authenticationManager.authenticate(token);
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(authentication);
		
		Taxon expectedTaxon = Taxon.NewInstance(BotanicalName.NewInstance(Rank.SPECIES()), null);
		UUID uuid = taxonService.save(expectedTaxon);
		TaxonBase<?> actualTaxon = taxonService.find(uuid);
		assertEquals(expectedTaxon, actualTaxon);
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
		//userService.update(user);
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
		try{
			taxonService.saveOrUpdate(actualTaxon);
		}catch(Exception e){
			Assert.fail();
		}
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
		DescriptionElementBase element = elements.next();
		TextData textData = (TextData) element;
		Media media = Media.NewInstance();
		textData.addMedia(media);
		
		descriptionService.saveDescriptionElement(element);
		
		taxon = (Taxon) taxonService.find(UUID.fromString("7b8b5cb3-37ba-4dba-91ac-4c6ffd6ac331"));
		descriptions = taxon.getDescriptions();
		
		iterator = descriptions.iterator();
		
		description = iterator.next();
		
		//taxonService.saveOrUpdate(taxon);
	}
}
