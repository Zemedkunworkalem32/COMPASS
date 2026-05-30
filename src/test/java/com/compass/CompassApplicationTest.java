package com.compass;

import com.compass.models.CampusLocation;
import com.compass.models.CompassData;
import com.compass.models.Complaint;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompassApplicationTest {
    @Test
    void applicationClassExists() {
        assertNotNull(CompassApplication.class);
    }

    @Test
    void complaintDefaultsAreSensible() {
        Complaint complaint = new Complaint();

        assertEquals(Complaint.ComplaintPriority.MEDIUM, complaint.getPriority());
        assertEquals(Complaint.ComplaintStatus.SUBMITTED, complaint.getStatus());
    }

    @Test
    void routeFormattingUsesLocationNames() {
        CampusLocation library = new CampusLocation();
        library.setLocationId(1);
        library.setLocationName("Library");

        CampusLocation lab = new CampusLocation();
        lab.setLocationId(2);
        lab.setLocationName("Lab");

        String route = new CompassData().formatRoute(List.of(1, 2), List.of(library, lab));

        assertEquals("Library -> Lab", route);
    }
}
