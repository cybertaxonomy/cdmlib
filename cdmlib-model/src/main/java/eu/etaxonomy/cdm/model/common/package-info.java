/* Package Annotations*/

@GenericGenerator(
		name="system-increment", 
		strategy = "increment"
)
@org.hibernate.annotations.TypeDefs( { 
    @org.hibernate.annotations.TypeDef(name="uuidUserType", typeClass=eu.etaxonomy.cdm.model.common.UUIDUserType.class)
})
package eu.etaxonomy.cdm.model.common;



import org.hibernate.annotations.GenericGenerator;	