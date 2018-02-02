package org.store.core.beans;

import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostLoadEvent;
import org.hibernate.event.PostLoadEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.PreDeleteEvent;
import org.hibernate.event.PreDeleteEventListener;
import org.hibernate.event.PreInsertEvent;
import org.hibernate.event.PreInsertEventListener;
import org.hibernate.event.PreUpdateEvent;
import org.hibernate.event.PreUpdateEventListener;

/**
 * Created by IntelliJ IDEA.
 * User: Administrador
 * Date: Apr 20, 2010
 */
public class StoreHibernateEventListener implements PostLoadEventListener,
        PreInsertEventListener, PreUpdateEventListener,
        PostInsertEventListener, PostUpdateEventListener,
        PreDeleteEventListener, PostDeleteEventListener {

    public void onPostLoad(PostLoadEvent postLoadEvent) {
        if (postLoadEvent.getEntity() instanceof BaseBean) {
            BaseBean bean = (BaseBean) postLoadEvent.getEntity();
            bean.handlePostLoad(postLoadEvent.getSession());
        }
    }

    public boolean onPreInsert(PreInsertEvent preInsertEvent) {
        if (preInsertEvent.getEntity() instanceof BaseBean) {
            BaseBean bean = (BaseBean) preInsertEvent.getEntity();
            return bean.handlePreUpdate(preInsertEvent.getSession(),true);
        }
        return false;
    }

    public boolean onPreUpdate(PreUpdateEvent preUpdateEvent) {
        if (preUpdateEvent.getEntity() instanceof BaseBean) {
            BaseBean bean = (BaseBean) preUpdateEvent.getEntity();
            return bean.handlePreUpdate(preUpdateEvent.getSession(), false);
        }
        return false;
    }

    public void onPostInsert(PostInsertEvent postInsertEvent) {
        if (postInsertEvent.getEntity() instanceof BaseBean) {
            BaseBean bean = (BaseBean) postInsertEvent.getEntity();
            bean.handlePostUpdate(postInsertEvent.getSession(), true);
        }
    }

    public void onPostUpdate(PostUpdateEvent postUpdateEvent) {
        if (postUpdateEvent.getEntity() instanceof BaseBean) {
            BaseBean bean = (BaseBean) postUpdateEvent.getEntity();
            bean.handlePostUpdate(postUpdateEvent.getSession(), false);
        }
    }

    public void onPostDelete(PostDeleteEvent postDeleteEvent) {
        if (postDeleteEvent.getEntity() instanceof BaseBean) {
            BaseBean bean = (BaseBean) postDeleteEvent.getEntity();
            bean.handlePostDelete(postDeleteEvent.getSession());
        }
    }

    public boolean onPreDelete(PreDeleteEvent preDeleteEvent) {
        if (preDeleteEvent.getEntity() instanceof BaseBean) {
            BaseBean bean = (BaseBean) preDeleteEvent.getEntity();
            return bean.handlePreDelete(preDeleteEvent.getSession());
        }
        return false;
    }
}
