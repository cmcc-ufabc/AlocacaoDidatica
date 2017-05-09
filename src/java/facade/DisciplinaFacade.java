package facade;

import controller.HibernateUtil;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import model.Disciplina;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

@Stateless
public class DisciplinaFacade extends AbstractFacade<Disciplina> {

    public DisciplinaFacade() {
        super(Disciplina.class);
    }

    @Override
    protected SessionFactory getSessionFactory() {
        return HibernateUtil.getSessionFactory();
    }
    
    //Busca a lista de afinidades da disciplina
    public Disciplina inicializarColecaoAfinidades(Disciplina d) {
        Session session = getSessionFactory().openSession();
        session.refresh(d);
        Hibernate.initialize(d);
        Hibernate.initialize(d.getAfinidades());
        session.close();
        return d;
    }

    /**
     * Localiza uma disciplina de acordo com seu nome
     * @param nome string com o nome da disciplina
     * @return Objeto Disciplina
     */
    public List<Disciplina> findByName(String nome) {
        Session session = getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(Disciplina.class);
        criteria.add(Restrictions.eq("nome", nome));

        List results = criteria.list();
        session.close();

        return results;
    }

    
    /**
     * Localiza uma disciplina pelo seu código ou pelo nome
     * @param codigo string com o codigo da disciplina
     * @param nome 
     * @return Objeto Disciplina ou null
     */
    public Disciplina findByCodOrName(String codigo, String nome){
        Session session = getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(Disciplina.class);
        criteria.add(Restrictions.or(Restrictions.eq("codigo", codigo), Restrictions.eq("nome", nome)));
        
        List<Disciplina> results = criteria.list();
        session.close();
        
        if(!results.isEmpty()){
            return results.get(0);
        } else{
            return null;
        }  
    }

    /**
     * Localiza uma lista de disciplinas de acordo com os eixos e os cursos buscados
     * @param eixos lista de string com os eixos
     * @param cursos lista de string com os cursos
     * @return lista de Disciplina
     */
    public List<Disciplina> findByEixoCurso(List<String> eixos, List<String> cursos) {
        List<Disciplina> disciplinas = new ArrayList<>();
        try {
            Session session = getSessionFactory().openSession();
            if (!eixos.isEmpty()) {
                for (String eixo : eixos) {
                    Criteria criteria = session.createCriteria(Disciplina.class);
                    criteria.add(Restrictions.eq("eixo", eixo));
                    List<Disciplina> resultado = criteria.list();
                    disciplinas.addAll(resultado);                          
                }
            }
            if (!cursos.isEmpty()) {
                for (String curso : cursos) {
                    Criteria criteria = session.createCriteria(Disciplina.class);
                    criteria.add(Restrictions.eq("curso", curso));
                    List<Disciplina> resultado = criteria.list();
                    disciplinas.addAll(resultado);
//                    Query query = session.createQuery("from Disciplina d where d.curso = :curso ");
//                    query.setParameter("curso", curso);
//                    List resultado = query.list();
//                    disciplinas.addAll(resultado);
                }
            }
            return disciplinas;
        } catch (HibernateException e) {
            return null;
        }
    }
}

