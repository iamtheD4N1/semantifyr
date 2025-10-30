/*
 * SPDX-FileCopyrightText: 2025 The Semantifyr Authors
 *
 * SPDX-License-Identifier: EPL-2.0
 *//*


package hu.bme.mit.semantifyr.oxsts.lang.typesystem;

import hu.bme.mit.semantifyr.oxsts.model.oxsts.*;
import hu.bme.mit.semantifyr.oxsts.model.oxsts.Package;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.validation.CheckType;
import org.eclipse.xtext.validation.FeatureBasedDiagnostic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionTypeEvaluator {

    private static final String INCORRECT_TYPE_ERROR = "INCORRECT_TYPE";
    private static final String INCORRECT_TYPE_ERROR_MESSAGE = "This expression contains an incompatible type";
    private static final String CONSTANT_EXPRESSION_WARNING = "CONSTANT_EXPRESSION";
    private static final String CONSTANT_EXPRESSION_WARNING_MESSAGE = "This is a constant expression.";
    private static final String STATIC_EXPRESSION_WARNING = "STATIC_EXPRESSION";
    private static final String STATIC_EXPRESSION_WARNING_MESSAGE = "This evaulation always has the same result.";


    public EvalType evaluateType(Expression expression) {
        if(!expressionEvalTypeMap.containsKey(expression))
            expressionEvalTypeMap.put(expression, computeType(expression));
        return expressionEvalTypeMap.get(expression);
    }

    protected EvalType computeType(Expression expression) {
        return switch (expression) {
            case OperatorExpression operatorExpression -> computeType(operatorExpression);
            case ReferenceExpression referenceExpression -> computeType(referenceExpression);
            case LiteralExpression literalExpression -> computeType(literalExpression);
            default -> new InvalidType();
        };
    }

    protected EvalType computeType(OperatorExpression operatorExpression) {
        return switch (operatorExpression) {
            case UnaryOperator unaryOperator -> new InvalidType();
            case ArithmeticBinaryOperator arithmeticBinaryOperator -> computeType(arithmeticBinaryOperator);
            case ComparisonOperator comparisonOperator -> computeType(comparisonOperator);
            case BooleanOperator booleanOperator -> computeType(booleanOperator);
            default -> new InvalidType();
        };
    }

    protected EvalType computeType(ArithmeticBinaryOperator arithmeticBinaryOperator) {
        var lType = evaluateType(arithmeticBinaryOperator.getLeft());
        var rType = evaluateType(arithmeticBinaryOperator.getRight());

        if (expectType(arithmeticBinaryOperator.getLeft(), lType, IntegerDataType.class) &&
                expectType(arithmeticBinaryOperator.getRight(), rType, IntegerDataType.class)) {
            Modality newmod = lType.modality.combine(rType.modality);
            if (newmod == Modality.Constant)
                return new IntegerDataType(Modality.Constant);
            if (arithmeticBinaryOperator.getLeft() instanceof ReferenceExpression &&
                    arithmeticBinaryOperator.getRight() instanceof ReferenceExpression) {
                if (getReference((ReferenceExpression) arithmeticBinaryOperator.getLeft()).equals(
                        getReference((ReferenceExpression) arithmeticBinaryOperator.getRight())))
                    return new IntegerDataType(Modality.Static);
            }
            return new IntegerDataType(newmod);
        }
        return new InvalidType();
    }

    protected EvalType computeType(ComparisonOperator comparisonOperator) {
        var lType = evaluateType(comparisonOperator.getLeft());
        var rType = evaluateType(comparisonOperator.getRight());

        if (expectType(comparisonOperator.getLeft(), lType, IntegerDataType.class) &&
                expectType(comparisonOperator.getRight(), rType, IntegerDataType.class)) {
            Modality newmod = lType.modality.combine(rType.modality);
            if (newmod == Modality.Constant)
                return new BooleanDataType(Modality.Constant);
            if (comparisonOperator.getLeft() instanceof ReferenceExpression &&
                    comparisonOperator.getRight() instanceof ReferenceExpression) {
                if (getReference((ReferenceExpression) comparisonOperator.getLeft()).equals(
                        getReference((ReferenceExpression) comparisonOperator.getRight())))
                    return new BooleanDataType(Modality.Static);
            }
            return new BooleanDataType(newmod);
        }
        return new InvalidType();
    }

    protected EvalType computeType(BooleanOperator booleanOperator) {
        var lType = evaluateType(booleanOperator.getLeft());
        var rType = evaluateType(booleanOperator.getRight());

        if (expectType(booleanOperator.getLeft(), lType, BooleanDataType.class) &&
                expectType(booleanOperator.getRight(), rType, BooleanDataType.class)) {
            Modality newmod = lType.modality.combine(rType.modality);
            if(newmod == Modality.Constant)
                return new BooleanDataType(Modality.Constant);
            if(booleanOperator.getLeft() instanceof ReferenceExpression &&
                    booleanOperator.getRight() instanceof ReferenceExpression) {
                if(getReference((ReferenceExpression)booleanOperator.getLeft()).equals(
                        getReference((ReferenceExpression)booleanOperator.getRight())))
                    return new BooleanDataType(Modality.Static);
            }
            return new BooleanDataType(nwmod);
        }
        return new InvalidType();
    }


    protected EvalType computeType(ReferenceExpression referenceExpression) {
        if (referenceExpression instanceof ContextualReference) {
            return new InvalidType();
        }

        if (referenceExpression instanceof ChainingExpression chainingExpression) {
            var chains = chainingExpression.getChains();
            var referencedElement = chains.getLast().getElement();

            // what kind of element... Feature, Variable, Argument
            switch (referencedElement) {
                case Variable variable:
                    return variableEvalTypeMap.get(variable);
                // case Feature feature...
                default:
                    return new InvalidType();
            }
        }
        return new InvalidType();
    }

    protected EvalType computeType(LiteralExpression literalExpression) {
        switch (literalExpression) {
            case LiteralInteger literalInteger:
                return new IntegerDataType(Modality.Constant);
            case LiteralBoolean literalBoolean:
                return new BooleanDataType(Modality.Constant);
            default:
                return new InvalidType();
        }
    }


    protected boolean expectType(Expression expression, EvalType actual, Class<?> expected) {
        if (actual instanceof InvalidType)
            return false;

        if (expected.isInstance(actual))
            return true;

        diagnosticList.add(new FeatureBasedDiagnostic(
                Diagnostic.ERROR,
                INCORRECT_TYPE_ERROR_MESSAGE,
                expression,
                null,
                0,
                CheckType.EXPENSIVE,
                INCORRECT_TYPE_ERROR
        ));

        return false;
    }


    public void checkFeatureSubsetting(Feature feature) {
        if (feature.getSubsets().isEmpty()) return;

        var featureTyping = feature.getTyping();
        var subsettingTyping = feature.getSubsets().stream().findFirst().get().getTyping();

        if (featureTyping instanceof IntegerType) {
            if (!(subsettingTyping instanceof IntegerType)) {
                diagnosticList.add(new FeatureBasedDiagnostic(
                        Diagnostic.ERROR,
                        "Feature must have type that is compatible with subsetted feature",
                        feature,
                        null,
                        0,
                        CheckType.EXPENSIVE,
                        INCORRECT_TYPE_ERROR
                ));
            } else {
                return;
            }
        } else if (featureTyping instanceof BooleanType) {
            if (!(subsettingTyping instanceof BooleanType)) {
                diagnosticList.add(new FeatureBasedDiagnostic(
                        Diagnostic.ERROR,
                        "Feature must have type that is compatible with subsetted feature",
                        feature,
                        null,
                        0,
                        CheckType.EXPENSIVE,
                        INCORRECT_TYPE_ERROR
                ));
            } else {
                return;
            }
        }

        var featureType = getType(featureTyping);
        var subsettingType = getType(subsettingTyping);

        if (!isSupertypeOf(subsettingType, featureType)) {
            diagnosticList.add(new FeatureBasedDiagnostic(
                    Diagnostic.ERROR,
                    "Feature must have type that is compatible with subsetted feature",
                    feature,
                    null,
                    0,
                    CheckType.EXPENSIVE,
                    INCORRECT_TYPE_ERROR
            ));
        }
    }


    public void checkFeatureRedefinition(Feature feature) {
        if (feature.getRedefines() == null) return;

        var featureTyping = feature.getTyping();
        var redefinedTyping = feature.getRedefines().getTyping();

        if (featureTyping instanceof IntegerType) {
            if (!(redefinedTyping instanceof IntegerType)) {
                diagnosticList.add(new FeatureBasedDiagnostic(
                        Diagnostic.ERROR,
                        "Feature must have type that is compatible with redefined feature",
                        feature,
                        null,
                        0,
                        CheckType.EXPENSIVE,
                        INCORRECT_TYPE_ERROR
                ));
            } else {
                return;
            }
        } else if (featureTyping instanceof BooleanType) {
            if (!(redefinedTyping instanceof BooleanType)) {
                diagnosticList.add(new FeatureBasedDiagnostic(
                        Diagnostic.ERROR,
                        "Feature must have type that is compatible with redefined feature",
                        feature,
                        null,
                        0,
                        CheckType.EXPENSIVE,
                        INCORRECT_TYPE_ERROR
                ));
            } else {
                return;
            }
        }

        var featureType = getType(featureTyping);
        var redefinedType = getType(redefinedTyping);

        if (!isSupertypeOf(redefinedType, featureType)) {
            diagnosticList.add(new FeatureBasedDiagnostic(
                    Diagnostic.ERROR,
                    "Feature must have type that is compatible with redefined feature",
                    feature,
                    null,
                    0,
                    CheckType.EXPENSIVE,
                    INCORRECT_TYPE_ERROR
            ));
        }
    }


    public void checkCompositeTransitionInlining(InlineComposite operation) {
        var feature = (Feature) getReference(operation.getReference());
        var multiplicity = feature.getMultiplicity();

//        if (multiplicity instanceof OneMultiplicity || multiplicity instanceof OptionalMultiplicity) {
//            error("Composite inlining must reference feature with many multiplicity",
//                    OxstsPackage.Literals.INLINE_COMPOSITE__FEATURE,
//                    INVALID_MULTIPLICITIY);
//        }
//    }
*/
/*
    private Type getType(Typing typing) {
        if (typing instanceof ReferenceTyping referenceTyping) {
            return (Type) referenceTyping.getReference().getChains().getLast().getElement();
        }

        throw new RuntimeException("Typing is of incorrect form!");
    }

    private boolean isSupertypeOf(Type superType, Type type) {
        if (type == null) return false;

        if (type == superType) {
            return true;
        }

        return isSupertypeOf(superType, type.getSupertype());
    }

    private Element getReference(ReferenceExpression reference) {
        if (reference instanceof ChainingExpression chainReference) {
            return chainReference.getChains().getLast().getElement();
        }

        throw new RuntimeException("Reference expression is of incorrect form!");
    }
    /*
}
*/
