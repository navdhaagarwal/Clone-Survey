package com.nucleus.core.generator;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import com.nucleus.core.dbmapper.util.DataBaseMappingUtil;

public class NeutrinoNamingStrategy extends PhysicalNamingStrategyStandardImpl  {

    private static final long serialVersionUID = 6890554030847046251L;

    /**
     * Transforms class names to table names by using the described naming conventions.
     * @param className
     * @return  The constructed table name.
     */
  /* 
    public String classToTableName(String className) {
        String tableNameInSingularForm = super.classToTableName(className);
        return transformToPluralForm(tableNameInSingularForm);
    }*/

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        String underScoreText = addUnderscores(name.getText());
    	return new Identifier(transformToPluralForm(underScoreText), name.isQuoted());
    }
    
    
    
    
    /*@Override
    public String propertyToColumnName(String propertyName) {
        String propertyToColumnName = super.propertyToColumnName(propertyName);
        return transformToPluralForm(propertyToColumnName);
    }*/
    
    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
    	String underScoreText = addUnderscores(name.getText());
    	return new Identifier(transformToPluralForm(underScoreText), name.isQuoted());
    }
    
    @Override
	public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment context) {
    	String underScoreText = addUnderscores(name.getText());
    	return new Identifier(transformToPluralForm(underScoreText), name.isQuoted());
	}
    
    
    
   /* @Override
    public String collectionTableName(String ownerEntity, String ownerEntityTable, String associatedEntity, String associatedEntityTable, String propertyName) {
        String collectionTableName = super.collectionTableName(ownerEntity, ownerEntityTable, associatedEntity, associatedEntityTable, propertyName);
        return transformToPluralForm(collectionTableName);
    }*/
  /*  
    @Override
    public String logicalCollectionColumnName(String columnName, String propertyName, String referencedColumn) {
        String logicalCollectionColumnName = super.logicalCollectionColumnName(columnName, propertyName, referencedColumn);
        return transformToPluralForm(logicalCollectionColumnName);
    }*/
    
    /*@Override
    public String logicalColumnName(String columnName, String propertyName) {
        String  logicalColumnName=super.logicalColumnName(columnName, propertyName) ;
        return transformToPluralForm(logicalColumnName);
    }
    @Override
    public String logicalCollectionTableName(String tableName, String ownerEntityTable, String associatedEntityTable, String propertyName) {
        String  logicalCollectionTableName=super.logicalCollectionTableName(tableName, ownerEntityTable, associatedEntityTable, propertyName);
        return transformToPluralForm(logicalCollectionTableName);
    }
    
    @Override
    public String joinKeyColumnName(String joinedColumn, String joinedTable) {
        String  logicalColumnName=super.joinKeyColumnName(joinedColumn, joinedTable);
        return transformToPluralForm(logicalColumnName);
    }
    
    @Override
    public String foreignKeyColumnName(String propertyName, String propertyEntityName, String propertyTableName, String referencedColumnName) {
        String  logicalColumnName=super.foreignKeyColumnName(propertyName, propertyEntityName, propertyTableName, referencedColumnName);
        return transformToPluralForm(logicalColumnName);
    }

    
    
*/    
    
    private String transformToPluralForm(String tableNameInSingularForm) {
        return DataBaseMappingUtil.abbreviateName(tableNameInSingularForm);
    }
    
    protected static String addUnderscores(String name) {
        final StringBuilder buf = new StringBuilder(name.replace('.', '_'));
        for (int i = 1; i < buf.length() - 1; i++) {
            if (Character.isLowerCase(buf.charAt(i - 1)) &&
                Character.isUpperCase(buf.charAt(i)) &&
                Character.isLowerCase(buf.charAt(i + 1))) {
                buf.insert(i++, '_');
            }
        }
        return buf.toString().toLowerCase();
    }
    

}