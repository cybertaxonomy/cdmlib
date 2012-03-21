// $Id$
/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.dwca.in;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.IoStateBase;
import eu.etaxonomy.cdm.io.common.events.IIoEvent;
import eu.etaxonomy.cdm.io.common.events.IIoObserver;
import eu.etaxonomy.cdm.io.common.events.IoProblemEvent;
import eu.etaxonomy.cdm.io.dwca.TermUri;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @date 23.11.2011
 *
 */
public abstract class PartitionableConverterBase<STATE extends DwcaImportState> 
		/*implements IPartitionableConverter<CsvStreamItem, IReader<CdmBase>, String> */ {
	
	private static final Logger logger = Logger.getLogger(PartitionableConverterBase.class);

	protected STATE state;
	
	
	protected void fireWarningEvent(String message, CsvStreamItem item, Integer severity) {
		fireWarningEvent(message, getDataLocation(item), severity, 1);
	}
	
	private String getDataLocation(CsvStreamItem item) {
		String location = item.getLocation();
		return location;
	}
	
	protected void fireWarningEvent(String message, String dataLocation, Integer severity) {
		fireWarningEvent(message, dataLocation, severity, 1);
	}
	
	protected void fireWarningEvent(String message, String dataLocation, Integer severity, int stackDepth) {
		stackDepth++;
		StackTraceElement[] stackTrace = new Exception().getStackTrace();
		int lineNumber = stackTrace[stackDepth].getLineNumber();
		String methodName = stackTrace[stackDepth].getMethodName();

		IoProblemEvent event = IoProblemEvent.NewInstance(this.getClass(), message, dataLocation, 
				lineNumber, severity, methodName);
		
		//for performance improvement one may read:
		//http://stackoverflow.com/questions/421280/in-java-how-do-i-find-the-caller-of-a-method-using-stacktrace-or-reflection
//		Object o = new SecurityManager().getSecurityContext();
		
		fire(event);
	}
	
	protected void fire(IIoEvent event){
		Set<IIoObserver> observers = state.getConfig().getObservers();
		for (IIoObserver observer: observers){
			observer.handleEvent(event);
		}
		if (observers.size() == 0){
			logger.warn(event.getMessage() +  " (no observer for message!).");
		}
	}
	


	/**
	 * Returns the value for the given term in the item.
	 */
	protected String getValue(CsvStreamItem item, TermUri term) {
		return item.get(term.getUriString());
	}
	
	/**
	 * Checks if the given term has a value in item that is not blank (null, empty or whitespace only). 
	 * @param term
	 * @param item
	 * @return true if value is not blank
	 */
	protected boolean exists(TermUri term, CsvStreamItem item) {
		return ! StringUtils.isBlank(getValue(item, term));
	}
	

	
	public Map<String, Set<String>> getPartitionForeignKeys(IReader<CsvStreamItem> instream) {
		Map<String, Set<String>> result = new HashMap<String, Set<String>>();
		
		while (instream.hasNext()){
			CsvStreamItem next = instream.read();
			makeForeignKeysForItem(next, result);
		}
		return result;
	}

	protected abstract void makeForeignKeysForItem(CsvStreamItem next, Map<String, Set<String>> result);

	
	protected boolean hasValue(String string) {
		return StringUtils.isNotBlank(string);
	}
	

	protected Set<String> getKeySet(String key, Map<String, Set<String>> fkMap) {
		Set<String> keySet = fkMap.get(key);
		if (keySet == null){
			keySet = new HashSet<String>();
			fkMap.put(key, keySet);
		}
		return keySet;
	}
	

	protected <T extends TaxonBase> T getTaxonBase(String id, CsvStreamItem item, Class<T> clazz, STATE state) {
		if (clazz == null){
			clazz = (Class)TaxonBase.class;
		}
		List<T> taxonList = state.get(TermUri.DWC_TAXON.toString(), id, clazz);
		if (taxonList.size() > 1){
			String message = "Undefined taxon mapping for id %s.";
			message = String.format(message, id);
			fireWarningEvent(message, item, 8);
			logger.warn(message);  //TODO remove when events are handled correctly
			return null;
		}else if (taxonList.isEmpty()){
			return null;
		}else{
			return taxonList.get(0);
		}
	}
	
}
