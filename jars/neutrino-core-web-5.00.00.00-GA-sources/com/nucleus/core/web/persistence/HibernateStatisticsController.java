package com.nucleus.core.web.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.access.prepost.PreAuthorize;

import com.nucleus.persistence.EntityDao;
import com.nucleus.web.common.controller.BaseController;

@Controller
@RequestMapping("/hibernateStatistics")
public class HibernateStatisticsController extends BaseController {

    @Inject
    @Named("entityDao")
    protected EntityDao entityDao;

    @PreAuthorize("hasAuthority('ADMIN_AUTHORITY')")
    @RequestMapping(value = "/getPage")
    public String getPage(ModelMap map) {

        return "hibernateStatistics";
    }

    @PreAuthorize("hasAuthority('ADMIN_AUTHORITY')")
    @ResponseBody
    @RequestMapping(value = "/getCacheStatistics")
    public Map<String, Object> getCacheStatistics(ModelMap map) {
        Session session = (Session) entityDao.getEntityManager().getDelegate();
        SessionFactory sessionFactory = session.getSessionFactory();

        Statistics statistics = sessionFactory.getStatistics();
        List<QueryStatistics> qs = new ArrayList<QueryStatistics>();

        for (String q : statistics.getQueries()) {
            qs.add(statistics.getQueryStatistics(q));
        }
        Map<String, Object> map2 = new HashMap<String, Object>();
        map2.put("Statistics", statistics);
        map2.put("QueryStatistics", qs);

        return map2;
    }

    @PreAuthorize("hasAuthority('ADMIN_AUTHORITY')")
    @ResponseBody
    @RequestMapping(value = "/clearQueryCache")
    public String clearQueryCache(ModelMap map) {

        Session session = (Session) entityDao.getEntityManager().getDelegate();
        SessionFactory sessionFactory = session.getSessionFactory();
        sessionFactory.getCache().evictQueryRegions();
        return "done";
    }

}