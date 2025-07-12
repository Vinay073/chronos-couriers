package model;

public class Assignment {
    private final String packageId;
    private final String riderId;
    private final long assignTime;

    public Assignment(String packageId, String riderId, long assignTime) {
        this.packageId = packageId;
        this.riderId = riderId;
        this.assignTime = assignTime;
    }

    public String getPackageId() {
        return packageId;
    }

    public String getRiderId() {
        return riderId;
    }

    public long getAssignTime() {
        return assignTime;
    }
}
