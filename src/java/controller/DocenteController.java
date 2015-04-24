package controller;

import facade.AfinidadeFacade;
import facade.DocenteFacade;
import facade.PessoaFacade;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import model.Afinidade;
import model.Docente;
import model.Pessoa;
import util.AfinidadeDataModel;
import util.DocenteDataModel;
import util.PessoaLazyModel;

@Named(value = "docenteController")
@SessionScoped
public class DocenteController implements Serializable{
    
    public DocenteController(){
        
    }
    
    //Docente atual
    private Docente docente;
    
    //Docente para salvar
    private Docente docenteSalvar;
    
    @EJB
    private DocenteFacade docenteFacade;
    
    @EJB
    private PessoaFacade pessoaFacade;
    
    @EJB
    private AfinidadeFacade afinidadeFacade;
    
    //----------------------------------------Getters e Setters----------------------------------------------------

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }

    public Docente getDocenteSalvar() {
        
        if(docenteSalvar == null){
            docenteSalvar = new Docente();
        }
        
        return docenteSalvar;
    }

    public void setDocenteSalvar(Docente docenteSalvar) {
        this.docenteSalvar = docenteSalvar;
    }
    
    //-----------------------------------------DataModel--------------------------------------------------
    private DocenteDataModel docenteDataModel;

    public DocenteDataModel getDocenteDataModel() {
        
        if(docenteDataModel == null){
            docenteDataModel = new DocenteDataModel(this.listarTodas());
        }
        
        return docenteDataModel;
    }

    public void setDocenteDataModel(DocenteDataModel docenteDataModel) {
        this.docenteDataModel = docenteDataModel;
    }
    
    public int qtdAfinidades(Docente d){
        
        Set<Afinidade> afinidades = d.getAfinidades();
        int qtd = 0;
        
        for(Afinidade a: afinidades){
            if(a.getEstado().equals("Adicionada")){
                qtd++;
            }
        }
        
        
        return qtd;
        
    }
    
   
    
    
    //Afinidades de acordo com o docente
    private AfinidadeDataModel afinidadesDoDocente;
    
    private AfinidadeDataModel afinidadesFiltradas;

    public AfinidadeDataModel getAfinidadesFiltradas() {
        return afinidadesFiltradas;
    }

    public void setAfinidadesFiltradas(AfinidadeDataModel afinidadesFiltradas) {
        this.afinidadesFiltradas = afinidadesFiltradas;
    }
    
    

    public AfinidadeDataModel getAfinidadesDoDocente() {
        
        if(afinidadesDoDocente == null){
//            Set<Afinidade> all = docente.getAfinidades();
//            Set<Afinidade> adicionadas = new HashSet<>();
//            for(Afinidade a: all){
//                if(a.getEstado().equals("Adicionada")){
//                    adicionadas.add(a);
//                }
//            }
//            
//            List<Afinidade> afs = new ArrayList<>();
//            afs.addAll(adicionadas);
//            afinidadesDoDocente = new AfinidadesLazyModel(afs);
            
            afinidadesDoDocente = new AfinidadeDataModel(afinidadeFacade.findAll());
        }
        
        return afinidadesDoDocente;
    }

    public void setAfinidadesDoDocente(AfinidadeDataModel afinidadesDoDocente) {
        this.afinidadesDoDocente = afinidadesDoDocente;
    }
    
    public void preencherAfinidadesDoDocente(){
        
        if(incluirRemovidas){
            afinidadesDoDocente = new AfinidadeDataModel((List<Afinidade>) docente.getAfinidades());
        }
        
        else{
            
            Set<Afinidade> all = docente.getAfinidades();
            Set<Afinidade> adicionadas = new HashSet<>();
            for(Afinidade a: all){
                if(a.getEstado().equals("Adicionada")){
                    adicionadas.add(a);
                }
            }
            
            List<Afinidade> afs = new ArrayList<>();
            afs.addAll(adicionadas);
            afinidadesDoDocente = new AfinidadeDataModel(afs);
            
        }
        
        
    }
    
    public void falseIncluirRemovidas(){
        
        incluirRemovidas = false;
        
    }
    
    //inclui afinidades removidas
    private boolean incluirRemovidas;

    public boolean isIncluirRemovidas() {
        return incluirRemovidas;
    }

    public void setIncluirRemovidas(boolean incluirRemovidas) {
        this.incluirRemovidas = incluirRemovidas;
    }
    
    //------------------------------Filtros de Docente-------------------------------------------
    
    private List<String> filtrosCentros;
    
    private List<String> filtrosSelecCentros;
    
    private List<String> filtrosAreaAtuacao;
    
    private List<String> filtrosSelecAreaAtuacao;

    public List<String> getFiltrosCentros() {
        return filtrosCentros;
    }

    public void setFiltrosCentros(List<String> filtrosCentros) {
        this.filtrosCentros = filtrosCentros;
    }

    public List<String> getFiltrosSelecCentros() {
        return filtrosSelecCentros;
    }

    public void setFiltrosSelecCentros(List<String> filtrosSelecCentros) {
        this.filtrosSelecCentros = filtrosSelecCentros;
    }

    public List<String> getFiltrosAreaAtuacao() {
        return filtrosAreaAtuacao;
    }

    public void setFiltrosAreaAtuacao(List<String> filtrosAreaAtuacao) {
        this.filtrosAreaAtuacao = filtrosAreaAtuacao;
    }

    public List<String> getFiltrosSelecAreaAtuacao() {
        return filtrosSelecAreaAtuacao;
    }

    public void setFiltrosSelecAreaAtuacao(List<String> filtrosSelecAreaAtuacao) {
        this.filtrosSelecAreaAtuacao = filtrosSelecAreaAtuacao;
    }

    
    
    public void filtrar() {

        List<Docente> docentesFiltrados = docenteFacade.findByCentroArea(filtrosSelecCentros, filtrosSelecAreaAtuacao);
        
        docenteDataModel = new DocenteDataModel(docentesFiltrados);

    }
    
    public void limparFiltro(){
        
        //filtros2 = null;
        filtrosSelecAreaAtuacao = null;
        filtrosSelecCentros = null;
//        filtros = null;
        docenteDataModel = null;
        
    }
    
    
    
    
    
    
    //-----------------------------------------LazyDataModel------------------------------------------------------
    
    private PessoaLazyModel docenteLazyModel;

    public PessoaLazyModel getDocenteLazyModel() {
        
        if(docenteLazyModel == null){
            docenteLazyModel = new PessoaLazyModel(pessoaFacade.listDocentes());
        }
        
        return docenteLazyModel;
    }
    
    @PostConstruct
    public void init() {
        docenteLazyModel = new PessoaLazyModel(pessoaFacade.listDocentes());
        
        filtrosCentros = new ArrayList<>();
        filtrosCentros.add("CMCC");
        filtrosCentros.add("CECS");
        filtrosCentros.add("CCNH");
        
        filtrosAreaAtuacao = new ArrayList<>();
        filtrosAreaAtuacao.add("Cognicao");
    }
    
    
    //------------------------------------------CRUD---------------------------------------------------------------------------------------------
    
    public Docente buscar(Long id) {

        return docenteFacade.find(id);
    }
    
    public void salvar(){
        try {
            docenteFacade.save(docenteSalvar);
            JsfUtil.addSuccessMessage("Docente " + docenteSalvar.getNome() + " cadastrado com sucesso!");
            docenteSalvar = null;
            docenteLazyModel = null;
            
        } catch (Exception e) {
            JsfUtil.addErrorMessage("Não foi possível cadastrar o docente");
        }
    }

    public void editar() {
        try {
            docenteFacade.edit(docente);
            JsfUtil.addSuccessMessage("Docente editado com sucesso!");
            docente = null;
            docenteLazyModel = null;
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Ocorreu um erro de persistência, não foi possível editar o docente: " + e.getMessage());

        }
    }

    public void delete() {
        docente = (Docente) docenteLazyModel.getRowData();
        try {
            docenteFacade.remove(docente);
            docente = null;
            JsfUtil.addSuccessMessage("Docente Deletado");
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Ocorreu um erro de persistência");
        }

        docenteLazyModel = null;
    }
    
    public List<Docente> listarTodas(){
        return docenteFacade.findAll();
    }
    
    //----------------------------------------Páginas web------------------------------------------------------
    
    public String prepareEdit() {
        docente = (Docente) (Pessoa) docenteLazyModel.getRowData();
        return "/Cadastro/editDocente";
    }
    
    //------------------------------------------Cadastro-------------------------------------------------------------------------------------------
   
    public void cadastrarDocentes() {

        String[] palavras;

        try {

            try (BufferedReader lerArq = new BufferedReader(new InputStreamReader(new FileInputStream("/home/charles/NetBeansProjects/Arquivos CSV/docentes.csv"), "UTF-8"))) {
                
                String linha = lerArq.readLine(); //cabeçalho
                
                linha = lerArq.readLine();
                
                while (linha != null) {
                    
                    linha = linha.replaceAll("\"", "");
                    
                    palavras = linha.split(",");
                    
                    List<Docente> docentes = docenteFacade.findByName(trataNome(palavras[1]));
                    
                    if (docentes.isEmpty()) {
                        
                        Docente d = new Docente();
                        
                        d.setNome(trataNome(palavras[1]));
                        d.setSiape(palavras[2]);
                        d.setEmail(palavras[3]);
                        d.setCentro(palavras[4]);
                        d.setAdm(false);
                        
                        docenteFacade.save(d);
                        
                    }
                    
                    linha = lerArq.readLine();
                }
            } 

        } catch (IOException e) {
            System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage());
        }
        
        docenteLazyModel = null;
        JsfUtil.addSuccessMessage("Cadastro de docentes realizado com sucesso", "");

    }
  
    private String trataNome(String nome) { 
        
     String retorno = "";
     String[] palavras = nome.split(" ");
     
     for(String p: palavras){
         
         if(p.equals("DAS") || p.equals("DOS") || p.length() <= 2){
             p = p.toLowerCase();
             retorno += p + " ";
         }
        
         
         else{
             p = p.charAt(0) + p.substring(1, p.length()).toLowerCase();
             retorno += p + " ";
         }
         
     }
        
return retorno;

} 
   
}
