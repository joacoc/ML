import com.opencsv.CSVReader;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Main {

    static Instances instances;
    static CalculadorFechas calculadorFechas = new CalculadorFechas();
    static HashMap<String,ArrayList<String[]>> users_lineas = new HashMap<>();
    static Procesador procesador = new Procesador();

    public static void main(String[] args) {
        // Seteo las instancias
        setInstances();
        leerCSV();
//        guardarArff();
    }

    // Esta funcion se encarga de crear
    public static void setInstances(){

        // Creo la lista de atributos
        List<Attribute> atts = new ArrayList<Attribute>();

        // Fecha de crompra del producto
        atts.add(new Attribute("SHP_DATE_CREATED_ID",true));
        // Fecha y hora de compra del producto
        atts.add(new Attribute("SHP_DATETIME_CREATED_ID",true));

        //Mes del ano
        atts.add(new Attribute("SHP_MONTHOFYEAR_CREATED_ID"));
        //Dia del mes
        atts.add(new Attribute("SHP_DAYOFMONTH_CREATED_ID"));
        //Dia de la semana
        atts.add(new Attribute("SHP_DAY_CREATED_ID"));
        //Hora del dia
        atts.add(new Attribute("SHP_HOUR_CREATED_ID"));
        //Discretizacion hora
        atts.add(new Attribute("SHP_HOUR_DISC_CREATED_ID",true));

        // Fecha de aprobacion de pago
        atts.add(new Attribute("SHP_DATE_HANDLING_ID",true));
        // Fecha y hora de aprobacion de pago
        atts.add(new Attribute("SHP_DATETIME_HANDLING_ID",true));

        //Mes del ano aprobacion
        atts.add(new Attribute("SHP_MONTHOFYEAR_HANDLING_ID"));
        //Dia del mes aprobacion
        atts.add(new Attribute("SHP_DAYOFMONTH_HANDLING_ID"));
        //Dia de la semana aprobacion
        atts.add(new Attribute("SHP_DAY_HANDLING_ID"));
        //Hora del dia aprobacion
        atts.add(new Attribute("SHP_HOUR_HANDLING_ID"));
        //Discretizacion hora
        atts.add(new Attribute("SHP_HOUR_DISC_HANDLING_ID",true));

        // Diferencia de horas entre aprobacion y compra
        atts.add(new Attribute("DIF_HORAS_CREATE_HANDLING"));
        // Diferencia de dias entre aprobacion y compra
        atts.add(new Attribute("DIF_DIAS_CREATED_HANDLING"));


        //Diferencia de horas entre aprobacion y correo
        atts.add(new Attribute("DIF_HORAS_SHIPPED_HANDLING"));
        //Diferencia de dias entre aprobacion y correo
        atts.add(new Attribute("DIF_DIAS_SHIPPED_HANDLING"));

        // Estado del envio
        atts.add(new Attribute("SHP_STATUS_ID",true));
        // Identificador del vendedor
        atts.add(new Attribute("SHP_SENDER_ID",true));
        // Costo del producto
        atts.add(new Attribute("SHP_ORDER_COST"));
        // Categoria del producto
        atts.add(new Attribute("CAT_CATEG_ID_L7",true));
        // Codigo zip del vendedor
        atts.add(new Attribute("SHP_ADD_ZIP_CODE"));

        // Fecha de despacho del producto en el correo por parte del vendedor
        atts.add(new Attribute("SHP_DATE_SHIPPED_ID",true));
        // Fecha y hora de despacho del producto en el correo del vendedor
        atts.add(new Attribute("SHP_DATETIME_SHIPPED_ID",true));

        //Mes del ano delivery
        atts.add(new Attribute("SHP_MONTHOFYEAR_SHIPPED_ID"));
        //Dia del mes delivery
        atts.add(new Attribute("SHP_DAYOFMONTH_SHIPPED_ID"));
        //Dia de la semana delivery
        atts.add(new Attribute("SHP_DAY_SHIPPED_ID"));
        //Hora del dia delivery
        atts.add(new Attribute("SHP_HOUR_SHIPPED_ID"));
        //Discretizacion delivery
        atts.add(new Attribute("SHP_HOUR_DISC_SHIPPED_ID",true));

        // Tipo de envio
        atts.add(new Attribute("SHP_PICKING_TYPE_ID",true));
        // Horas que tardo el envio
        atts.add(new Attribute("HT_REAL"));

        instances = new Instances("demora",(ArrayList<Attribute>) atts,1);
    }

    public static void leerCSV(){
        //Tiene una longitud de 137837152 lineas de datos
        try {
            CSVReader csvReader = new CSVReader(new FileReader("data_handling.csv"));
            String linea[];

            //No hay de id de primera columna repetidos
            //La primer linea la salteo.
            csvReader.readNext();
            ArrayList<String[]> al;
            while (((linea = csvReader.readNext()) != null)) {
                if (users_lineas.containsKey(linea[6])){
                    al = users_lineas.get(linea[6]);
                    al.add(linea);
                    users_lineas.put(linea[6],al);
                }else{
                    al = new ArrayList<>();
                    al.add(linea);
                    users_lineas.put(linea[6],al);
                }
            }

//            for (String key : users_lineas.keySet()) {
                for (String[] arr : users_lineas.get("be5d6e32444e1e5c3db74845d226064e")) {
                    instanciarAtributos(arr);
                }
                instances = procesador.procesarUsuario(instances);
                guardarArff("be5d6e32444e1e5c3db74845d226064e");
                instances.delete();
//            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void instanciarAtributos(String linea[]){
        double [] vals = new double[instances.numAttributes()];
        ArrayList<String> preproceso;

        //est = HT corregido
        double est = Double.valueOf(calculadorFechas.diferenciaDeDias_Horas(linea[4],linea[11]));
        vals[31] = est;

        preproceso = preProcesamiento(linea);

        // Valores no utilizados y reemplazados por otros
        vals[0] = instances.attribute(0).addStringValue(preproceso.get(0));//Fecha de compra
        vals[1] = instances.attribute(1).addStringValue(preproceso.get(1));//Fecha y  hora de compra
        vals[2] = Double.valueOf(preproceso.get(2));                            //Mes de compra
        vals[3] = Double.valueOf(preproceso.get(3));                            //Dia del mes de compra
        vals[4] = Double.valueOf(preproceso.get(4));                            //Dia de la semana de compra
        vals[5] = Double.valueOf(preproceso.get(5));                            //Hora de compra
        vals[6] = instances.attribute(6).addStringValue(preproceso.get(6));//Discretizacion hora compra

        vals[7] = instances.attribute(7).addStringValue(preproceso.get(7));    //Fecha de aprobacion
        vals[8] = instances.attribute(8).addStringValue(preproceso.get(8));    //Fecha y horade aprobacion
        vals[9] = Double.valueOf(preproceso.get(9));                                //Mes de aprobacion
        vals[10] = Double.valueOf(preproceso.get(10));                              //Dia del mes de aprobacion
        vals[11] = Double.valueOf(preproceso.get(11));                              //Dia de la semana de aprobacion
        vals[12] = Double.valueOf(preproceso.get(12));                              //Hora de aprobacion
        vals[13] = instances.attribute(13).addStringValue(preproceso.get(13)); //Discretizacion hora aprobacion

        vals[14] = Double.valueOf(preproceso.get(21));  // Diferencia de horas entre aprobacion y compra
        vals[15] = Double.valueOf(preproceso.get(22));  // Diferencia de dias entre aprobacion y compra
        vals[16] = Double.valueOf(preproceso.get(23));  // Diferencia de horas entre aprobacion y delivery
        vals[17] = Double.valueOf(preproceso.get(24));  // Diferencia de dias entre aprobacion y delivery

        vals[18] = instances.attribute(18).addStringValue(linea[5]); //Status ID
        vals[19] = instances.attribute(19).addStringValue(linea[6]); //Sender ID
        vals[20] = Double.valueOf(linea[7]); //Order cost
        vals[21] = instances.attribute(21).addStringValue(linea[8]); //Categoria
        vals[22] = Double.valueOf(linea[9]); //Zip Code

        vals[23] = instances.attribute(23).addStringValue(preproceso.get(14)); //Fecha despacho
        vals[24] = instances.attribute(24).addStringValue(preproceso.get(15)); //Fecha y hora despacho
        vals[25] = Double.valueOf(preproceso.get(16));                              //Mes de aprobacion
        vals[26] = Double.valueOf(preproceso.get(17));                              //Dia del mes de aprobacion
        vals[27] = Double.valueOf(preproceso.get(18));                              //Dia de la semana de aprobacion
        vals[28] = Double.valueOf(preproceso.get(19));                              //Hora de aprobacion
        vals[29] = instances.attribute(29).addStringValue(preproceso.get(20)); //Discretizacion hora aprobacion

        vals[30] = instances.attribute(30).addStringValue(linea[12]); //Picking type

        Instance i = new DenseInstance(1.0, vals);
        instances.add(i);
    }

    public static String discretizacionHoraDia(int hora){
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

    public static ArrayList<String> desmenuzarFecha(String fecha){
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

    public static ArrayList<String> preProcesamiento(String linea[]){

        ArrayList<String> resultado = new ArrayList<>();

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

    public static void guardarArff(String key){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("testingbe5d6e32444e1e5c3db74845d226064e.arff"));
            writer.write(instances.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
