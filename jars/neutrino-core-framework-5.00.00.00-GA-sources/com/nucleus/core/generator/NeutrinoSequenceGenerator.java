/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.generator;

import java.io.Serializable;
import java.util.Properties;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.relational.QualifiedName;
import org.hibernate.boot.model.relational.QualifiedNameImpl;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.NoopOptimizer;
import org.hibernate.id.enhanced.PooledOptimizer;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.dbmapper.util.DataBaseMappingUtil;
import com.nucleus.finnone.pro.base.constants.CoreConstant;
import com.nucleus.persistence.sequence.DatabaseSequenceGenerator;
import org.hibernate.id.enhanced.OptimizerFactory;

/**
 * This is an implementation of SequenceStyleGenerator which limits the name of  sequence/table (auto created for sequence generation)
 * to not exceed 30.
 * 
 */
public class NeutrinoSequenceGenerator extends SequenceStyleGenerator {

    public static final String ENTITY_TARGET_TABLE_NAME = "target_table";
	public static final String FULLY_QUALIFIED_ENTITY_NAME = "entity_name";

    
    private static Boolean isPostgresDb;
    
    private static DatabaseSequenceGenerator databaseSequenceGenerator;
    
    private static PlatformTransactionManager transactionManager;
    
    
	private static void setIsPostgresDb(Boolean isPostgresDb) {
		NeutrinoSequenceGenerator.isPostgresDb = isPostgresDb;
	}


	private static void setDatabaseSequenceGenerator(DatabaseSequenceGenerator databaseSequenceGenerator) {
		NeutrinoSequenceGenerator.databaseSequenceGenerator = databaseSequenceGenerator;
	}


	private static void setTransactionManager(PlatformTransactionManager transactionManager) {
		NeutrinoSequenceGenerator.transactionManager = transactionManager;
	}


	/**
     * Determine the name of the sequence to use. This method overrides the one provided by SequenceStyleGenerator
     *  and uses the target table name to determine the sequence name instead of the entity name.
     * <p/>
     *
     * @param params The params supplied in the generator config (plus some standard useful extras).
     * @param dialect The dialect in effect
     * @return The sequence name
     */
    @Override
    protected QualifiedName determineSequenceName(Properties params, Dialect dialect, JdbcEnvironment jdbcEnv) {
        String sequencePerEntitySuffix = ConfigurationHelper.getString(CONFIG_SEQUENCE_PER_ENTITY_SUFFIX, params,
                DEF_SEQUENCE_SUFFIX);
        // JPA_ENTITY_NAME value honors <class ... entity-name="..."> (HBM) and @Entity#name (JPA) overrides.
        String sequenceName = ConfigurationHelper.getBoolean(CONFIG_PREFER_SEQUENCE_PER_ENTITY, params, false) ? params
                .getProperty(ENTITY_TARGET_TABLE_NAME) + sequencePerEntitySuffix : DEF_SEQUENCE_NAME;
        sequenceName = DataBaseMappingUtil.abbreviateName(sequenceName);
        sequenceName = ConfigurationHelper.getString(SEQUENCE_PARAM, params, sequenceName);
		if(isPostgresDb()){
			return new QualifiedNameImpl(jdbcEnv.getCurrentCatalog(), null, new Identifier(sequenceName,false));
		}
        return new QualifiedNameImpl(jdbcEnv.getCurrentCatalog(), jdbcEnv.getCurrentSchema(), new Identifier(sequenceName,false));
    }


	@Override
	public Serializable generate(SharedSessionContractImplementor session, Object object)  {
		
		if(isPostgresDb() && TransactionSynchronizationManager.isCurrentTransactionReadOnly())
		{
			 String sequenceName=super.getDatabaseStructure().getName();
			 TransactionTemplate template = new TransactionTemplate(NeutrinoSequenceGenerator.transactionManager);
	         template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	         return template.execute((TransactionStatus status)->
	        	  NeutrinoSequenceGenerator.databaseSequenceGenerator.getNextValue(sequenceName)
	         );
		}
		return super.generate(session, object);
	}

	@Override
	protected int determineIncrementSize(Properties params) {
		int customIncrementSize = CustomIdGenerationMapUtility
				.getOptimizedEntityDetail(ConfigurationHelper.getString(FULLY_QUALIFIED_ENTITY_NAME, params));
		if (customIncrementSize < 1) {
			return ConfigurationHelper.getInt(INCREMENT_PARAM, params, DEFAULT_INCREMENT_SIZE);
		} else {
			return customIncrementSize;
		}
	}

	public boolean isPostgresDb() {

		if(isPostgresDb==null )
		{
			DatabaseSequenceGenerator dbSequenceGenerator = NeutrinoSpringAppContextUtil
					.getBeanByName("neutrinoSequenceGenerator",DatabaseSequenceGenerator.class);

			String dbType=dbSequenceGenerator.getDatabaseType();
			if(CoreConstant.POSTGRES.equals(dbType))
			{
				NeutrinoSequenceGenerator.setIsPostgresDb(true);
				NeutrinoSequenceGenerator.setDatabaseSequenceGenerator(dbSequenceGenerator);
				NeutrinoSequenceGenerator.setTransactionManager(NeutrinoSpringAppContextUtil
						.getBeanByName("transactionManager",PlatformTransactionManager.class));
			}
			else
			{
				NeutrinoSequenceGenerator.setIsPostgresDb(false);
			}

		}

		return isPostgresDb;
	}
}
