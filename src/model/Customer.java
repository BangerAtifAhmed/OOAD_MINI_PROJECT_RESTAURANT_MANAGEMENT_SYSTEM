package model;

import java.time.LocalDateTime;

/** FR-8: Customer profile for loyalty tracking */
public class Customer {
    private int id;
    private String name;
    private String phone;
    private String email;
    private LocalDateTime createdAt;

    public Customer() {}

    public Customer(String name, String phone, String email) {
        this.name  = name;
        this.phone = phone;
        this.email = email;
    }

    // Getters
    public int getId()                    { return id; }
    public String getName()               { return name; }
    public String getPhone()              { return phone; }
    public String getEmail()              { return email; }
    public LocalDateTime getCreatedAt()   { return createdAt; }

    // Setters
    public void setId(int id)                         { this.id = id; }
    public void setName(String name)                  { this.name = name; }
    public void setPhone(String phone)                { this.phone = phone; }
    public void setEmail(String email)                { this.email = email; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("%s  |  %s  |  %s", name, phone, email);
    }
}
