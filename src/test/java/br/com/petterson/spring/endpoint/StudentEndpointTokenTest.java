package br.com.petterson.spring.endpoint;

import br.com.petterson.spring.model.Student;
import br.com.petterson.spring.repository.StudentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableAutoConfiguration
public class StudentEndpointTokenTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @LocalServerPort
    private int port;
    @MockBean
    private StudentRepository studentRepository;
    @Autowired
    private MockMvc mockMvc;
    private HttpEntity<Void> protectedHeader;
    private HttpEntity<Void> adminHeader;
    private HttpEntity<Void> wrongHeader;

    @Before
    public void configProtectedHeaders() {
        String str = "{\"username\": \"protected\", \"password\": \"123456\"}";
        HttpHeaders headers = restTemplate.postForEntity("/login", str, String.class).getHeaders();
        this.protectedHeader = new HttpEntity<>(headers);
    }

    @Before
    public void configAdminHeaders() {
        String str = "{\"username\": \"petterson\", \"password\": \"123456\"}";
        HttpHeaders headers = restTemplate.postForEntity("/login", str, String.class).getHeaders();
        this.adminHeader = new HttpEntity<>(headers);
    }

    @Before
    public void configWrongHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "122111");
        this.wrongHeader = new HttpEntity<>(headers);
    }


    static class Config {

        @Bean
        public RestTemplateBuilder restTemplateBuilder() {
            return new RestTemplateBuilder().basicAuthentication("petterson", "123456");
        }
    }

    @Before
    public void setup() {
        Student student = new Student(1L, "teste1", "teste1@hotmail.com");
        BDDMockito.when(studentRepository.findById(student.getId())).thenReturn(
            Optional.of(student));
    }

    @Test
    public void listStudentsWhenTokenIsIncorrectShouldReturnStatusCode403() {
        ResponseEntity<String> response = restTemplate
            .exchange("/v1/protected/students/", HttpMethod.GET, wrongHeader, String.class);
        Assertions.assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void getStudentsByIdWhenTokenIsIncorrectShouldReturnStatusCode403() {
        ResponseEntity<String> response = restTemplate
            .exchange("/v1/protected/students/1", HttpMethod.GET, wrongHeader, String.class);
        Assertions.assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void listStudentsWhenTokenIsCorrectShouldReturnStatusCode200() {
        ResponseEntity<Student> response = restTemplate
            .exchange("/v1/protected/students/", HttpMethod.GET, protectedHeader, Student.class);
        Assertions.assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    public void getStudentsByIdWhenTokenIsCorrectShouldReturnStatusCode200() {
        ResponseEntity<Student> response = restTemplate
            .exchange("/v1/protected/students/1", HttpMethod.GET, protectedHeader, Student.class);
        Assertions.assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    public void getStudentsByIdWhenTokenIsCorrectAndStudentDoesNotExistShouldReturnStatusCode404() {
        ResponseEntity<Student> response = restTemplate
            .exchange("/v1/protected/students/-1", HttpMethod.GET, protectedHeader, Student.class);
        Assertions.assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    public void deleteWhenUserHasRoleAdminAndStudentExistsShouldReturnStatusCode200() {
        BDDMockito.doNothing().when(studentRepository).deleteById(1L);
        ResponseEntity<String> exchange = restTemplate
            .exchange("/v1/admin/students/1", HttpMethod.DELETE, adminHeader, String.class);
        Assertions.assertThat(exchange.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    public void deleteWhenUserHasRoleAdminAndStudentDoesNotExistsShouldReturnStatusCode404()
        throws Exception {
        String token = adminHeader.getHeaders().get("Authorization").get(0);
        BDDMockito.doThrow(new EmptyResultDataAccessException(-1)).when(studentRepository)
            .deleteById(-1L);
        mockMvc.perform(MockMvcRequestBuilders
            .delete("/v1/admin/students/{id}", -1L).header("Authorization", token))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void deleteWhenUserHasRoleAdminShouldReturnStatusCode403() throws Exception {
        String token = protectedHeader.getHeaders().get("Authorization").get(0);
        BDDMockito.doThrow(new EmptyResultDataAccessException(1)).when(studentRepository)
            .deleteById(1L);
        mockMvc.perform(MockMvcRequestBuilders
            .delete("/v1/admin/students/{id}", 1L).header("Authorization", token))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void createWhenNameIsNullShouldReturnStatusCode400BadRequest() throws Exception {
        Student student = new Student(3L, null, "teste1@hotmail.com");
        BDDMockito.when(studentRepository.save(student)).thenReturn(student);

        ResponseEntity<String> response = restTemplate
            .exchange("/v1/admin/students/", HttpMethod.POST,
                new HttpEntity<>(student, adminHeader.getHeaders()), String.class);
        Assertions.assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    public void createShouldPersistDataAndReturnStatusCode201() throws Exception {
        Student student = new Student(3L, "Sam", "teste1@hotmail.com");
        BDDMockito.when(studentRepository.save(student)).thenReturn(student);

        ResponseEntity<Student> response = restTemplate
            .exchange("/v1/admin/students/", HttpMethod.POST,
                new HttpEntity<>(student, adminHeader.getHeaders()), Student.class);
        Assertions.assertThat(response.getStatusCodeValue()).isEqualTo(201);
        Assertions.assertThat(response.getBody().getId()).isNotNull();
    }

    @Test
    public void updateWhenNameIsExistsShouldReturnStatusCode200() throws Exception {
        String token = adminHeader.getHeaders().get("Authorization").get(0);
        ObjectMapper mapper = new ObjectMapper();
        Student student = new Student(3L, "Sam", "teste1@hotmail.com");
        BDDMockito.when(studentRepository.save(student)).thenReturn(student);

        mockMvc.perform(
            MockMvcRequestBuilders.put("/v1/admin/students/").header("Authorization", token)
                .content(mapper.writeValueAsString(student))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void updateWhenNameIsNullShouldReturnStatusCode404() throws Exception {
        String token = adminHeader.getHeaders().get("Authorization").get(0);
        ObjectMapper mapper = new ObjectMapper();
        Student student = new Student();
        BDDMockito.when(studentRepository.save(student)).thenReturn(student);

        mockMvc.perform(
            MockMvcRequestBuilders.put("/v1/admin/students/").header("Authorization", token)
                .content(mapper.writeValueAsString(student))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

}
