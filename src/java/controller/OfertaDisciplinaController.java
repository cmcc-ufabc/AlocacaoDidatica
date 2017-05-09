package controller;

import facade.DisciplinaFacade;
import facade.DisponibilidadeFacade;
import facade.PessoaFacade;
import facade.OfertaDisciplinaFacade;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import model.Disciplina;
import model.Disponibilidade;
import model.Docente;
import model.Pessoa;
import model.OfertaDisciplina;
import util.DisponibilidadeDataModel;
import util.OfertaDisciplinaDataModel;
import util.OfertaDisciplinaLazyModel;


@Named(value = "ofertaController")
@SessionScoped
public class OfertaDisciplinaController extends Filtros implements Serializable {
    
    private ExternalContext externalContext;
    private LoginBean loginBean;
    private Pessoa docente;
    
    public OfertaDisciplinaController() {
        externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sessionMap = externalContext.getSessionMap();
        loginBean = (LoginBean) sessionMap.get("loginBean");
        docente = loginBean.getUsuario();
    }

    @EJB
    private OfertaDisciplinaFacade ofertaDisciplinaFacade;
    
    @EJB
    private DisciplinaFacade disciplinaFacade;
    
    @EJB
    private DisponibilidadeFacade disponibilidadeFacade;
    
    @EJB
    private PessoaFacade pessoaFacade;
    
    private OfertaDisciplina oferta;

    private List<OfertaDisciplina> escolhidas;
    
    private OfertaDisciplinaDataModel dataModel;
        
    //--------------------------------------Filtros----------------------------------------------------------
    
    private boolean filtrarAfinidades;
    
    private String campus;
    
    private String turno;
    
    private int quadrimestre;
        
    //-------------------------------------Getters e Setters--------------------------------------------------------

    public List<OfertaDisciplina> getEscolhidas() {
        return escolhidas;
    }

    public void setEscolhidas(List<OfertaDisciplina> escolhidas) {
        this.escolhidas = escolhidas;
    }

    public OfertaDisciplina getOferta() {
        return oferta;
    }

    public void setOferta(OfertaDisciplina oferta) {
        this.oferta = oferta;
    }

    //Datamodel das ofertas de disciplinas
    public OfertaDisciplinaDataModel getDataModel() {
        if(dataModel == null){
            List<OfertaDisciplina> ofertas = ofertaDisciplinaFacade.findAll();
            dataModel = new OfertaDisciplinaDataModel(ofertas);
        }
        return dataModel;
    }

    public void setDataModel(OfertaDisciplinaDataModel dataModel) {
        this.dataModel = dataModel;
    }

    public boolean isFiltrarAfinidades() {
        return filtrarAfinidades;
    }

