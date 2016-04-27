package org.store.core.hibernate;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.inject.Inject;
import org.store.core.globals.BaseAction;
import org.store.core.globals.config.Store20Database;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionException;
import org.hibernate.Transaction;
import org.hibernate.TransactionException;
import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.InvalidValue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * Interceptor for Hibernate Session and Transaction Injection
 *
 * <br/><br/>
 *
 * Interceptor para inje��o da Sess�o Hibernate e da Transa��o
 *
 * @author Jose Yoshiriro - jyoshiriro@gmail.com
 *
 */

public class SessionTransactionInjectorInterceptor extends GenericInterceptor {

	/**
	 *
	 */
	private static final long serialVersionUID = 2222692750520221708L;

	static Logger log = Logger.getLogger(SessionTransactionInjectorInterceptor.class);

    private String resultError;

	private String sessionTarget;

	private String transactionTarget;

	private String customSessionFactoryClass;

	private String getSessionMethod;

	private String staticGetSessionMethod = "true";

	private String closeSessionAfterInvoke = "true";

	private String configurationFiles = HibernateSessionFactory.DEFAULT_HIBERATE_CONFIGFILE;

	private String closeSessionMethod;

	private boolean useSessionObjectInCloseMethod = true;

	private static Set<String> excludedPackages;

