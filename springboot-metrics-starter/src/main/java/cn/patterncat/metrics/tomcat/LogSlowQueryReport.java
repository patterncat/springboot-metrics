package cn.patterncat.metrics.tomcat;

import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.apache.tomcat.jdbc.pool.interceptor.AbstractQueryReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by patterncat on 2017-07-08.
 */
public class LogSlowQueryReport extends AbstractQueryReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogSlowQueryReport.class);

    @Override
    protected String reportSlowQuery(String query, Object[] args, String name, long start, long delta) {
        String sql = super.reportSlowQuery(query, args, name, start, delta);
        LOGGER.error("slow sql cost:{},sql:{}",delta,sql);
        return sql;
    }

    @Override
    protected String reportFailedQuery(String query, Object[] args, String name, long start, Throwable t) {
        String sql = super.reportFailedQuery(query, args, name, start, t);
        LOGGER.error("sql failed:{}",sql);
        return sql;
    }

    @Override
    public void setProperties(Map<String, PoolProperties.InterceptorProperty> properties) {
        super.setProperties(properties);
        final String threshold = "threshold";
        PoolProperties.InterceptorProperty p1 = properties.get(threshold);
        if (p1!=null) {
            setThreshold(Long.parseLong(p1.getValue()));
        }
    }

    @Override
    protected void prepareStatement(String sql, long time) {
        //do nothing
    }

    @Override
    protected void prepareCall(String query, long time) {
        //do nothing
    }

    @Override
    public void closeInvoked() {
        //do nothing
    }
}
