package cn.patterncat.metrics.config;

import org.apache.catalina.connector.Connector;
import org.apache.commons.lang3.StringUtils;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.Http11NioProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by patterncat on 2017-01-04.
 */
public class TomcatCustomizer implements EmbeddedServletContainerCustomizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TomcatCustomizer.class);

    private Map<String,Object> attributes = new HashMap<>();

    AdvancedThreadExecutor advancedThreadExecutor;

    public TomcatCustomizer(AdvancedThreadExecutor advancedThreadExecutor) {
        this.advancedThreadExecutor = advancedThreadExecutor;
    }

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {
        if(container instanceof TomcatEmbeddedServletContainerFactory){
            TomcatEmbeddedServletContainerFactory factory = (TomcatEmbeddedServletContainerFactory)container;
            factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
                @Override
                public void customize(Connector connector) {
                    connector.getService().addExecutor(advancedThreadExecutor);
                    ProtocolHandler handler = connector.getProtocolHandler();
//                    logger.info("handler:{}",handler.getClass().getName());
//                    connector.setAttribute("maxThreads",400);
//                    connector.setAttribute("acceptCount",400);

                    LOGGER.info("====== tomcat protocol config start ======");
                    boolean http11 = handler instanceof Http11NioProtocol;
                    if(!http11){
                        return ;
                    }
                    Http11NioProtocol http11NioProtocol = (Http11NioProtocol)handler;
                    http11NioProtocol.setExecutor(advancedThreadExecutor);
                    Method[] methods = Http11NioProtocol.class.getMethods();
                    for(Method m:methods){
                        if(!m.getName().startsWith("get")){
                            continue;
                        }
//                            if("getSslEnabledProtocols".equals(m.getName()) || "getSSLProtocol".equals(m.getName())){
//                                //defaultSSLHostConfig.getEnabledProtocols() = null
//                                // StringUtils.join(this.defaultSSLHostConfig.getEnabledProtocols());
//                                continue;
//                            }
                        Class<?> methodParams[] = m.getParameterTypes();
                        if(methodParams == null || methodParams.length == 0){
                            Object value = null;
                            try {
                                value = m.invoke(http11NioProtocol);
                            } catch (Exception e) {
                                LOGGER.warn("invoke http11NioProtocol method:{},error:{}",m.toString(),e.getMessage());
                            }
                            String prop = StringUtils.uncapitalize(m.getName().substring(3));
                            LOGGER.info("property:{} value:{}",prop,value);
                            attributes.put(prop,value);
                        }
                    }
                    LOGGER.info("====== tomcat protocol config end ======");
                }
            });
        }
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
