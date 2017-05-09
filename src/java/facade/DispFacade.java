package facade;

import controller.HibernateUtil;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import model.Disciplina;
import model.Disp;
import model.Pessoa;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author erick
 */
@Stateless
public class DispFacade extends AbstractFacade<Disp>{
    
    public DispFacade(){
        super(Disp.class);
    }
    
    @Override
    protected SessionFactory getSessionFactory() {
        return HibernateUtil.getSessionFactory();
    }
    
    /**
     * Retorna uma lista de disponibilidades de acordo com o docente pesquisado
     *
     * @param docente objeto da classe pessoa
     * @return Lista com as disponibilidades pesquisadas de acordo com o
     * parametro informado
     */
    public List<Disp> findByDocente(Pessoa docente) {
        try {
            Session session = getSessionFactory().openSession();
            Criteria criteria = session.createCriteria(Disp.class);
            criteria.add(Restrictions.eq("pessoa", docente));
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            List resultado = criteria.list();

            session.close();
            return resultado;
        } catch (HibernateException e) {
            return null;
        }
    }

    /**
     * Retorna uma lista de disponibilidade de acordo com o docente e o
     * quadrimestre buscados
     *
     * @param docente objeto da classe Pessoa
     * @param quad inteiro representando o quadrimestre
     * @return
     */
    public List<Disp> findByDocenteQuad(Pessoa docente, int quad) {
        try {
            Session session = getSessionFactory().openSession();
            Criteria criteria = session.createCriteria(Disp.class);
            criteria.add(Restrictions.eq("pessoa", docente));
            if (quad != 0) {
                criteria.createAlias("ofertaDisciplina", "t").add(Restrictions.eq("t.quadrimestre", quad));
            }
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            List resultado = criteria.list();

            session.close();
            return resultado;
        } catch (HibernateException e) {
            return null;
        }
    }

    /**
     * Retorna uma lista de disponibilidade de acordo com a área de atuação do
     * docente e o quadrimestre buscados
     *
     * @param areasAtuacao lista de String com todos filtros escolhidos
     * @param quad inteiro representando o quadrimestre
     * @return
     */
    public List<Disp> findByAreaQuad(List<String> areasAtuacao, int quad) {
        try {
            Session session = getSessionFactory().openSession();
            Criteria criteria = session.createCriteria(Disp.class);
            List<Disp> disponibilidades = new ArrayList<>();
            if (quad != 0) {
                if (areasAtuacao != null) {
                    for (String a : areasAtuacao) {
                        criteria.createAlias("ofertaDisciplina", "o").add(Restrictions.eq("o.quadrimestre", quad));
                        criteria.createAlias("pessoa", "p").add(Restrictions.eq("p.areaAtuacao", a));
                        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
                        List resultado = criteria.list();
                        disponibilidades.addAll(resultado);
                        criteria = session.createCriteria(Disp.class);
                    }
                } else {
                    criteria.createAlias("ofertaDisciplina", "o").add(Restrictions.eq("o.quadrimestre", quad));
                    List resultado = criteria.list();
                    disponibilidades.addAll(resultado);
//                   criteria = session.createCriteria(Disponibilidade.class);
                }
            }
            session.close();
            return disponibilidades;
        } catch (HibernateException e) {
            return null;
        }
    }

    /**
     * Retorna a lista de disponibilidade de acordo com a disciplina pesquisada
     *
     * @param disciplina
     * @param campus string opcional
     * @param turno string opcional
     * @param quadrimestre int opcional
     * @return
     */
    public List<Disp> findByDiscTurCamQuad(Disciplina disciplina,
            String campus, String turno, int quadrimestre) {
        try {
            Session session = getSessionFactory().openSession();
            Criteria crit = session.createCriteria(Disp.class);
            if (disciplina != null) {
                if (quadrimestre != 0) {
                    crit.add(Restrictions.eq("quadrimestre", quadrimestre));
                }
                crit.createAlias("turma", "t").add(Restrictions.eq("t.disciplina", disciplina));
                if (!campus.equals("")) {
                    crit.add(Restrictions.eq("t.campus", campus));
                }
                if (!turno.equals("")) {
                    crit.add(Restrictions.eq("t.turno", turno));
                }
            } else {
                if (quadrimestre != 0) {
                    crit.add(Restrictions.eq("quadrimestre", quadrimestre));
                }
                crit.createAlias("turma", "t");
                if (!campus.equals("")) {
                    crit.add(Restrictions.eq("t.campus", campus));
                }
                if (!turno.equals("")) {
                    crit.add(Restrictions.eq("t.turno", turno));
                }
            }
            crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            List result = crit.list();
            return result;
        } catch (HibernateException e) {
            return null;
        }
    }
}
