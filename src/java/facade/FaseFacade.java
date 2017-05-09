package facade;

import controller.HibernateUtil;
import java.util.List;
import javax.ejb.Stateless;
import model.Fase;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author erick
 */
@Stateless
public class FaseFacade extends AbstractFacade<Fase> {

    public FaseFacade() {
        super(Fase.class);
    }

    @Override
    protected SessionFactory getSessionFactory() {
        return HibernateUtil.getSessionFactory();
    }

    //Acha o último registro da tabela fase
    public Fase achaMax() {
        try {
            Session session = getSessionFactory().openSession();
            Fase result = (Fase) session.createCriteria(Fase.class)
                    .addOrder(Order.desc("id")).setMaxResults(1).uniqueResult();
            return result;
        } catch (HibernateException e) {
            return null;
        }
    }
}
