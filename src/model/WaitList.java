package model;

import java.util.ArrayList;
import java.util.List;

public class WaitList
{
    private final LinkedQueue<Party> small;
    private final LinkedQueue<Party> medium;
    private final LinkedQueue<Party> large;
    
    public WaitList() {
        this.small = new LinkedQueue<>();
        this.medium = new LinkedQueue<>();
        this.large = new LinkedQueue<>();
    }
    public LinkedQueue<Party> getQueue(SizeGroup group) {
        switch (group) {
            case SMALL: return small;
            case MEDIUM: return medium;
            case LARGE: return large;
            default: throw new IllegalArgumentException("Unknown size group");
        }
    }
    public void addParty(Party party) {
        SizeGroup group = SizeGroup.forSize(party.getSize());
        getQueue(group).enqueue(party);
    }
    public boolean removePartyByName(String name) {
        return removePartyFromQueue(small, name) || 
            removePartyFromQueue(medium, name) || 
            removePartyFromQueue(large, name);
    }
    private boolean removePartyFromQueue(LinkedQueue<Party> queue, String name) {
        LinkedQueue.Node<Party> curr = queue.peek() != null ? null : null;
        List<Party> parties = extractAll(queue);
        Party target = null;
        for(Party p : parties) {
            if(p.getName().equalsIgnoreCase(name)) {
                target = p;
                break;
            }
        }
        if (target != null) {
            return queue.remove(target);
        }
        return false;
    }
    public List<Party> getAllParties(){
        List<Party> list = new ArrayList<>();
        list.addAll(extractAll(small));
        list.addAll(extractAll(medium));
        list.addAll(extractAll(large));
        return list;
    }
    private List<Party> extractAll(LinkedQueue<Party> queue) {
        List<Party> list = new ArrayList<>();
        LinkedQueue<Party> temp = new LinkedQueue<>();
        while (!queue.isEmpty()) {
            Party p = queue.dequeue();
            list.add(p);
            temp.enqueue(p);
        }
        while(!temp.isEmpty()) {
            Party p = queue.dequeue();
            list.add(p);
            temp.enqueue(p);
        }
        while(!temp.isEmpty()) {
            queue.enqueue(temp.dequeue());
        }
        return list;
        
    }
    
    public Party nextFor(int capacity) {
        SizeGroup targetGroup = SizeGroup.forSize(capacity);
        
        LinkedQueue<Party> exactQueue = getQueue(targetGroup);
        if(!exactQueue.isEmpty()) {
            return exactQueue.peek();
        }
        if(targetGroup == SizeGroup.MEDIUM) {
            if(!small.isEmpty()) return small.peek();
        } else if (targetGroup == SizeGroup.LARGE) {
            if(!medium.isEmpty()) return medium.peek();
            if(!small.isEmpty()) return small.peek();
        }
        return null;
    }
    public Party seatNext(int capacity) {
        Party p = nextFor(capacity);
        if(p != null) {
            removePartyByName(p.getName());
        }
        return p;
    }
}
