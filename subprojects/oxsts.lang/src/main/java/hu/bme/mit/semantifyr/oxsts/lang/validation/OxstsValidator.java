/*
 * SPDX-FileCopyrightText: 2023-2025 The Semantifyr Authors
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package hu.bme.mit.semantifyr.oxsts.lang.validation;

import com.google.inject.Inject;
import hu.bme.mit.semantifyr.oxsts.lang.library.builtin.BuiltinSymbolResolver;
import hu.bme.mit.semantifyr.oxsts.lang.naming.NamingUtil;
import hu.bme.mit.semantifyr.oxsts.lang.semantics.typesystem.*;
import hu.bme.mit.semantifyr.oxsts.model.oxsts.*;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.resource.ILocationInFileProvider;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.xtext.validation.Check;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class contains custom validation rules.
 * <p>
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
@SuppressWarnings("unused") // check functions are used by reflection
public class OxstsValidator extends AbstractOxstsValidator {

    @Inject
    private ILocationInFileProvider locationInFileProvider;

    private static final String ISSUE_PREFIX = "hu.bme.mit.semantifyr.oxsts.lang.validation.OxstsValidator.";
    public static final String DUPLICATE_NAME_ISSUE = ISSUE_PREFIX + "DUPLICATE_NAME";
    public static final String DYNAMIC_INLINE_IF_GUARD_ISSUE = ISSUE_PREFIX + "DYNAMIC_INLINE_IF";
    public static final String STATIC_IF_GUARD_ISSUE = ISSUE_PREFIX + "DYNAMIC_INLINE_IF";
    public static final String DATA_TYPE_NOT_IN_BUILTIN_ISSUE = ISSUE_PREFIX + "DATA_TYPE_NOT_IN_BUILTIN";
    public static final String INCORRECT_MODALITY_ERROR = ISSUE_PREFIX + "INCORRECT_MODALITY";
    public static final String INVALID_TYPE_ERROR = ISSUE_PREFIX + "INVALID_TYPE";

    @Inject
    protected BuiltinSymbolResolver builtinSymbolResolver;

    @Inject
    private ExpressionTypeEvaluatorProvider expressionTypeEvaluatorProvider;

    @Inject
    private ExpressionModalityEvaluatorProvider expressionModalityEvaluatorProvider;

    @Inject
    protected TypeCompatibility typeCompatibility;

    @Check
    public void checkTypes(OxstsModelPackage oxstsModelPackage) {
        var evaluator = expressionTypeEvaluatorProvider.getEvaluator(oxstsModelPackage);
        var diagnostics = evaluator.checkTypes(oxstsModelPackage);

        for (var diagnostic : diagnostics) {
            switch (diagnostic.getSeverity()) {
                case Diagnostic.INFO -> info(diagnostic.getMessage(), diagnostic.getSourceEObject(),
                        diagnostic.getFeature(), diagnostic.getIndex(), diagnostic.getIssueCode(),
                        diagnostic.getIssueData());
                case Diagnostic.WARNING -> warning(diagnostic.getMessage(), diagnostic.getSourceEObject(),
                        diagnostic.getFeature(), diagnostic.getIndex(), diagnostic.getIssueCode(),
                        diagnostic.getIssueData());
                case Diagnostic.ERROR -> error(diagnostic.getMessage(), diagnostic.getSourceEObject(),
                        diagnostic.getFeature(), diagnostic.getIndex(), diagnostic.getIssueCode(),
                        diagnostic.getIssueData());
                default -> throw new IllegalStateException("Unknown severity %s of %s"
                        .formatted(diagnostic.getSeverity(), diagnostic));
            }
        }
    }

    @Override
    protected void handleExceptionDuringValidation(Throwable targetException) throws RuntimeException {
        // swallow all exceptions!
    }

    @Check
    public void checkDataTypeOnlyInBuiltin(DataTypeDeclaration dataTypeDeclaration) {
        if (! builtinSymbolResolver.isBuiltin(dataTypeDeclaration)) {
            var message = "Custom data types are not allowed!";
            acceptError(message, dataTypeDeclaration, DATA_TYPE_NOT_IN_BUILTIN_ISSUE);
        }
    }

    @Check
    public void checkUniqueDeclarations(OxstsModelPackage oxstsPackage) {
        checkUniqueSimpleNames(oxstsPackage.getDeclarations());
    }

    @Check
    public void checkUniqueEnumLiterals(EnumDeclaration enumDeclaration) {
        checkUniqueSimpleNames(enumDeclaration.getLiterals());
    }

    @Check
    public void checkUniqueMembers(RecordDeclaration recordDeclaration) {
        checkUniqueSimpleNames(recordDeclaration.getMembers());
    }

    @Check
    public void checkUniqueMembers(ClassDeclaration classDeclaration) {
        checkUniqueSimpleNames(classDeclaration.getMembers());
    }

    @Check
    public void checkUniqueMembers(FeatureDeclaration featureDeclaration) {
        checkUniqueSimpleNames(featureDeclaration.getInnerFeatures());
    }

    protected void checkUniqueSimpleNames(Iterable<? extends NamedElement> namedElements) {
        var names = new LinkedHashMap<String, Set<NamedElement>>();
        for (var namedElement : namedElements) {
            var name = NamingUtil.getName(namedElement);
            var elementsWithName = names.computeIfAbsent(name, ignored -> new LinkedHashSet<>());
            elementsWithName.add(namedElement);
        }
        for (var entry : names.entrySet()) {
            var elementsWithName = entry.getValue();
            if (elementsWithName.size() <= 1) {
                continue;
            }
            var name = entry.getKey();
            var message = "Duplicate name '%s'.".formatted(name);
            for (var namedElement : elementsWithName) {
                acceptError(message, namedElement, OxstsPackage.Literals.NAMED_ELEMENT__NAME, 0, DUPLICATE_NAME_ISSUE);
            }
        }
    }

    protected void acceptError(String message, EObject object, String code, String... issueData) {
        var region = locationInFileProvider.getFullTextRegion(object);
        acceptError(message, object, region.getOffset(), region.getLength(), code, issueData);
    }


    @Check
    public void ifOperationGuardMustBeBool(IfOperation ifOperation) {
        var evaluation = expressionTypeEvaluatorProvider.evaluate(ifOperation.getGuard());

        if (evaluation.getDomain() != builtinSymbolResolver.boolDatatype(ifOperation)) {
            acceptError("Guard is not bool", ifOperation, OxstsPackage.Literals.IF_OPERATION__GUARD, 0, INVALID_TYPE_ERROR);
        }
    }

    @Check
    public void ifOperationGuardMustBeBool(InlineIfOperation inlineIfOperation) {
        var evaluation = expressionTypeEvaluatorProvider.evaluate(inlineIfOperation.getGuard());

        if (evaluation.getDomain() != builtinSymbolResolver.boolDatatype(inlineIfOperation)) {
            acceptError("Guard is not bool", inlineIfOperation, OxstsPackage.Literals.INLINE_IF_OPERATION__GUARD, 0, DUPLICATE_NAME_ISSUE);
        }
    }

    @Check
    public void inlineIfGuardMustBeStatic(InlineIfOperation inlineIfOperation) {
        var modality = expressionModalityEvaluatorProvider.evaluateExpressionModality(inlineIfOperation.getGuard());

        if (modality == Modality.Dynamic) {
            acceptError("Guard is incompatible modality", inlineIfOperation, null, 0, DYNAMIC_INLINE_IF_GUARD_ISSUE);
        }
    }

    @Check
    public void ifGuardShouldBeDynamic(IfOperation ifOperation) {
        var modality = expressionModalityEvaluatorProvider.evaluateExpressionModality(ifOperation.getGuard());

        if (modality != Modality.Dynamic) {
            acceptError("This can be converted to inline if", ifOperation, null, 0, DYNAMIC_INLINE_IF_GUARD_ISSUE);
        }
    }

    @Check
    protected void featureDeclarationIsNotDynamic(FeatureDeclaration expression) {
        if (expressionModalityEvaluatorProvider.evaluateExpressionModality(expression.getExpression()) == Modality.Dynamic) {
            acceptError("Feature declaration value required to be at least static!", expression, null, 0, INCORRECT_MODALITY_ERROR);
        }
    }

    @Check
    public void variableDeclarationAssignmentMustBeCompatible(VariableDeclaration variableDeclaration) {
        var type = variableDeclaration.getType();
        if (type == null) {
            return;
        }
        var expression = variableDeclaration.getExpression();
        var expressionType = expressionTypeEvaluatorProvider.evaluate(expression);
        if(!typeCompatibility.isAssignable(new ImmutableTypeEvaluation(type), expressionType, expression))
            acceptError("message", variableDeclaration, DUPLICATE_NAME_ISSUE);
    }
}
