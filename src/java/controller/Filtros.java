package controller;

import facade.DisciplinaFacade;
import facade.DocenteFacade;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import model.Disciplina;
import model.Docente;
import util.DocenteDataModel;

public class Filtros {

    public Filtros() {
        
    }
    
    @EJB
    private DisciplinaFacade disciplinaFacade;

    //------------------------------Filtros de Disciplina-----------------------------------------------
    
    //Todos os eixos
    private List<String> filtrosEixos;
    
    public List<String> getFiltrosEixos() {
        filtrosEixos = new ArrayList<>();
        filtrosEixos.add("Ciencia, Tecnologia e Inovacao");
        filtrosEixos.add("Comunicacao e Informacao");
        filtrosEixos.add("Energia");
        filtrosEixos.add("Espaco, Cultura e Temporalidade");
        filtrosEixos.add("Estado, Sociedade e Mercado");
        filtrosEixos.add("Estrutura da Materia");
        filtrosEixos.add("Humanidades");
        filtrosEixos.add("Pensamento, Expressao e Significado");
        filtrosEixos.add("Processos de Transformacao");
        filtrosEixos.add("Mais de um eixo");
        filtrosEixos.add("Representacao e Simulacao");
        
        return filtrosEixos;
    }

    public void setFiltrosEixos(List<String> filtrosEixos) {
        this.filtrosEixos = filtrosEixos;
    }
    
    //Eixos escolhidos para filtrar
    private List<String> filtrosSelecEixos;
    
    public List<String> getFiltrosSelecEixos() {
        return filtrosSelecEixos;
    }

    public void setFiltrosSelecEixos(List<String> filtrosSelecEixos) {
        this.filtrosSelecEixos = filtrosSelecEixos;
    }
    
    //Todos os cursos
    private List<String> filtrosCursos;
    
    public List<String> getFiltrosCursos() {
        filtrosCursos = new ArrayList<>();
        filtrosCursos.add("Bacharelado em Ciencia da Computacao");        
        filtrosCursos.add("Bacharelado em Ciencia e Tecnologia");
        filtrosCursos.add("Bacharelado em Ciencia e Humanidades");
        filtrosCursos.add("Bacharelado em Economia");
        filtrosCursos.add("Bacharelado em Matematica");
        filtrosCursos.add("Bacharelado em Neurociencia");
        filtrosCursos.add("Bacharelado em Planejamento Territorial");
        filtrosCursos.add("Bacharelado em Politicas Publicas");
        filtrosCursos.add("Bacharelado em Relacoes Internacionais");
        filtrosCursos.add("Ciencias Biologicas");
        filtrosCursos.add("Comum aos BI's");
        filtrosCursos.add("Engenharia Aeroespacial");
        filtrosCursos.add("Engenharia Ambiental e Urbana");
        filtrosCursos.add("Engenharia Biomedica");
        filtrosCursos.add("Engenharia de Automacao e Robotica");
        filtrosCursos.add("Engenharia de Energia");
        filtrosCursos.add("Engenharia de Gestao");
        filtrosCursos.add("Engenharia de Informacao");
        filtrosCursos.add("Engenharia de Materiais");
        filtrosCursos.add("Engenharias");
        filtrosCursos.add("Filosofia");
        filtrosCursos.add("Fisica");
        filtrosCursos.add("Licenciatura em Matematica");
        filtrosCursos.add("Licenciaturas");
        filtrosCursos.add("Quimica");
        return filtrosCursos;
    }

    public void setFiltrosCursos(List<String> filtrosCursos) {
        this.filtrosCursos = filtrosCursos;
    }
    
    //Cursos escolhidos para filtrar
    private List<String> filtrosSelecCursos;

    public List<String> getFiltrosSelecCursos() {
        return filtrosSelecCursos;
    }

    public void setFiltrosSelecCursos(List<String> filtrosSelecCursos) {
        this.filtrosSelecCursos = filtrosSelecCursos;
    }
    
    //--------------------------------Filtros de Docentes---------------------------------------------
    
    //Todos os centros
    private List<String> filtrosCentros;
    
    public List<String> getFiltrosCentros() {
        filtrosCentros = new ArrayList<>();
        filtrosCentros.add("CCNH");
        filtrosCentros.add("CECS");
        filtrosCentros.add("CMCC");
        return filtrosCentros;
    }

