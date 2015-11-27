package org.solovyev.android.plotter.math;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import org.solovyev.android.plotter.Check;
import org.solovyev.android.plotter.Function;

public final class ExpressionFunction extends Function {

    @NonNull
    private final Expression expression;
    @NonNull
    private final String[] variables;

    private ExpressionFunction(@Nullable String name, @NonNull String expression, @NonNull String... variables) {
        super(name);
        final ExpressionBuilder builder = new ExpressionBuilder(expression);
        for (String variable : variables) {
            Check.isTrue(!TextUtils.isEmpty(variable));
            builder.variable(variable);
        }
        this.expression = builder.build();
        this.variables = new String[variables.length];
        System.arraycopy(variables, 0, this.variables, 0, variables.length);
    }

    @NonNull
    public static ExpressionFunction create(@NonNull String expression, @NonNull String... variables) {
        return new ExpressionFunction(expression, expression, variables);
    }

    @NonNull
    public static ExpressionFunction createNamed(@NonNull String name, @NonNull String expression, @NonNull String... variables) {
        return new ExpressionFunction(name, expression, variables);
    }

    @Override
    public int getArity() {
        return variables.length;
    }

    @Override
    public float evaluate() {
        return (float) expression.evaluate();
    }

    @Override
    public float evaluate(float x) {
        expression.setVariable(variables[0], x);
        try {
            return (float) expression.evaluate();
        } catch (ArithmeticException e) {
            return Float.NaN;
        }
    }

    @Override
    public float evaluate(float x, float y) {
        expression.setVariable(variables[0], x);
        expression.setVariable(variables[1], y);
        try {
            return (float) expression.evaluate();
        } catch (ArithmeticException e) {
            return Float.NaN;
        }
    }

    @Override
    public String toString() {
        return super.toString() + "[exp]";
    }
}