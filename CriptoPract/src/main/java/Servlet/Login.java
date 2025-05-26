package Servlet;

import dto.Persona;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
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
@WebServlet(name = "Login", urlPatterns = {"/login"})
public class Login extends HttpServlet {

    private EntityManagerFactory emf;

    @Override
    public void init() throws ServletException {
        // Inicialización única del EntityManagerFactory
        emf = Persistence.createEntityManagerFactory("com.mycompany_CriptoPract_war_1.0-SNAPSHOTPU");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        configurarCORS(response);

        try {
            // Leer y validar datos de entrada
            JSONObject requestData = leerJSON(request);
            String usuario = requestData.optString("usuario", "").trim();
            String clave = requestData.optString("clave", "").trim();

            if (usuario.isEmpty() || clave.isEmpty()) {
                enviarError(response, "Usuario y contraseña son requeridos");
                return;
            }

            // Validar hash MD5 (32 caracteres hexadecimales)
            if (!clave.matches("^[a-f0-9]{32}$")) {
                enviarError(response, "Formato de contraseña inválido");
                return;
            }

            // Autenticar usuario
            Persona usuarioEncontrado = autenticarUsuario(usuario, clave.toLowerCase());

            if (usuarioEncontrado != null) {
                // Crear sesión
                crearSesion(request, usuarioEncontrado);

                // Respuesta exitosa
                JSONObject respuesta = new JSONObject()
                        .put("status", "ok")
                        .put("message", "Login exitoso")
                        .put("usuario", usuarioEncontrado.getLogiPers())
                        .put("usuarioId", usuarioEncontrado.getCodiPers())
                        .put("nombre", usuarioEncontrado.getNombPers())
                        .put("redirect", "principal.html");

                enviarRespuesta(response, respuesta);
            } else {
                enviarError(response, "Usuario o contraseña incorrectos");
            }

        } catch (Exception e) {
            enviarError(response, "Error interno del servidor");
        }
    }

    private Persona autenticarUsuario(String usuario, String claveHash) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                    "SELECT p FROM Persona p WHERE p.logiPers = :usuario AND p.passPers = :clave",
                    Persona.class)
                    .setParameter("usuario", usuario)
                    .setParameter("clave", claveHash)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        } finally {
            em.close();
        }
    }

    private void crearSesion(HttpServletRequest request, Persona usuario) {
        HttpSession sesion = request.getSession();
        sesion.setAttribute("usuario", usuario);
        sesion.setAttribute("usuarioId", usuario.getCodiPers());
        sesion.setAttribute("usuarioNombre", usuario.getNombPers());
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
