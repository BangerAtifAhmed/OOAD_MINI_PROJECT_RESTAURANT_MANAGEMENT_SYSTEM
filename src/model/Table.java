package model;

public class Table {

    public enum Status { AVAILABLE, OCCUPIED }

    private int id;
    private int tableNumber;
    private int capacity;
    private Status status;

    public Table() {}

    public Table(int id, int tableNumber, int capacity, Status status) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status;
    }

    // Getters
    public int getId()            { return id; }
    public int getTableNumber()   { return tableNumber; }
    public int getCapacity()      { return capacity; }
    public Status getStatus()     { return status; }

    // Setters
    public void setId(int id)                   { this.id = id; }
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }
    public void setCapacity(int capacity)       { this.capacity = capacity; }
    public void setStatus(Status status)        { this.status = status; }

    public boolean isAvailable() { return status == Status.AVAILABLE; }

    @Override
    public String toString() {
        return String.format("Table %d  (seats %d)  —  %s", tableNumber, capacity, status);
    }
}
