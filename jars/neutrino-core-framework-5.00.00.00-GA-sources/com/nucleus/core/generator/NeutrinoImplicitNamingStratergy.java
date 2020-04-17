package com.nucleus.core.generator;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.ImplicitBasicColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitCollectionTableNameSource;
import org.hibernate.boot.model.naming.ImplicitJoinColumnNameSource;
import org.hibernate.boot.model.naming.ImplicitJoinTableNameSource;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.internal.util.StringHelper;

import com.nucleus.core.dbmapper.util.DataBaseMappingUtil;

public class NeutrinoImplicitNamingStratergy extends ImplicitNamingStrategyJpaCompliantImpl {

	private static final long serialVersionUID = 6890554030847046251L;

	@Override
	public Identifier determineCollectionTableName(ImplicitCollectionTableNameSource source) {
		final String entityName = transformEntityName(source.getOwningEntityNaming());

		String propertyName = transformAttributePath(source.getOwningAttributePath());
		
		String nameTemp = entityName + '_' + propertyName;// propName;
		
		return toIdentifier(nameTemp, source.getBuildingContext());

	}

	@Override
	public Identifier determineJoinTableName(ImplicitJoinTableNameSource source) {
		// JPA states we should use the following as default:
		// "The concatenated names of the two associated primary entity tables
		// (owning side
		// first), separated by an underscore."
		// aka:
		// {OWNING SIDE PRIMARY TABLE NAME}_{NON-OWNING SIDE PRIMARY TABLE NAME}
		String propertyName = transformAttributePath(source.getAssociationOwningAttributePath());
		
		String nameTemp = source.getOwningPhysicalTableName() + '_' + propertyName;

		return toIdentifier(nameTemp, source.getBuildingContext());
	}

	

	@Override
	public Identifier determineJoinColumnName(ImplicitJoinColumnNameSource source) {
		String name = null;
		if (source.getNature() == ImplicitJoinColumnNameSource.Nature.ELEMENT_COLLECTION
				|| source.getAttributePath() == null) {
			name = transformEntityName(source.getEntityNaming());
		} else {
			name = transformAttributePath(source.getAttributePath());
		}
		
		return toIdentifier(name, source.getBuildingContext());

	}

}
