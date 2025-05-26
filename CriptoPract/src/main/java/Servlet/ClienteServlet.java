/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package Servlet;

import dto.Cliente;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author ANDREA
 */
@WebServlet(name = "ClienteServlet", urlPatterns = {"/clienteservlet"})
public class ClienteServlet extends HttpServlet {
    
    // COLOCAR PERSISTENCIA DEL PROYECTO
    @PersistenceUnit(unitName = "com.mycompany_CriptoPract_war_1.0-SNAPSHOTPU")
    private EntityManagerFactory emf;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            emf = Persistence.createEntityManagerFactory("com.mycompany_CriptoPract_war_1.0-SNAPSHOTPU");
            System.out.println("EntityManagerFactory inicializado correctamente");
        } catch (Exception e) {
            System.err.println("Error al inicializar EntityManagerFactory: " + e.getMessage());
            e.printStackTrace();
            throw new ServletException("No se pudo inicializar la base de datos", e);
        }
    }

    private EntityManager getEntityManager() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("com.mycompany_CriptoPract_war_1.0-SNAPSHOTPU");
        }
        return emf.createEntityManager();
    }

    // Método para listar clientes - CON DEBUGGING
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("=== INICIANDO doGet en ClienteServlet ===");
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        
        // Permitir CORS si es necesario
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        EntityManager em = null;
        PrintWriter out = null;
        
        try {
            out = response.getWriter();
            em = getEntityManager();
            System.out.println("EntityManager obtenido correctamente");

            // Intentar obtener todos los clientes
            List<Cliente> clientes = em.createNamedQuery("Cliente.findAll", Cliente.class).getResultList();
            System.out.println("Número de clientes encontrados: " + clientes.size());

            JSONArray jsonArray = new JSONArray();

            for (Cliente c : clientes) {
                System.out.println("Procesando cliente: " + c.getNombClie());
                JSONObject obj = new JSONObject();
                obj.put("codiClie", c.getCodiClie());
                obj.put("ndniClie", c.getNdniClie() != null ? c.getNdniClie() : "");
                obj.put("nombClie", c.getNombClie() != null ? c.getNombClie() : "");
                obj.put("celuClie", c.getCeluClie() != null ? c.getCeluClie() : "");
                obj.put("direClie", c.getDireClie() != null ? c.getDireClie() : "");
                jsonArray.put(obj);
            }

            String jsonResponse = jsonArray.toString();
            System.out.println("JSON generado: " + jsonResponse);
            
            out.print(jsonResponse);
            out.flush();
            
            System.out.println("Respuesta enviada correctamente");

        } catch (Exception e) {
            System.err.println("ERROR en doGet: " + e.getMessage());
            e.printStackTrace();
            
            // Enviar error en formato JSON
            JSONObject errorObj = new JSONObject();
            errorObj.put("error", true);
            errorObj.put("mensaje", "Error al obtener clientes: " + e.getMessage());
            
            if (out != null) {
                out.print(errorObj.toString());
                out.flush();
            }
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
                System.out.println("EntityManager cerrado");
            }
            System.out.println("=== FINALIZANDO doGet en ClienteServlet ===");
        }
    }

    // Método para crear cliente
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("=== INICIANDO doPost en ClienteServlet ===");
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        EntityManager em = null;
        
        try {
            BufferedReader reader = request.getReader();
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            System.out.println("JSON recibido: " + jsonBuilder.toString());

            JSONObject json = new JSONObject(jsonBuilder.toString());
            Cliente cliente = new Cliente();
            cliente.setCodiClie(json.getInt("codiClie"));
            cliente.setNdniClie(json.getString("ndniClie"));
            cliente.setNombClie(json.getString("nombClie"));
            cliente.setCeluClie(json.optString("celuClie", ""));
            cliente.setDireClie(json.optString("direClie", ""));

            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(cliente);
            em.getTransaction().commit();
            
            System.out.println("Cliente creado exitosamente: " + cliente.getNombClie());
            
            JSONObject responseObj = new JSONObject();
            responseObj.put("success", true);
            responseObj.put("mensaje", "Cliente creado exitosamente");
            
            PrintWriter out = response.getWriter();
            out.print(responseObj.toString());
            out.flush();
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            
        } catch (Exception e) {
            System.err.println("ERROR en doPost: " + e.getMessage());
            e.printStackTrace();
            
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            
            JSONObject errorObj = new JSONObject();
            errorObj.put("error", true);
            errorObj.put("mensaje", "Error al crear cliente: " + e.getMessage());
            
            PrintWriter out = response.getWriter();
            out.print(errorObj.toString());
            out.flush();
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
            System.out.println("=== FINALIZANDO doPost en ClienteServlet ===");
        }
    }

    // Método para actualizar cliente
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("=== INICIANDO doPut en ClienteServlet ===");
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        EntityManager em = null;
        
        try {
            BufferedReader reader = request.getReader();
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JSONObject json = new JSONObject(jsonBuilder.toString());
            int codiClie = json.getInt("codiClie");

            em = getEntityManager();
            em.getTransaction().begin();
            Cliente cliente = em.find(Cliente.class, codiClie);
            
            if (cliente != null) {
                cliente.setNdniClie(json.getString("ndniClie"));
                cliente.setNombClie(json.getString("nombClie"));
                cliente.setCeluClie(json.optString("celuClie", ""));
                cliente.setDireClie(json.optString("direClie", ""));
                em.merge(cliente);
                em.getTransaction().commit();
                
                System.out.println("Cliente actualizado: " + cliente.getNombClie());
                
                JSONObject responseObj = new JSONObject();
                responseObj.put("success", true);
                responseObj.put("mensaje", "Cliente actualizado exitosamente");
                
                PrintWriter out = response.getWriter();
                out.print(responseObj.toString());
                out.flush();
                
            } else {
                JSONObject errorObj = new JSONObject();
                errorObj.put("error", true);
                errorObj.put("mensaje", "Cliente no encontrado");
                
                PrintWriter out = response.getWriter();
                out.print(errorObj.toString());
                out.flush();
                
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
            
        } catch (Exception e) {
            System.err.println("ERROR en doPut: " + e.getMessage());
            e.printStackTrace();
            
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            
            JSONObject errorObj = new JSONObject();
            errorObj.put("error", true);
            errorObj.put("mensaje", "Error al actualizar cliente: " + e.getMessage());
            
            PrintWriter out = response.getWriter();
            out.print(errorObj.toString());
            out.flush();
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
            System.out.println("=== FINALIZANDO doPut en ClienteServlet ===");
        }
    }

    // Método para eliminar cliente
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("=== INICIANDO doDelete en ClienteServlet ===");
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        EntityManager em = null;
        
        try {
            String codiClieParam = request.getParameter("codiClie");
            if (codiClieParam == null || codiClieParam.isEmpty()) {
                throw new IllegalArgumentException("Parámetro codiClie es requerido");
            }
            
            int codiClie = Integer.parseInt(codiClieParam);

            em = getEntityManager();
            em.getTransaction().begin();
            Cliente cliente = em.find(Cliente.class, codiClie);
            
            if (cliente != null) {
                em.remove(cliente);
                em.getTransaction().commit();
                
                System.out.println("Cliente eliminado: " + cliente.getNombClie());
                
                JSONObject responseObj = new JSONObject();
                responseObj.put("success", true);
                responseObj.put("mensaje", "Cliente eliminado exitosamente");
                
                PrintWriter out = response.getWriter();
                out.print(responseObj.toString());
                out.flush();
                
            } else {
                JSONObject errorObj = new JSONObject();
                errorObj.put("error", true);
                errorObj.put("mensaje", "Cliente no encontrado");
                
                PrintWriter out = response.getWriter();
                out.print(errorObj.toString());
                out.flush();
                
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
            
        } catch (Exception e) {
            System.err.println("ERROR en doDelete: " + e.getMessage());
            e.printStackTrace();
            
            if (em != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            
            JSONObject errorObj = new JSONObject();
            errorObj.put("error", true);
            errorObj.put("mensaje", "Error al eliminar cliente: " + e.getMessage());
            
            PrintWriter out = response.getWriter();
            out.print(errorObj.toString());
            out.flush();
            
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
            System.out.println("=== FINALIZANDO doDelete en ClienteServlet ===");
        }
    }
    
    @Override
    public void destroy() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            System.out.println("EntityManagerFactory cerrado en destroy()");
        }
        super.destroy();
    }
}
