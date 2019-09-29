/**
* Copyright (C) 2007 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/

package eu.etaxonomy.cdm.io.common.mapping.out;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import eu.etaxonomy.cdm.io.common.DbExportStateBase;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.common.Language;
import eu.etaxonomy.cdm.model.common.LanguageString;
import eu.etaxonomy.cdm.model.description.TextData;

/**
 * Maps text data to a database string field. (Only handles one language)
 * @author a.mueller
 * @since 06.02.2012
 */
public class DbTextDataMapper extends DbSingleAttributeExportMapperBase<DbExportStateBase<?, IExportTransformer>> implements IDbExportMapper<DbExportStateBase<?, IExportTransformer>, IExportTransformer>{
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(DbTextDataMapper.class);

	private final List<Language> languages;
	private boolean restrictToGivenLanguages;

    /**
     * Returns a mapper that uses the default language representation first.
     * If it does not exist it uses any existing representation.
     * @param dbAttributeString the target column name
     * @return the DbTextDataMapper
     */
    public static DbTextDataMapper NewDefaultInstance(String dbAttributeString){
        return new DbTextDataMapper(null, dbAttributeString, false, null);
    }

    /**
     * Returns a mapper that uses the given language representation first.
     * If it does not exist it uses the default language or, if it also does not exist,
     * any existing representation.
     * @param dbAttributeString the target column name
     * @param language the preferred language of the representation to use
     * @return the DbTextDataMapper
     */
	public static DbTextDataMapper NewInstance(String dbAttributeString, Language language){
		return new DbTextDataMapper(toList(language), dbAttributeString, false, null);
	}

    /**
     * Returns a mapper that uses the given language representation.
     * If a representation in the given language does not exist the default value is returned.
     * Representations in other languages are NOT considered.
     * @param dbAttributeString the target column name
     * @param language the ONLY language of the representation to use
     * @return the DbTextDataMapper
     */
	public static DbTextDataMapper NewInstance(String dbAttributeString, Language language, String defaultValue){
	    return new DbTextDataMapper(toList(language), dbAttributeString, true, defaultValue);
	}

    /**
     * Returns a mapper that returns a representation according to the
     * language priorisation of the *languages* attribute.
     * If no representation in any of the given languages exists
     * and *restrictToGivenLanguages* is <code>false</code> first the
     * any other representation is returned with the applications default
     * language having priority.
     * If still no representation exists or if *restrictToGivenLanguages*
     * is <code>true</code> the *defaultValue* is returned.

     * @param dbAttributeString the target column name
     * @param languages the sorted list of (preferred) languages
     * @param restrictToGivenLanguages flag wether to restrict to given languages or not
     * @param defaultValue the default value
     * @return the DbTextDataMapper
     */
    public static DbTextDataMapper NewInstance(String dbAttributeString, List<Language> languages,
            boolean restrictToGivenLanguages, String defaultValue){
        return new DbTextDataMapper(languages, dbAttributeString, restrictToGivenLanguages, defaultValue);
    }

    private static List<Language> toList(Language language) {
        return Arrays.asList(new Language[]{language});
    }

//************************* CONSTRUCTOR ********************************************/

	protected DbTextDataMapper(List<Language> languages, String dbAttributeString,
	            boolean restrictToGivenLanguages, Object defaultValue) {

	    super("multiLanguageText", dbAttributeString, defaultValue);
	    if (languages == null){
	        languages = new ArrayList<>();
	    }
		this.languages  = languages;
		this.restrictToGivenLanguages = restrictToGivenLanguages;
	}

	@Override
	protected Object getValue(CdmBase cdmBase) {
		if (cdmBase.isInstanceOf(TextData.class)){
			TextData textData = CdmBase.deproxy(cdmBase, TextData.class);
			LanguageString langString = textData.getPreferredLanguageString(languages, restrictToGivenLanguages);
			if (langString != null){
				return langString.getText();
			}else{
				return null;
			}
		}else{
			throw new ClassCastException("CdmBase for "+this.getClass().getName() +" must be of type TextData, but was " + cdmBase.getClass());
		}
	}

	@Override
	protected int getSqlType() {
		return Types.VARCHAR;
	}

	@Override
	public Class<?> getTypeClass() {
		return String.class;
	}
}
