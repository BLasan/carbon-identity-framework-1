package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsCookie;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import javax.servlet.http.Cookie;

/**
 * Javascript wrapper for Java level Cookie for GraalJs Execution.
 * This provides controlled access to Cookie object via provided javascript native syntax.
 * e.g
 * var commonAuthIdDomain = context.request.cookies.commonAuthId.domain
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime Cookie.
 */
public class GraalJsCookie extends AbstractJSObjectWrapper<Cookie> implements ProxyObject, JsCookie {

    private static final Log LOG = LogFactory.getLog(GraalJsCookie.class);

    public GraalJsCookie(Cookie cookie) {
        super(cookie);
    }

    @Override
    public Object getMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_COOKIE_NAME:
                return getWrapped().getName();
            case FrameworkConstants.JSAttributes.JS_COOKIE_VALUE:
                return getWrapped().getValue();
            case FrameworkConstants.JSAttributes.JS_COOKIE_COMMENT:
                return getWrapped().getComment();
            case FrameworkConstants.JSAttributes.JS_COOKIE_DOMAIN:
                return getWrapped().getDomain();
            case FrameworkConstants.JSAttributes.JS_COOKIE_MAX_AGE:
                return getWrapped().getMaxAge();
            case FrameworkConstants.JSAttributes.JS_COOKIE_PATH:
                return getWrapped().getPath();
            case FrameworkConstants.JSAttributes.JS_COOKIE_SECURE:
                return getWrapped().getSecure();
            case FrameworkConstants.JSAttributes.JS_COOKIE_VERSION:
                return getWrapped().getVersion();
            case FrameworkConstants.JSAttributes.JS_COOKIE_HTTP_ONLY:
                return getWrapped().isHttpOnly();
            default:
                return super.getMember(name);
        }
    }

    @Override
    public Object getMemberKeys() {

        return null;
    }

    @Override
    public boolean hasMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_COOKIE_NAME:
                return getWrapped().getName() != null;
            case FrameworkConstants.JSAttributes.JS_COOKIE_VALUE:
                return getWrapped().getValue() != null;
            case FrameworkConstants.JSAttributes.JS_COOKIE_COMMENT:
                return getWrapped().getComment() != null;
            case FrameworkConstants.JSAttributes.JS_COOKIE_DOMAIN:
                return getWrapped().getDomain() != null;
            case FrameworkConstants.JSAttributes.JS_COOKIE_MAX_AGE:
                return getWrapped().getMaxAge() != -1;
            case FrameworkConstants.JSAttributes.JS_COOKIE_PATH:
                return getWrapped().getPath() != null;
            case FrameworkConstants.JSAttributes.JS_COOKIE_SECURE:
            case FrameworkConstants.JSAttributes.JS_COOKIE_HTTP_ONLY:
                return true;
            case FrameworkConstants.JSAttributes.JS_COOKIE_VERSION:
                return getWrapped().getVersion() != 0;
            default:
                return super.hasMember(name);
        }
    }

    @Override
    public void putMember(String key, Value value) {
        LOG.warn("Unsupported operation. Cookie is read only. Can't remove parameter " + key);
    }
}
