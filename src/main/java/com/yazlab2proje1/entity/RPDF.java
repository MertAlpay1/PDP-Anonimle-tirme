
package com.yazlab2proje1.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;

@Entity
public class RPDF {
    
    @Id
    private int id;
    
    @Lob
    private byte[] Rpdf;
    
    @ManyToOne
    private PDF pdf; 

    public RPDF() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getRpdf() {
        return Rpdf;
    }

    public void setRpdf(byte[] Apdf) {
        this.Rpdf = Apdf;
    }

    public PDF getPdf() {
        return pdf;
    }

    public void setPdf(PDF pdf) {
        this.pdf = pdf;
    }
    
    
    
}
