package service;

import model.*;
import model.Package;

import java.util.*;

public class DispatchCenter {
    private final Map<String, Package> packageMap = new HashMap<>();
    private final Map<String, Rider> riderMap = new HashMap<>();
    private final List<Assignment> auditTrail = new ArrayList<>();

    private final PriorityQueue<Package> packageQueue = new PriorityQueue<>(
            (a, b) -> {
                if (a.getPriority() != b.getPriority()) {
                    return a.getPriority() == PackagePriority.EXPRESS ? -1 : 1;
                } else if (a.getDeadline() != b.getDeadline()) {
                    return Long.compare(a.getDeadline(), b.getDeadline());
                } else {
                    return Long.compare(a.getOrderTime(), b.getOrderTime());
                }
            }
    );

    public void addRider(Rider rider) {
        riderMap.put(rider.getId(), rider);
    }

    public void updateRiderStatus(String riderId, RiderStatus newStatus) {
        Rider rider = riderMap.get(riderId);
        if (rider == null) throw new IllegalArgumentException("Rider not found");
        rider.setStatus(newStatus);
    }

    public void placeOrder(Package newPackage) {
        if (packageMap.containsKey(newPackage.getId())) {
            throw new IllegalArgumentException("Duplicate package ID");
        }
        packageMap.put(newPackage.getId(), newPackage);
        packageQueue.offer(newPackage);
        assignPackages(); // Try immediate dispatch
    }

    public void assignPackages() {
        List<Package> deferred = new ArrayList<>();

        while (!packageQueue.isEmpty()) {
            Package pkg = packageQueue.poll();

            Optional<Rider> maybeRider = riderMap.values().stream()
                    .filter(r -> r.getStatus() == RiderStatus.AVAILABLE)
                    .filter(r -> !pkg.isFragile() || r.canHandleFragile())
                    .min(Comparator.comparingInt(Rider::getCurrentLoad));

            if (maybeRider.isPresent()) {
                Rider rider = maybeRider.get();
                pkg.setStatus(PackageStatus.ASSIGNED);
                pkg.setPickupTime(System.currentTimeMillis());

                rider.setStatus(RiderStatus.BUSY);
                rider.incrementLoad();

                auditTrail.add(new Assignment(pkg.getId(), rider.getId(), System.currentTimeMillis()));
                System.out.println("Assigned package " + pkg.getId() + " to rider " + rider.getId());
            } else {
                deferred.add(pkg);
            }
        }

        packageQueue.addAll(deferred);
    }

    public void simulateDelivery(String packageId) {
        Package pkg = packageMap.get(packageId);
        if (pkg == null) throw new IllegalArgumentException("Package not found");
        if (pkg.getStatus() != PackageStatus.ASSIGNED) throw new IllegalStateException("Package not assigned");

        pkg.setStatus(PackageStatus.DELIVERED);
        pkg.setDeliveryTime(System.currentTimeMillis());

        auditTrail.stream()
                .filter(a -> a.getPackageId().equals(packageId))
                .findFirst()
                .ifPresent(assignment -> {
                    Rider rider = riderMap.get(assignment.getRiderId());
                    rider.decrementLoad();
                    rider.setStatus(RiderStatus.AVAILABLE);
                });
    }

    public PackageStatus getPackageStatus(String packageId) {
        Package pkg = packageMap.get(packageId);
        if (pkg == null) throw new IllegalArgumentException("Package not found");
        return pkg.getStatus();
    }

    public List<Assignment> getRiderDeliveriesInLast24Hours(String riderId) {
        long now = System.currentTimeMillis();
        long twentyFourHoursAgo = now - 24 * 60 * 60 * 1000L;

        return auditTrail.stream()
                .filter(a -> a.getRiderId().equals(riderId))
                .filter(a -> a.getAssignTime() >= twentyFourHoursAgo)
                .toList();
    }

    public List<Package> getMissedExpressDeliveries() {
        return packageMap.values().stream()
                .filter(pkg -> pkg.getPriority() == PackagePriority.EXPRESS)
                .filter(pkg -> pkg.getStatus() == PackageStatus.DELIVERED)
                .filter(pkg -> pkg.getDeliveryTime() != null && pkg.getDeliveryTime() > pkg.getDeadline())
                .toList();
    }
}