    public void setFiltrosCentros(List<String> filtrosCentros) {
        
        this.filtrosCentros = filtrosCentros;
    }
    
    //Centros escolhidos para filtrar
    private List<String> filtrosSelecCentros;
    
    public List<String> getFiltrosSelecCentros() {
        return filtrosSelecCentros;
    }

    public void setFiltrosSelecCentros(List<String> filtrosSelecCentros) {
        this.filtrosSelecCentros = filtrosSelecCentros;
    }
    
    //Todas as áreas de atuação dos docentes
    private List<String> filtrosAreaAtuacao;
    
    public List<String> getFiltrosAreaAtuacao() {
        filtrosAreaAtuacao = new ArrayList<>();
        filtrosAreaAtuacao.add("Ciencia da Computacao");
        filtrosAreaAtuacao.add("Cognicao");
        filtrosAreaAtuacao.add("Ensino de Matematica");
        filtrosAreaAtuacao.add("Matematica");
        return filtrosAreaAtuacao;
    }

    public void setFiltrosAreaAtuacao(List<String> filtrosAreaAtuacao) {
        this.filtrosAreaAtuacao = filtrosAreaAtuacao;
    }
    
    //Áreas escolhidas para filtrar
    private List<String> filtrosSelecAreaAtuacao;

    public List<String> getFiltrosSelecAreaAtuacao() {
        return filtrosSelecAreaAtuacao;
    }

    public void setFiltrosSelecAreaAtuacao(List<String> filtrosSelecAreaAtuacao) {
        this.filtrosSelecAreaAtuacao = filtrosSelecAreaAtuacao;
    }

    //Filtra os docentes de acordo com as áreas de atuação
    public List<Docente> filtrarDocente(DocenteDataModel docenteDataModel, DocenteFacade docenteFacade) {
        if (!filtrosSelecAreaAtuacao.isEmpty()) {
            List<Docente> docentesFiltrados = docenteFacade.findByArea(filtrosSelecAreaAtuacao);

            filtrosSelecAreaAtuacao = null;
            return docentesFiltrados;
        }
        else{
            return new ArrayList<>();
        }
    }
    
    //Limpa os filtros dos docentes selecionados
    public void limparFiltroDocente(){
        filtrosSelecAreaAtuacao = null;
        filtrosSelecCentros = null;  
    }

//----------------------------------------AutoComplete----------------------------------------------------------------------------------------
  
//Disciplina----------------------------------------------------------------------------------------------------------    
    public List<Disciplina> completeDisciplina(String query) {
        query = query.toLowerCase();
        List<Disciplina> allDisciplinas = disciplinaFacade.findAll();
        List<Disciplina> filteredDisciplinas = new ArrayList<>();

        for (Disciplina d : allDisciplinas) {
            if (d.getNome().toLowerCase().startsWith(query)) {
                filteredDisciplinas.add(d);
            }
        }
        return filteredDisciplinas;
    }
    
 //Eixo----------------------------------------------------------------------------------------------------------
    public List<String> completeEixo(String query){
        query = query.toLowerCase();
        List<String> filteredEixos = new ArrayList<>();

        for (String e : this.getFiltrosEixos()) {
            if (e.toLowerCase().startsWith(query)) {
                filteredEixos.add(e);
            }
        }
        return filteredEixos;   
    }
    
    //Curso-----------------------------------------------------------------------------------------------
    public List<String> completeCurso(String query){
        query = query.toLowerCase();
        List<String> filteredCursos = new ArrayList<>();

        for (String c : this.getFiltrosCursos()) {
            if (c.toLowerCase().startsWith(query)) {
                filteredCursos.add(c);
            }
        }
        return filteredCursos;
    }
    
    //Área de atuação do docente------------------------------------------------------------------------
    
    public List<String> completeArea(String query){
        query = query.toLowerCase();
        List<String> filteredAreas = new ArrayList<>();

        for (String a : this.getFiltrosAreaAtuacao()) {
            if (a.toLowerCase().startsWith(query)) {
                filteredAreas.add(a);
            }
        }
        return filteredAreas;  
    }
}
