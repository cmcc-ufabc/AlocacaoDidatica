package controller;

import facade.PessoaFacade;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import model.Pessoa;

import util.PessoaLazyModel;

@Named(value = "pessoaController")
@SessionScoped
public class PessoaController implements Serializable {

    public PessoaController() {
        pessoa = new Pessoa();
    }

    //Guarda a  pessoa atual
    private Pessoa pessoa;

    private Pessoa adm;

    private Pessoa pessoaSalvar;

    @EJB
    private PessoaFacade pessoaFacade;

    //-----------------------------------Getters e Setters---------------------------------------------
    public void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }

    private Pessoa getPessoa(Long key) {
        return this.buscar(key);
    }

    public Pessoa getPessoa() {
        if (pessoa == null) {
            pessoa = new Pessoa();
        }
        return pessoa;
    }

    public Pessoa getAdm() {
        return adm;
    }

    public void setAdm(Pessoa adm) {
        this.adm = adm;
    }

    public Pessoa getPessoaSalvar() {
        if (pessoaSalvar == null) {
            pessoaSalvar = new Pessoa();
        }
        return pessoaSalvar;
    }

    public void setPessoaSalvar(Pessoa pessoaSalvar) {
        this.pessoaSalvar = pessoaSalvar;
    }

    //---------------------------------------Páginas web------------------------------------------------------------
    public String prepareCreate(int i) {
        pessoa = new Pessoa();
        if (i == 1) {
            return "/view/pessoa/Create";
        } else {
            return "Create";
        }
    }

    public String index() {
        pessoa = null;
        pessoaDataModel = null;
        return "/index";
    }

    public String prepareEdit() {
        pessoa = (Pessoa) pessoaDataModel.getRowData();
        return "/Cadastro/editDocente";
    }

    public String prepareView() {
        pessoa = (Pessoa) pessoaDataModel.getRowData();
        return "View";
    }

    //---------------------------LazyData Model--------------------------------------------------------------------
    private PessoaLazyModel pessoaDataModel;

    //Carrega o lazymodel Pessoa
    public PessoaLazyModel getPessoaLazyModel() {
        if (pessoaDataModel == null) {
            pessoaDataModel = new PessoaLazyModel(this.listarTodas());
        }
        return this.pessoaDataModel;
    }

    //Data model com os administradores do sistema
    private PessoaLazyModel admLazyModel;

    //Carrega o lazymodel ADM
    public PessoaLazyModel getAdmLazyModel() {
        if (admLazyModel == null) {
            admLazyModel = new PessoaLazyModel(pessoaFacade.listAdms());
        }
        return admLazyModel;
    }

    //Inicia os lazy e datamodels
    @PostConstruct
    public void init() {
        pessoaDataModel = new PessoaLazyModel(this.listarTodas());
        admLazyModel = new PessoaLazyModel(pessoaFacade.listAdms());
    }

    //---------------------------------------------------CRUD-------------------------------------------------------
    //Busca todas as pessoas
    private List<Pessoa> listarTodas() {
        return pessoaFacade.findAll();
    }

    //Cria uma nova pessoa
    public void salvarNoBanco() {
        try {
            pessoaFacade.save(pessoa);
            JsfUtil.addSuccessMessage("Pessoa " + pessoa.getNome() + " criado com sucesso!");
            pessoa = null;
            recriarModelo();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Ocorreu um erro de persistência");
        }
    }

    //Busca pessoa por id
    public Pessoa buscar(Long id) {
        return pessoaFacade.find(id);
    }

    //Salva no banco
    public void salvar() {
        try {
            pessoaFacade.save(pessoaSalvar);
            JsfUtil.addSuccessMessage("Docente " + pessoaSalvar.getNome() + " cadastrado com sucesso!");
            pessoaSalvar = null;
            pessoaDataModel = null;
        } catch (Exception e) {
            JsfUtil.addErrorMessage("Não foi possível cadastrar o docente");
        }
    }

    //Edita uma pessoa
    public void editar() {
        try {
            pessoaFacade.edit(pessoa);
            JsfUtil.addSuccessMessage("Docente editado com sucesso!");
            pessoa = null;
            pessoaDataModel = null;
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Ocorreu um erro de persistência, não foi possível editar o docente: " + e.getMessage());
        }
    }

    //Deleta uma pessoa
    public void delete() {
        pessoa = (Pessoa) pessoaDataModel.getRowData();
        try {
            pessoaFacade.remove(pessoa);
            pessoa = null;
            JsfUtil.addSuccessMessage("Pessoa Deletado");
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Ocorreu um erro de persistência");
        }
        recriarModelo();
    }

    //Salva um novo adm
    public void salvarAdm() {
        try {
            adm.setAdm(true);
            pessoaFacade.edit(adm);
            adm = null;
            admLazyModel = null;
            JsfUtil.addSuccessMessage("Administrador Salvo!");
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Ocorreu um erro de persistência");
        }
    }

    //Remove um adm
    public void removerAdm() {
        pessoa = (Pessoa) admLazyModel.getRowData();
        try {
            pessoa.setAdm(false);
            pessoaFacade.edit(pessoa);
            pessoa = null;
            admLazyModel = null;
            JsfUtil.addSuccessMessage("Administrador removido");
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, "Ocorreu um erro de persistência");
        }
    }

    public SelectItem[] getItemsAvaiableSelectOne() {
        return JsfUtil.getSelectItems(pessoaFacade.findAll(), true);
    }

    //--------------------------------------------------------------------------------------------------------------
    public void recriarModelo() {
        pessoaDataModel = null;
    }

    //Cadastro-------------------------------------------------------------------------------------------
    //Faz o cadastro de pessoas
    public void cadastrarPessoas() {
        String[] palavras;
        try {
            FileReader arq = new FileReader("/home/charles/NetBeansProjects/Arquivos CSV/docentes.csv");
            BufferedReader lerArq = new BufferedReader(arq);
            String linha = lerArq.readLine(); //cabeçalho
            // lê a primeira linha 
            // a variável "linha" recebe o valor "null" quando o processo 
            // de repetição atingir o final do arquivo texto 
            linha = lerArq.readLine();
//            linha = linha.replaceAll("\"", "");
            while (linha != null) {
                linha = linha.replaceAll("\"", "");
                palavras = linha.split(",");
                List<Pessoa> pessoas = pessoaFacade.findByName(trataNome(palavras[1]));
                if (pessoas.isEmpty()) {
                    Pessoa p = new Pessoa();
                    p.setNome(trataNome(palavras[1]));
                    p.setSiape(palavras[2]);
                    p.setEmail(palavras[3]);
                    p.setCentro(palavras[4]);
                    pessoaFacade.save(p);
                }
                linha = lerArq.readLine();
            }
            arq.close();
        } catch (IOException e) {
            System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage());
        }
        recriarModelo();
        JsfUtil.addSuccessMessage("Cadastro de docentes realizado com sucesso", "");
    }

    //Faz o tratamento dos nomes
    private String trataNome(String nome) {
        String retorno = "";
        String[] palavras = nome.split(" ");
        for (String p : palavras) {
            if (p.equals("DAS") || p.equals("DOS") || p.length() <= 2) {
                p = p.toLowerCase();
                retorno += p + " ";
            } else {
                p = p.charAt(0) + p.substring(1, p.length()).toLowerCase();
                retorno += p + " ";
            }
        }
        return retorno;
    }

    //AutoComplete----------------------------------------------------------------------------------------
    public List<Pessoa> completePessoa(String query) {
        query = query.toLowerCase();
        List<Pessoa> allPessoas = this.listarTodas();
        List<Pessoa> filteredPessoas = new ArrayList<>();
        for (Pessoa p : allPessoas) {
            if (p.getNome().toLowerCase().startsWith(query)) {
                filteredPessoas.add(p);
            }
        }
        return filteredPessoas;
    }

    //Centro--------------------------------------------------------------------------------------------
    public List<String> completeCentro(String query) {
        query = query.toLowerCase();
        List<String> centros = new ArrayList<>();
        centros.add("CCNH");
        centros.add("CECS");
        centros.add("CMCC");
        List<String> filteredCentros = new ArrayList<>();
        for (String c : centros) {
            if (c.toLowerCase().startsWith(query)) {
                filteredCentros.add(c);
            }
        }
        return filteredCentros;
    }

    //----------------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------------
    @FacesConverter(forClass = Pessoa.class)
    public static class PessoaControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            PessoaController controller = (PessoaController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "pessoaController");
            return controller.getPessoa(getKey(value));
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
            if (object instanceof Pessoa) {
                Pessoa d = (Pessoa) object;
                return getStringKey(new BigDecimal(d.getID().toString()).setScale(0, BigDecimal.ROUND_HALF_UP).longValue());

            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Pessoa.class.getName());
            }
        }
    }
}
