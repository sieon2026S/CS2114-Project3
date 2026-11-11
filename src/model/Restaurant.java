package model;

import java.util.ArrayList;
import java.util.List;

public class Restaurant {
    private final Waitlist waitlist;
    private final Table[] tables;
    private int nextArrival;
    private Party lastAdded;
    private final List<RestaurantListener> listeners;

    public Restaurant(Table[] initialTables) {
        this.waitlist = new Waitlist();
        this.tables = initialTables != null ? initialTables : new Table[0];
        this.nextArrival = 1;
        this.lastAdded = null;
        this.listeners = new ArrayList<>();
    }

    public void addListener(RestaurantListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    private void notifyListeners() {
        for (RestaurantListener listener : listeners) {
            listener.modelChanged();
        }
    }

    public void addParty(String name, int size) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        
        // Check for duplicate names across the entire model
        for (Party p : waitlist.getAllParties()) {
            if (p.getName().equalsIgnoreCase(name.trim())) {
                throw new IllegalArgumentException("Party name already exists on the waitlist. Please include a last name or unique identifier.");
            }
        }
        for (Table t : tables) {
            if (!t.isFree() && t.getSeatedParty().getName().equalsIgnoreCase(name.trim())) {
                throw new IllegalArgumentException("A seated party already has this name.");
            }
        }

        Party newParty = new Party(name, size, nextArrival++);
        waitlist.addParty(newParty);
        lastAdded = newParty;
        notifyListeners();
    }

    public void seatAt(int tableId) {
        Table table = getTableById(tableId);
        if (table == null) {
            throw new IllegalArgumentException("Invalid table ID.");
        }
        if (!table.isFree()) {
            throw new IllegalArgumentException("Table is already occupied.");
        }

        Party partyToSeat = waitlist.nextFor(table.getCapacity());
        if (partyToSeat == null) {
            throw new IllegalArgumentException("No suitable waiting party for this table capacity.");
        }

        waitlist.removePartyByName(partyToSeat.getName());
        table.seats(partyToSeat);
        notifyListeners();
    }

    public void removeParty(String name) {
        boolean removed = waitlist.removePartyByName(name);
        if (!removed) {
            throw new IllegalArgumentException("Party not found on the waitlist.");
        }
        notifyListeners();
    }

    public void freeTable(int tableId) {
        Table table = getTableById(tableId);
        if (table == null) {
            throw new IllegalArgumentException("Invalid table ID.");
        }
        if (table.isFree()) {
            throw new IllegalArgumentException("Table is already free.");
        }
        table.clear();
        notifyListeners();
    }

    public void undoLastAdd() {
        if (lastAdded == null) {
            throw new IllegalArgumentException("No recent add action to undo.");
        }
        waitlist.removePartyByName(lastAdded.getName());
        lastAdded = null;
        notifyListeners();
    }

    public List<Party> getWaitingParties() {
        return waitlist.getAllParties();
    }

    public List<Table> getTables() {
        List<Table> copy = new ArrayList<>();
        for (Table t : tables) {
            copy.add(t); // or a read-only view/copy wrapper if strictly required
        }
        return copy;
    }

    private Table getTableById(int id) {
        for (Table t : tables) {
            if (t.getNumber() == id) return t;
        }
        return null;
    }
}

