/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package Servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

/**
 *
 * @author kiara
 */
@WebServlet(name = "reportePDF", urlPatterns = {"/reportePDF"})
public class reportePDF extends HttpServlet {
    private EntityManagerFactory emf;
    
   /*
    try {
        

        // 1. Conectar a la base de datos
       // Class.forName("com.mysql.cj.jdbc.Driver");
       // conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/crippractica", "root", ""); // Ajusta tu user y pass

       
       
        // 2. Obtener la ruta del .jasper
        String rutaJasper = getServletContext().getRealPath("/WEB-INF/reportes/reporte.jasper");

        // 3. Parámetros opcionales (puedes poner null si no usas)
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("titulo", "Reporte generado desde Java Web");

        // 4. Generar el reporte
        JasperPrint print = JasperFillManager.fillReport(rutaJasper, parametros, conn);

        // 5. Enviar el PDF como respuesta
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=reporte.pdf");

        OutputStream out = response.getOutputStream();
        JasperExportManager.exportReportToPdfStream(print, out);
        out.flush();
        out.close();

    } catch (Exception e) {
        e.printStackTrace();
        response.setContentType("text/plain");
       
        
        // Imprimir causa exacta
    Throwable cause = e.getCause();
    if (cause != null) {
        response.getWriter().println("Error generando el reporte: " + cause.getMessage());
    } else {
        response.getWriter().println("Error generando el reporte: " + e.getMessage());
    }
    } finally {
        if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
    }
}


}*/
    @Override
    public void init() throws ServletException {
        emf = Persistence.createEntityManagerFactory("com.mycompany_CriptoPract_war_1.0-SNAPSHOTPU");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        EntityManager em = null;
        Connection conn = null;

        try {
            em = emf.createEntityManager();
            conn = em.unwrap(Connection.class);  // ✅ Obtener conexión desde JPA

            String rutaJasper = getServletContext().getRealPath("/WEB-INF/reportes/reporte.jasper");

            Map<String, Object> parametros = new HashMap<>();
            parametros.put("titulo", "Reporte generado desde Java Web");

            JasperPrint print = JasperFillManager.fillReport(rutaJasper, parametros, conn);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=reporte.pdf");

            OutputStream out = response.getOutputStream();
            JasperExportManager.exportReportToPdfStream(print, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("text/plain");

            Throwable cause = e.getCause();
            if (cause != null) {
                response.getWriter().println("Error generando el reporte: " + cause.getMessage());
            } else {
                response.getWriter().println("Error generando el reporte: " + e.getMessage());
            }
        } finally {
            if (em != null && em.isOpen()) em.close();  // ✅ Cerrar EM
        }
    }

    @Override
    public void destroy() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}








