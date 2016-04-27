package org.store.core.hibernate;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.opensymphony.xwork2.inject.Inject;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

public abstract class GenericInterceptor extends AbstractInterceptor {

	protected static Logger log;

	protected boolean strutsDevMode;

	public GenericInterceptor() {
		if (log==null) {
			log = Logger.getLogger(this.getClass());
			if (!log.getRootLogger().getAllAppenders().hasMoreElements()) {
				log.addAppender(new ConsoleAppender(new PatternLayout("%d{HH:mm:ss} - %p: %m %n")));
			}
			log.setLevel(Level.WARN);
		}
	}

	@Inject(value="struts.devMode", required=false)
	public void setStrutsDevMode(String strutsDevMode) {
		this.strutsDevMode = new Boolean(strutsDevMode);
		if (this.strutsDevMode) {
			log.setLevel(Level.DEBUG);
			HibernateSessionFactory.initLogger();
			HibernateSessionFactory.setLoggerDebugLevel();
		}
	}
}
