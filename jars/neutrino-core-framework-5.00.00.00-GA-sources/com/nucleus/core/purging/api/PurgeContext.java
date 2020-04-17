/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.purging.api;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.nucleus.persistence.EntityDao;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
public class PurgeContext{

    private List<Long>                 ids;

    private Connection                 connection;

    private NamedParameterJdbcTemplate jdbcTemplate;

    private EntityDao                  entityDao;

    public PurgeContext(List<Long> ids, NamedParameterJdbcTemplate jdbcTemplate, EntityDao entityDao) {
        super();
        this.ids = Collections.unmodifiableList(ids);
        this.jdbcTemplate = jdbcTemplate;
        this.entityDao = entityDao;
    }

    public List<Long> getIds() {
        return ids;
    }

    public Connection getConnection() {
        return connection;
    }

    public NamedParameterJdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public EntityDao getEntityDao() {
        return entityDao;
    }

}
