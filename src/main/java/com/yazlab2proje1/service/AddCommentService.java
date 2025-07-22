package com.yazlab2proje1.service;

import com.yazlab2proje1.entity.APDF;
import com.yazlab2proje1.entity.Review;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class AddCommentService {

    

    public File addComment(APDF apdf,List<Review> reviews) throws IOException {
        File outputFile = null;
        try {

        PDDocument document = PDDocument.load(apdf.getApdf());

        PDPage newPage = new PDPage();
        document.addPage(newPage);
        // Yorumları yeni sayfaya ekle
        PDPageContentStream contentStream = new PDPageContentStream(document, newPage);
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(100, 700);
        contentStream.showText("Hakem Yorumlari:");
        contentStream.newLineAtOffset(0, -20);

        for (Review review : reviews) {
            
            
            String text=review.getReview();
            System.out.println(text);
            text = text.replace("ı", "i").replace("ğ", "g").replace("ş","s").replace("İ", "I");
            System.out.println(text);
            
            contentStream.showText(review.getReviewer().getName() + ": " +text );
            contentStream.newLineAtOffset(0, -30);
        }

        contentStream.endText();
        contentStream.close();

        outputFile = new File("updated_pdf_with_comments.pdf");
        document.save(outputFile);
        document.close();

        }catch (IOException e) {
            e.printStackTrace();
        }
       

        return outputFile;
        }
        
   
}
