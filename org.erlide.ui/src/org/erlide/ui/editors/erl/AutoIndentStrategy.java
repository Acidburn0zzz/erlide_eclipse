/*******************************************************************************
 * Copyright (c) 2004 Eric Merritt and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Eric Merritt
 *     Vlad Dumitrescu
 *******************************************************************************/
package org.erlide.ui.editors.erl;

import java.util.Arrays;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.erlide.core.erlang.ErlModelException;
import org.erlide.core.erlang.IErlElement;
import org.erlide.core.erlang.IErlMember;
import org.erlide.runtime.backend.BackendManager;
import org.erlide.runtime.backend.IBackend;
import org.erlide.ui.ErlideUIPlugin;
import org.erlide.ui.prefs.plugin.IndentationPreferencePage;

import erlang.ErlideIndent;
import erlang.IndentResult;

/**
 * The erlang auto indent strategy
 * 
 * 
 * @author Eric Merritt [cyberlync at gmail dot com]
 */
public class AutoIndentStrategy implements IAutoEditStrategy {
	// extends DefaultIndentLineAutoEditStrategy {

	private final ErlangEditor fEditor;

	public AutoIndentStrategy(ErlangEditor editor) {
		super();
		fEditor = editor;
	}

	/**
	 * The default indent depth
	 */
	// private static final int INDENT_DEPTH = 4;
	/**
	 * Get the actual indent itself
	 * 
	 * @param depth
	 *            the depth of the indent;
	 * @return the indent
	 */
	private String getIndent(int depth) {
		final char[] x = new char[depth];
		Arrays.fill(x, ' ');

		return new String(x);
	}

	private void autoIndentAfterNewLine(IDocument d, DocumentCommand c) {
		try {
			indentAfterNewLine(d, c);
		} catch (final BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("boxing")
	protected void indentAfterNewLine(IDocument d, DocumentCommand c)
			throws BadLocationException {
		final int offset = c.offset;
		String txt = null;
		final IErlElement element = fEditor.getElementAt(offset, false);
		final IErlMember member = (IErlMember) element;
		if (member != null) {
			int start;
			try {
				start = member.getSourceRange().getOffset();
				txt = d.get(start, offset - start);
			} catch (final ErlModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (txt == null) {
			txt = d.get(0, offset);
		}
		final int lineN = d.getLineOfOffset(offset);
		final int lineOffset = d.getLineOffset(lineN);
		final int lineLength = d.getLineLength(lineN);
		final String oldLine = d.get(offset, lineLength + lineOffset - offset);
		try {
			final IBackend b = BackendManager.getDefault().getIdeBackend();
			int tabw = ErlideUIPlugin
					.getDefault()
					.getPreferenceStore()
					.getInt(
							AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
			if (tabw == 0) {
				tabw = EditorsUI
						.getPreferenceStore()
						.getInt(
								AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
			}

			final Map<String, Integer> prefs = IndentationPreferencePage
					.getKeysAndPrefs();
			final IndentResult res = ErlideIndent.indentLine(b, oldLine, txt,
					c.text, tabw, prefs);

			if (res.addNewLine) {
				c.text += "\n";
			}
			c.text += getIndent(res.indentWith);
			c.length += res.removeNext;
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Override a DocumentCommand if it ends with a line delim (CR) to include
	 * space characters for autoindentation
	 * 
	 * @param d
	 *            the document
	 * @param c
	 *            the command
	 */

	// FIXME flytta en del av denna logik till erlang!! (t.ex. s� vill man inte
	// vara "elektrisk" i kommentarer)
	public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
		if (c.length == 0 && c.text != null) {
			if (TextUtilities.endsWith(d.getLegalLineDelimiters(), c.text) != -1) {
				autoIndentAfterNewLine(d, c);
			} else if (c.text.endsWith(",")) {
				autoIndentAfterNewLine(d, c);
			} else if (c.text.endsWith(";")) {
				autoIndentAfterNewLine(d, c);
			} else if (c.text.endsWith(".")) {
				autoIndentAfterNewLine(d, c);
			} else if (c.text.endsWith(">")) {
				try {
					if (c.offset > 0 && c.offset <= d.getLength()
							&& d.getChar(c.offset - 1) == '-') {
						autoIndentAfterNewLine(d, c);
					}
				} catch (final BadLocationException e) {
					// never mind...
				}
			}
		}
	}
}
