package model;

public class Waiter {
    private int id;
    private String name;
    private String phone;
    private boolean active;

    public Waiter() {}

    public Waiter(String name, String phone, boolean active) {
        this.name   = name;
        this.phone  = phone;
        this.active = active;
    }

    public int     getId()     { return id; }
    public String  getName()   { return name; }
    public String  getPhone()  { return phone; }
    public boolean isActive()  { return active; }

    public void setId(int id)           { this.id = id; }
    public void setName(String name)    { this.name = name; }
    public void setPhone(String phone)  { this.phone = phone; }
    public void setActive(boolean a)    { this.active = a; }

    @Override
    public String toString() { return name; }   // used directly in JComboBox
}
