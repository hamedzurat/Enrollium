package enrollium.client.page.database;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


class StudentData {
    private final SimpleStringProperty id;
    private final SimpleStringProperty universityId;
    private final SimpleStringProperty name;
    private final SimpleStringProperty email;

    public StudentData(String id, String universityId, String name, String email) {
        this.id           = new SimpleStringProperty(id);
        this.universityId = new SimpleStringProperty(universityId);
        this.name         = new SimpleStringProperty(name);
        this.email        = new SimpleStringProperty(email);
    }

    public StringProperty idProperty()               {return id;}

    public StringProperty universityIdProperty()     {return universityId;}

    public StringProperty nameProperty()             {return name;}

    public StringProperty emailProperty()            {return email;}

    public String getUniversityId()                  {return universityId.get();}

    public void setUniversityId(String universityId) {this.universityId.set(universityId);}

    public String getName()                          {return name.get();}

    public void setName(String name)                 {this.name.set(name);}

    public String getEmail()                         {return email.get();}

    public void setEmail(String email)               {this.email.set(email);}
}
