/*
 * SPDX-FileCopyrightText: 2025 The Semantifyr Authors
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package hu.bme.mit.semantifyr.oxsts.lang.ide.editor.quickfix;

import hu.bme.mit.semantifyr.oxsts.lang.validation.OxstsValidator;
import hu.bme.mit.semantifyr.oxsts.model.oxsts.InlineIfOperation;
import hu.bme.mit.semantifyr.oxsts.model.oxsts.impl.IfOperationImpl;
import hu.bme.mit.semantifyr.oxsts.model.oxsts.impl.InlineIfOperationImpl;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.ide.editor.quickfix.AbstractDeclarativeIdeQuickfixProvider;
import org.eclipse.xtext.ide.editor.quickfix.DiagnosticResolutionAcceptor;
import org.eclipse.xtext.ide.editor.quickfix.QuickFix;

import java.util.Collections;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class OxstsQuickFixProvider extends AbstractDeclarativeIdeQuickfixProvider {

//    @QuickFix(OxstsValidator.DATA_TYPE_NOT_IN_BUILTIN_ISSUE)
//    public void removeNotBuiltinDataType(DiagnosticResolutionAcceptor acceptor) {
//        acceptor.accept("Remove", ((diagnostic, object, document) -> {
//            return createTextEdit(diagnostic, "");
//        }));
//    }

    @QuickFix(OxstsValidator.DATA_TYPE_NOT_IN_BUILTIN_ISSUE)
    public void removeNotBuiltinDataType(DiagnosticResolutionAcceptor acceptor) {
        acceptor.accept("Remove", ((diagnostic, object) -> {
            return (o) -> EcoreUtil2.remove(o);
        }));
    }

    @QuickFix(OxstsValidator.DYNAMIC_INLINE_IF_GUARD_ISSUE)
    public void changeInlineIfToIf(DiagnosticResolutionAcceptor acceptor) {
        acceptor.accept("Remove inline", ((diagnostic, object, document) -> {
            var range = diagnostic.getRange();
            range.setEnd(new Position(range.getStart().getLine(), range.getStart().getCharacter() + "inline ".length()));
            return Collections.singletonList(new TextEdit(range, ""));
        }));
    }

    @QuickFix(OxstsValidator.STATIC_IF_GUARD_ISSUE)
    public void changeIfToInlineIf(DiagnosticResolutionAcceptor acceptor) {
        acceptor.accept("Add inline", ((diagnostic, object, document) -> {
            var range = diagnostic.getRange();
            range.setEnd(new Position(range.getStart().getLine(), range.getStart().getCharacter()));
            return Collections.singletonList(new TextEdit(range, "inline "));
        }));
    }

    // TODO: add quick fixes
    //  - constant expression could be simplified -> replace with evaluation
    //   - will need ConstantExpressionEvaluator and ConstantExpressionEvaluatorTransformer
    //  - guard of 'if' is static -> could be inline if (same with for)

}
