package controller;

import facade.DisciplinaFacade;
import facade.DispFacade;
import facade.TurmaDocenteFacade;
import facade.TurmaFacade;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
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
import model.Afinidade;
import model.Disciplina;
import model.Disp;
//import model.Docente;
import model.Horario;
import model.Pessoa;
import model.Turma;
import model.TurmaDocente;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleModel;
//import util.TurmaDataModel;
import util.TurmaLazyModel;

@Named(value = "turmaController")
@SessionScoped
public class TurmaController implements Serializable {

    private ExternalContext externalContext;
    private LoginBean loginBean;
    
    public TurmaController() {
        externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sessionMap = externalContext.getSessionMap();
        loginBean = (LoginBean) sessionMap.get("loginBean");
        docente = loginBean.getDocente();
    }

    @EJB
    private DisciplinaFacade disciplinaFacade;

    @EJB
    private TurmaFacade turmaFacade;

    @EJB
    private DispFacade dispFacade;

    //Cadastro-------------------------------------------------------------------------------------------
    //Método de cadastro com novo arquivo (A ser difinido como olficial)
    //obs: ao apagar as turmas acontecem problemas para carregar a fase 2 devido à consulta por idTurma nas escolhas 
    //dos docentes, por isso é preciso apagar as escolhas dos docentes primeiro
    public void cadastrarTurmas2() {
        String[] palavras;
        try {
            try (BufferedReader lerArq = new BufferedReader(new InputStreamReader(new FileInputStream("/home/charles/Documentos/Erick/Teste.csv"), "UTF-8"))) {
                String linha = lerArq.readLine();
                while (linha != null) {
                    linha = linha.replaceAll("\"", "");
                    palavras = linha.split(";", -1);

                    //Turma t = new Turma();
                    String nome = palavras[0];
                    String codigo = palavras[1];

                    Disciplina d = disciplinaFacade.findByCodOrName(codigo, nome);

                    //t.setDisciplina(d);
                    palavras[3] = palavras[3].replaceAll(" ,", ",");
                    palavras[3] = palavras[3].replaceAll("\"", "");
                    palavras[4] = palavras[4].replaceAll(" ,", ",");
                    palavras[4] = palavras[4].replaceAll("\"", "");

                    String teoria = palavras[3];
                    String pratica = palavras[4];

                    if (nome.equals("Processamento da Informação")) {
                        Disciplina dt = disciplinaFacade.findByCodOrName(codigo, "Processamento da Informação Teoria");
                        palavras[3] = teoria;
                        palavras[4] = "";
                        criaTurma(palavras, dt);

                        Disciplina dp = disciplinaFacade.findByCodOrName(codigo, "Processamento da Informação Prática");
                        palavras[4] = pratica;
                        palavras[3] = "";
                        criaTurma(palavras, dp);
                    } else {
                        criaTurma(palavras, d);
                    }

                    linha = lerArq.readLine();
                }
            }
        } catch (IOException e) {
            System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage());
        }
    }

    public void criaTurma(String palavras[], Disciplina d) {
        Turma t = new Turma();
        t.setDisciplina(d);
        String teoria = palavras[3];
        String pratica = palavras[4];

        if (!teoria.equals("") || !pratica.equals("")) {
            if (teoria.contains("quinzenal II")) {
                teoria = teoria.replace("quinzenal II", "quinzenal 2");
            }
            if (teoria.contains("quinzenal I")) {
                teoria = teoria.replace("quinzenal I", "quinzenal 1");
            }
            if (pratica.contains("quinzenal II")) {
                pratica = pratica.replace("quinzenal II", "quinzenal 2");
            }
            if (pratica.contains("quinzenal I")) {
                pratica = pratica.replace("quinzenal I", "quinzenal 1");
            }
            if (teoria.contains("quinzenal 2,")) {
                teoria = teoria.replace("quinzenal 2,", "quinzenal 2 ");
            }
            if (teoria.contains("quinzenal 1,")) {
                teoria = teoria.replace("quinzenal 1,", "quinzenal 1 ");
            }
            if (teoria.contains("semanal,")) {
                teoria = teoria.replace("semanal,", "semanal");
            }
            if (pratica.contains("quinzenal 2,")) {
                pratica = pratica.replace("quinzenal 2,", "quinzenal 2 ");
            }
            if (pratica.contains("quinzenal 1,")) {
                pratica = pratica.replace("quinzenal 1,", "quinzenal 1 ");
            }
            if (pratica.contains("semanal,")) {
                pratica = pratica.replace("semanal,", "semanal");
            }
            String tp;
            if (teoria.isEmpty() || pratica.isEmpty()) {
                tp = teoria + pratica;
            } else {
                tp = teoria + " " + pratica;
            }

            String[] horariosCompletos = tp.trim().split("(?<=semanal)|(?<=quinzenal 1)|(?<=quinzenal 2)");

            List<Horario> horarios = new ArrayList<Horario>();

            for (String horarioCompleto : horariosCompletos) {
                horarioCompleto = horarioCompleto.trim();
                String[] partes = horarioCompleto.split("das|,");
                Horario h = new Horario();

                h.setDia(partes[0]);
                h.setHora(partes[1]);
                h.setSala(partes[2]);
                h.setPeriodicidade(partes[3]);

                horarios.add(h);
            }

            t.setHorarios(horarios);

            String campus = palavras[5];
            String codturma = palavras[2];
            String turno = palavras[6];
            String curso = palavras[12];

            if (campus.equals("Santo André")) {
                campus = "SA";
            }
            if (campus.equals("São Bernardo do Campo")) {
                campus = "SBC";
            }
            if (turno.equals("diurno")) {
                turno = "D";
            }
            if (turno.equals("noturno")) {
                turno = "N";
            }

            t.setCampus(campus);
            t.setCodturma(codturma);
            t.setTurno(turno);
            t.setCurso(curso);
            turmaFacade.save(t);
        }
    }

    //Apaga todas as turmas cadastradas
    public void deleteTurmas() {
        List<Turma> turmasOfertadas = turmaFacade.findAll();
        //For para apagar cada turma do banco.
        for (Turma t : turmasOfertadas) {
            //t = turmaFacade.find(t.getID());
            turmaFacade.remove(t);
        }
        turmalazymodel = null;
    }

    //DataModel e LazyModel ---------------------------------------------------------------------------------
    
    private TurmaLazyModel turmalazymodel; //LazyModel para página de Cadastro e Turmas da Fase 2

    public TurmaLazyModel getTurmalazymodel() {
        if (turmalazymodel == null) {

            List<Turma> turmas = turmaFacade.findAll();
            turmalazymodel = new TurmaLazyModel(turmas);
        }
        return turmalazymodel;
    }

    private TurmaLazyModel listaRequisicoes; //DataModel para o docente administrar suas turmas na Fase 2

    public TurmaLazyModel getListaRequisicoes() {
        List<Turma> requisicoes = new ArrayList<>();
        Long id = docente.getID();
        List<TurmaDocente> salvas = new ArrayList<>();
        //Método para buscar turmas do docente em TurmaDocenteFacade
        if (listaRequisicoes == null) {
            salvas = turmasEscolhidasFacade.listTurmas(id);
            for (TurmaDocente d : salvas) {
                Turma t = turmaFacade.listByID(d.getIdTurma());
                //String turma = t.getCodturma();
                //String turno = t.getTurno();
                //String campus = t.getCampus();
                requisicoes.add(t);
            }
            listaRequisicoes = new TurmaLazyModel(requisicoes);
        }
        return listaRequisicoes;
    }

    private int total;

    //Método para buscar o número de solicitações por turma
    public int solicitacoes(Long id) {
        total = turmasEscolhidasFacade.totalSolicitacoes(id);

        return total;
    }

    //Método para visualizar os horários das turmas
    public String horariosTurma(Long id) {
        String horario = "";
        Turma t = turmaFacade.find(id);
        List<Horario> h = t.getHorarios();
        for (int i = 0; i < h.size(); i++) {
            if (i < h.size() - 1) {
                horario = horario + h.get(i).getDia() + " " + h.get(i).getHora() + " " + h.get(i).getPeriodicidade() + ", ";
            } else {
                horario = horario + h.get(i).getDia() + " " + h.get(i).getHora() + " " + h.get(i).getPeriodicidade();
            }
        }
        return horario;
    }

    //Filtros para a Fase II ---------------------------------------------------------------------------------
    private String campus;

    private String turno;

    private String valor;

    private String planejadas;

    private boolean filtrarAfinidades;

    private Set<Afinidade> afinidades;

    private List<Disciplina> AfinidadesDocente;

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
        return turno;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getPlanejadas() {
        return planejadas;
    }

    public void setPlanejadas(String planejadas) {
        this.planejadas = planejadas;
    }

    public boolean isFiltrarAfinidades() {
        return filtrarAfinidades;
    }

    public void setFiltrarAfinidades(boolean filtrarAfinidades) {
        this.filtrarAfinidades = filtrarAfinidades;
    }
    
    //Métodos que atualizam os filtros no evento de Clique
    public void onChangeCampus(ValueChangeEvent event) {
        campus = event.getNewValue().toString();
        if (campus.equals("Ambos")) {
            campus = "";
            if (turno.equals("Ambos")) {
                turno = "";
            }
            filtrar();
        } else {
            if (turno.equals("Ambos")) {
                turno = "";
            }
            filtrar();
        }
    }

    public void onChangeTurno(ValueChangeEvent event) {
        turno = event.getNewValue().toString();
        if (turno.equals("Ambos")) {
            turno = "";
            if (campus.equals("Ambos")) {
                campus = "";
            }
            filtrar();
        } else {
            if (campus.equals("Ambos")) {
                campus = "";
            }
            filtrar();
        }
    }

    public void onChangeAfinidades(ValueChangeEvent event) {
        valor = event.getNewValue().toString();
        turno = "";
        campus = "";
        if (valor.equals("sim")) {
            filtrarAfinidades = true;
            filtrar();
        } else {
            filtrarAfinidades = false;
            limparFiltros();
        }
    }

    public void onChangePlanejamento(ValueChangeEvent event) {
        planejadas = event.getNewValue().toString();
        turno = "";
        campus = "";
        filtrar();
    }

    //Método para filtrar as turmas da Fase 2
    public void filtrar() {
        turmalazymodel = null;
        AfinidadesDocente = new ArrayList<>();

        if (filtrarAfinidades) {
            afinidades = docente.getAfinidades();
            for (Afinidade a : afinidades) {
                if (a.getEstado().equals("Adicionada")) {
                    AfinidadesDocente.add(a.getDisciplina());
                }
            }
        }
        
        //Ainda é preciso passar o quadrimestre vigente para fazer a consulta e não fixar o primeiro quadrimestre

        List<Disp> planejamento = new ArrayList<Disp>();
        if (planejadas.equals("sim")) {
            planejamento = dispFacade.findByDocenteQuad(docente, 1);
            for (Disp disp : planejamento) {
                Disciplina d = disp.getOfertaDisciplina().getDisciplina();
                AfinidadesDocente.add(d);
            }
        }

        turmalazymodel = new TurmaLazyModel(turmaFacade.filtrarTurmas(AfinidadesDocente, campus, turno));

        filtrarAfinidades = false;
        campus = "";
        turno = "";
        //teste = null;
        docenteSchedule = null;
        atual = null;
        //getTeste();
        getDocenteSchedule();
    }

    //Método para limpar os filtros e retornar o datamodel completo
    public void limparFiltros() {
        turmalazymodel = null;
        List<Turma> turmas = turmaFacade.findAll();
        turmalazymodel = new TurmaLazyModel(turmas);
        //teste = null;
        docenteSchedule = null;
        atual = null;
        //getTeste();
        getDocenteSchedule();
    }

    //Fase II Disponibilidade -------------------------------------------------------------------------------
    //Turma que será selecionada para visualização
    private Turma selectedTurma;

    public Turma getSelectedTurma() {
        return selectedTurma;
    }

    public void setSelectedTurma(Turma selectedTurma) {
        this.selectedTurma = selectedTurma;
    }

    //Método que trata do evento de seleção de turmas
    public void onRowSelect(SelectEvent event) {
        aux = event; //Atribui a aux a turma selecionada para ser usada por outros métodos
        //teste = null;
        docenteSchedule = null;
        atual = new ArrayList();
        Turma t = (Turma) event.getObject();
        atual.add(t);

        getDocenteSchedule();
    }

    public void onRowUnselect(UnselectEvent event) {
        //teste = null;
        //getTeste();
        atual = null;
    }
    
    //Mostrará as turmas já selecionadas pelo docente
    private ScheduleModel docenteSchedule;
    
    //Preenche o Schedule
    public ScheduleModel getDocenteSchedule() {
        docenteSchedule = new DefaultScheduleModel();
        List<TurmaDocente> requisicoes = new ArrayList<TurmaDocente>();
        List<Turma> escolhidas = new ArrayList<Turma>();
        Turma t;
        Long id = docente.getID();

        requisicoes = turmasEscolhidasFacade.listTurmas(id);
        for (TurmaDocente td : requisicoes) {
            t = turmaFacade.find(td.getIdTurma());
            escolhidas.add(t);
        }
        preencheDocente(escolhidas);

        return docenteSchedule;
    }

    public void setDocenteSchedule(ScheduleModel docenteSchedule) {
        this.docenteSchedule = docenteSchedule;
    }

    //Método para salvar turmas selecionadas
    public void incluirTurma() {
        try {
            listaRequisicoes = null;
            FacesContext context = FacesContext.getCurrentInstance();
            Turma t = (Turma) aux.getObject();

            //int testeaaa = 0;
            Long id = t.getID();
            Long docenteid = docente.getID();
            boolean conflito = verificar(t);

            if (conflito) {
                context.addMessage(null, new FacesMessage("Error", "Turma Requisitada não pode ser Salva devido à conflitos de horário!"));
            } else {
                TurmaDocente turmasEscolhidas = new TurmaDocente();
                turmasEscolhidas.setIdTurma(id);
                turmasEscolhidas.setIdDocente(docenteid);
                turmasEscolhidasFacade.save(turmasEscolhidas);
                //preecherSchedule();
                atual = null;
                //getTeste();
                getDocenteSchedule();
                context.addMessage(null, new FacesMessage("Successful", "Turma Requisitada Salva!"));
            }
        } catch (Exception e) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage("Error", "Turma Requisitada não pode ser Salva!"));
        }
    }

    //Verifica se existem conflitos entre os horários da turma selecionada e as turmas já salvas
    public boolean verificar(Turma escolhida) {
        List<Horario> selecionado = escolhida.getHorarios();
        boolean conflito = false;

        List<TurmaDocente> salvas = new ArrayList<>();
        salvas = turmasEscolhidasFacade.listTurmas(docente.getID());
        for (TurmaDocente td : salvas) {
            Turma t = turmaFacade.find(td.getIdTurma());
            //String nome = t.getDisciplina().getNome();
            List<Horario> horaAula = t.getHorarios();
            for (Horario h : horaAula) {
                //String hora1 = h.getDia() + h.getHora();
                for (Horario h2 : selecionado) {
                    //String hora2 = h2.getDia() + h2.getHora();
                    if (h2.getDia().equals(h.getDia()) && h2.getHora().equals(h.getHora())) {
                        conflito = true;
                    }
                }
            }
        }
        return conflito;
    }

    //Turma que será selecionada para exclusão
    private Turma selectedTurma2;

    public Turma getSelectedTurma2() {
        return selectedTurma;
    }

    public void setSelectedTurma2(Turma selectedTurma) {
        this.selectedTurma = selectedTurma;
    }

    public void onRowSelect2(SelectEvent event) {
        aux2 = event; //Atribui a aux a turma selecionada para ser usada por outros métodos
    }

    public void onRowUnselect2(UnselectEvent event) {

    }

    //Método para deletar a turma selecionada
    public void excluirTurma() {
        FacesContext context = FacesContext.getCurrentInstance();
        Turma t = (Turma) aux2.getObject();
        Long idTurma = t.getID();
        Long idDocente = docente.getID();
        TurmaDocente td = turmasEscolhidasFacade.TurmaSelecionada(idTurma, idDocente);
        turmasEscolhidasFacade.remove(td);
        listaRequisicoes = null;
        //preecherSchedule();
        atual = null;
        //getTeste();
        getDocenteSchedule();
        context.addMessage(null, new FacesMessage("Successful", "Turma Deletada"));
    }

    //Método para listar e preencher as turmas do Docente no Schedule
    public void preecherSchedule() {
        docenteSchedule = null;
        atual = null;
        //getTeste();
        getDocenteSchedule();
    }

    private static SelectEvent aux;

    private static SelectEvent aux2;

    private Pessoa docente;

    private List<Turma> atual;

    //private TurmaDocente turmasEscolhidas;
    @EJB
    private TurmaDocenteFacade turmasEscolhidasFacade;

    public Pessoa getDocente() {
        return docente;
    }

    public void setDocente(Pessoa docente) {
        this.docente = docente;
    }

    //Inicia as variáveis quando a página é carregada
    @PostConstruct
    public void init() {
        //turmasSchedule = new DefaultScheduleModel();
        docenteSchedule = null;
        //teste = null;
        atual = null;
        campus = "Ambos";
        turno = "Ambos";
        valor = "nao";
        planejadas = "nao";
    }
    
    //Método que busca as turmas já salvas para preencher o Schedule
    public void preencheDocente(List<Turma> turma) {
        for (Turma t : turma) {
            List<Horario> horarios = t.getHorarios();
            for (Horario h : horarios) {
                int dia = conversorDia(h.getDia());

                String hora = h.getHora().trim();
                int horaInicio = Integer.parseInt(hora.substring(0, 2));
                int minutoInicio = Integer.parseInt(hora.substring(3, 5));

                int horaFim = Integer.parseInt(hora.substring(9, 11));
                int minutoFim = Integer.parseInt(hora.substring(12, 14));

                //Inicio do horario
                Calendar inicio = Calendar.getInstance();
                inicio.set(Calendar.DAY_OF_WEEK, dia);
                if (horaInicio > 12) {
                    inicio.set(Calendar.AM_PM, Calendar.PM);
                    inicio.set(Calendar.HOUR, horaInicio - 12);
                } else {
                    inicio.set(Calendar.AM_PM, Calendar.AM);
                    inicio.set(Calendar.HOUR, horaInicio);
                }
                inicio.set(Calendar.MINUTE, minutoInicio);

                //Fim do horario
                Calendar fim = Calendar.getInstance();
                fim.set(Calendar.DAY_OF_WEEK, dia);
                if (horaFim > 12) {
                    fim.set(Calendar.AM_PM, Calendar.PM);
                    fim.set(Calendar.HOUR, horaFim - 12);
                } else {
                    fim.set(Calendar.AM_PM, Calendar.AM);
                    fim.set(Calendar.HOUR, horaFim);
                }
                fim.set(Calendar.MINUTE, minutoFim);

                String sigla = converterSigla(t);
                //Preenche o Schdule do Docente
                DefaultScheduleEvent schedule = new DefaultScheduleEvent(sigla, inicio.getTime(), fim.getTime(), "darkgreen-event");
                //teste.addEvent(schedule);
                docenteSchedule.addEvent(schedule);
            }
        }
        if (!(atual == null)) {
            Turma ta = atual.get(0);
            List<Horario> horarios2 = ta.getHorarios();
            for (Horario h : horarios2) {
                int dia2 = conversorDia(h.getDia());

                String hora2 = h.getHora().trim();
                int horaInicio2 = Integer.parseInt(hora2.substring(0, 2));
                int minutoInicio2 = Integer.parseInt(hora2.substring(3, 5));

                int horaFim2 = Integer.parseInt(hora2.substring(9, 11));
                int minutoFim2 = Integer.parseInt(hora2.substring(12, 14));

                //Inicio do horario
                Calendar inicio2 = Calendar.getInstance();
                inicio2.set(Calendar.DAY_OF_WEEK, dia2);
                if (horaInicio2 > 12) {
                    inicio2.set(Calendar.AM_PM, Calendar.PM);
                    inicio2.set(Calendar.HOUR, horaInicio2 - 12);
                } else {
                    inicio2.set(Calendar.AM_PM, Calendar.AM);
                    inicio2.set(Calendar.HOUR, horaInicio2);
                }
                inicio2.set(Calendar.MINUTE, minutoInicio2);

                //Fim do horario
                Calendar fim2 = Calendar.getInstance();
                fim2.set(Calendar.DAY_OF_WEEK, dia2);
                if (horaFim2 > 12) {
                    fim2.set(Calendar.AM_PM, Calendar.PM);
                    fim2.set(Calendar.HOUR, horaFim2 - 12);
                } else {
                    fim2.set(Calendar.AM_PM, Calendar.AM);
                    fim2.set(Calendar.HOUR, horaFim2);
                }
                fim2.set(Calendar.MINUTE, minutoFim2);

                String sigla2 = converterSigla(ta);
                //Preenche o Schdule do Docente
                DefaultScheduleEvent schedule2 = new DefaultScheduleEvent(sigla2, inicio2.getTime(), fim2.getTime(), "blue-event");
                //teste.addEvent(schedule2);
                docenteSchedule.addEvent(schedule2);
            }
        }
    }

    //Método para converter o dia para Calendar
    private int conversorDia(String dia) {
        dia = dia.trim();
        switch (dia) {
            case "segunda":
                return Calendar.MONDAY;
            case "terca":
                return Calendar.TUESDAY;
            case "quarta":
                return Calendar.WEDNESDAY;
            case "quinta":
                return Calendar.THURSDAY;
            case "sexta":
                return Calendar.FRIDAY;
            case "sabado":
                return Calendar.SATURDAY;
        }
        return 0;
    }

    //Método para transformar o nome da disciplina em sigla
    private String converterSigla(Turma t) {
        String nome = t.getDisciplina().getNome();
        String cod = t.getCodturma();
        String sigla = "";
        int n = nome.length();
        for (int i = 0; i < n; i++) {
            if (nome.charAt(i) >= 'A' && nome.charAt(i) <= 'Z') {
                sigla = sigla + nome.charAt(i);
            } else if (nome.charAt(i) == 'Á' || nome.charAt(i) == 'É' || nome.charAt(i) == 'Ó') {
                sigla = sigla + nome.charAt(i);
            }
        }
        sigla = sigla + "-" + cod;
        return sigla;
    }
}
