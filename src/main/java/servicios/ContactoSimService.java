package servicios;

import interfaces.InterfazContactoSim;
import modelo.DatosSimulation;
import modelo.DatosSolicitud;
import modelo.Entidad;
import modelo.Punto;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
public class ContactoSimService implements InterfazContactoSim {

    private static final String BASE_URL = "http://servicio:8080";
    private static final String USUARIO = "alumno";

    private final List<Entidad> entidades;

    public ContactoSimService() {
        entidades = new ArrayList<>();

        Entidad e1 = new Entidad();
        e1.setId(0);
        e1.setName("Test1");
        e1.setDescripcion("Nombre ejemplo");

        Entidad e2 = new Entidad();
        e2.setId(1);
        e2.setName("Test2");
        e2.setDescripcion("Nombre ejemplo");

        entidades.add(e1);
        entidades.add(e2);
    }

    @Override
    public int solicitarSimulation(DatosSolicitud sol) {
        try {
            URL url = new URL(BASE_URL + "/Solicitud/Solicitar?nombreUsuario=" + USUARIO);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            Map<Integer, Integer> nums = sol.getNums();

            StringBuilder cantidades = new StringBuilder();
            StringBuilder nombres = new StringBuilder();
            entidades.stream()
                    .sorted(Comparator.comparingInt(Entidad::getId))
                    .forEach(e -> {
                        if (cantidades.length() > 0) cantidades.append(",");
                        if (nombres.length() > 0) nombres.append(",");
                        cantidades.append(nums.getOrDefault(e.getId(), 0));
                        nombres.append("\"").append(e.getName()).append("\"");
                    });

            String json = "{\"cantidadesIniciales\":[" + cantidades + "],"
                    + "\"nombreEntidades\":[" + nombres + "]}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes());
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = br.readLine()) != null) sb.append(linea);
            String respuesta = sb.toString();

            int token = extraerIntDeJson(respuesta, "tokenSolicitud");
            return token;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public DatosSimulation descargarDatos(int ticket) {
        try {
            URL url = new URL(BASE_URL + "/Resultados?nombreUsuario=" + USUARIO + "&tok=" + ticket);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = br.readLine()) != null) sb.append(linea);
            String respuesta = sb.toString();

            System.out.println("RESPUESTA VM: " + respuesta); // <- log
            String data = extraerStringDeJson(respuesta, "data");
            System.out.println("DATA EXTRAIDA: " + data); // <- log

            if (data == null) return new DatosSimulation();

            data = data.replace("\\r\\n", "\n").replace("\\n", "\n").replace("\\r", "\n");

            String[] lineas = data.split("\n");
            int anchoTablero = Integer.parseInt(lineas[0].trim());

            Map<Integer, List<Punto>> puntos = new HashMap<>();
            int maxTiempo = 0;

            for (int i = 1; i < lineas.length; i++) {
                String l = lineas[i].trim();
                if (l.isEmpty()) continue;

                String[] partes = l.split(",");
                if (partes.length < 4) continue;

                int tiempo = Integer.parseInt(partes[0].trim());
                int y      = Integer.parseInt(partes[1].trim());
                int x      = Integer.parseInt(partes[2].trim());
                String color = partes[3].trim();

                Punto p = new Punto();
                p.setX(x);
                p.setY(y);
                p.setColor(color);

                puntos.computeIfAbsent(tiempo, k -> new ArrayList<>()).add(p);
                if (tiempo > maxTiempo) maxTiempo = tiempo;
            }

            DatosSimulation ds = new DatosSimulation();
            ds.setAnchoTablero(anchoTablero);
            ds.setMaxSegundos(maxTiempo + 1);
            ds.setPuntos(puntos);
            return ds;

        } catch (Exception e) {
            e.printStackTrace();
            return new DatosSimulation();
        }
    }

    private int extraerIntDeJson(String json, String clave) {
        String buscar = "\"" + clave + "\":";
        int idx = json.indexOf(buscar);
        if (idx == -1) return -1;
        int inicio = idx + buscar.length();
        int fin = inicio;
        while (fin < json.length() && (Character.isDigit(json.charAt(fin)) || json.charAt(fin) == '-'))
            fin++;
        return Integer.parseInt(json.substring(inicio, fin));
    }

    private String extraerStringDeJson(String json, String clave) {
        String buscar = "\"" + clave + "\":\"";
        int idx = json.indexOf(buscar);
        if (idx == -1) return null;
        int inicio = idx + buscar.length();
        int fin = inicio;
        while (fin < json.length()) {
            if (json.charAt(fin) == '"' && json.charAt(fin - 1) != '\\') break;
            fin++;
        }
        return json.substring(inicio, fin);
    }

    @Override
    public List<Entidad> getEntities() {
        return entidades;
    }

    @Override
    public boolean isValidEntityId(int id) {
        return entidades.stream().anyMatch(e -> e.getId() == id);
    }
}