    public void setFiltrarAfinidades(boolean filtrarAfinidades) {
        this.filtrarAfinidades = filtrarAfinidades;
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

    public int getQuadrimestre() {
        return quadrimestre;
    }

    public void setQuadrimestre(int quadrimestre) {
        this.quadrimestre = quadrimestre;
    }

    public DisponibilidadeDataModel getDispDataModel() {
        return dispDataModel;
    }

    public void setDispDataModel(DisponibilidadeDataModel dispDataModel) {
        this.dispDataModel = dispDataModel;
    }
     
//------------------------------------------Fase I---------------------------------------------------------------
    
    //Prepara as páginas de escolha de Oferta de Disciplina de acordo com o quadrimestre-------------------------
    
    public String prepareQuad1(){
        dataModel = new OfertaDisciplinaDataModel(this.listarTodasQuad(1));
        return "/Disponibilidade/FaseIQuad1";    
    }
    
    public String prepareQuad2(){
        dataModel = new OfertaDisciplinaDataModel(this.listarTodasQuad(2));
        return "/Disponibilidade/FaseIQuad2";    
    }
    
    public String prepareQuad3(){
        dataModel = new OfertaDisciplinaDataModel(this.listarTodasQuad(3));
        return "/Disponibilidade/FaseIQuad3";
    }

    private OfertaDisciplinaLazyModel ofertas1LazyModel;
    private OfertaDisciplinaLazyModel ofertas2LazyModel;
    private OfertaDisciplinaLazyModel ofertas3LazyModel;
    
    //Inicia os lazymodel com todas as ofertas do quadrimestre
    @PostConstruct
    public void init() {
        ofertas1LazyModel = new OfertaDisciplinaLazyModel(this.listarTodasQuad(1));
        ofertas2LazyModel = new OfertaDisciplinaLazyModel(this.listarTodasQuad(2));
        ofertas3LazyModel = new OfertaDisciplinaLazyModel(this.listarTodasQuad(3));
    }

    //Busca todas as disciplinas
    public OfertaDisciplinaLazyModel getOfertas1LazyModel() { 
        if(ofertas1LazyModel == null){
            ofertas1LazyModel = new OfertaDisciplinaLazyModel(this.listarTodasQuad(1));
        }
        return ofertas1LazyModel;
    }

    public void setOfertas1LazyModel(OfertaDisciplinaLazyModel ofertas1LazyModel) {
        this.ofertas1LazyModel = ofertas1LazyModel;
    }

    public OfertaDisciplinaLazyModel getOfertas2LazyModel() {
        if(ofertas2LazyModel == null){
            ofertas2LazyModel = new OfertaDisciplinaLazyModel(this.listarTodasQuad(2));
        }
        return ofertas2LazyModel;
    }

    public void setOfertas2LazyModel(OfertaDisciplinaLazyModel ofertas2LazyModel) {
        this.ofertas2LazyModel = ofertas2LazyModel;
    }

    public OfertaDisciplinaLazyModel getOfertas3LazyModel() {
        if(ofertas3LazyModel == null){
            ofertas3LazyModel = new OfertaDisciplinaLazyModel(this.listarTodasQuad(3));
        }
        return ofertas3LazyModel;
    }

    public void setOfertas3LazyModel(OfertaDisciplinaLazyModel ofertas3LazyModel) {
        this.ofertas3LazyModel = ofertas3LazyModel;
    }
       
//---------------------------------------Resumo Fase I-----------------------------------------------------------    

    /**
     * Filtra as disponibilidades do docente de acordo com os parametros escolhidos
     */
    public void filtrarOfertas() {
        List<OfertaDisciplina> ofertas;
        ofertas = ofertaDisciplinaFacade.filtrarEixoCursoTurnoCampusQuad(getFiltrosSelecEixos(), getFiltrosSelecCursos(), turno, campus, quadrimestre);
        dataModel = new OfertaDisciplinaDataModel(ofertas);
        
        //Após filtrar volta os parametros para os valores default
        setFiltrosSelecEixos(null);
        setFiltrosSelecCursos(null);
        turno = "";
        quadrimestre = 0;
        campus = "";
    }

    /**
     * Limpa os filtros e carrega todas as disponibilidades escolhidas
     */
    public void limparFiltroOfertas() {     
        dataModel = null;
    }
    
    //Disponibilidades de acordo com a Oferta de Disciplina escolhida, para mostrar no resumo
    DisponibilidadeDataModel dispDataModel;
    
    public void preencherDisponibilidadesOferta() {
        List<Disponibilidade> disponibilidades;
        if (oferta != null) {
            disponibilidades = new ArrayList<>(oferta.getDisponibilidades());
        } else {
            disponibilidades = new ArrayList<>();
        } 
        dispDataModel = new DisponibilidadeDataModel(disponibilidades);
    }

    //---------------------------------------------------CRUD-------------------------------------------------------
    //Retorna todas as ofertas
    private List<OfertaDisciplina> listarTodas() {
        return ofertaDisciplinaFacade.findAll();
    }
    
    //Retorna as ofertas por quadrimestre
    private List<OfertaDisciplina> listarTodasQuad(int quad){        
        return ofertaDisciplinaFacade.findAllQuad(quad);
    }
    
    public void salvarNoBanco() {
        try {
            ofertaDisciplinaFacade.save(oferta);
//            JsfUtil.addSuccessMessage("Pessoa " + pessoa.getNome() + " criado com sucesso!");
            oferta= null;
//            recriarModelo();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Ocorreu um erro de persistência");
        }
    }

    public OfertaDisciplina buscar(Long id) {
        return ofertaDisciplinaFacade.find(id);
    }

    public void editar() {
        try {
            ofertaDisciplinaFacade.edit(oferta);
//            JsfUtil.addSuccessMessage("Pessoa Editado com sucesso!");
            oferta= null;
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Ocorreu um erro de persistência, não foi possível editar a oferta de disciplina: " + e.getMessage());
        }
    }

    //Deletar as ofertas de disciplina
    public void deleteAll(Long quad) {
        List<OfertaDisciplina> ofertas = listarTodasQuad((int)(long)quad);
        for (OfertaDisciplina o : ofertas) {
            o = ofertaDisciplinaFacade.inicializarColecaoDisponibilidades(o);
            Set<Disponibilidade> ds = o.getDisponibilidades();
            Pessoa atual;
            for (Disponibilidade d : ds) {
                o.getDisponibilidades().remove(d);
                atual = d.getPessoa();
                atual.getDisponibilidades().remove(d);
                pessoaFacade.edit(atual);
                disponibilidadeFacade.remove(d);
            }
            ofertaDisciplinaFacade.remove(o);
        }    
        ofertas1LazyModel = null;
    }
        
    //Deletar as ofertas de disciplina
    public void deleteAllQuad(Long quad) {
        Integer q = (int)(long) quad;    
        List<OfertaDisciplina> all = listarTodasQuad(q);
        for (OfertaDisciplina t : all) {
            t = ofertaDisciplinaFacade.inicializarColecaoDisponibilidades(t);
            Set<Disponibilidade> ds = t.getDisponibilidades();
            Pessoa atual;
            ofertaDisciplinaFacade.remove(t);
        }
        if(q == 1){
           ofertas1LazyModel = null; 
        }
        else{
            if(q == 2){
                ofertas2LazyModel = null;
            }
            else{
                ofertas3LazyModel = null;
            }
        }    
    }

    //Pega os itens selecionados
    public SelectItem[] getItemsAvaiableSelectOne() {
        return JsfUtil.getSelectItems(ofertaDisciplinaFacade.findAll(), true);
    }

    //Resumo Fase I--------------------------------------------------------------------------------------------------------------

    //Quantidade de ofertas de disciplina do docente
    public int qdtDocentesDisponibilidade(OfertaDisciplina of){
        of = ofertaDisciplinaFacade.inicializarColecaoDisponibilidades(of);
        Set<Disponibilidade> disponibilidades = of.getDisponibilidades();
        return disponibilidades.size();        
    }
    
    private DisponibilidadeDataModel docentesPorDisciplina;

    //Quantidade de docentes por disciplina
    public DisponibilidadeDataModel getDocentesPorDisciplina() {
        if(docentesPorDisciplina == null){
            docentesPorDisciplina = new DisponibilidadeDataModel();
        }
        return docentesPorDisciplina;
    }

    public void setDocentesPorDisciplina(DisponibilidadeDataModel docentesPorDisciplina) {
        this.docentesPorDisciplina = docentesPorDisciplina;
    }

    //Cadastro-------------------------------------------------------------------------------------------

    //Cadastrar oferta do primeiro quadrimestre
    public void cadastrarOfertasQuad1(){
        String[] palavras;
        //Primeiro quadrimestre
            try {
            try (BufferedReader lerArq = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\Juliana\\Documents\\NetBeansProjects\\alocacao\\Arquivos Alocação\\Arquivos CSV\\quad1.csv"), "UTF-8"))) {
                String linha = lerArq.readLine(); //cabeçalho
                linha = lerArq.readLine();

//            linha = linha.replaceAll("\"", "");
                while (linha != null) {
                    linha = linha.replaceAll("\"", "");
                    palavras = linha.split("_", -1);
                    oferta = new OfertaDisciplina();
                    oferta.setCurso(palavras[2]);
                    String nome = palavras[4];
                    String codigo = palavras[3];
                    Disciplina d = disciplinaFacade.findByCodOrName(codigo, nome);
                    if (d != null) {
//                        Disciplina d = disciplinaFacade.findByName(nome).get(0);
                        oferta.setDisciplina(d);
                    }
                    oferta.setT(Integer.parseInt(palavras[5]));
                    oferta.setP(Integer.parseInt(palavras[6]));
                    oferta.setTurno(palavras[11]);
                    oferta.setCampus(palavras[12]);
                    if (!palavras[13].equals("")) {
                        oferta.setNumTurmas(Integer.parseInt(palavras[13]));
                    }
                    if (!palavras[19].equals("")) {
                        oferta.setPeriodicidade(palavras[19]);
                    }
                    oferta.setQuadrimestre(1);
                    salvarNoBanco();
                    linha = lerArq.readLine();
//                linha = linha.replaceAll("\"", "");
                }
            } //cabeçalho
                ofertas1LazyModel = null;
            } catch (IOException e) {
                System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage());
            }      
    }
    
