// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.xper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.api.application.CdmApplicationUtils;
import fr_jussieu_snv_lis.base.BaseObject;
import fr_jussieu_snv_lis.base.BaseObjectResource;
import fr_jussieu_snv_lis.base.Group;
import fr_jussieu_snv_lis.base.IBase;
import fr_jussieu_snv_lis.base.Individual;
import fr_jussieu_snv_lis.base.IndividualGroupNode;
import fr_jussieu_snv_lis.base.IndividualGroupTree;
import fr_jussieu_snv_lis.base.IndividualNode;
import fr_jussieu_snv_lis.base.IndividualTree;
import fr_jussieu_snv_lis.base.Variable;
import fr_jussieu_snv_lis.base.XPResource;

/**
 * @author a.mueller
 * @date 12.04.2011
 *
 */
public class BaseCdm implements IBase {
	private static final Logger logger = Logger.getLogger(BaseCdm.class);
	private CdmXperBaseControler baseControler;
	
	//temporary store
	private List<Variable> cdmVariables; // A set of Variable
	private List<Individual> cdmIndividuals;
	private IndividualTree cdmIndividualTree;
	
	//FIXME mapping for groups not yet decided
	private List<Group> groups = new ArrayList<Group>();


	protected void setBaseControler(CdmXperBaseControler cdmXperBaseControler) {
		this.baseControler = cdmXperBaseControler; 
	}

	protected CdmXperBaseControler getBaseControler() {
		return baseControler;
	}
	
