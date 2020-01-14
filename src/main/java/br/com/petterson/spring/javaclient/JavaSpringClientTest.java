package br.com.petterson.spring.javaclient;

import br.com.petterson.spring.model.Student;


public class JavaSpringClientTest {

    public static void main(String[] args) {

        Student studentPost = new Student();
        studentPost.setName("TESTE");
        studentPost.setEmail("petterson@test.com.br");
        studentPost.setId(14L);

        JavaClientDAO dao = new JavaClientDAO();

//        System.out.println(dao.findById(100));
//        System.out.println(dao.listAll());
//        System.out.println(dao.save(studentPost));
//        dao.update(studentPost);
//          dao.delete(14L);
    }


}