    //Cadastrar oferta do segundo quadrimestre
    public void cadastrarOfertasQuad2(){
       String[] palavras;
        //Primeiro quadrimestre
        try {
            try (BufferedReader lerArq = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\Juliana\\Documents\\NetBeansProjects\\alocacao\\Arquivos Alocação\\Arquivos CSV\\quad2.csv"), "UTF-8"))) {
                String linha = lerArq.readLine(); //cabeçalho
                linha = lerArq.readLine();
//            linha = linha.replaceAll("\"", "");
                while (linha != null) {
                    linha = linha.replaceAll("\"", "");
                    palavras = linha.split("_", -1);
                    oferta = new OfertaDisciplina();
                    oferta.setCurso(palavras[2]);
                    String nome = palavras[4];
                    String codigo = palavras[3];
                    Disciplina d = disciplinaFacade.findByCodOrName(codigo, nome);
                    if (d != null) {
//                        Disciplina d = disciplinaFacade.findByName(nome).get(0);
                        oferta.setDisciplina(d);
                    }
                    oferta.setT(Integer.parseInt(palavras[5]));
                    oferta.setP(Integer.parseInt(palavras[6]));
                    oferta.setTurno(palavras[11]);
                    oferta.setCampus(palavras[12]);
                    if (!palavras[13].equals("")) {
                        oferta.setNumTurmas(Integer.parseInt(palavras[13]));
                    }
                    if (!palavras[19].equals("")) {
                        oferta.setPeriodicidade(palavras[19]);
                    }
                    oferta.setQuadrimestre(2);
                    salvarNoBanco();
                    linha = lerArq.readLine();
//                linha = linha.replaceAll("\"", "");
                }
            } //cabeçalho
                ofertas2LazyModel = null;
        } catch (IOException e) {
            System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage());
        }
    }
    
    //Cadastrar oferta do terceiro quadrimestre
    public void cadastrarOfertasQuad3(){
        String[] palavras;
        //Primeiro quadrimestre
        try {
            try (BufferedReader lerArq = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\Juliana\\Documents\\NetBeansProjects\\alocacao\\Arquivos Alocação\\Arquivos CSV\\quad3.csv"), "UTF-8"))) {
                String linha = lerArq.readLine(); //cabeçalho
                linha = lerArq.readLine();
//            linha = linha.replaceAll("\"", "");
                while (linha != null) {
                    linha = linha.replaceAll("\"", "");
                    palavras = linha.split("_", -1);
                    oferta = new OfertaDisciplina();
                    oferta.setCurso(palavras[2]);
                    String nome = palavras[4];
                    String codigo = palavras[3];
                    Disciplina d = disciplinaFacade.findByCodOrName(codigo, nome);
                    if (d != null) {
//                        Disciplina d = disciplinaFacade.findByName(nome).get(0);
                        oferta.setDisciplina(d);
                    }
                    oferta.setT(Integer.parseInt(palavras[5]));
                    oferta.setP(Integer.parseInt(palavras[6]));
                    oferta.setTurno(palavras[11]);
                    oferta.setCampus(palavras[12]);
                    if (!palavras[13].equals("")) {
                        oferta.setNumTurmas(Integer.parseInt(palavras[13]));
                    }
                    if (!palavras[19].equals("")) {
                        oferta.setPeriodicidade(palavras[19]);
                    }
                    oferta.setQuadrimestre(3);
                    salvarNoBanco();
                    linha = lerArq.readLine();
//                linha = linha.replaceAll("\"", "");
                }
            } //cabeçalho
            ofertas3LazyModel = null;
        } catch (IOException e) {
            System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage());
        }
    }

    private OfertaDisciplina getTurma(Long key) {
        return buscar(key);    
    }
        
    //---------------------------------------------------------------------------------------------------
    
    @FacesConverter(forClass = OfertaDisciplina.class)
    public static class OfertaDisciplinaControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            OfertaDisciplinaController controller = (OfertaDisciplinaController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "ofertaController");
            return controller.getTurma(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof OfertaDisciplina) {
                OfertaDisciplina d = (OfertaDisciplina) object;    
                return getStringKey(d.getID());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + OfertaDisciplina.class.getName());
            }
        }
    }
}