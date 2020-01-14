package br.com.petterson.spring.endpoint;


import br.com.petterson.spring.error.ResourceNotFoundException;
import br.com.petterson.spring.model.Student;
import br.com.petterson.spring.repository.StudentRepository;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1")
public class StudentEndpoint {


    private final StudentRepository studentDAO;

    @Autowired
    public StudentEndpoint(StudentRepository studentDAO) {
        this.studentDAO = studentDAO;
    }

    @GetMapping(path = "protected/students")
    @ApiOperation(value = "Return a list with all students", response = Student[].class)
    public ResponseEntity<?> listAll(Pageable pageable) {
        return new ResponseEntity<>(studentDAO.findAll(pageable), HttpStatus.OK);
    }

    @GetMapping(path = "protected/students/{id}")
    @ApiOperation(value = "Return a stundent by id", response = Student[].class)
    public ResponseEntity<?> getStudentById(@PathVariable("id") Long id) {
        try {
            Student student = studentDAO.findById(id).get();
            return new ResponseEntity<>(student, HttpStatus.OK);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Student not found for ID: " + id);
        }
    }

    @GetMapping(path = "protected/students/findbyname/{name}")
    @ApiOperation(value = "Return student by name", response = Student[].class)
    public ResponseEntity<?> findByName(@PathVariable String name) {
        return new ResponseEntity<>(studentDAO.findByNameIgnoreCaseContaining(name), HttpStatus.OK);
    }

    @PostMapping(path = "admin/students")
    @ApiOperation(value = "save student", response = Student[].class)
    public ResponseEntity<?> save(@Valid @RequestBody Student student) {
        return new ResponseEntity<>(studentDAO.save(student), HttpStatus.CREATED);
    }

    @DeleteMapping(path = "admin/students/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation(value = "delete student by id", response = Student[].class)
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            studentDAO.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Student not found for ID: " + id);
        }
    }

    @PutMapping(path = "admin/students")
    @ApiOperation(value = "update student", response = Student[].class)
    public ResponseEntity<?> update(@RequestBody Student student) {
        try {
            studentDAO.save(student);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Student not found for ID: " + student.getId());
        }
    }
}
