package Servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dao.PersonaJpaController;
import dto.Persona;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
@WebServlet(name = "RegistroServlet", urlPatterns = {"/registro"})
public class RegistroServlet extends HttpServlet {

    private static EntityManagerFactory emf;
    private PersonaJpaController personaDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        emf = Persistence.createEntityManagerFactory("com.mycompany_CriptoIIP_war_1.0-SNAPSHOTPU");
        personaDAO = new PersonaJpaController(emf);
        gson = new Gson();
    }

    // Registro básico (solo nombre, login, password)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        configurarCORS(response);

        try {
            // Leer datos JSON del request
            JsonObject jsonData = gson.fromJson(request.getReader(), JsonObject.class);

            // Validar datos requeridos
            if (!validarDatosBasicos(jsonData)) {
                enviarError(response, "Faltan datos requeridos: nombre, login y password");
                return;
            }

            String nombre = jsonData.get("nombre").getAsString().trim();
            String login = jsonData.get("login").getAsString().trim();
            String password = jsonData.get("password").getAsString();

            // Verificar si el login ya existe
            if (loginExiste(login)) {
                enviarError(response, "El nombre de usuario ya existe");
                return;
            }

            // Crear nueva persona con datos básicos
            Persona nuevaPersona = new Persona();
            nuevaPersona.setNombPers(nombre);
            nuevaPersona.setLogiPers(login);
            nuevaPersona.setPassPers(hashMD5(password));

            // Guardar en base de datos
            personaDAO.create(nuevaPersona);

            enviarRespuesta(response, "Usuario registrado exitosamente", nuevaPersona.getCodiPers());

        } catch (Exception e) {
            e.printStackTrace();
            enviarError(response, "Error interno del servidor al registrar usuario");
        }
    }

    // Registro completo (todos los datos de persona)
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        configurarCORS(response);

        try {
            JsonObject jsonData = gson.fromJson(request.getReader(), JsonObject.class);

            // Validar datos requeridos para registro completo
            if (!validarDatosCompletos(jsonData)) {
                enviarError(response, "Faltan datos requeridos para registro completo");
                return;
            }

            String nombre = jsonData.get("nombre").getAsString().trim();
            String login = jsonData.get("login").getAsString().trim();
            String password = jsonData.get("password").getAsString();
            String dni = jsonData.get("dni").getAsString().trim();
            String fechaNacimiento = jsonData.get("fechaNacimiento").getAsString();
            double peso = jsonData.get("peso").getAsDouble();

            // Verificar si el login ya existe
            if (loginExiste(login)) {
                enviarError(response, "El nombre de usuario ya existe");
                return;
            }

            // Verificar si el DNI ya existe
            if (dniExiste(dni)) {
                enviarError(response, "El DNI ya está registrado");
                return;
            }

            // Convertir fecha
            Date fechaNaci = convertirFecha(fechaNacimiento);
            if (fechaNaci == null) {
                enviarError(response, "Formato de fecha inválido. Use YYYY-MM-DD");
                return;
            }

            // Crear nueva persona con todos los datos
            Persona nuevaPersona = new Persona();
            nuevaPersona.setNombPers(nombre);
            nuevaPersona.setLogiPers(login);
            nuevaPersona.setPassPers(hashMD5(password));
            nuevaPersona.setNdniPers(dni);
            nuevaPersona.setFechaNaciPers(fechaNaci);
            nuevaPersona.setPesoPers(peso);

            // Guardar en base de datos
            personaDAO.create(nuevaPersona);

            enviarRespuesta(response, "Usuario registrado exitosamente con datos completos",
                    nuevaPersona.getCodiPers());

        } catch (Exception e) {
            e.printStackTrace();
            enviarError(response, "Error interno del servidor al registrar usuario");
        }
    }

    // Manejo de OPTIONS para CORS
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        configurarCORS(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // Validar datos básicos para registro
    private boolean validarDatosBasicos(JsonObject data) {
        return data.has("nombre") && !data.get("nombre").getAsString().trim().isEmpty()
                && data.has("login") && !data.get("login").getAsString().trim().isEmpty()
                && data.has("password") && !data.get("password").getAsString().isEmpty();
    }

    // Validar datos completos para registro
    private boolean validarDatosCompletos(JsonObject data) {
        return validarDatosBasicos(data)
                && data.has("dni") && !data.get("dni").getAsString().trim().isEmpty()
                && data.has("fechaNacimiento") && !data.get("fechaNacimiento").getAsString().isEmpty()
                && data.has("peso") && data.get("peso").getAsDouble() > 0;
    }

    // Verificar si el login ya existe
    private boolean loginExiste(String login) {
        try {
            List<Persona> personas = personaDAO.findPersonaEntities();
            for (Persona p : personas) {
                if (login.equals(p.getLogiPers())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

// Verificar si el DNI ya existe
    private boolean dniExiste(String dni) {
        try {
            List<Persona> personas = personaDAO.findPersonaEntities();
            for (Persona p : personas) {
                if (dni.equals(p.getNdniPers())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // Convertir string de fecha a Date
    private Date convertirFecha(String fechaStr) {
        try {
            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
            formato.setLenient(false);
            return formato.parse(fechaStr);
        } catch (ParseException e) {
            return null;
        }
    }

    // Hash MD5
    private String hashMD5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();

        for (byte b : digest) {
            hexString.append(String.format("%02x", b));
        }

        return hexString.toString();
    }

    // Respuesta JSON exitosa
    private void enviarRespuesta(HttpServletResponse response, String mensaje, Integer userId)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        JsonObject obj = new JsonObject();
        obj.addProperty("status", "success");
        obj.addProperty("message", mensaje);
        if (userId != null) {
            obj.addProperty("userId", userId);
        }
        out.print(obj.toString());
        out.flush();
        out.close();
    }

    // Respuesta de error
    private void enviarError(HttpServletResponse response, String mensaje) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        PrintWriter out = response.getWriter();
        JsonObject obj = new JsonObject();
        obj.addProperty("status", "error");
        obj.addProperty("message", mensaje);
        out.print(obj.toString());
        out.flush();
        out.close();
    }

    // Configuración CORS
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
