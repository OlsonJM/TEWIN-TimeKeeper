/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timekeeper;
import java.sql.*;
//import java.util.ArrayList;
import javax.swing.*;
import java.util.Calendar;
//import java.util.Locale;

/**
 *
 * @author James M. Olson
 */
public class DataConnection {
    private Connection conn;
    private String filename;
    private String database;
    private Statement s;
    private PreparedStatement ps;
    private boolean connected;

    DataConnection(){
        this.filename = getDBFile();
        this.database = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
        this.database+= this.filename.trim() + ";DriverID=22;READONLY=false}";
        connected = false;
    }

    /**
     * @param accepts filename of database
     * 
     */
    DataConnection(String filename){
        this.filename = filename;
        this.database = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
        this.database+= this.filename.trim() + ";DriverID=22;READONLY=true}";
        connected = false;

    }
        

    private String getDBFile(){
        String fn;
        SettingsForm sf = new SettingsForm();
        fn = sf.getDBFile();
        sf.dispose();
        return fn;
    }

    public boolean isConnected(){
        return connected;
    }

    public void connect(){
        try{
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            this.conn = DriverManager.getConnection(this.database ,"","");
            this.s = this.conn.createStatement(
                                      ResultSet.TYPE_SCROLL_INSENSITIVE,
                                      ResultSet.CONCUR_UPDATABLE);
            connected = true;

        }
            catch (Exception e) {
            connectionErr(e);
        }

    }


    public ResultSet getRecordCount(String dbName){
        ResultSet rs;
        String command = "SELECT COUNT(*) AS rowcount " +
                "FROM " + dbName;

        try{
            this.s.execute(command);
            rs = s.getResultSet();

        }
            catch (Exception e) {
            connectionErr(e);
            rs = null;
        }

        return rs;

    }

    /**
     * Selects time card data for week/year provided
     * @param year
     * @param weekNumber
     * @return ResultSet
     */

    public ResultSet getWeekData(int year, int weekNumber){
        ResultSet rs;
  
        String command = "SELECT * " +
                "FROM TimeData " +
                "WHERE (Week_Number=" + Integer.toString(weekNumber) +
                " AND Year =" + Integer.toString(year) + ")";

        try{
            this.s.execute(command);
            rs = s.getResultSet();

        }
            catch (Exception e) {
            connectionErr(e);
            rs = null;
        }

        return rs;
    }

    /**
     * Returns list of years currently in database
     * @return ResultSet
     */

    public ResultSet getYearList(){
        ResultSet rs;

        String command = "SELECT Year " +
                "FROM TimeData";

        try{
            this.s.execute(command);
            rs = s.getResultSet();

        }
            catch (Exception e) {
            connectionErr(e);
            rs = null;
        }

        return rs;
    }

    /**
     * Close connection and statments to database
     */

    public void closeConnection(){
        try{
            this.s.close();
            this.conn.close();
            connected = false;
        }
            catch (Exception e) {
            connectionErr(e);
        }

    }

    /**
     * get week totals for selected time card
     * @param year
     * @param weekNumber
     * @return ResultSet
     */

    public ResultSet getWeekTotals(int year, int weekNumber){
        ResultSet rs;

        String command = "SELECT Sum(Sunday_Hours) AS tSun "+
                ",Sum(Monday_Hours) AS tMon " +
                ",Sum(Tuesday_Hours) AS tTue " +
                ",Sum(Wednesday_Hours) AS tWed " +
                ",Sum(Thursday_Hours) AS tThu " +
                ",Sum(Friday_Hours) AS tFri " +
                ",Sum(Saturday_Hours) AS tSat "+
                "FROM TimeData " +
                "WHERE (Week_Number=" + Integer.toString(weekNumber) +
                " AND Year= " + Integer.toString(year) + ")";
        try{
            this.s.execute(command);
            rs = s.getResultSet();
        }
            catch (Exception e) {
            connectionErr(e);
            rs = null;
        }
        return rs;

    }

    /**
     * Gets comment for selected time card
     * @param year
     * @param weekNumber
     * @return ResultSet
     */

    public ResultSet getComment(int year, int weekNumber){
        ResultSet rs;
        String command =  "SELECT * " +
                "FROM Comment " +
                "WHERE (Week_Number=" + Integer.toString(weekNumber) +
                "AND Year=" +Integer.toString(year)+")";
        try{
            this.s.execute(command);
            rs = s.getResultSet();
        }
            catch (Exception e) {
            connectionErr(e);
            rs = null;
        }

        return rs;

    }

    /**
     * Select job numbers for week in range +1 week to -3 weeks
     * @param year
     * @param weekNumber
     * @return ResultSet
     */

