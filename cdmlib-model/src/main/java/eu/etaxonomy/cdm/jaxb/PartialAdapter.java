/**
* Copyright (C) 2009 EDIT
* European Distributed Institute of Taxonomy
* http://www.e-taxonomy.eu
*
* The contents of this file are subject to the Mozilla Public License Version 1.1
* See LICENSE.TXT at the top of this package for the full license terms.
*/ 

package eu.etaxonomy.cdm.jaxb;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import eu.etaxonomy.cdm.model.common.Partial;

public class PartialAdapter extends XmlAdapter<String, Partial> {
	
	protected static Pattern PATTERN;
	private static String REGEX = "^(\\d{4})(?:\\-(\\d{1,2})(?:\\-(\\d{1,2})(?:T(\\d{2})(?:\\:(\\d{2})(?:\\:(\\d{2})(?:\\.(\\d+))?)?)?)?)?)?$";
	
	static {
		PATTERN = Pattern.compile(REGEX);
	}

	@Override
    public String marshal(Partial partial) throws Exception {
		if (partial == null || partial.isEmpty()) {
			return "";
		}
		StringBuilder stringBuilder = new StringBuilder();
		if (partial.hasYear()) {
			stringBuilder.append(partial.getYear());
		}
		if (partial.hasMonth()) {
			stringBuilder.append('-').append(partial.getMonth());
		}
		if (partial.hasDay()) {
			stringBuilder.append('-').append(partial.getDay());
		}
		if (partial.hasHour()) {
			stringBuilder.append('T').append(String.format("%02d", partial.getHour()));
		}
		if (partial.hasMinute()) {
			stringBuilder.append(':').append(String.format("%02d", partial.getMinute()));
		}
		if (partial.hasSecond()) {
			stringBuilder.append(':').append(String.format("%02d", partial.getSecond()));
		}
		if (partial.hasMillis()) {
			stringBuilder.append('.').append(partial.getMillis());
		}
		return stringBuilder.toString();
	}

	@Override
	public Partial unmarshal(String string) throws Exception {
		Matcher matcher = PATTERN.matcher(string);
		if(matcher.matches()) {
			Partial result = Partial.of(
					parseGroup(matcher, 1),
					parseGroup(matcher, 2),
					parseGroup(matcher, 3),
					parseGroup(matcher, 4),
					parseGroup(matcher, 5),
					parseGroup(matcher, 6),
					parseGroup(matcher, 7));
			return result;
		} else {
			throw new RuntimeException("Could not parse " + string + " with regex " + PartialAdapter.REGEX);
		}
	}

	private static Integer parseGroup(Matcher matcher, int group) {
		String value = matcher.group(group);
		return value == null ? null : Integer.parseInt(value);
	}

}
