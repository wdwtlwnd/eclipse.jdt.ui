/*******************************************************************************
 * Copyright (c) 2000, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jdt.internal.ui.text.correction;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.refactoring.changes.CompilationUnitChange;
import org.eclipse.jdt.internal.corext.textmanipulation.SimpleTextEdit;
import org.eclipse.jdt.internal.ui.JavaPlugin;

public class CorrectPackageDeclarationProposal extends CUCorrectionProposal {

	public CorrectPackageDeclarationProposal(ProblemPosition problemPos) throws CoreException {
		super("Correct package declaration", problemPos);
	}

	/*
	 * @see CUCorrectionProposal#addEdits(CompilationUnitChange)
	 */
	protected void addEdits(CompilationUnitChange change) throws CoreException {
		ICompilationUnit cu= getCompilationUnit();
		
		IPackageFragment parentPack= (IPackageFragment) cu.getParent();
		IPackageDeclaration[] decls= cu.getPackageDeclarations();
		
		if (parentPack.isDefaultPackage() && decls.length > 0) {
			for (int i= 0; i < decls.length; i++) {
				ISourceRange range= decls[i].getSourceRange();
				change.addTextEdit("Remove Declaration", SimpleTextEdit.createDelete(range.getOffset(), range.getLength()));
			}
			return;
		}
		if (!parentPack.isDefaultPackage() && decls.length == 0) {
			String lineDelim= StubUtility.getLineDelimiterUsed(cu);
			String str= "package " + parentPack.getElementName() + ";" + lineDelim + lineDelim;
			change.addTextEdit("Add Declaration", SimpleTextEdit.createInsert(0, str));
			return;
		}
		
		ProblemPosition pos= getProblemPosition();
		change.addTextEdit("Change Name", SimpleTextEdit.createReplace(pos.getOffset(), pos.getLength(), parentPack.getElementName()));
	}
	
	/*
	 * @see ICompletionProposal#getDisplayString()
	 */
	public String getDisplayString() {
		ICompilationUnit cu= getCompilationUnit();
		IPackageFragment parentPack= (IPackageFragment) cu.getParent();
		try {
			IPackageDeclaration[] decls= cu.getPackageDeclarations();		
			if (parentPack.isDefaultPackage() && decls.length > 0) {
				return "Remove package declaration 'package " + decls[0].getElementName() + ";'";
			}
			if (!parentPack.isDefaultPackage() && decls.length == 0) {	
				return ("Add package declaration '" + parentPack.getElementName() + "'");
			}
		} catch(JavaModelException e) {
			JavaPlugin.log(e);
		}
		return ("Change package declaration to '" + parentPack.getElementName() + "'");
	}	

}
