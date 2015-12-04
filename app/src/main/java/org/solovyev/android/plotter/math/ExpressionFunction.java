package org.solovyev.android.plotter.math;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.solovyev.android.plotter.Check;
import org.solovyev.android.plotter.Function;

public final class ExpressionFunction extends Function {

    private static final String JSON_NAME = "n";
    private static final String JSON_EXPRESSION = "e";
    private static final String JSON_ARGUMENTS = "a";

    @NonNull
    private final Expression expression;
    @NonNull
    private final String[] arguments;
    @NonNull
    private final String expressionString;

    private ExpressionFunction(@Nullable String name, @NonNull String expression, @NonNull String... arguments) {
        super(TextUtils.isEmpty(name) ? null : name);
        final ExpressionBuilder builder = new ExpressionBuilder(expression);
        for (String variable : arguments) {
            Check.isTrue(!TextUtils.isEmpty(variable));
            builder.variable(variable);
        }
        this.expression = builder.build();
        this.arguments = new String[arguments.length];
        System.arraycopy(arguments, 0, this.arguments, 0, arguments.length);
        this.expressionString = expression;
    }

    @NonNull
    public static ExpressionFunction create(@NonNull String expression, @NonNull String... arguments) {
        return new ExpressionFunction(null, expression, arguments);
    }

    @NonNull
    public static ExpressionFunction createNamed(@NonNull String name, @NonNull String expression, @NonNull String... arguments) {
        return new ExpressionFunction(name, expression, arguments);
    }

    @NonNull
    public static ExpressionFunction create(@NonNull JSONObject json) throws JSONException {
        final String name = json.optString(JSON_NAME);
        final String expression = json.getString(JSON_EXPRESSION);
        final JSONArray jsonArguments = json.getJSONArray(JSON_ARGUMENTS);
        final String[] arguments = new String[jsonArguments.length()];
        for (int i = 0; i < jsonArguments.length(); i++) {
            arguments[i] = jsonArguments.getString(i);
        }
        if (TextUtils.isEmpty(name)) {
            return create(expression, arguments);
        } else {
            return createNamed(name, expression, arguments);
        }
    }

    @Nullable
    @Override
    public String getName() {
        return hasName() ? super.getName() : expressionString;
    }

    @Override
    public int getArity() {
        return arguments.length;
    }

    @Override
    public float evaluate() {
        return (float) expression.evaluate();
    }

    @Override
    public float evaluate(float x) {
        expression.setVariable(arguments[0], x);
        try {
            return (float) expression.evaluate();
        } catch (ArithmeticException e) {
            return Float.NaN;
        }
    }

    @Override
    public float evaluate(float x, float y) {
        expression.setVariable(arguments[0], x);
        expression.setVariable(arguments[1], y);
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

    @NonNull
    public JSONObject toJson() throws JSONException {
        final JSONObject json = new JSONObject();
        final String name = getName();
        if (!TextUtils.equals(name, expressionString)) {
            json.put(JSON_NAME, name);
        }
        json.put(JSON_EXPRESSION, expressionString);
        final JSONArray jsonArguments = new JSONArray();
        for (int i = 0; i < arguments.length; i++) {
            jsonArguments.put(i, arguments[i]);
        }
        json.put(JSON_ARGUMENTS, jsonArguments);
        return json;
    }

    @NonNull
    public String getExpressionString() {
        return expressionString;
    }
}