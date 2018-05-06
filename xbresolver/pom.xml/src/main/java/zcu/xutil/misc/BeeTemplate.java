/*
 * Copyright 2009 zaichu xiao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package zcu.xutil.misc;

import static zcu.xutil.util.Objutil.validate;
import static zcu.xutil.util.Constants.PATTERN_ILLEGAL_START;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.FileResourceLoader;
import org.beetl.ext.web.ParameterWrapper;
import org.beetl.ext.web.SessionWrapper;

import zcu.xutil.web.Resolver;
import zcu.xutil.web.WebContext;

public class BeeTemplate extends Configuration implements Resolver {
	private String root = "/";
	private volatile GroupTemplate group;

	public BeeTemplate() throws IOException {
		setDirectByteOutput(true);
	}

	public void setRoot(String s) {
		validate((root = s).startsWith("/"), PATTERN_ILLEGAL_START, s);
	}

	@Override
	public void resolve(String view, WebContext context) throws IOException {
		if (group == null)
			synchronized (this) {
				if (group == null) {
					group = new GroupTemplate(
							new FileResourceLoader(context.getServletContext().getRealPath(root), getCharset()), this);
				}
			}
		HttpServletRequest hreq = context.getRequest();
		HttpServletResponse hresp = context.getResponse();
		Template t = group.getTemplate(view);
		Enumeration<String> e = hreq.getAttributeNames();
		while (e.hasMoreElements()) {
			String s = e.nextElement();
			t.binding(s, hreq.getAttribute(s));
		}
		t.binding("request", hreq);
		t.binding("response", hresp);
		t.binding("session", new SessionWrapper(hreq, null));
		t.binding("parameter", new ParameterWrapper(hreq));
		hresp.setContentType("text/html;charset=".concat(getCharset()));
		hresp.setHeader("Cache-Control", "no-store, no-cache");

		if (isDirectByteOutput())
			t.renderTo(hresp.getOutputStream());
		else
			t.renderTo(hresp.getWriter());
	}
}
