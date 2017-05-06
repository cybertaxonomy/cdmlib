/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.taxon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

import eu.etaxonomy.cdm.model.agent.Person;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.name.Rank;
import eu.etaxonomy.cdm.model.name.TaxonNameBase;
import eu.etaxonomy.cdm.model.name.TaxonNameFactory;
//import eu.etaxonomy.cdm.model.reference.Book;
//import eu.etaxonomy.cdm.model.reference.Journal;
import eu.etaxonomy.cdm.model.reference.Reference;
import eu.etaxonomy.cdm.model.reference.ReferenceFactory;

/**
 * @author a.mueller
 * @created 01.04.2009
 * @version 1.0
 */
public class ClassificationTest {
	private static final Logger logger = Logger.getLogger(ClassificationTest.class);

	private static String treeName1;
	private static Classification classification1;
	private static TaxonNode taxonNode1;
	private static TaxonNode taxonNode2;
	private static TaxonNode taxonNode3;
	private static TaxonNode taxonNode12;
	private static TaxonNode taxonNode121;
	private static Taxon taxon1;
	private static Taxon taxon2;
	private static Taxon taxon3;
	private static Taxon taxon12;
	private static Taxon taxon121;
	private static TaxonNameBase<?,?> taxonName1;
	private static TaxonNameBase<?,?> taxonName2;
	private static TaxonNameBase<?,?> taxonName3;
	private static TaxonNameBase<?,?> taxonName12;
	private static TaxonNameBase<?,?> taxonName121;
	private static Reference ref1;
	private static Reference ref2;
	private static Reference ref3;
	//private ReferenceFactory refFactory;



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
		treeName1 = "Greuther, 1993";
		//refFactory = ReferenceFactory.newInstance();
		classification1 = Classification.NewInstance(treeName1);
		taxonName12 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		taxonName121 = TaxonNameFactory.NewBotanicalInstance(Rank.SUBSPECIES());
		taxonName1 = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
		taxonName2 = TaxonNameFactory.NewZoologicalInstance(Rank.GENUS());
		taxonName3 = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		ref1 = ReferenceFactory.newJournal();
		ref2 = ReferenceFactory.newJournal();
		ref3 = ReferenceFactory.newJournal();
		taxon1 = Taxon.NewInstance(taxonName1, ref1);
		taxon2 = Taxon.NewInstance(taxonName2, ref2);
		taxon3 = Taxon.NewInstance(taxonName3, ref3);
		taxon12 = Taxon.NewInstance(taxonName12, ref3);
		taxon121 = Taxon.NewInstance(taxonName121, ref3);

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
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Classification#addRoot(eu.etaxonomy.cdm.model.taxon.Taxon, eu.etaxonomy.cdm.model.reference.Reference, java.lang.String, eu.etaxonomy.cdm.model.taxon.Synonym)}.
	 */
	@Test
	public void testAddRoot() {
		TaxonNameBase<?,?> synonymName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		Synonym synonym = Synonym.NewInstance(synonymName, ref1);
		TaxonNode taxonNode1 = classification1.addChildTaxon(taxon1, null, null);
		taxonNode1.setSynonymToBeUsed(synonym);



		//test root node
		List<TaxonNode> rootNodes = classification1.getChildNodes();
		assertFalse("List of root nodes should not be empty", rootNodes.isEmpty());
		assertEquals("Number of root nodes should be 1", 1, rootNodes.size());
		TaxonNode root = rootNodes.iterator().next();
		assertEquals(taxon1, root.getTaxon());
		assertSame(taxonNode1, root);
		assertNull(root.getReference());
		assertNull(root.getMicroReference());
		assertEquals(synonym, root.getSynonymToBeUsed());

		//any node
		List<TaxonNode> allNodes = classification1.getChildNodes();
		assertFalse("List of root nodes should not be empty", allNodes.isEmpty());
		assertEquals("Number of root nodes should be 1", 1, allNodes.size());
		TaxonNode anyNode = allNodes.iterator().next();
		assertSame("Taxon for TaxonNode should be the same added to the view", taxon1, anyNode.getTaxon());
		assertSame("TaxonNode should be the same added to the view", taxonNode1, anyNode);

	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Classification#isTaxonInView(eu.etaxonomy.cdm.model.taxon.Taxon)}.
	 */
	@Test
	public void testIsTaxonInTree() {
		classification1.addChildTaxon(taxon1, null, null);

		assertTrue(classification1.isTaxonInTree(taxon1));
		Taxon anyTaxon = Taxon.NewInstance(null, null);
		assertFalse(classification1.isTaxonInTree(anyTaxon));
	}


	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Classification#makeRootChildOfOtherNode(eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.model.taxon.TaxonNode, eu.etaxonomy.cdm.model.reference.Reference, java.util.String)}.
	 */
	@Test
	public void testMakeRootChildOfOtherNode() {
		TaxonNode root1 = classification1.addChildTaxon(taxon1, null, null);
		TaxonNode root2 = classification1.addChildTaxon(taxon2, null, null);
		Taxon taxon3 = Taxon.NewInstance(null, null);
		root2.addChildTaxon(taxon3, null, null);
		String microRef = "p55";

		assertFalse("Root1 must not yet be child of root 2", root2.getChildNodes().contains(root1));
		assertNotSame("Root2 must not yet be parent of root 1", root2, root1.getParent());
		assertEquals("view must contain 3 nodes", 3, classification1.getAllNodes().size());
		assertEquals("view must still contain 2 root", 2, classification1.getChildNodes().size());
		assertEquals("root2 must have 1 child", 1, root2.getChildNodes().size());

		classification1.makeTopmostNodeChildOfOtherNode(root1, root2, ref1, microRef);
		assertTrue("Root1 must be child of root 2", root2.getChildNodes().contains(root1));
		assertSame("Root2 must be parent of root 1", root2, root1.getParent());
		assertEquals("view must contain 3 nodes", 3, classification1.getAllNodes().size());
		assertEquals("view must contain 1 root", 1, classification1.getChildNodes().size());
		assertEquals("new child node must have the expected reference for parent child relationship", ref1, root1.getReference());
		assertEquals("new child node must have the expected micro reference for parent child relationship", microRef, root1.getMicroReference());
		assertEquals("root2 must have 2 children", 2, root2.getChildNodes().size());

	}

	@Test
	public void testIsTopmostInTree() {
		TaxonNode root = classification1.addChildTaxon(taxon1, null, null);

		assertTrue(classification1.isTaxonInTree(taxon1));
		assertTrue(classification1.isTopmostInTree(taxon1));
		Taxon anyTaxon = Taxon.NewInstance(null, null);
		assertFalse(classification1.isTaxonInTree(anyTaxon));
		assertFalse(classification1.isTopmostInTree(anyTaxon));
		Taxon child = Taxon.NewInstance(null, null);
		root.addChildTaxon(child, null, null);
		assertTrue(classification1.isTaxonInTree(child));
		assertFalse(classification1.isTopmostInTree(child));
	}

	@Test
	public void testGetTopmostNode() {
		TaxonNode root = classification1.addChildTaxon(taxon1, null, null);

		assertEquals(root, classification1.getTopmostNode(taxon1));
		Taxon anyTaxon = Taxon.NewInstance(null, null);
		assertFalse(classification1.isTaxonInTree(anyTaxon));
		assertNull(classification1.getTopmostNode(anyTaxon));
		Taxon child = Taxon.NewInstance(null, null);
		root.addChildTaxon(child, null, null);
		assertTrue(classification1.isTaxonInTree(child));
		assertNull(classification1.getTopmostNode(child));
	}

	@Test
	public void testAddParentChild() {

		TaxonNameBase<?,?> synonymName = TaxonNameFactory.NewBotanicalInstance(Rank.SPECIES());
		Synonym synonym = Synonym.NewInstance(synonymName, ref1);
		TaxonNode rootNode = classification1.addChildTaxon(taxon1, null, null);
		rootNode.setSynonymToBeUsed(synonym);
		Assert.assertEquals(0,rootNode.getChildNodes().size());

		//add child to existing root
		classification1.addParentChild(taxon1, taxon2, ref1, "Micro1");
		Assert.assertTrue(classification1.isTaxonInTree(taxon2));
		Assert.assertFalse(classification1.isTopmostInTree(taxon2));
		Assert.assertEquals(1,rootNode.getChildNodes().size());
		TaxonNode childNode = rootNode.getChildNodes().iterator().next();
		Assert.assertEquals(taxon2, childNode.getTaxon());

		//relationship already exists
		classification1.addParentChild(taxon1, taxon2, ref2, "Micro2");
		Assert.assertTrue(classification1.isTaxonInTree(taxon2));
		Assert.assertFalse(classification1.isTopmostInTree(taxon2));
		Assert.assertEquals(2, classification1.getAllNodes().size());
		Assert.assertEquals(1,rootNode.getChildNodes().size());
		childNode = rootNode.getChildNodes().iterator().next();
		Assert.assertEquals(taxon2, childNode.getTaxon());
		Assert.assertEquals(ref2, childNode.getReference());
		Assert.assertEquals("Micro2", childNode.getMicroReference());

		logger.info("testAddParentChild not yet fully implemented");

	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Classification#generateTitle()}.
	 */
	@Test
	public void testGenerateTitle() {
		Classification taxonomicViewLocal = Classification.NewInstance(treeName1);
		//Maybe changed if title cache is generated in a different way
		assertEquals(treeName1, taxonomicViewLocal.getTitleCache());
	}

	/**
	 * Test method for {@link eu.etaxonomy.cdm.model.taxon.Classification#generateTitle()}.
	 */
	@Test
	public void play() {

			CdmBase referencedCdmBase = Person.NewInstance();
			Set<Class<? extends CdmBase>> allCdmClasses = findAllCdmClasses();

			Class<? extends CdmBase> referencedClass = referencedCdmBase.getClass();
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

	private boolean handleSingleClass(Class<?> classToBeSearchedFor, Class<?> type, Field field, Class<?> cdmClass, Set<CdmBase> result, CdmBase value){
		if (! Modifier.isStatic(field.getModifiers())){
			String methodName = StringUtils.rightPad(field.getName(), 30);
			String className = StringUtils.rightPad(cdmClass.getSimpleName(), 30);
			String returnTypeName = StringUtils.rightPad(type.getSimpleName(), 30);

			System.out.println(methodName +   "\t\t" + className + "\t\t" + returnTypeName);
//			result_old.add(method);
			result.addAll(getCdmBasesByFieldAndClass(field, cdmClass, value));
		}
		return true;
	}

	private Set<Field> getFields(Class<?> clazz){
		Set<Field> result = new HashSet<Field>();
		for (Field field: clazz.getDeclaredFields()){
			if (!Modifier.isStatic(field.getModifiers())){
				result.add(field);
			}
		}
		Class<?> superclass = clazz.getSuperclass();
		if (CdmBase.class.isAssignableFrom(superclass)){
			result.addAll(getFields(superclass));
		}
		return result;
	}

	private Set<CdmBase> getCdmBasesByFieldAndClass(Field field, Class<?> clazz, CdmBase value){
		//FIXME make not dummy but use dao
		Set<CdmBase> result = new HashSet<CdmBase>();

		//genericDao.getCdmBasesByFieldAndClass(clazz, field.getName(), value);


		TaxonNameBase<?,?> name = TaxonNameFactory.NewBotanicalInstance(Rank.GENUS());
		name.setTitleCache("A dummy name", true);
		result.add(name);
		Reference ref = ReferenceFactory.newBook();
		ref.setTitleCache("A dummy book", true);
		result.add(ref);

		return result;
	}

	private Set<Class<? extends CdmBase>> findAllCdmClasses(){


		//init
		Set<Class<? extends CdmBase>> allCdmClasses = new HashSet<>();
		allCdmClasses.add(TaxonBase.class);
		allCdmClasses.add(TaxonNameBase.class);

		int count;
		do{
			count = allCdmClasses.size();
			Set<Class<? extends CdmBase>> iteratorSet = new HashSet<>();
			iteratorSet.addAll(allCdmClasses);
			for (Class<? extends CdmBase> cdmClass : iteratorSet){
				Method[] methods = cdmClass.getMethods();
				for (Method method: methods){
					Class<?> returnType = method.getReturnType();
					handleClass(allCdmClasses,returnType);
					Class<?>[] params = method.getParameterTypes();
					for (Class<?> paramClass : params){
						handleClass(allCdmClasses, paramClass);
					}
				}
			}
		}
		while (allCdmClasses.size() > count);
		boolean withAbstract = false;
		if (! withAbstract){
			Iterator<Class<? extends CdmBase>> iterator = allCdmClasses.iterator();
			while (iterator.hasNext()){
				Class<?> clazz = iterator.next();
				if (Modifier.isAbstract(clazz.getModifiers())){
					iterator.remove();
				}
			}
		}
		return allCdmClasses;
	}

	private void handleClass(Set<Class<? extends CdmBase>> allCdmClasses, Class<?> returnType){
		if (CdmBase.class.isAssignableFrom(returnType)){
			if (! allCdmClasses.contains(returnType)){
				//System.out.println(returnType.getSimpleName());
				allCdmClasses.add((Class)returnType);
				Class<?> superClass = returnType.getSuperclass();
				handleClass(allCdmClasses, superClass);
			}
		}
	}

	@Test
	public void testCloneClassification(){

		taxonNode1 = classification1.addChildTaxon(taxon1, null, null);
		taxonName1.setTitleCache("name1", true);
		taxonName12.setTitleCache("name12", true);
		taxonName121.setTitleCache("name121", true);
		taxonName2.setTitleCache("name2", true);
		taxonName3.setTitleCache("name3", true);

		taxonNode12 = taxonNode1.addChildTaxon(taxon12, null, null);
		taxonNode121 = taxonNode12.addChildTaxon(taxon121, null, null);
		taxonNode2 = classification1.addChildTaxon(taxon2, null, null);
		taxonNode2.addChildTaxon(taxon3, null, null);
		Classification clone = (Classification)classification1.clone();
		assertEquals(classification1.getAllNodes().size(), clone.getAllNodes().size());
		TaxonNode cloneNode = clone.getNode(taxon1);
		assertNotSame(cloneNode, taxonNode1);
	}

    @Test
    public void beanTests(){
//	    #5307 Test that BeanUtils does not fail
        BeanUtils.getPropertyDescriptors(Classification.class);
    }

}
