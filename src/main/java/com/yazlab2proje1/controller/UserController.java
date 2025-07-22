package com.yazlab2proje1.controller;

import com.yazlab2proje1.entity.FPDF;
import com.yazlab2proje1.entity.Log;
import com.yazlab2proje1.entity.Message;
import com.yazlab2proje1.entity.PDF;
import com.yazlab2proje1.entity.Review;
import com.yazlab2proje1.repository.FPDFRepository;
import com.yazlab2proje1.repository.LogRepository;
import com.yazlab2proje1.repository.MessageRepository;
import com.yazlab2proje1.repository.PDFRepository;
import com.yazlab2proje1.repository.ReviewRepository;
import com.yazlab2proje1.service.AnonymizationService;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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
public class UserController {
    
    @Autowired
    private AnonymizationService anonymizationService;
    @Autowired
    private PDFRepository pdfRepository;
    @Autowired
    private LogRepository logRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private FPDFRepository FpdfRepository;
    
    @GetMapping("/")
    public String MainPage(){
        
        return "makalesistemi";
    } 
    
    @PostMapping("/makalesistemi")
    public String UserUpload(@RequestParam("e-posta") String email, @RequestParam("pdf") MultipartFile file,Model model) {
    try {
        byte[] pdfBytes = file.getBytes();
        
        PDF pdf = new PDF();
        pdf.setPdf(pdfBytes); 
        pdf.setEmail(email);  
        pdfRepository.save(pdf);
        
        Log log=new Log();
        log.setPdf(pdf);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDate = LocalDateTime.now().format(formatter);
        log.setDate(formattedDate);
        
        String LogText="Editöre "+formattedDate+" tarihinde geldi.";
        
        log.setText(LogText);
        
        logRepository.save(log);
        
        model.addAttribute("success", "Makaleniniz başarıyla yüklendi.Makale numaranız :"+pdf.getId());
        
        
        return "makalesistemi";
    } catch (Exception e) {
        e.printStackTrace();
        model.addAttribute("error", "Yanlış makale numarası veya e-posta adresi.");

        return "error"; 
       }
    }
   
    @GetMapping("/bulmakale")
    public String MakaleBul(@RequestParam("id") int id, @RequestParam("email") String email, Model model) {
    Optional<PDF> Opdf = pdfRepository.findById(id);
    
    if (Opdf.isEmpty() || !Opdf.get().getEmail().equals(email)) {
        model.addAttribute("error", "Yanlış makale numarası veya e-posta adresi.");
        return "makalesistemi"; 
    }
    
    PDF pdf=Opdf.get();
    
    model.addAttribute("pdf",pdf);
    
    Optional<FPDF> Ofpdf=FpdfRepository.findByPdf(pdf);
    if(!Ofpdf.isEmpty()){
        FPDF fpdf=Ofpdf.get();
        model.addAttribute("finalmakale",fpdf);
    }
    
    
    return "makaledurumsorgulama"; 
    }
 

    @GetMapping("/makaledurumsorgulama/{id}")
    public String makaleMenu(@PathVariable int id,Model model){
        Optional<PDF> pdf = pdfRepository.findById(id);
        
        if (pdf.isEmpty()) {
        model.addAttribute("error", "Yanlış makale numarası.");
        return "makalesistemi"; 
    }
        
        model.addAttribute("pdf", pdf.get());        
        
        return "makaledurumsorgulama";
    }
    
    @GetMapping("/indir/{id}")
    public ResponseEntity<byte[]> downloadPDF(@PathVariable int id) {
    Optional<PDF> pdf = pdfRepository.findById(id);
    if (pdf.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    byte[] pdfData = pdf.get().getPdf();

    return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"makale.pdf\"")
            .body(pdfData);
    }


    /*
    
    @PostMapping()
    public String UploadPDF(@RequestParam("pdf") MultipartFile file){
       
        
        return "/makaledurumsorgulama";
        
    }
    */

    @GetMapping("/usermesajmenu/{id}")
    public String UserMessageMenu(@PathVariable int id,Model model){
        
        Optional<PDF> pdf=pdfRepository.findById(id);
        
        if (pdf.isEmpty()) {
        model.addAttribute("error","makale bulunamadı");
        return "makaledurumsorgulama";
        }
        
        model.addAttribute("pdf",pdf.get().getId());
        
        model.addAttribute("user","yazar");
        
        List<Message> messages=messageRepository.findByPdf(pdf.get());
        
        model.addAttribute("mesaj",messages);
        
        return "mesajmenu";
    }
    
    @PostMapping("/usermesajgönder/{id}")
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
    
        return "redirect:/usermesajmenu/"+id;
    }
    
    @PostMapping("/makaledüzenle/{id}")
    public String MakaleDüzenle(@PathVariable int id,@RequestParam("pdf") MultipartFile file,Model model) throws IOException{
        Optional<PDF> Opdf = pdfRepository.findById(id);
        PDF pdf = Opdf.get();
        
        //Kontrol işlemleri ekle
        List<Review> reviews =reviewRepository.findByPdf(pdf);
        if(!reviews.isEmpty()){
            
            
            
          model.addAttribute("error","makale çoktan değerlendirmeye alındı.Değişim yapılamaz");
            //System.out.println("aaaaaaa");
          return "redirect:/makaledurumsorgulama/" + id;

        }
        
        
        byte[] pdfBytes = file.getBytes();
        
        pdf.setPdf(pdfBytes);
        
        pdfRepository.save(pdf);
        
        /*
        Optional<PDF> pdf = pdfRepository.findById(id);
        
        //Kontrol işlemleri ekle
        List<Review> reviews =reviewRepository.findByPdf(pdf);
        
        byte[] pdfBytes = file.getBytes();
        
        pdf.get().setPdf(pdfBytes);
        
        pdfRepository.save(pdf.get());
        */
        
        return "redirect:/makaledurumsorgulama/" + id;
    }
    
    
}
