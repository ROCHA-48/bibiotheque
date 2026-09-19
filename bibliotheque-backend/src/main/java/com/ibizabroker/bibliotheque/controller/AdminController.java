package com.ibizabroker.bibliotheque.controller;

import com.ibizabroker.bibliotheque.dao.RoleRepository;
import com.ibizabroker.bibliotheque.dao.UsersRepository;
import com.ibizabroker.bibliotheque.entity.Role;
import com.ibizabroker.bibliotheque.entity.Users;
import com.ibizabroker.bibliotheque.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@CrossOrigin("http://localhost:4200/")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/users")
//    @PreAuthorize("hasRole('Admin')")
    public Users addUserByAdmin(@RequestBody Users user) {
        // Lie les rôles existants par leur nom au lieu d'insérer de nouveaux rôles
        if (user.getRole() != null) {
            Set<Role> resolvedRoles = new HashSet<>();
            for (Role r : user.getRole()) {
                Role existing = roleRepository.findByRoleName(r.getRoleName())
                        .orElseThrow(() -> new NotFoundException("Rôle inconnu : " + r.getRoleName()));
                resolvedRoles.add(existing);
            }
            user.setRole(resolvedRoles);
        }

        String password = user.getPassword();
        String encryptPassword = passwordEncoder.encode(password);
        user.setPassword(encryptPassword);
        usersRepository.save(user);
        return user;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('Admin')")
    public List<Users> getAllUsers() {
        return usersRepository.findAll();
    }

    @PreAuthorize("hasRole('Admin')")
    @GetMapping("/users/{id}")
    public ResponseEntity<Users> getUserById(@PathVariable Integer id) {
        Users user = usersRepository.findById(id).orElseThrow(() -> new NotFoundException("User with id "+ id +" does not exist."));
        return ResponseEntity.ok(user);
    }

    @PreAuthorize("hasRole('Admin')")
    @PutMapping("/users/{id}")
    public ResponseEntity<Users> updateUser(@PathVariable Integer id, @RequestBody Users userDetails) {
        Users user = usersRepository.findById(id).orElseThrow(() -> new NotFoundException("User with id "+ id +" does not exist."));

        user.setName(userDetails.getName());
        user.setUsername(userDetails.getUsername());

        if (userDetails.getRole() != null) {
            Set<Role> resolvedRoles = new HashSet<>();
            for (Role r : userDetails.getRole()) {
                Role existing = roleRepository.findByRoleName(r.getRoleName())
                        .orElseThrow(() -> new NotFoundException("Rôle inconnu : " + r.getRoleName()));
                resolvedRoles.add(existing);
            }
            user.setRole(resolvedRoles);
        }

        Users updatedUser = usersRepository.save(user);
        return ResponseEntity.ok(updatedUser);
    }

    @Autowired
    private com.ibizabroker.bibliotheque.dao.ReservationRepository reservationRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @PreAuthorize("hasRole('Admin')")
    @Transactional
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " does not exist."));

        // Nettoyage des références avant suppression (user_role et Reservation)
        reservationRepository.deleteByUserId(id);
        jdbcTemplate.update("DELETE FROM user_role WHERE user_id = ?", id);

        usersRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
