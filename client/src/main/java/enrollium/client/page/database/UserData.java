package enrollium.client.page.database;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


class UserData {
    private final SimpleStringProperty id;
    private final SimpleStringProperty name;
    private final SimpleStringProperty email;
    private final SimpleStringProperty role;

    public UserData(String id, String name, String email, String role) {
        this.id    = new SimpleStringProperty(id);
        this.name  = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.role  = new SimpleStringProperty(role);
    }

    public StringProperty idProperty()    {return id;}

    public StringProperty nameProperty()  {return name;}

    public StringProperty emailProperty() {return email;}

    public StringProperty roleProperty()  {return role;}

    public String getName()               {return name.get();}

    public void setName(String name)      {this.name.set(name);}

    public String getEmail()              {return email.get();}

    public void setEmail(String email)    {this.email.set(email);}

    public String getRole()               {return role.get();}

    public void setRole(String role)      {this.role.set(role);}
}
