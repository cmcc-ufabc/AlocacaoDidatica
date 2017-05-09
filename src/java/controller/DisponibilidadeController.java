package controller;

import facade.CreditoFacade;
import facade.DispFacade;
import facade.DisponibilidadeFacade;
import facade.OfertaDisciplinaFacade;
import facade.PreferenciasFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;
import javax.swing.event.ChangeEvent;
import model.Afinidade;
import model.Credito;
import model.Disciplina;
import model.Disp;
import model.Disponibilidade;
import model.Docente;
import model.Pessoa;
import model.OfertaDisciplina;
import model.Preferencias;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.ReorderEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.UnselectEvent;
import util.DispDataModel;
import util.DisponibilidadeDataModel;
import util.OfertaDisciplinaDataModel;
import util.OfertaDisciplinaLazyModel;

@Named(value = "disponibilidadeController")
@SessionScoped
public class DisponibilidadeController extends Filtros implements Serializable {

    private ExternalContext externalContext;
    private LoginBean loginBean;
    
    //Construtor (pega o usuario Logado)
    public DisponibilidadeController() {
        externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sessionMap = externalContext.getSessionMap();
        loginBean = (LoginBean) sessionMap.get("loginBean");
        docente = loginBean.getDocente();
    }

    private Disponibilidade disponibilidade;

    @EJB
    private OfertaDisciplinaFacade turmasFacade;

    @EJB
    private DisponibilidadeFacade disponibilidadeFacade;

    @EJB
    private CreditoFacade creditoFacade;

//-----------------------------------Fase I Alocacao-----------------------------------------------------    
    //Quantidade de creditos por quadrimestre para o planejamento anual
    private double creditosPlanejados;

    public double getCreditosPlanejados() {
        return docente.getCreditoQuad(quadrimestre);
    }

    private double creditosPlanejados2;

    public double getCreditosPlanejados2() {
        return docente.getCreditoQuad(quadrimestre);
    }

    public void setCreditosPlanejados2(double creditosPlanejados2) {
        this.creditosPlanejados2 = creditosPlanejados2;
    }

    public void setCreditosPlanejados(double creditosPlanejados) {
        this.creditosPlanejados = creditosPlanejados;
    }

    //Vai mudando de acordo com a oferta de disciplina escolhida ou retirada pelo docente
    private double creditosEscolhidos;

    //Busca os créditos escolhidos
    public double getCreditosEscolhidos() {
        //Fazer as consultas de disponibilidades e ofertadisciplina e a contagem de tp
        //Busca de disponibilidades do docente no quadrimestre
        pessoa = docente;
        //List<Disponibilidade> quad = disponibilidadeFacade.findByDocenteQuad(pessoa, quadrimestre);
        List<Disp> quad = dispFacade.findByDocenteQuad(pessoa, quadrimestre);
        //Busca de oferta de disciplinas no quadrimestre
        List<OfertaDisciplina> oferta = turmasFacade.findAllQuad(quadrimestre);
        //For para listar apenas as disciplinas planejadas pelo docente
        List<OfertaDisciplina> planejados = new ArrayList<>();
        long id_o = 0;
        long id_d = 0;
        for (OfertaDisciplina o : oferta) {
            for (Disp d : quad) {
                id_o = o.getID();
                id_d = d.getOfertaDisciplina().getID();
                if (id_o == id_d) {
                    planejados.add(o);
                }
            }
        }
        //Contagem de créditos
        int creditos = 0;
        for (OfertaDisciplina o : planejados) {
            for (Disp d : quad) {
                id_o = o.getID();
                id_d = d.getOfertaDisciplina().getID();
                if (id_o == id_d) {
                    if (d.getTp().equals("Teoria")) {
                        creditos += o.getT();
                    } else if (d.getTp().equals("Pratica")) {
                        creditos += o.getP();
                    } else {
                        creditos += o.getP() + o.getT();
                    }
                }
            }
        }
        int total = creditos;

        return creditosEscolhidos = total;
    }

    public void setCreditosEscolhidos(double creditosEscolhidos) {
        this.creditosEscolhidos = creditosEscolhidos;
    }

    public boolean changeColor() {
        double cred = docente.getCreditoQuad(quadrimestre);
        return creditosEscolhidos > cred;
    }

