package org.store.core.beans;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.struts2.json.JSONException;
import org.apache.struts2.json.JSONUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Comentario de los productos
 */
@Entity
@Table(name = "t_product_review")
public class ProductReview extends BaseBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReview;

    // Usuario de usuario conectado
    private Long idUser;

    // Nombre de usuario
    @Column(length = 512)
    private String userName;

    // Correo electronico del usuario
    @Column(length = 1024)
    private String email;

    // Titulo del comentario
    @Column(length = 512)
    private String title;

    // Comentario
    @Lob
    private String opinion;

    // Puntuacion Promedio
    private Double averageScore;

    // Puntuacion detallada
    @Lob
    private String detailedScore;
    @Transient
    private Map<String, Integer> detailedScoreValues;

    // Visible en el sitio
    private Boolean visible;

    // fecha
    private Date created;

    // Producto
    @ManyToOne
    private Product product;


    public ProductReview() {
        setCreated(Calendar.getInstance().getTime());
    }

    public Long getIdReview() {
        return idReview;
    }

    public void setIdReview(Long idReview) {
        this.idReview = idReview;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }

    public Boolean getVisible() {
        return visible!=null && visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getDetailedScore() {
        return detailedScore;
    }

    public void setDetailedScore(String detailedScore) {
        this.detailedScore = detailedScore;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Map<String, Integer> getDetailedScoreValues() {
        if (detailedScoreValues == null || detailedScoreValues.isEmpty()) deserialize();
        return detailedScoreValues;
    }

    public Integer getDetailedScore(String cad) {
        if (detailedScoreValues == null || detailedScoreValues.size() < 1) deserialize();
        return (detailedScoreValues != null && detailedScoreValues.containsKey(cad)) ? detailedScoreValues.get(cad) : null;
    }

    public void setDetailedScore(String cad, Integer value) {
        if (detailedScoreValues == null) detailedScoreValues = new HashMap<String, Integer>();
        detailedScoreValues.put(cad, value);
        serialize();
    }

    public void serialize() {
        try {
            detailedScore = (detailedScoreValues != null && detailedScoreValues.size() > 0) ? JSONUtil.serialize(detailedScoreValues) : null;
            // Calcular average
            if (detailedScoreValues != null && detailedScoreValues.size() > 0) {
                double res = 0;
                int count = 0;
                for (Integer v : detailedScoreValues.values()) {
                    res += v;
                    count++;
                }
                averageScore = res / count;
            } else averageScore = null;
        }
        catch (JSONException e) {log.error(e.getMessage(), e); }
    }

    public void deserialize() {
        try { detailedScoreValues = (StringUtils.isNotEmpty(detailedScore)) ? (HashMap) JSONUtil.deserialize(detailedScore) : null; }
        catch (JSONException e) { log.error(e.getMessage(), e);  }
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getIdReview())
                .toString();
    }

}
