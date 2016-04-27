package org.store.core.beans;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.struts2.json.JSONUtil;
import org.hibernate.criterion.Restrictions;
import org.hibernate.event.EventSource;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.List;

/**
 * Fees
 */
@Entity
@Table(name = "t_inquiry_answer")
public class InquiryAnswer extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Lob
    private String answer;
    @Transient
    private HashMap<String, String> mapAnswer;

    @ManyToOne
    private InquiryQuestion question;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public InquiryQuestion getQuestion() {
        return question;
    }

    public void setQuestion(InquiryQuestion question) {
        this.question = question;
    }

    public HashMap<String, String> getMapAnswer() {
        return mapAnswer;
    }

    public void setMapAnswer(HashMap<String, String> mapAnswer) {
        this.mapAnswer = mapAnswer;
    }

    public String getAnswer(String lang) {
         try {
             if (mapAnswer==null || mapAnswer.isEmpty()) mapAnswer = (!isEmpty(answer)) ? (HashMap) JSONUtil.deserialize(answer) : null;
         } catch (Exception ignored) {}
         return (mapAnswer!=null && mapAnswer.containsKey(lang)) ? mapAnswer.get(lang) : null;
    }

    public void setAnswer(String lang, String a) {
        try {
            if (mapAnswer==null) mapAnswer = new HashMap<String,String>();
            mapAnswer.put(lang, a);
            this.answer =  (mapAnswer!=null && !mapAnswer.isEmpty()) ? JSONUtil.serialize(mapAnswer) :null;
        } catch (Exception ignored) {}
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof InquiryAnswer)) return false;
        InquiryAnswer castOther = (InquiryAnswer) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }

    @Override
    public boolean handlePreDelete(EventSource session) {
        List l = session.createCriteria(InquiryAnswerUser.class).add(Restrictions.eq("answer", this)).list();
        if (l!=null && !l.isEmpty()) for(Object o : l) session.delete(o);
        return true;
    }

}