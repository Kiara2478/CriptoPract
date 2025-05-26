package Servlet;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
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
@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> responseData = new HashMap<>();

        try {
            HttpSession session = request.getSession(false);

            if (session != null) {
                // Invalidar la sesión
                session.invalidate();
                responseData.put("success", true);
                responseData.put("mensaje", "Sesión cerrada correctamente");
            } else {
                responseData.put("success", false);
                responseData.put("mensaje", "No hay sesión activa");
            }

        } catch (Exception e) {
            e.printStackTrace();
            responseData.put("success", false);
            responseData.put("mensaje", "Error al cerrar sesión: " + e.getMessage());
        }

        out.print(new Gson().toJson(responseData));
        out.close();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Permitir también GET para el logout
        doPost(request, response);
    }
}
