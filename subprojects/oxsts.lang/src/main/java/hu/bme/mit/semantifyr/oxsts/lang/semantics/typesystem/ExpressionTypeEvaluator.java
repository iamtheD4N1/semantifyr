/*
 * SPDX-FileCopyrightText: 2025 The Semantifyr Authors
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package hu.bme.mit.semantifyr.oxsts.lang.semantics.typesystem;

import com.google.inject.Inject;
import hu.bme.mit.semantifyr.oxsts.lang.library.builtin.BuiltinSymbolResolver;
import hu.bme.mit.semantifyr.oxsts.lang.semantics.expression.ArrayEvaluation;
import hu.bme.mit.semantifyr.oxsts.lang.semantics.expression.ExpressionEvaluation;
import hu.bme.mit.semantifyr.oxsts.lang.semantics.expression.ExpressionEvaluator;
import hu.bme.mit.semantifyr.oxsts.model.oxsts.*;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.validation.CheckType;
import org.eclipse.xtext.validation.FeatureBasedDiagnostic;

import java.util.ArrayList;
import java.util.List;

public class ExpressionTypeEvaluator extends ExpressionEvaluator<TypeEvaluation> {

    @Inject
    private VariableTypeEvaluator variableTypeEvaluator;

    @Inject
    protected BuiltinSymbolResolver builtinSymbolResolver;

    @Inject
    protected TypeCompatibility typeCompatibility;

    @Override
    protected TypeEvaluation visit(RangeExpression expression) {
        return InvalidTypeEvaluation.INSTANCE;
    }


    private static final String INCORRECT_TYPE_ERROR = "INCORRECT_TYPE";
    private static final String INCORRECT_TYPE_ERROR_MESSAGE = "This expression contains an incompatible type";
    private static final String CONSTANT_EXPRESSION_WARNING_MESSAGE = "This is a constant expression.";

    private final List<FeatureBasedDiagnostic> diagnosticList = new ArrayList<>();

    public List<FeatureBasedDiagnostic> checkTypes(OxstsModelPackage oxstsModelPackage) {
        var expressions = EcoreUtil2.eAllOfType(oxstsModelPackage, Expression.class);
        for (var expr : expressions) {
            evaluate(expr);
        }

        return diagnosticList;
    }

    @Override
    protected TypeEvaluation visit(ComparisonOperator expression) {
        var left = evaluate(expression.getLeft());
        var right = evaluate(expression.getRight());

        if (left instanceof InvalidTypeEvaluation) {
            return InvalidTypeEvaluation.INSTANCE;
        }
        if (right instanceof InvalidTypeEvaluation) {
            return InvalidTypeEvaluation.INSTANCE;
        }

        if (expression.getOp() == ComparisonOp.EQ ||
            expression.getOp() == ComparisonOp.NOT_EQ) {
            if (typeCompatibility.isAssignable(left, right, expression)
                    || typeCompatibility.isAssignable(right, left, expression)) {
                return new ImmutableTypeEvaluation(builtinSymbolResolver.boolDatatype(expression));
            }
        }
        else {
            if (
                left instanceof ImmutableTypeEvaluation(DomainDeclaration leftdomain)
                && right instanceof ImmutableTypeEvaluation(DomainDeclaration rightdomain)
            ) {
                if(
                    leftdomain == builtinSymbolResolver.intDatatype(expression)
                    && rightdomain == builtinSymbolResolver.intDatatype(expression)
                ) {
                    return new ImmutableTypeEvaluation(builtinSymbolResolver.boolDatatype(expression));
                }
            }
        }
        diagnosticList.add(new FeatureBasedDiagnostic(
                Diagnostic.ERROR,
                "Unknown type",
                expression,
                null,
                0,
                CheckType.EXPENSIVE,
                INCORRECT_TYPE_ERROR
        ));
        return InvalidTypeEvaluation.INSTANCE;
    }

    @Override
    protected TypeEvaluation visit(ArithmeticBinaryOperator expression) {
        var left = evaluate(expression.getLeft());
        var right = evaluate(expression.getRight());

        if (left instanceof InvalidTypeEvaluation) {
            return InvalidTypeEvaluation.INSTANCE;
        }
        if (right instanceof InvalidTypeEvaluation) {
            return InvalidTypeEvaluation.INSTANCE;
        }
        if (
            left instanceof ImmutableTypeEvaluation(DomainDeclaration leftdomain)
            && right instanceof ImmutableTypeEvaluation(DomainDeclaration rightdomain)
        ) {
            if(
                leftdomain == builtinSymbolResolver.intDatatype(expression)
                && rightdomain == builtinSymbolResolver.intDatatype(expression)
            ) {
                return new ImmutableTypeEvaluation(builtinSymbolResolver.intDatatype(expression));
            }
        }
        diagnosticList.add(new FeatureBasedDiagnostic(
                Diagnostic.ERROR,
                "Unknown type",
                expression,
                null,
                0,
                CheckType.EXPENSIVE,
                INCORRECT_TYPE_ERROR
        ));
        return InvalidTypeEvaluation.INSTANCE;
    }

    @Override
    protected TypeEvaluation visit(BooleanOperator expression) {
        var left = evaluate(expression.getLeft());
        var right = evaluate(expression.getRight());

        if (left instanceof InvalidTypeEvaluation) {
            return InvalidTypeEvaluation.INSTANCE;
        }
        if (right instanceof InvalidTypeEvaluation) {
            return InvalidTypeEvaluation.INSTANCE;
        }
        if (
            left instanceof ImmutableTypeEvaluation(DomainDeclaration leftdomain)
            && right instanceof ImmutableTypeEvaluation(DomainDeclaration rightdomain)
        ) {
            if(
                leftdomain == builtinSymbolResolver.boolDatatype(expression)
                && rightdomain == builtinSymbolResolver.boolDatatype(expression)
            ) {
                return new ImmutableTypeEvaluation(builtinSymbolResolver.boolDatatype(expression));
            }
        }
        diagnosticList.add(new FeatureBasedDiagnostic(
                Diagnostic.ERROR,
                "Unknown type",
                expression,
                null,
                0,
                CheckType.EXPENSIVE,
                INCORRECT_TYPE_ERROR
        ));
        return InvalidTypeEvaluation.INSTANCE;
    }

    @Override
    protected TypeEvaluation visit(ArithmeticUnaryOperator expression) {
        var body = evaluate(expression.getBody());

        if (body instanceof InvalidTypeEvaluation) {
            return InvalidTypeEvaluation.INSTANCE;
        }
        if (
            body instanceof ImmutableTypeEvaluation(DomainDeclaration bodydomain)
        ) {
            if(
                bodydomain == builtinSymbolResolver.intDatatype(expression)
            ) {
                return new ImmutableTypeEvaluation(builtinSymbolResolver.intDatatype(expression));
            }
        }
        diagnosticList.add(new FeatureBasedDiagnostic(
                Diagnostic.ERROR,
                "Unknown type",
                expression,
                null,
                0,
                CheckType.EXPENSIVE,
                INCORRECT_TYPE_ERROR
        ));
        return InvalidTypeEvaluation.INSTANCE;
    }

    @Override
    protected TypeEvaluation visit(NegationOperator expression) {
        var body = evaluate(expression.getBody());

        if (body instanceof InvalidTypeEvaluation) {
            return InvalidTypeEvaluation.INSTANCE;
        }
        if (
            body instanceof ImmutableTypeEvaluation(DomainDeclaration bodydomain)
        ) {
            if(
                bodydomain == builtinSymbolResolver.boolDatatype(expression)
            ) {
                return new ImmutableTypeEvaluation(builtinSymbolResolver.boolDatatype(expression));
            }
        }
        diagnosticList.add(new FeatureBasedDiagnostic(
                Diagnostic.ERROR,
                "Unknown type",
                expression,
                null,
                0,
                CheckType.EXPENSIVE,
                INCORRECT_TYPE_ERROR
        ));
        return InvalidTypeEvaluation.INSTANCE;
    }

    @Override
    protected TypeEvaluation visit(ArrayLiteral expression) {
        TypeEvaluation elementType = visit(expression.getValues().getFirst());
        for (var element : expression.getValues()) {
            if (!typeCompatibility.isAssignable(visit(element), elementType, expression)) {
                diagnosticList.add(new FeatureBasedDiagnostic(
                        Diagnostic.ERROR,
                        "Elements incompatible",
                        element,
                        null,
                        0,
                        CheckType.EXPENSIVE,
                        INCORRECT_TYPE_ERROR
                ));
                return InvalidTypeEvaluation.INSTANCE;
            }
        }
        return new ArrayTypeEvaluation(elementType);
    }

    @Override
    protected TypeEvaluation visit(LiteralInfinity expression) {
        return InvalidTypeEvaluation.INSTANCE;
    }

    @Override
    protected TypeEvaluation visit(LiteralReal expression) {
        return new ImmutableTypeEvaluation(builtinSymbolResolver.realDatatype(expression));
    }

    @Override
    protected TypeEvaluation visit(LiteralInteger expression) {
        return new ImmutableTypeEvaluation(builtinSymbolResolver.intDatatype(expression));
    }

    @Override
    protected TypeEvaluation visit(LiteralString expression) {
        return new ImmutableTypeEvaluation(builtinSymbolResolver.stringDatatype(expression));
    }

    @Override
    protected TypeEvaluation visit(LiteralBoolean expression) {
        return new ImmutableTypeEvaluation(builtinSymbolResolver.boolDatatype(expression));
    }

    @Override
    protected TypeEvaluation visit(LiteralNothing expression) {
        // Ha nem adunk vissza errort, akkor a későbbiekben azt hiszi a rendszer, hogy mar lekezeltuk
        // IGNORE
        // TODO: Should be refactored with a ignored type
        return InvalidTypeEvaluation.INSTANCE;
    }

    @Override
    protected TypeEvaluation visit(ElementReference expression) {
        var referencedElement = expression.getElement();

        if (referencedElement.eIsProxy()) {
            throw new IllegalStateException("Element could not be resolved!");
        }

        // TODO: add validation diagnostic
        return switch (referencedElement) {
            case VariableDeclaration variableDeclaration -> variableTypeEvaluator.evaluate(variableDeclaration);
            case DomainDeclaration domainDeclaration -> new ImmutableTypeEvaluation(domainDeclaration);
            case ParameterDeclaration parameterDeclaration -> new ImmutableTypeEvaluation(parameterDeclaration.getType());
            default -> throw new IllegalStateException("Unexpected value: " + referencedElement);
        };
    }

    @Override
    protected TypeEvaluation visit(SelfReference expression) {
        var classDeclaration = expression.getClass_();

        if (classDeclaration == null) {
            classDeclaration = EcoreUtil2.getContainerOfType(expression, ClassDeclaration.class);
        }

        if (classDeclaration == null) {
            // TODO: add validation diagnostic
            return InvalidTypeEvaluation.INSTANCE;
        }

        if (classDeclaration.eIsProxy()) {
            throw new IllegalStateException("Class declaration could not be resolved!");
        }

        return new ImmutableTypeEvaluation(classDeclaration);
    }

    @Override
    protected TypeEvaluation visit(NavigationSuffixExpression expression) {
        var member = expression.getMember();

        if (member.eIsProxy()) {
            throw new IllegalStateException("NavigationSuffix.member could not be resolved!");
        }

        return switch (member) {
            case VariableDeclaration variableDeclaration -> variableTypeEvaluator.evaluate(variableDeclaration);
            case DomainDeclaration domainDeclaration -> new ImmutableTypeEvaluation(domainDeclaration);
            default -> throw new IllegalStateException("Unexpected value: " + member);
        };
    }

    @Override
    protected TypeEvaluation visit(CallSuffixExpression expression) {
        // TODO: should compute the called type, and then compute its return type
        return InvalidTypeEvaluation.INSTANCE;
    }

    @Override
    protected TypeEvaluation visit(IndexingSuffixExpression expression) {
        return evaluate(expression.getPrimary());
    }
}
