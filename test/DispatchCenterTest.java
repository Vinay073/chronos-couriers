package test;


import model.*;
import model.Package;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.DispatchCenter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DispatchCenterTest {

    private DispatchCenter dispatchCenter;

    @BeforeEach
    void setup() {
        dispatchCenter = new DispatchCenter();

        Rider r1 = new Rider("R1", true, 4.5);
        Rider r2 = new Rider("R2", false, 4.0);

        dispatchCenter.addRider(r1);
        dispatchCenter.addRider(r2);

        dispatchCenter.updateRiderStatus("R1", RiderStatus.AVAILABLE);
        dispatchCenter.updateRiderStatus("R2", RiderStatus.AVAILABLE);
    }

    @Test
    void testPlaceOrderAndAssignment() {
        Package p1 = new Package("P1", PackagePriority.EXPRESS, System.currentTimeMillis() + 10000, System.currentTimeMillis(), false);
        dispatchCenter.placeOrder(p1);

        assertEquals(PackageStatus.ASSIGNED, dispatchCenter.getPackageStatus("P1"));
    }

    @Test
    void testPriorityOrderAssignment() {
        Package standard = new Package("P2", PackagePriority.STANDARD, System.currentTimeMillis() + 30000, System.currentTimeMillis(), false);
        Package express = new Package("P3", PackagePriority.EXPRESS, System.currentTimeMillis() + 20000, System.currentTimeMillis(), false);

        dispatchCenter.placeOrder(standard);
        dispatchCenter.placeOrder(express);

        assertEquals(PackageStatus.ASSIGNED, dispatchCenter.getPackageStatus("P3"));
    }

    @Test
    void testFragilePackageOnlyToQualifiedRider() {
        Rider r3 = new Rider("R3", false, 4.0);
        dispatchCenter.addRider(r3);
        dispatchCenter.updateRiderStatus("R3", RiderStatus.AVAILABLE);

        Package fragile = new Package("P4", PackagePriority.EXPRESS, System.currentTimeMillis() + 5000, System.currentTimeMillis(), true);
        dispatchCenter.placeOrder(fragile);

        assertEquals(PackageStatus.ASSIGNED, dispatchCenter.getPackageStatus("P4"));
    }

    @Test
    void testSimulateDelivery() {
        Package p5 = new Package("P5", PackagePriority.EXPRESS, System.currentTimeMillis() + 10000, System.currentTimeMillis(), false);
        dispatchCenter.placeOrder(p5);

        dispatchCenter.simulateDelivery("P5");

        assertEquals(PackageStatus.DELIVERED, dispatchCenter.getPackageStatus("P5"));
    }

    @Test
    void testMarkPackageAsFailed() {
        Package p6 = new Package("P6", PackagePriority.STANDARD, System.currentTimeMillis() + 10000, System.currentTimeMillis(), false);
        dispatchCenter.placeOrder(p6);

        dispatchCenter.markPackageAsFailed("P6");

        assertEquals(PackageStatus.FAILED, dispatchCenter.getPackageStatus("P6"));
    }

    @Test
    void testAuditTrailForAssignedRider() {
        long now = System.currentTimeMillis();

        Package p7 = new Package("P7", PackagePriority.EXPRESS, now + 10000, now, false);
        dispatchCenter.placeOrder(p7);
        dispatchCenter.simulateDelivery("P7");

        Assignment assignment = dispatchCenter.getAuditTrail().stream()
                .filter(a -> a.getPackageId().equals("P7"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No assignment found for P7"));

        List<Assignment> history = dispatchCenter.getRiderDeliveriesInLast24Hours(assignment.getRiderId());
        assertTrue(history.stream().anyMatch(a -> a.getPackageId().equals("P7")));
    }


    @Test
    void testMissedExpressDelivery() throws InterruptedException {
        long past = System.currentTimeMillis() - 10000;

        Package lateExpress = new Package("P8", PackagePriority.EXPRESS, past, System.currentTimeMillis(), false);
        dispatchCenter.placeOrder(lateExpress);

        Thread.sleep(100);

        dispatchCenter.simulateDelivery("P8");

        List<Package> missed = dispatchCenter.getMissedExpressDeliveries();
        assertEquals(1, missed.size());
        assertEquals("P8", missed.get(0).getId());
    }
}

