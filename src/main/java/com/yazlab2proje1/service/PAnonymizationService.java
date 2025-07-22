package com.yazlab2proje1.service;

import com.spire.pdf.FileFormat;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.graphics.PdfImage;
import org.opencv.core.*;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import nu.pattern.OpenCV;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import javax.imageio.ImageIO;

@Service
public class PAnonymizationService {

    public File anonymizePdf(String pdfPath) throws IOException {
        OpenCV.loadLocally();
        
        
        String outputPdf="C:\\Users\\aa\\Downloads\\replaceImage.pdf";
        String outputImage="C:\\Users\\aa\\Documents\\tempJava\\temp_image.png";

        PdfDocument doc = new PdfDocument();
        doc.loadFromFile(pdfPath);
        
        
        //Sayfa sayısı sınırı Demo olduğundans
        if(doc.getPages().getCount()>9){
            
            File AnonymizatedPdfFile=new File(pdfPath);
            System.out.println("10 Sayfadan büyük olamaz");
            return AnonymizatedPdfFile;
        }
        

        for (int pageIndex = 0; pageIndex < doc.getPages().getCount(); pageIndex++) {
            
            PdfPageBase page = doc.getPages().get(pageIndex);

            BufferedImage[] images = page.extractImages();
            doc.saveToFile(outputPdf, FileFormat.PDF);
            
            if (images != null) {
                int index = 0; 
                
                for (BufferedImage image : images) {
                    PdfDocument doc2 = new PdfDocument();
                    doc.loadFromFile(outputPdf);
                    PdfPageBase page2 = doc.getPages().get(pageIndex);

                    
                    ImageIO.write(image, "PNG", new File(outputImage));

                    String processedImagePath = blurFaces(outputImage);

                    PdfImage pdfImage = PdfImage.fromFile(processedImagePath);

                    page2.replaceImage(index, pdfImage);

                    index++;
                    doc.saveToFile(outputPdf, FileFormat.PDF);
                    System.out.println(index);
                }
            } else {
              //  System.out.println("No images found on page " + pageIndex);
            }
        }
        
        doc.close();

        File AnonymizatedPdfFile=new File(outputPdf);
        
        return AnonymizatedPdfFile;
    }

    // Yüz bulanıklaştırma 
    private String blurFaces(String inputImagePath) {

        String faceCascadePath = "C:\\Users\\aa\\Documents\\haarcascade_frontalface_default.xml";
        CascadeClassifier faceCascade = new CascadeClassifier(faceCascadePath);

        Mat image = Imgcodecs.imread(inputImagePath);


        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);


        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(grayImage, faces, 1.1, 10, 0, new Size(30, 30), new Size());


        for (Rect face : faces.toArray()) {
            Mat faceROI = image.submat(face);
            Imgproc.GaussianBlur(faceROI, faceROI, new Size(101, 101), 50);
        }

        String outputImagePath = "processed_image.png";
        Imgcodecs.imwrite(outputImagePath, image);

        return outputImagePath;
    }
    
    
    public File deanonymizePdf(String originImagePath,String pdfPath) throws IOException{
        OpenCV.loadLocally();
        
        
        String outputPdf="C:\\Users\\aa\\Downloads\\replaceImage.pdf";
        String outputImage="C:\\Users\\aa\\Documents\\tempJava\\temp_image.png";

        PdfDocument doc = new PdfDocument();
        doc.loadFromFile(pdfPath);
        PdfDocument doc2 = new PdfDocument();
        doc2.loadFromFile(originImagePath);        
        
        //Sayfa sayısı sınırı Demo olduğundans
        if(doc.getPages().getCount()>10){
            
            File deAnonymizatedPdfFile=new File(pdfPath);
            System.out.println("10 Sayfadan büyük olamaz");
            return deAnonymizatedPdfFile;
        }
        

        for (int pageIndex = 0; pageIndex < doc.getPages().getCount()-1; pageIndex++) {
            
            PdfPageBase page = doc2.getPages().get(pageIndex);

            BufferedImage[] images = page.extractImages();
            doc.saveToFile(pdfPath, FileFormat.PDF);
            
            if (images != null) {
                int index = 0; 
                
                for (BufferedImage image : images) {
                    doc.loadFromFile(pdfPath);
                    PdfPageBase page2 = doc.getPages().get(pageIndex);

                    
                    PdfImage pdfImage = PdfImage.fromImage(image);
                    
                    page2.replaceImage(index, pdfImage);

                    index++;
                    doc.saveToFile(pdfPath, FileFormat.PDF);
                    //System.out.println(index);
                }
            } else {
                //System.out.println("No images found on page " + pageIndex);
            }
        }
        
        doc.close();

        File AnonymizatedPdfFile=new File(pdfPath);
        
        return AnonymizatedPdfFile;
        
    }
    
}
