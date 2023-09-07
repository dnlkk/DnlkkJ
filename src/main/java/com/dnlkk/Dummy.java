package com.dnlkk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class Dummy {
    private String text;

    public Dummy() {
        this.text = "silly!";
    }    
}