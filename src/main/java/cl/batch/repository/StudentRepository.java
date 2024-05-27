package cl.batch.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.batch.entities.Student;

public interface StudentRepository extends JpaRepository<Student, Integer> {
}
