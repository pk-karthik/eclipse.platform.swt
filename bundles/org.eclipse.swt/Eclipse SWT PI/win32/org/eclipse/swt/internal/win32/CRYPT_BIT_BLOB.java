/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.internal.win32;


public class CRYPT_BIT_BLOB {
	public int cbData;
	/** @field cast=(BYTE *) */
	public int /*long*/ pbData;
	public int cUnusedBits;

	static final public int sizeof = OS.CRYPT_BIT_BLOB_sizeof ();
}