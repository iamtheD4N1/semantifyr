/*
 * SPDX-FileCopyrightText: 2025 The Semantifyr Authors
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package hu.bme.mit.semantifyr.oxsts.lang.semantics.typesystem;

import hu.bme.mit.semantifyr.oxsts.lang.semantics.expression.ExpressionEvaluator;
import hu.bme.mit.semantifyr.oxsts.model.oxsts.*;

public class ExpressionModalityEvaluator extends ExpressionEvaluator<Modality> {

    @Override
    protected Modality visit(RangeExpression expression) {
        return evaluate(expression.getLeft()).combine(evaluate(expression.getRight()));
    }

    @Override
    protected Modality visit(ComparisonOperator expression) {
        return evaluate(expression.getLeft()).combine(evaluate(expression.getRight()));
    }

    @Override
    protected Modality visit(ArithmeticBinaryOperator expression) {
        return evaluate(expression.getLeft()).combine(evaluate(expression.getRight()));
    }

    @Override
    protected Modality visit(BooleanOperator expression) {
        return evaluate(expression.getLeft()).combine(evaluate(expression.getRight()));
    }

    @Override
    protected Modality visit(ArithmeticUnaryOperator expression) {
        return evaluate(expression.getBody());
    }

    @Override
    protected Modality visit(NegationOperator expression) {
        return evaluate(expression.getBody());
    }

    @Override
    protected Modality visit(ArrayLiteral expression) {
        return expression.getValues().stream()
                .map(this::evaluate)
                .reduce(Modality.Constant, Modality::combine);
    }

    @Override
    protected Modality visit(LiteralInfinity expression) {
        return Modality.Constant;
    }

    @Override
    protected Modality visit(LiteralReal expression) {
        return Modality.Constant;
    }

    @Override
    protected Modality visit(LiteralInteger expression) {
        return Modality.Constant;
    }

    @Override
    protected Modality visit(LiteralString expression) {
        return Modality.Constant;
    }

    @Override
    protected Modality visit(LiteralBoolean expression) {
        return Modality.Constant;
    }

    @Override
    protected Modality visit(LiteralNothing expression) {
        return Modality.Constant;
    }

    @Override
    protected Modality visit(ElementReference expression) {
        var element = expression.getElement();
        return getModality(element);
    }

    private Modality getModality(NamedElement element) {
        return switch (element) {
            case EnumLiteral ignored -> Modality.Constant;
            case FeatureDeclaration ignored -> Modality.Static;
            case VariableDeclaration ignored -> Modality.Dynamic;
            default -> throw new IllegalArgumentException("This is a malformed expression!");
        };
    }

    @Override
    protected Modality visit(SelfReference expression) {
        return Modality.Static;
    }

    @Override
    protected Modality visit(NavigationSuffixExpression expression) {
        return evaluate(expression.getPrimary()).combine(getModality(expression.getMember()));
    }

    @Override
    protected Modality visit(CallSuffixExpression expression) {
        return Modality.Static; // TODO: may be constant at times
    }

    @Override
    protected Modality visit(IndexingSuffixExpression expression) {
        return evaluate(expression.getPrimary()).combine(evaluate(expression.getIndex()));
    }
}
