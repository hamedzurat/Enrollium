package enrollium.server.db.entity.types;

public enum TrimesterStatus {
    UPCOMING,            //
    COURSE_SELECTION,    // Course selection phase
    SECTION_CREATION,    // Admin creating sections
    SECTION_SELECTION,   // Students selecting sections
    ONGOING,             // Classes running
    COMPLETED
}
