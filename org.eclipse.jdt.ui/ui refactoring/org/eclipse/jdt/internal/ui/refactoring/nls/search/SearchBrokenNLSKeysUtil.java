/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.ui.refactoring.nls.search;

import org.eclipse.core.resources.IFile;

import org.eclipse.search.ui.NewSearchUI;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.SearchEngine;


public class SearchBrokenNLSKeysUtil {

	public static void search(String scopeName, IType[] accessorClasses, IFile[] propertieFiles) {
		NLSSearchQuery query= new NLSSearchQuery(accessorClasses, propertieFiles, SearchEngine.createWorkspaceScope(), scopeName);
		NewSearchUI.runQueryInBackground(query);
	}

	private SearchBrokenNLSKeysUtil() {
	}

}
