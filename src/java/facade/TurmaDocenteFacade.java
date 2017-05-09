package facade;

import controller.HibernateUtil;
import java.util.List;
import javax.ejb.Stateless;
import model.TurmaDocente;
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
public class TurmaDocenteFacade extends AbstractFacade<TurmaDocente>{
    
    public TurmaDocenteFacade(){
        super(TurmaDocente.class);
    }
    
    @Override
    protected SessionFactory getSessionFactory() {
        return HibernateUtil.getSessionFactory();
    }
    
    //Lista turmas do Docente
    public List<TurmaDocente> listTurmas(Long id){ 
        Session session = getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(TurmaDocente.class);
        criteria.add(Restrictions.eq("idDocente", id));
        //criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        List results = criteria.list();
        session.close();

        return results;      
    }
    
    //Busca a turma selecionada
    public TurmaDocente TurmaSelecionada(Long turma, Long docente){
        Session session = getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(TurmaDocente.class);
        criteria.add(Restrictions.eq("idTurma", turma));
        criteria.add(Restrictions.eq("idDocente", docente));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        
        List<TurmaDocente> results = criteria.list();
        session.close();

        return results.get(0);
    }
    
    //Contagem de Solicitações da Turma selecionada
    public int totalSolicitacoes(Long id){ 
        Session session = getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(TurmaDocente.class);
        criteria.add(Restrictions.eq("idTurma", id));

        List result = criteria.list();
        session.close();

        return result.size();
    }    
}
