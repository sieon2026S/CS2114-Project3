package model;
public class Table
{
    private final int number;
    private final int capacity;
    private Party seatedParty;
    
    public Table(int number, int capacity) {
        if(number < 0 || capacity <= 0) {
            throw new IllegalArgumentException("Invalid table number or capacity.");
        }
        this.number = number;
        this.capacity = capacity;
        this.seatedParty = null;
    }
    public int getNumber() {
        return number;
    }
    public int getCapacity() {
        return capacity;
    }
    public Party getSeatedParty() {
        return seatedParty;
    }
    public boolean isFree() {
        return seatedParty == null;
    }
    void seats(Party party) {
        this.seatedParty = party;
    }
    void clear() {
        this.seatedParty = null;
    }
    
}
