package enrollium.client.page.database;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


class SpaceTimeData {
    private final SimpleStringProperty id;
    private final SimpleStringProperty roomName;
    private final SimpleStringProperty roomNumber;
    private final SimpleStringProperty dayOfWeek;
    private final SimpleStringProperty timeSlot;

    public SpaceTimeData(String id, String roomName, String roomNumber, String dayOfWeek, String timeSlot) {
        this.id         = new SimpleStringProperty(id);
        this.roomName   = new SimpleStringProperty(roomName);
        this.roomNumber = new SimpleStringProperty(roomNumber);
        this.dayOfWeek  = new SimpleStringProperty(dayOfWeek);
        this.timeSlot   = new SimpleStringProperty(timeSlot);
    }

    public StringProperty idProperty()           {return id;}

    public StringProperty roomNameProperty()     {return roomName;}

    public StringProperty roomNumberProperty()   {return roomNumber;}

    public StringProperty dayOfWeekProperty()    {return dayOfWeek;}

    public StringProperty timeSlotProperty()     {return timeSlot;}

    public String getRoomName()                  {return roomName.get();}

    public void setRoomName(String roomName)     {this.roomName.set(roomName);}

    public String getRoomNumber()                {return roomNumber.get();}

    public void setRoomNumber(String roomNumber) {this.roomNumber.set(roomNumber);}

    public String getDayOfWeek()                 {return dayOfWeek.get();}

    public void setDayOfWeek(String dayOfWeek)   {this.dayOfWeek.set(dayOfWeek);}

    public String getTimeSlot()                  {return timeSlot.get();}

    public void setTimeSlot(String timeSlot)     {this.timeSlot.set(timeSlot);}
}
