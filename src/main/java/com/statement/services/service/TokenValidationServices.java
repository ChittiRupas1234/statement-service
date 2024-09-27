package com.statement.services.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
@Service
public class TokenValidationServices {

    @Autowired
    private RestTemplate restTemplate;

    private final String USER_SERVICE_URL = "http://localhost:8080/api/auth/validate";

    public boolean validateToken(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    USER_SERVICE_URL,
                    HttpMethod.GET, // or POST, depending on your API
                    entity,
                    String.class
            );
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            System.out.println("Exception during token validation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

/*
@Service
public class TokenValidationService {
    @Autowired
    private RestTemplate restTemplate;

    private final String USER_SERVICE_URL = "http://localhost:8080/api/auth/validate";

    //now adding to the Statement Service application using restTemplate
    */
/*@Autowired
    public TokenValidationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }*//*

    public boolean validateToken(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization",  token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    USER_SERVICE_URL,
                    HttpMethod.POST,// Changed from POST to GET as it seems more appropriate for validation
                    entity,
                    String.class
            );
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            System.out.println("Exception during token validation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}*/
