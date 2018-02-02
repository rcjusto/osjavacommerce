package org.store.core.hibernate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Set the annotated field as a Hibernate Core Session target at Hibernate Plugin for Struts 2.<br/>
 * @author Jos� Yoshiriro - jyoshiriro@gmail.com
 *
 */
@Target({ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface SessionTarget {

}
