package eu.etaxonomy.cdm.remote.l10n;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.etaxonomy.cdm.api.service.ITermService;
import eu.etaxonomy.cdm.model.common.Language;

@Component
public class LocaleContext {
	
	protected static ThreadLocal<Vector<Locale>> localesHolder = new ThreadLocal<Vector<Locale>>(){

		@Override
		protected Vector<Locale> initialValue() {
			return new Vector<Locale>();
		}
		
	};
	
	protected static Hashtable<String, List<Language>> languageMap = new Hashtable<String, List<Language>>(); 
	
	private static ITermService termService;
	
	@Autowired
	public void setTermService(ITermService termService) {
		this.termService = termService;
	}

	public static void setLocales(Enumeration<Locale> locales) {
		Vector<Locale> l = new Vector<Locale>();
		while(locales.hasMoreElements()){
			l.add(locales.nextElement());
		}
		LocaleContext.localesHolder.set(l);
		mapToLanguages(l);
	}
	
	public static Enumeration<Locale> getLocales(){
		return localesHolder.get().elements();
	}
	
	/**
	 * 
	 * @return
	 */
	public static List<Language> getLanguages(){
		String localesKey = composeLocalesKey(getLocales());
		return languageMap.get(localesKey);
	}

	private static void mapToLanguages(Vector<Locale> locales) {
		
		String localesKey = composeLocalesKey(locales.elements());

		if(!languageMap.containsKey(localesKey)){
			List<Language> languages = termService.getLanguagesByLocale(locales.elements());
			languageMap.put(localesKey, languages);
		}
	}

	private static String composeLocalesKey(Enumeration<Locale> locales) {
		String localesKey = "";
		while(locales.hasMoreElements()){
			localesKey += locales.nextElement() + ",";
		}
		return localesKey;
	}



}
