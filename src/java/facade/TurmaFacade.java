package facade;

import controller.HibernateUtil;
import java.util.List;
import javax.ejb.Stateless;
import model.Disciplina;
import model.Turma;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author Juliana
 */
@Stateless
public class TurmaFacade extends AbstractFacade<Turma> {

    public TurmaFacade() {
        super(Turma.class);
    }

    @Override
    protected SessionFactory getSessionFactory() {
        return HibernateUtil.getSessionFactory();
    }

    //Método para buscar turmas pelo id do docente
    public Turma listByID(Long id) {
        Session session = getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(Turma.class);
        criteria.add(Restrictions.eq("ID", id));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        List<Turma> results = criteria.list();
        session.close();

        return results.get(0);
    }

    //Método para filtrar as turmas ofertadas
    public List<Turma> filtrarTurmas(List<Disciplina> disciplinas, String campus, String turno) {
        try {
            Session session = getSessionFactory().openSession();
            Criteria criteria = session.createCriteria(Turma.class);
            if (!disciplinas.isEmpty()) {
                criteria.add(Restrictions.in("disciplina", disciplinas));
            }
            if (!campus.equals("")) {
                criteria.add(Restrictions.eq("campus", campus));
            }
            if (!turno.equals("")) {
                criteria.add(Restrictions.eq("turno", turno));
            }
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

            List result = criteria.list();
            session.close();
            return result;
        } catch (HibernateException e) {
            return null;
        }
    }
}
