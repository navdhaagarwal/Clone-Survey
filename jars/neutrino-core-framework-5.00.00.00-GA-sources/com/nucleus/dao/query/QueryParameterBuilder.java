/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.dao.query;

import javax.persistence.Query;

/**
 * Interface to represent query parameter builder.
 * @author Nucleus Software India Pvt Ltd
 */
public interface QueryParameterBuilder {

    void setParameterIntoQuery(Query query);

}
