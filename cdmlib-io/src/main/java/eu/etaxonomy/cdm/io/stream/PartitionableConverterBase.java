/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/
package eu.etaxonomy.cdm.io.stream;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.etaxonomy.cdm.io.common.events.IIoEvent;
import eu.etaxonomy.cdm.io.common.events.IIoObserver;
import eu.etaxonomy.cdm.io.common.events.IoProblemEvent;
import eu.etaxonomy.cdm.io.dwca.in.DwcaDataImportConfiguratorBase;
import eu.etaxonomy.cdm.io.stream.terms.TermUri;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.description.TaxonDescription;
import eu.etaxonomy.cdm.model.taxon.Taxon;
import eu.etaxonomy.cdm.model.taxon.TaxonBase;

/**
 * @author a.mueller
 * @since 23.11.2011
 *
 * FIXME URI
 */
public abstract class PartitionableConverterBase<CONFIG extends DwcaDataImportConfiguratorBase, STATE extends StreamImportStateBase<CONFIG,StreamImportBase>>
      implements IPartitionableConverter<StreamItem, IReader<CdmBase>, String> {

    private static final Logger logger = LogManager.getLogger();

	protected STATE state;
	protected CONFIG config;  //for convenience only (must always be same as state.getConfig())

	public PartitionableConverterBase(STATE state) {
		super();
		this.state = state;
		this.config = state.getConfig();  //for fast access
	}

	protected void fireWarningEvent(String message, StreamItem item, Integer severity) {
		fireWarningEvent(message, getDataLocation(item), severity, 1);
	}

	private String getDataLocation(StreamItem item) {
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
		String className = stackTrace[stackDepth].getClassName();
		Class<?> declaringClass;
		try {
			declaringClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			declaringClass = this.getClass();
		}

		IoProblemEvent event = IoProblemEvent.NewInstance(declaringClass, message, dataLocation,
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
	protected String getValue(StreamItem item, TermUri term) {
		return item.get(term.getUriString());
	}

	/**
	 * Checks if the given term has a value in item that is not blank (null, empty or whitespace only).
	 * @return true if value is not blank
	 */
	protected boolean exists(TermUri term, StreamItem item) {
		return ! StringUtils.isBlank(getValue(item, term));
	}

    /*
     * To be implemented by subclasses if needed.
     */
	@Override
    public ItemFilter<StreamItem> getItemFilter(){
	    return null;
	}

	@Override
    public Map<String, Set<String>> getPartitionForeignKeys(IReader<StreamItem> instream) {
		Map<String, Set<String>> result = new HashMap<>();

		while (instream.hasNext()){
			StreamItem next = instream.read();
			makeForeignKeysForItem(next, result);
		}
		return result;
	}

	/**
	 * Fills the the foreign key map with foreign keys required for this item.
	 * @param next
	 * @param foreignKeyMap
	 */
	protected abstract void makeForeignKeysForItem(StreamItem next, Map<String, Set<String>> foreignKeyMap);

	/**
	 * False if string is null, empty or whitespace only. True otherwise.
	 * @param string String to test.
	 */
	protected boolean hasValue(String string) {
		return StringUtils.isNotBlank(string);
	}

	/**
	 * Returns the key set for a given key or creates a new one.
	 */
	protected Set<String> getKeySet(String key, Map<String, Set<String>> fkMap) {
		Set<String> keySet = fkMap.get(key);
		if (keySet == null){
			keySet = new HashSet<>();
			fkMap.put(key, keySet);
		}
		return keySet;
	}

	protected <T extends TaxonBase<?>> T getTaxonBase(String id, StreamItem item, Class<T> clazz, STATE state) {
		if (clazz == null){
			clazz = (Class)TaxonBase.class;
		}
		List<T> taxonList = state.get(TermUri.DWC_TAXON.toString(), id, clazz);
		if (taxonList.size() > 1){
			String message = "Ambigous taxon mapping for id %s. There is more than 1 matching taxon.";
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

	protected TaxonDescription getTaxonDescription(Taxon taxon, boolean isImageGallery) {
		TaxonDescription result = null;
		Set<TaxonDescription> descs = taxon.getDescriptions();
		for (TaxonDescription desc : descs){
			if (desc.isImageGallery() == isImageGallery){
				result = desc;
				break;
			}
		}
		if (result == null){
			result = TaxonDescription.NewInstance(taxon);
			result.setImageGallery(isImageGallery);
		}
		return result;
	}

    protected Language getDcLanguage(StreamItem item, List<MappedCdmBase<? extends CdmBase>> resultList ) {
        String namespace = TermUri.DC_LANGUAGE.toString();
        String languageStr = item.get(TermUri.DC_LANGUAGE);
        if (isBlank(languageStr)){
            return null;
        }

        List<Language> list = state.get(namespace, languageStr, Language.class);
        if (list.isEmpty()){
            //try to find in cdm
            Language lang = Language.getLanguageByIsoCode(languageStr);
            if (lang == null){
                 lang = Language.getLanguageByLabel(languageStr);
            }
            if (lang == null){
                lang = state.getCurrentIO().getLanguage(state, null, languageStr, languageStr, languageStr, null);
            }
            state.putMapping(namespace, languageStr, lang);
            list.add(lang);
//            MappedCdmBase<? extends CdmBase> languageMcb = new MappedCdmBase<>(TermUri.DC_LANGUAGE, languageStr, lang);
//            resultList.add(languageMcb);
        }
        if (list.size() > 1){
            String message = "There is more than 1 cdm entity matching given language '%s'. I take an arbitrary one.";
            fireWarningEvent(String.format(message, languageStr), item, 4);
        }
        Language result = list.iterator().next();
        return result;
    }

	protected boolean isNotBlank(String str){
		return StringUtils.isNotBlank(str);
	}

	protected boolean isBlank(String str){
		return StringUtils.isBlank(str);
	}
}