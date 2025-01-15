package enrollium.client.page.database;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


class NotificationData {
    private final SimpleStringProperty id;
    private final SimpleStringProperty title;
    private final SimpleStringProperty content;
    private final SimpleStringProperty category;
    private final SimpleStringProperty scope;

    public NotificationData(String id, String title, String content, String category, String scope) {
        this.id       = new SimpleStringProperty(id);
        this.title    = new SimpleStringProperty(title);
        this.content  = new SimpleStringProperty(content);
        this.category = new SimpleStringProperty(category);
        this.scope    = new SimpleStringProperty(scope);
    }

    public StringProperty idProperty()       {return id;}

    public StringProperty titleProperty()    {return title;}

    public StringProperty contentProperty()  {return content;}

    public StringProperty categoryProperty() {return category;}

    public StringProperty scopeProperty()    {return scope;}

    public String getTitle()                 {return title.get();}

    public void setTitle(String title)       {this.title.set(title);}

    public String getContent()               {return content.get();}

    public void setContent(String content)   {this.content.set(content);}

    public String getCategory()              {return category.get();}

    public void setCategory(String category) {this.category.set(category);}

    public String getScope()                 {return scope.get();}

    public void setScope(String scope)       {this.scope.set(scope);}
}
