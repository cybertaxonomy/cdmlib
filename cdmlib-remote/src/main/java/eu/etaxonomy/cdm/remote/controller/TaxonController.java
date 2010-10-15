// $Id$
/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.remote.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import eu.etaxonomy.cdm.api.service.IOccurrenceService;
import eu.etaxonomy.cdm.api.service.ITaxonService;
import eu.etaxonomy.cdm.model.occurrence.SpecimenOrObservationBase;
import eu.etaxonomy.cdm.model.taxon.Synonym;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;
import eu.etaxonomy.cdm.model.taxon.TaxonNode;
import eu.etaxonomy.cdm.persistence.query.OrderHint;
import eu.etaxonomy.cdm.persistence.query.OrderHint.SortOrder;

/**
 * TODO write controller documentation
 * 
 * @author a.kohlbecker
 * @date 20.07.2009
 *
 */
@Controller
@RequestMapping(value = {"/taxon/{uuid}"})
public class TaxonController extends AnnotatableController<TaxonBase, ITaxonService>
{
	public static final Logger logger = Logger.getLogger(TaxonController.class);
	
	@Autowired
	private IOccurrenceService occurrenceService;
	
	protected static final List<String> TAXONNODE_INIT_STRATEGY = Arrays.asList(new String []{
			"taxonNodes"
	});
	
	public TaxonController(){
		super();
		setInitializationStrategy(Arrays.asList(new String[]{"$","name.nomenclaturalReference"}));
	}
	
	/* (non-Javadoc)
	 * @see eu.etaxonomy.cdm.remote.controller.GenericController#setService(eu.etaxonomy.cdm.api.service.IService)
	 */
	@Autowired
	@Override
	public void setService(ITaxonService service) {
		this.service = service;
	}
	

	/**
	 * Get the set of accepted {@link Taxon} entities for a given
	 * {@link TaxonBase} entity identified by the <code>{taxon-uuid}</code>.
	 * <p>
	 * URI: <b>&#x002F;{datasource-name}&#x002F;taxon&#x002F;{taxon-uuid}&#x002F;accepted</b>
	 * 
	 * @param request
	 * @param response
	 * @return a set on a list of {@link Taxon} entities which are initialized
	 *         using the following initialization strategy:
	 *         {@link #DEFAULT_INIT_STRATEGY}
	 * @throws IOException
	 */
	@RequestMapping(value = "accepted", method = RequestMethod.GET)
	public Set<TaxonBase> doGetAccepted(
			@PathVariable("uuid") UUID uuid,
			HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		logger.info("getAccepted() " + request.getServletPath());
		TaxonBase tb = service.load(uuid);
		HashSet<TaxonBase> resultset = new HashSet<TaxonBase>();
		if(tb instanceof Taxon){
			//the taxon already is accepted
			//FIXME take the current view into account once views are implemented!!!
			resultset.add((Taxon)tb);
		} else {
			Synonym syn = (Synonym)tb;
			resultset.addAll(syn.getAcceptedTaxa());
			//TODO init Synonyms
		}
		return resultset;
	}
	
	@RequestMapping(value = "taxonNodes", method = RequestMethod.GET)
	public Set<TaxonNode>  doGetTaxonNodes(
			@PathVariable("uuid") UUID uuid,
			HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		TaxonBase tb = service.load(uuid, TAXONNODE_INIT_STRATEGY);
		if(tb instanceof Taxon){
			return ((Taxon)tb).getTaxonNodes();
		} else {
			HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
			return null;
		}
	}
	
	@RequestMapping(value = "specimensOrObersvations", method = RequestMethod.GET)
	public ModelAndView doListSpecimensOrObersvations(
			@PathVariable("uuid") UUID uuid,
			HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		logger.info("doGetDescriptionElementsByType() - " + request.getServletPath());
		
		ModelAndView mv = new ModelAndView();

		TaxonBase tb = service.load(uuid);
		
		List<OrderHint> orderHints = new ArrayList<OrderHint>();
		orderHints.add(new OrderHint("titleCache", SortOrder.DESCENDING));
		
		if(tb instanceof Taxon){
			List<SpecimenOrObservationBase> specimensOrObersvations = occurrenceService.listByAnyAssociation(
					null, (Taxon)tb, null, 0, orderHints, null);
			mv.addObject(specimensOrObersvations);
		} else {
			HttpStatusMessage.UUID_REFERENCES_WRONG_TYPE.send(response);
			return null;
		}
		
		return mv;
	}
		
}