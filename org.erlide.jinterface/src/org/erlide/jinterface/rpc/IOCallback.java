/*******************************************************************************
 * Copyright (c) 2009 * and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available
 * at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     *
 *******************************************************************************/
package org.erlide.jinterface.rpc;

import org.erlide.jinterface.rpc.IOServer.Encoding;

import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangPid;

public interface IOCallback {

	OtpErlangObject putChars(OtpErlangPid from, Encoding encoding,
			OtpErlangObject chars);

	OtpErlangObject putChars(OtpErlangPid from, Encoding latin1, String module,
			String function, OtpErlangObject[] args);

	OtpErlangObject getUntil(Encoding latin1, OtpErlangObject otpErlangObject);

	OtpErlangObject getUntil(Encoding latin1, OtpErlangObject otpErlangObject,
			long n);

	OtpErlangObject getUntil(Encoding valueOf, OtpErlangObject otpErlangObject,
			String m, String f, OtpErlangObject[] a);

	OtpErlangObject getOpts();

	OtpErlangObject setOpts(OtpErlangObject[] opts);

}
