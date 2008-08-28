/*******************************************************************************
 * Copyright (c) 2005 Vlad Dumitrescu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vlad Dumitrescu
 *     Jakob Cederlund
 *******************************************************************************/
package org.erlide.runtime.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.erlide.runtime.ErlLogger;
import org.erlide.runtime.backend.BackendManager.BackendOptions;
import org.erlide.runtime.debug.ErlangDebugTarget;

import erlang.ErlideDebug;

public class ErlangLaunchConfigurationDelegate extends
		LaunchConfigurationDelegate {

	public void launch(final ILaunchConfiguration config, final String mode,
			final ILaunch launch, final IProgressMonitor monitor)
			throws CoreException {
		try {
			final String[] projectNames = config.getAttribute(
					IErlLaunchAttributes.PROJECTS, "").trim().split(";");
			final String module = config.getAttribute(
					IErlLaunchAttributes.MODULE, "").trim();
			final String function = config.getAttribute(
					IErlLaunchAttributes.FUNCTION, "").trim();
			final String args = config.getAttribute(
					IErlLaunchAttributes.ARGUMENTS, "").trim();
			final String runtime = config.getAttribute(
					IErlLaunchAttributes.RUNTIME_NAME, "").trim();
			final String nodeName = config.getAttribute(
					IErlLaunchAttributes.NODE_NAME, "").trim();
			final String cookie = config.getAttribute(
					IErlLaunchAttributes.COOKIE, "").trim();
			final boolean startMe = config.getAttribute(
					IErlLaunchAttributes.START_ME, false);

			System.out.println("Debug:: about to start a backend in " + mode
					+ " mode, with attributes::");
			System.out.println("  projects: " + Arrays.toString(projectNames));
			System.out.println("  module: " + module);
			System.out.println("  function: " + function);
			System.out.println("  args: " + args);

			System.out.println("  runtime: " + runtime);
			System.out.println("  node name: " + nodeName);
			System.out.println("  cookie: " + cookie);
			if (startMe) {
				System.out.println("  * start it if not running");
			}
			System.out.println("---------------");

			for (String s : projectNames) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot()
						.getProject(s);
				if (project == null) {
					ErlLogger.error("Launch: project not found: '%s'!", s);
					return;
				}
			}

			final RuntimeInfo rt = RuntimeInfo.copy(RuntimeInfoManager
					.getDefault().getRuntime(runtime), false);
			rt.setNodeName(nodeName);
			rt.setCookie(cookie);

			final EnumSet<BackendOptions> options = EnumSet
					.noneOf(BackendOptions.class);
			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				options.add(BackendOptions.DEBUG);
			}
			if (startMe) {
				options.add(BackendOptions.AUTOSTART);
			}

			final IBackend b = BackendManager.getDefault().create(rt, options,
					launch);
			if (b == null) {
				ErlLogger.error("Launch: got null backend!");
				return;
			}
			ExecutionBackend backend = b.asExecution();

			// launch.addProcess(null);
			backend.registerProjects(projectNames);
			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				// add debug target
				final IProject[] projects = BackendUtil
						.getProjects(projectNames);
				final ErlangDebugTarget target = new ErlangDebugTarget(launch,
						backend, projects);
				launch.addDebugTarget(target);
				// interpret everything we can
				if (projects != null) {
					for (final IProject p : projects) {
						interpretAll(backend, p);
					}
				}
				// send started to target
				target.sendStarted();
			}

			if (module.length() > 0 && function.length() > 0) {
				if (args.length() > 0) {
					// TODO issue #84
					backend.rpc(module, function, "s", args);
				} else {
					backend.rpc(module, function, "");
				}
			}
		} catch (final Exception e) {
			ErlLogger.debug("Could not launch Erlang:::");
			e.printStackTrace();
		}
	}

	private void interpretAll(final ExecutionBackend backend,
			final IProject project) {
		final List<String> beams = new ArrayList<String>();
		final List<String> erls = new ArrayList<String>();
		final Map<String, String> erlLocations = new TreeMap<String, String>();
		try {
			project.accept(new IResourceVisitor() {
				public boolean visit(final IResource resource)
						throws CoreException {
					final IPath fullPath = resource.getFullPath();
					if (fullPath != null) {
						final String ext = fullPath.getFileExtension();
						if (ext != null) {
							final String baseName = fullPath
									.removeFileExtension().lastSegment();
							if (ext.equals("beam")) {
								beams.add(baseName);
							} else if (ext.equals("erl")) {
								final IPath location = resource.getLocation();
								if (location != null) {
									erls.add(baseName);
									erlLocations.put(baseName, location
											.toString());
								}
							}
						}
					}
					return true;
				}
			}, IResource.DEPTH_INFINITE, 0);
			for (final String erl : erls) {
				if (beams.contains(erl)) {
					ErlLogger.debug("interpret " + erlLocations.get(erl));
					ErlideDebug.interpret(backend, erlLocations.get(erl));
				}
			}
		} catch (final CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getCmdLine(final ILaunchConfiguration configuration) {
		try {
			return configuration.getAttribute(IProcess.ATTR_CMDLINE, "");
		} catch (final CoreException e) {
			return "";
		}
	}

	protected String getAdditionalArgs(final ILaunchConfiguration configuration) {
		return "";
	}

}
