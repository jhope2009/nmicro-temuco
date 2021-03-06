
package inf.uct.nmicro.fragments;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import org.osmdroid.util.GEMFFile;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import inf.uct.nmicro.model.Company;
import inf.uct.nmicro.model.Instruction;
import inf.uct.nmicro.model.Point;
import inf.uct.nmicro.model.Route;
import inf.uct.nmicro.model.Stop;
import inf.uct.nmicro.model.Travel;
import inf.uct.nmicro.sqlite.DataBaseHelper;

/**
 * Created by Esteban Campos A on 11-11-2016.
 */
public class SearchTraveling extends Activity {

    private final int POSITION_DIAMETER = 250;
    DrawInMap DrawinMap = new DrawInMap();

    public List<Route> GetRoutebyStartPoint(GeoPoint startPoint, List<Company> companies) {
        List<Route> candidatos = new ArrayList<>();
        for (Company c : companies) {
            for (Route ruta : c.getRoutes()) {
                if (DrawinMap.isRouteInArea(ruta, startPoint)) {
                    candidatos.add(ruta);
                }
            }
        }
        return candidatos;
    }

    public List<Route> GetRoutebyEndpoint(GeoPoint endpoint, List<Company> companies) {
        List<Route> utiles = new ArrayList<>();
        for (Company c : companies) {
            for (Route ruta : c.getRoutes()) {
                if (DrawinMap.isRouteInArea(ruta, endpoint)) {
                    utiles.add(ruta);
                }
            }
        }
        return utiles;
    }

    public List<Stop> GetStops4Travel(List<Route> inicio, List<Route> finales, List<GeoPoint> puntos) {
        List<Stop> paradas = new ArrayList<>();

        for (Route r : inicio) {
            for (Stop st : r.getStops()) {
                for (GeoPoint gp : puntos) {
                    int distance = new GeoPoint(st.getLatitude(), st.getLongitude()).distanceTo(gp);
                    if (distance < POSITION_DIAMETER) {
                        paradas.add(st);

                    }
                }
            }
        }
        for (Route ro : finales) {
            for (Stop st : ro.getStops()) {
                for (GeoPoint gp : puntos) {
                    int distance = new GeoPoint(st.getLatitude(), st.getLongitude()).distanceTo(gp);
                    if (distance < POSITION_DIAMETER) {
                        paradas.add(st);

                    }
                }
            }
        }
        return paradas;
    }

    public List<String> GetAdress4Intermedios(Geocoder geocoder, List<GeoPoint> pinteres) throws IOException {
        List<String> direction4points = new ArrayList<>();
        String direccion = "";
        for (GeoPoint gp : pinteres) {
            List<Address> ub0 = geocoder.getFromLocation(gp.getLatitude(), gp.getLongitude(), 1);
            direccion = ub0.get(0).toString();
            direction4points.add(direccion);
            ub0.clear();
        }
        return direction4points;
    }

//terminar el metodo

    public Travel GetTheTravel(List<Route> r,List<String> direcciones){
                Travel viaje=new Travel();
                List<GeoPoint> intermedio=getIntermedios(r.get(0),r.get(1));
                List<Instruction> instructions=GetInstruccions4Travel(intermedio,r.get(0),r.get(1),direcciones.get(0),direcciones.get(1));
                viaje.setRoutes(r);
                viaje.SetName(r.get(0).getName()+r.get(1).getName());
                viaje.setInstructions(instructions);


        return viaje;

    }
    public List<GeoPoint> getIntermedios(Route Origen,Route Destino){
        List<GeoPoint> intermedios = new ArrayList<>();
        for(Point pt1 :Origen.getPoints()){
                GeoPoint gp =new GeoPoint(pt1.getLatitude(),pt1.getLongitude());
                if(DrawinMap.isRouteInArea(Destino,gp)){
                    intermedios.add(gp);

                }
        }
        return intermedios;
    }



