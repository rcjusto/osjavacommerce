package org.store.config;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.StrutsConstants;
import org.apache.struts2.dispatcher.VelocityResult;
import org.apache.struts2.views.JspSupportServlet;
import org.apache.struts2.views.velocity.VelocityManager;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by IntelliJ IDEA.
 * User: Administrador
 * Date: Mar 22, 2010
 */
public class ConfigVelocityResult extends VelocityResult {

    private static final long serialVersionUID = 7268830767762559424L;

    private static final Logger log = LoggerFactory.getLogger(VelocityResult.class);

    private String defaultEncoding;
    private ConfigVelocityManager velocityManager;

    public ConfigVelocityResult() {
        super();
    }

    public ConfigVelocityResult(String location) {
        super(location);
    }

    @Inject(StrutsConstants.STRUTS_I18N_ENCODING)
    public void setDefaultEncoding(String val) {
        defaultEncoding = val;
    }

    @Inject
    public void setConfigVelocityManager(ConfigVelocityManager mgr) {
        this.velocityManager = mgr;
    }

    /**
     * Creates a Velocity context from the action, loads a Velocity template and executes the
     * template. Output is written to the servlet output stream.
     *
     * @param finalLocation the location of the Velocity template
     * @param invocation    an encapsulation of the action execution state.
     * @throws Exception if an error occurs when creating the Velocity context, loading or executing
     *                   the template or writing output to the servlet response stream.
     */
    public void doExecute(String finalLocation, ActionInvocation invocation) throws Exception {
        ValueStack stack = ActionContext.getContext().getValueStack();

        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();
        JspFactory jspFactory = null;
        ServletContext servletContext = ServletActionContext.getServletContext();
        Servlet servlet = JspSupportServlet.jspSupportServlet;
        try {
            velocityManager.init(servletContext);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        boolean usedJspFactory = false;
        PageContext pageContext = (PageContext) ActionContext.getContext().get(ServletActionContext.PAGE_CONTEXT);

        if (pageContext == null && servlet != null) {
            jspFactory = JspFactory.getDefaultFactory();
            pageContext = jspFactory.getPageContext(servlet, request, response, null, true, 8192, true);
            ActionContext.getContext().put(ServletActionContext.PAGE_CONTEXT, pageContext);
            usedJspFactory = true;
        }

        try {
            String encoding = getEncoding(finalLocation);
            String contentType = getContentType(finalLocation);

            if (encoding != null) {
                contentType = contentType + ";charset=" + encoding;
            }

            Template t = getTemplate(stack, velocityManager.getVelocityEngine(), invocation, finalLocation, encoding);

            Context context = createContext(velocityManager, stack, request, response, finalLocation);
            Writer writer = new OutputStreamWriter(response.getOutputStream(), encoding);


            response.setContentType(contentType);

            t.merge(context, writer);

            // always flush the writer (we used to only flush it if this was a jspWriter, but someone asked
            // to do it all the time (WW-829). Since Velocity support is being deprecated, we'll oblige :)
            writer.flush();
        } catch (Exception e) {
            log.error("Unable to render Velocity Template, '" + finalLocation + "'", e);
            throw e;
        } finally {
            if (usedJspFactory) {
                jspFactory.releasePageContext(pageContext);
            }
        }

        return;
    }

    /**
     * Retrieve the content type for this template.
     * <p/>
     * People can override this method if they want to provide specific content types for specific templates (eg text/xml).
     *
     * @return The content type associated with this template (default "text/html")
     */
    protected String getContentType(String templateLocation) {
        if (templateLocation.startsWith("/WEB-INF/views/js/") || templateLocation.startsWith("/resources/js/")) return "application/x-javascript";
        else if (templateLocation.startsWith("/WEB-INF/views/css/") || templateLocation.startsWith("/resources/css/")) return "text/css";
        else if (templateLocation.startsWith("/WEB-INF/views/txt/") || templateLocation.startsWith("/resources/txt/")) return "text/plain";
        else return "text/html";
    }

    /**
     * Retrieve the encoding for this template.
     * <p/>
     * People can override this method if they want to provide specific encodings for specific templates.
     *
     * @return The encoding associated with this template (defaults to the value of 'struts.i18n.encoding' property)
     */
    protected String getEncoding(String templateLocation) {
        String encoding = defaultEncoding;
        if (encoding == null) {
            encoding = System.getProperty("file.encoding");
        }
        if (encoding == null) {
            encoding = "UTF-8";
        }
        return encoding;
    }

    /**
     * Given a value stack, a Velocity engine, and an action invocation, this method returns the appropriate
     * Velocity template to render.
     *
     * @param stack      the value stack to resolve the location again (when parse equals true)
     * @param velocity   the velocity engine to process the request against
     * @param invocation an encapsulation of the action execution state.
     * @param location   the location of the template
     * @param encoding   the charset encoding of the template
     * @return the template to render
     * @throws Exception when the requested template could not be found
     */
    protected Template getTemplate(ValueStack stack, VelocityEngine velocity, ActionInvocation invocation, String location, String encoding) throws Exception {
        if (!location.startsWith("/")) {
            location = invocation.getProxy().getNamespace() + "/" + location;
        }

        Template template = velocity.getTemplate(location, encoding);

        return template;
    }

    /**
     * Creates the VelocityContext that we'll use to render this page.
     *
     * @param velocityManager a reference to the velocityManager to use
     * @param stack           the value stack to resolve the location against (when parse equals true)
     * @param location        the name of the template that is being used
     * @return the a minted Velocity context.
     */
    protected Context createContext(VelocityManager velocityManager, ValueStack stack, HttpServletRequest request, HttpServletResponse response, String location) {
        return velocityManager.createContext(stack, request, response);
    }

}
