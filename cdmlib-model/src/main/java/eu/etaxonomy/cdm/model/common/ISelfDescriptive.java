package eu.etaxonomy.cdm.model.common;

/**
 * A general interface for objects that can provide a meaningful description of
 * themselves. If an entity implements this interface, and it turns out to violate some
 * validation constraint, the CVI will use {@link #getDescription()} when saving a
 * description of the entity to the error tables. Otherwise it will simply use the
 * entity's {@code toString()} method. This description will be displayed in the
 * taxeditor's "Validation Problems" view, and should enable the user to find the entity
 * back in other parts of the taxeditor (e.g. the classification tree). Thus, an entity's
 * description is most likely the most useful element in the taxeditor's
 * "Validation Problems" view - together, of course, with the constraint validator's
 * message about what exactly was wrong. So it pays to implement this interface. See also
 * {@code EntityValidationResultDaoHibernateImpl} in the cdmlib-persistence project.
 * 
 * @author ayco_holleman
 * 
 */
public interface ISelfDescriptive {

	String getDescription();

}
