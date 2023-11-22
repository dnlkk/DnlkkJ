package com.dnlkk.security;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class Security {
    @Getter
    @Setter
    private static CORSPolicy corsPolicy;
}
