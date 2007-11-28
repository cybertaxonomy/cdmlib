/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy 
* http://www.e-taxonomy.eu
* 
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.model.common;


import org.apache.log4j.Logger;

import java.util.*;
import javax.persistence.*;

/**
 * list of languages according to current internet best practices as given by IANA
 * or ISO codes.  http://www.ietf.org/rfc/rfc4646.txt http://www.loc.
 * gov/standards/iso639-2/php/English_list.php
 * @author m.doering
 * @version 1.0
 * @created 08-Nov-2007 13:06:31
 */
@Entity
public class Language extends NonOrderedTermBase {
	static Logger logger = Logger.getLogger(Language.class);
	static Language langEN = new Language("English","EN");
	static Language langCH = new Language("Chinese","CH");
	static Language langGE = new Language("German","DE");


	public Language(String text, String label, Language lang) {
		super(text, label);
		this.addRepresentation(new Representation(text,label,lang));
	}
	public Language(String label, String text) {
		this(label,text, DEFAULT());
	}

	public static final Language DEFAULT(){
		return langEN;
	}
	
	public static final Language CHINESE(){
		return langCH;
	}

	public static final Language ENGLISH(){
		return langEN;
	}

	public static final Language SPANISH(){
		return null;
	}

	public static final Language HINDI(){
		return null;
	}

	public static final Language ARABIC(){
		return null;
	}

	public static final Language BENGALI(){
		return null;
	}

	public static final Language RUSSIAN(){
		return null;
	}

	public static final Language PORTUGUESE(){
		return null;
	}

	public static final Language JAPANESE(){
		return null;
	}

	public static final Language GERMAN(){
		return langGE;
	}

}