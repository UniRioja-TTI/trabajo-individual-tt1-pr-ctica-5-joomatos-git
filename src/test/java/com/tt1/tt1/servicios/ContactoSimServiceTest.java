package com.tt1.tt1.servicios;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import modelo.DatosSolicitud;
import servicios.ContactoSimService;

import java.util.HashMap;

class ContactoSimServiceTest {

    private final ContactoSimService service = new ContactoSimService();

    @Test
    void testSolicitarSimulation() {
        DatosSolicitud sol = new DatosSolicitud(new HashMap<>());
        int result = service.solicitarSimulation(sol);

        assertNotEquals(0, result, "El token no debería ser 0");
    }

    @Test
    void testGetEntitiesNoEsNulo() {
        assertNotNull(service.getEntities());
    }
}