    /**
     * Associa a quantidade de créditos ao quadrimestre atual e ao docente que
     * está fazendo o planejamento
     * @param q Long
     */
    public void salvarCreditos(Long q) {

        Integer quad = (int) (long) q;

        //Verifica se já existe um planejamento de crédito para aquele quadrimestre
        List<Credito> listCreditos = docente.getCreditos();

        Credito credito = creditoFacade.creditoQuadrimestre(docente, quad);
        if (credito != null) { //Já existe um planejamento para o quadrimestre
            try {
                int posicao = listCreditos.indexOf(credito);
                credito.setQuantidade(creditosPlanejados);
                listCreditos.add(posicao, credito);
                docente.setCreditos(listCreditos);
                creditoFacade.edit(credito);
                JsfUtil.addSuccessMessage("Créditos editados com sucesso!");
            } catch (Exception e) {
                JsfUtil.addErrorMessage(e, "Ocorreu um erro de persistência, não foi possível editar os créditos " + e.getMessage());
            }
        } else {
            credito = new Credito();
            credito.setQuadrimestre(quad);
            credito.setQuantidade(creditosPlanejados);
            credito.setDocente(docente);
            listCreditos.add(credito);
            docente.setCreditos(listCreditos);
            try {
                creditoFacade.save(credito);
                JsfUtil.addSuccessMessage("Créditos salvos com sucesso!");
            } catch (Exception e) {
                JsfUtil.addErrorMessage(e, "Ocorreu um erro de persistência, não foi possível salvar os créditos " + e.getMessage());
            }
        }
        creditosPlanejados = 0.0;
        //Atualiza o usuário logado
        //LoginBean.setUsuario(docente);
    }

    /**
     * Associa a quantidade de créditos ao quadrimestre atual e ao docente que
     * está fazendo o planejamento
     */
    public void salvarCreditos() {

        //Verifica se já existe um planejamento de crédito para aquele quadrimestre
        List<Credito> listCreditos = docente.getCreditos();

        Credito credito = creditoFacade.creditoQuadrimestre(docente, quadrimestre);
        if (credito != null) { //Já existe um planejamento para o quadrimestre
            try {
                int posicao = listCreditos.indexOf(credito);
                credito.setQuantidade(creditosPlanejados);
                listCreditos.add(posicao, credito);
                docente.setCreditos(listCreditos);
                creditoFacade.edit(credito);
                JsfUtil.addSuccessMessage("Créditos editados com sucesso!");
            } catch (Exception e) {
                JsfUtil.addErrorMessage(e, "Ocorreu um erro de persistência, não foi possível editar os créditos " + e.getMessage());
            }
        } else {
            credito = new Credito();
            credito.setQuadrimestre(quadrimestre);
            credito.setQuantidade(creditosPlanejados);
            credito.setDocente(docente);
            listCreditos.add(credito);
            docente.setCreditos(listCreditos);
            try {
                creditoFacade.save(credito);

                JsfUtil.addSuccessMessage("Créditos salvos com sucesso!");
            } catch (Exception e) {
                JsfUtil.addErrorMessage(e, "Ocorreu um erro de persistência, não foi possível salvar os créditos " + e.getMessage());
            }
        }
        creditosPlanejados = 0.0;
        //Atualiza o usuário logado
        //LoginBean.setUsuario(docente);
    }
    
    private int preferencias;
    
    private String informacoes;
    
    public int getPreferencias(){
        return preferencias;
    }
    
    public void setPreferencias(int preferencias){
        this.preferencias = preferencias;
    }
    
    public String getInformacoes(){
        return informacoes;
    }
    
    public void setInformacoes(String informacoes){
        this.informacoes = informacoes;
    }

    @EJB
    private PreferenciasFacade preferenciaFacade;
    
    //Método para salvar a preferência marcada pelo docente sobre a configuração de horários desejada
    public void salvaPreferencias(){
        Preferencias preferencia = preferenciaFacade.verificaPreferencia(pessoa.getID(), quadrimestre);
        if(preferencia.getID() != null){
            preferencias = preferencia.getPreferenciaHorarios();
            informacoes = preferencia.getObservacoes();
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Warning", "Já existe um registro com as suas preferências!"));
        } else{
            preferencia.setPreferenciaHorarios(preferencias);
            preferencia.setObservacoes(informacoes);
            preferencia.setQuadrimestre(quadrimestre);
            preferencia.setPessoa_id(pessoa.getID());
            preferenciaFacade.save(preferencia);

            informacoes = null;
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Successful", "Informações salvas com Sucesso!"));
        }
    }
    
    //Método para editar as informações já salvas pelo docente
    public void editarPreferencias(){
        Preferencias preferencia = preferenciaFacade.verificaPreferencia(pessoa.getID(), quadrimestre);
        preferencia.setObservacoes(informacoes);
        preferencia.setPreferenciaHorarios(preferencias);
        preferenciaFacade.merge(preferencia);
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Successful", "Informações alteradas com Sucesso!"));
    }

