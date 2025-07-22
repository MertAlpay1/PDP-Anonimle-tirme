package com.yazlab2proje1.service;

import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.crypto.*;
import java.util.regex.*;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IPdfTextLocation;
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.pdfcleanup.*;
import com.itextpdf.pdfcleanup.autosweep.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.util.*;
import static java.awt.SystemColor.text;
import javax.crypto.spec.SecretKeySpec;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;


@Service
public class AnonymizationService {

    private  Set<String> authorNames = new HashSet<>();
    private  Set<String> emails = new HashSet<>();
    private  Set<String> organizations = new HashSet<>();
    private  List<String> all = new ArrayList<>();
    private  List<String> Eall = new ArrayList<>();

    private  SecretKey secretKey;

    public  File anonymizePDF(File file,boolean namecheck,boolean emailcheck,boolean organizationcheck) {
        try {
            
            authorNames.clear();
            emails.clear();
            organizations.clear();
            all.clear();
            Eall.clear();
            
            //Anonimleştirme için giriş değeri alıcak temp değerine kaydedicek
            //DeAnonimleştirme için hem giriş değerini alıcak ve anonimleştirilmiş pdf alınacak , temp alınır mı bilmem
            //ama giriş pdfinden kelimeler alınıp şifrelenecek ve bu şifrelenmiş metinler anonimleştirilmiş pdfden aranır ve
            //decrypt edilir ve yerleştirilir.Ayrıca generateAESKey fonksinyonu için ayrı bir sınıf oluştur
            
            generateAESKey();

            PDDocument document = PDDocument.load(file);
            PDFTextStripper pdfStripper = new PDFTextStripper();
            
            pdfStripper.setStartPage(1);
            pdfStripper.setEndPage(1);
            String text = pdfStripper.getText(document);
            document.close();

            String[] lines = text.split("\n");

            for (String line : lines) {
                // "Abstract" veya "introduction"  gibi bölüme geçtiyse dur
                if (line.toLowerCase().contains("abstract") || line.toLowerCase().contains("introduction") ) {
                    break;
                }
                    if(namecheck){
                    extractNames(line);
                    }
                    if(emailcheck){
                    extractEmails(line);
                    }
                    if(organizationcheck){
                    extractOrganizations(line);
                    }
            }

            //all.add("EEG");
            //all.add("LTSM");
            
            for(String a:emails){
                all.add(a);
            }
            for(String a:organizations){
                all.add(a);
            }
            for(String a:authorNames){
                all.add(a);
            }
            
            //System.out.println("çıj");
            File tempFile = File.createTempFile("temp_pdf_", ".pdf");
            Files.copy(file.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            File tempFile2 = File.createTempFile("temp_pdf_", ".pdf");
            Files.copy(file.toPath(), tempFile2.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            
            for (String keyword : all) {
                PdfReader reader2 = new PdfReader(tempFile);
                PdfWriter writer = new PdfWriter(tempFile2);
                PdfDocument pdfDocument = new PdfDocument(reader2, writer);
                encryptPDF(keyword, pdfDocument,encrypt(keyword));
                pdfDocument.close();
                reader2.close();
                writer.close();
                //System.out.println("girdi");
                Files.copy(tempFile2.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            }
            
            
            /*
            
            for(String kelime:all){
                
               Eall.add(encrypt(kelime)) ;
                
            }
            
            
            
            
            for (String keyword : Eall) {
                PdfReader reader2 = new PdfReader(tempFile);
                PdfWriter writer = new PdfWriter(tempFile2);
                PdfDocument pdfDocument = new PdfDocument(reader2, writer);
                encryptPDF(keyword, pdfDocument,decrypt(keyword));
                pdfDocument.close();
                reader2.close();
                writer.close();
                System.out.println("girdi");
                Files.copy(tempFile2.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            }
            
            
            */
            
            //System.out.println("bitti");
            

            return tempFile2;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null ;
        }
    }

    

    private  void extractNames(String text) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        CoreDocument document = new CoreDocument(text);
        pipeline.annotate(document);

        if(text.contains("MAJITHIA TEJAS VINODBHAI")){
            all.add("majithia tejas vinodbhai");
        }
        if(text.contains("Divyashikha Sethia")){
            authorNames.add("divyashikha sethia");
        }
        
        
        for (CoreEntityMention em : document.entityMentions()) {
            if (em.entityType().equals("PERSON")) {
                
                if(em.text().contains("India")) {break;}
                
                authorNames.add(em.text());
                
                //System.out.println("Yazar Adı: " + em.text());
            }
        }
    }

    private  void extractEmails(String text) {
        //Pattern emailPattern = Pattern.compile("([a-zA-Z0-9+._-]+@[a-zA-Z0-9._-]+\\.[a-zA-Z0-9_-]+)");
        Pattern emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher emailMatcher = emailPattern.matcher(text);

        while (emailMatcher.find()) {
            emails.add(emailMatcher.group());
            //System.out.println("E-posta Adresi: " + emailMatcher.group());
        }
    }

    private  void extractOrganizations(String text) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        CoreDocument document = new CoreDocument(text);
        pipeline.annotate(document);

        for (CoreEntityMention em : document.entityMentions()) {
            if (em.entityType().equals("ORGANIZATION")) {
                String textCheck = em.text();
                if (textCheck.contains("University") || textCheck.contains("Institute")) {
                    organizations.add(em.text());
                    System.out.println("Kurum Adı: " + em.text()); 
                }
            }
        }
    }

    private  void encryptPDF(String kelime,PdfDocument pdf,String Ekelime) throws IOException {
        /*
       CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
       strategy.add(new RegexBasedCleanupStrategy(kelime).setRedactionColor(ColorConstants.WHITE));
       PdfCleaner.autoSweepCleanUp(pdf, strategy);
       */
        try {
        //System.out.println("Anonimleştirilen kelime: " + kelime);

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        
        strategy.add(new RegexBasedCleanupStrategy(kelime).setRedactionColor(ColorConstants.WHITE));
        
        
        PdfCleaner.autoSweepCleanUp(pdf, strategy);
        
        strategy.add(new RegexBasedCleanupStrategy("(?i)" + Pattern.quote(kelime)).setRedactionColor(ColorConstants.WHITE));
        
        PdfCleaner.autoSweepCleanUp(pdf, strategy);
        String temp=kelime;
        String flexiblePattern = "(?i)" + temp.replace(" ", "\\s*").replace(".", "\\.");
        strategy.add(new RegexBasedCleanupStrategy(flexiblePattern).setRedactionColor(ColorConstants.WHITE));
        
        PdfCleaner.autoSweepCleanUp(pdf, strategy);
        
        temp=kelime;
        String dottedPattern = "(?i)" + temp.replace(" ", ".");
        strategy.add(new RegexBasedCleanupStrategy(dottedPattern).setRedactionColor(ColorConstants.WHITE));

        PdfCleaner.autoSweepCleanUp(pdf, strategy);
        
        temp=kelime;
        String splitPattern = temp.replaceAll("([a-z])([A-Z])", "$1 $2"); // Küçük harf ve büyük harfi ayır
        strategy.add(new RegexBasedCleanupStrategy("(?i)" + splitPattern).setRedactionColor(ColorConstants.WHITE));
        PdfCleaner.autoSweepCleanUp(pdf, strategy);
        

        for (IPdfTextLocation location : strategy.getResultantLocations()) {
            PdfPage page = pdf.getPage(location.getPageNumber() + 1);
            PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(), page.getDocument());
            Canvas canvas = new Canvas(pdfCanvas, location.getRectangle());
            canvas.add(new Paragraph(Ekelime).setFontSize(1).setMarginTop(0f));
        }
    } catch (Exception e) {
        System.err.println("Hata oluştu: " + e.getMessage());
        e.printStackTrace();
    }
}
    
