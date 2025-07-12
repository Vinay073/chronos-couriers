package model;

public class Package {
    private final String id;
    private final PackagePriority priority;
    private final long deadline;
    private final long orderTime;
    private PackageStatus status;
    private final boolean fragile;
    private Long pickupTime;
    private Long deliveryTime;

    public Package(String id, PackagePriority priority, long deadline, long orderTime, boolean fragile) {
        this.id = id;
        this.priority = priority;
        this.deadline = deadline;
        this.orderTime = orderTime;
        this.status = PackageStatus.PENDING;
        this.fragile = fragile;
    }

    public String getId() {
        return id;
    }

    public PackagePriority getPriority() {
        return priority;
    }

    public long getDeadline() {
        return deadline;
    }

    public long getOrderTime() {
        return orderTime;
    }

    public PackageStatus getStatus() {
        return status;
    }

    public void setStatus(PackageStatus status) {
        this.status = status;
    }

    public boolean isFragile() {
        return fragile;
    }

    public Long getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(Long pickupTime) {
        this.pickupTime = pickupTime;
    }

    public Long getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(Long deliveryTime) {
        this.deliveryTime = deliveryTime;
    }
}
