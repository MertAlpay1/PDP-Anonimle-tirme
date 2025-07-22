
package com.yazlab2proje1.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;

@Entity
public class FPDF {
    
    @Id
    private int id;
    
    @Lob
    private byte[] Fpdf;
    
    @ManyToOne
    private PDF pdf; 

    public FPDF() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getFpdf() {
        return Fpdf;
    }

    public void setFpdf(byte[] Fpdf) {
        this.Fpdf = Fpdf;
    }

    public PDF getPdf() {
        return pdf;
    }

    public void setPdf(PDF pdf) {
        this.pdf = pdf;
    }
    
    
    
    
    
}
