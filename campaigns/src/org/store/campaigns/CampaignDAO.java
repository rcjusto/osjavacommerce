package org.store.campaigns;

import org.store.campaigns.beans.Campaign;
import org.store.campaigns.beans.CampaignUser;
import org.store.campaigns.beans.CampaignUserClick;
import org.store.campaigns.beans.DesignedMail;
import org.store.core.beans.Mail;
import org.store.core.beans.User;
import org.store.campaigns.beans.UserGroup;
import org.store.campaigns.beans.UserGroupMember;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.dao.HibernateDAO;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.Type;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rogelio Caballero
 * 17/12/11 16:17
 */
public class CampaignDAO extends HibernateDAO {

    public CampaignDAO(HibernateDAO dao) {
        super(dao.gethSession(), dao.getStoreCode());
    }

    public List<Campaign> getCampaigns(DataNavigator nav) {
        Criteria cri = createCriteriaForStore(Campaign.class);
        if (nav != null) {
            cri.setProjection(Projections.countDistinct("id"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(null);
            cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            if (nav.getTotalRows() > 0) {
                cri.setMaxResults(nav.getPageRows());
                cri.setFirstResult(nav.getFirstRow() - 1);
            }
        }
        cri.addOrder(Order.desc("dateToSend"));
        cri.addOrder(Order.asc("id"));
        return cri.list();
    }

    public List getCampaignEmailStatus(Long idCampaign) {
        if (idCampaign == null) return null;
        return gethSession().createCriteria(Mail.class)
                .add(Restrictions.eq("reference", Campaign.EMAIL_REFERENCE_PREFIX + idCampaign.toString()))
                .setProjection(Projections.projectionList().add(Projections.groupProperty("status")).add(Projections.count("idMail")))
                .list();


    }

    public void removeMailsForCampaign(Campaign campaign) {
        gethSession().createQuery("delete Mail m where m.reference = :reference").setString("reference", Campaign.EMAIL_REFERENCE_PREFIX + campaign.getId().toString()).executeUpdate();
        gethSession().createQuery("delete CampaignUser c where c.idCampaign = :id").setLong("id", campaign.getId()).executeUpdate();
    }

    public List<UserGroup> getUserGroups() {
        return createCriteriaForStore(UserGroup.class).addOrder(Order.asc("groupName")).list();
    }

    public String isUsedUserGroup(UserGroup bean) {
        StringBuilder respuesta = new StringBuilder();
        List l;
        l = gethSession().createCriteria(Campaign.class).add(Restrictions.eq("userGroup", bean)).list();
        if (l != null && !l.isEmpty()) {
            respuesta.append("Used in campains:");
            for (int i = 0; i < Math.min(10, l.size()); i++)
                respuesta.append(" ").append(((Campaign) l.get(i)).getId());
            respuesta.append(" ...");
            return respuesta.toString();
        }
        return null;
    }

    public void deleteUserGroup(UserGroup bean) {
        gethSession().createQuery("delete Campaign b where b.userGroup = :bean").setEntity("bean", bean).executeUpdate();
        gethSession().delete(bean);
    }

    public List<DesignedMail> getDesignedMails(DataNavigator nav) {
        Criteria cri = createCriteriaForStore(DesignedMail.class).addOrder(Order.desc("id"));
        if (nav != null) {
            cri.setProjection(Projections.countDistinct("id"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(null);
            cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            if (nav.getTotalRows() > 0) {
                cri.setMaxResults(nav.getPageRows());
                cri.setFirstResult(nav.getFirstRow() - 1);
            }
        }
        cri.addOrder(Order.desc("id"));
        return cri.list();
    }

    public List<User> getAllUsersForGroup(UserGroup group) {
        Criteria cri = gethSession().createCriteria(UserGroupMember.class).add(Restrictions.eq("group", group)).addOrder(Order.desc("id")).setProjection(Projections.property("user"));
        return cri.list();
    }

    public List<UserGroupMember> getUsersForGroup(DataNavigator nav, UserGroup group) {
        Criteria cri = gethSession().createCriteria(UserGroupMember.class).add(Restrictions.eq("group", group)).addOrder(Order.desc("user.idUser"));
        if (nav != null) {
            cri.setProjection(Projections.countDistinct("id"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(null);
            cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            if (nav.getTotalRows() > 0) {
                cri.setMaxResults(nav.getPageRows());
                cri.setFirstResult(nav.getFirstRow() - 1);
            }
        }
        cri.addOrder(Order.desc("id"));
        return cri.list();
    }

    public UserGroupMember getMember(UserGroup group, Long idUser) {
        List<UserGroupMember> list = gethSession().createCriteria(UserGroupMember.class)
                .add(Restrictions.eq("group", group))
                .add(Restrictions.eq("user.idUser", idUser))
                .list();
        return (list!=null && !list.isEmpty()) ? list.get(0) : null;
    }

    public List<Object[]> getCampaignStatus(Long idCampaign) {
        return gethSession().createCriteria(CampaignUser.class)
                .add(Restrictions.eq("idCampaign", idCampaign))
                .setProjection(Projections.projectionList().add(Projections.count("opened")).add(Projections.count("clicked")))
        .list();

    }

    public Map<String, Integer> getCampaignDatesDetails(Long idCampaign, int hours, Date start, Date end, String field) {

        Criteria criteria = gethSession().createCriteria(CampaignUser.class)
                .add(Restrictions.eq("idCampaign", idCampaign))
                .add(Restrictions.ge(field, start))
                .add(Restrictions.le(field, end));


        ProjectionList projList = Projections.projectionList();
                projList.add( Projections.count(field) );
                projList.add( Projections.sqlGroupProjection(
                    String.format( "DATE_ADD( DATE( %s_."+field+" ), INTERVAL( HOUR( %s_."+field+" ) - HOUR( %s_."+field+") %% %d ) HOUR) as hourly", criteria.getAlias(), criteria.getAlias(), criteria.getAlias(), hours ),
                    String.format( "DATE_ADD( DATE( %s_."+field+" ), INTERVAL( HOUR( %s_."+field+") - HOUR( %s_."+field+" ) %% %d ) HOUR)", criteria.getAlias(), criteria.getAlias(), criteria.getAlias(), hours ),
                    new String[]{ "hourly" },
                    new Type[]{ Hibernate.TIMESTAMP } )
                );
                criteria.setProjection( projList );

        List<Object[]> list = criteria.setCacheable(false).list();

        Map<String, Integer> result = new HashMap<String, Integer>();


        return result;
    }


    public Object getCampaignStatsInRange(Long idCampaign, Date start, Date end, String field) {
        return gethSession().createCriteria(CampaignUser.class)
                .add(Restrictions.eq("idCampaign", idCampaign))
                .add(Restrictions.ge(field, start))
                .add(Restrictions.le(field, end))
                .setProjection(Projections.count(field))
                .uniqueResult();
    }

    public List<Object[]> getClickedLinks(Long id) {
        List<Object[]> list = gethSession().createCriteria(CampaignUserClick.class)
            .add(Restrictions.eq("idCampaign",id))
            .setProjection(Projections.projectionList().add(Projections.groupProperty("url")).add(Projections.count("id"))).list();
        Collections.sort(list, new Comparator<Object[]>() {
            public int compare(Object[] o1, Object[] o2) {
                Long c1 = (o1!=null && o1.length>1 && o1[1]!=null && o1[1] instanceof Number) ? ((Number)o1[1]).longValue() : 0;
                Long c2 = (o2!=null && o2.length>1 && o2[1]!=null && o2[1] instanceof Number) ? ((Number)o2[1]).longValue() : 0;
                return c2.compareTo(c1);
            }
        });
        return list;
    }

    public List getMembers(DataNavigator nav, Long id, String filter) {
        Criteria cri = gethSession().createCriteria(CampaignUser.class).add(Restrictions.eq("idCampaign", id));
        if ("opened".equalsIgnoreCase(filter)) {
            cri.add(Restrictions.isNotNull("opened"));
            cri.addOrder(Order.asc("opened"));
        } else if ("clicked".equalsIgnoreCase(filter)) {
            cri.add(Restrictions.isNotNull("clicked"));
            cri.addOrder(Order.asc("clicked"));
        } else {
            cri.addOrder(Order.asc("idUser"));
        }

        if (nav != null) {
            cri.setProjection(Projections.countDistinct("id"));
            Number total = (Number) cri.uniqueResult();
            nav.setTotalRows(total != null ? total.intValue() : 0);
            cri.setProjection(null);
            cri.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            if (nav.getTotalRows() > 0) {
                cri.setMaxResults(nav.getPageRows());
                cri.setFirstResult(nav.getFirstRow() - 1);
            }
        }

        List<CampaignUser> list = cri.list();
        if (list!=null && !list.isEmpty()) {
            for(CampaignUser cu : list) {
                User u = (User) get(User.class, cu.getIdUser());
                if (u!=null) cu.addProperty("user", u);

                Object obj = gethSession().createCriteria(CampaignUserClick.class)
                        .add(Restrictions.eq("idCampaign", id))
                        .add(Restrictions.eq("idUser", cu.getIdUser()))
                        .setProjection(Projections.count("id"))
                        .uniqueResult();
                cu.addProperty("clicks", (obj!=null && obj instanceof Number) ? ((Number)obj).intValue() : 0);
            }
        }

        return list;
    }

    public List<CampaignUserClick> getClicks(Long idCampaign, Long idUser) {
        return gethSession().createCriteria(CampaignUserClick.class)
                .add(Restrictions.eq("idCampaign", idCampaign))
                .add(Restrictions.eq("idUser", idUser))
                .list();
    }
}
