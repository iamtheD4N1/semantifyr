/*
 * SPDX-FileCopyrightText: 2025 The Semantifyr Authors
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package hu.bme.mit.semantifyr.oxsts.lang.semantics.typesystem;

import hu.bme.mit.semantifyr.oxsts.lang.semantics.expression.ExpressionEvaluator;
import hu.bme.mit.semantifyr.oxsts.model.oxsts.*;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.CheckType;
import org.eclipse.xtext.validation.FeatureBasedDiagnostic;

import java.util.ArrayList;
import java.util.List;

public class ExpressionModalityEvaluator extends ExpressionEvaluator<Modality> {
    private final List<FeatureBasedDiagnostic> diagnosticList = new ArrayList<>();

    private static final String INCORRECT_MODALITY_ERROR = "INCORRECT_MODALITY";

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

    public List<FeatureBasedDiagnostic> checkModalities(OxstsModelPackage oxstsModelPackage) {

        var inlineIfs = EcoreUtil2.eAllOfType(oxstsModelPackage, InlineIfOperation.class);
        var inlineFors = EcoreUtil2.eAllOfType(oxstsModelPackage, InlineForOperation.class);
        var varDeclarations = EcoreUtil2.eAllOfType(oxstsModelPackage, VariableDeclaration.class);
        var featureDeclarations = EcoreUtil2.eAllOfType(oxstsModelPackage, FeatureDeclaration.class);

        for (var inlineIf : inlineIfs) {
            check(inlineIf);
        }
        for (var inlineFor : inlineFors) {
            check(inlineFor);
        }
        for (var varDeclaration : varDeclarations) {
            check(varDeclaration);
        }
        for (var featureDeclaration : featureDeclarations) {
            check(featureDeclaration);
        }

        return diagnosticList;
    }

    @Check
    protected Modality check(InlineIfOperation expression) {
        if (visit(expression.getGuard()) == Modality.Dynamic) {
            diagnosticList.add(new FeatureBasedDiagnostic(
                    Diagnostic.ERROR,
                    "Inline if guard required to be at least static!",
                    expression,
                    null,
                    0,
                    CheckType.EXPENSIVE,
                    INCORRECT_MODALITY_ERROR
            ));
        }

        return Modality.Static;
    }

    @Check
    protected Modality check(InlineForOperation expression) {
        if (visit(expression.getRangeExpression()) == Modality.Dynamic) {
            diagnosticList.add(new FeatureBasedDiagnostic(
                    Diagnostic.ERROR,
                    "Inline for range required to be at least static!",
                    expression,
                    null,
                    0,
                    CheckType.EXPENSIVE,
                    INCORRECT_MODALITY_ERROR
            ));
        }

        return Modality.Static;
    }

    // not sure of this
    @Check
    protected Modality check(VariableDeclaration expression) {
        if (visit(expression.getExpression()) == Modality.Dynamic) {
            diagnosticList.add(new FeatureBasedDiagnostic(
                    Diagnostic.ERROR,
                    "Variable declaration value required to be at least static!",
                    expression,
                    null,
                    0,
                    CheckType.EXPENSIVE,
                    INCORRECT_MODALITY_ERROR
            ));
        }

        return Modality.Static;
    }

    @Check
    protected Modality check(FeatureDeclaration expression) {
        if (visit(expression.getExpression()) == Modality.Dynamic) {
            diagnosticList.add(new FeatureBasedDiagnostic(
                    Diagnostic.ERROR,
                    "Feature declaration value required to be at least static!",
                    expression,
                    null,
                    0,
                    CheckType.EXPENSIVE,
                    INCORRECT_MODALITY_ERROR
            ));
        }

        return Modality.Static;
    }
}