	static {
		excludedPackages = new LinkedHashSet<String>();
		excludedPackages.add("org.apache.");
		excludedPackages.add("org.hibernate.");
		excludedPackages.add("com.opensymphony.");
		excludedPackages.add("org.springframework.");
		excludedPackages.add("org.jboss.");
		excludedPackages.add("java.");
		excludedPackages.add("javax.");
		excludedPackages.add("sun.");
		excludedPackages.add("com.sun.");
		excludedPackages.add("com.caucho.");
		excludedPackages.add("javassist.");
	}

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {

		Object action = invocation.getAction();
        Store20Database databaseConfig = (action instanceof BaseAction) ? ((BaseAction)action).getDatabaseConfig() : null;

		String namespaceName = invocation.getProxy().getNamespace();
		String actionName = invocation.getProxy().getActionName();
		String methodName = invocation.getProxy().getMethod();
		StringBuilder sbMapping = new StringBuilder(namespaceName);
		if (!namespaceName.equals("/"))
			sbMapping.append("/");
		sbMapping.append(actionName);
		sbMapping.append(" - Method: ");
		sbMapping.append(action.getClass().getName());
		sbMapping.append(".");
		sbMapping.append(methodName);
		sbMapping.append("()");

		if (action.getClass().equals(ActionSupport.class)) {
			log.warn("Action class \""+action.getClass().getName()+
					"\" from mapping \""+namespaceName+actionName+"\" does not extends \""+
					ActionSupport.class.getName()+"\". " +
					"Hibernate Session and Transaction will not be injected!");
			return invocation.invoke();
		}


		log.debug("Preparing Injection Hibernate Session and Transaction process: "+sbMapping.toString());

		HibernateSessionTransactionInfo sessionInfo = new HibernateSessionTransactionInfo();
		HibernateSessionTransactionInfo transactionInfo = new HibernateSessionTransactionInfo();

		List<Field> fields = getFieldsFromAction(invocation.getAction());

		StringBuilder sbErrorMessage = new StringBuilder("Error! Please, check your JDBC/JDNI Configurations and Database Server avaliability. \n ");
		sbErrorMessage.append("at "+sbMapping.toString()+"\n ");

		boolean sessionNotInjected = false;
		boolean transactionNotInjected = false;

		try {
			if (sessionTarget!=null && (!sessionTarget.equals(""))) {
				sessionInfo = injectHibernateSessionByConfiguration(fields, invocation, databaseConfig);
			}
			boolean sessionInjectedByConfigutarion = (sessionInfo.getSessionObject()!=null);
			if (!sessionInjectedByConfigutarion)
				sessionInfo.setSessionObject(getHibernateSessionFromFactory(databaseConfig));
			sessionNotInjected = ( (!injectHibernateCoreSessionByAnnotation(action, sessionInfo.getSessionObject(), false)) && (!sessionInjectedByConfigutarion) );
			if (sessionNotInjected)
				log.warn("No target setted for Hibernate Session object at "+sbMapping.toString()+". Use the *"+Constants.HIBERNATEPLUGIN_SESSIONTARGET+"* property or the SessionTarget Annotation. " +
						"If any annotated Session object be found, will be closed after the request.");
		} catch (HibernateException e) {
            sbErrorMessage.append("Could not open or put a Hibernate Session in ValueStack: ");
            sbErrorMessage.append(e.getMessage());
            String message = sbErrorMessage.toString();
            if (action instanceof ActionSupport) {
                ((ActionSupport)action).addActionError(message);
            }
            if (resultError!=null && !"".equals(resultError) && "/hibernate.cfg.xml not found".equalsIgnoreCase(e.getMessage())) return resultError;
            log.error(e.getMessage(), e); 
            throw new SessionException(message);
		} catch (Exception e) {
			sbErrorMessage.append("Could not open or put a Hibernate Session in ValueStack: ");
			sbErrorMessage.append(e.getMessage());
			String message = sbErrorMessage.toString();
			System.err.println(message);
			log.error(e.getMessage(), e); 
			throw new SessionException(message);
		}

		try {
			if (transactionTarget!=null && (!transactionTarget.equals(""))) {
				transactionInfo = injectHibernateTransactionByConfiguration(fields, sessionInfo.getSessionObject(), invocation);
			}
			boolean transactionInjectedByConfigutarion = (transactionInfo.getTransactionObject()!=null);
			if (!transactionInjectedByConfigutarion)
				transactionInfo.setTransactionObject(sessionInfo.getSessionObject().beginTransaction());
			transactionNotInjected = ((!injectHibernateTransactionByAnnotation(action, transactionInfo.getTransactionObject(), false)) && (!transactionInjectedByConfigutarion));
			if ( transactionNotInjected ) {
				log.warn("No target setted for Hibernate Transaction object at "+sbMapping.toString()+". Use the *"+Constants.HIBERNATEPLUGIN_TRANSACTIONTARGET+"* property or the TransactionTarget Annotation. " +
						"If any annotated Transaction object be found, will be commited the request.");
			}
        } catch (HibernateException e) {
            log.error(e.getMessage(), e); 
		} catch (Exception e) {
			sbErrorMessage.append("Could not open or put a Hibernate Transaction in ValueStack: ");
			sbErrorMessage.append(e.getMessage());
			String message = sbErrorMessage.toString();
			System.err.println(message);
			log.error(e.getMessage(), e); 
			throw new TransactionException(message);
		}

		String returnName = "";
		try {
			returnName = invocation.invoke();
		} catch (Exception e) {
			System.err.println("Error invoking Action using Hibernate Core Session / Transaction injection at "+sbMapping.toString());
			log.error(e.getMessage(), e); 
			Session hibernateSession = sessionInfo.getSessionObject();
			Transaction hibernateTransation = transactionInfo.getTransactionObject();
			if (hibernateTransation!=null) {
				if ( (hibernateTransation.isActive()) && (!hibernateTransation.wasCommitted()) ) {
					hibernateTransation.rollback();
					log.debug("Hibernate Transation rolledback");
				}
			}
			closeHibernateSession(hibernateSession, databaseConfig);
			detectAndCloseHibernateCoreSessionByAnnotation(action, databaseConfig);
			detectAndCloseHibernateTransactionByAnnotation(action);
			throw e;
		}

		Session hibernateSession = sessionInfo.getSessionObject();
		Transaction hibernateTransation = transactionInfo.getTransactionObject();

		if ((hibernateTransation.isActive()) && (!hibernateTransation.wasRolledBack())) {
			try {
				hibernateTransation.commit();
				log.debug("Hibernate Transaction Committed");
				detectAndCloseHibernateTransactionByAnnotation(action);
			}
			catch (Exception e) {
				closeHibernateSession(hibernateSession, databaseConfig);
				detectAndCloseHibernateCoreSessionByAnnotation(action, databaseConfig);
				sbErrorMessage.append("Could not commit the Hibernate Transaction: ");
				sbErrorMessage.append(e.getMessage());
				String message = sbErrorMessage.toString();
				System.err.println(message);
				if (e instanceof InvalidStateException) {
					InvalidStateException ise = (InvalidStateException)e;
					for (InvalidValue iv:ise.getInvalidValues())
						System.err.println(" "+iv);
				}
				log.error(e.getMessage(), e); 
				throw new Exception(message);
			}
		}

		closeHibernateSession(hibernateSession, databaseConfig);
		detectAndCloseHibernateCoreSessionByAnnotation(action, databaseConfig);

		log.debug("Injection Hibernate Session and Transaction process for "+sbMapping.toString()+" finished");

		return returnName ;
	}

