package org.store.core.beans;

import org.store.core.beans.utils.StoreBean;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.struts2.json.JSONUtil;
import org.hibernate.event.EventSource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.List;

/**
 * Fees
 */
@Entity
@Table(name = "t_inquiry_question")
public class InquiryQuestion extends BaseBean implements StoreBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Lob
    private String question;
    @Transient
    private HashMap<String, String> mapQuestion;

    private Boolean customAnswer;

    private Integer useInRegister;

    @OneToMany(mappedBy = "question")
    private List<InquiryAnswer> answers;

    // Tienda en la q esta configurado el fee
    @Column(length = 10)
    private String inventaryCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public Boolean getCustomAnswer() {
        return customAnswer;
    }

    public void setCustomAnswer(Boolean customAnswer) {
        this.customAnswer = customAnswer;
    }

    public Integer getUseInRegister() {
        return useInRegister;
    }

    public void setUseInRegister(Integer useInRegister) {
        this.useInRegister = useInRegister;
    }

    public List<InquiryAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<InquiryAnswer> answers) {
        this.answers = answers;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

     public String getQuestion(String lang) {
         try {
             if (mapQuestion==null || mapQuestion.isEmpty()) mapQuestion = (!isEmpty(question)) ? (HashMap) JSONUtil.deserialize(question) : null;
         } catch (Exception ignored) {}
         return (mapQuestion!=null && mapQuestion.containsKey(lang)) ? mapQuestion.get(lang) : null;
    }

    public void setQuestion(String lang, String q) {
        try {
            if (mapQuestion==null) mapQuestion = new HashMap<String,String>();
            mapQuestion.put(lang, q);
            question =  (mapQuestion!=null && !mapQuestion.isEmpty()) ? JSONUtil.serialize(mapQuestion) :null;
        } catch (Exception ignored) {}
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getId())
                .toString();
    }

    public boolean equals(Object other) {
        if ((this == other)) return true;
        if (!(other instanceof InquiryQuestion)) return false;
        InquiryQuestion castOther = (InquiryQuestion) other;
        return new EqualsBuilder()
                .append(this.getId(), castOther.getId())
                .isEquals();
    }

    @Override
    public boolean handlePreDelete(EventSource session) {
        if (answers!=null && !answers.isEmpty())
            for(InquiryAnswer answer : answers)
                session.delete(answer);
        return true;
    }

}