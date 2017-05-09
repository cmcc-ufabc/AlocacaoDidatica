package facade;

import controller.HibernateUtil;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import model.Docente;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

@Stateless
public class DocenteFacade extends AbstractFacade<Docente>{
    
    public DocenteFacade() {
        super(Docente.class);
    }

    @Override
    protected SessionFactory getSessionFactory() {
        return HibernateUtil.getSessionFactory();
    }
    
    /**
     * Busca um docente pelo nome
     * @param nome string
     * @return Lista de docentes com aquele nome
     */
    public List<Docente> findByName(String nome){
        Session session = getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(Docente.class);
        criteria.add(Restrictions.eq("nome", nome));
        
        List results = criteria.list();
        session.close();
        
        return results;        
    }
    
    public Docente buscaDocente(String nome){
        Session session = getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(Docente.class);
        criteria.add(Restrictions.eq("nome", nome));
        
        List<Docente> results = criteria.list();
        session.close();
        
        return results.get(0);        
    }
    
    /**
     * Busca docentes de acordo com centros ou áreas de atuação escolhidos
     * @param centros lista de string
     * @param areas lista de string
     * @return lista de docentes de acordo com os critérios fornecidos
     */
    public List<Docente> findByCentroArea(List<String> centros, List<String> areas) {
        List<Docente> docentes = new ArrayList<>();
        try {
            Session session = getSessionFactory().openSession();
            Criteria criteria = session.createCriteria(Docente.class);
            if (!centros.isEmpty()) {
                if (!areas.isEmpty()) {
                    for (String centro : centros) {
                        for (String area : areas) {
                            criteria.add(Restrictions.eq("centro", centro));
                            criteria.add(Restrictions.eq("areaAtuacao", area));
                            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
                            List resultado = criteria.list();
                            docentes.addAll(resultado);
                        }

                    }
                } else {
                    for (String centro : centros) {

                        criteria.add(Restrictions.eq("centro", centro));
                        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
                        List resultado = criteria.list();
                        docentes.addAll(resultado);
                    }
                }
            } else {
                if (!areas.isEmpty()) {

                    for (String area : areas) {
                        criteria.add(Restrictions.eq("areaAtuacao", area));
                        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
                        List resultado = criteria.list();
                        docentes.addAll(resultado);
                    }
                }
            }
            return docentes;
        } catch (HibernateException e) {
            return null;
        }
    }
    
    /**
     * Busca docentes de acordo com as áreas de atuação informadas
     * @param areas lista de string
     * @return lista de docentes de acordo com os critérios especificados
     */
    public List<Docente> findByArea(List<String> areas) {
        List<Docente> docentes = new ArrayList<>();
        try {
            Session session = getSessionFactory().openSession();
            Criteria criteria = session.createCriteria(Docente.class);
                for (String area : areas) {
                    criteria.add(Restrictions.eq("areaAtuacao", area));
                    criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
                    List resultado = criteria.list();
                    docentes.addAll(resultado);
                }
            return docentes;
        } catch (HibernateException e) {
            return null;
        }
    }
}