    public List<Travel> GetTravel(List<Company> companies, GeoPoint origen, GeoPoint destino, Geocoder geocoder, String Punto_Origen, String Punto_Destino, DataBaseHelper mydb) {
        List<Route> candidato1 = GetRoutebyStartPoint(origen, companies);
        List<Route> candidato2 = GetRoutebyEndpoint(destino, companies);
        List<Route> finales1 = new ArrayList<>();
        List<Route> finales2 = new ArrayList<>();
        List<GeoPoint> intermedios = new ArrayList<>();
        //busqueda de puntos intermedios
        for (Route ruta_Origen : candidato1) {
            outerloop:
            for (Route ruta_Destino : candidato2) {
                for (Point punto_ruta_Origen : ruta_Origen.getPoints()) {
                    GeoPoint Punto_intermedio = new GeoPoint(punto_ruta_Origen.getLatitude(), punto_ruta_Origen.getLongitude());
                    if (ruta_Origen.getIdRoute() != ruta_Destino.getIdRoute()) {
                        if (DrawinMap.isRouteInArea(ruta_Destino, Punto_intermedio)) {
                            intermedios.add(Punto_intermedio);
                            break outerloop;
                        }
                    }
                }
            }
        }//fin de la busqueda de candidatos
        //inicia la busqueda de rutas que sirven
        for (Route Ruta_Origen : candidato1) {
            for (GeoPoint Punto_Intermedio : intermedios) {
                int posicion_PIntermedio_en_RutaOrigen = DrawinMap.isRouteInArea2(Ruta_Origen, Punto_Intermedio);
                int posicion_POrigen_en_RutaOrigen = DrawinMap.isRouteInArea2(Ruta_Origen, origen);
                if (posicion_POrigen_en_RutaOrigen < posicion_PIntermedio_en_RutaOrigen) {
                    finales1.add(Ruta_Origen);
                }
            }
        }
        for (Route Ruta_destino : candidato2) {
            for (GeoPoint Punto_Intermedio : intermedios) {
                int posicion_PIntermedio_en_RutaDestino = DrawinMap.isRouteInArea2(Ruta_destino, Punto_Intermedio);
                int posicion_PDestino_en_RutaDestino = DrawinMap.isRouteInArea2(Ruta_destino, destino);
                if (posicion_PDestino_en_RutaDestino > posicion_PIntermedio_en_RutaDestino) {
                    finales2.add(Ruta_destino);
                }
            }
        }
        //elimino las rutas que se repiten en los arreglos ademas de eliminar los puntos intermedios que tambien se repiten.
        Set<Route> aux = new HashSet<Route>();
        aux.addAll(finales2);
        finales2.clear();
        finales2.addAll(aux);

        Set<Route> aux2 = new HashSet<Route>();
        aux2.addAll(finales1);
        finales1.clear();
        finales1.addAll(aux2);

        Set<GeoPoint> aux3 = new HashSet<GeoPoint>();
        aux3.addAll(intermedios);
        intermedios.clear();
        intermedios.addAll(aux3);

        List<Travel> viajes = GetFinalTravels(finales2, finales1, intermedios, geocoder, Punto_Origen, Punto_Destino);
        return viajes;

    }//metodo de busqueda del viaje

    public List<Travel> GetFinalTravels(List<Route> Rxorigen, List<Route> Rxdestino, List<GeoPoint> intermedios, Geocoder geocoder, String Punto_Origen, String Punto_Destino) {
        int aux = 0;
        List<Travel> travels = new ArrayList<>();

        List<Address> ub1 = DrawinMap.findLocationByAddress(Punto_Origen + " Temuco, Araucania, Chile", geocoder, getApplication());
        List<Address> ub2 = DrawinMap.findLocationByAddress(Punto_Destino + " Temuco, Araucania, Chile", geocoder, getApplication());

        for (Route r1 : Rxorigen) {
            for (Route r2 : Rxdestino) {
               Stop SOringe=GetStops(new GeoPoint(ub1.get(0).getLatitude(),ub1.get(0).getLongitude()),r1);
               Stop  SFinal= GetStops(new GeoPoint(ub2.get(0).getLatitude(),ub2.get(0).getLongitude()),r2);
                List<Instruction> instruccionesFinales=GetInstruccions4Travel(intermedios,r1,r2,Punto_Origen,Punto_Destino);
                List<Route> rtravel = new ArrayList<>();
                rtravel.add(r2);
                rtravel.add(r1);
                travels.add(new Travel(aux,r2.getName() + " - " + r1.getName(), rtravel, instruccionesFinales,SOringe,SFinal));
                aux++;
            }
        }
        Set<Travel> viajesclean = new HashSet<Travel>();
        viajesclean.addAll(travels);
        travels.clear();
        travels.addAll(viajesclean);

        return travels;
    }

public List<Instruction> GetInstruccions4Travel(List<GeoPoint> intermedios, Route Ruta_Origen, Route RutaDestino, String Punto_Origen, String Punto_Destino){
    List<Instruction> Instructions4Travel=new ArrayList<Instruction>();
    Instructions4Travel.clear();
    List<String> direcciones = new ArrayList<>();
    Instruction bajada1=new Instruction();
    Instruction subida=new Instruction();
    subida.setIndication("Toma la micro en: "+Punto_Origen);
    Instructions4Travel.add(subida);
    int diferencia=0;
    outloop2:
    for(Stop st :RutaDestino.getStops()){
        for(GeoPoint gpI: intermedios) {
            GeoPoint gp = new GeoPoint(st.getLatitude(), st.getLongitude());
            diferencia=gp.distanceTo(gpI);
            if(diferencia<=550){
                bajada1.setIndication("Bajate en: "+st.getAddress());
                Log.i("La parada ",String.valueOf(st.getIdStop())+st.getAddress());
                bajada1.setStop(st);
                Instructions4Travel.add(bajada1);
                break outloop2;
            }
        }
    }
    diferencia=0;
    Instruction bajada2=new Instruction();
    outloop:
    for(Stop st :Ruta_Origen.getStops()){
        for(GeoPoint gp : intermedios){
            diferencia=gp.distanceTo(new GeoPoint(st.getLatitude(),gp.getLongitude()));
            if(diferencia<=150){
                bajada2.setIndication("Toma la micro: "+Ruta_Origen.getName()+" en: "+st.getAddress());
                bajada2.setStop(st);
                Instructions4Travel.add(bajada2);
                break outloop;
            }
        }
    }
    Instruction DestinoFinal=new Instruction();
    DestinoFinal.setIndication("Camina hasta: "+Punto_Destino);
    Instructions4Travel.add(DestinoFinal);
    return Instructions4Travel;
    }

    public Stop GetStops(GeoPoint gp, Route r){
        Stop st1=new Stop();
        for(Stop st : r.getStops()){
            int a=new GeoPoint(st.getLatitude(),st.getLongitude()).distanceTo(gp);
            if(a<=5250){
                st1=st;
                break;
            }
        }
        return st1;
    }






}




