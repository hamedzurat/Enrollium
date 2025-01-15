package enrollium.client.page.database;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


class TrimesterData {
    private final SimpleStringProperty id;
    private final SimpleStringProperty code;
    private final SimpleStringProperty year;
    private final SimpleStringProperty season;
    private final SimpleStringProperty status;

    public TrimesterData(String id, String code, String year, String season, String status) {
        this.id     = new SimpleStringProperty(id);
        this.code   = new SimpleStringProperty(code);
        this.year   = new SimpleStringProperty(year);
        this.season = new SimpleStringProperty(season);
        this.status = new SimpleStringProperty(status);
    }

    public StringProperty idProperty()     {return id;}

    public StringProperty codeProperty()   {return code;}

    public StringProperty yearProperty()   {return year;}

    public StringProperty seasonProperty() {return season;}

    public StringProperty statusProperty() {return status;}

    public String getCode()                {return code.get();}

    public void setCode(String code)       {this.code.set(code);}

    public String getYear()                {return year.get();}

    public void setYear(String year)       {this.year.set(year);}

    public String getSeason()              {return season.get();}

    public void setSeason(String season)   {this.season.set(season);}

    public String getStatus()              {return status.get();}

    public void setStatus(String status)   {this.status.set(status);}
}
