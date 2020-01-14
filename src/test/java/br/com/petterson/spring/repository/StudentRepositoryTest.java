package br.com.petterson.spring.repository;

import br.com.petterson.spring.model.Student;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
public class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void createShouldPersistData() {
        Student student = new Student("Petterson", "petterson.santos@teste.com.br");
        this.studentRepository.save(student);
        Assertions.assertThat(student.getId()).isNotNull();
        Assertions.assertThat(student.getName()).isEqualTo("Petterson");
        Assertions.assertThat(student.getEmail()).isEqualTo("petterson.santos@teste.com.br");
    }

    @Test
    public void deleteShouldRemoveData() {
        Student student = new Student("Petterson", "petterson.santos@teste.com.br");
        this.studentRepository.save(student);
        this.studentRepository.delete(student);
        Assertions.assertThat(studentRepository.findById(student.getId())).isEmpty();
    }

    @Test
    public void updateShouldChangeAndPersistData() {
        Student student = new Student("Petterson", "petterson.santos@teste.com.br");
        this.studentRepository.save(student);
        student.setName("Petterson49");
        student.setEmail("petterson12345.santos@teste.com.br");
        this.studentRepository.save(student);
        Optional<Student> student1 = this.studentRepository.findById(student.getId());
        student = student1.get();
        Assertions.assertThat(student.getName()).isEqualTo("Petterson49");
        Assertions.assertThat(student.getEmail()).isEqualTo("petterson12345.santos@teste.com.br");
    }

    @Test
    public void findByNameIgnoreCaseContainingShouldIgnoreCase() {
        Student student = new Student("Petterson", "petterson.santos@teste.com.br");
        Student student2 = new Student("petterson", "petterson.santos@teste.com.br");
        this.studentRepository.save(student);
        this.studentRepository.save(student2);
        List<Student> studentList = studentRepository.findByNameIgnoreCaseContaining("petterson");
        Assertions.assertThat(studentList.size()).isEqualTo(2);
    }

}
