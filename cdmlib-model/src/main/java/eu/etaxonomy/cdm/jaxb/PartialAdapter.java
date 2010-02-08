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

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.DateTimeFieldType;
import org.joda.time.Partial;

public class PartialAdapter extends XmlAdapter<String, Partial> {
	
	protected static Pattern PATTERN;
	private static String REGEX = "^(\\d{4})(?:\\-(\\d{1,2})(?:\\-(\\d{1,2})(?:T(\\d{2})(?:\\:(\\d{2})(?:\\:(\\d{2})(?:\\.(\\d+))?)?)?)?)?)?$";
	
	static {
		PATTERN = Pattern.compile(REGEX);
	}

	@Override
	public String marshal(Partial partial) throws Exception {
		StringBuilder stringBuilder = new StringBuilder();
		int[] values = partial.getValues();
		
		switch(values.length) {
		case 7:
			stringBuilder.append("." + values[6]);
		case 6:
			stringBuilder.insert(0,":" + values[5]);
		case 5: 
			stringBuilder.insert(0,":" + values[4]);
		case 4:
			stringBuilder.insert(0, "T" + values[3]);
		case 3:
			stringBuilder.insert(0, "-" + values[2]);
		case 2:
			stringBuilder.insert(0, "-" + values[1]);
		case 1:
			stringBuilder.insert(0, values[0]);
		}
		
		return stringBuilder.toString();
	}

	@Override
	public Partial unmarshal(String string) throws Exception {
		Matcher matcher = PATTERN.matcher(string);
		if(matcher.matches()) {
            int nonNullGroups = 0;
			for(int i = 1; i <= matcher.groupCount(); i++) {
				if(matcher.group(i) != null)
					nonNullGroups++;
			}
			DateTimeFieldType[] dateTimeFieldTypes = new DateTimeFieldType[nonNullGroups];
			int[] values = new int[nonNullGroups];
			switch(nonNullGroups) {
			case 7:
				dateTimeFieldTypes[6] = DateTimeFieldType.millisOfSecond();
				values[6] = Integer.parseInt(matcher.group(7));
			case 6:
				dateTimeFieldTypes[5] = DateTimeFieldType.secondOfMinute();
				values[5] = Integer.parseInt(matcher.group(6));
			case 5:
				dateTimeFieldTypes[4] = DateTimeFieldType.minuteOfHour();
				values[4] = Integer.parseInt(matcher.group(5));
			case 4:
				dateTimeFieldTypes[3] = DateTimeFieldType.hourOfDay();
				values[3] = Integer.parseInt(matcher.group(4));
			case 3:
				dateTimeFieldTypes[2] = DateTimeFieldType.dayOfMonth();
				values[2] = Integer.parseInt(matcher.group(3));
			case 2:
				dateTimeFieldTypes[1] = DateTimeFieldType.monthOfYear();
				values[1] = Integer.parseInt(matcher.group(2));
			case 1:
				dateTimeFieldTypes[0] = DateTimeFieldType.year();
				values[0] = Integer.parseInt(matcher.group(1));
			}
			return new Partial(dateTimeFieldTypes, values);
		} else {
			throw new RuntimeException("Could not parse " + string + " with regex " + PartialAdapter.REGEX);
		}
	}

}
