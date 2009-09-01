// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.agent.TeamOrPersonBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.BotanicalName;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.ZoologicalName;
import eu.etaxonomy.cdm.model.reference.Article;
import eu.etaxonomy.cdm.model.reference.Book;
import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.ReferenceBase;

/**
 * @author a.mueller
 * @created 01.04.2009
 * @version 1.0
 */
public class TaxonomicTreeTest {
	private static final Logger logger = Logger.getLogger(TaxonomicTreeTest.class);

	private static String viewName1;
	private static TaxonomicTree taxonomicView1;
	private static TaxonNode taxonNode1;
	private static TaxonNode taxonNode2;
	private static TaxonNode taxonNode3;
	private static Taxon taxon1;
	private static Taxon taxon2;
	private static TaxonNameBase<?,?> taxonName1;
	private static TaxonNameBase<?,?> taxonName2;
	private static ReferenceBase ref1;
	private static ReferenceBase ref2;
	
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		viewName1 = "Greuther, 1993";
		taxonomicView1 = TaxonomicTree.NewInstance(viewName1);
		taxonName1 = BotanicalName.NewInstance(Rank.SPECIES());
		taxonName1 = ZoologicalName.NewInstance(Rank.SPECIES());
		ref1 = Journal.NewInstance();
		ref2 = Journal.NewInstance();
		taxon1 = Taxon.NewInstance(taxonName1, ref1);
		taxon2 = Taxon.NewInstance(taxonName2, ref2);
		
