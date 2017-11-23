import com.opencsv.CSVReader;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

    static Instances instances;

    public static void main(String[] args) {
        // Seteo las instancias
        setInstances();
        leerCSV();
        guardarArff();
    }

    // Esta funcion se encarga de crear
    public static void setInstances(){
        // Creo la lista de atributos
        List<Attribute> atts = new ArrayList<Attribute>();

        // Fecha de crompra del producto
        atts.add(new Attribute("SHP_DATE_CREATED_ID"));
        // Fecha y hora de compra del producto
        atts.add(new Attribute("SHP_DATETIME_CREATED_ID"));
        // Fecha de aprobacion de pago
        atts.add(new Attribute("SHP_DATE_HANDLING_ID"));
        // Fecha y hora de aprobacion de pago
        atts.add(new Attribute("SHP_DATETIME_HANDLING_ID"));
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
        atts.add(new Attribute("SHP_DATE_SHIPPED_ID"));
        // Fecha y hora de despacho del producto en el correo del vendedor
        atts.add(new Attribute("SHP_DATETIME_SHIPPED_ID"));
        // Tipo de envio
        atts.add(new Attribute("SHP_PICKING_TYPE_ID",true));
        // Horas que tardo el envio
        atts.add(new Attribute("HT_REAL"));


        instances = new Instances("demora",(ArrayList<Attribute>) atts,1);
    }

    public static void leerCSV(){

        //Tiene una longitud de 137837152 lineas de datos
        try {
//            BufferedReader bufferedReader = new BufferedReader(new FileReader("data_handling.csv"));
            CSVReader csvReader = new CSVReader(new FileReader("data_handling.csv"));

            // Para testear voy a dividir el archivo en tres
            // 60% learning     20% cv      20% testing
            // Tamano = 670858 lineas
            // 670858 * 0.6 =
            String linea[];

            //No hay de id de primera columna repetidos
            //La primer linea la salteo.
            csvReader.readNext();
            int i = 0;
            while (((linea = csvReader.readNext()) != null) && i<80000) {
                instanciarAtributos(linea);
                i++;
            }

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void instanciarAtributos(String linea[]){
        double [] vals = new double[instances.numAttributes()];

        preProcesamiento(linea);

        vals[0] = Double.valueOf(linea[1]);
        vals[1] = Double.valueOf(linea[2]);
        vals[2] = Double.valueOf(linea[3]);
        vals[3] = Double.valueOf(linea[4]);
        vals[4] = instances.attribute(4).addStringValue(linea[5]);
        vals[5] = instances.attribute(5).addStringValue(linea[6]);
        vals[6] = Double.valueOf(linea[7]);
        vals[7] = instances.attribute(7).addStringValue(linea[8]);
        vals[8] = Double.parseDouble(linea[9]);
        vals[9] = Double.parseDouble(linea[10]);
        vals[10] = Double.parseDouble(linea[11]);
        vals[11] = instances.attribute(11).addStringValue(linea[12]);
        vals[12] = Double.parseDouble(linea[13]);

        Instance i = new DenseInstance(1.0,vals);
        instances.add(i);
    }

    public static void preProcesamiento(String linea[]){

            // Fecha compra de producto
            String fechaInicial = linea[1];
            // Fecha y hora compra de producto
            // Remuevo los microsegundos
            String fecha_horaInicial = linea[2];

            // Fecha aprobacion de pago
            String fecha_aprobacion = String.valueOf(diferenciaDeDias(fechaInicial, linea[3]));
            String fecha_horaAprobacion = String.valueOf(diferenciaDeDias_Horas(fecha_horaInicial, linea[4]));


            // Fecha que deja el paquete en el correo
            String fecha_correo = String.valueOf(diferenciaDeDias(fechaInicial, linea[10]));
            // Fecha y hora en el que deja el paquete en el correo
            String fecha_horaCorreo = String.valueOf(diferenciaDeDias_Horas(fecha_horaInicial, linea[11]));

            linea[1] = "0";
            linea[2] = "0";
            linea[3] = fecha_aprobacion;
            linea[4] = fecha_horaAprobacion;
            linea[10] = fecha_correo;
            linea[11] = fecha_horaCorreo;
    }

    //Diferencia de dias entre dos fechas
    public static long diferenciaDeDias(String str_fechaInicial, String str_fechaFinal){

        //Fecha y hora ej: 2017-07-03
        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date1 = formatoFecha.parse(str_fechaInicial);
            Date date2 = formatoFecha.parse(str_fechaFinal);
            long diff = date2.getTime() - date1.getTime();
            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

    //Diferencia de dias entre dos fechas con horas.
    public static long diferenciaDeDias_Horas(String str_fechaInicial, String str_fechaFinal){

        //Fecha y hora ej: 2017-07-03 11:08:26.0
        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        try {
            Date date1 = formatoFecha.parse(str_fechaInicial);
            Date date2 = formatoFecha.parse(str_fechaFinal);
            long diff = date2.getTime() - date1.getTime();
            return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void guardarArff(){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("./data.arff"));
            writer.write(instances.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