    private List<OfertaDisciplina> ofertasEtapa1;

    public List<OfertaDisciplina> getOfertasEtapa1() {
        if (ofertasEtapa1 == null) {
            ofertasEtapa1 = new ArrayList<>();
        }
        return ofertasEtapa1;
    }

    public void setOfertasEtapa1(List<OfertaDisciplina> ofertasEtapa1) {
        this.ofertasEtapa1 = ofertasEtapa1;
    }

    private Docente docente;

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }

    //Data Model das OfertaDisciplina da Etapa I
    private OfertaDisciplinaLazyModel dataModel;
    
    //Carrega o lazymodel de ofertas
    public OfertaDisciplinaLazyModel getDataModel() {
        if (dataModel == null) {
            List<OfertaDisciplina> turmas = turmasFacade.findAllQuad(quadrimestre);
            List<OfertaDisciplina> disponivel = new ArrayList();
            //Buscar turmas já salvas pelo docente
            List<Disp> salvas = dispFacade.findByDocenteQuad(docente, quadrimestre);
            List<OfertaDisciplina> retirar = new ArrayList();
            for(Disp salva: salvas){
                retirar.add(salva.getOfertaDisciplina());
            }
            //Remover turmas salvas do datamodel
            for(OfertaDisciplina turma: turmas){
                if(!retirar.contains(turma)){
                    disponivel.add(turma);
                }
            }
            if(!turno.equals("Ambos") || !campus.equals("Ambos") || !valor.equals("nao")){
                if(valor.equals("sim") && turno.equals("Ambos") && campus.equals("Ambos")){
                    turno = "";
                    campus = "";
                } else if(!turno.equals("Ambos") && campus.equals("Ambos")){
                    campus = "";
                } else if(!campus.equals("Ambos") && turno.equals("Ambos")){
                    turno = "";
                }
                filtrarTurmasQuad();
            } else if(cursos != null){
                filtrarCursos();
            } else{
                dataModel = new OfertaDisciplinaLazyModel(disponivel);
            }
            //dataModel = new OfertaDisciplinaLazyModel(disponivel);
        }
        return dataModel;
    }

    public void setDataModel(OfertaDisciplinaLazyModel dataModel) {
        this.dataModel = dataModel;
    }

    private double quantidadeCreditos;

    public double getQuantidadeCreditos() {
        return quantidadeCreditos;
    }

    public void setQuantidadeCreditos(double quantidadeCreditos) {
        this.quantidadeCreditos = quantidadeCreditos;
    }

    //Guarda se o docente escolheu teoria, pratica ou ambas para determinada
    //oferta de disciplina
    private List<String> selectedOptions;

    public List<String> getSelectedOptions() {
        return selectedOptions;
    }

    public void setSelectedOptions(List<String> selectedOptions) {
        this.selectedOptions = selectedOptions;
    }

    /**
     * Preenche o atributo "função" de uma oferta de disciplina que
     * indica se o docente vai dar Teoria, Prática ou Ambas
     * @param oferta
     */
    public void setFuncaoOferta(OfertaDisciplina oferta) {
        if (selectedOptions == null) {
            oferta.setFuncao("T e P");
        } else if (selectedOptions.size() > 1) {
            oferta.setFuncao("T e P");
        } else if (selectedOptions.get(0).equals("T")) {
            oferta.setFuncao("Teoria");
        } else if (selectedOptions.get(0).equals("P")) {
            oferta.setFuncao("Pratica");
        }
    }

    /**
     * Quando o docente escolhe uma oferta de disciplina, adiciona os créditos
     * da teoria e/ou prática a variável creditosEscolhidos
     * @param event
     */
    public void adicionaCredito(SelectEvent event) {
        OfertaDisciplina oferta = (OfertaDisciplina) event.getObject();

        switch (oferta.getFuncao()) {
            case "Teoria":
                creditosEscolhidos += oferta.getT();
                break;
            case "Pratica":
                creditosEscolhidos += oferta.getP();
                break;
            default:
                creditosEscolhidos += oferta.getT() + oferta.getP();
        }
    }

    /**
     * Quando o docente retira uma oferta de disciplina, remove os créditos da
     * teoria e/ou prática da variável creditosEscolhidos
     * @param event
     */
    public void removeCredito(UnselectEvent event) {
        OfertaDisciplina oferta = (OfertaDisciplina) event.getObject();

        switch (oferta.getFuncao()) {
            case "Teoria":
                creditosEscolhidos -= oferta.getT();
                break;
            case "Pratica":
                creditosEscolhidos -= oferta.getP();
                break;
            default:
                creditosEscolhidos -= oferta.getT() + oferta.getP();
        }
    }

    //guarda as disponibilidades escolhidas pelo docente em cada quadrimestre
    private DisponibilidadeDataModel dispdataModel;

    public DisponibilidadeDataModel getDispdataModel() {
        if (dispdataModel == null) {
            List<Disponibilidade> d = disponibilidadeFacade.findByDocente(docente);
            dispdataModel = new DisponibilidadeDataModel(d);
        }
        return dispdataModel;
    }

    //Busca as disponibilidades do docente
    public DisponibilidadeDataModel getDispdataModel(int quad) {
        if (dispdataModel == null) {
            List<Disponibilidade> d = disponibilidadeFacade.findByDocenteQuad(docente, quad);
            dispdataModel = new DisponibilidadeDataModel(d);
        }
        return dispdataModel;
    }

    public void setDispdataModel(DisponibilidadeDataModel dispdataModel) {
        this.dispdataModel = dispdataModel;
    }

    private DisponibilidadeDataModel dispdataModel2;

    public DisponibilidadeDataModel getDispdataModel2() {
        if (dispdataModel2 == null) {
            List<Disponibilidade> d = disponibilidadeFacade.findByDocenteQuad(docente, quadrimestre);
            dispdataModel2 = new DisponibilidadeDataModel(d);
        }
        return dispdataModel2;
    }

    public void setDispdataModel2(DisponibilidadeDataModel dispdataModel2) {
        this.dispdataModel2 = dispdataModel2;
    }

    //Salva a disponibilidade do docente
    public void salvarDisponibilidade() {
        for (OfertaDisciplina oferta : ofertasEtapa1) {
            //Regarrega o objeto turma, inicializando a Colecao de Disponibilidades(Lazy)
            oferta = turmasFacade.inicializarColecaoDisponibilidades(oferta);
//            disponibilidade = new Disponibilidade("", docente, t);
            String funcao = oferta.getFuncao();
            if (funcao == null) {
                funcao = "T e P";
            }
            disponibilidade = new Disponibilidade("", funcao, docente, oferta);
            disponibilidadeFacade.save(disponibilidade);
        }
        dispdataModel = null;
        dispdataModel2 = null;
    }

    //Novo Disponibilidade-------------------------------------------------------
    private DispDataModel dDataModel;

    private Disp dispo;

    @EJB
    private DispFacade dispFacade;

    //Preenche o datamodel
    public DispDataModel getDDataModel() {
        if (dDataModel == null) {
            List<Disp> d = dispFacade.findByDocente(docente);
            dDataModel = new DispDataModel(d);
        }
        return dDataModel;
    }

    public DispDataModel getDispDataModel(int quad) {
        if (dDataModel == null) {
            List<Disp> d = dispFacade.findByDocenteQuad(docente, quad);
            dDataModel = new DispDataModel(d);
        }
        return dDataModel;
    }

    public void setDispdataModel(DispDataModel dDataModel) {
        this.dDataModel = dDataModel;
    }

    //Evento de reordenação de disponibilidades
    public void onRowReorder(ReorderEvent event) {
        //FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Row Moved", "From: " + event.getFromIndex() + ", To:" + event.getToIndex());
        int ini = event.getFromIndex();
        int fim = event.getToIndex();
        List<Disp> d = dispFacade.findByDocenteQuad(docente, quadrimestre);

        Disp vet[] = new Disp[d.size()+1];
        int ordem = 0;
        for (Disp disp : d) {
            ordem = Integer.parseInt(disp.getOrdemPreferencia());
            vet[ordem] = disp;
        }
        Disp origem = vet[ini+1];
        origem.setOrdemPreferencia(Integer.toString(fim+1));
        dispFacade.merge(origem);
        Disp destino = vet[fim+1];
        destino.setOrdemPreferencia(Integer.toString(ini+1));
        dispFacade.merge(destino);
        vet[fim+1] = origem;
        vet[ini+1] = destino;
        List<Disp> ordenada = new ArrayList();
        Disp verifica;
        for (int i = 0; i < d.size()+1; i++) {
            verifica = vet[i];
            if (!(verifica == null)) {
                ordenada.add(vet[i]);
            }
        }
        dModel2 = new DispDataModel(ordenada);
        //FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    private DispDataModel dModel2;

    //Segundo datamodel
    public DispDataModel getDModel2() {
        //if (dModel2 == null) {
            List<Disp> d = dispFacade.findByDocenteQuad(docente, quadrimestre);
            //dModel2 = new DispDataModel(d);
            Disp vet[] = new Disp[d.size()+1];//de 10 para d.size
            int ordem = 0;
            for (Disp disp : d) {
                ordem = Integer.parseInt(disp.getOrdemPreferencia());
                vet[ordem] = disp;
            }
            List<Disp> ordenada = new ArrayList();
            Disp verifica;
            for (int i = 0; i < d.size()+1; i++) { //de 10 para d.size
                verifica = vet[i];
                if (!(verifica == null)) {
                    ordenada.add(vet[i]);
                }
            }
            dModel2 = new DispDataModel(ordenada);
        //}
        return dModel2;
    }

    public void setDModel2(DispDataModel dModel2) {
        this.dModel2 = dModel2;
    }
    
    private String msg;
    
    //Mensagem de aviso sobre a quantidade de disciplinas do BC&T salvas pelo docente
    public String getMsg(){
        int cont = 0;
        List<Disp> salvas = dispFacade.findByDocenteQuad(pessoa, quadrimestre);
        for(Disp salva: salvas){
            if(salva.getOfertaDisciplina().getCurso().equals("Bacharelado em Ciência e Tecnologia")){
                cont++;
            }
        }
        if(cont < 2){
            msg = "Escolha pelo menos duas turmas do BC&T para incluir no planejamento!";
        } else{
            msg = "";
        }
        return msg;
    }
    
    public void setMsg(String msg){
        this.msg = msg;
    }
    
    //Verifica quantas disciplinas o docente escolheu do BC&T
    public boolean verificarBCT(){
        int cont = 0;
        List<Disp> salvas = dispFacade.findByDocenteQuad(pessoa, quadrimestre);
        for(Disp salva: salvas){
            if(salva.getOfertaDisciplina().getCurso().equals("Bacharelado em Ciência e Tecnologia")){
                cont++;
            }
        }
        if(cont < 2){
            return true;
        } else{
            return false;
        }
    }

    //Salva a disponibilidade do docente
    public void salvarDisp() {
        int cont = 0;
        for (OfertaDisciplina oferta : ofertasEtapa1) {
            //Regarrega o objeto turma, inicializando a Colecao de Disponibilidades(Lazy)
            oferta = turmasFacade.inicializarColecaoDisponibilidades(oferta);
            String funcao = oferta.getFuncao();
            if (funcao == null) {
                funcao = "T e P";
            }
            List<Disp> l = dispFacade.findByDocenteQuad(pessoa, quadrimestre);
            String prioridade = Integer.toString(l.size()+1);
            dispo = new Disp(prioridade, funcao, docente, oferta);
            dispFacade.save(dispo);
        }
        List<Disp> salvas = dispFacade.findByDocenteQuad(pessoa, quadrimestre);
        for(Disp salva: salvas){
            if(salva.getOfertaDisciplina().getCurso().equals("Bacharelado em Ciência e Tecnologia")){
                cont++;
            }
        }
        //Set<Disp> l = docente.getDispo();
        FacesContext context = FacesContext.getCurrentInstance();

        //getOrdem();
        dataModel = null;
        ofertasEtapa1 = null;
        dDataModel = null;
        dModel2 = null;
        //getDModel2();
        //List<Disp> d = dispFacade.findByDocenteQuad(pessoa, quadrimestre);
        //dModel2 = new DispDataModel(d);
        if(cont > 0){
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Successful", "Turmas salvas com Sucesso!"));
        } else{
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Warning", "Selecione pelo menos uma Turma do BC&T!"));
        }
    }

    //Row select----------------------------------------------------------------

    public void onRowUnselect(UnselectEvent event) {

    }

    //Disponibilidade que será selecionada para exclusao
    private List<Disp> selectedDisp;

    public List<Disp> getSelectedDisp() {
        return selectedDisp;
    }

    public void setSelectedDisp(List<Disp> selectedDisp) {
        this.selectedDisp = selectedDisp;
    }

    //Apagar Disponibilidades
    public void deleteEscolha(Disponibilidade d) {
        FacesContext context = FacesContext.getCurrentInstance();
        //Disponibilidade d = (Disponibilidade) aux.getObject();
        disponibilidadeFacade.remove(d);
        dispdataModel2 = null;
        context.addMessage(null, new FacesMessage("Successful", "Disponibilidade Apagada com Sucesso!"));
    }

    //Remove disponibilidade
    public void deleteDisp(Disp d) {
        FacesContext context = FacesContext.getCurrentInstance();
        int ordem = Integer.parseInt(d.getOrdemPreferencia());
        int ordem2 = 0;
        String sub = "";

        dispFacade.remove(d);

        List<Disp> list = dispFacade.findByDocenteQuad(pessoa, quadrimestre);
        for (Disp l : list) {
            ordem2 = Integer.parseInt(l.getOrdemPreferencia());
            if (ordem2 > ordem) {
                ordem2 -= 1;
            }
            sub = Integer.toString(ordem2);
            l.setOrdemPreferencia(sub);
            sub = l.getOrdemPreferencia();
            dispFacade.merge(l);
        }
        //dataModel = null;
        //dDataModel = null;
        dModel2 = null;
        context.addMessage(null, new FacesMessage("Successful", "Disciplina Apagada com Sucesso!"));
    }

    //Método para editar as escolhas de disponibilidade, definindo a ordem de preferencia e 
    //se prefere dar teoria, prática ou ambos
    public void onCellEdit(CellEditEvent event) {
        Disp d = (Disp) dModel2.getRowData();
        dispFacade.merge(d);
    }

    //Usado para ordem de preferência do docente na escolha da oferta de disciplina
    private List<String> ordem;

    public List<String> getOrdem() {

        int tamanho = 0;
        List<Disp> dp = dispFacade.findByDocenteQuad(pessoa, quadrimestre);
        for (Disp d : dp) {
            int quad = d.getOfertaDisciplina().getQuadrimestre();
            if (d.getOfertaDisciplina().getQuadrimestre() == quadrimestre) {
                tamanho++;
            }
        }
        ordem = new ArrayList<>();
        ordem.add("Selecione");
        for (int i = 1; i <= tamanho; i++) {
            ordem.add(String.valueOf(i));
        }
        return ordem;
    }

    public List<String> getOrdem(Long quad) {

        int tamanho = 0;
        for (Disp d : docente.getDispo()) {

            if (d.getOfertaDisciplina().getQuadrimestre() == (int) (long) quad) {
                tamanho++;
            }
        }

        ordem = new ArrayList<>();
        ordem.add("Selecione");
        for (int i = 1; i <= tamanho; i++) {
            ordem.add(String.valueOf(i));
        }
        return ordem;
    }

    public void setOrdem(List<String> ordem) {
        this.ordem = ordem;
    }

    //Usado para o docente definir se ele quer dar teoria, prática ou ambos 
    public List<String> getTipoOferta(OfertaDisciplina o) {
        List<String> tp;
        tp = new ArrayList<>();
        tp.add("Selecione");
        if (o.getT() > 0) {
            tp.add("Teoria");
        }
        if (o.getP() > 0) {
            tp.add("Prática");
        }
        if (o.getP() > 0 && o.getT() > 0) {
            tp.add("Teoria & Prática");
        }
        return tp;
    }

    //Retorna a lista com as escolha de teoria e prática do docente
    public List<String> getTipoDisp(Disp d) {
        List<String> tp;
        tp = new ArrayList<>();
        tp.add("Selecione");
        if (d.getOfertaDisciplina().getT() > 0) {
            tp.add("Teoria");
        }
        if (d.getOfertaDisciplina().getP() > 0) {
            tp.add("Prática");
        }
        if (d.getOfertaDisciplina().getP() > 0 && d.getOfertaDisciplina().getT() > 0) {
            tp.add("Teoria & Prática");
        }
        return tp;
    }

    //Finalizar a operação
    public void sucessoFase1() {
        FacesContext context = FacesContext.getCurrentInstance();
        ofertasEtapa1 = null;
        dataModel = null;
        dDataModel = null;
        //JsfUtil.addSuccessMessage("Disponibilidades em turmas salvas com sucesso!");
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,"Successful","Alterações salvas com Sucesso!"));
        getDModel2();
    }

