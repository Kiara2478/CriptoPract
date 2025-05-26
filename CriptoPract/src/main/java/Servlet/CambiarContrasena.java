package Servlet;

import com.google.gson.Gson;
import dto.Persona;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author ANDREA
 */
@WebServlet(name = "CambiarContrasena", urlPatterns = {"/cambiarcontrasena"})
public class CambiarContrasena extends HttpServlet {
     private EntityManagerFactory emf;

    @Override
    public void init() throws ServletException {
        emf = Persistence.createEntityManagerFactory("com.mycompany_CriptoPract_war_1.0-SNAPSHOTPU");
    }

    @Override
    public void destroy() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    // Método para generar MD5
    private String generateMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generando MD5", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, String> response_data = new HashMap<>();

        String logiUsua = request.getParameter("logiUsua");
        String passUsua = request.getParameter("passUsua");
        String newpass = request.getParameter("newpass");

        try {
            // Validar campos
            if (logiUsua == null || logiUsua.trim().isEmpty()
                    || passUsua == null || passUsua.trim().isEmpty()
                    || newpass == null || newpass.trim().isEmpty()) {
                response_data.put("resultado", "error");
                response_data.put("mensaje", "Todos los campos son obligatorios");
                out.print(new Gson().toJson(response_data));
                return;
            }

            if (newpass.length() < 4) {
                response_data.put("resultado", "error");
                response_data.put("mensaje", "La nueva contraseña debe tener al menos 4 caracteres");
                out.print(new Gson().toJson(response_data));
                return;
            }

            EntityManager em = emf.createEntityManager();
            try {
                // Generar MD5 de la contraseña actual para comparar
                String md5CurrentPass = generateMD5(passUsua);
                
                TypedQuery<Persona> query = em.createQuery(
                        "SELECT p FROM Persona p WHERE p.logiPers = :logi AND p.passPers = :pass",
                        Persona.class
                );
                query.setParameter("logi", logiUsua.trim());
                query.setParameter("pass", md5CurrentPass);

                Persona persona;
                try {
                    persona = query.getSingleResult();
                } catch (NoResultException ex) {
                    response_data.put("resultado", "error");
                    response_data.put("mensaje", "Usuario o contraseña actual incorrectos");
                    out.print(new Gson().toJson(response_data));
                    return;
                }

                // Actualizar contraseña con MD5
                em.getTransaction().begin();
                String newPassMD5 = generateMD5(newpass);
                persona.setPassPers(newPassMD5);
                em.merge(persona);
                em.getTransaction().commit();

                // Invalidar sesión
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }

                response_data.put("resultado", "ok");
                response_data.put("mensaje", "Contraseña cambiada exitosamente. Por favor, inicie sesión nuevamente.");
                out.print(new Gson().toJson(response_data));

            } catch (Exception e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                e.printStackTrace();
                response_data.put("resultado", "error");
                response_data.put("mensaje", "Error interno del servidor: " + e.getMessage());
                out.print(new Gson().toJson(response_data));
            } finally {
                em.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            response_data.put("resultado", "error");
            response_data.put("mensaje", "Error procesando la solicitud");
            out.print(new Gson().toJson(response_data));
        } finally {
            out.close();
        }
    }
}
