package Servlet;

import com.google.gson.Gson;
import dto.Persona;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
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
@WebServlet(name = "SessionInfoServlet", urlPatterns = {"/sessioninfoservlet"})
public class SessionInfoServlet extends HttpServlet {
    
    private EntityManagerFactory emf;

    @Override
    public void init() throws ServletException {
        emf = Persistence.createEntityManagerFactory("com.mycompany_CriptoPract_war_1.0-SNAPSHOTPU");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        EntityManager em = null;
        Map<String, Object> sessionData = new HashMap<>();

        try {
            HttpSession session = request.getSession(false);

            if (session == null) {
                sessionData.put("error", true);
                sessionData.put("mensaje", "No hay sesión activa");
                out.print(new Gson().toJson(sessionData));
                return;
            }

            Integer usuarioId = (Integer) session.getAttribute("usuarioId");
            String usuarioNombre = (String) session.getAttribute("usuarioNombre");
            String usuarioLogin = (String) session.getAttribute("usuarioLogin");

            if (usuarioId == null) {
                sessionData.put("error", true);
                sessionData.put("mensaje", "Sesión inválida");
                out.print(new Gson().toJson(sessionData));
                return;
            }

            em = emf.createEntityManager();

            // Obtener persona por ID usando JPA
            Persona persona = em.find(Persona.class, usuarioId);

            if (persona == null) {
                sessionData.put("error", true);
                sessionData.put("mensaje", "Usuario no encontrado en la base de datos");
            } else {
                sessionData.put("error", false);
                sessionData.put("usuarioId", persona.getCodiPers());
                sessionData.put("usuarioNombre", persona.getNombPers());
                sessionData.put("usuarioLogin", persona.getLogiPers());
                sessionData.put("nombUsua", persona.getNombPers());

                sessionData.put("ndniPers", persona.getNdniPers());
                sessionData.put("fechaNaciPers", persona.getFechaNaciPers() != null ? persona.getFechaNaciPers().toString() : null);
                sessionData.put("pesoPers", persona.getPesoPers());

                // Calcular edad si hay fecha de nacimiento
                if (persona.getFechaNaciPers() != null) {
                    java.util.Date hoy = new java.util.Date();
                    long diff = hoy.getTime() - persona.getFechaNaciPers().getTime();
                    int edad = (int) (diff / (365.25 * 24 * 60 * 60 * 1000));
                    sessionData.put("edad", edad);
                }

                sessionData.put("nombRol", "Usuario"); // Puedes reemplazar si tienes roles
                sessionData.put("nombEmpr", "Mi Empresa"); // Puedes reemplazar si hay empresa
            }

            out.print(new Gson().toJson(sessionData));

        } catch (Exception e) {
            e.printStackTrace();
            sessionData.put("error", true);
            sessionData.put("mensaje", "Error interno del servidor: " + e.getMessage());
            out.print(new Gson().toJson(sessionData));
        } finally {
            if (em != null) {
                em.close();
            }
            out.close();
        }
    }
}
