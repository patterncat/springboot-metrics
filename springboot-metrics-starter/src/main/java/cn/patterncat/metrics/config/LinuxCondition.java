package cn.patterncat.metrics.config;

import cn.patterncat.metrics.utils.OSHelper;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Created by patterncat on 2017-03-26.
 */
public class LinuxCondition implements Condition {
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        return OSHelper.IS_LINUX;
    }
}