		//taxonNode1 = new TaxonNode(taxon1, taxonomicView1);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

//****************************** TESTS *****************************************/

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonomicTree#addRoot(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.reference.ReferenceBase, java.lang.String, eu.etaxonomy.cdm.model.taxon.Synonym)}.
	 */
	@Test
	public void testAddRoot() {
		TaxonNameBase<?,?> synonymName = BotanicalName.NewInstance(Rank.SPECIES());
		Synonym synonym = Synonym.NewInstance(synonymName, ref1);
		TaxonNode taxonNode1 = taxonomicView1.addRoot(taxon1, synonym, null);
		
		
		
		
		//test root node
		Set<TaxonNode> rootNodes = taxonomicView1.getRootNodes();
		assertFalse("List of root nodes should not be empty", rootNodes.isEmpty());
		assertEquals("Number of root nodes should be 1", 1, rootNodes.size());
		TaxonNode root = rootNodes.iterator().next();
		assertEquals(taxon1, root.getTaxon());
		assertSame(taxonNode1, root);
		assertNull(root.getReferenceForParentChildRelation());
		assertNull(root.getMicroReferenceForParentChildRelation());
		assertEquals(synonym, root.getSynonymToBeUsed());
		
		//any node
		Set<TaxonNode> allNodes = taxonomicView1.getRootNodes();
		assertFalse("List of root nodes should not be empty", allNodes.isEmpty());
		assertEquals("Number of root nodes should be 1", 1, allNodes.size());
		TaxonNode anyNode = allNodes.iterator().next();
		assertSame("Taxon for TaxonNode should be the same added to the view", taxon1, anyNode.getTaxon());
		assertSame("TaxonNode should be the same added to the view", taxonNode1, anyNode);
				
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonomicTree#isTaxonInView(eu.etaxonomy.cdm.model.taxon.Taxon)}.
	 */
	@Test
	public void testIsTaxonInTree() {
		taxonomicView1.addRoot(taxon1, null, null);
		
		assertTrue(taxonomicView1.isTaxonInTree(taxon1));
		Taxon anyTaxon = Taxon.NewInstance(null, null);
		assertFalse(taxonomicView1.isTaxonInTree(anyTaxon));
	}
	
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonomicTree#makeRootChildOfOtherNode(eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.model.reference.ReferenceBase, java.util.String)}.
	 */
	@Test
	public void testMakeRootChildOfOtherNode() {
		TaxonNode root1 = taxonomicView1.addRoot(taxon1, null, null);
		TaxonNode root2 = taxonomicView1.addRoot(taxon2, null, null);
		Taxon taxon3 = Taxon.NewInstance(null, null);
		root2.addChild(taxon3);
		String microRef = "p55";
		
		assertFalse("Root1 must not yet be child of root 2", root2.getChildNodes().contains(root1));
		assertNotSame("Root2 must not yet be parent of root 1", root2, root1.getParent());
		assertEquals("view must contain 3 nodes", 3, taxonomicView1.getAllNodes().size());
		assertEquals("view must still contain 2 root", 2, taxonomicView1.getRootNodes().size());
		assertEquals("root2 must have 1 child", 1, root2.getChildNodes().size());
		
		taxonomicView1.makeRootChildOfOtherNode(root1, root2, ref1, microRef);
		assertTrue("Root1 must be child of root 2", root2.getChildNodes().contains(root1));
		assertSame("Root2 must be parent of root 1", root2, root1.getParent());
		assertEquals("view must contain 3 nodes", 3, taxonomicView1.getAllNodes().size());
		assertEquals("view must contain 1 root", 1, taxonomicView1.getRootNodes().size());
		assertEquals("new child node must have the expected reference for parent child relationship", ref1, root1.getReferenceForParentChildRelation());
		assertEquals("new child node must have the expected micro reference for parent child relationship", microRef, root1.getMicroReferenceForParentChildRelation());
		assertEquals("root2 must have 2 children", 2, root2.getChildNodes().size());
		
	}
	
	@Test
	public void testIsRootInTree() {
		TaxonNode root = taxonomicView1.addRoot(taxon1, null, null);
		
		assertTrue(taxonomicView1.isTaxonInTree(taxon1));
		assertTrue(taxonomicView1.isRootInTree(taxon1));
		Taxon anyTaxon = Taxon.NewInstance(null, null);
		assertFalse(taxonomicView1.isTaxonInTree(anyTaxon));
		assertFalse(taxonomicView1.isRootInTree(anyTaxon));
		Taxon child = Taxon.NewInstance(null, null);
		root.addChild(child);
		assertTrue(taxonomicView1.isTaxonInTree(child));
		assertFalse(taxonomicView1.isRootInTree(child));
	}
	
	@Test
	public void testGetRootNode() {
		TaxonNode root = taxonomicView1.addRoot(taxon1, null, null);
		
		assertEquals(root, taxonomicView1.getRootNode(taxon1));
		Taxon anyTaxon = Taxon.NewInstance(null, null);
		assertFalse(taxonomicView1.isTaxonInTree(anyTaxon));
		assertNull(taxonomicView1.getRootNode(anyTaxon));
		Taxon child = Taxon.NewInstance(null, null);
		root.addChild(child);
		assertTrue(taxonomicView1.isTaxonInTree(child));
		assertNull(taxonomicView1.getRootNode(child));
	}
	
	@Test
	public void testAddParentChild() {

		TaxonNameBase<?,?> synonymName = BotanicalName.NewInstance(Rank.SPECIES());
		Synonym synonym = Synonym.NewInstance(synonymName, ref1);
		TaxonNode rootNode = taxonomicView1.addRoot(taxon1, synonym, null);
		Assert.assertEquals(0,rootNode.getChildNodes().size());
		
		//add child to existing root
		taxonomicView1.addParentChild(taxon1, taxon2, ref1, "Micro1");
		Assert.assertTrue(taxonomicView1.isTaxonInTree(taxon2));
		Assert.assertFalse(taxonomicView1.isRootInTree(taxon2));
		Assert.assertEquals(1,rootNode.getChildNodes().size());
		TaxonNode childNode = rootNode.getChildNodes().iterator().next();
		Assert.assertEquals(taxon2, childNode.getTaxon());
		
		//relationship already exists
		taxonomicView1.addParentChild(taxon1, taxon2, ref2, "Micro2");
		Assert.assertTrue(taxonomicView1.isTaxonInTree(taxon2));
		Assert.assertFalse(taxonomicView1.isRootInTree(taxon2));
		Assert.assertEquals(2, taxonomicView1.getAllNodes().size());
		Assert.assertEquals(1,rootNode.getChildNodes().size());
		childNode = rootNode.getChildNodes().iterator().next();
		Assert.assertEquals(taxon2, childNode.getTaxon());
		Assert.assertEquals(ref2, childNode.getReferenceForParentChildRelation());
		Assert.assertEquals("Micro2", childNode.getMicroReferenceForParentChildRelation());
		
		logger.info("testAddParentChild not yet fully implemented");

	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonomicTree#generateTitle()}.
	 */
	@Test
	public void testGenerateTitle() {
		TaxonomicTree taxonomicViewLocal = TaxonomicTree.NewInstance(viewName1);
		//Maybe changed if title cache is generated in a different way
		assertEquals(viewName1, taxonomicViewLocal.getTitleCache());
	}
	
	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.TaxonomicTree#generateTitle()}.
	 */
	@Test
	public void play() {
			
			CdmBase referencedCdmBase = Person.NewInstance();
			Set<Class<? extends CdmBase>> allCdmClasses = findAllCdmClasses();
			
			Class referencedClass = referencedCdmBase.getClass();
			Set<CdmBase> result = new HashSet<CdmBase>();
			System.out.println("Referenced Class: " + referencedClass.getName());
			
			
			for (Class<? extends CdmBase> cdmClass : allCdmClasses){
				Set<Field> fields = getFields(cdmClass);
				for (Field field: fields){
					Class<?> type = field.getType();
					if (! type.isInterface()){
						if (referencedClass.isAssignableFrom(type)|| 
								type.isAssignableFrom(referencedClass) && CdmBase.class.isAssignableFrom(type)){
							handleSingleClass(referencedClass, type, field, cdmClass, result, referencedCdmBase);
						}
					}else{  //interface
						if (type.isAssignableFrom(referencedClass)){
							handleSingleClass(referencedClass, type, field, cdmClass, result, referencedCdmBase);
						}
					}
//					Class[] interfaces = referencedClass.getInterfaces();
//					for (Class interfaze: interfaces){
//						if (interfaze == type){
////						if(interfaze.isAssignableFrom(returnType)){
//						}
//					}
					
					
				}	
			}
			return ;
//			find(cdmClass, )
			
		}
		
		private boolean handleSingleClass(Class classToBeSearchedFor, Class type, Field field, Class cdmClass, Set<CdmBase> result,CdmBase value){
			if (! Modifier.isStatic(field.getModifiers())){
				String methodName = StringUtils.rightPad(field.getName(), 30);
				String className = StringUtils.rightPad(cdmClass.getSimpleName(), 30);
				String returnTypeName = StringUtils.rightPad(type.getSimpleName(), 30);
				
				System.out.println(methodName +   "\t\t" + className + "\t\t" + returnTypeName);
//				result_old.add(method);
				result.addAll(getCdmBasesByFieldAndClass(field, cdmClass, value));
			}
			return true;
		}
		
		private Set<Field> getFields(Class clazz){
			Set<Field> result = new HashSet<Field>();
			for (Field field: clazz.getDeclaredFields()){
				if (!Modifier.isStatic(field.getModifiers())){
					result.add(field);	
				}
			}
			Class superclass = clazz.getSuperclass();
			if (CdmBase.class.isAssignableFrom(superclass)){
				result.addAll(getFields(superclass));
			}
			return result;
		}
		
		private Set<CdmBase> getCdmBasesByFieldAndClass(Field field, Class clazz, CdmBase value){
			//FIXME make not dummy but use dao
			Set<CdmBase> result = new HashSet<CdmBase>();
			
			//genericDao.getCdmBasesByFieldAndClass(clazz, field.getName(), value);
			
			
			BotanicalName name = BotanicalName.NewInstance(Rank.GENUS());
			name.setTitleCache("A dummy name");
			result.add(name);
			ReferenceBase ref = Book.NewInstance();
			ref.setTitleCache("A dummy book");
			result.add(ref);
			
			return result;
		}
	private Set<Class<? extends CdmBase>> findAllCdmClasses(){
		
		
		//init
		Set<Class<? extends CdmBase>> allCdmClasses = new HashSet<Class<? extends CdmBase>>();
		allCdmClasses.add(TaxonBase.class);
		allCdmClasses.add(BotanicalName.class);
		
		int count;
		do{
			count = allCdmClasses.size();
			Set<Class<? extends CdmBase>> iteratorSet = new HashSet<Class<? extends CdmBase>>();
			iteratorSet.addAll(allCdmClasses);
			for (Class<? extends CdmBase> cdmClass : iteratorSet){
				Method[] methods = cdmClass.getMethods();
				for (Method method: methods){
					Class<?> returnType = method.getReturnType();
					handleClass(allCdmClasses,returnType);
					Class<?>[] params = method.getParameterTypes();
					for (Class paramClass : params){
						handleClass(allCdmClasses, paramClass);
					}
				}	
			}
		}while (allCdmClasses.size() > count);
		boolean withAbstract = false;
		if (! withAbstract){
			Iterator<Class<? extends CdmBase>> iterator = allCdmClasses.iterator();
			while (iterator.hasNext()){
				Class clazz = iterator.next();
				if (Modifier.isAbstract(clazz.getModifiers())){
					iterator.remove();
				}
			}
		}
		return allCdmClasses;
	}
	
	private void handleClass(Set<Class<? extends CdmBase>> allCdmClasses, Class returnType){
		if (CdmBase.class.isAssignableFrom(returnType)){
			if (! allCdmClasses.contains(returnType)){
				//System.out.println(returnType.getSimpleName());
				allCdmClasses.add((Class)returnType);
				Class superClass = returnType.getSuperclass();
				handleClass(allCdmClasses, superClass);
			}
		}
	}

}
