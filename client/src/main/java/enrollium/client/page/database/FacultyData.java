package enrollium.client.page.database;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


class FacultyData {
    private final SimpleStringProperty id;
    private final SimpleStringProperty shortcode;
    private final SimpleStringProperty name;
    private final SimpleStringProperty email;

    public FacultyData(String id, String shortcode, String name, String email) {
        this.id        = new SimpleStringProperty(id);
        this.shortcode = new SimpleStringProperty(shortcode);
        this.name      = new SimpleStringProperty(name);
        this.email     = new SimpleStringProperty(email);
    }

    public StringProperty idProperty()         {return id;}

    public StringProperty shortcodeProperty()  {return shortcode;}

    public StringProperty nameProperty()       {return name;}

    public StringProperty emailProperty()      {return email;}

    public String getShortcode()               {return shortcode.get();}

    public void setShortcode(String shortcode) {this.shortcode.set(shortcode);}

    public String getName()                    {return name.get();}

    public void setName(String name)           {this.name.set(name);}

    public String getEmail()                   {return email.get();}

    public void setEmail(String email)         {this.email.set(email);}
}
