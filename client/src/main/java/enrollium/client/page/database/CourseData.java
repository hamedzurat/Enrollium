package enrollium.client.page.database;

import enrollium.server.db.entity.types.CourseStatus;
import javafx.beans.property.*;
import lombok.Data;


@Data
public class CourseData {
    private final StringProperty               id            = new SimpleStringProperty();
    private final LongProperty                 version       = new SimpleLongProperty();
    private final ObjectProperty<CourseStatus> status        = new SimpleObjectProperty<>();
    private final StringProperty               studentId     = new SimpleStringProperty();
    private final StringProperty               studentName   = new SimpleStringProperty();
    private final StringProperty               subjectId     = new SimpleStringProperty();
    private final StringProperty               subjectName   = new SimpleStringProperty();
    private final StringProperty               trimesterId   = new SimpleStringProperty();
    private final IntegerProperty              trimesterCode = new SimpleIntegerProperty();
    private final StringProperty               sectionId     = new SimpleStringProperty();
    private final StringProperty               sectionName   = new SimpleStringProperty();
    private final ObjectProperty<Double>       grade         = new SimpleObjectProperty<>();

    // Constructor
    public CourseData() {}

    public CourseData(String id, long version, CourseStatus status, String studentId, String studentName,
                      String subjectId, String subjectName, String trimesterId, int trimesterCode,
                      String sectionId, String sectionName, double grade) {
        this.id.set(id);
        this.version.set(version);
        this.status.set(status);
        this.studentId.set(studentId);
        this.studentName.set(studentName);
        this.subjectId.set(subjectId);
        this.subjectName.set(subjectName);
        this.trimesterId.set(trimesterId);
        this.trimesterCode.set(trimesterCode);
        this.sectionId.set(sectionId);
        this.sectionName.set(sectionName);
        this.grade.set(grade);
    }

    // Getters and setters for property access
    public StringProperty idProperty() {
        return id;
    }

    public LongProperty versionProperty() {
        return version;
    }

    public ObjectProperty<CourseStatus> statusProperty() {
        return status;
    }

    public StringProperty studentIdProperty() {
        return studentId;
    }

    public StringProperty studentNameProperty() {
        return studentName;
    }

    public StringProperty subjectIdProperty() {
        return subjectId;
    }

    public StringProperty subjectNameProperty() {
        return subjectName;
    }

    public StringProperty trimesterIdProperty() {
        return trimesterId;
    }

    public IntegerProperty trimesterCodeProperty() {
        return trimesterCode;
    }

    public StringProperty sectionIdProperty() {
        return sectionId;
    }

    public StringProperty sectionNameProperty() {
        return sectionName;
    }

    public ObjectProperty<Double> gradeProperty() {
        return grade;
    }

    // Convenience getters and setters for values
    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public long getVersion() {
        return version.get();
    }

    public void setVersion(long version) {
        this.version.set(version);
    }

    public CourseStatus getStatus() {
        return status.get();
    }

    public void setStatus(CourseStatus status) {
        this.status.set(status);
    }

    public String getStudentId() {
        return studentId.get();
    }

    public void setStudentId(String studentId) {
        this.studentId.set(studentId);
    }

    public String getStudentName() {
        return studentName.get();
    }

    public void setStudentName(String studentName) {
        this.studentName.set(studentName);
    }

    public String getSubjectId() {
        return subjectId.get();
    }

    public void setSubjectId(String subjectId) {
        this.subjectId.set(subjectId);
    }

    public String getSubjectName() {
        return subjectName.get();
    }

    public void setSubjectName(String subjectName) {
        this.subjectName.set(subjectName);
    }

    public String getTrimesterId() {
        return trimesterId.get();
    }

    public void setTrimesterId(String trimesterId) {
        this.trimesterId.set(trimesterId);
    }

    public int getTrimesterCode() {
        return trimesterCode.get();
    }

    public void setTrimesterCode(int trimesterCode) {
        this.trimesterCode.set(trimesterCode);
    }

    public String getSectionId() {
        return sectionId.get();
    }

    public void setSectionId(String sectionId) {
        this.sectionId.set(sectionId);
    }

    public String getSectionName() {
        return sectionName.get();
    }

    public void setSectionName(String sectionName) {
        this.sectionName.set(sectionName);
    }

    public Double getGrade() {
        return grade.get();
    }

    public void setGrade(double grade) {
        this.grade.set(grade);
    }
}
