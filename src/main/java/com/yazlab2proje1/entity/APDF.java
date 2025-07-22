
package com.yazlab2proje1.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;

@Entity
public class APDF {
    
    @Id
    private int id;
    
    @Lob
    private byte[] Apdf;
    
    @ManyToOne
    private PDF pdf; 

    public APDF() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte[] getApdf() {
        return Apdf;
    }

    public void setApdf(byte[] Apdf) {
        this.Apdf = Apdf;
    }

    public PDF getPdf() {
        return pdf;
    }

    public void setPdf(PDF pdf) {
        this.pdf = pdf;
    }
    
    
    
    
    
}
