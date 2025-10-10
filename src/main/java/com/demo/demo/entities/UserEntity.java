package com.demo.demo.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;


@Entity
@Data
@Table(name="users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    private Long id ;
    @Column(name="firstName",length = 10,nullable = true)
    @Size(max=10,message="le nom ne doit pas depasser 10 caracteres",min=3)
    private String firstName ;
    private String lastName;
    @Column(nullable = false, length = 100,unique=true)
    private String email ;
    @Column(unique=true)
    private String username;
    private String address;
    private String password;
    private String confirmPassword;
    private Date dateInscription;
    private int cin ;
    private int telephone;
    private int age;
    @Enumerated(EnumType.STRING)

    private niveau niveau;
    @Enumerated(EnumType.STRING)
private badge badge;
    private float tsi;

    // A user has only one role
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
    // 🔹 Relation Many-to-Many avec Module
    @ManyToMany
    @JoinTable(
            name = "user_module",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "module_id")
    )
    private Set<Module> modules = new HashSet<>();

    //  Invitations envoyées (OneToMany)
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Invitation> invitationsEnvoyees = new ArrayList<>();

    //  Invitations reçues (OneToMany)
    @OneToMany(mappedBy = "guest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Invitation> invitationsRecues = new ArrayList<>();
    // 🔹 Portefeuille individuel (un seul)
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "portefeuille_indiv_id", referencedColumnName = "id")
    private Portefeuille portefeuilleIndividuel;

    // 🔹 Portefeuilles partagés (plusieurs)
    @ManyToMany
    @JoinTable(
            name = "user_portefeuille_shared",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "portefeuille_id")
    )
    private Set<Portefeuille> portefeuillesPartages = new HashSet<>();
    // 🔹 Many-to-Many pour actualités consultées
    @ManyToMany
    @JoinTable(
            name = "user_actualite",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "actualite_id")
    )
    private Set<Actualite> actualitesConsultees = new HashSet<>();
    // 🔹 Many-to-Many pour les simulations
    @ManyToMany
    @JoinTable(
            name = "user_simulation",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "simulation_id")
    )
    private Set<Simulation> simulationsParticipees = new HashSet<>();


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @Size(max = 10, message = "le nom ne doit pas depasser 10 caracteres", min = 3) String getFirstName() {
        return firstName;
    }

    public void setFirstName(@Size(max = 10, message = "le nom ne doit pas depasser 10 caracteres", min = 3) String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }






    public Date getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(Date dateInscription) {
        this.dateInscription = dateInscription;
    }

    public int getCin() {
        return cin;
    }

    public void setCin(int cin) {
        this.cin = cin;
    }

    public int getTelephone() {
        return telephone;
    }

    public void setTelephone(int telephone) {
        this.telephone = telephone;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public com.demo.demo.entities.niveau getNiveau() {
        return niveau;
    }

    public void setNiveau(com.demo.demo.entities.niveau niveau) {
        this.niveau = niveau;
    }

    public com.demo.demo.entities.badge getBadge() {
        return badge;
    }

    public void setBadge(com.demo.demo.entities.badge badge) {
        this.badge = badge;
    }

    public float getTsi() {
        return tsi;
    }

    public void setTsi(float tsi) {
        this.tsi = tsi;
    }
}
