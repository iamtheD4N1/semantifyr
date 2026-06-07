/*
 * SPDX-FileCopyrightText: 2026 The Semantifyr Authors
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package hu.bme.mit.semantifyr.oxsts.lang.semantics.typesystem;

public final class ArrayTypeEvaluation implements TypeEvaluation {
    public TypeEvaluation elementType;

    public ArrayTypeEvaluation(TypeEvaluation typeEvaluation) {
        elementType = typeEvaluation;
    }
}