    public ResultSet getJobList(int year,int weekNumber){
        ResultSet rs;
        Calendar weekSearch = Calendar.getInstance();
        weekSearch.set(Calendar.YEAR, year);
        weekSearch.set(Calendar.WEEK_OF_YEAR, weekNumber);
        weekSearch.set(Calendar.DAY_OF_WEEK, 7);

        //move back 4 weeks from current date
        weekSearch.add(Calendar.WEEK_OF_YEAR,-4);

        String command =  "SELECT Job_Number " +
                "FROM TimeData " +
                "WHERE ";

        //select job numbers starting 4 weeks prior to date to current week.
        for(int i=0;i<4;i++){
            command+="(Week_Number=" + Integer.toString(weekSearch.get(Calendar.WEEK_OF_YEAR)) +
                " AND Year = " + Integer.toString(weekSearch.get(Calendar.YEAR)) +")" + " Or ";
            weekSearch.add(Calendar.WEEK_OF_YEAR,1);
        }

        //Add one week ahead of current week.
        command+="(Week_Number = " + Integer.toString(weekSearch.get(Calendar.WEEK_OF_YEAR)) +
                " AND Year = " + Integer.toString(weekSearch.get(Calendar.YEAR)) +")";

        try{
            this.s.execute(command);
            rs = s.getResultSet();
        }
            catch (Exception e) {
            connectionErr(e);
            rs = null;
        }

        return rs;

    }

    public void setComment(int ID, int year, int weekNumber, String comment, boolean newCom){


        try{
            if(newCom){
                this.ps = this.conn.prepareStatement("INSERT INTO Comment " +
                        "(Year, Week_Number, TC_Comment) values(?,?,?)");
                this.ps.setInt(1,year);
                this.ps.setInt(2, weekNumber);
                this.ps.setString(3, comment);
                this.ps.executeUpdate();

            }else
            {
                this.ps = this.conn.prepareStatement("UPDATE Comment " +
                    "SET TC_Comment = ? WHERE ID = ?");
                this.ps.setString(1, comment);
                this.ps.setInt(2, ID);
                this.ps.executeUpdate();

            }
            this.ps.close();
        }
        catch (Exception e) {
            connectionErr(e);

        }
    }

