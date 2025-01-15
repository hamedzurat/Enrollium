package enrollium.client.page.database;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


class CourseData {
    private final SimpleStringProperty id;
    private final SimpleStringProperty status;
    private final SimpleStringProperty student;

    public CourseData(String id, String status, String student) {
        this.id      = new SimpleStringProperty(id);
        this.status  = new SimpleStringProperty(status);
        this.student = new SimpleStringProperty(student);
    }

    public StringProperty idProperty()      {return id;}

    public StringProperty statusProperty()  {return status;}

    public StringProperty studentProperty() {return student;}

    public String getStatus()               {return status.get();}

    public void setStatus(String status)    {this.status.set(status);}

    public String getStudent()              {return student.get();}

    public void setStudent(String student)  {this.student.set(student);}
}
