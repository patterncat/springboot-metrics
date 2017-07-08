package cn.patterncat.metrics.config;

import org.springframework.boot.actuate.endpoint.mvc.AbstractMvcEndpoint;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Created by patterncat on 2017-06-05.
 */
public class TomcatConfigEndpoint extends AbstractMvcEndpoint {

    private TomcatCustomizer tomcatCustomizer;

    public TomcatConfigEndpoint(TomcatCustomizer tomcatCustomizer) {
        super("/tomcat-config", false /*sensitive*/);
        this.tomcatCustomizer = tomcatCustomizer;
    }

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String,Object> listTomcatServerConfig() {
        return tomcatCustomizer.getAttributes();
    }
}
