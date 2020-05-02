package me.whiteship.natural.user;

import org.junit.Test;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class CurrentUserTests {

    @Test
    public void anonymousUserToNull() {
        // Given
        String principal = "anonymousUser";
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setRootObject(principal);
        context.setVariable("this", principal);

        // When
        Expression expression = parser.parseExpression("#this eq 'anonymousUser' ? null : user");

        // Then
        System.out.println(expression.getValue(context));
    }

//    @Test
//    public void currenttPrincipalToUser() {
//        // Given
//        Principal principal = new
//        ExpressionParser parser = new SpelExpressionParser();
//        StandardEvaluationContext context = new StandardEvaluationContext();
//        context.setRootObject(principal);
//        context.setVariable("this", principal);
//
//        // When
//        Expression expression = parser.parseExpression("#this == 'anonymousUser' ? null : user");
//
//        // Then
//        System.out.println(expression.getValue(context));
//    }
}
