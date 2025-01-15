package enrollium.client.page.database;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


class PrerequisiteData {
    private final SimpleStringProperty id;
    private final SimpleStringProperty subject;
    private final SimpleStringProperty prerequisite;
    private final SimpleStringProperty minimumGrade;

    public PrerequisiteData(String id, String subject, String prerequisite, String minimumGrade) {
        this.id           = new SimpleStringProperty(id);
        this.subject      = new SimpleStringProperty(subject);
        this.prerequisite = new SimpleStringProperty(prerequisite);
        this.minimumGrade = new SimpleStringProperty(minimumGrade);
    }

    public StringProperty idProperty()               {return id;}

    public StringProperty subjectProperty()          {return subject;}

    public StringProperty prerequisiteProperty()     {return prerequisite;}

    public StringProperty minimumGradeProperty()     {return minimumGrade;}

    public String getSubject()                       {return subject.get();}

    public void setSubject(String subject)           {this.subject.set(subject);}

    public String getPrerequisite()                  {return prerequisite.get();}

    public void setPrerequisite(String prerequisite) {this.prerequisite.set(prerequisite);}

    public String getMinimumGrade()                  {return minimumGrade.get();}

    public void setMinimumGrade(String minimumGrade) {this.minimumGrade.set(minimumGrade);}
}
