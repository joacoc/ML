import com.opencsv.CSVReader;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.lazy.IBk;
import weka.core.*;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class Main {

    static List<Attribute> atts;
    static Instances instances;
    static CalculadorFechas calculadorFechas = new CalculadorFechas();
    static HashMap<String, ArrayList<String[]>> hash_lineas = new HashMap<>();
    static Procesador procesador = new Procesador();
    static PreProcesador preProcesador = new PreProcesador();
    static double total_correlation = 0;
    static double cant_inst_proc = 0;

    public static void main(String[] args) {
        // Seteo las instancias
        setInstances();
        leerCSV();
//        try {
//            testing();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public static void testing() throws Exception {
        IBk iBk = (IBk) SerializationHelper.read(new FileInputStream("model.weka"));
    }

    // Esta funcion se encarga de crear la estructura de las instancias
    public static void setInstances(){

        // Creo la lista de atributos
        atts = new ArrayList<>();

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

    //Levanta un CSV y almacena cada linea en un hashmap para su posterior procesamiento.
    public static void leerCSV(){
        //Tiene una longitud de 137837152 lineas de datos
        try {
            CSVReader csvReader = new CSVReader(new FileReader("data_handling.csv"));
            String linea[];

            //No hay de id de primera columna repetidos
            //La primer linea la salteo.
            csvReader.readNext();

            while (((linea = csvReader.readNext()) != null))
//                cargarUsers(linea);
                cargarCodigoZip();
            try {
//                procesarUsuarios();
                procesarZip();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Se encarga de procesar cada una de las direcciones zip
    public static void procesarZip() throws Exception {

        int instancias_totales = 0;
        for (String key : hash_lineas.keySet()) {
            for (String[] arr : hash_lineas.get(key)) {
                instanciarAtributos(arr);
            }
            instances = procesador.procesarCodigoZip(instances);

            // Almaceno en instances_aux el set de testeo
            Instances instances_aux = new Instances(instances);
            instances_aux.clear();
            int num_instancias = instances.numInstances()-1;

            for (int i = num_instancias; i > (int) (num_instancias*0.8); i--){
                instances_aux.add(instances.get(i));
                instances.remove(i);
            }
            instancias_totales += instances.numInstances();
            instances_aux.setClassIndex(6);
            instances.setClassIndex(6);
            evaluarIbk(instances,instances_aux,1,key);

            //Guardo los datos utilizados en los modelos y los correspondientes data set de testing
            guardarArff(".\\modelos\\zip\\"+key+".arff");
            instances = instances_aux;
            guardarArff(".\\modelos\\zip\\test"+key+".arff");
            instances.delete();

            //Cuando se pasan las instancias por el procesador se eliminan varias columnas
            instances = new Instances("demora",(ArrayList<Attribute>) atts,1);
        }
        System.out.println("Instancias totales: " +instancias_totales);
    }

    //Carga los datos de un codigo zip para su posterior procesamiento
    public static void cargarCodigoZip(String[] linea){
        ArrayList<String[]> al;
        //La key es el codigo zip
        if (hash_lineas.containsKey(linea[9])){
            al = hash_lineas.get(linea[9]);
            al.add(linea);
            hash_lineas.put(linea[9],al);
        }else{
            al = new ArrayList<>();
            al.add(linea);
            hash_lineas.put(linea[9],al);
        }
    }


    //Procesa y almacena los datos para construir los modelos de los usuarios
    public static void procesarUsuarios() throws Exception {
        System.out.println("Iniciando procesamiento");
        for (String key : hash_lineas.keySet()) {
            for (String[] arr : hash_lineas.get(key)) {
                instanciarAtributos(arr);
            }
            instances = procesador.procesarUsuario(instances);

            // Almaceno en instances_aux el set de testeo
            Instances instances_aux = new Instances(instances);
            instances_aux.clear();
            int num_instancias = instances.numInstances()-1;

            for (int i = num_instancias; i> (int) (num_instancias*0.8); i--){
                instances_aux.add(instances.get(i));
                instances.remove(i);
            }

            //Seteo el HT como la clase.
            instances_aux.setClassIndex(instances_aux.numAttributes()-1);
            instances.setClassIndex(instances.numAttributes()-1);
            evaluarIbk(instances,instances_aux, 1,key);

            //Guardo los datos de los modelos y los set de testeo.
            guardarArff(".\\modelos\\usuarios\\" + key + ".arff");
            instances = instances_aux;
            guardarArff(".\\modelos\\usuarios\\test"+key+".arff");
            instances.delete();

            //Cuando paso por el procesador se eliminaron varias columnas
            instances = new Instances("demora", (ArrayList<Attribute>) atts, 1);
            }
    }

    //Carga los datos de cada instancia de los usuarios
    public static void cargarUsers(String[] linea){
        ArrayList<String[]> al;
        //Cada key es el id del usuario
        if (hash_lineas.containsKey(linea[6])){
            al = hash_lineas.get(linea[6]);
            al.add(linea);
            hash_lineas.put(linea[6],al);
        }else{
            al = new ArrayList<>();
            al.add(linea);
            hash_lineas.put(linea[6],al);
        }
    }

    public static void instanciarAtributos(String linea[]){
        double [] vals = new double[instances.numAttributes()];

        ArrayList<String> preproceso;
        preproceso = preProcesador.preProcesamiento(linea);

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
        vals[30] = instances.attribute(30).addStringValue(linea[12]);           //Picking type

        //est = HT corregido
        double est = Double.valueOf(calculadorFechas.diferenciaDeDias_Horas(linea[4],linea[11]));
        vals[31] = est;

        Instance i = new DenseInstance(1.0, vals);
        instances.add(i);
    }

    //Clasificador Ibk
    public static void evaluarIbk(Instances instances, Instances instances_aux, int i, String key) throws Exception {
        IBk iBk = new IBk();
        iBk.setKNN(i);
        iBk.buildClassifier(instances);

        //Evaluo que tan efectivo es el modelo
        Evaluation eval = new Evaluation(instances);
        eval.evaluateModel(iBk,instances_aux);

        guardarModelo(iBk,key,eval.correlationCoefficient());

    }

    //Clasificar de regresion lineal.
    public static void evaluarLinearRegression(Instances instances_aux, Instances instances) throws Exception {
        weka.classifiers.functions.LinearRegression linearRegression = new weka.classifiers.functions.LinearRegression();
        linearRegression.buildClassifier(instances);
        Evaluation eval = new Evaluation(instances);
        eval.evaluateModel(linearRegression,instances_aux);
        total_correlation += eval.correlationCoefficient();
        cant_inst_proc++;
    }

    public static void guardarArff(String direccion){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(direccion));
            writer.write(instances.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void guardarModelo(Classifier cls, String key,double efectividad) throws IOException {

        DecimalFormat df = new DecimalFormat("#.##");
        String dx=df.format(efectividad);
        efectividad=Double.valueOf(dx);

        // serialize model
        ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(".\\modelos\\definitivos\\zip\\"+key+"-"+efectividad"+.model"));
        oos.writeObject(cls);
        oos.flush();
        oos.close();
    }


}