//-----------------------------------------Paginas web-----------------------------------------------
    private List<OfertaDisciplina> listarTodasQuad(int quad) {
        return turmasFacade.findAllQuad(quad);
    }

    public String prepareAfinidades() {
        return "/Afinidades/DefinirAfinidade";
    }

    public String preparePlanejamento1() {
        //dataModel = new OfertaDisciplinaDataModel(this.listarTodasQuad(1));
        //dataModel = new OfertaDisciplinaLazyModel(this.listarTodasQuad(1));        
        quadrimestre = 1;
        dataModel = null;
        getDataModel();
        dModel2 = null;
        return "/Disponibilidade/Quadrimestre";
    }

    public String preparePlanejamento2() {
        //dataModel = new OfertaDisciplinaDataModel(this.listarTodasQuad(2));
        //dataModel = new OfertaDisciplinaLazyModel(this.listarTodasQuad(2));
        quadrimestre = 2;
        dataModel = null;
        getDataModel();
        dModel2 = null;
        return "/Disponibilidade/Quadrimestre";
    }

    public String preparePlanejamento3() {
        //dataModel = new OfertaDisciplinaDataModel(this.listarTodasQuad(3));
        //dataModel = new OfertaDisciplinaLazyModel(this.listarTodasQuad(3));        
        quadrimestre = 3;
        dataModel = null;
        getDataModel();
        dModel2 = null;
        return "/Disponibilidade/Quadrimestre";
    }

