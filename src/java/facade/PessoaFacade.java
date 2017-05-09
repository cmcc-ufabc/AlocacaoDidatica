package facade;

import controller.HibernateUtil;
import java.util.List;
import javax.ejb.Stateless;
import model.Pessoa;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

@Stateless
public class PessoaFacade extends AbstractFacade<Pessoa> {

    public PessoaFacade() {
        super(Pessoa.class);
    }

    @Override
    protected SessionFactory getSessionFactory() {
        return HibernateUtil.getSessionFactory();
    }

    /**
     * Lozaliza uma pessoa de acordo com o nome
     *
     * @param nome
     * @return objeto Pessoa
     */
    public List<Pessoa> findByName(String nome) {
        Session session = getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(Pessoa.class);
        criteria.add(Restrictions.eq("nome", nome));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        List results = criteria.list();
        session.close();

        return results;
    }

    /**
     * Localiza uma pessoa de acordo com o seu nome de usuário
     *
     * @param username
     * @return Um objeto Pessoa
     */
    public Pessoa findByUsername(String username) {
        try {
            username = username + "@%";
            Session session = getSessionFactory().openSession();
            Query query = session.createQuery("from Pessoa p where p.email like :email ");
            query.setParameter("email", username);
            List resultado = query.list();

            if (resultado.size() == 1) {
                Pessoa userFound = (Pessoa) resultado.get(0);
                return userFound;
            } else {
                return null;
            }
        } catch (HibernateException e) {
            return null;
        }
    }

    /**
     * Busca os usuarios administradores do sistema
     *
     * @return Lista de usuarios administradores
     */
    public List<Pessoa> listAdms() {
        Session session = getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(Pessoa.class);
        criteria.add(Restrictions.eq("adm", true));

        List results = criteria.list();
        session.close();

        return results;
    }

    /**
     * Busca todas as pessoas que são docentes
     *
     * @return Lista de docentes
     */
    public List<Pessoa> listDocentes() {
        Session session = getSessionFactory().openSession();
        Criteria criteria = session.createCriteria(Pessoa.class);
        criteria.add(Restrictions.eq("class", "Docente"));
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        List results = criteria.list();
        session.close();

        return results;
    }
}