	private CdmXperAdapter getAdapter(){
		return getBaseControler().getAdapter();
	}
	
//******************* IBase *****************************************************/	
	
	
	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getGroups()
	 */
	@Override
	public List<Group> getGroups() {
		logger.warn("getGroups Not yet implemented (groups not yet implemented)");
		return groups;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getGroupAt(int)
	 */
	@Override
	public Group getGroupAt(int i) {
		logger.warn("getGroupAt Not yet implemented (groups not yet implemented)");
		return null;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#setGroups(java.util.List)
	 */
	@Override
	public void setGroups(List<Group> n) {
		logger.warn("setGroups Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#addGroup(fr_jussieu_snv_lis.base.Group)
	 */
	@Override
	public boolean addGroup(Group obj) {
		logger.warn("addGroup Not yet implemented");
		return false;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#addGroupAt(int, fr_jussieu_snv_lis.base.Group)
	 */
	@Override
	public void addGroupAt(int i, Group group) {
		logger.warn("addGroupAt Not yet implemented");

	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#deleteGroup(fr_jussieu_snv_lis.base.Group)
	 */
	@Override
	public boolean deleteGroup(Group group) {
		logger.warn("deleteGroup Not yet implemented");
		return false;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getVariables()
	 */
	@Override
	public List<Variable> getVariables() {
		if (cdmVariables == null){
			cdmVariables = new ArrayList<Variable>();
			getBaseControler().loadFeatures();
		}
		return cdmVariables;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getVariableAt(int)
	 */
	@Override
	public Variable getVariableAt(int i) {
		try {
			return getVariables().get(i);
		} catch (IndexOutOfBoundsException ioobe) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#setVariables(java.util.List)
	 */
	@Override
	public void setVariables(List<Variable> al) {
		logger.warn("setVariables Not yet implemented");

	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#deleteVariable(fr_jussieu_snv_lis.base.Variable)
	 */
	@Override
	public boolean deleteVariable(Variable variable) {
		logger.warn("deleteVariable should be handled by controler");
		if (getVariables().remove(variable)) {
			BaseObject.checkIndex(getVariables());
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#addVariable(fr_jussieu_snv_lis.base.Variable)
	 */
	@Override
	public boolean addVariable(Variable variable) {
		getVariables();
		if (!getVariables().contains(variable)) {
			if (getVariables().add(variable)) {
				// int i = variables.lastIndexOf(variable);
				// ((BaseObject) variable).setIndex("" + (i + 1)); // index ++
				BaseObject.checkIndex(getVariables());
				return true;
			}

			return false;
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#addVariableAt(fr_jussieu_snv_lis.base.Variable, int)
	 */
	@Override
	public void addVariableAt(Variable variable, int i) {
		logger.warn("addVariableAt Not yet implemented");

	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#addIndividual(fr_jussieu_snv_lis.base.Individual)
	 */
	@Override
	public boolean addIndividual(Individual individual) {
		if (!this.getIndividuals().contains(individual)) {
			if (this.getIndividuals().add(individual)) {
				// int index = individuals.lastIndexOf(obj);
				// ((BaseObject) obj).setIndex("" + (index + 1));
				BaseObject.checkIndex(this.getIndividuals());
				return true;
			}

			return false;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#addIndividualAt(int, fr_jussieu_snv_lis.base.Individual)
	 */
	@Override
	public boolean addIndividualAt(int i, Individual ind) {
		logger.warn("addIndividualAt Not yet implemented");
		return false;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#deleteIndividual(fr_jussieu_snv_lis.base.Individual)
	 */
	@Override
	public boolean deleteIndividual(Individual obj) {
		logger.warn("deleteIndividual Not yet implemented");
		return false;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getIndividuals()
	 */
	@Override
	public List<Individual> getIndividuals() {
		if (cdmIndividuals == null){
			cdmIndividuals = new ArrayList<Individual>();
			getAdapter().loadTaxa();
		}
		return cdmIndividuals;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getIndividualAt(int)
	 */
	@Override
	public Individual getIndividualAt(int i) {
		return this.cdmIndividuals.get(i);
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#setIndividualList(java.util.List)
	 */
	@Override
	public void setIndividualList(List<Individual> n) {
		logger.warn("setIndividualList Not yet implemented");

	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getAuthors()
	 */
	@Override
	public Set<String> getAuthors() {
		logger.warn("getAuthors Not yet implemented");
		return null;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getAuthorAt(int)
	 */
	@Override
	public String getAuthorAt(int i) {
		logger.warn("getAuthorAt Not yet implemented");
		return null;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#setAuthors(java.lang.Object[])
	 */
	@Override
	public void setAuthors(Object[] list) {
		logger.warn("setAuthors Not yet implemented");

	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#addAuthor(java.lang.String)
	 */
	@Override
	public boolean addAuthor(String s) {
		logger.warn("addAuthor Not yet implemented");
		return false;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#deleteAuthor(java.lang.String)
	 */
	@Override
	public boolean deleteAuthor(String s) {
		logger.warn("deleteAuthor Not yet implemented");
		return false;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getName()
	 */
	@Override
	public String getName() {
		String result = this.getAdapter().getWorkingSet().getLabel();
		result = StringUtils.isBlank(result)? "<no title>": result;
		return result;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#setName(java.lang.String)
	 */
	@Override
	public void setName(String n) {
		logger.warn("setName Not yet implemented");

	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getPathName()
	 */
	@Override
	public String getPathName() {
		File directory;
		try {
			directory = CdmApplicationUtils.getWritableResourceDir();
		} catch (IOException e) {
			String message = "Application directory could not be found.";
			throw new RuntimeException(message);
		}
		String result = directory.getAbsolutePath() + File.separator + "xper" + File.separator;
		logger.info("pathname is " + result);
		return result;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#setPathName(java.lang.String)
	 */
	@Override
	public void setPathName(String n) {
		logger.warn("setPathName Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getShortname()
	 */
	@Override
	public String getShortname() {
		logger.warn("getShortname Not yet implemented");
		return "";
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#setShortname(java.lang.String)
	 */
	@Override
	public void setShortname(String string) {
		logger.warn("setShortname Not yet implemented");

	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getDescription()
	 */
	@Override
	public String getDescription() {
		logger.warn("getDescription Not yet implemented");
		return "";
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String string) {
		logger.warn("setDescription Not yet implemented");

	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#setLastEdition(java.lang.String)
	 */
	@Override
	public void setLastEdition(String s) {
		logger.warn("setLastEdition Not yet implemented");

	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getLastEdition()
	 */
	@Override
	public String getLastEdition() {
		logger.warn("getLastEdition Not yet implemented");
		return "";
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#setFirstEdition(java.lang.String)
	 */
	@Override
	public void setFirstEdition(String s) {
		logger.warn("setFirstEdition Not yet implemented");

	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getFirstEdition()
	 */
	@Override
	public String getFirstEdition() {
		logger.warn("getFirstEdition Not yet implemented");
		return "";
	}

	/***** Base management operations *****/
	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getNbVariables()
	 */
	@Override
	public int getNbVariables() {
		return getVariables().size();
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#setNbVariables(int)
	 */
	@Override
	public void setNbVariables(int n) {
		logger.warn("setNbVariables Not yet implemented");

	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getNbIndividuals()
	 */
	@Override
	public int getNbIndividuals() {
		return this.cdmIndividuals.size();
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#setNbIndividuals(int)
	 */
	@Override
	public void setNbIndividuals(int n) {
		logger.warn("setNbIndividuals Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getNbModes()
	 */
	@Override
	public int getNbModes() {
		logger.warn("getNbModes Not yet implemented");
		return 0;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getNbGroups()
	 */
	@Override
	public int getNbGroups() {
		logger.info("getNbGroups Not yet implemented (groups not yet implemented)");
		return groups.size();
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#setNbGroups(int)
	 */
	@Override
	public void setNbGroups(int n) {
		logger.warn("setNbGroups Not yet implemented");

	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getNbAuthors()
	 */
	@Override
	public int getNbAuthors() {
		logger.warn("getNbAuthors Not yet implemented");
		return 0;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#hasGroups()
	 */
	@Override
	public boolean hasGroups() {
		logger.warn("hasGroups Not yet implemented (groups not yet implemented)");
		return ! groups.isEmpty();
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#hasAuthors()
	 */
	@Override
	public boolean hasAuthors() {
		//TODO
		return false;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#hasVariables()
	 */
	@Override
	public boolean hasVariables() {
		logger.warn("hasVariables Not yet implemented");
		return false;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#hasIndividuals()
	 */
	@Override
	public boolean hasIndividuals() {
		logger.warn("hasIndividuals Not yet implemented");
		return false;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#isIllustrated()
	 */
	@Override
	public boolean isIllustrated() {
		logger.warn("isIllustrated Not yet implemented");
		return false;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#isIllustrated(fr_jussieu_snv_lis.base.BaseObject[])
	 */
	@Override
	public boolean isIllustrated(BaseObject[] bo) {
		logger.warn("isIllustrated Not yet implemented");
		return false;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getIllustratedBo(fr_jussieu_snv_lis.base.XPResource)
	 */
	@Override
	public List<BaseObject> getIllustratedBo(XPResource xpr) {
		logger.warn("getIllustratedBo Not yet implemented");
		return null;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getAllResources()
	 */
	@Override
	public List<BaseObjectResource> getAllResources() {
		logger.warn("getAllResources Not yet implemented");
		return null;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getAllResources(java.lang.String)
	 */
	@Override
	public HashSet<Object> getAllResources(String str) {
		logger.warn("getAllResources Not yet implemented");
		return null;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#containsBaseObject(fr_jussieu_snv_lis.base.BaseObject)
	 */
	@Override
	public boolean containsBaseObject(BaseObject bo) {
		logger.warn("containsBaseObject Not yet implemented");
		return false;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getVariablesWithoutGroup()
	 */
	@Override
	public List<Variable> getVariablesWithoutGroup() {
		logger.warn("getVariablesWithoutGroup Not yet implemented");
		return null;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object arg0) {
		logger.warn("compareTo Not yet implemented");
		return 0;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#free()
	 */
	@Override
	public void free() {
		logger.warn("free Not yet implemented");

	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#isComplete()
	 */
	@Override
	public boolean isComplete() {
		logger.warn("isComplete Not yet implemented");
		return false;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getCompletePercentage()
	 */
	@Override
	public float getCompletePercentage() {
		logger.warn("getCompletePercentage Not yet implemented");
		return 0;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#toTabFile()
	 */
	@Override
	public String toTabFile() {
		logger.warn("toTabFile Not yet implemented");
		return null;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#toHtml()
	 */
	@Override
	public String toHtml() {
		logger.warn("toHtml Not yet implemented");
		return null;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getResource()
	 */
	@Override
	public BaseObjectResource getResource() {
		logger.warn("getResource Not yet implemented");
		return null;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#setResource(fr_jussieu_snv_lis.base.BaseObjectResource)
	 */
	@Override
	public void setResource(BaseObjectResource b) {
		logger.warn("setResource Not yet implemented");

	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#removeResource()
	 */
	@Override
	public void removeResource() {
		logger.warn("removeResource Not yet implemented");

	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#hasAnIllustration()
	 */
	@Override
	public boolean hasAnIllustration() {
		logger.warn("hasAnIllustration Not yet implemented");
		return false;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getLanguage()
	 */
	@Override
	public String getLanguage() {
		logger.warn("getLanguage Not yet implemented");
		return "";
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#setLanguage(java.lang.String)
	 */
	@Override
	public void setLanguage(String language) {
		logger.warn("setLanguage Not yet implemented");

	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#setLinks(java.lang.Object[])
	 */
	@Override
	public void setLinks(Object[] objects) {
		logger.warn("setLinks Not yet implemented");

	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getLinks()
	 */
	@Override
	public Set<String> getLinks() {
		logger.warn("getLinks Not yet implemented. Returns empty list");
		return new HashSet<String>();
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#addLinks(java.lang.String)
	 */
	@Override
	public boolean addLinks(String link) {
		logger.warn("addLinks Not yet implemented");
		return false;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#removeLinks(java.lang.String)
	 */
	@Override
	public boolean removeLinks(String link) {
		logger.warn("removeLinks Not yet implemented");
		return false;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getLicense()
	 */
	@Override
	public String getLicense() {
		logger.warn("getLicense Not yet implemented");
		return "";
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#setLicense(java.lang.String)
	 */
	@Override
	public void setLicense(String license) {
		logger.warn("setLicense Not yet implemented");

	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getNbLinks()
	 */
	@Override
	public int getNbLinks() {
		logger.warn("getNbLinks Not yet implemented");
		return 0;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getUnknownData()
	 */
	@Override
	public Map<Individual, Variable> getUnknownData() {
		logger.warn("getUnknownData Not yet implemented");
		return null;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getEmptyData()
	 */
	@Override
	public Map<Individual, Variable> getEmptyData() {
		logger.warn("getEmptyData Not yet implemented");
		return null;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#getHelp()
	 */
	@Override
	public String getHelp() {
		logger.warn("getHelp Not yet implemented");
		return "";
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#setHelp(java.lang.String)
	 */
	@Override
	public void setHelp(String help) {
		logger.warn("setHelp Not yet implemented");

	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#isPresentNumVariable()
	 */
	@Override
	public boolean isPresentNumVariable() {
		logger.warn("isPresentNumVariable Not yet implemented");
		return false;
	}

	/* (non-Javadoc)
	 * @see fr_jussieu_snv_lis.base.IBase#addResource(fr_jussieu_snv_lis.base.BaseObject, fr_jussieu_snv_lis.base.BaseObjectResource)
	 */
	@Override
	public boolean addResource(BaseObject bo, BaseObjectResource rsc) {
		logger.warn("addResource(bo, rsc) Not yet implemented");
		return false;
	}

	@Override
	public int getNbIndividualNodes() {
		//preliminary until classification is implemented in a better way for CDM
		return this.cdmIndividualTree.getIndividualNodes().size();
	}

	@Override
	public IndividualNode getIndividualNodeAt(int i) {
		//preliminary until classification is implemented in a better way for CDM
		return this.cdmIndividualTree.getIndividualNodes().get(i);
	}

	@Override
	public boolean addIndividualNode(IndividualNode node) {
		logger.warn ("addIndividualNode not yet implemented (added Xper 2.3)");
		throw new RuntimeException("Individual Node can't be added in CDM version of Xper. Editing taxonomy is not allowed.");
	}

	@Override
	public IndividualTree getIndividualtree() {
		//preliminary until classification is implemented in a better way for CDM
		return this.cdmIndividualTree;
	}

	@Override
	public void setIndividualTree(IndividualTree individualTree) {
		//preliminary until classification is implemented in a better way for CDM
		this.cdmIndividualTree = individualTree;
	}

	@Override
	public int getNbIndividualNodes(IndividualNode idNode) {
		//preliminary until classification is implemented in a better way for CDM
		return this.cdmIndividualTree.getNbIndividualNodes(idNode);
	}

	@Override
	public boolean deleteIndividualNode(IndividualNode individualNode) {
		throw new RuntimeException("Individual node can't be deleted in CDM version of Xper. Editing taxonomy is not allowed.");
	}

	@Override
	public boolean moveIndividualNode(IndividualNode nodeToMove, IndividualNode nodeTarget) {
		throw new RuntimeException("Individual node can't be moved in CDM version of Xper. Editing taxonomy is not allowed.");
	}

	@Override
	public boolean moveIndividualNodeInIndividualTreeRoot(List<IndividualNode> nodesDaughters) {
		throw new RuntimeException("Individual node can't be moved in CDM version of Xper. Editing taxonomy is not allowed.");
	}

	@Override
	public boolean moveIndividualNodeInIndividualTreeRoot(IndividualNode targerNode) {
		throw new RuntimeException("Individual node can't be moved in CDM version of Xper. Editing taxonomy is not allowed.");
	}

	@Override
	public boolean checkIndNodeByName(String keyNode) {
		logger.warn ("checkIndNodeByName not yet implemented (added Xper 2.3)");
		return false;
	}

	@Override
	public IndividualNode getIndNodeByName(String refTaxonHierarchyNodeTaxoNameIND) {
		logger.warn ("getIndNodeByName not yet implemented (added Xper 2.3)");
		return null;
	}

	@Override
	public boolean checkIfIndividualNodeIsInTree(Individual individual) {
		logger.warn ("checkIfIndividualNodeIsInTree not yet implemented (added Xper 2.3)");
		return false;
	}

	@Override
	public IndividualNode getIndividualNodeByLeaf(Individual individual) {
		logger.warn ("getIndividualNodeByLeaf not yet implemented (added Xper 2.3)");
		return null;
	}

	@Override
	public void setIndividualtreeLabel(String string) {
		logger.warn("setIndividualtreeLabel not yet implemented (added Xper 2.3)");
	}

	@Override
	public void setIndividualtreeHierarchyType(String string) {
		logger.warn ("setIndividualtreeHierarchyType not yet implemented (added Xper 2.3)");
		
	}

	@Override
	public List<IndividualTree> getListIndividualTree() {
		logger.warn ("getListIndividualTree not yet implemented (added Xper 2.3)");
		return null;
	}

	@Override
	public void setListIndividualTree(List<IndividualTree> listOfTree) {
		logger.warn ("setListIndividualTree not yet implemented (added Xper 2.3)");
		
	}

	@Override
	public IndividualGroupNode getIndividualNodeInTreeGroupAt(int i) {
		logger.warn ("getIndividualNodeInTreeGroupAt not yet implemented (added Xper 2.3)");
		return null;
	}

	@Override
	public int getNbIndividualNodesGroupInTree() {
		logger.warn ("getNbIndividualNodesGroupInTree not yet implemented (added Xper 2.3)");
		return 0;
	}

	@Override
	public IndividualGroupTree getIndividualGroupTree() {
		logger.warn ("getIndividualGroupTree not yet implemented (added Xper 2.3)");
		return null;
	}

	@Override
	public boolean addIndividualGroupNode(IndividualGroupNode targetNode,IndividualGroupNode node2add) {
		logger.warn ("addIndividualGroupNode not yet implemented (added Xper 2.3)");
		return false;
	}

	@Override
	public boolean addIndGroup(Group newGroup) {
		logger.warn ("addIndGroup not yet implemented (added Xper 2.3)");
		return false;
	}

	@Override
	public boolean deleteIndividualGroupNode(Individual ind) {
		logger.warn ("deleteIndividualGroupNode not yet implemented (added Xper 2.3)");
		return false;
	}

	@Override
	public boolean deleteIndividualGroupNode(Group group) {
		logger.warn ("deleteIndividualGroupNode not yet implemented (added Xper 2.3)");
		return false;
	}

	@Override
	public boolean deleteIndividualGroupNode(IndividualGroupNode node2Delete) {
		logger.warn ("deleteIndividualGroupNode not yet implemented (added Xper 2.3)");
		return false;
	}

	@Override
	public int getNbIndividualGroups() {
		logger.warn ("getNbIndividualGroups not yet implemented (added Xper 2.3)");
		return 0;
	}

	@Override
	public List<Group> getIndividualGroups() {
		logger.warn ("getIndividualGroups not yet implemented (added Xper 2.3)");
		return null;
	}

	@Override
	public Group getIndividualGroupAt(int i) {
		logger.warn ("getIndividualGroupAt not yet implemented (added Xper 2.3)");
		return null;
	}

	@Override
	public boolean addDuplicateIndividualGroupNode(IndividualGroupNode targetNode, IndividualGroupNode node2copy) {
		logger.warn ("addDuplicateIndividualGroupNode not yet implemented (added Xper 2.3)");
		return false;
	}

	@Override
	public boolean removeIndividualGroupNode(IndividualGroupNode mother, IndividualGroupNode node2del) {
		logger.warn ("removeIndividualGroupNode not yet implemented (added Xper 2.3)");
		return false;
	}

	@Override
	public IndividualGroupNode addIndividualGroupNode(Group group) {
		logger.warn ("addIndividualGroupNode not yet implemented (added Xper 2.3)");
		return null;
	}

	@Override
	public boolean moveIndividualGroupNode(String idTargetNode, IndividualGroupNode node2move) {
		logger.warn ("moveIndividualGroupNode not yet implemented (added Xper 2.3)");
		return false;
	}

	@Override
	public IndividualGroupNode getIndividualNodeByGroupId(String idGroupParent) {
		logger.warn ("getIndividualNodeByGroupId not yet implemented (added Xper 2.3)");
		return null;
	}

	@Override
	public void sortIndGrpList() {
		logger.warn ("sortIndGrpList not yet implemented (added Xper 2.3)");
		
	}

	@Override
	public boolean hasIndividualGroup(Individual ind) {
		logger.warn ("hasIndividualGroup not yet implemented (added Xper 2.3)");
		return false;
	}

	@Override
	public List<IndividualGroupNode> getIndividualGroupNodeByLeaf(Individual ind) {
		logger.warn ("getIndividualGroupNodeByLeaf not yet implemented (added Xper 2.3)");
		return null;
	}

	@Override
	public void setIndividualGroupTree(IndividualGroupTree individualGroupTree) {
		logger.warn ("setIndividualGroupTree not yet implemented (added Xper 2.3)");
		
	}

	@Override
	public void setListIndividualGroupTree(List<IndividualGroupTree> listOfIndividualGroupTree) {
		logger.warn ("setListIndividualGroupTree not yet implemented (added Xper 2.3)");
		
	}

	@Override
	public List<IndividualGroupTree> getListIndividualGroupTree() {
		logger.warn ("getListIndividualGroupTree not yet implemented (added Xper 2.3)");
		return null;
	}

	@Override
	public void sortIndividualTree() {
		logger.info("sortIndividualTree not implemented for BaseCdm. Probably not needed (added since Xper 2.3)");
	}

	@Override
	public void sortIndividualGroupTree() {
		logger.info("sortIndividualGroupTree not implemented for BaseCdm. Probably not needed (added since Xper 2.3)");
	}

	@Override
	public void checkTreeIndex() {
		logger.info("checkTreeIndex not implemented for BaseCdm. Probably not needed (added since Xper 2.3)");
	}
	
	
}
