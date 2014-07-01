/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.persistence.dao.taxon.tmp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.etaxonomy.cdm.model.description.PresenceAbsenceTermBase;
import eu.etaxonomy.cdm.model.location.NamedArea;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.dao.taxon.tmp.LogicFilter.Op;

/**
 * 
 * Preliminary.
 * @author a.mueller
 *
 */
public class TaxonNodeFilter {
	
	List<LogicFilter<TaxonNode>> taxonNodes = new ArrayList<LogicFilter<TaxonNode>>();
	
	List<LogicFilter<NamedArea>> areaFilter = new ArrayList<LogicFilter<NamedArea>>();

	@SuppressWarnings("rawtypes")
	List<LogicFilter<PresenceAbsenceTermBase>> distributionStatusFilter = new ArrayList<LogicFilter<PresenceAbsenceTermBase>>();
	

	public void reset(){
		taxonNodes = new ArrayList<LogicFilter<TaxonNode>>();
		
		resetArea();
		
		resetDistributionStatus();
	}

	@SuppressWarnings("rawtypes")
	private void resetDistributionStatus() {
		distributionStatusFilter = new ArrayList<LogicFilter<PresenceAbsenceTermBase>>();
	}

	private void resetArea() {
		areaFilter = new ArrayList<LogicFilter<NamedArea>>();
	}
	
	public TaxonNodeFilter(TaxonNode node){
		reset();
		LogicFilter<TaxonNode> filter = new LogicFilter<TaxonNode>(node);
		taxonNodes.add(filter);
	}
	
	public List<LogicFilter<TaxonNode>>getTaxonNodesFilter(){
		return Collections.unmodifiableList(taxonNodes);
	}
	
	public List<LogicFilter<NamedArea>>getAreaFilter(){
		return Collections.unmodifiableList(areaFilter);
	}
	
	@SuppressWarnings("rawtypes")
	public List<LogicFilter<PresenceAbsenceTermBase>>getDistributionStatusFilter(){
		return Collections.unmodifiableList(distributionStatusFilter);
	}
	
	public TaxonNodeFilter or(TaxonNode taxonNode){
		taxonNodes.add( new LogicFilter<TaxonNode>(taxonNode, Op.OR));
		return this;
	}
	
	public TaxonNodeFilter not(TaxonNode taxonNode){
		taxonNodes.add( new LogicFilter<TaxonNode>(taxonNode, Op.NOT));
		return this;
	}
	
	public TaxonNodeFilter set(NamedArea area){
		resetArea();
		areaFilter.add( new LogicFilter<NamedArea>(area, Op.AND));
		return this;
	}

	
}
