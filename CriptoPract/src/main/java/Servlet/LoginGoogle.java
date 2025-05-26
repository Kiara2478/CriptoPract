package Servlet;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import dto.Persona;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONObject;

/**
 *
 * @author ANDREA
 */
@WebServlet(name = "LoginGoogle", urlPatterns = {"/logingoogle"})
public class LoginGoogle extends HttpServlet {

    private static final String CLIENT_ID = "683576227359-a17m87huqbg468fu1tlknkcnru125fl6.apps.googleusercontent.com";
    private EntityManagerFactory emf;
    private GoogleIdTokenVerifier verifier;

    @Override
    public void init() throws ServletException {
        emf = Persistence.createEntityManagerFactory("com.mycompany_CriptoIIP_war_1.0-SNAPSHOTPU");

        verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory())
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        configurarCORS(response);

        try {
            // Leer token de Google
            JSONObject requestData = leerJSON(request);
            String idToken = requestData.optString("id_token", "").trim();

            if (idToken.isEmpty()) {
                enviarError(response, "Token de Google requerido");
                return;
            }

            // Verificar token
            GoogleIdToken token = verifier.verify(idToken);

            if (token == null) {
                enviarError(response, "Token de Google inválido");
                return;
            }

            // Obtener información del usuario
            GoogleIdToken.Payload payload = token.getPayload();
            String email = payload.getEmail();
            String nombre = (String) payload.get("name");

            if (email == null || email.trim().isEmpty()) {
                enviarError(response, "No se pudo obtener el email de Google");
                return;
            }

            // Buscar o crear usuario
            Persona usuario = buscarOCrearUsuario(email, nombre);

            if (usuario != null) {
                crearSesion(request, usuario, "google");

                JSONObject respuesta = new JSONObject()
                        .put("status", "ok")
                        .put("message", "Login con Google exitoso")
                        .put("usuario", usuario.getLogiPers())
                        .put("usuarioId", usuario.getCodiPers())
                        .put("nombre", usuario.getNombPers())
                        .put("redirect", "principal.html");

                enviarRespuesta(response, respuesta);
            } else {
                enviarError(response, "Error al procesar usuario de Google");
            }

        } catch (Exception e) {
            enviarError(response, "Error al procesar login de Google");
        }
    }

    private Persona buscarOCrearUsuario(String email, String nombre) {
        EntityManager em = emf.createEntityManager();
        try {
            // Buscar usuario existente
            Persona usuario = em.createQuery(
                    "SELECT p FROM Persona p WHERE p.logiPers = :email", Persona.class)
                    .setParameter("email", email)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            // Si no existe, crear uno nuevo
            if (usuario == null) {
                em.getTransaction().begin();
                usuario = new Persona();
                usuario.setLogiPers(email);
                usuario.setNombPers(nombre != null ? nombre : email);
                usuario.setPassPers(""); // Sin contraseña para usuarios de Google
                em.persist(usuario);
                em.getTransaction().commit();
            }

            return usuario;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return null;
        } finally {
            em.close();
        }
    }

    private void crearSesion(HttpServletRequest request, Persona usuario, String loginType) {
        HttpSession sesion = request.getSession();
        sesion.setAttribute("usuario", usuario);
        sesion.setAttribute("usuarioId", usuario.getCodiPers());
        sesion.setAttribute("usuarioNombre", usuario.getNombPers());
        sesion.setAttribute("loginType", loginType);
    }

    // Métodos auxiliares comunes
    private void configurarCORS(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    private JSONObject leerJSON(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return new JSONObject(sb.toString());
    }

    private void enviarRespuesta(HttpServletResponse response, JSONObject json) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.print(json.toString());
        }
    }

    private void enviarError(HttpServletResponse response, String mensaje) throws IOException {
        JSONObject error = new JSONObject()
                .put("status", "error")
                .put("message", mensaje);
        enviarRespuesta(response, error);
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        configurarCORS(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    public void destroy() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
