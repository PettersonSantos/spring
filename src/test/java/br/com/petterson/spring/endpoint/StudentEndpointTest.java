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
public class StudentEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @LocalServerPort
    private int port;
    @MockBean
    private StudentRepository studentRepository;
    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
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
    public void listStudentsWhenUsernameAndPasswordAreIncorrectShouldReturnStatusCode401() {
        restTemplate = restTemplate.withBasicAuth("1", "1");
        ResponseEntity<String> response = restTemplate
            .getForEntity("/v1/protected/students/", String.class);
        Assertions.assertThat(response.getStatusCodeValue()).isEqualTo(401);
    }

    @Test
    public void getStudentsByIdWhenUsernameAndPasswordAreIncorrectShouldReturnStatusCode401() {
        restTemplate = restTemplate.withBasicAuth("1", "1");
        ResponseEntity<String> response = restTemplate
            .getForEntity("/v1/protected/students/1", String.class);
        Assertions.assertThat(response.getStatusCodeValue()).isEqualTo(401);
    }

    @Test
    public void listStudentsWhenUsernameAndPasswordAreCorrectShouldReturnStatusCode200() {
        List<Student> students = Arrays.asList(new Student(1L, "teste1", "teste1@hotmail.com"),
            new Student(2L, "teste2", "teste2@hotmail.com"));
        BDDMockito.when(studentRepository.findAll()).thenReturn(students);
        ResponseEntity<Student> response = restTemplate
            .getForEntity("/v1/protected/students/", Student.class);
        Assertions.assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    public void getStudentsByIdWhenUsernameAndPasswordAreCorrectShouldReturnStatusCode200() {
        ResponseEntity<Student> response = restTemplate
            .getForEntity("/v1/protected/students/{id}", Student.class, 1);
        Assertions.assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    public void getStudentsByIdWhenUsernameAndPasswordAreCorrectAndStudentDoesNotExistShouldReturnStatusCode404() {
        ResponseEntity<Student> response = restTemplate
            .getForEntity("/v1/protected/students/{id}", Student.class, -1);
        Assertions.assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    public void deleteWhenUserHasRoleAdminAndStudentExistsShouldReturnStatusCode200() {
        BDDMockito.doNothing().when(studentRepository).deleteById(1L);
        ResponseEntity<String> exchange = restTemplate
            .exchange("/v1/admin/students/{id}", HttpMethod.DELETE, null, String.class, 1L);
        Assertions.assertThat(exchange.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @WithMockUser(username = "xx", password = "xx", roles = {"USER", "ADMIN"})
    public void deleteWhenUserHasRoleAdminAndStudentDoesNotExistsShouldReturnStatusCode404()
        throws Exception {
        BDDMockito.doThrow(new EmptyResultDataAccessException(-1)).when(studentRepository)
            .deleteById(-1L);
//        ResponseEntity<String> exchange = restTemplate
//            .exchange("/v1/admin/students/{id}", HttpMethod.DELETE, null, String.class, -1L);
//        Assertions.assertThat(exchange.getStatusCodeValue()).isEqualTo(404);
        mockMvc.perform(MockMvcRequestBuilders
            .delete("/v1/admin/students/{id}", -1L))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(username = "xx", password = "xx", roles = {"USER"})
    public void deleteWhenUserHasRoleAdminShouldReturnStatusCode403() throws Exception {
        BDDMockito.doThrow(new EmptyResultDataAccessException(-1)).when(studentRepository)
            .deleteById(-1L);
//        ResponseEntity<String> exchange = restTemplate
//            .exchange("/v1/admin/students/{id}", HttpMethod.DELETE, null, String.class, -1L);
//        Assertions.assertThat(exchange.getStatusCodeValue()).isEqualTo(404);
        mockMvc.perform(MockMvcRequestBuilders
            .delete("/v1/admin/students/{id}", -1L))
            .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    public void createWhenNameIsNullShouldReturnStatusCode400BadRequest() throws Exception {
        Student student = new Student(3L, null, "teste1@hotmail.com");
        BDDMockito.when(studentRepository.save(student)).thenReturn(student);

        ResponseEntity<String> response = restTemplate
            .postForEntity("/v1/admin/students/", student, String.class);
        Assertions.assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    public void createShouldPersistDataAndReturnStatusCode201() throws Exception {
        Student student = new Student(3L, "Sam", "teste1@hotmail.com");
        BDDMockito.when(studentRepository.save(student)).thenReturn(student);

        ResponseEntity<Student> response = restTemplate
            .postForEntity("/v1/admin/students/", student, Student.class);
        Assertions.assertThat(response.getStatusCodeValue()).isEqualTo(201);
        Assertions.assertThat(response.getBody().getId()).isNotNull();
    }

    @Test
    @WithMockUser(username = "xx", password = "xx", roles = {"USER", "ADMIN"})
    public void updateWhenNameIsExistsShouldReturnStatusCode200() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Student student = new Student(3L, "Sam", "teste1@hotmail.com");
        BDDMockito.when(studentRepository.save(student)).thenReturn(student);

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/admin/students/")
            .content(mapper.writeValueAsString(student))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username = "xx", password = "xx", roles = {"USER", "ADMIN"})
    public void updateWhenNameIsNullShouldReturnStatusCode404() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Student student = new Student();
        BDDMockito.when(studentRepository.save(student)).thenReturn(student);

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/admin/students/")
            .content(mapper.writeValueAsString(student))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

}
