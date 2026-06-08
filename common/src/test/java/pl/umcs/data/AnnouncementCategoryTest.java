package pl.umcs.data;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnnouncementCategoryTest {

    @Test
    void testEnumValues() {
        AnnouncementCategory[] categories = AnnouncementCategory.values();
        assertEquals(10, categories.length);
        
        assertEquals(AnnouncementCategory.REAL_ESTATE, AnnouncementCategory.valueOf("REAL_ESTATE"));
        assertEquals(AnnouncementCategory.VEHICLES, AnnouncementCategory.valueOf("VEHICLES"));
        assertEquals(AnnouncementCategory.JOBS, AnnouncementCategory.valueOf("JOBS"));
        assertEquals(AnnouncementCategory.SERVICES, AnnouncementCategory.valueOf("SERVICES"));
        assertEquals(AnnouncementCategory.ELECTRONICS, AnnouncementCategory.valueOf("ELECTRONICS"));
        assertEquals(AnnouncementCategory.HOME_AND_GARDEN, AnnouncementCategory.valueOf("HOME_AND_GARDEN"));
        assertEquals(AnnouncementCategory.FASHION, AnnouncementCategory.valueOf("FASHION"));
        assertEquals(AnnouncementCategory.EDUCATION, AnnouncementCategory.valueOf("EDUCATION"));
        assertEquals(AnnouncementCategory.ANIMALS, AnnouncementCategory.valueOf("ANIMALS"));
        assertEquals(AnnouncementCategory.OTHER, AnnouncementCategory.valueOf("OTHER"));
    }
}
