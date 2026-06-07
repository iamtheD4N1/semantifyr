/*
 * SPDX-FileCopyrightText: 2026 The Semantifyr Authors
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package hu.bme.mit.semantifyr.oxsts.lang.semantics.typesystem;

import com.google.inject.Inject;
import hu.bme.mit.semantifyr.oxsts.lang.library.builtin.BuiltinSymbolResolver;
import hu.bme.mit.semantifyr.oxsts.model.oxsts.DomainDeclaration;
import hu.bme.mit.semantifyr.oxsts.model.oxsts.Expression;

public class TypeCompatibility {
    @Inject
    private BuiltinSymbolResolver builtinSymbolResolver;

    public boolean isAssignable(TypeEvaluation assignee, TypeEvaluation assigned, Expression expression) {
        // check if the assigned is compatible with the assignee

        if (isInvalid(assignee) || isInvalid(assigned)) {
            return false;
        }

        if (isEquivalent(assignee, assigned)) {
            return true;
        }

        if (isIntToRealCastable(assignee, assigned, expression)) {
            return true;
        }

        if (isClassType(assignee) && isClassType(assigned)) {
            return isClassCompatible(assignee, assigned);
        }

        return false;
    }

    private boolean isClassType(TypeEvaluation assigned) {
        return false;
    }

    private boolean isClassCompatible(TypeEvaluation assignee, TypeEvaluation assigned) {
        return false;
    }

    private boolean isEquivalent(TypeEvaluation assignee, TypeEvaluation assigned) {
        if (assignee instanceof ImmutableTypeEvaluation(DomainDeclaration assigneeDomain)
            && assigned instanceof ImmutableTypeEvaluation(DomainDeclaration assignedDomain)) {
            return assigneeDomain == assignedDomain;
        }
        return false;
    }

    private boolean isIntToRealCastable(TypeEvaluation assignee, TypeEvaluation assigned, Expression expression) {

        if (assignee instanceof ImmutableTypeEvaluation(DomainDeclaration assigneeDomain) &&
                assigned instanceof ImmutableTypeEvaluation(DomainDeclaration assignedDomain)) {
            return assigneeDomain == builtinSymbolResolver.realDatatype(expression) &&
                    assignedDomain == builtinSymbolResolver.intDatatype(expression);
        }
        return false;
    }

    private boolean isInvalid(TypeEvaluation assignee) {
        return assignee instanceof InvalidTypeEvaluation;
    }

}
