/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sv.edu.ues.occ.ingenieria.tpi135.pupasv.core.boundary.rest.server;


import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;


/**
 *
 * @author HL21029

 */

public class RestResourceHeaderPattern{

    public final static String TOTAL_REGISTROS="total-refistros";
    public final static String DETALLE_ERROR="detalle-error";
    public final static int STATUS_PARAMETRO_FALTANTE=422;
    public final static String DETALLE_PARAMETRO_FALTANTE="parametro-faltante";
    public final static int STATUS_PARAMETRO_EQUIVOCADO =400;
    public final static String DETALLE_PARAMETRO_EQUIVOCADO="parametro-equivocado";
    
}
