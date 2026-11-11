package model;
public enum SizeGroup
{
    SMALL(1, 2),
    MEDIUM(3, 4),
    LARGE(5, 6);
    
    private final int minSize;
    private final int maxSize;
    
    SizeGroup(int minSize, int maxSize){
        this.minSize = minSize;
        this.maxSize = maxSize;
    }
    public int getMinSize() {
        return minSize;
    }
    public int getMaxSize() {
        return maxSize;
    }
    
    public static SizeGroup forSize(int size) {
        if(size >= 1 && size <= 2) return SMALL;
        if(size >= 3 && size <= 4) return MEDIUM;
        if(size >= 5 && size <= 6) return LARGE;
        throw new IllegalArgumentException("Party size " + size + " out of supported range (1-6).");
    }
}
