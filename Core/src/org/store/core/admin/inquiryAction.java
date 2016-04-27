package org.store.core.admin;

import org.store.core.beans.InquiryAnswer;
import org.store.core.beans.InquiryQuestion;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.beans.utils.DataNavigator;
import org.store.core.globals.StoreMessages;
import org.apache.commons.lang.ArrayUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class inquiryAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        question = (InquiryQuestion) dao.get(InquiryQuestion.class, idQuestion);
    }

    public String list() throws Exception {
        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                InquiryQuestion bean = (InquiryQuestion) dao.get(InquiryQuestion.class, id);
                if (bean != null) dao.delete(bean);
            }
            dao.flushSession();
        }
        DataNavigator nav = new DataNavigator(request, "questions");
        nav.setListado(dao.getQuestions(nav));
        addToStack("questions", nav);
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.inquiry.list"), null, null));
      return SUCCESS;
    }

    public String edit() throws Exception {
        if (question != null) {
            if (selecteds != null && selecteds.length > 0) {
                for (Long id : selecteds) {
                    InquiryAnswer bean = (InquiryAnswer) dao.get(InquiryAnswer.class, id);
                    if (bean != null && question.equals(bean.getQuestion())) dao.delete(bean);
                }
                dao.flushSession();
            }
        }
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.inquiry.list"), url("listinquiry","/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(question!=null ? "admin.inquiry.modify" : "admin.inquiry.new"), null, null));
       return SUCCESS;
    }

    public String save() throws Exception {
        if (question != null) {
            question.setInventaryCode(getStoreCode());
            if (questionText != null && ArrayUtils.isSameLength(questionText, getLanguages())) {
                for (int i = 0; i < getLanguages().length; i++)
                    question.setQuestion(getLanguages()[i], questionText[i]);
            }
            dao.save(question);
        }
        return SUCCESS;
    }

    @Action(value = "inquiryaddanswer", results = @Result(type = "redirectAction", location = "editinquiry?idQuestion=${question.id}"))
    public String addAnswer() {
        if (question != null) {
            if (answerText != null && ArrayUtils.isSameLength(answerText, getLanguages())) {
                InquiryAnswer answer = new InquiryAnswer();
                answer.setQuestion(question);
                for (int i = 0; i < getLanguages().length; i++)
                    answer.setAnswer(getLanguages()[i], answerText[i]);
                dao.save(answer);
            }
        }
        return SUCCESS;
    }

    private InquiryQuestion question;
    private Long idQuestion;
    private String[] questionText;
    private String[] answerText;

    public InquiryQuestion getQuestion() {
        return question;
    }

    public void setQuestion(InquiryQuestion question) {
        this.question = question;
    }

    public Long getIdQuestion() {
        return idQuestion;
    }

    public void setIdQuestion(Long idQuestion) {
        this.idQuestion = idQuestion;
    }

    public String[] getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String[] questionText) {
        this.questionText = questionText;
    }

    public String[] getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String[] answerText) {
        this.answerText = answerText;
    }
}
