/*
 * SPDX-FileCopyrightText: 2025 The Semantifyr Authors
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package hu.bme.mit.semantifyr.oxsts.lang.semantics.typesystem;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import hu.bme.mit.semantifyr.oxsts.lang.utils.OnResourceSetChangeEvictingCache;
import hu.bme.mit.semantifyr.oxsts.model.oxsts.Expression;
import org.eclipse.emf.ecore.EObject;

@Singleton
public class ExpressionModalityEvaluatorProvider {

    private static final String CACHE_KEY = "hu.bme.mit.semantifyr.oxsts.lang.semantics.typesystem.ExpressionModalityEvaluatorProvider.CACHE_KEY";

    @Inject
    private OnResourceSetChangeEvictingCache resourceScopeCache;

    @Inject
    private Provider<ExpressionModalityEvaluator> expressionModalityEvaluatorProvider;

    public ExpressionModalityEvaluator getExpressionModalityEvaluator(EObject eObject) {
        return resourceScopeCache.get(CACHE_KEY, eObject.eResource(), expressionModalityEvaluatorProvider);
    }

    public Modality evaluateExpressionModality(Expression expression) {
        return getExpressionModalityEvaluator(expression).evaluate(expression);
    }

}
