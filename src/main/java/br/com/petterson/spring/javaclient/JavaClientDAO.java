package br.com.petterson.spring.javaclient;

import br.com.petterson.spring.handler.RestResponseExceptionHandler;
import br.com.petterson.spring.model.PageableResponse;
import br.com.petterson.spring.model.Student;
import java.util.List;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class JavaClientDAO {


    private RestTemplate restTemplate = new RestTemplateBuilder()
        .rootUri("http://localhost:8080/v1/protected/students")
        .basicAuthentication("santos", "123456")
        .errorHandler(new RestResponseExceptionHandler())
        .build();

    private RestTemplate restTemplateAdmin = new RestTemplateBuilder()
        .rootUri("http://localhost:8080/v1/admin/students")
        .basicAuthentication("petterson", "123456")
        .errorHandler(new RestResponseExceptionHandler())
        .build();

    public Student findById(long id) {
        return restTemplate.getForObject("/{id}", Student.class, id);
//        ResponseEntity<Student> forEntity = restTemplate.getForEntity("/{id}", Student.class, 10);
    }

    public List<Student> listAll() {
        ResponseEntity<PageableResponse<Student>> exchange = restTemplate
            .exchange("/", HttpMethod.GET, null,
                new ParameterizedTypeReference<PageableResponse<Student>>() {
                });
        return exchange.getBody().getContent();
    }

    public Student save(Student student) {
        ResponseEntity<Student> exchangePost = restTemplateAdmin.exchange("/",
            HttpMethod.POST, new HttpEntity<>(student, createJsonHeader()),
            Student.class);

        return exchangePost.getBody();
    }

    public void update(Student student) {
        restTemplateAdmin.put("/", student);
    }

    public void delete(long id) {
        restTemplateAdmin.delete("/{id}", id);
    }

    private static HttpHeaders createJsonHeader() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return httpHeaders;
    }

}
