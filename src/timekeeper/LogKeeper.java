/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timekeeper;
import java.util.ArrayList;
import java.util.Calendar;
import tewin.DataConnection;

/**
 *
 * @author E0051024
 */
public class LogKeeper {
    private ArrayList<Object> log;
    private ArrayList<Object> events;
    private ArrayList<Object> tasks;
    private int taskKeeperID;
    //private TimeKeeperDBMgr logDB;
    private int saveEventIndex;
    private int saveTaskIndex;
    private boolean newDBrecord;

    public LogKeeper(){
        log = new ArrayList<Object>();
        events = new ArrayList<Object>();
        tasks = new ArrayList<Object>();
        //logDB = new TimeKeeperDBMgr();
        newDBrecord = true;
        taskKeeperID = -1;
        saveEventIndex = 0;
        saveTaskIndex = 0;
    }

    public void addEvent(Object event)
    {
        log.add(event);
    }

    public void addTask(Object task)
    {
        tasks.add(task);
    }

    public void lookupTaskKeeperID(java.util.Calendar date, int logID)
    {
        DataConnection dc = new DataConnection();
        dc.connect();
        taskKeeperID = dc.getTimeKeeperID(date, logID);
        dc.closeConnection();
    }


    public int saveRecords()
    {
        DataConnection dc = new DataConnection();
        dc.connect();
        Calendar now = Calendar.getInstance();
        //dc.createTimeKeeperTables();
        
        if(newDBrecord)
        {
            taskKeeperID = dc.addTimeKeeperLog(now);
            newDBrecord = false;
        }

        //save Tasks
        for(int i=saveTaskIndex;i<tasks.size();i++)
        {
            dc.addTimeKeeperTask(taskKeeperID, i, (String)tasks.get(i));
            saveTaskIndex++;
        }

        //save Events
        for(int i=saveEventIndex;i<events.size();i++)
        {
            Object[] temp = (Object[])events.get(i);
            dc.addTimeKeeperEvent(taskKeeperID, (String)temp[0], (Integer)temp[1], i, (Long)temp[2]);
            saveEventIndex++;
        }

        dc.closeConnection();

        return taskKeeperID;
    }

    public Integer getTaskKeeperID(){
        return this.taskKeeperID;
    }

    public String getTask(int taskNumber){
        return (String)tasks.get(taskNumber);
    }

    public Integer getTaskID(String task){
        return tasks.indexOf((String)task);
    }

    public void addTrackingEvent(Object[] event)
    {
        events.add(event);
    }

    public ArrayList getEvents()
    {
        return log;
    }

    public ArrayList<Object> getTrackingEvents(){
        return this.events;
    }

    public ArrayList<Object> getTrackingTasks()
    {
        return this.tasks;
    }

    public void clearLog()
    {
        log.clear();
        events.clear();
        newDBrecord = true;
        saveEventIndex = 0;
        saveTaskIndex = 0;
        taskKeeperID= -1;

    }

    public void setEventVector(ArrayList<Object> events)
    {
        this.events = events;
    }

    public void setLogVector(ArrayList<Object> log)
    {
        this.log = log;
    }

    public int getEventCount(){
        return log.size();
    }
}
