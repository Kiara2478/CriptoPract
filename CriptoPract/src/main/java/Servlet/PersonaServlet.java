package Servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dao.PersonaJpaController;
import java.io.IOException;
import dto.Persona;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 *
 * @author ANDREA
 */
@WebServlet(name = "PersonaServlet", urlPatterns = {"/persona"})
public class PersonaServlet extends HttpServlet {
    
    private static EntityManagerFactory emf;
    private PersonaJpaController personaDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        emf = Persistence.createEntityManagerFactory("com.mycompany_CriptoPract_war_1.0-SNAPSHOTPU");
        personaDAO = new PersonaJpaController(emf);
        gson = new Gson();
    }

    // Obtener todas las personas
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        configurarCORS(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        JsonObject jsonObject = new JsonObject();

        try {
            List<Persona> personas = personaDAO.findPersonaEntities();
            jsonObject.add("data", gson.toJsonTree(personas));
            out.print(jsonObject.toString());
        } catch (Exception e) {
            jsonObject.addProperty("error", "Error al listar personas.");
            out.print(jsonObject.toString());
        } finally {
            out.flush();
            out.close();
        }
    }

    // Crear una nueva persona
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        configurarCORS(response);
        Persona persona = gson.fromJson(request.getReader(), Persona.class);

        try {
            // Hash MD5 antes de guardar
            persona.setPassPers(hashMD5(persona.getPassPers()));
            personaDAO.create(persona);
            enviarRespuesta(response, "Persona creada correctamente");
        } catch (Exception e) {
            enviarError(response, "Error al crear persona");
        }
    }

    // Actualizar persona existente
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        configurarCORS(response);
        Persona persona = gson.fromJson(request.getReader(), Persona.class);

        try {
            // Reaplicar MD5 solo si la clave no está ya en formato hash
            if (!persona.getPassPers().matches("^[a-fA-F0-9]{32}$")) {
                persona.setPassPers(hashMD5(persona.getPassPers()));
            }
            personaDAO.edit(persona);
            enviarRespuesta(response, "Persona actualizada correctamente");
        } catch (Exception e) {
            enviarError(response, "Error al actualizar persona");
        }
    }

    // Eliminar persona
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        configurarCORS(response);

        String idParam = request.getParameter("id");
        if (idParam == null) {
            enviarError(response, "Se requiere el parámetro ID");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            personaDAO.destroy(id);
            enviarRespuesta(response, "Persona eliminada correctamente");
        } catch (Exception e) {
            enviarError(response, "Error al eliminar persona");
        }
    }

    // Manejo de OPTIONS para CORS
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        configurarCORS(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // Método utilitario: Hash MD5
    private String hashMD5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();

        for (byte b : digest) {
            hexString.append(String.format("%02x", b));
        }

        return hexString.toString();
    }

    // Respuesta JSON genérica
    private void enviarRespuesta(HttpServletResponse response, String mensaje) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject obj = new JsonObject();
        obj.addProperty("status", "ok");
        obj.addProperty("message", mensaje);
        out.print(obj.toString());
        out.flush();
        out.close();
    }

    // Respuesta de error
    private void enviarError(HttpServletResponse response, String mensaje) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject obj = new JsonObject();
        obj.addProperty("status", "error");
        obj.addProperty("message", mensaje);
        out.print(obj.toString());
        out.flush();
        out.close();
    }

    // CORS
    private void configurarCORS(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setCharacterEncoding("UTF-8");
    }

    @Override
    public void destroy() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
