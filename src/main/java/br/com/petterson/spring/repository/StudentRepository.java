package br.com.petterson.spring.repository;

import br.com.petterson.spring.model.Student;
import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface StudentRepository extends PagingAndSortingRepository<Student, Long> {

    List<Student> findByNameIgnoreCaseContaining(String name);
}
