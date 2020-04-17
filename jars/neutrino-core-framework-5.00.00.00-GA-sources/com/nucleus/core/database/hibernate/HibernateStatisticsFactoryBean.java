package com.nucleus.core.database.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.FactoryBean;

public class HibernateStatisticsFactoryBean implements FactoryBean<Statistics> {

    private SessionFactory sessionFactory;

    private boolean        statisticsEnabled;

    @Override
    public Statistics getObject() throws Exception {
        Statistics statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(statisticsEnabled);
        return statistics;
    }

    @Override
    public Class<?> getObjectType() {
        return Statistics.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setStatisticsEnabled(boolean statisticsEnabled) {
        this.statisticsEnabled = statisticsEnabled;
    }
}
