import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.InterquartileRange;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.RemoveType;
import weka.filters.unsupervised.attribute.StringToNominal;
import weka.filters.unsupervised.instance.RemoveWithValues;

import javax.management.InstanceAlreadyExistsException;

/**
 * Created by Joaking on 11/26/2017.
 */
public class Procesador {
    public Procesador(){
    }

    private Instances removerAtributos(Instances instances, int desde, int hasta){
        for (int i = hasta; i>=desde; i--)
            instances.deleteAttributeAt(i);
        return instances;
    }

    public Instances procesarUsuario(Instances instances){
        //Elimino atributos que no necesi   to
        instances = removerAtributos(instances,23,30);
        instances = removerAtributos(instances,14,20);
        instances = removerAtributos(instances,0,8);


        try {
            StringToNominal stringToNominalFilter = new StringToNominal();
            stringToNominalFilter.setAttributeRange("5,6");
            stringToNominalFilter.setInputFormat(instances);
            instances = Filter.useFilter(instances,stringToNominalFilter);

            NumericToNominal numericToNominal = new NumericToNominal();
            numericToNominal.setAttributeIndices("1-4,7");
            numericToNominal.setInputFormat(instances);
            instances = Filter.useFilter(instances,numericToNominal);

            //Remuevo los extremos
            if (instances.numInstances()>4) {
                InterquartileRange interquartileRange = new InterquartileRange();
                interquartileRange.setAttributeIndices("8-8");
                interquartileRange.setExtremeValuesAsOutliers(true);
                interquartileRange.setInputFormat(instances);
                instances = Filter.useFilter(instances, interquartileRange);

                RemoveWithValues removeWithValues = new RemoveWithValues();
                removeWithValues.setAttributeIndex("9");
                removeWithValues.setNominalIndices("2");
                removeWithValues.setInputFormat(instances);
                instances = Filter.useFilter(instances, removeWithValues);

                //Remuevo las columnas creadas por el atributo anterior
                instances.deleteAttributeAt(9);
                instances.deleteAttributeAt(8);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instances;
    }


    public Instances procesarCategoria(Instances instances){

        //Elimino atributos que no necesito
        instances = removerAtributos(instances,14,30);
        instances = removerAtributos(instances,0,8);

        try {
            StringToNominal stringToNominalFilter = new StringToNominal();
            stringToNominalFilter.setAttributeRange("5,6");
            stringToNominalFilter.setInputFormat(instances);
            instances = Filter.useFilter(instances, stringToNominalFilter);

            NumericToNominal numericToNominal = new NumericToNominal();
            numericToNominal.setAttributeIndices("1-4,7");
            numericToNominal.setInputFormat(instances);
            instances = Filter.useFilter(instances, numericToNominal);

            //Remuevo los extremos
            InterquartileRange interquartileRange = new InterquartileRange();
            interquartileRange.setAttributeIndices("8-8");
            interquartileRange.setExtremeValuesAsOutliers(true);
            interquartileRange.setInputFormat(instances);
            instances = Filter.useFilter(instances, interquartileRange);

            RemoveWithValues removeWithValues = new RemoveWithValues();
            removeWithValues.setAttributeIndex("9");
            removeWithValues.setNominalIndices("2");
            removeWithValues.setInputFormat(instances);
            instances = Filter.useFilter(instances, removeWithValues);

            //Remuevo las columnas creadas por el atributo anterior
            instances.deleteAttributeAt(9);
            instances.deleteAttributeAt(8);
        } catch (Exception e) {
        e.printStackTrace();
        }

        return instances;
    }

    public Instances procesarCodigoZip(Instances instances){
        //Elimino atributos que no necesito
        instances = removerAtributos(instances,22,30);
        instances.deleteAttributeAt(20);
        instances.deleteAttributeAt(19);
        instances = removerAtributos(instances,14,18);
        instances = removerAtributos(instances,0,8);

        try {
            StringToNominal stringToNominalFilter = new StringToNominal();
            stringToNominalFilter.setAttributeRange("5,6");
            stringToNominalFilter.setInputFormat(instances);
            instances = Filter.useFilter(instances, stringToNominalFilter);

            NumericToNominal numericToNominal = new NumericToNominal();
            numericToNominal.setAttributeIndices("1-4");
            numericToNominal.setInputFormat(instances);
            instances = Filter.useFilter(instances, numericToNominal);

            //Remuevo los extremos
            if (instances.numInstances()>4) {
                InterquartileRange interquartileRange = new InterquartileRange();
                interquartileRange.setAttributeIndices("6");
                interquartileRange.setExtremeValuesAsOutliers(true);
                interquartileRange.setInputFormat(instances);
                instances = Filter.useFilter(instances, interquartileRange);

                RemoveWithValues removeWithValues = new RemoveWithValues();
                removeWithValues.setAttributeIndex("7");
                removeWithValues.setNominalIndices("2");
                removeWithValues.setInputFormat(instances);
                instances = Filter.useFilter(instances, removeWithValues);

                //Remuevo las columnas creadas por el atributo anterior
                instances.deleteAttributeAt(8);
                instances.deleteAttributeAt(7);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return instances;

    }
}
