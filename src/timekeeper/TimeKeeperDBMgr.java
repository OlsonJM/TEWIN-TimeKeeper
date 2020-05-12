/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timekeeper;
import java.util.Vector;
import java.sql.*;
import java.util.Calendar;

/**
 *
 * @author E0051024
 */
public class TimeKeeperDBMgr {

    DataConnection dc;

    public TimeKeeperDBMgr()
    {
        dc = new DataConnection();
    }

    public Vector<Object> getTimeKeeperRecords(){
        Vector<Object> results = new Vector<Object>();
        ResultSet rs = null;
        Calendar working = Calendar.getInstance();

        dc.connect();
        rs = dc.getTimeKeeperRecords();
        try
        {
            while(rs.next()){
                working.set(Calendar.YEAR, rs.getInt("Year"));
                working.set(Calendar.MONTH, rs.getInt("Month"));
                working.set(Calendar.DAY_OF_YEAR, rs.getInt("Day"));
            }
            results.add(new Object[]{(Calendar)working.clone(), rs.getInt("ID"),
                rs.getInt("LogNumber")});

        }catch (Exception e)
        {
            System.out.println(e);
        }
        dc.closeConnection();
        return results;
    }

    public void saveTimeKeeperRecord()
    {

    }

    public void updateTimeKeeperRecord()
    {

        
    }



}
