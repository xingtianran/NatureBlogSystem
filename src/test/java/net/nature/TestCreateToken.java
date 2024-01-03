package net.nature;

import net.nature.blog.utils.JwtUtil;

import java.util.HashMap;
import java.util.Map;

public class TestCreateToken {
    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "1164974102981640192");
        map.put("userName", "邢天然");
        // eyJhbGciOiJIUzI1NiJ9.eyJpZCI6IjExNjQ5NzQxMDI5ODE2NDAxOTIiLCJ1c2VyTmFtZSI6IumCouWkqeeEtiIsImV4cCI6MTY5Nzc5NzIwOH0._gj6VvHavoSC2CHtv-iukMIHqVe31dIdpuTt2TD_pdw
        // eyJhbGciOiJIUzI1NiJ9.eyJpZCI6IjExNjQ5NzQxMDI5ODE2NDAxOTIiLCJ1c2VyTmFtZSI6IumCouWkqeeEtiIsImV4cCI6MTY5Nzc5Njg3N30.0IMJ5p7m-ck8ww7WgTGrIWb6ojt7qIuJBD97xWrXFZU
        String token = JwtUtil.createToken(map, 1000 * 60);
        System.out.println(token);
    }
}
