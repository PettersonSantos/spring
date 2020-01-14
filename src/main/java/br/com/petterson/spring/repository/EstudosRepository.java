package br.com.petterson.spring.repository;

import br.com.petterson.spring.model.UserEstudos;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface EstudosRepository extends PagingAndSortingRepository<UserEstudos, Long> {

    UserEstudos findByUsername(String username);
}
