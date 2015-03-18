package controller;

import facade.AfinidadesFacade;
import facade.DisciplinaFacade;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Named;
import model.Afinidades;
import model.Disciplina;
import model.Pessoa;
import util.DisciplinaLazyModel;

@Named(value = "disciplinaController2")
@SessionScoped
public class DisciplinaController2 implements Serializable {

    public DisciplinaController2() {
        disponivel = new Disciplina();
        escolhida = new Disciplina();  
    }
    
    //Guarda o disciplina atual
    private Disciplina disciplina;

    @EJB
    private DisciplinaFacade disciplinaFacade;

//    @EJB
//    private PessoaFacade pessoaFacade;
    
    @EJB
    private AfinidadesFacade afinidadesFacade;
    
    
    //Lista de Afinidades, tanto com as disciplinas adicionadas, quanto removidas
    private List<Afinidades> afinidadesAtivas;
    
//    private CadastroBean cadastro;

    //Guarda a disciplina disponivel selecionada
    private Disciplina disponivel;

    //Guarda a disciplina escolhida selecionada
    private Disciplina escolhida;

    //Pessoa logada
    private Pessoa pessoa;
    
//    private Disciplina disciplina;
    
    //Disciplinas disponiveis
    private List<Disciplina> disponiveis;

    //Disciplinas escolhidas
    private List<Disciplina> escolhidas;
    

    //----------------------------------------Getters e Setters--------------------------------------------------------------------------------------------------------------
    
    public Disciplina getDisciplina() {
        if (disciplina == null) {
            disciplina = new Disciplina();
        }
        return disciplina;
    }
    
    public void setDisciplina(Disciplina disciplina){
        this.disciplina = disciplina;
    }
    
    private Disciplina getDisciplina(Long key) {
        return this.buscar(key);

    }
      
    public void setDisponiveis(List<Disciplina> disponiveis) {
        this.disponiveis = disponiveis;
    }

    public List<Disciplina> getEscolhidas() {
        return escolhidas;
    }

    public void setEscolhidas(List<Disciplina> escolhidas) {
        this.escolhidas = escolhidas;
    }
    
    //    public Disciplina getDisciplina() {
//        return disciplina;
//    }
//
//    public void setDisciplina(Disciplina disciplina) {
//        this.disciplina = disciplina;
//    }
    
    
    public Disciplina getDisponivel() {
        return disponivel;
    }

    public void setDisponivel(Disciplina disponivel) {
        this.disponivel = disponivel;
    }

    public Disciplina getEscolhida() {
        return escolhida;
    }

    public void setEscolhida(Disciplina escolhida) {
        this.escolhida = escolhida;
    }

    //--------------------------------------Lazy Data Model--------------------------------------------------------------
    
    private DisciplinaLazyModel disciplinaDataModel;
    
    @PostConstruct
    public void init() {
        disciplinaDataModel = new DisciplinaLazyModel(this.listarTodas());
    }
    
    public DisciplinaLazyModel getDisciplinaLazyModel() {
        
        if(disciplinaDataModel == null){
            disciplinaDataModel = new DisciplinaLazyModel(this.listarTodas());
        }
        
        
        return this.disciplinaDataModel;
    }
    
    public void recriarModelo() {
    
        disciplinaDataModel = null;

    }
  
    //----------------------------Preenchimento das listas Disponiveis e Escolhidas-----------------------------------------------------

    //Esse método vê quais disciplinas a pessoa já escolheu,
    //para exibir só as que ela ainda não selecionou 
    //(não estão salvas na afinidade, ou estão salvas como "Removida", porque ela pode escolher de novo nesse caso)
    public List<Disciplina> getDisponiveis() {

        if (disponiveis == null) {
            
            //Usuário que fez o logon
            pessoa = LoginBean.getUsuario();

            //Todas as afinidades do usuario
            afinidadesAtivas = new ArrayList(pessoa.getAfinidades());
            
            escolhidas = new ArrayList<>();
            
            //Adiciona ao array escolhidas as disciplinas que estao como "Adicionada" em afinidades
            for(Afinidades a: afinidadesAtivas){
                
                if(a.getEstado().equals("Adicionada")){
                    escolhidas.add(a.getDisciplina());
                }        
            }

            //Seleciona todas disciplinas do banco
            disponiveis = disciplinaFacade.findAll();

            //Remove de todas as disciplinas as que já foram escolhidas
            for (Disciplina e : escolhidas) {
                disponiveis.remove(e);
            }

        }

        return disponiveis;
    }
   