	private void closeHibernateSession(Session hibernateSession, Store20Database db) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if ( (hibernateSession==null) || (!hibernateSession.isOpen()) )
			return;
		if (isCloseSessionAfterInvoke()) {
			// If using the Plugin's Session Factory
			if ( (customSessionFactoryClass==null) || (customSessionFactoryClass.equalsIgnoreCase("plugin")) ) {
				HibernateSessionFactory.closeSession(db);
				log.debug("Hibernate Session closed by Full Hibernate Plugin's Hibernate Session Factory");
			}
			else {
				// Using a custom Session Factory Class
				Object sessionFactory = null;
				sessionFactory = Class.forName(customSessionFactoryClass, false, this.getClass().getClassLoader());
				if (useSessionObjectInCloseMethod) {
					Method method = Class.forName(customSessionFactoryClass).getDeclaredMethod(closeSessionMethod, hibernateSession.getClass());
					method.invoke(sessionFactory, hibernateSession);
				}
				else {
					Method method = Class.forName(customSessionFactoryClass).getDeclaredMethod(closeSessionMethod, null);
					method.invoke(sessionFactory, null);
				}
				log.debug("Hibernate Session closed by custom Hibernate Session Factory ("+sessionFactory.getClass().getName()+")");
			}
		} else {
			//TODO: verificar se o Hibernate Manager est� Habilitado antes de por as sess�es no contexto de aplica��o para economizar mem�ria!
			Set<SessionInfo> hibernateSessions = (Set<SessionInfo>) ActionContext.getContext().getApplication().get("struts2HibernatePlugin_Sessions");
			if (hibernateSessions==null)
				hibernateSessions = new LinkedHashSet<SessionInfo>();
			SessionInfo info = new SessionInfo(hibernateSession,new Date(),ServletActionContext.getRequest().getSession());
			hibernateSessions.add(info);
			ActionContext.getContext().getApplication().put("struts2HibernatePlugin_Sessions", hibernateSessions);
		}
	}

	private HibernateSessionTransactionInfo injectHibernateSessionByConfiguration(List<Field> fields, ActionInvocation invocation, Store20Database databaseConfig) throws Exception {

		String trueTarget = "";
		Session hibernateSession = null;
		for (Field field:fields) {
			String[] targetAsArray = sessionTarget.replace(".", ",").split(",");

			if (!Pattern.matches(HibernatePluginUtils.wildcardToRegex(targetAsArray[0]), field.getName()))
				continue;

			targetAsArray[0] = field.getName();
			trueTarget = Arrays.toString(targetAsArray).replace("[", "").replace("]", "").replace(", ",".");

			if (hibernateSession==null) {
				hibernateSession = getHibernateSessionFromFactory(databaseConfig);
			}
			invocation.getStack().setValue(trueTarget, hibernateSession);
			log.debug("Hibernate Session injected (by configuration) into Action. Field \""+trueTarget+"\"");
		}
		HibernateSessionTransactionInfo sessionInfo = new HibernateSessionTransactionInfo();
		sessionInfo.setSessionObject(hibernateSession);
		sessionInfo.setSessionTarget(trueTarget);

		return sessionInfo;
	}

	private HibernateSessionTransactionInfo injectHibernateTransactionByConfiguration(List<Field> fields,
			Session hibernateSession, ActionInvocation invocation) throws Exception {

		String trueTarget = "";
		Transaction transaction = null;
		for (Field field:fields) {
			String[] targetAsArray = transactionTarget.replace(".", ",").split(",");

			if (!Pattern.matches(HibernatePluginUtils.wildcardToRegex(targetAsArray[0]), field.getName()))
				continue;

			targetAsArray[0] = field.getName();
			trueTarget = Arrays.toString(targetAsArray).replace("[", "").replace("]", "").replace(", ",".");

			if (transaction==null)
				//TODO: Look for the last TODO item. When finish this, change this line to *getTransaction()* method.
				transaction = hibernateSession.beginTransaction();

			invocation.getStack().setValue(trueTarget, transaction);
			log.debug("Hibernate Transaction injected (by configuration) into Action. Field \""+trueTarget+"\"");
		}
		HibernateSessionTransactionInfo transactionInfo = new HibernateSessionTransactionInfo();
		transactionInfo.setTransactionObject(transaction);
		transactionInfo.setTransactionTarget(trueTarget);

		return transactionInfo;
	}

	private Session getHibernateSessionFromFactory(Store20Database databaseConfig) throws Exception {
		Session hibernateSession = null;
		// Using the PLUGIN Session Factory Class
		if ( (customSessionFactoryClass==null) || (customSessionFactoryClass.equalsIgnoreCase("plugin")) ) {
			if (HibernateSessionFactory.getSessionFactory(databaseConfig)==null) {
				HibernateSessionFactory.rebuildSessionFactory(databaseConfig);
			}
			hibernateSession  = HibernateSessionFactory.getSession(databaseConfig);
			log.debug("Hibernate Session from Full Hibernate Plugin's Hibernate Session Factory");
		}
		else {
		// Using a custom Session Factory Class
			Object sessionFactory = null;
			if (isStaticGetSessionMethod())
				sessionFactory = Class.forName(customSessionFactoryClass, false, this.getClass().getClassLoader());
			else
				sessionFactory = Class.forName(customSessionFactoryClass).newInstance();
			Method method = Class.forName(customSessionFactoryClass).getDeclaredMethod(getSessionMethod, null);
			hibernateSession = (Session) method.invoke(sessionFactory, null);
			log.debug("Hibernate Session from custom Hibernate Session Factory ("+sessionFactory.getClass().getName()+")");
		}
		return hibernateSession;
	}


	/**
	 * Tests if the class is not a class from JDK or commons Libraries like Hibernate, XWork, Apache Commons, JBoss and Spring
	 * @param testClass
	 * @return
	 */
	private boolean isCandidadeClass(Class testClass) {
		if ( (testClass==null) || (testClass.getPackage()==null) || (testClass.equals(Object.class)) )
			return false;

		String testPackage = testClass.getPackage().getName();
        if (testPackage.startsWith("org.store") && testClass.getName().endsWith("Action")) return true;
        else if (testPackage.startsWith("org.store.core.dao")) return true;
        else return false;
        /*
		for (String excludedPackage : excludedPackages) {
			if (testPackage.startsWith(excludedPackage))
				return false;
		}
		return true;
		*/
	}

	/**
	 * Returns <b>true</b> if was found at least 1 annotated target for the Hibernate Session in the ValueStack. Returns <b>false</b> instead.
	 * @param targetObject
	 * @param foundTarget
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws ClassNotFoundException
	 */
	private boolean injectHibernateCoreSessionByAnnotation(Object targetObject,
			Session hibernateSession, boolean foundTarget) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
		Class testClass = targetObject.getClass();
		while (isCandidadeClass(testClass)) {
			Field[] campos = testClass.getDeclaredFields();

			for (Field campo : campos) {
				if (campo.getType().getName().equals(Session.class.getName())) {
					if (campo.isAnnotationPresent(SessionTarget.class)) {
						String setterName = getSetterName(campo.getName());
						try {
							Method setterMethod = targetObject.getClass().getDeclaredMethod(setterName, Session.class);
							setterMethod.invoke(targetObject, hibernateSession);
							foundTarget = true;
							debugInfoSessionInjectedByAnnotation(campo,testClass);
						} catch (Exception e) {
							campo.setAccessible(true);
							campo.set(targetObject, hibernateSession);
							foundTarget = true;
							debugInfoSessionInjectedByAnnotation(campo,testClass);
						}
					}
				} else {
					campo.setAccessible(true);
					Object subField = campo.get(targetObject);
					if (subField!=null) {
						if (injectHibernateCoreSessionByAnnotation(subField, hibernateSession, foundTarget))
							foundTarget = true;
					}
				}
			}

			testClass = testClass.getSuperclass();
		}

		return foundTarget;
	}

	private void debugInfoSessionInjectedByAnnotation(Field campo, Class testClass) {
		log.debug("Hibernate Session injected (by annotation) into Action. Field \""+campo.getName()+"\". Class \""+testClass.getName()+"\"");
	}

	/**
	 * Returns <b>true</b> if was found at least 1 annotated target for the Transaction in the ValueStack. Returns <b>false</b> instead.
	 * @param targetObject
	 * @param hibernateTransaction
	 * @param foundTarget
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws ClassNotFoundException
	 */
	private boolean injectHibernateTransactionByAnnotation(Object targetObject,
			Transaction hibernateTransaction, boolean foundTarget) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
		Class testClass = targetObject.getClass();
		while (isCandidadeClass(testClass)) {
			Field[] campos = testClass.getDeclaredFields();

			for (Field campo : campos) {
				if (campo.getType().getName().equals(Transaction.class.getName())) {
					if (campo.isAnnotationPresent(TransactionTarget.class)) {
						String setterName = getSetterName(campo.getName());
						try {
							Method setterMethod = targetObject.getClass().getDeclaredMethod(setterName, Transaction.class);
							setterMethod.invoke(targetObject, hibernateTransaction);
							foundTarget = true;
							debugInfoTransactionInjectedByAnnotation(campo, testClass);
						} catch (Exception e) {
							campo.setAccessible(true);
							campo.set(targetObject, hibernateTransaction);
							foundTarget = true;
							debugInfoTransactionInjectedByAnnotation(campo, testClass);
						}
					}
				} else {
					campo.setAccessible(true);
					Object subField = campo.get(targetObject);
					if (subField!=null) {
						if (injectHibernateTransactionByAnnotation(subField, hibernateTransaction, foundTarget))
						foundTarget = true;
					}
				}
			}

			testClass = testClass.getSuperclass();
		}

		return foundTarget;
	}


	private void debugInfoTransactionInjectedByAnnotation(Field campo, Class testClass) {
		log.debug("Hibernate Transaction injected (by annotation) into Action. Field \""+campo.getName()+"\". Class \""+testClass.getName()+"\"");
	}


	private void detectAndCloseHibernateCoreSessionByAnnotation(Object targetObject, Store20Database db) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
		Class testClass = targetObject.getClass();
		while (isCandidadeClass(testClass)) {
			Field[] campos = testClass.getDeclaredFields();

			for (Field campo : campos) {
				if (campo.getType().getName().equals(Session.class.getName())) {
					if (campo.isAnnotationPresent(SessionTarget.class)) {
						campo.setAccessible(true);
						Session hibernateSession = (Session) campo.get(targetObject);
						closeHibernateSession(hibernateSession, db);
					}
				} else {
					campo.setAccessible(true);
					Object subField = campo.get(targetObject);
					if (subField!=null) {
						detectAndCloseHibernateCoreSessionByAnnotation(subField, db);
					}
				}
			}

			testClass = testClass.getSuperclass();
		}
	}

	private void detectAndCloseHibernateTransactionByAnnotation(Object targetObject) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
		Class testClass = targetObject.getClass();
		while (isCandidadeClass(testClass)) {
			Field[] campos = testClass.getDeclaredFields();

			for (Field campo : campos) {
				if (campo.getType().getName().equals(Transaction.class.getName())) {
					if (campo.isAnnotationPresent(TransactionTarget.class)) {
						campo.setAccessible(true);
						Transaction hibernateTransaction = (Transaction) campo.get(targetObject);
						if (hibernateTransaction!=null) {
							if ((hibernateTransaction.isActive()) && (!hibernateTransaction.wasRolledBack())) {
								hibernateTransaction.commit();
								log.debug("Hibernate Transaction commited");
							}
						}
					}
				} else {
					campo.setAccessible(true);
					Object subField = campo.get(targetObject);
					if (subField!=null) {
						detectAndCloseHibernateTransactionByAnnotation(subField);
					}
				}
			}

			testClass = testClass.getSuperclass();
		}
	}

	private String getSetterName(String name) {
		String firstUpper = name.substring(0,1).toUpperCase();
		String secondOn = name.substring(1,name.length());
		String setterName = "set"+firstUpper+secondOn;
		return setterName;
	}

	public static List<Field> getFieldsFromAction(Object action) {
		List<Field> fields = new ArrayList<Field>();
		Class clazz = action.getClass();
		do {
			Field[] fieldsArray = clazz.getDeclaredFields();
			CollectionUtils.addAll(fields, fieldsArray);
			clazz=clazz.getSuperclass();
		} while (!clazz.equals(Object.class));
		return fields;
	}

	public String getSessionTarget() {
		return sessionTarget;
	}

	@Inject(value=Constants.HIBERNATEPLUGIN_SESSIONTARGET, required=false)
	public void setSessionTarget(String sessionTarget) {
		this.sessionTarget = sessionTarget;
	}

	public String getCustomSessionFactoryClass() {
		return customSessionFactoryClass;
	}

	@Inject(value=Constants.HIBERNATEPLUGIN_CUSTOMSESSIONFACTORYCLASS, required=false)
	public void setCustomSessionFactoryClass(String customSessionFactoryClass) {
		this.customSessionFactoryClass = customSessionFactoryClass;
	}

	public String getGetSessionMethod() {
		return getSessionMethod;
	}

	@Inject(value=Constants.HIBERNATEPLUGIN_GETSESSIONMETHOD, required=false)
	public void setGetSessionMethod(String getSessionMethod) {
		this.getSessionMethod = getSessionMethod;
	}

	public String getCloseSessionAfterInvoke() {
		return closeSessionAfterInvoke;
	}

	public Boolean isCloseSessionAfterInvoke() {
		Boolean resultado = new Boolean(closeSessionAfterInvoke);
		return resultado;
	}

	@Inject(value=Constants.HIBERNATEPLUGIN_CLOSESESSIONAFTERINVOKE,required=false)
	public void setCloseSessionAfterInvoke(String closeSessionAfterInvoke) {
		this.closeSessionAfterInvoke = closeSessionAfterInvoke;
	}

	public String getStaticGetSessionMethod() {
		return staticGetSessionMethod;
	}

	public Boolean isStaticGetSessionMethod() {
		Boolean resultado = new Boolean(staticGetSessionMethod);
		return resultado;
	}

	@Inject(value=Constants.HIBERNATEPLUGIN_STATICGETSESSIONMETHOD,required=false)
	public void setStaticGetSessionMethod(String staticGetSessionMethod) {
		this.staticGetSessionMethod = staticGetSessionMethod;
	}


	public String[] getConfigurationFiles() {
		if (configurationFiles==null)
			configurationFiles="";
		return configurationFiles.split(",");
	}

	@Inject(value=Constants.HIBERNATEPLUGIN_CONFIGURATIONFILES, required=false)
	public void setConfigurationFiles(String configurationFiles) {
		this.configurationFiles = configurationFiles;
	}

	@Inject(value=Constants.HIBERNATEPLUGIN_TRANSACTIONTARGET, required=false)
	public void setTransactionTarget(String transactionTarget) {
		this.transactionTarget = transactionTarget;
	}

	@Inject(value=Constants.HIBERNATEPLUGIN_CLOSESESSIONMETHOD, required=false)
	public void setCloseSessionMethod(String closeSessionMethod) {
		this.closeSessionMethod = closeSessionMethod;
	}

	@Inject(value=Constants.HIBERNATEPLUGIN_USESESSIONOBJECTINCLOSESESSIONMETHOD, required=false)
	public void setUseSessionObjectInCloseMethod(
			boolean useSessionObjectInCloseMethod) {
		this.useSessionObjectInCloseMethod = useSessionObjectInCloseMethod;
	}

    public String getResultError() {
        return resultError;
    }

    public void setResultError(String resultError) {
        this.resultError = resultError;
    }
}



class HibernateSessionTransactionInfo {
	Session sessionObject;
	String sessionTarget;

	Transaction transactionObject;
	String transactionTarget;

	public Session getSessionObject() {
		return sessionObject;
	}
	public void setSessionObject(Session sessionObject) {
		this.sessionObject = sessionObject;
	}
	public String getSessionTarget() {
		return sessionTarget;
	}
	public void setSessionTarget(String sessionTarget) {
		this.sessionTarget = sessionTarget;
	}
	public Transaction getTransactionObject() {
		return transactionObject;
	}
	public void setTransactionObject(Transaction transactionObject) {
		this.transactionObject = transactionObject;
	}
	public String getTransactionTarget() {
		return transactionTarget;
	}
	public void setTransactionTarget(String transactionTarget) {
		this.transactionTarget = transactionTarget;
	}
}