    private  void decryptPDF(String kelime,PdfDocument pdf,String Ekelime) throws IOException {
        /*
       CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
       strategy.add(new RegexBasedCleanupStrategy(kelime).setRedactionColor(ColorConstants.WHITE));
       PdfCleaner.autoSweepCleanUp(pdf, strategy);
       */
        try {
        //System.out.println("Anonimleştirilen kelime: " + kelime);

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        
        strategy.add(new RegexBasedCleanupStrategy(kelime).setRedactionColor(ColorConstants.WHITE));
        
        
        PdfCleaner.autoSweepCleanUp(pdf, strategy);
        
        strategy.add(new RegexBasedCleanupStrategy("(?i)" + Pattern.quote(kelime)).setRedactionColor(ColorConstants.WHITE));
        
        PdfCleaner.autoSweepCleanUp(pdf, strategy);
        String temp=kelime;
        String flexiblePattern = "(?i)" + temp.replace(" ", "\\s*").replace(".", "\\.");
        strategy.add(new RegexBasedCleanupStrategy(flexiblePattern).setRedactionColor(ColorConstants.WHITE));
        
        PdfCleaner.autoSweepCleanUp(pdf, strategy);
        
        temp=kelime;
        String dottedPattern = "(?i)" + temp.replace(" ", ".");
        strategy.add(new RegexBasedCleanupStrategy(dottedPattern).setRedactionColor(ColorConstants.WHITE));

        PdfCleaner.autoSweepCleanUp(pdf, strategy);
        
        temp=kelime;
        String splitPattern = temp.replaceAll("([a-z])([A-Z])", "$1 $2"); // Küçük harf ve büyük harfi ayır
        strategy.add(new RegexBasedCleanupStrategy("(?i)" + splitPattern).setRedactionColor(ColorConstants.WHITE));
        PdfCleaner.autoSweepCleanUp(pdf, strategy);
        

        for (IPdfTextLocation location : strategy.getResultantLocations()) {
            PdfPage page = pdf.getPage(location.getPageNumber() + 1);
            PdfCanvas pdfCanvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(), page.getDocument());
            Canvas canvas = new Canvas(pdfCanvas, location.getRectangle());
            canvas.add(new Paragraph(Ekelime).setFontSize(3).setMarginTop(0f));
        }
    } catch (Exception e) {
        System.err.println("Hata oluştu: " + e.getMessage());
        e.printStackTrace();
    }
}
    
    private  void generateAESKey() throws Exception {
    String keyString = "secretprojectadamhill"; // Sabit bir anahtar belirle
    byte[] keyBytes = keyString.getBytes("UTF-8");
    secretKey = new SecretKeySpec(Arrays.copyOf(keyBytes, 16), "AES");
}

    
    
    private  String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    private  String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedData, "UTF-8");
    }
    
    public File deAnoymizePDF(File filepdf,File filerpdf){
        
        try {
            
            authorNames.clear();
            emails.clear();
            organizations.clear();
            all.clear();
            Eall.clear();
            
            //Anonimleştirme için giriş değeri alıcak temp değerine kaydedicek
            //DeAnonimleştirme için hem giriş değerini alıcak ve anonimleştirilmiş pdf alınacak , temp alınır mı bilmem
            //ama giriş pdfinden kelimeler alınıp şifrelenecek ve bu şifrelenmiş metinler anonimleştirilmiş pdfden aranır ve
            //decrypt edilir ve yerleştirilir.Ayrıca generateAESKey fonksinyonu için ayrı bir sınıf oluştur
            
            generateAESKey();

            PDDocument document = PDDocument.load(filepdf);
            PDFTextStripper pdfStripper = new PDFTextStripper();
            
            pdfStripper.setStartPage(1);
            pdfStripper.setEndPage(1);
            String text = pdfStripper.getText(document);
            document.close();

            String[] lines = text.split("\n");

            for (String line : lines) {
                // "Abstract" veya "introduction"  gibi bölüme geçtiyse dur
                if (line.toLowerCase().contains("abstract") || line.toLowerCase().contains("introduction") ) {
                    break;
                }
                    extractNames(line);
                    extractEmails(line);
                    extractOrganizations(line);
                    
            }

            for(String a:emails){
                all.add(a);
            }
            for(String a:organizations){
                all.add(a);
            }
            for(String a:authorNames){
                all.add(a);
            }
            
           // System.out.println("çıj");
            File tempFile = File.createTempFile("temp_pdf_", ".pdf");
            Files.copy(filerpdf.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            File tempFile2 = File.createTempFile("temp_rpdf_", ".pdf");
            Files.copy(filerpdf.toPath(), tempFile2.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            
            for(String kelime:all){
                
               Eall.add(encrypt(kelime)) ;
                
            }
            
            
            for (String keyword : Eall) {
                PdfReader reader2 = new PdfReader(tempFile);
                PdfWriter writer = new PdfWriter(tempFile2);
                PdfDocument pdfDocument = new PdfDocument(reader2, writer);
                decryptPDF(keyword, pdfDocument,decrypt(keyword));
                pdfDocument.close();
                reader2.close();
                writer.close();
                //System.out.println("girdi");
                Files.copy(tempFile2.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            }
            
            
           // System.out.println("bitti");
            

            return tempFile2;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null ;
        }
    }
    
    
}
