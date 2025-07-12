package model;

public class Rider {
    private final String id;
    private RiderStatus status;
    private double reliabilityRating;
    private boolean canHandleFragile;
    private int currentLoad;

    public Rider(String id, boolean canHandleFragile, double reliabilityRating) {
        this.id = id;
        this.canHandleFragile = canHandleFragile;
        this.reliabilityRating = reliabilityRating;
        this.status = RiderStatus.OFFLINE;
        this.currentLoad = 0;
    }

    public String getId() { return id; }
    public RiderStatus getStatus() { return status; }
    public void setStatus(RiderStatus status) { this.status = status; }
    public double getReliabilityRating() { return reliabilityRating; }
    public void setReliabilityRating(double reliabilityRating) { this.reliabilityRating = reliabilityRating; }
    public boolean canHandleFragile() { return canHandleFragile; }
    public int getCurrentLoad() { return currentLoad; }
    public void incrementLoad() { currentLoad++; }
    public void decrementLoad() { currentLoad--; }
}

