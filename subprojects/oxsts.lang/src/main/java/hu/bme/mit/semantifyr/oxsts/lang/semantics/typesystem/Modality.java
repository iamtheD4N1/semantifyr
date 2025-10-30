/*
 * SPDX-FileCopyrightText: 2025 The Semantifyr Authors
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package hu.bme.mit.semantifyr.oxsts.lang.semantics.typesystem;

public enum Modality {
    Constant, Static, Dynamic;
    Modality combine(Modality other) {
        if (ordinal() > other.ordinal())
            return this;
        return other;
    }
}