    public boolean setWeekData(Integer[] data, Double[] hrs, String[] com, boolean newTC){
        boolean pass = false;
        try{
            if(newTC){
                this.ps = this.conn.prepareStatement("INSERT INTO TimeData " +
                        "(Year,Week_Number,Job_Number,Task_ID,Labor_Type,Sunday_Hours,"+
                        "Sunday_Comment,Monday_Hours,Monday_Comment,Tuesday_Hours," +
                        "Tuesday_Comment,Wednesday_Hours,Wednesday_Comment,Thursday_Hours,"+
                        "Thursday_Comment,Friday_Hours,Friday_Comment,Saturday_Hours,"+
                        "Saturday_Comment) "+
                        "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                    this.ps.setInt(1, data[0]);
                    this.ps.setInt(2, data[1]);
                    this.ps.setString(3, com[0]);
                    this.ps.setString(4, com[1]);
                    this.ps.setString(5, com[2]);
                    this.ps.setDouble(6, hrs[0]);
                    this.ps.setString(7, com[3]);
                    this.ps.setDouble(8, hrs[1]);
                    this.ps.setString(9, com[4]);
                    this.ps.setDouble(10, hrs[2]);
                    this.ps.setString(11, com[5]);
                    this.ps.setDouble(12, hrs[3]);
                    this.ps.setString(13, com[6]);
                    this.ps.setDouble(14, hrs[4]);
                    this.ps.setString(15, com[7]);
                    this.ps.setDouble(16, hrs[5]);
                    this.ps.setString(17, com[8]);
                    this.ps.setDouble(18, hrs[6]);
                    this.ps.setString(19, com[9]);

                    this.ps.executeUpdate();
                    pass = true;
            }else
            {
                //ADD CODE TO UPDATE
                this.ps = this.conn.prepareStatement("UPDATE TimeData " +
                        "SET Job_Number =?,Task_ID =?,Labor_Type =?,Sunday_Hours=?,"+
                        "Sunday_Comment=?,Monday_Hours=?,Monday_Comment=?,Tuesday_Hours=?," +
                        "Tuesday_Comment=?,Wednesday_Hours=?,Wednesday_Comment=?,Thursday_Hours=?,"+
                        "Thursday_Comment=?,Friday_Hours=?,Friday_Comment=?,Saturday_Hours=?,"+
                        "Saturday_Comment=? " +
                        "WHERE (ID = " + data[2] + ")");
                    this.ps.setString(1, com[0]);
                    this.ps.setString(2, com[1]);
                    this.ps.setString(3, com[2]);
                    this.ps.setDouble(4, hrs[0]);
                    this.ps.setString(5, com[3]);
                    this.ps.setDouble(6, hrs[1]);
                    this.ps.setString(7, com[4]);
                    this.ps.setDouble(8, hrs[2]);
                    this.ps.setString(9, com[5]);
                    this.ps.setDouble(10, hrs[3]);
                    this.ps.setString(11, com[6]);
                    this.ps.setDouble(12, hrs[4]);
                    this.ps.setString(13, com[7]);
                    this.ps.setDouble(14, hrs[5]);
                    this.ps.setString(15, com[8]);
                    this.ps.setDouble(16, hrs[6]);
                    this.ps.setString(17, com[9]);

                    this.ps.executeUpdate();
                    pass = true;
            }
            this.ps.close();
            
        }
        catch (Exception e) {
            connectionErr(e);
            pass = false;
        }
        return pass;

    }

    private void connectionErr(Exception e){
        JOptionPane.showMessageDialog(null, "DataConnection Error: " + e);
    }

    
    public int getRecordCount(int year, int week){
        ResultSet rs;
        String command = "SELECT COUNT (*) FROM TimeData " +
                "WHERE (Year = " + Integer.toString(year) +
                " AND Week_Number = " + Integer.toString(week) + " )";

        int count= 0;

        try{
            this.s.execute(command);
            rs = s.getResultSet();
            if(rs.next())
                count = rs.getInt(1);
            else
                count = 0;
            rs.close();
        }
            catch (Exception e) {
                connectionErr(e);
                count = 0;
        }

        return count;
    }

    public boolean deleteRecord(int ID){
        String command = "DELETE FROM TimeData WHERE (ID = " + ID + ")";
        boolean sucess;

        try{
            this.s.execute(command);
            sucess = true;
        }
            catch (Exception e) {
            connectionErr(e);
            sucess = false;
        }

        return sucess;

    }

    public boolean deleteOTrecord(int ID){
        String command = "DELETE FROM OT WHERE (ID = " + ID + ")";
        boolean sucess;

        try{
            this.s.execute(command);
            sucess = true;
        }
            catch (Exception e) {
            connectionErr(e);
            sucess = false;
        }

        return sucess;

    }


    public void deleteComment(int ID){
        String command = "DELETE FROM Comment " +
                "WHERE (ID = " + Integer.toString(ID) + " )";
        try{
            this.s.execute(command);
        }
            catch (Exception e) {
            connectionErr(e);
        }

    }

    public void deleteExpense(int ID){
        String command = "DELETE FROM Expense " +
                "WHERE (ID = " + Integer.toString(ID) + " )";
        try{
            this.s.execute(command);
        }
            catch (Exception e) {
            connectionErr(e);
        }
    }

    public ResultSet dbQuery(String query){
        ResultSet rs;
        String command = query;

        try{
            this.s.execute(command);
            rs = s.getResultSet();
        }
            catch (Exception e) {
                connectionErr(e);
                rs = null;
        }

        return rs;

    }

    public void saveDayChange(int ID, int year, int week, int dayKey, String data[], Double hours){

        String hrs=null;
        String com=null;

        switch(dayKey){
            case 1:
                hrs = "Sunday_Hours";
                com = "Sunday_Comment";
                break;
            case 2:
                hrs = "Monday_Hours";
                com = "Monday_Comment";
                break;
            case 3:
                hrs = "Tuesday_Hours";
                com = "Tuesday_Comment";
                break;
            case 4:
                hrs = "Wednesday_Hours";
                com = "Wednesday_Comment";
                break;
            case 5:
                hrs = "Thursday_Hours";
                com = "Thursday_Comment";
                break;
            case 6:
                hrs = "Friday_Hours";
                com = "Friday_Comment";
                break;
            case 7:
                hrs = "Saturday_Hours";
                com = "Saturday_Comment";
                break;
            default:
                hrs = null;
                com = null;
                break;

        }
        String statement=null;
        if(ID==0){

            statement = "INSERT INTO TimeData " +
                               "(Job_Number,Task_ID,Labor_Type," +
                               hrs + "," + com + ",Year, Week_Number)" +
                               "values (?,?,?,?,?,?,?)";
        }else
        {
            statement = "UPDATE TimeData " +
                               "SET Job_Number =?,Task_ID =?,Labor_Type =?, " +
                               hrs + " =?, " + com + " =? " +
                               "WHERE (ID = ?)";
        }

        if(hrs!=null&&statement!=null){
            try{
            this.ps = this.conn.prepareStatement(statement);
                        this.ps.setString(1, data[0]);
                        this.ps.setString(2, data[1]);
                        this.ps.setString(3, data[2]);
                        this.ps.setDouble(4, hours);
                        this.ps.setString(5, data[3]);
                        if(ID!=0){
                            this.ps.setInt(6, ID);
                        }else if(ID==0)
                        {
                            this.ps.setInt(6, year);
                            this.ps.setInt(7,week);
                        }
                        this.ps.executeUpdate();
            }
            catch (Exception e) {
                    connectionErr(e);
            }
        }
    }

    public void addOThrs(int year,int week, Double st, Double th, Double dt){
        try{
            
            this.ps = this.conn.prepareStatement("INSERT INTO OT " +
                "(Year,Week,ST,TH,DT,RcvdST,RcvdTH,RcvdDT) "+
                "values(?,?,?,?,?,?,?,?)");
            this.ps.setInt(1, year);
            this.ps.setInt(2, week);
            this.ps.setDouble(3, st);
            this.ps.setDouble(4, th);
            this.ps.setDouble(5, dt);
            this.ps.setBoolean(6, false);
            this.ps.setBoolean(7, false);
            this.ps.setBoolean(8, false);
            this.ps.executeUpdate();
            this.ps.close();
        }
        catch (Exception e) {
            connectionErr(e);

        }
    }

    public void updateOThrs(int ID, Double st, Double th, Double dt, boolean stu, boolean thu, boolean dtu){
        try{

            this.ps = this.conn.prepareStatement("UPDATE OT " +
                "SET ST = ?, TH = ?, DT = ?, RcvdST = ?, RcvdTH = ?, RcvdDT = ? " +
                "WHERE ID = ?");
            this.ps.setDouble(1, st);
            this.ps.setDouble(2, th);
            this.ps.setDouble(3, dt);
            this.ps.setBoolean(4, stu);
            this.ps.setBoolean(5, thu);
            this.ps.setBoolean(6, dtu);
            this.ps.setInt(7, ID);
            this.ps.executeUpdate();
            this.ps.close();
        }
        catch (Exception e) {
            connectionErr(e);

        }
    }

    public void updateOTflags(int ID, boolean ST, boolean TH, boolean DT){
        try{

            this.ps = this.conn.prepareStatement("UPDATE OT " +
                "SET RcvdST = ?, RcvdTH = ?, RcvdDT = ? WHERE " +
                "(ID = ? )");
            this.ps.setBoolean(1, ST);
            this.ps.setBoolean(2, TH);
            this.ps.setBoolean(3, DT);
            this.ps.setInt(4, ID);
            this.ps.executeUpdate();
            this.ps.close();
        }
        catch (Exception e) {
            connectionErr(e);

        }
    }

    public void updatePayHrs(int ID, int year, int month, int day, Double stHrs, Double thHrs,
            Double dtHrs, boolean ST, boolean TH, boolean DT){
        try{

            this.ps = this.conn.prepareStatement("UPDATE PayHistory " +
                "SET PayYear = ?, PayMonth=?, PayDay =?, ST = ?, TH = ?, DT = ?, RcvdST = ?, RcvdTH = ?, RcvdDT = ? " +
                "WHERE (ID = ?)");

            this.ps.setInt(1, year);
            this.ps.setInt(2, month);
            this.ps.setInt(3, day);
            this.ps.setDouble(4, stHrs);
            this.ps.setDouble(5, thHrs);
            this.ps.setDouble(6, dtHrs);
            this.ps.setBoolean(7, ST);
            this.ps.setBoolean(8, TH);
            this.ps.setBoolean(9, DT);
            this.ps.setInt(10, ID);
            this.ps.executeUpdate();
            this.ps.close();
        }
        catch (Exception e) {
            connectionErr(e);

        }
    }

    public void delPayHrs(int ID){
        String command ="DELETE FROM PayHistory " +
                "WHERE ID = " + Integer.toString(ID);
        try{
            this.s.execute(command);

        }
            catch (Exception e) {
            connectionErr(e);
        }

    }


    public int isPayExisting(java.util.Calendar check){
        int ID = -1;
        ResultSet rs;

        String command = "SELECT * FROM PayHistory WHERE ( " +
                "PayYear = " + Integer.toString(check.get(Calendar.YEAR)) +
                " AND PayMonth = " + Integer.toString(check.get(Calendar.MONTH)) +
                " AND PayDay = " + Integer.toString(check.get(Calendar.DAY_OF_MONTH)) +
                " )";
        try{
            this.s.execute(command);
            rs = s.getResultSet();
            if(rs.next())
            {
                ID = rs.getInt("ID");
            }
        }
            catch (Exception e) {
            connectionErr(e);
        }


        return ID;
    }

    public void addPayHrs(int year, int month, int day,Double stHrs, Double thHrs,
            Double dtHrs, boolean ST, boolean TH, boolean DT){
        try{

            this.ps = this.conn.prepareStatement("INSERT INTO PayHistory " +
                "(PayYear,PayMonth,PayDay,ST,TH,DT,RcvdST,RcvdTH,RcvdDT) " +
                "values(?,?,?,?,?,?,?,?,?)");
            this.ps.setInt(1,year);
            this.ps.setInt(2,month);
            this.ps.setInt(3, day);
            this.ps.setDouble(4, stHrs);
            this.ps.setDouble(5, thHrs);
            this.ps.setDouble(6, dtHrs);
            this.ps.setBoolean(7, ST);
            this.ps.setBoolean(8, TH);
            this.ps.setBoolean(9, DT);
            this.ps.executeUpdate();
            this.ps.close();
        }
        catch (Exception e) {
            connectionErr(e);

        }

    }

    public ResultSet getPayHistory(java.sql.Date date){
        ResultSet rs=null;
        String dateStr = date.toString();
        String command ="SELECT * FROM PayHistory " +
                "WHERE PayDate >= " + dateStr;

        try{
            this.s.execute(command);
            rs = s.getResultSet();
        }
            catch (Exception e) {
            connectionErr(e);
            rs = null;
        }


        return rs;
    }

    public ResultSet getOTdata(int year, int week){
        ResultSet rs=null;
        String command ="SELECT * FROM OT " +
                "WHERE Year = " + Integer.toString(year) +
                " AND Week = " + Integer.toString(week);

        try{
            this.s.execute(command);
            rs = s.getResultSet();
        }
            catch (Exception e) {
            connectionErr(e);
            rs = null;
        }

        return rs;


    }

    public ResultSet getExpenses(int lineID,int day)
    {
        ResultSet rs = null;
        String command ="SELECT * FROM Expense " +
                "WHERE (LineID = " + Integer.toString(lineID) + 
                "AND ExpDayOfWeek = " + Integer.toString(day) + ")";

        try{
            this.s.execute(command);
            rs = s.getResultSet();
        }
            catch (Exception e) {
            connectionErr(e);
            rs = null;
        }

        return rs;
    }

    public void addExpense(int lineID, int year, int week, int dayOfWeek, String jobNumber, String taskID,
            String expType, String pmtType, double expTotal, String expComment){
        try{

            this.ps = this.conn.prepareStatement("INSERT INTO Expense " +
                "(LineID,Job_Number,Task_ID,Year,Week_Number,ExpDayOfWeek,ExpType,PmtType,ExpTotal,ExpComment) " +
                "values(?,?,?,?,?,?,?,?,?,?)");
            this.ps.setInt(1, lineID);
            this.ps.setString(2,jobNumber);
            this.ps.setString(3,taskID);
            this.ps.setInt(4, year);
            this.ps.setInt(5, week);
            this.ps.setInt(6, dayOfWeek);
            this.ps.setString(7, expType);
            this.ps.setString(8, pmtType);
            this.ps.setDouble(9, expTotal);
            this.ps.setString(10, expComment);
            this.ps.executeUpdate();
            this.ps.close();
        }
        catch (Exception e) {
            connectionErr(e);

        }

    }

    public void updateExpense(int ID, int day, String jobNumber, String taskID,
            String expType, String pmtType, double expTotal, String expComment){
        try{

            this.ps = this.conn.prepareStatement("UPDATE Expense " +
                "SET Job_Number = ?, Task_ID=?, ExpType = ?, PmtType = ?, ExpTotal = ?, ExpComment = ? " +
                "WHERE (ID = ? AND ExpDayOfWeek = ?)");

            this.ps.setString(1,jobNumber);
            this.ps.setString(2,taskID);
            this.ps.setString(3, expType);
            this.ps.setString(4, pmtType);
            this.ps.setDouble(5, expTotal);
            this.ps.setString(6, expComment);
            this.ps.setInt(7, ID);
            this.ps.setInt(8, day);
            this.ps.executeUpdate();
            this.ps.close();
        }
        catch (Exception e) {
            connectionErr(e);

        }
    }

    public void updateExpJobInfo(int lineID, String jobNumber, String taskID)
    {
        try{

            this.ps = this.conn.prepareStatement("UPDATE Expense " +
                "SET Job_Number = ?, Task_ID=? " +
                "WHERE (LineID = ?)");

            this.ps.setString(1,jobNumber);
            this.ps.setString(2,taskID);
            this.ps.setInt(3, lineID);
            this.ps.executeUpdate();
            this.ps.close();
        }
        catch (Exception e) {
            connectionErr(e);

        }

    }

    public void deleteLineExpenses(int lineID){
        String command ="DELETE FROM Expense " +
            "WHERE LineID = " + Integer.toString(lineID);
        try{
            this.s.execute(command);
        }catch (Exception e)
        {
            connectionErr(e);
        }
    }
    
    public boolean createOTtablesInMDB(){
        Boolean success;
        String otTbl = "CREATE TABLE OT (" +
                "ID AUTOINCREMENT CONSTRAINT PKEY Primary Key, " +
                "Year INTEGER, " +
                "Week INTEGER, " +
                "ST DOUBLE, " +
                "TH DOUBLE, " +
                "DT DOUBLE, " +
                "RcvdST BIT , " +
                "RcvdTH BIT, " +
                "RcvdDT BIT " +
                ")";


        String payTbl = "CREATE TABLE PayHistory (" +
                "ID AUTOINCREMENT CONSTRAINT PKEY Primary Key, " +
                "PayYear INTEGER, " +
                "PayMonth INTEGER, " +
                "PayDay INTEGER, " +
                "ST DOUBLE, " +
                "TH DOUBLE, " +
                "DT DOUBLE, " +
                "RcvdST BIT , " +
                "RcvdTH BIT, " +
                "RcvdDT BIT " +
                ")";

        String expTbl = "CREATE TABLE Expense (" +
                "ID AUTOINCREMENT CONSTRAINT PKEY Primary Key, " +
                "Year INTEGER, " +
                "Week_Number INTEGER, " +
                "Job_Number VARCHAR(30), " +
                "Task_ID VARCHAR(15), " +
                "ExpType VARCHAR(50), " +
                "PmtType VARCHAR(50), " +
                "LineID INTEGER, " +
                "ExpDayOfWeek INTEGER, " +
                "ExpTotal DOUBLE, " +
                "ExpComment VARCHAR(250) " +
                ")";

        try{
            this.s.executeUpdate(otTbl);
            this.s.executeUpdate(payTbl);
            this.s.executeUpdate(expTbl);
            success = true;
        }
            catch (Exception e) {
                connectionErr(e);
                success = false;

        }
        return success;

    }

    public boolean createBaseTables(){
        Boolean success = false;
        String timeTbl = "CREATE TABLE TimeData (" +
                "ID AUTOINCREMENT CONSTRAINT PKEY Primary Key, " +
                "Year INTEGER, " +
                "Week_Number INTEGER, " +
                "Job_Number VARCHAR(30), " +
                "Task_ID VARCHAR(20), " +
                "Labor_Type VARCHAR(25), " +
                "Sunday_Hours DOUBLE, " +
                "Sunday_Comment VARCHAR(100), " +
                "Monday_Hours DOUBLE, " +
                "Monday_Comment VARCHAR(100), " +
                "Tuesday_Hours DOUBLE, " +
                "Tuesday_Comment VARCHAR(100), " +
                "Wednesday_Hours DOUBLE, " +
                "Wednesday_Comment VARCHAR(100), " +
                "Thursday_Hours DOUBLE, " +
                "Thursday_Comment VARCHAR(100), " +
                "Friday_Hours DOUBLE, " +
                "Friday_Comment VARCHAR(100), " +
                "Saturday_Hours DOUBLE, " +
                "Saturday_Comment VARCHAR(100) " +
                ")";
        String commentTbl = "CREATE TABLE Comment (" +
                "ID AUTOINCREMENT CONSTRAINT PKEY Primary Key, " +
                "Year INTEGER, " +
                "Week_Number INTEGER, " +
                "TC_Comment VARCHAR(30) " +
                ")";

        try{
            this.s.executeUpdate(timeTbl);
            this.s.executeUpdate(commentTbl);
            success = true;
        }
            catch (Exception e) {
                connectionErr(e);
                success = false;

        }

        return success;
    }

    public boolean createTimeKeeperTables(){
        Boolean success = false;

        String tkTbl = "CREATE TABLE TimeKeeper (" +
                "ID AUTOINCREMENT CONSTRAINT PKEY Primary Key, " +
                "Year INTEGER, " +
                "Month INTEGER, " +
                "Day INTEGER, " +
                "LogNumber INTEGER )";
        String taskTbl = "CREATE TABLE TkTasks (" +
                "ID INTEGER, " +
                "TaskNumber INTEGER, " +
                "Task VARCHAR(50) " +
                ")";

        String eventTbl = "CREATE TABLE TkEvents (" +
                "ID INTEGER, " +
                "TaskNumber INTEGER, " +
                "EventType VARCHAR(50), " +
                "Sequence INTEGER, " +
                "TimeInMillis NUMERIC " +
                ")";

        String reportTbl = "CREATE TABLE TkReport (" +
                "ID INTEGER, " +
                "Line INTEGER, " +
                "Selected BIT, " +
                "TaskID VARCHAR(50), " +
                "JobNumber VARCHAR(50), " +
                "LaborType VARCHAR(25), " +
                "Comment VARCHAR(150) " +
                ")";

        try{
            this.s.executeUpdate(tkTbl);
            this.s.executeUpdate(taskTbl);
            this.s.executeUpdate(eventTbl);
            this.s.executeUpdate(reportTbl);
            success = true;
        }
            catch (Exception e) {
                connectionErr(e);
                success = false;

        }

        return success;

    }

    public Integer addTimeKeeperLog(Calendar date)
    {
        /**
         * check if there are any exising logs for the
         * current date.  add new & return ID
         */
        Integer count = getTimeKeeperCount(date);
        Integer newRecordID = -1;

        //Increase record count by 1
        count++;

        try{

            this.ps = this.conn.prepareStatement("INSERT INTO TimeKeeper " +
                "(Year,Month,Day,LogNumber) " +
                "values(?,?,?,?)");
            this.ps.setInt(1, date.get(Calendar.YEAR));
            this.ps.setInt(2, date.get(Calendar.MONTH));
            this.ps.setInt(3, date.get(Calendar.DAY_OF_MONTH));
            this.ps.setInt(4, count);
            this.ps.executeUpdate();
            this.ps.close();
        }
        catch (Exception e) {
            connectionErr(e);

        }

        newRecordID = getTimeKeeperID(date, count);

        return newRecordID;
    }

    public void addTimeKeeperTask(int ID, int taskNum, String task)
    {
        try{

            this.ps = this.conn.prepareStatement("INSERT INTO TkTasks " +
                "(ID,TaskNumber,Task) " +
                "values(?,?,?)");
            this.ps.setInt(1, ID);
            this.ps.setInt(2, taskNum);
            this.ps.setString(3, task);
            this.ps.executeUpdate();
            this.ps.close();
        }
        catch (Exception e) {
            connectionErr(e);

        }

    }

    public boolean addTimeKeeperEvent(int ID, String type, int taskNum,int seq, long time)
    {
        Boolean sucess = false;
        java.math.BigDecimal temp = java.math.BigDecimal.valueOf(time);


        try{

            this.ps = this.conn.prepareStatement("INSERT INTO TkEvents " +
                "(ID,TaskNumber,EventType,Sequence,TimeInMillis) " +
                "values(?,?,?,?,?)");
            this.ps.setInt(1, ID);
            this.ps.setInt(2, taskNum);
            this.ps.setString(3, type);
            this.ps.setInt(4, seq);
            this.ps.setBigDecimal(5, temp);
            this.ps.executeUpdate();
            this.ps.close();
            sucess = true;
        }
        catch (Exception e) {
            connectionErr(e);

        }

        return sucess;
        
    }

    public Integer getTimeKeeperID(java.util.Calendar date, int logNumber)
    {
        ResultSet rs;

        String command = "SELECT * FROM TimeKeeper " +
                "WHERE (Year = " + Integer.toString(date.get(Calendar.YEAR)) +
                " AND Month = " + Integer.toString(date.get(Calendar.MONTH)) +
                " AND Day = " + Integer.toString(date.get(Calendar.DAY_OF_MONTH)) +
                " AND LogNumber = " + Integer.toString(logNumber) + " )";

        int count= 0;

        try{
            this.s.execute(command);
            rs = s.getResultSet();
            if(rs.next())
                count = rs.getInt("ID");
            else
                count = -1;
            rs.close();
        }
            catch (Exception e) {
                connectionErr(e);
                count = -1;
        }

        return count;
    }

    public Integer getTimeKeeperCount(java.util.Calendar date){
        ResultSet rs;
        
        String command = "SELECT COUNT (*) FROM TimeKeeper " +
                "WHERE (Year = " + Integer.toString(date.get(Calendar.YEAR)) +
                " AND Month = " + Integer.toString(date.get(Calendar.MONTH)) +
                " AND Day = " + Integer.toString(date.get(Calendar.DAY_OF_MONTH)) + " )";

        int count= 0;

        try{
            this.s.execute(command);
            rs = s.getResultSet();
            if(rs.next())
                count = rs.getInt(1);
            else
                count = 0;
            rs.close();
        }
            catch (Exception e) {
                connectionErr(e);
                count = 0;
        }

        return count;
    }

    public void delTimeKeeperHistorical(java.util.Calendar now)
    {
        ResultSet rs;
        java.util.ArrayList<Integer> ID = new java.util.ArrayList<Integer>();
        boolean error = false;

        String command ="SELECT * FROM TimeKeeper " +
            "WHERE (Year <= " + Integer.toString(now.get(Calendar.YEAR-1)) +
            "AND Month = " + Integer.toString(now.get(Calendar.MONTH)) +
            "AND Day = " + Integer.toString(now.get(Calendar.DAY_OF_YEAR)) +
            " )";
        try{
            this.s.execute(command);
            rs = s.getResultSet();
            while(rs.next()){
                ID.add(rs.getInt("ID"));
            }
        }catch (Exception e)
        {
            connectionErr(e);
            error = true;
        }

        if(!error)
        {
            /* delete records matching ID's
             *
             */
            for(int i=0; i<ID.size();i++)
            {
                command ="DELETE FROM TimeKeeper " +
                    "WHERE ID = " + Integer.toString(ID.get(i));
                try{
                    this.s.execute(command);
                }catch (Exception e)
                {
                    connectionErr(e);
                }

                command ="DELETE FROM TkTasks " +
                    "WHERE ID = " + Integer.toString(ID.get(i));
                try{
                    this.s.execute(command);
                }catch (Exception e)
                {
                    connectionErr(e);
                }

                command ="DELETE FROM TkEvents " +
                    "WHERE ID = " + Integer.toString(ID.get(i));
                try{
                    this.s.execute(command);
                }catch (Exception e)
                {
                    connectionErr(e);
                }

                command ="DELETE FROM TkReport " +
                    "WHERE ID = " + Integer.toString(ID.get(i));
                try{
                    this.s.execute(command);
                }catch (Exception e)
                {
                    connectionErr(e);
                }

            }
        }
    }

    public ResultSet getTimeKeeperRecords()
    {
        ResultSet rs = null;
        String command ="SELECT * FROM TimeKeeper";

        try{
            this.s.execute(command);
            rs = s.getResultSet();
        }
            catch (Exception e) {
            connectionErr(e);
            rs = null;
        }

        return rs;
    }

    public java.util.ArrayList<String> getTaskButtons(int ID)
    {
        java.util.ArrayList<String> results = new java.util.ArrayList<String>();
        ResultSet rs = null;

        String command ="SELECT * FROM TkTasks WHERE ID = " + Integer.toString(ID);

        try{
            this.s.execute(command);
            rs = s.getResultSet();
            int temp =0;
            while(rs.next())
            {
                temp = rs.getInt("TaskNumber");
                results.add(temp, rs.getString("Task"));
            }
        }
            catch (Exception e) {
            connectionErr(e);
        }

        return results;
    }

    public java.util.ArrayList<Long> getTaskElapsedTime(int ID)
    {
        java.util.ArrayList<Long> results = new java.util.ArrayList<Long>();

        ResultSet rs = null;

        String command ="SELECT * FROM TkEvents WHERE ID = " + Integer.toString(ID);

        try{
            this.s.execute(command);
            rs = s.getResultSet();
            int temp =0;
            long totalizer;
            long holder;
            long start=0;
            boolean started = false;
            boolean stopped = false;
            long stop=0;
            boolean neither = false;
            int currentTask=-1;
            String type;
            while(rs.next())
            {
                neither = true;
                temp = rs.getInt("TaskNumber");
                try
                {
                    results.get(temp);
                }catch(Exception e)
                {
                    //check if adding out of sequence
                    if(temp>results.size())
                    {
                        for(int i = results.size()-1;i<=temp;i++)
                        {
                            try{
                                results.get(i);
                            }catch (Exception err)
                            {
                                results.add(i,0l);
                            }
                        }
                    }else
                        results.add(temp,0l);
                }

                totalizer = results.get(temp);
                type = rs.getString("EventType");
                if(type.equalsIgnoreCase("START")||type.equalsIgnoreCase("RESUME")){
                    start = rs.getLong("TimeInMillis");
                    currentTask = temp;
                    started = true;
                    stopped = false;
                    neither = false;
                }
                else if((type.equalsIgnoreCase("STOP")||type.equalsIgnoreCase("PAUSE"))&&started&&(currentTask==temp))
                {
                    stopped = true;
                    started = false;
                    neither = false;
                    stop = rs.getLong("TimeInMillis");
                }

                if(!neither&&stopped)
                {
                    totalizer+=stop-start;
                    holder = results.get(temp) + totalizer;
                    if(temp<results.size())
                        results.set(temp,holder);
                }

            }
        }
            catch (Exception e) {
            connectionErr(e);
        }


        return results;
    }

    public java.util.ArrayList<Object> getTrackEvents(int ID)
    {
        java.util.ArrayList<Object> results = new java.util.ArrayList<Object>();
        ResultSet rs = null;


        String command ="SELECT * FROM TkEvents WHERE ID = " + Integer.toString(ID);


        try{
            this.s.execute(command);
            rs = s.getResultSet();
            int temp =0;
            while(rs.next())
            {
                temp = rs.getInt("Sequence");
                results.add(temp, new Object[] {rs.getString("EventType"),rs.getInt("TaskNumber"),
                        rs.getLong("TimeInMillis")});
            }
        }
            catch (Exception e) {
            connectionErr(e);
        }

        return results;
    }

    public java.util.ArrayList<Object> getLogEvents(int ID, java.util.ArrayList<Object> events)
    {
        java.util.ArrayList<Object> log = new java.util.ArrayList<Object>();
        java.util.Calendar working = java.util.Calendar.getInstance();
        java.text.DateFormat dateFormat = new java.text.SimpleDateFormat("hh:mm:ss aa");
        Object[] holder;
        ResultSet rs = null;

        String command ="SELECT * FROM TkTasks WHERE (ID = " + Integer.toString(ID) +
                " AND TaskNumber = ";
        String query = "";


        try{
            for(int i=0;i<events.size();i++)
            {
                holder = (Object[])events.get(i);
                query = command;
                query+= holder[1].toString() + ")";
                this.s.execute(query);
                rs = s.getResultSet();
                if(rs.next())
                {
                   working.setTimeInMillis((Long)holder[2]);
                   log.add(holder[0] + " " +  rs.getString("Task") + " at "  +
                           dateFormat.format(working.getTime()));
                }

            }

        }
            catch (Exception e) {
            connectionErr(e);
        }

        return log;
    }

    public void setTkReportValues (int ID, int line, Object[] record)
    {
        try{
            this.ps = this.conn.prepareStatement("INSERT INTO TkReport " +
                "(ID, Line, Selected, JobNumber, TaskID, LaborType, Comment) " +
                "values(?,?,?,?,?,?,?)");
            this.ps.setInt(1, ID );
            this.ps.setInt(2, line);
            this.ps.setBoolean(3, (Boolean)record[0]);
            this.ps.setString(4, (String)record[1]);
            this.ps.setString(5, (String)record[2]);
            this.ps.setString(6, (String)record[3]);
            this.ps.setString(7, (String)record[4]);
            this.ps.executeUpdate();
            this.ps.close();

        }
        catch (Exception e) {
            connectionErr(e);

        }
    }

    public void updateTkReportValues (int ID, int line, Object[] record)
    {
        try{
            this.ps = this.conn.prepareStatement("UPDATE TkReport SET " +
                "Selected = ?, JobNumber = ?, TaskID = ?, LaborType = ?, Comment =? " +
                "WHERE(ID = ? AND Line = ?)");
            this.ps.setBoolean(1, (Boolean)record[0]);
            this.ps.setString(2, (String)record[1]);
            this.ps.setString(3, (String)record[2]);
            this.ps.setString(4, (String)record[3]);
            this.ps.setString(5, (String)record[4]);
            this.ps.setInt(6, ID );
            this.ps.setInt(7, line);
            this.ps.executeUpdate();
            this.ps.close();

        }
        catch (Exception e) {
            connectionErr(e);

        }


    }

    public java.util.ArrayList<Object[]> getReportData(int ID)
    {
        java.util.ArrayList<Object[]> lines = new java.util.ArrayList<Object[]>();
        ResultSet rs = null;


        String command ="SELECT * FROM TkReport WHERE ID = " + Integer.toString(ID) +
                " ORDER BY Line ASC";

        try{
            this.s.execute(command);
            rs = s.getResultSet();
            int ctr;
            while(rs.next())
            {
               
                ctr = rs.getInt("Line");
                boolean inc = rs.getBoolean("Selected");
                String job = rs.getString("JobNumber");
                String tid = rs.getString("TaskID");
                String lbr = rs.getString("LaborType");
                String com = rs.getString("Comment");
                String hrs = "";

                lines.add(ctr, new Object[]{inc,job,lbr,tid,hrs,com});

            }
        }
            catch (Exception e) {
            connectionErr(e);
        }

        return lines;
    }

    public boolean tk2teSave(int ID, int dayWeek, Double hrs, String com)
    {
        String command = "UPDATE TimeData SET ";
        boolean sucess = false;

        switch(dayWeek)
        {
            case 1:
                command+="Sunday_Hours = ?, Sunday_Comment = ? ";
                break;
            case 2:
                command+="Monday_Hours = ?, Monday_Comment = ? ";
                break;
            case 3:
                command+="Tuesday_Hours = ?, Tuesday_Comment = ? ";
                break;
            case 4:
                command+="Wednesday_Hours = ?, Wednesday_Comment = ? ";
                break;
            case 5:
                command+="Thursday_Hours = ?, Thursday_Comment = ? ";
                break;
            case 6:
                command+="Friday_Hours = ?, Friday_Comment = ? ";
                break;
            case 7:
                command+="Saturday_Hours = ?, Saturday_Comment = ? ";
                break;
            default:
                break;
        }

        command+=" WHERE (ID = ?)";


        try{
            this.ps = this.conn.prepareStatement(command);
            this.ps.setDouble(1, hrs);
            this.ps.setString(2, com);
            this.ps.setInt(3, ID);
            this.ps.executeUpdate();
            this.ps.close();
            sucess = true;
        }
        catch (Exception e) {
            connectionErr(e);
            sucess=false;
        }
        return sucess;
    }

}
