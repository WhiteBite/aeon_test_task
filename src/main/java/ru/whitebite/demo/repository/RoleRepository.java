 
package ru.whitebite.demo.repository;

import ru.whitebite.demo.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

}
