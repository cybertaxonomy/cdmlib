/**
 * Copyright (C) 2015 EDIT
 * European Distributed Institute of Taxonomy
 * http://www.e-taxonomy.eu
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * See LICENSE.TXT at the top of this package for the full license terms.
 */
package eu.etaxonomy.cdm.format;

import eu.etaxonomy.cdm.format.ICdmFormatter.FormatKey;
import eu.etaxonomy.cdm.format.occurrences.DefaultCdmFormatter;
import eu.etaxonomy.cdm.format.occurrences.DerivedUnitFormatter;
import eu.etaxonomy.cdm.format.occurrences.FieldUnitFormatter;
import eu.etaxonomy.cdm.format.occurrences.MediaSpecimenFormatter;
import eu.etaxonomy.cdm.format.occurrences.SequenceFormatter;
import eu.etaxonomy.cdm.format.occurrences.SingleReadFormatter;
import eu.etaxonomy.cdm.model.common.CdmBase;
import eu.etaxonomy.cdm.model.molecular.Sequence;
import eu.etaxonomy.cdm.model.molecular.SingleRead;
import eu.etaxonomy.cdm.model.occurrence.DerivedUnit;
import eu.etaxonomy.cdm.model.occurrence.FieldUnit;
import eu.etaxonomy.cdm.model.occurrence.MediaSpecimen;

/**
 * Factory class that instantiates a matching ICdmFormatter for the given object
 * and configures the format according to the given formatKeys.
 * 
 * @author pplitzner
 \* @since Nov 30, 2015
 *
 */
public class CdmFormatterFactory {

	/**
	 * Returns a matching ICdmFormatter for the given object configured with the
	 * given formatKeys
	 * 
	 * @param object
	 *            the object which should be formatted as a string
	 * @param formatKeys
	 *            the formatKeys specifying the parts of which the string is
	 *            built
	 * @return an ICdmFormatter for the given object configured with the given
	 *         formatKeys
	 */
	public static ICdmFormatter getFormatter(Object object,
			FormatKey... formatKeys) {
		ICdmFormatter formatter = null;
		if (object instanceof CdmBase) {
			CdmBase cdmBase = (CdmBase) object;
			if (cdmBase.isInstanceOf(Sequence.class)) {
				return new SequenceFormatter(object, formatKeys);
			}
			else if (cdmBase.isInstanceOf(SingleRead.class)) {
				return new SingleReadFormatter(object, formatKeys);
			}
			else if (cdmBase.isInstanceOf(MediaSpecimen.class)) {
				return new MediaSpecimenFormatter(object, formatKeys);
			}
			else if (cdmBase.isInstanceOf(DerivedUnit.class)) {
				return new DerivedUnitFormatter(object, formatKeys);
			}
			else if (cdmBase.isInstanceOf(FieldUnit.class)) {
				return new FieldUnitFormatter(object, formatKeys);
			}
		}
		if (formatter == null) {
			formatter = new DefaultCdmFormatter(object, formatKeys);
		}
		return formatter;
	}

	/**
	 * Convenience method which directly formats the given object according to
	 * the given formatKeys.
	 * 
	 * @param object
	 *            the object which should be formatted as a string
	 * @param formatKeys
	 *            the formatKeys specifying the parts of which the string is
	 *            built
	 * @return a string representation of the given object according to the
	 *         given formatKeys
	 */
	public static String format(Object object, FormatKey... formatKeys) {
		ICdmFormatter formatter = null;
		if (object instanceof CdmBase) {
			CdmBase cdmBase = (CdmBase) object;
			if (cdmBase.isInstanceOf(Sequence.class)) {
				formatter = new SequenceFormatter(object, formatKeys);
			}
			else if (cdmBase.isInstanceOf(SingleRead.class)) {
				formatter = new SingleReadFormatter(object, formatKeys);
			}
			else if (cdmBase.isInstanceOf(MediaSpecimen.class)) {
				formatter = new MediaSpecimenFormatter(object, formatKeys);
			}
			else if (cdmBase.isInstanceOf(DerivedUnit.class)) {
				formatter = new DerivedUnitFormatter(object, formatKeys);
			}
			else if (cdmBase.isInstanceOf(FieldUnit.class)) {
				formatter = new FieldUnitFormatter(object, formatKeys);
			}
		}
		if (formatter == null) {
			formatter = new DefaultCdmFormatter(object, formatKeys);
		}
		return formatter.format(object, formatKeys);
	}

}
