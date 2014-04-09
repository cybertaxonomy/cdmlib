package eu.etaxonomy.cdm.validation;

/**
 * The CRUD event that triggered a validation. When an entity violates some constraint, it
 * might be helpful to report back to the user what type of CRUD event caused the violation.
 * Note that validation may not not have been triggered by any CRUD event at all, e.g. during
 * some batch-like validation process. Level-2 validation can never be triggered by a DELETE
 * event, because Level-2 validation only validates the entity itself. However, a DELETE event
 * <i>can</i> possibly trigger a Level-3 validation, because that disrupts the object graph the
 * entity was part of.
 * 
 * @author ayco holleman
 * 
 */
public enum CRUDEventType
{
	NONE, INSERT, UPDATE, DELETE
}
