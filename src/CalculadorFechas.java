import org.apache.commons.lang3.time.DateUtils;
import sun.util.calendar.LocalGregorianCalendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class CalculadorFechas {

    private Calendar calendar = Calendar.getInstance();
    private HashMap<String, String> feriados = new HashMap<>();

    //Fecha y hora ej: 2017-07-03 11:08:26.0
    private SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");

    //Fecha y hora ej: 2017-07-03 11:08:26.0
    private SimpleDateFormat formatoFechaHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

    public CalculadorFechas(){
    };

    public long getMes(String fecha_completa){
        try {
            Date fecha = formatoFecha.parse(fecha_completa);
            calendar.setTime(fecha);
            //Los meses arrancan en 0. Osea, enero = 0;
            return calendar.get(Calendar.MONTH+1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -2;
    }


    //Diferencia de dias entre dos fechas
    public long diferenciaDeDias(String str_fechaInicial, String str_fechaFinal){
        try {
            Date fechaInicial = formatoFecha.parse(str_fechaInicial);
            Date fechaFinal = formatoFecha.parse(str_fechaFinal);

            return -2;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -2;
    }

    //Diferencia de horas entre dos fechas con horas.
    public long diferenciaDeDias_Horas(String str_fechaInicial, String str_fechaFinal){

        try {
            Date fechaInicial = formatoFechaHora.parse(str_fechaInicial);
            Date fechaFinal = formatoFechaHora.parse(str_fechaFinal);

            return horasHabiles(fechaInicial, fechaFinal);
        } catch (ParseException e) {
            System.out.println("Fecha_Inicial: " +str_fechaInicial);
            System.out.println("Fecha_Final: " +str_fechaFinal);
            e.printStackTrace();
        }
        return -1;
    }



    // Obtiene la cantidad de horas habiles entre dos fechas
    public long horasHabiles(Date fechaInicial, Date fechaFinal){

        //Cantidad de dias entre fechas
        long dias = TimeUnit.DAYS.convert(fechaFinal.getTime() - fechaInicial.getTime(), TimeUnit.MILLISECONDS);
        Date fecha = fechaInicial;
        long cantHoras = 0;
        int dia = 0;

        long horasRestantes = 0;
        horasRestantes = TimeUnit.HOURS.convert(fechaFinal.getTime() - fechaInicial.getTime(), TimeUnit.MILLISECONDS);
        calendar.setTime(fecha);
        dia = calendar.get(Calendar.DAY_OF_WEEK);

        if (DateUtils.isSameDay(fechaInicial,fechaFinal))
            return horasRestantes+1;
        else
            // Puede suceder de que se apruebe el sabado y se entregue en el correo un domingo.
            // En ese caso las horas habiles igual ~24hs??
            while (!DateUtils.isSameDay(fecha,fechaFinal) && fecha.before(fechaFinal)) {
                if (dia == Calendar.SUNDAY ) {
                    horasRestantes = horasRestantes - (24 - calendar.get(Calendar.HOUR_OF_DAY));
                    fecha = DateUtils.addDays(fecha, 1);
                    fecha = DateUtils.setHours(fecha, 0);
                    fecha = DateUtils.setMinutes(fecha, 0);
                    fecha = DateUtils.setSeconds(fecha, 0);
                    calendar.setTime(fecha);
                } else if (dia == Calendar.SATURDAY) {
                    horasRestantes = horasRestantes - (24 - calendar.get(Calendar.HOUR_OF_DAY)) - 24;
                    fecha = DateUtils.addDays(fecha, 2);
                    fecha = DateUtils.setHours(fecha, 0);
                    fecha = DateUtils.setMinutes(fecha, 0);
                    fecha = DateUtils.setSeconds(fecha, 0);
                    calendar.setTime(fecha);
                }else {
                    fecha = DateUtils.addDays(fecha, 1);
                    fecha = DateUtils.setHours(fecha, 0);
                    fecha = DateUtils.setMinutes(fecha, 0);
                    fecha = DateUtils.setSeconds(fecha, 0);
                    calendar.setTime(fecha);
                }
                dia = calendar.get(Calendar.DAY_OF_WEEK);
            }

        if (horasRestantes<0) // Caso que se apruebe un sabado y se entregue un domingo
            return 24;
        return horasRestantes+1;

        /*
        calendar.setTime(fecha);
        //Agrego las horas restantes del primer dia.
        if (dias>0)
            cantHoras = 24 - calendar.get(Calendar.HOUR_OF_DAY);
        else
            cantHoras = calendar.get(Calendar.HOUR_OF_DAY);

        for (int i=1; i< dias; i++) {
            fecha = DateUtils.addDays(fecha,1);
            calendar.setTime(fecha);
            dia = calendar.get(Calendar.DAY_OF_WEEK);

//            if (dia != Calendar.SUNDAY || dia != Calendar.SATURDAY || !esFeriado(fecha))
            if (dia != Calendar.SUNDAY && dia != Calendar.SATURDAY)
                //TODO: Que pasa si comparo un sabado o un domingo? TESTEAR VARIOS CASOS.
                //Si es un dia habil, le sumo 24hs.
                cantHoras += 24;
        }

        //Agrego las horas del ultimo dia
        calendar.setTime(fechaFinal);
        if (dias>0)
            cantHoras += calendar.get(Calendar.HOUR_OF_DAY);
        else
            cantHoras = calendar.get(Calendar.HOUR_OF_DAY) - cantHoras;

        return cantHoras;
        */
    }

    public Date aumentarYformatear(Date fecha, int i){
        Date nueva_fecha = (Date) fecha.clone();
        fecha = DateUtils.addDays(fecha, i);
        fecha = DateUtils.setHours(fecha, 0);
        fecha = DateUtils.setMinutes(fecha, 0);
        fecha = DateUtils.setSeconds(fecha, 0);

        return nueva_fecha;
    }

    public boolean esFeriado(Date fecha){

        return true;
    }


}