//-------------------------------------------Filtros----------------------------------------------------------
    //Filtros para as turmas da Etapa I
    private boolean filtrarAfinidades;

    private boolean filtrarDisponibilidades;

    private String campus;

    private String turno;

    private String valor;
    
    private List<String> cursos;

    private Set<Afinidade> afinidades;

    private Set<Disponibilidade> setDisponibilidades;

    private List<Disciplina> discAfinidades;

    private List<Disciplina> discEtapa1;

    private int quadrimestre;

    public boolean isFiltrarAfinidades() {
        return filtrarAfinidades;
    }

    public boolean isFiltrarDisponibilidades() {
        return filtrarDisponibilidades;
    }

    public void setFiltrarAfinidades(boolean filtrarAfinidades) {
        this.filtrarAfinidades = filtrarAfinidades;
    }

    public void setFiltrarDisponibilidades(boolean filtrarDisponibilidades) {
        this.filtrarDisponibilidades = filtrarDisponibilidades;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public List<String> getCursos(){
        if(cursos == null){
            cursos = super.getFiltrosSelecCursos();           
        }
        return cursos;
    }
    
    public void setCursos(List<String> cursos){
        this.cursos = cursos;
    }

    public int getQuadrimestre() {
        return quadrimestre;
    }

    public void setQuadrimestre(int quadrimestre) {
        this.quadrimestre = quadrimestre;
    }

    //Inicia as variáveis quando a página é carregada
    @PostConstruct
    public void init() {
        campus = "Ambos";
        turno = "Ambos";
        valor = "nao";
        preferencias = 3;
        informacoes = null;
        dModel2 = null;
        dataModel = null;
    }

    //Inicia os filtros
    public void iniciarFiltro() {
        String c = campus;
        String t = turno;
        if (c == null) {
            campus = "";
        } else {
            campus = c;
        }
        if (t == null) {
            turno = "";
        } else {
            turno = t;
        }
    }

    //Métodos para atualizar a opção dos filtros com o evento de clique
    public void onChangeCampus(ValueChangeEvent event) {
        campus = event.getNewValue().toString();
        if (campus.equals("Ambos")) {
            campus = "";
            if (turno.equals("Ambos")) {
                turno = "";
            }
            filtrarTurmasQuad();
        } else {
            if (turno.equals("Ambos")) {
                turno = "";
            }
            filtrarTurmasQuad();
        }
    }

    public void onChangeTurno(ValueChangeEvent event) {
        turno = event.getNewValue().toString();
        if (turno.equals("Ambos")) {
            turno = "";
            if (campus.equals("Ambos")) {
                campus = "";
            }
            filtrarTurmasQuad();
        } else {
            if (campus.equals("Ambos")) {
                campus = "";
            }
            filtrarTurmasQuad();
        }
    }

    public void onChangeAfinidades(ValueChangeEvent event) {
        valor = event.getNewValue().toString();
        turno = "";
        campus = "";
        if (valor.equals("sim")) {
            filtrarAfinidades = true;
            filtrarTurmasQuad();
        } else {
            filtrarAfinidades = false;
            limparFiltroQuad();
        }
    }

    public void filtrarCursos(){
        dataModel = null;
        cursos = null;
        cursos = super.getFiltrosSelecCursos();
        //Falha ao fazer a busca no banco de dados
        List<OfertaDisciplina> filtradas = turmasFacade.buscaCurso(cursos, quadrimestre);
        List<Disp> salvas = dispFacade.findByDocenteQuad(pessoa, quadrimestre);
        List<OfertaDisciplina> remover = new ArrayList();
        for(Disp salva : salvas){
            remover.add(salva.getOfertaDisciplina());
        }
        List<OfertaDisciplina> disponivel = new ArrayList();
        for(OfertaDisciplina filtrada: filtradas){
            if(!remover.contains(filtrada)){
                disponivel.add(filtrada);
            }
        }
        dataModel = new OfertaDisciplinaLazyModel(disponivel);
    }

    //Métodos dos filtros
    public void filtrarTurmasQuad(Long quad) {
        dataModel = null;
        discAfinidades = new ArrayList<>();

        //Caso o usuário queira filtrarTurmas por afinidades
        if (filtrarAfinidades) {
            afinidades = docente.getAfinidades();

            //Quais disciplinas ele tem afinidade
            for (Afinidade a : afinidades) {
                if (a.getEstado().equals("Adicionada")) {
                    discAfinidades.add(a.getDisciplina());
                }
            }
        }

        Integer q = (int) (long) quad;
        //dataModel = new OfertaDisciplinaDataModel(turmasFacade.filtrarAfinidTurnCampQuad(discAfinidades, turno, campus, q));
        List<OfertaDisciplina> filtradas = turmasFacade.filtrarAfinidTurnCampQuad(discAfinidades, turno, campus, q);
        List<Disp> salvas = dispFacade.findByDocenteQuad(pessoa, q);
        List<OfertaDisciplina> remover = new ArrayList();
        for(Disp salva : salvas){
            remover.add(salva.getOfertaDisciplina());
        }
        List<OfertaDisciplina> disponivel = new ArrayList();
        for(OfertaDisciplina filtrada: filtradas){
            if(!remover.contains(filtrada)){
                disponivel.add(filtrada);
            }
        }
        dataModel = new OfertaDisciplinaLazyModel(disponivel);
    }

    public void filtrarTurmasQuad() {
        dataModel = null;
        discAfinidades = new ArrayList<>();

        //Caso o usuário queira filtrarTurmas por afinidades
        if (filtrarAfinidades) {
            afinidades = docente.getAfinidades();

            //Quais disciplinas ele tem afinidade
            for (Afinidade a : afinidades) {
                if (a.getEstado().equals("Adicionada")) {
                    discAfinidades.add(a.getDisciplina());
                }
            }
        }

        //dataModel = new OfertaDisciplinaDataModel(turmasFacade.filtrarAfinidTurnCampQuad(discAfinidades, turno, campus, quadrimestre));
        List<OfertaDisciplina> filtradas = turmasFacade.filtrarAfinidTurnCampQuad(discAfinidades, turno, campus, quadrimestre);
        List<Disp> salvas = dispFacade.findByDocenteQuad(pessoa, quadrimestre);
        List<OfertaDisciplina> remover = new ArrayList();
        for(Disp salva : salvas){
            remover.add(salva.getOfertaDisciplina());
        }
        List<OfertaDisciplina> disponivel = new ArrayList();
        for(OfertaDisciplina filtrada: filtradas){
            if(!remover.contains(filtrada)){
                disponivel.add(filtrada);
            }
        }
        dataModel = new OfertaDisciplinaLazyModel(disponivel);
    }

    //Limpar os filtros
    public void limparFiltroQuad(Long quad) {
        //dispdataModel = null;
        dDataModel = null;
        //dataModel = new OfertaDisciplinaDataModel(this.listarTodasQuad((int) (long) quad));
        //dataModel = new OfertaDisciplinaLazyModel(this.listarTodasQuad((int) (long) quad));
        dataModel = null;
        getDataModel();
    }

    public void limparFiltroQuad() {
        //dispdataModel = null;
        cursos = null;
        dDataModel = null;
        //dataModel = new OfertaDisciplinaDataModel(this.listarTodasQuad(quadrimestre));
        //dataModel = new OfertaDisciplinaLazyModel(this.listarTodasQuad(quadrimestre));
        dataModel = null;
        getDataModel();
    }

    //Filtros para o Log das Disponibilidades da Etapa I-------------------------------------------------------
    private Disciplina disciplina;

    private Pessoa pessoa;

    public Pessoa getPessoa() {
        return pessoa;
    }

    public void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(Disciplina disciplina) {
        this.disciplina = disciplina;
    }

    private List<Disponibilidade> disponibilidades;

    public List<Disponibilidade> getDisponibilidades() {
        return disponibilidades;
    }

    public void setDisponibilidades(List<Disponibilidade> disponibilidades) {
        this.disponibilidades = disponibilidades;
    }

    private List<Disp> displ;

    public List<Disp> getDispl() {
        return displ;
    }

    public void setDisp(List<Disp> displ) {
        this.displ = displ;
    }

    public void limparFiltroDisp() {
        //dispdataModel = null;
        dDataModel = null;
    }
}