    //----------------------------------Metodos de CRUD--------------------------------------------------------------------------------
    

    public void salvarAfinidade() {
        
        //Data da inclusao/remocao
        Calendar cal = Calendar.getInstance();
        
        //Regarrega o objeto disciplina, inicializando a Colecao de Afinidades(Lazy)
        disponivel = disciplinaFacade.inicializarColecaoAfinidades(disponivel);
        
        Afinidades afinidade = new Afinidades("Adicionada", cal.getTime(), pessoa, disponivel);
        
        afinidadesFacade.merge(afinidade);
        
        disponiveis = null;
        disponivel = null;
    }
    
    public void removerAfinidade(){
        
        //Percorre a lista de afinidades da pessoa e muda o status de Adicionada para removida
        for(Afinidades a: afinidadesAtivas){
            
            if(a.getDisciplina() == escolhida){

                Calendar cal = Calendar.getInstance();
                a.setDataAcao(cal.getTime());
                a.setEstado("Removida");
                afinidadesFacade.edit(a);
            }
            
        }
        
        disponiveis = null;
        escolhida = null;
        disponivel = null;
                 
    }
    
    public void delete() {
        disciplina = (Disciplina) disciplinaDataModel.getRowData();
        try {
            disciplinaFacade.remove(disciplina);
            disciplina = null;
            JsfUtil.addSuccessMessage("Disciplina Deletado");
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Ocorreu um erro de persistência");
        }

        recriarModelo();
    }
    
    public Disciplina buscar(Long id) {

        return disciplinaFacade.find(id);
    }

    private List<Disciplina> listarTodas() {
        return disciplinaFacade.findAll();
    }
 

    //---------------------------------------Páginas web------------------------------------------------------------
    public String prepareCreate(int i) {
        disciplina = new Disciplina();
        if (i == 1) {
            return "/view/disciplina/Create";
        } else {
            return "Create";
        }
    }

    public String index() {
        disciplina = null;
        disciplinaDataModel = null;
        return "/index";
    }

    public String prepareEdit() {
        disciplina = (Disciplina) disciplinaDataModel.getRowData();
        return "Edit";
    }

    public String prepareView() {
        disciplina = (Disciplina) disciplinaDataModel.getRowData();
        //disciplina = disciplinaFacade.find(disciplina.getID());
        //disciplinaFacade.edit(disciplinaFacade.find(disciplina.getID()));
        //disciplinaFacade.edit(disciplina);
        return "View";
    }
    
    
    
//----------------------------------------AutoComplete----------------------------------------------------------------------------------------
    
    public List<Disciplina> completeDisciplina(String query) {
        
        query = query.toLowerCase();
        
        List<Disciplina> allDisciplinas = this.listarTodas();
        List<Disciplina> filteredDisciplinas = new ArrayList<>();

        for (Disciplina d : allDisciplinas) {
            if (d.getNome().toLowerCase().startsWith(query)) {
                filteredDisciplinas.add(d);
            }
        }
        return filteredDisciplinas;
    }

    //----------------------------------------------------------------------------------------------------

//    private boolean incluirRemovidas;
//
//    public boolean isIncluirRemovidas() {
//        return incluirRemovidas;
//    }
//
//    public void setIncluirRemovidas(boolean incluirRemovidas) {
//        this.incluirRemovidas = incluirRemovidas;
//    }
//    
//    
//    
//    private List<Afinidades> afinidadesPorDisciplina;
//
//    public List<Afinidades> getAfinidadesPorDisciplina() {
//        return afinidadesPorDisciplina;
//    }
//
//    public void setAfinidadesPorDisciplina(List<Afinidades> afinidadesPorDisciplina) {
//        this.afinidadesPorDisciplina = afinidadesPorDisciplina;
//    }
    

    
//--------------------------------------------Cadastro-------------------------------------------------------------------------------------------
    
