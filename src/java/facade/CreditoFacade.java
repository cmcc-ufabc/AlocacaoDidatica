package facade;

import controller.HibernateUtil;
import java.util.List;
import javax.ejb.Stateless;
import model.Credito;
import model.Disponibilidade;
import model.Docente;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

@Stateless
public class CreditoFacade extends AbstractFacade<Credito>{
    
    public CreditoFacade() {
        super(Credito.class);
    }

    @Override
    protected SessionFactory getSessionFactory() {
        return HibernateUtil.getSessionFactory();
    }
    
    //Retorna a quantidade de créditos do docente no quadrimestre passado como parâmetro
    public Credito creditoQuadrimestre(Docente docente, int quadrimestre){
        try {
            Session session = getSessionFactory().openSession();
            Criteria criteria = session.createCriteria(Credito.class);
            criteria.add(Restrictions.eq("docente", docente));
            criteria.add(Restrictions.eq("quadrimestre", quadrimestre));
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            List resultado = criteria.list();
            session.close();
            
            if(!resultado.isEmpty()){
                return (Credito) resultado.get(0);
            }
            else{
                return null;
            }
        } catch (HibernateException e) {
            return null;
        }   
    } 
}


