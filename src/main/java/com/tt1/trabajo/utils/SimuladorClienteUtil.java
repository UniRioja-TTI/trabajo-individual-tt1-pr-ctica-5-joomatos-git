package com.tt1.trabajo.utils;

import interfaces.InterfazContactoSim;
import modelo.DatosSolicitud;

import java.util.Map;

public class SimuladorClienteUtil {
    private final InterfazContactoSim servicio;

    public SimuladorClienteUtil(InterfazContactoSim servicio) {
        this.servicio = servicio;
    }

    public void ejecutarPruebaRapida() {
        DatosSolicitud ds = new DatosSolicitud(Map.of(0, 10, 1, 5));
        int token = servicio.solicitarSimulation(ds);
        System.out.println("Utilidad: Petición enviada. Token recibido: " + token);
    }
}