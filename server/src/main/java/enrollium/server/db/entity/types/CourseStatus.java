package enrollium.server.db.entity.types;

public enum CourseStatus {
    SELECTED,           // Initial selection, no section
    REGISTERED,         // Has section, ongoing
    COMPLETED,          // Finished with grade
    DROPPED             // Dropped the course
}
