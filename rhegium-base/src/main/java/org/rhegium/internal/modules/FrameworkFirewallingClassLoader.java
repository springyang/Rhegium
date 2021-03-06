/*
 * Copyright (C) 2011 Rhegium Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rhegium.internal.modules;

import java.io.InputStream;
import java.net.URL;

import org.rhegium.internal.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrameworkFirewallingClassLoader extends ClassLoader {

	private static final Logger LOG = LoggerFactory.getLogger(FrameworkFirewallingClassLoader.class);

	private final String[] privilegedPackages;

	public FrameworkFirewallingClassLoader(final ClassLoader parent, final String[] privilegedPackages) {

		super(parent);

		this.privilegedPackages = privilegedPackages;
	}

	@Override
	protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {

		if (LOG.isTraceEnabled()) {
			LOG.trace(StringUtils.join(" ", "Trying to access ", name));
		}

		final String path = name.substring(0, name.lastIndexOf("."));
		if (!accept(path)) {
			if (LOG.isTraceEnabled()) {
				LOG.trace(StringUtils.join(" ", "Access to class ", name, " denied."));
			}

			throw new ClassNotFoundException(name);
		}

		return super.loadClass(name, resolve);
	}

	@Override
	public URL getResource(final String name) {
		if (!acceptResourcePath(name)) {
			return null;
		}

		return super.getResource(name);
	}

	@Override
	public InputStream getResourceAsStream(final String name) {
		if (!acceptResourcePath(name)) {
			return null;
		}

		return super.getResourceAsStream(name);
	}

	private boolean acceptResourcePath(final String path) {
		final String normalized = path.replace("/", ".").replace("\\", ".");
		final String normalizedPath = normalized.substring(0, normalized.lastIndexOf("."));

		return accept(normalizedPath);
	}

	private boolean accept(final String path) {
		if (!path.startsWith("org.rhegium.")) {
			return true;
		}

		if (!path.contains(".impl.") && !path.endsWith("Impl")) {
			return true;
		}

		final String packageName = path.substring(path.lastIndexOf("."));
		for (final String privilegedPackage : privilegedPackages) {
			if (packageName.startsWith(privilegedPackage)) {
				return true;
			}
		}

		return false;
	}
}
