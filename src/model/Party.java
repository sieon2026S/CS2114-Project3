package model;
public class Party
{
    private final String name;
    private final int size;
    private final int arrivalNumber;
    
    public Party(String name, int size, int arrivalNumber) {
        if(name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Party name cannot be null or empty.");
        }
        if(size<=0) {
            throw new IllegalArgumentException("Party size must be greater than zero.");
        }
        if (arrivalNumber < 0) {
            throw new IllegalArgumentException("Arrival number cannot be negative.");
        }
        this.name = name.trim();
        this.size = size;
        this.arrivalNumber = arrivalNumber;
    }
    public String getName() {
        return name;
    }
    public int getSize() {
        return size;
    }
    public int getArrivalNumber() {
        return arrivalNumber;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(!(obj instanceof Party)) return false;
        Party other = (Party50bj -> other);
        return this.name.equalsIgnoreCase(((Party) obj).name);
    }
    @Override 
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }
}
