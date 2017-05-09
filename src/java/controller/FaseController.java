package controller;

import facade.FaseFacade;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import model.Fase;
import org.primefaces.context.RequestContext;

@ManagedBean(name = "fase")
@SessionScoped
@Stateless
public class FaseController implements Serializable {

    private static final long serialVersionUID = 1;
    String opt;

    Boolean afinidades;
    @EJB
    private FaseFacade faseFacade;

    Date data = new Date();
    private Fase fase = new Fase(false, false, false, false, false, data);

    public Fase getFase() {
        return this.fase;
    }

    public void setFase(Fase fase) {
        this.fase = fase;
    }

    public Boolean getAfinidades() {
        return afinidades;
    }

    public void setAfinidades(Boolean afinidades) {
        this.afinidades = afinidades;
    }

    public String getOpt() {
        return this.opt;
    }

    public void setOpt(String opt) {
        this.opt = opt;
    }

    //Padronizar o formato da data
    private Date formData(Date dataAtual) {
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        String mysqlDateString = formatter.format(dataAtual);
        try {
            dataAtual = formatter.parse(mysqlDateString);
        } catch (ParseException ex) {
            Logger.getLogger(Fase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dataAtual;
    }

    //Converte para String a opção selecionada
    public String optToString(String opt) {
        if (opt.equals("fase1")) {
            opt = "Fase I - Planejamento Anual";
        } else if (opt.equals("fase21")) {
            opt = "Fase II - Primeiro Quadrimestre ";
        } else if (opt.equals("fase22")) {
            opt = "Fase II - Segundo Quadrimestre ";
        } else if (opt.equals("fase23")) {
            opt = "Fase II - Terceiro Quadrimestre ";
        }
        return opt;
    }

    //Verifica quais as opções selecionadas
    public void Verifica() {
        if ("fase1".equals(this.opt)) {
            this.fase.setFase1(true);
            Date data1 = new Date();
            this.fase.setDataAtual(formData(data1));
        } else if ("fase21".equals(this.opt)) {
            this.fase.setFase2_quad1(true);
            Date data2 = new Date();
            this.fase.setDataAtual(formData(data2));
        } else if ("fase22".equals(this.opt)) {
            this.fase.setFase2_quad2(true);
            Date data3 = new Date();
            this.fase.setDataAtual(formData(data3));
        } else if ("fase23".equals(this.opt)) {
            this.fase.setFase2_quad3(true);
            Date data4 = new Date();
            this.fase.setDataAtual(formData(data4));
        } else {
            this.fase.setFase1(false);
            this.fase.setFase2_quad1(false);
            this.fase.setFase2_quad2(false);
            this.fase.setFase2_quad3(false);
            Date data5 = new Date();
            this.fase.setDataAtual(formData(data5));
        }
    }

    //Desebilita todas as fases do sistema
    public void desabilitarFases() {
        this.fase.setAfinidades(false);
        this.fase.setFase1(false);
        this.fase.setFase2_quad1(false);
        this.fase.setFase2_quad2(false);
        this.fase.setFase2_quad3(false);
        Date data6 = new Date();
        this.fase.setDataAtual(formData(data6));
        this.faseFacade.save(this.fase);

        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Todas as fases foram desabilitadas!");
        RequestContext.getCurrentInstance().showMessageInDialog(message);
    }

    //Salva as informações
    public void saveData() {
        this.fase.setAfinidades(afinidades);
        this.fase.setFase1(false);
        this.fase.setFase2_quad1(false);
        this.fase.setFase2_quad2(false);
        this.fase.setFase2_quad3(false);
        Date data7 = new Date();
        this.fase.setDataAtual(formData(data7));
        this.Verifica();

        if ((afinidades) || !("".equals(opt))) {
            this.faseFacade.save(this.fase);
            if (afinidades && ("".equals(opt))) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Afinidades habilitada!");
                RequestContext.getCurrentInstance().showMessageInDialog(message);
            } else if (afinidades && (!("".equals(opt)))) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Afinidades e " + optToString(opt) + " habilitadas!");
                RequestContext.getCurrentInstance().showMessageInDialog(message);
            } else {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "", optToString(opt) + " habilitada!");
                RequestContext.getCurrentInstance().showMessageInDialog(message);
            }
        } else {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Nenhuma opção selecionada!");
            RequestContext.getCurrentInstance().showMessageInDialog(message);
        }
    }
}
