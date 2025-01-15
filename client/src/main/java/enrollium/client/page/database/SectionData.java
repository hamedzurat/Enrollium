package enrollium.client.page.database;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


class SectionData {
    private final SimpleStringProperty id;
    private final SimpleStringProperty name;
    private final SimpleStringProperty subject;
    private final SimpleStringProperty trimester;
    private final SimpleStringProperty maxCapacity;

    public SectionData(String id, String name, String subject, String trimester, String maxCapacity) {
        this.id          = new SimpleStringProperty(id);
        this.name        = new SimpleStringProperty(name);
        this.subject     = new SimpleStringProperty(subject);
        this.trimester   = new SimpleStringProperty(trimester);
        this.maxCapacity = new SimpleStringProperty(maxCapacity);
    }

    public StringProperty idProperty()             {return id;}

    public StringProperty nameProperty()           {return name;}

    public StringProperty subjectProperty()        {return subject;}

    public StringProperty trimesterProperty()      {return trimester;}

    public StringProperty capacityProperty()       {return maxCapacity;}

    public String getName()                        {return name.get();}

    public void setName(String name)               {this.name.set(name);}

    public String getSubject()                     {return subject.get();}

    public void setSubject(String subject)         {this.subject.set(subject);}

    public String getTrimester()                   {return trimester.get();}

    public void setTrimester(String trimester)     {this.trimester.set(trimester);}

    public String getMaxCapacity()                 {return maxCapacity.get();}

    public void setMaxCapacity(String maxCapacity) {this.maxCapacity.set(maxCapacity);}
}
