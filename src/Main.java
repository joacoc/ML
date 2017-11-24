import com.opencsv.CSVReader;
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

        /*Atributos no utilizados y reemplazados por el momento de aprobacion de
            pago exacto

        // Fecha de crompra del producto
        atts.add(new Attribute("SHP_DATE_CREATED_ID"));
        // Fecha y hora de compra del producto
        atts.add(new Attribute("SHP_DATETIME_CREATED_ID"));
        // Fecha de aprobacion de pago
        atts.add(new Attribute("SHP_DATE_HANDLING_ID"));
        // Fecha y hora de aprobacion de pago

        atts.add(new Attribute("SHP_DATETIME_HANDLING_ID"));
        */

        //Hora del dia
        atts.add(new Attribute("SHP_HOUR_HANDLING_ID"));
        //Dia de la semana
        atts.add(new Attribute("SHP_DAY_HANDLING_ID"));
        //Dia del mes
        atts.add(new Attribute("SHP_DAYOFMONTH_HANDLING_ID"));
        //Mes del ano
        atts.add(new Attribute("SHP_MONTHOFYEAR_HANDLING_ID"));
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
        // MIX otras y fecha hora
        atts.add(new Attribute("SHP_MIX_HT_DATETIME"));

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
            int i = 0;

            while (((linea = csvReader.readNext()) != null) && i<3000) {
                instanciarAtributos(linea);
                i++;
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void instanciarAtributos(String linea[]){
        double [] vals = new double[instances.numAttributes()];

        double est = Double.valueOf(calculadorFechas.diferenciaDeDias_Horas(linea[4],linea[11]));
        vals[12] = est;

        // Delimito aquellos valores extremos
        if (est<145 && Double.parseDouble(linea[7])<7000) {
            preProcesamiento(linea);

/* Valores no utilizados y reemplazados por otros
        vals[0] = 0; //Fecha de compra
        vals[1] = 0; //Fecha y  hora de compra
        vals[2] = Double.valueOf(linea[1]); //Fecha de aprobacion
        vals[3] = Double.valueOf(linea[2]); //Fecha y hora de aprobacion
 */
            vals[0] = Double.valueOf(linea[1]); //Hora de aprobacion
            vals[1] = Double.valueOf(linea[2]); //Dia de la Semana
            vals[2] = Double.valueOf(linea[3]); //Dia del mes
            vals[3] = Double.valueOf(linea[4]); //Mes del ano
            vals[4] = instances.attribute(4).addStringValue(linea[5]); //Status ID
            vals[5] = instances.attribute(5).addStringValue(linea[6]); //Sender ID
            vals[6] = Double.valueOf(linea[7]); //Order cost
            vals[7] = instances.attribute(7).addStringValue(linea[8]); //Categoria
            vals[8] = Double.valueOf(linea[9]); //Zip Code
            vals[9] = Double.valueOf(linea[10]); //Fecha despacho
            vals[10] = Double.valueOf(linea[11]); //Fecha y hora despacho
            vals[11] = instances.attribute(11).addStringValue(linea[12]); //Picking type

            vals[13] = vals[9] + 0.5 * vals[12];

            Instance i = new DenseInstance(1.0, vals);
            instances.add(i);
        }
    }

    public static void preProcesamiento(String linea[]){

        Calendar c = Calendar.getInstance();
        //Fecha ej: 2017-07-03
        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
        //Fecha y hora ej: 2017-07-03 11:08:26.0
        SimpleDateFormat formatoFechaHora = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

        // Fecha de aprobacion de compra
        String fechaInicial = linea[3];

        // Fecha y hora aprobacion de compra
        // Remuevo los microsegundos
        String fecha_horaInicial = linea[4];

        // Fecha aprobacion de pago
        try {
            Date fecha = formatoFecha.parse(fechaInicial);
            c.setTime(fecha);
            String mes_aprobacion = String.valueOf( c.get(Calendar.MONTH) + 1);
            String dia_mes_aprobacion = String.valueOf( c.get(Calendar.DAY_OF_MONTH));
            String dia_sem_aprobacion = String.valueOf( c.get(Calendar.DAY_OF_WEEK) );

            fecha = formatoFechaHora.parse(linea[4]);
            c.setTime(fecha);
            String hora_aprobacion = String.valueOf( c.get(Calendar.HOUR_OF_DAY));


            // Fecha que deja el paquete en el correo
            String fecha_correo = String.valueOf(calculadorFechas.diferenciaDeDias_noHabiles(fechaInicial, linea[10]));
            // Fecha y hora en el que deja el paquete en el correo
            String fecha_horaCorreo = String.valueOf(calculadorFechas.diferenciaDeDias_Horas_noHabiles(fecha_horaInicial, linea[11]));

            linea[1] = hora_aprobacion;
            linea[2] = dia_sem_aprobacion;
            linea[3] = dia_mes_aprobacion;
            linea[4] = mes_aprobacion;
            linea[10] = fecha_correo;
            linea[11] = fecha_horaCorreo;

        } catch (ParseException e) {
            e.printStackTrace();
        }
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
