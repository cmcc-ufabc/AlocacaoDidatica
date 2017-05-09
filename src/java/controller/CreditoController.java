package controller;

import facade.CreditoFacade;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import model.Credito;
import model.Docente;
import model.OfertaDisciplina;
import model.Pessoa;
import org.primefaces.event.SelectEvent;

@Named(value = "creditoController")
@SessionScoped
public class CreditoController extends Filtros implements Serializable{
    
    private ExternalContext externalContext;
    private LoginBean loginBean;
    private Docente docente;
    
    private static final long serialVersionUID = 1L;
    
    public CreditoController() {
        externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sessionMap = externalContext.getSessionMap();
        loginBean = (LoginBean) sessionMap.get("loginBean");
        docente = loginBean.getDocente();
    }
    
    @EJB
    private CreditoFacade creditoFacade;
        
    //----------------------Fase I Disponibilidade----------------------------------------
    //private Docente docente;
    
    private double quantidade;

    public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    //Método para buscar a quantidade de créditos salvos pelo docente
    public double creditosQuad(Long quad){
        
        //docente = (Docente) LoginBean.getUsuario();
        Integer quadrimestre = (int) (long) quad;
        double credito = 0;
        List<Credito> all = docente.getCreditos();
        for(Credito c : all){
            if(c.getQuadrimestre() == quadrimestre){
                credito = c.getQuantidade();
            }
        }
        
        return credito;        
    }

    //Soma créditos para o docente
    public void adicionaCredito(SelectEvent event) {
        OfertaDisciplina oferta = (OfertaDisciplina) event.getObject();

        switch(oferta.getFuncao()){
            case "Teoria":
                quantidade += oferta.getT();
                break;
            case "Pratica":
                quantidade += oferta.getP();
                break;
            default:
                quantidade += oferta.getT() + oferta.getP();
        }
    }
       
    //Reduz créditos para o docente
    public void diminuiCredito(SelectEvent event){
        OfertaDisciplina oferta = (OfertaDisciplina) event.getObject();
        quantidade -= oferta.getT();
    }
}

