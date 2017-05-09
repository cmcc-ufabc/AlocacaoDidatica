package facade;

import controller.HibernateUtil;
import java.util.List;
import javax.ejb.Stateless;
import model.Disciplina;
import model.OfertaDisciplina;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

@Stateless
public class OfertaDisciplinaFacade extends AbstractFacade<OfertaDisciplina>{
    
    public OfertaDisciplinaFacade() {
        super(OfertaDisciplina.class);
    }

    @Override
    protected SessionFactory getSessionFactory() {
        return HibernateUtil.getSessionFactory();
    }
    
    //Retorna a lista de disponibilidades
    public OfertaDisciplina inicializarColecaoDisponibilidades(OfertaDisciplina oD){
        Session session = getSessionFactory().openSession();
        session.refresh(oD);
        Hibernate.initialize(oD);
        Hibernate.initialize(oD.getDispo());
        session.close();
        return oD;   
    }

    /**
     * Filtra as Ofertas de disciplina por disciplina(s) com afinidades
     * e/ou turno e/ou campus e quadrimestre
     * @param disciplinas List  de Disciplina
     * @param turno string D ou N
     * @param campus string SA ou SB
     * @param quadrimestre int 1, 2 ou 3
     * @return Lista de ofertas de disciplinas filtradas de acordo com os parametros
     */
    public List<OfertaDisciplina> filtrarAfinidTurnCampQuad(List<Disciplina> disciplinas, String turno, String campus, int quadrimestre) {
        try {
            Session session = getSessionFactory().openSession();
            Criteria criteria = session.createCriteria(OfertaDisciplina.class);

            if (!campus.equals("")) {
                criteria.add(Restrictions.eq("campus", campus));
            }
            if (!turno.equals("")) {
                criteria.add(Restrictions.eq("turno", turno));
            }
            if (quadrimestre != 0) {
                criteria.add(Restrictions.eq("quadrimestre", quadrimestre));
            }
            if (!disciplinas.isEmpty()) { //Caso tenha sido escolhido para filtrar por disciplinas com afinidades
                criteria.add(Restrictions.in("disciplina", disciplinas));
            }
            List resultado = criteria.list();

            session.close();
            return resultado;
        } catch (HibernateException e) {
            return null;
        }
    }
    
    /**
     * Filtro completo de ofertas de disciplinas
     *
     * @param eixos
     * @param cursos
     * @param turno
     * @param campus
     * @param quadrimestre
     * @return Lista de ofertas de disciplinas filtradas
     */
    public List<OfertaDisciplina> filtrarEixoCursoTurnoCampusQuad(List<String> eixos, List<String> cursos, String turno, String campus, int quadrimestre) {
        try {
            Session session = getSessionFactory().openSession();
            Criteria criteria = session.createCriteria(OfertaDisciplina.class);

            if (!campus.equals("")) {//Se o usuario escolheu filtrar pelo campus
                criteria.add(Restrictions.eq("campus", campus));
            }
            if (!turno.equals("")) {//Se o usuario escolheu filtrar pelo turno
                criteria.add(Restrictions.eq("turno", turno));
            }
            if (quadrimestre != 0) {//Se o usuario escolheu filtrar pelo quadrimestre
                criteria.add(Restrictions.eq("quadrimestre", quadrimestre));
            }
            if (eixos.size() > 0) {//Caso algum filtro de eixo tenha sido selecionado
                criteria.createAlias("disciplina", "d");
                if (cursos.size() > 0) {//Caso algum filtro de curso tenha sido selecionado
                    criteria.add(Restrictions.or(Restrictions.in("d.eixo", eixos), Restrictions.in("d.curso", cursos)));
                } else {
                    criteria.add(Restrictions.in("d.eixo", eixos));
                }
            } else {//Caso nenhum filtro de eixo tenha sido selecionado
                if (cursos.size() > 0) {//Caso algum filtro de curso tenha sido selecionado
                    criteria.createAlias("disciplina", "d").add(Restrictions.or(Restrictions.in("d.curso", cursos)));
                }
            }
            List resultado = criteria.list();

            session.close();
            return resultado;
        } catch (HibernateException e) {
            return null;
        }
    }

    /**
     * Lista as ofertas de disciplina por quadrimestre
     * @param quadrimestre int
     * @return Lista de ofertas de disciplinas filtradas
     */
    public List<OfertaDisciplina> findAllQuad(int quadrimestre) {
        Session session = getSessionFactory().openSession();
        Criteria crit = session.createCriteria(OfertaDisciplina.class);
        crit.add(Restrictions.eq("quadrimestre", quadrimestre));
        crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        
        List results = crit.list();
        
        session.close();
        return results;
    }
    
    public List<OfertaDisciplina> ordenarCursos(int quadrimestre){
        Session session = getSessionFactory().openSession();
        Criteria crit = session.createCriteria(OfertaDisciplina.class);
        crit.add(Restrictions.eq("quadrimestre", quadrimestre));
        crit.addOrder(Order.asc("curso"));
        crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        
        List results = crit.list();
        
        session.close();
        return results;
    }

    public List<OfertaDisciplina> buscaCurso(List<String> cursos, int quadrimestre){
        Session session = getSessionFactory().openSession();
        Criteria crit = session.createCriteria(OfertaDisciplina.class);
        crit.add(Restrictions.eq("quadrimestre", quadrimestre));
        if (cursos.size() > 0) {
            crit.createAlias("disciplina", "d").add(Restrictions.or(Restrictions.in("d.curso", cursos)));
        }
        crit.addOrder(Order.asc("curso"));
        crit.addOrder(Order.asc("d.nome"));
        crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        
        List results = crit.list();
        
        session.close();
        return results;
    }
}


