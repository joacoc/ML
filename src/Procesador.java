import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.InterquartileRange;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.RemoveType;
import weka.filters.unsupervised.attribute.StringToNominal;
import weka.filters.unsupervised.instance.RemoveWithValues;

/**
 * Created by Joaking on 11/26/2017.
 */
public class Procesador {
    public Procesador(){

    }

    public Instances procesarUsuario(Instances instances){
        //Elimino atributos que no necesito
        for (int i = 30; i>=23; i--)
            instances.deleteAttributeAt(i);

        for (int i = 20; i>=14; i--)
            instances.deleteAttributeAt(i);

        for (int i = 8; i>=0; i--)
            instances.deleteAttributeAt(i);

        try {
            StringToNominal stringToNominalFilter = new StringToNominal();
            stringToNominalFilter.setAttributeRange("5,6");
            stringToNominalFilter.setInputFormat(instances);
            instances = Filter.useFilter(instances,stringToNominalFilter);

            //Remuevo los extremos
            InterquartileRange interquartileRange = new InterquartileRange();
            interquartileRange.setAttributeIndices("8-8");
            interquartileRange.setExtremeValuesAsOutliers(true);
            interquartileRange.setInputFormat(instances);
            instances = Filter.useFilter(instances,interquartileRange);

            RemoveWithValues removeWithValues = new RemoveWithValues();
            removeWithValues.setAttributeIndex("9");
            removeWithValues.setNominalIndices("2");
            removeWithValues.setInputFormat(instances);
            instances = Filter.useFilter(instances,removeWithValues);

            //Remuevo las columnas creadas por el atributo anterior
            instances.deleteAttributeAt(9);
            instances.deleteAttributeAt(8);

            NumericToNominal numericToNominal = new NumericToNominal();
            numericToNominal.setAttributeIndices("1-4");
            numericToNominal.setInputFormat(instances);
            instances = Filter.useFilter(instances,numericToNominal);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return instances;
    }
}
