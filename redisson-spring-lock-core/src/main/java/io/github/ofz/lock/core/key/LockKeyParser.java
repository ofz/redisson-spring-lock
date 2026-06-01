package io.github.ofz.lock.core.key;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

public class LockKeyParser {

    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final ParameterNameDiscoverer DISCOVERER =
            new DefaultParameterNameDiscoverer();

    public static String parse(Method method, Object[] args, String spel) {
        StandardEvaluationContext ctx = new StandardEvaluationContext();
        String[] names = DISCOVERER.getParameterNames(method);

        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                ctx.setVariable(names[i], args[i]);
            }
        }
        return PARSER.parseExpression(spel).getValue(ctx, String.class);
    }
}
