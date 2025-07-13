import model.*;
import model.Package;
import service.DispatchCenter;

import java.util.Scanner;
import java.util.UUID;

public class Main {
    private static final DispatchCenter dispatchCenter = new DispatchCenter();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        printWelcome();

        while (true) {
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) break;
            if (input.isEmpty()) continue;

            try {
                processCommand(input);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        System.out.println("Exiting Chronos Couriers CLI.");
        scanner.close();
    }

    private static void printWelcome() {
        System.out.println("Welcome to Chronos Couriers");
        System.out.println("Type `exit` to quit.\n");
    }

    private static void processCommand(String input) {
        String[] parts = input.split(" ");
        String command = parts[0].toLowerCase();
        String incorrectFormatMessage = "Incorrect command format. ";

        switch (command) {
            case "place_order" -> {
                if (parts.length < 4)
                    throw new IllegalArgumentException(incorrectFormatMessage + "Example: place_order EXPRESS 1899999999000 false");
                PackagePriority priority = PackagePriority.valueOf(parts[1].toUpperCase());
                long deadline = Long.parseLong(parts[2]);
                boolean fragile = Boolean.parseBoolean(parts[3]);
                long now = System.currentTimeMillis();
                String packageId = "P" + UUID.randomUUID().toString().substring(0, 6);
                Package newPackage = new Package(packageId, priority, deadline, now, fragile);
                dispatchCenter.placeOrder(newPackage);
                System.out.println("Package placed with ID: " + packageId);
            }

            case "update_rider_status" -> {
                if (parts.length < 3)
                    throw new IllegalArgumentException(incorrectFormatMessage + "Example: update_rider_status R1 AVAILABLE");
                String riderId = parts[1];
                RiderStatus status = RiderStatus.valueOf(parts[2].toUpperCase());
                dispatchCenter.updateRiderStatus(riderId, status);
                System.out.println("Rider " + riderId + " updated to status " + status);
            }

            case "add_rider" -> {
                if (parts.length < 4)
                    throw new IllegalArgumentException(incorrectFormatMessage + "Example: add_rider R1 true 4.5");
                String riderId = parts[1];
                boolean canHandleFragile = Boolean.parseBoolean(parts[2]);
                double rating = Double.parseDouble(parts[3]);
                Rider rider = new Rider(riderId, canHandleFragile, rating);
                dispatchCenter.addRider(rider);
                System.out.println("Rider added: " + riderId);
            }

            case "assign_packages" -> {
                dispatchCenter.assignPackages();
                System.out.println("Assignment cycle completed");
            }

            case "simulate_delivery" -> {
                if (parts.length < 2)
                    throw new IllegalArgumentException(incorrectFormatMessage + "Example: simulate_delivery p12345");
                String packageId = parts[1];
                dispatchCenter.simulateDelivery(packageId);
                System.out.println("Package " + packageId + " marked as delivered");
            }

            case "get_package_status" -> {
                if (parts.length < 2)
                    throw new IllegalArgumentException(incorrectFormatMessage + "Example: get_package_status p12345");
                String packageId = parts[1];
                PackageStatus status = dispatchCenter.getPackageStatus(packageId);
                System.out.println("Status: " + status);
            }

            case "audit" -> {
                if (parts.length < 4 || !parts[1].equalsIgnoreCase("rider") || !parts[3].equalsIgnoreCase("last24hrs")) {
                    throw new IllegalArgumentException(incorrectFormatMessage + "Example: audit rider R1 last24hrs");
                }
                String riderId = parts[2];
                var logs = dispatchCenter.getRiderDeliveriesInLast24Hours(riderId);
                if (logs.isEmpty()) {
                    System.out.println("No deliveries in last 24 hours.");
                } else {
                    System.out.println("Deliveries by " + riderId + ":");
                    logs.forEach(a -> System.out.println(" - Package: " + a.getPackageId() + ", Assigned at: " + a.getAssignTime()));
                }
            }
            case "mark_failed" -> {
                if (parts.length < 2)
                    throw new IllegalArgumentException(incorrectFormatMessage + "Example: mark_failed <packageId>");
                String packageId = parts[1];
                dispatchCenter.markPackageAsFailed(packageId);
                System.out.println("Package " + packageId + " marked as FAILED");
            }

            case "missed_express" -> {
                var missed = dispatchCenter.getMissedExpressDeliveries();
                if (missed.isEmpty()) {
                    System.out.println("No missed express deliveries.");
                } else {
                    System.out.println("Missed EXPRESS packages:");
                    missed.forEach(p -> System.out.println(" - " + p.getId() + " missed deadline: " + p.getDeadline() + ", delivered: " + p.getDeliveryTime()));
                }
            }

            default -> System.out.println("Unknown command: " + command);
        }
    }
}
