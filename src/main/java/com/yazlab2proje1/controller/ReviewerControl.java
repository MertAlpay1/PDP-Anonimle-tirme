package com.yazlab2proje1.controller;

import com.yazlab2proje1.entity.APDF;
import com.yazlab2proje1.entity.Log;
import com.yazlab2proje1.entity.PDF;
import com.yazlab2proje1.entity.RPDF;
import com.yazlab2proje1.entity.Review;
import com.yazlab2proje1.entity.Reviewer;
import com.yazlab2proje1.entity.ReviewerPdf;
import com.yazlab2proje1.repository.APDFRepository;
import com.yazlab2proje1.repository.LogRepository;
import com.yazlab2proje1.repository.PDFRepository;
import com.yazlab2proje1.repository.RPDFRepository;
import com.yazlab2proje1.repository.ReviewRepository;
import com.yazlab2proje1.repository.ReviewerPdfRepository;
import com.yazlab2proje1.repository.ReviewerRepository;
import com.yazlab2proje1.service.AddCommentService;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReviewerControl {
    
    
    
    @Autowired
    private PDFRepository pdfRepository;
    @Autowired
    private ReviewerRepository reviewerRepository;
    @Autowired
    private APDFRepository apdfRepository;
    @Autowired
    private ReviewerPdfRepository reviewerPdfRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private RPDFRepository RpdfRepository;
    @Autowired
    private LogRepository logRepository;
    
    
    @GetMapping("/degerlendirici/{id}")
    public String selectPdf(@PathVariable int id,Model model){
        
        Optional<Reviewer> reviewer=reviewerRepository.findById(id);
        
        if(reviewer.isEmpty()){
            model.addAttribute("error","Böyle bir hakem bulunamadı.");
        }
        
        model.addAttribute("hakem",reviewer.get());
        
        List<ReviewerPdf> reviewerPdfs =reviewerPdfRepository.findByReviewer(reviewer.get());
        
        if(reviewerPdfs.isEmpty()){
            model.addAttribute("error","Henüz bir değerlendirmeniz gereken bir makale bulunmamakta.");
        }
        
        List<APDF> reviewersPdf = reviewerPdfs.stream().map(ReviewerPdf::getApdf).toList();
        
        model.addAttribute("makale",reviewersPdf);
        
        return "degerlendiricisecim";
    }
    
    @GetMapping("/selectrpdf/{id}")
    public String ReviewerMenu(@PathVariable int id,@RequestParam("hakemId") int reviewerId,Model model){
        
        Optional<Reviewer> reviewer=reviewerRepository.findById(reviewerId);
        
        Optional<PDF> Opdf =pdfRepository.findById(id);
        
        PDF pdf=Opdf.get();
        
        List<Review> reviews =reviewRepository.findByPdf(pdf);
        
        model.addAttribute("makale",pdf);
        
        model.addAttribute("yorumlar",reviews);
        
        model.addAttribute("hakem",reviewer.get());
        
        return "degerlendiricimenu";
    }
    @PostMapping("/yorumekle/{id}")
    public String AddComment(@PathVariable int id, 
                         @RequestParam("yorum") String yorumText, 
                         @RequestParam("hakemId") int hakemId, 
                         Model model) {
    try {
        Optional<PDF> Opdf = pdfRepository.findById(id);
        Optional<Reviewer> Ohakem = reviewerRepository.findById(hakemId);

        if (Opdf.isEmpty() || Ohakem.isEmpty()) {
            model.addAttribute("error", "Makale veya hakem bulunamadı.");
            return "redirect:/hakem/panel/" + id; 
        }

        PDF pdf = Opdf.get();
        Reviewer hakem = Ohakem.get();

        Review review = new Review();
        review.setPdf(pdf);
        review.setReviewer(hakem);
        review.setReview(yorumText);
        reviewRepository.save(review);
        
        
        List<Review> reviews = reviewRepository.findByPdf(pdf);
        
        Optional<APDF> AOpdf = apdfRepository.findByPdf(pdf);

        AddCommentService addCommentService = new AddCommentService();
        File updatedPdf = addCommentService.addComment(AOpdf.get(),reviews );
        byte[] pdfBytes =  Files.readAllBytes(updatedPdf.toPath());
         
         
        Optional<RPDF> Orpdf=RpdfRepository.findByPdf(pdf);
        if(Orpdf.isEmpty()){
            RPDF rpdf=new RPDF();
            rpdf.setId(pdf.getId());
            rpdf.setPdf(pdf);
            rpdf.setRpdf(pdfBytes);
            RpdfRepository.save(rpdf);
        }
        else{
            RPDF rpdf=Orpdf.get();
            rpdf.setRpdf(pdfBytes);
            RpdfRepository.save(rpdf);
        }
        
        Log log = new Log();
        log.setPdf(pdf);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String formattedDate = LocalDateTime.now().format(formatter);
        log.setDate(formattedDate);
        
        String logText = "Editör "+hakemId+ " " + formattedDate + " tarihinde cevap  verdi.";
        log.setText(logText);
        
        logRepository.save(log);
        
        
    } catch (Exception e) {
        model.addAttribute("error", "Yorum eklenirken hata oluştu: " + e.getMessage());
        System.out.println(e);
    }

    return "redirect:/degerlendirici/"+hakemId ;
    }

    
}
