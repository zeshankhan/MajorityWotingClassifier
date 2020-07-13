/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lire_feature_extraction;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import net.semanticmetadata.lire.imageanalysis.AutoColorCorrelogram;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.ColorLayout;
import net.semanticmetadata.lire.imageanalysis.EdgeHistogram;
import net.semanticmetadata.lire.imageanalysis.FCTH;
import net.semanticmetadata.lire.imageanalysis.Gabor;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.imageanalysis.PHOG;
import net.semanticmetadata.lire.imageanalysis.Tamura;

/**
 *
 * @author fast
 */
public class Lire_feature_extraction {

    public static double [][] feature_extraction(String image_path,String [] lbls){
        BufferedImage img = null;
        //System.out.println(image_path);
        try {
            img = ImageIO.read( new File(image_path));
        } catch (IOException ex) {
            Logger.getLogger(Lire_feature_extraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        LireFeature lf=null;
        double [][] features=new double[lbls.length][];
        int f_counter=0;
        
        if(Arrays.asList(lbls).contains("JCD")){
            lf=new CEDD();
            lf.extract(img);
            double [] temp1 = lf.getDoubleHistogram();

            lf=new FCTH();
            lf.extract(img);
            double [] temp2 = lf.getDoubleHistogram();

            features[f_counter++]=new double[temp1.length+temp2.length];
            System.arraycopy(temp1, 0, features[0], 0, temp1.length);
            System.arraycopy(temp2, 0, features[0], temp1.length, temp2.length);
        }
        if(Arrays.asList(lbls).contains("Tamura")){
            lf=new Tamura();
            lf.extract(img);
            features[f_counter++] = lf.getDoubleHistogram();
        }
        if(Arrays.asList(lbls).contains("ColorLayout")){
            lf=new ColorLayout();
            lf.extract(img);
            features[f_counter++] = lf.getDoubleHistogram();
        }
        if(Arrays.asList(lbls).contains("EdgeHistogram")){
            lf=new EdgeHistogram();
            lf.extract(img);
            features[f_counter++] = lf.getDoubleHistogram();
        }
        if(Arrays.asList(lbls).contains("AutoColorCorrelogram")){
            lf=new AutoColorCorrelogram();
            lf.extract(img);
            features[f_counter++] = lf.getDoubleHistogram();
        }
        if(Arrays.asList(lbls).contains("PHOG")){
            lf=new PHOG();
            lf.extract(img);
            features[f_counter++] = lf.getDoubleHistogram();
        }
        if(Arrays.asList(lbls).contains("Gabor")){
            lf=new Gabor();
            lf.extract(img);
            features[f_counter++] = lf.getDoubleHistogram();
        }
        return features;
    }
    public static void extract_store_features_dir(String [] lbls,String base_path,int dir_num, String train_test, String [] label_map,String features_path,String dataset_name) {
        BufferedWriter [] writers= new BufferedWriter[lbls.length];
        for(int i =0;i<lbls.length;i++){
            writers[i]=null;
        }
        try {
            String data_path=base_path+dir_num+"\\"+train_test+"\\";
            for(int i=0;i<lbls.length;i++){
                writers[i] = new BufferedWriter(new FileWriter(new File(features_path+dataset_name+"_"+dir_num+"_"+train_test+"_"+lbls[i]+".csv")));
            }
            String [] folders=new File(data_path).list();
            for(int i =0 ;i<folders.length;i++){
                int y_lbl=Arrays.asList(label_map).indexOf(folders[i])+1;
                String lbl=folders[i];
                System.out.println(folders[i]);
                String [] images=new File(data_path+folders[i]).list();
                for(int j =0;j<images.length;j++){
                    //System.out.println(images[j]);
                    double[][] features=feature_extraction(data_path+folders[i]+"\\"+images[j],lbls);
                    for(int f=0;f<lbls.length;f++){
                        String featurestr="";
                        for(int temp=0;temp<features[f].length-1;temp++){
                            featurestr+=features[f][temp]+",";
                        }
                        featurestr+=features[f][features[f].length-1]+","+y_lbl+","+images[j].split(".")[0]+"\n";
                        writers[f].append(featurestr);
                    }
                    //break;
                }
                //break;
            }
            for(int i=0;i<lbls.length;i++){
                writers[i].close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Lire_feature_extraction.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                for(int i=0;i<lbls.length;i++){
                    writers[i].close();
                }
            } catch (IOException ex) {
                Logger.getLogger(Lire_feature_extraction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * @param lbls The labels of the features that needed to be stored
     * @param data_path The path of the directory where there are image files
     * @param features_path The path of the features
     * @param data_set_name The parameter for the name of dataset being used
     * @param label_map The labels order for the Y/label
     */
    //
    public static void extract_store_features(String [] lbls,String data_path,String data_set_name, String features_path) {
        BufferedWriter [] writers= new BufferedWriter[lbls.length];
        for(int i =0;i<lbls.length;i++){
            writers[i]=null;
        }
        try {
            for(int i=0;i<lbls.length;i++){
                writers[i] = new BufferedWriter(new FileWriter(new File(features_path+data_set_name+"_"+lbls[i]+".csv")));
            }
            String [] folders=new File(data_path).list();
            for(int i =0 ;i<folders.length;i++){
                System.out.println(folders[i]);
                String [] images=new File(data_path+folders[i]).list();
                for(int j =0;j<images.length;j++){
                    //System.out.println(images[j]);
                    double[][] features=feature_extraction(data_path+folders[i]+"/"+images[j],lbls);
                    for(int f=0;f<lbls.length;f++){
                        String featurestr=i+",";
                        for(int temp=0;temp<features[f].length-1;temp++){
                            featurestr+=features[f][temp]+",";
                        }
                        featurestr+=features[f][features[f].length-1]+","+folders[i]+","+images[j]+"\n";
                        writers[f].append(featurestr);
                    }
                }
            }
            for(int i=0;i<lbls.length;i++){
                writers[i].close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Lire_feature_extraction.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                for(int i=0;i<lbls.length;i++){
                    writers[i].close();
                }
            } catch (IOException ex) {
                Logger.getLogger(Lire_feature_extraction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * @param lbls The labels of the features that needed to be stored
     * @param data_path The path of the directory where there are image files
     * @param features_path The path of the features
     * @param data_set_name The parameter for the name of dataset being used
     */
    //
    public static void extract_store_features_test(String [] lbls,String data_path,String data_set_name, String features_path) {
        System.out.println("Processing Started:\t"+data_set_name);
        BufferedWriter [] writers= new BufferedWriter[lbls.length];
        for(int i =0;i<lbls.length;i++){
            writers[i]=null;
        }
        try {
            for(int i=0;i<lbls.length;i++){
                writers[i] = new BufferedWriter(new FileWriter(new File(features_path+data_set_name+"_"+lbls[i]+".csv")));
            }
            String [] folders=new File(data_path).list();
            //for(int i =0 ;i<folders.length;i++)
            {
                //System.out.println(folders[i]);
                String [] images=new File(data_path).list();
                for(int j =0;j<images.length;j++){
                    //System.out.println(images[j]);
                    double[][] features=feature_extraction(data_path+images[j],lbls);
                    for(int f=0;f<lbls.length;f++){
                        String featurestr="";
                        for(int temp=0;temp<features[f].length-1;temp++){
                            featurestr+=features[f][temp]+",";
                        }
                        featurestr+=features[f][features[f].length-1]+"\n";
                        writers[f].append(featurestr);
                    }
                }
            }
            for(int i=0;i<lbls.length;i++){
                writers[i].close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Lire_feature_extraction.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                for(int i=0;i<lbls.length;i++){
                    writers[i].close();
                }
            } catch (IOException ex) {
                Logger.getLogger(Lire_feature_extraction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    /**
     * @param args the command line arguments
     */
    //
    //lengths=[168,18,33,80,256,630]
    public static void main(String[] args) {
        String [] lbls={"JCD", "Tamura", "ColorLayout", "EdgeHistogram", "AutoColorCorrelogram", "PHOG","Gabor"};
        String features_path="path_to_features_file [A directory]";
        String base_path="path_to_dataset [A directory]";
        String dataset_name="icpr_20202";//Any name to be used in file name as indication
        if(args.length>1){
            features_path=args[1];
        }
        if(args.length>2){
            base_path=args[2];
        }
        if(args.length>3){
            dataset_name=args[3];
        }
        extract_store_features(lbls,base_path,dataset_name,features_path);
    }
}
