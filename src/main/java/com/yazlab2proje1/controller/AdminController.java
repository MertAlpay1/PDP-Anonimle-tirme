
package com.yazlab2proje1.controller;

import com.yazlab2proje1.entity.APDF;
import com.yazlab2proje1.entity.FPDF;
import com.yazlab2proje1.entity.Interest;
import com.yazlab2proje1.entity.Keywords;
import com.yazlab2proje1.entity.Log;
import com.yazlab2proje1.entity.Message;
import com.yazlab2proje1.entity.PDF;
import com.yazlab2proje1.entity.RPDF;
import com.yazlab2proje1.entity.Reviewer;
import com.yazlab2proje1.entity.ReviewerPdf;
import com.yazlab2proje1.repository.APDFRepository;
import com.yazlab2proje1.repository.FPDFRepository;
import com.yazlab2proje1.repository.InterestRepository;
import com.yazlab2proje1.repository.KeywordsRepository;
import com.yazlab2proje1.repository.LogRepository;
import com.yazlab2proje1.repository.MessageRepository;
import com.yazlab2proje1.repository.PDFRepository;
import com.yazlab2proje1.repository.RPDFRepository;
import com.yazlab2proje1.repository.ReviewerPdfRepository;
import com.yazlab2proje1.repository.ReviewerRepository;
import com.yazlab2proje1.service.AnonymizationService;
import com.yazlab2proje1.service.KeywordsService;
import com.yazlab2proje1.service.PAnonymizationService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class AdminController {
    
    @Autowired
    private AnonymizationService anonymizationService;
    @Autowired
    private KeywordsService keywordsService;
    @Autowired
    private PAnonymizationService PanonymizationService;
    @Autowired
    private PDFRepository pdfRepository;
    @Autowired
    private LogRepository logRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private KeywordsRepository keywordRepository;
    @Autowired
    private APDFRepository apdfRepository;
    @Autowired
    private ReviewerRepository reviewerRepository;
    @Autowired
    private InterestRepository interestRepository;
    @Autowired
    private ReviewerPdfRepository reviewerPdfRepository;
    @Autowired
    private RPDFRepository RpdfRepository;
    @Autowired
    private FPDFRepository FpdfRepository;
    
    
    
    @GetMapping("/yonetici")
    public String Admin(){
        
        return "yonetici";
    }
    
    @GetMapping("/makaledüzenleme")
    public String GetMakale(Model model){
        List<PDF> findAll = pdfRepository.findAll();
        model.addAttribute("makaleler",findAll);
        
        return "yoneticimakale";
    }
    
    @GetMapping("/yoneticimakale")
    public String YoneticiMakaleYonetim(Model model){
        
        
        return "yoneticimakale";
    }
    
    @GetMapping("/makale/goruntule/{id}")
    public String GoruntuleMakale(@PathVariable int id, Model model) {
        
        Optional<PDF> pdf = pdfRepository.findById(id);

        if (pdf != null) {
            model.addAttribute("makale", pdf.get());  
            return "yoneticimakaledüzenleme";  
        } else {
            model.addAttribute("error", "Makale bulunamadı.");
            return "yonetici"; 
        }
    }
    
    @PostMapping("/alanatama/{id}")
    public String MakaleAlanAtama(@PathVariable int id, Model model){
    
        Optional<PDF> Opdf = pdfRepository.findById(id);
        PDF pdf;
        if(Opdf.isPresent()) { 
            pdf = Opdf.get();
        } else {
            model.addAttribute("error", "Makale bulunamadı.");
            return "yonetici"; 
        }
    
        byte[] dosya = pdf.getPdf();
    
        File tempFile = null;
        try {
            tempFile = File.createTempFile("temp_pdf_", ".pdf");
    
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(dosya);
            }
    
            List<String> keywords = keywordsService.findKeywords(tempFile);
            
            for (String keyword : keywords) {
            //System.out.println(keyword);
            
            if (!keywordRepository.existsByKeywordAndPdf(keyword, pdf)) {
                Keywords Kw = new Keywords();
                Kw.setPdf(pdf);
                Kw.setKeyword(keyword);
                keywordRepository.save(Kw);
            }
        }
            
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "PDF dosyası işlenirken hata oluştu.");
            return "yonetici";
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete(); 
            }
        }
        model.addAttribute("succes", "Makale başarıyla sınıflandırıldı.");
        return "redirect:/makale/goruntule/" + id;
    }
    
    @GetMapping("/AnonimlestirmeMenu/{id}")
    public String AnonMenu(@PathVariable int id ,Model model){
        
       Optional<PDF> pdf = pdfRepository.findById(id);
       Optional<APDF> apdf = apdfRepository.findById(id);
       if(apdf.isPresent()) {
        model.addAttribute("apdfExists", true); 
        } else {
        model.addAttribute("apdfExists", false); 
        }
       
       
        if (pdf != null) {
            model.addAttribute("makale", pdf.get());  
            return "yoneticianonimleştirmemenü";  
        } else {
            model.addAttribute("error", "Makale bulunamadı.");
            return "yonetici"; 
        }
        
    }
    
    @GetMapping("/indiranonim/{id}")
    public ResponseEntity<byte[]> downloadAPDF(@PathVariable int id) {
    Optional<APDF> apdf = apdfRepository.findById(id);
    if (apdf.isEmpty()) {
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    byte[] apdfData = apdf.get().getApdf();

    return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"anonimmakale.pdf\"")
            .body(apdfData);
    }
    
    
    
    @PostMapping("/Anonimleştirme/{id}")
    public String uploadAnonPDF(@PathVariable int id, Model model,
            @RequestParam("AdAnonim") boolean yazarAdiAnonim,
            @RequestParam("IletisimAnonim") boolean yazarIletisimAnonim,
            @RequestParam("KurumAnonim") boolean yazarKurumAnonim,
            @RequestParam("ResimAnonim") boolean yazarResimAnonim){ 
        
        Optional<PDF> Opdf = pdfRepository.findById(id);
        PDF pdf;
        
        /*
        
        Kontrol ekle yani bir makalede bir yorum varsa o makale tekrar anonimleştirilemez
        
        
        */
        
        
        
        if(Opdf.isPresent()) { 
            pdf = Opdf.get();
        } else {
            model.addAttribute("error", "Makale bulunamadı.");
            return "yonetici"; 
        }
        
        byte[] dosya = pdf.getPdf();
    
        File tempFile = null;
       
        
        try {
            tempFile = File.createTempFile("temp_pdf_", ".pdf");    
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(dosya);
            }
        
            
            
        
            File file=anonymizationService.anonymizePDF(tempFile,yazarAdiAnonim,yazarIletisimAnonim,yazarKurumAnonim);
            if(yazarResimAnonim){
            file=PanonymizationService.anonymizePdf(file.getAbsolutePath());
            }
            
            byte[] anonimleştirilmişDosya = Files.readAllBytes(file.toPath());
        
            APDF apdf=new APDF();
            apdf.setId(id);
            apdf.setPdf(pdf);
            apdf.setApdf(anonimleştirilmişDosya);
            apdfRepository.save(apdf);
        
        }
        catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "PDF dosyası işlenirken hata oluştu.");
            return "yonetici";
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete(); 
            }
        }
    
        model.addAttribute("success", "Makale başarıyla anonimleştirildi.");
        return "redirect:/AnonimlestirmeMenu/" + id;
    }
    
    @GetMapping("/adminmesajmenu/{id}")
    public String AdminMessageMenu(@PathVariable int id,Model model){
        
        Optional<PDF> pdf=pdfRepository.findById(id);
        
        if (pdf.isEmpty()) {
        model.addAttribute("error","makale bulunamadı");
        return "redirect:/AnonimlestirmeMenu/" + id;
        }
        
        model.addAttribute("pdf",pdf.get().getId());
        
        model.addAttribute("user","editör");
        
        List<Message> messages=messageRepository.findByPdf(pdf.get());
        
        model.addAttribute("mesaj",messages);
        
        
        return "mesajmenu";
    }
    
    @PostMapping("/adminmesajgönder/{id}")
    public String userMessageSend(@PathVariable int id, @RequestParam("sender") String sender, @RequestParam("text") String text) {
        Optional<PDF> pdf = pdfRepository.findById(id);
    
        if (pdf.isEmpty()) {
            return "redirect:/mesajmenu" ;
        }
    
        Message message = new Message();
        message.setSender(sender);
        message.setText(text);
        message.setPdf(pdf.get());
    
        messageRepository.save(message);
    
        return "redirect:/adminmesajmenu/"+id;
    }
    @GetMapping("/findpdfinterest/{id}")
    public String findkeywordpdftoreviewerinterest(@PathVariable int id,Model model){
        
        Optional<PDF> Opdf=pdfRepository.findById(id);
        PDF pdf=Opdf.get();     
        
        Optional<APDF> Oapdf=apdfRepository.findByPdf(pdf);
         APDF apdf=Oapdf.get(); 
        
        Optional<ReviewerPdf> existingReviewer = reviewerPdfRepository.findByApdf(apdf);
        if (existingReviewer.isPresent()) {
            model.addAttribute("pdf", pdf);
            model.addAttribute("reviewers", Collections.emptyList()); 
            model.addAttribute("error","Bu makale için hakem atanmış");
            return "yoneticihakemsecme";
        }
        
        
        List<Keywords> keywords = keywordRepository.findByPdfId(pdf.getId());
        
        Set<Reviewer> reviewers = new HashSet<>();
        
        for (Keywords keyword : keywords) {
            
        List<Interest> interests = interestRepository.findByinterestname(keyword.getKeyword());
        
        for (Interest interest : interests) {
            
            List<Reviewer> matchingReviewers = reviewerRepository.findByInterestsContaining(interest);
            reviewers.addAll(matchingReviewers);
          }
        }
    
       model.addAttribute("pdf",pdf);
       model.addAttribute("reviewers", reviewers);

        
       return "yoneticihakemsecme";
    }
    
    @PostMapping("/sendpdftoreviewer/{id}")
    public String sendPdfToReviewer(@PathVariable int id, @RequestParam("pdfId") int pdfId, Model model) {
    try {
        Optional<APDF> OApdf = apdfRepository.findById(pdfId);
        Optional<PDF> Opdf = pdfRepository.findById(pdfId);
        Optional<Reviewer> Oreviewer = reviewerRepository.findById(id);

        if (OApdf.isEmpty() || Opdf.isEmpty() || Oreviewer.isEmpty()) {
            model.addAttribute("error", "Belirtilen PDF veya Hakem bulunamadı.");
            return "redirect:/makale/goruntule/" + pdfId;
        }

        APDF apdf = OApdf.get();
        PDF pdf = Opdf.get();
        Reviewer reviewer = Oreviewer.get();


        ReviewerPdf RPdf = new ReviewerPdf();
        RPdf.setReviewer(reviewer);
        RPdf.setApdf(apdf);
        reviewerPdfRepository.save(RPdf);

        Log log = new Log();
        log.setPdf(pdf);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDate = LocalDateTime.now().format(formatter);
        log.setDate(formattedDate);
        
        String logText = "Editör " + formattedDate + " tarihinde  hakem "+reviewer.getId()+ " yollandı.";
        log.setText(logText);
        
        logRepository.save(log);

    } catch (Exception e) {
        model.addAttribute("error","Eklemeye çalıştığınız hakemin bu pdf için isteği var");
        return "redirect:/makale/goruntule/" + pdfId;
    }
      return "redirect:/makale/goruntule/" + pdfId;
    }

    @GetMapping("/adminlog/{id}")
    public String ShowLog(@PathVariable int id,Model model){
        
        Optional<PDF> Opdf = pdfRepository.findById(id);
        PDF pdf=Opdf.get();
        
        List<Log> logs=logRepository.findByPdf(pdf);
        
        model.addAttribute("logs",logs);
        
        return "yoneticilog";
    }
    
    @GetMapping("/geribildirimMenu/{id}")
    public String RpdfMenu(@PathVariable int id,Model model){
        
        Optional<PDF> Opdf = pdfRepository.findById(id);
        PDF pdf=Opdf.get();
        
        Optional<RPDF> Ordpf=RpdfRepository.findByPdf(pdf);
        
        if(!Ordpf.isEmpty()){
            RPDF rpdf = Ordpf.get();
            model.addAttribute("makale",rpdf);
        }
        else{
           model.addAttribute("error", "Makale degerlendirmeye alınmamış.");

        }
        
        Optional<FPDF> Ofdpf=FpdfRepository.findByPdf(pdf);
        if(!Ofdpf.isEmpty()){
            FPDF fpdf = Ofdpf.get();
            model.addAttribute("finalmakale",fpdf);
        }
        
        return "yoneticigeribildirimmenu";
    }
    
    @GetMapping("/indiranonimgeribildirim/{id}")
    public ResponseEntity<byte[]> downloadRPDF(@PathVariable int id) {
    Optional<RPDF> Orpdf = RpdfRepository.findById(id);
    if (Orpdf.isEmpty()) {
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    byte[] apdfData = Orpdf.get().getRpdf();

    return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"anonim_geri_bildirim_makale.pdf\"")
            .body(apdfData);
    }     
    @GetMapping("/indirgeribildirim/{id}")
    public ResponseEntity<byte[]> downloadFPDF(@PathVariable int id) {
    Optional<FPDF> Ofpdf = FpdfRepository.findById(id);
    if (Ofpdf.isEmpty()) {
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    byte[] apdfData = Ofpdf.get().getFpdf();
    
    return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"anonim_geri_bildirim_makale.pdf\"")
            .body(apdfData);
    }      

    @PostMapping("/geribildirimanonimkaldır/{id}")
    public String RpdftoFpdf(@PathVariable int id, Model model) {
    Optional<PDF> Opdf = pdfRepository.findById(id);
    if (Opdf.isPresent()) {
        PDF pdf = Opdf.get();
        Optional<RPDF> Ordpf = RpdfRepository.findByPdf(pdf);

        if (Ordpf.isPresent()) {
            RPDF rpdf = Ordpf.get();
            byte[] dosya = pdf.getPdf();
            byte[] dosya2 = rpdf.getRpdf();

            File temppdf = null;
            File temprpdf=null;

            try {
                temppdf = File.createTempFile("temp_pdf_", ".pdf");
                
                try (FileOutputStream fos = new FileOutputStream(temppdf)) {
                    fos.write(dosya);
                }
                temprpdf= File.createTempFile("temp_rpdf_", ".pdf");
                try (FileOutputStream fos2 = new FileOutputStream(temprpdf)) {
                    fos2.write(dosya2);
                }

                File deAnonymizedFile = anonymizationService.deAnoymizePDF(temppdf, temprpdf);
                deAnonymizedFile=PanonymizationService.deanonymizePdf(temppdf.getAbsolutePath(),deAnonymizedFile.getAbsolutePath());
                
               byte[] deanonimleştirilmişDosya = Files.readAllBytes(deAnonymizedFile.toPath());
                
                
        
               FPDF fpdf=new FPDF();
               fpdf.setId(id);
               fpdf.setPdf(pdf);
               fpdf.setFpdf(deanonimleştirilmişDosya);
               FpdfRepository.save(fpdf);

               Log log = new Log();
               log.setPdf(pdf);
               
               DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
               String formattedDate = LocalDateTime.now().format(formatter);
               log.setDate(formattedDate);
               
               String logText = "Editör " + formattedDate + " tarihinde  Yazara gönderildi.";
               log.setText(logText);
               
               logRepository.save(log);
               
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
      return "redirect:/geribildirimMenu/"+id;
    }

    
    

}