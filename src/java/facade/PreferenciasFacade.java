/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facade;

import controller.HibernateUtil;
import java.util.List;
import javax.ejb.Stateless;
import model.Preferencias;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author erick
 */

@Stateless
public class PreferenciasFacade extends AbstractFacade<Preferencias> {

    public PreferenciasFacade() {
        super(Preferencias.class);
    }

    //Verifica se já existe algum registro feito pelo docente no quadrimestre
    public Preferencias verificaPreferencia(Long pessoa_id, int quad){
        Session session = getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(Preferencias.class);
        criteria.add(Restrictions.eq("pessoa_id", pessoa_id));
        criteria.add(Restrictions.eq("quadrimestre", quad));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        
        List<Preferencias> results = criteria.list();
        session.close();

        if(results.size()>0){
            return results.get(0);
        } else{
            return null;
        }
    }
    
    @Override
    protected SessionFactory getSessionFactory() {
        return HibernateUtil.getSessionFactory();
    }
}