    private String converteEixo(String sigla) {

        char letra = sigla.charAt(2);

        switch (letra) {

            case 'J':
                return "Energia";
            case 'K':
                return "Estrutura da Matéria";
            case 'L':
                return "Processos de Transformação";
            case 'M':
                return "Comunicação e Informação";
            case 'N':
                return "Representação e Simulação";
            case 'O':
                return "Estado, Sociedade e Mercado";
            case 'P':
                return "Pensamento, Expressão e Significado";
            case 'Q':
                return "Espaço, Cultura e Temporalidade";
            case 'R':
                return "Ciência, Tecnologia e Inovação";
            default:
                return "Mais de um eixo";

        }
    }

    private String converteCurso(String sigla) {

        switch (sigla.substring(0, 2)) {

            //Cursos do CMCC
            case "MC":

                switch (sigla.charAt(2)) {

                    case 'A':
                        return "Bacharelado em Ciência da Computação";
                    case 'B':
                        return "Bacharelado em Matemática";
                    case 'C':
                        return "Bacharelado em Neurociência";
                    case 'D':
                        return "Licenciatura em Matemática";
                    case 'X':
                        return "Disciplina não ofertada";
                }

            //Cursos do CECS
            case "ES":

                switch (sigla.charAt(3)) {

                    case 'P':
                        return "Bacharelado em Políticas Públicas";
                    case 'S':
                        return "Engenharia Aeroespacial";
                    case 'U':
                        return "Engenharia Ambiental e Urbana";
                    case 'A':
                        return "Engenharia de Automação e Robótica";
                    case 'B':
                        return "Engenharia Biomédica";
                    case 'E':
                        return "Engenharia de Energia";
                    case 'G':
                        return "Engenharia de Gestão";
                    case 'I':
                        return "Engenharia de Informação";
                    case 'M':
                        return "Engenharia de Materiais";
                    case 'C':
                        return "Bacharelado em Economia";
                    case 'R':
                        return "Bacharelado em Relações Internacionais";
                    case 'T':
                        return "Bacharelado em Planejamento Territorial";
                    case 'X':
                        return "Disciplina não ofertada";
                }

            //Cursos do CCNH
            case "NH":

                switch (sigla.charAt(3)) {

                    case '1':
                        return "Ciências Biológicas";
                    case '2':
                        return "Filosofia";
                    case '3':
                        return "Física";
                    case '4':
                        return "Química";
                    case '5':
                        return "Licenciaturas";

                }
        }

        return null;

    }
    
    public void cadastrarDisciplinas() {

        String[] palavras;
        String eixo;
        String curso;

        try {

            FileReader arq = new FileReader("/home/charles/NetBeansProjects/Arquivos CSV/catalogoDisciplinas.csv");

            BufferedReader lerArq = new BufferedReader(arq);

            String linha = lerArq.readLine(); //cabeçalho
            // lê a primeira linha 
            // a variável "linha" recebe o valor "null" quando o processo 
            // de repetição atingir o final do arquivo texto 

            linha = lerArq.readLine();

            while (linha != null) {
//                linha = linha.replace("\"", "");

                palavras = linha.split(";");

                List<Disciplina> disciplinaExist = disciplinaFacade.findByName(palavras[2]);

                if (disciplinaExist.isEmpty()) {
                    Disciplina d = new Disciplina();
                    d.setNome(palavras[2]);

                    if (palavras[0].substring(0, 2).equals("BC") || palavras[0].substring(0, 2).equals("BH")
                            || palavras[0].substring(0, 2).equals("BI")) {

                        eixo = converteEixo(palavras[0]);
                        d.setEixo(eixo);

                    } else {

                        curso = converteCurso(palavras[0]);
                        d.setCurso(curso);

                    }

                    disciplinaFacade.save(d);
                }

                linha = lerArq.readLine();
            }

            arq.close();

        } catch (IOException e) {
            System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage());
        }

        Disciplina d = new Disciplina();
        recriarModelo();
        
        JsfUtil.addSuccessMessage("Cadastro de disciplinas realizado com sucesso", "");
   
    }
    
    @FacesConverter(forClass = Disciplina.class)
    public static class DisciplinaControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            DisciplinaController2 controller = (DisciplinaController2) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "disciplinaController2");
            return controller.getDisciplina(getKey(value));
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
            if (object instanceof Disciplina) {
                Disciplina d = (Disciplina) object;               
                return getStringKey(new BigDecimal(d.getID().toString()).setScale(0, BigDecimal.ROUND_HALF_UP).longValue());

            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Disciplina.class.getName());
            }
        }
    }
    
}
