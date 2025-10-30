/*
 * SPDX-FileCopyrightText: 2025 The Semantifyr Authors
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package hu.bme.mit.semantifyr.oxsts.lang.semantics.typesystem;

import com.google.inject.Inject;
import hu.bme.mit.semantifyr.oxsts.lang.library.builtin.BuiltinSymbolResolver;
import hu.bme.mit.semantifyr.oxsts.lang.semantics.expression.ExpressionEvaluator;
import hu.bme.mit.semantifyr.oxsts.model.oxsts.*;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.validation.FeatureBasedDiagnostic;

import java.util.ArrayList;
import java.util.List;

public class ExpressionTypeEvaluator extends ExpressionEvaluator<TypeEvaluation> {

    @Inject
    private VariableTypeEvaluator variableTypeEvaluator;

    @Inject
    protected BuiltinSymbolResolver builtinSymbolResolver;

    @Override
    protected TypeEvaluation visit(RangeExpression expression) {
        return TypeEvaluation.INVALID;
    }


    private static final String INCORRECT_TYPE_ERROR = "INCORRECT_TYPE";
    private static final String INCORRECT_TYPE_ERROR_MESSAGE = "This expression contains an incompatible type";
    private static final String CONSTANT_EXPRESSION_WARNING = "CONSTANT_EXPRESSION";
    private static final String CONSTANT_EXPRESSION_WARNING_MESSAGE = "This is a constant expression.";
    private static final String STATIC_EXPRESSION_WARNING = "STATIC_EXPRESSION";
    private static final String STATIC_EXPRESSION_WARNING_MESSAGE = "This evaulation always has the same result.";

    private final List<FeatureBasedDiagnostic> diagnosticList = new ArrayList<>();

    public List<FeatureBasedDiagnostic> checkTypes(OxstsModelPackage oxstsModelPackage) {
        var assignments = EcoreUtil2.eAllOfType(oxstsModelPackage, AssignmentOperation.class);
        var variables = EcoreUtil2.eAllOfType(oxstsModelPackage, VariableDeclaration.class);
        var expressions = EcoreUtil2.eAllOfType(oxstsModelPackage, Expression.class);

        for (var var : variables) {
            checkVariable(var);
        }
        for (var ass : assignments) {
            checkAssigsment(ass);
        }
        for (var expr : expressions) {
            evaluate(expr);
        }
        /*
        for(var key : expressionEvalTypeMap.keySet()){
            var type = expressionEvalTypeMap.get(key);
            if(type.modality == Modality.Constant && !(key instanceof LiteralExpression))
                diagnosticList.add(new FeatureBasedDiagnostic(
                        Diagnostic.WARNING,
                        CONSTANT_EXPRESSION_WARNING_MESSAGE,
                        key,
                        null,
                        0,
                        CheckType.EXPENSIVE,
                        CONSTANT_EXPRESSION_WARNING
                ));
            else if(type.modality == Modality.Static){
                diagnosticList.add(new FeatureBasedDiagnostic(
                        Diagnostic.WARNING,
                        STATIC_EXPRESSION_WARNING_MESSAGE,
                        key,
                        null,
                        0,
                        CheckType.EXPENSIVE,
                        STATIC_EXPRESSION_WARNING
                ));
            }
        }
        */
        return diagnosticList;
    }

    private void checkAssigsment(AssignmentOperation assignmentOperation) {
        /*var expression = assignmentOperation.getExpression();
        var exprType = evaluateType(expression);
        var varType = evaluateType(assignmentOperation.getReference());
        var variable = assignmentOperation.getReference().getChains().getLast().getElement();
        variableEvalTypeMap.get(variable).modality = Modality.Dynamic;

        switch (varType) {
            case IntegerDataType integerDataType:
                expectType(expression, exprType, IntegerDataType.class);
                return;
            case BooleanDataType booleanDataType:
                expectType(expression, exprType, BooleanDataType.class);
                return;
            default:
                diagnosticList.add(new FeatureBasedDiagnostic(
                        Diagnostic.ERROR,
                        "Unknown type",
                        expression,
                        null,
                        0,
                        CheckType.EXPENSIVE,
                        INCORRECT_TYPE_ERROR
                ));
        }*/
    }

    private void checkVariable(VariableDeclaration variable) {
        /*
        var expression = variable.getExpression();
        var varType = variable.getTyping();

        if (expression == null) {
            switch (varType) {
                case IntegerType integerType:
                    variableEvalTypeMap.put(variable, new IntegerDataType(Modality.Constant));
                    return;
                case BooleanType booleanType:
                    variableEvalTypeMap.put(variable, new BooleanDataType(Modality.Constant));
                    return;
                default:
                    diagnosticList.add(new FeatureBasedDiagnostic(
                            Diagnostic.ERROR,
                            "Unknown type",
                            variable,
                            null,
                            0,
                            CheckType.EXPENSIVE,
                            INCORRECT_TYPE_ERROR
                    ));
            }
        };

        var exprType = evaluateType(expression);

        switch (varType) {
            case IntegerType integerType:
                expectType(expression, exprType, IntegerDataType.class);
                variableEvalTypeMap.put(variable, exprType);
                return;
            case BooleanType booleanType:
                expectType(expression, exprType, BooleanDataType.class);
                variableEvalTypeMap.put(variable, exprType);
                return;
            default:
                diagnosticList.add(new FeatureBasedDiagnostic(
                        Diagnostic.ERROR,
                        "Unknown type",
                        expression,
                        null,
                        0,
                        CheckType.EXPENSIVE,
                        INCORRECT_TYPE_ERROR
                ));
        }
         */
    }

    @Override
    protected TypeEvaluation visit(ComparisonOperator expression) {
        var left = evaluate(expression.getLeft());
        var right = evaluate(expression.getRight());

        if (left instanceof InvalidTypeEvaluation) {
            return TypeEvaluation.INVALID;
        }
        if (right instanceof InvalidTypeEvaluation) {
            return TypeEvaluation.INVALID;
        }
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
        return TypeEvaluation.INVALID;
    }

    @Override
    protected TypeEvaluation visit(ArithmeticBinaryOperator expression) {
        return TypeEvaluation.INVALID;
    }

    @Override
    protected TypeEvaluation visit(BooleanOperator expression) {
        return TypeEvaluation.INVALID;
    }

    @Override
    protected TypeEvaluation visit(ArithmeticUnaryOperator expression) {
        return TypeEvaluation.INVALID;
    }

    @Override
    protected TypeEvaluation visit(NegationOperator expression) {
        return TypeEvaluation.INVALID;
    }

    @Override
    protected TypeEvaluation visit(ArrayLiteral expression) {
        return TypeEvaluation.INVALID;
    }

    @Override
    protected TypeEvaluation visit(LiteralInfinity expression) {
        return TypeEvaluation.INVALID;
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
        return TypeEvaluation.INVALID;
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
            return TypeEvaluation.INVALID;
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
        return TypeEvaluation.INVALID;
    }

    @Override
    protected TypeEvaluation visit(IndexingSuffixExpression expression) {
        return evaluate(expression.getPrimary());
    }

}
