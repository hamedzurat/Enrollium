package enrollium.client.page.database;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


class SubjectData {
    private final SimpleStringProperty id;
    private final SimpleStringProperty name;
    private final SimpleStringProperty codeName;
    private final SimpleStringProperty credits;
    private final SimpleStringProperty type;

    public SubjectData(String id, String name, String codeName, String credits, String type) {
        this.id       = new SimpleStringProperty(id);
        this.name     = new SimpleStringProperty(name);
        this.codeName = new SimpleStringProperty(codeName);
        this.credits  = new SimpleStringProperty(credits);
        this.type     = new SimpleStringProperty(type);
    }

    public StringProperty idProperty()       {return id;}

    public StringProperty nameProperty()     {return name;}

    public StringProperty codeNameProperty() {return codeName;}

    public StringProperty creditsProperty()  {return credits;}

    public StringProperty typeProperty()     {return type;}

    public String getName()                  {return name.get();}

    public void setName(String name)         {this.name.set(name);}

    public String getCodeName()              {return codeName.get();}

    public void setCodeName(String codeName) {this.codeName.set(codeName);}

    public String getCredits()               {return credits.get();}

    public void setCredits(String credits)   {this.credits.set(credits);}

    public String getType()                  {return type.get();}

    public void setType(String type)         {this.type.set(type);}
}
