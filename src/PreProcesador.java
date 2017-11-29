import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by Joaking on 11/29/2017.
 */
public class PreProcesador {

    public CalculadorFechas calculadorFechas = new CalculadorFechas();

    public PreProcesador(){}

    public ArrayList<String> preProcesamiento(String linea[]){

        ArrayList<String> resultado = new ArrayList<>();
        calculadorFechas = new CalculadorFechas();

        String fecha_horaInicialCompra = linea[2];
        String fecha_horaInicialAprobacion = linea[4];
        String fecha_horaInicialDelivery = linea[11];

        // Fecha de compra
        resultado.add(linea[1]);
        // Fecha y hora de compra
        resultado.add(fecha_horaInicialCompra);
        // Desmenuzo la fecha de compra
        resultado.addAll(desmenuzarFecha(fecha_horaInicialCompra));

        //Fecha de aprobacion
        resultado.add(linea[3]);
        //Fecha y hora de aprobacion
        resultado.add(fecha_horaInicialAprobacion);
        // Desmenuzo la fecha de aprobacion de pago
        resultado.addAll(desmenuzarFecha(fecha_horaInicialAprobacion));

        //Fecha de delivery
        resultado.add(linea[10]);
        //Fecha y hora de delivery
        resultado.add(fecha_horaInicialDelivery);
        //Desmenuzo la fecha de delivery
        resultado.addAll(desmenuzarFecha(fecha_horaInicialDelivery));

        // Diferencia horas habiles entre compra y aprobaccion
        long dif = calculadorFechas.diferenciaDeDias_Horas(fecha_horaInicialCompra,fecha_horaInicialAprobacion);
        resultado.add(String.valueOf(dif));
        // Diferencia dias habiles entre compra y aprobacion
        resultado.add(String.valueOf(TimeUnit.DAYS.convert(dif, TimeUnit.HOURS)));

        // Diferencia horas habiles entre aprobacion y delivery
        dif = calculadorFechas.diferenciaDeDias_Horas(fecha_horaInicialAprobacion,fecha_horaInicialDelivery);
        resultado.add(String.valueOf(dif));
        // Diferencia dias habiles entre aprobacion y delivery
        resultado.add(String.valueOf(TimeUnit.DAYS.convert(dif, TimeUnit.HOURS)));
        return resultado;
    }

    public String discretizacionHoraDia(int hora){
        if (0<hora && hora<8)
            return "madrugada";
        else
        if (hora<14)
            return "mediodia";
        else
        if (hora<18)
            return "tarde";
        else
            return "noche";
    }

    public ArrayList<String> desmenuzarFecha(String fecha){
        ArrayList<String> resultado = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        //Fecha y hora ej: 2017-07-03 11:08:26.0
        SimpleDateFormat formatoFechaHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

        // Desmenuzo la fecha de compra
        try {
            c.setTime(formatoFechaHora.parse(fecha));
            resultado.add(String.valueOf( c.get(Calendar.MONTH) + 1));
            resultado.add(String.valueOf( c.get(Calendar.DAY_OF_MONTH) ));
            resultado.add(String.valueOf( c.get(Calendar.DAY_OF_WEEK) ));

            int hora = c.get(Calendar.HOUR_OF_DAY);
            resultado.add(String.valueOf(hora));
            resultado.add(discretizacionHoraDia(hora));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return resultado;
    }
}
