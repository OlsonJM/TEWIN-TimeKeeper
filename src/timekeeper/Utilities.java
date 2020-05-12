/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package timekeeper;
import java.sql.ResultSet;
import java.io.*;
import javax.swing.*;



/**
 * Contiains utility methods accessed by all classes
 * @author James Olson
 */
public class Utilities {
    private String version;
    private String company;

    public Utilities(){
        this.version = "3.01b";
        this.company = "Eaton Corporation";
    }

    public String getVersion(){
        return this.version;
    }

    public String getCompany(){
        return this.company;
    }

    public boolean checkForCommas(String test){
        boolean commas = false;

        if(test == null)
            return false;

        if(test.contains(","))
            commas = true;
        else
            commas = false;

        return commas;
    }

    public String validateCom(String test){
        if(test == null)
            return "";
        else if(test.isEmpty())
            return "";
        else if(test.length() == 0)
            return "";
        else
            return test;

    }

    public Double validateHrs(String test){

        Double value;

        if(test==null)
            return 0.0;

        if(test.isEmpty())
            return 0.0;

        if(test.length() == 0)
            return 0.0;

        try{
            value = Double.valueOf(test);

        }catch(NumberFormatException e)
        {
            return 0.0;
        }

        if(value < 0)
            return 0.0;
        else
            return value;

    }

    public Object makeObj(final String item)  {
        return new Object() {@Override
            public String toString() { return item; } };
    }

    public JComboBox makeJobComboBox(int year, int weekNumber){
        javax.swing.JComboBox model = new JComboBox();
        DataConnection dc = new DataConnection();
        ResultSet jobs;
        String temp;
        boolean newJob;
        int ctr;

        dc.connect();
        jobs = dc.getJobList(year, weekNumber);

        try{
            while(jobs.next())
            {
                newJob = true;
                temp = jobs.getString(1);
                ctr = model.getModel().getSize();

                if(ctr>0){
                    //CHECK IF ALREADY ON LIST BEFORE ADDING
                    for(int i=0;i<ctr;i++){
                        if(temp.equalsIgnoreCase(model.getItemAt(i).toString()))
                            newJob = false;
                    }
                }
                if(newJob){
                         model.addItem(makeObj(temp));
                }
            }

        }
            catch (Exception e) {
                System.out.println(e);
                model = null;
        }
        dc.closeConnection();
        return model;

    }

    public void updateOTdb(int year, int week){
        DataConnection dcTE = new DataConnection();
        DataConnection dcOT = new DataConnection();
        ResultSet rs;
        Double totalST=0.0;
        Double totalTH=0.0;
        Double totalDT=0.0;
        Double monTot =0.0;
        Double tueTot =0.0;
        Double wedTot =0.0;
        Double thuTot =0.0;
        Double friTot =0.0;


        try{
            dcTE.connect();
            rs = dcTE.getWeekData(year, week);
            while(rs.next())
            {
                String labor = rs.getString(6);
                //sort by labor type
                if(labor.compareToIgnoreCase("EESD Time and Half")==0)
                {
                    totalTH+=rs.getDouble(7);
                    totalTH+=rs.getDouble(9);
                    totalTH+=rs.getDouble(11);
                    totalTH+=rs.getDouble(13);
                    totalTH+=rs.getDouble(15);
                    totalTH+=rs.getDouble(17);
                    totalTH+=rs.getDouble(19);
                }else if (labor.compareToIgnoreCase("EESD Double Time")==0)
                {
                    totalDT+=rs.getDouble(7);
                    totalDT+=rs.getDouble(9);
                    totalDT+=rs.getDouble(11);
                    totalDT+=rs.getDouble(13);
                    totalDT+=rs.getDouble(15);
                    totalDT+=rs.getDouble(17);
                    totalDT+=rs.getDouble(19);
                }else
                {
                    monTot+=rs.getDouble(9);
                    tueTot+=rs.getDouble(11);
                    wedTot+=rs.getDouble(13);
                    thuTot+=rs.getDouble(15);
                    friTot+=rs.getDouble(17);
                }
            }

            //sum ST hours for week
            if(monTot>8)
                totalST+= (monTot-8);
            if(tueTot>8)
                totalST+= (tueTot-8);
            if(wedTot>8)
                totalST+= (wedTot-8);
            if(thuTot>8)
                totalST+= (thuTot-8);
            if(friTot>8)
                totalST+= (friTot-8);

            dcTE.closeConnection();
            dcOT.connect();
            rs = dcOT.getOTdata(year, week);

            Double otST;
            Double otTH;
            Double otDT;
            boolean STrcvd;
            boolean THrcvd;
            boolean DTrcvd;
            boolean changeST;
            boolean changeTH;
            boolean changeDT;

            int ID;

            if(rs.next())
            {
                ID = rs.getInt(1);
                otST = rs.getDouble(4);
                otTH = rs.getDouble(5);
                otDT = rs.getDouble(6);
                STrcvd = rs.getBoolean(7);
                THrcvd = rs.getBoolean(8);
                DTrcvd = rs.getBoolean(9);
            }else
            {
                //no entry for the week if OT exists add new entry
                if(totalST>0||totalTH>0||totalDT>0){
                    dcOT.addOThrs(year, week, totalST, totalTH, totalDT);
                }

                //add sucessfull return to program
                return;
            }

            //check if data in OT db is same as data in TE database. if not update

            if(STrcvd&&!(Double.toString(totalST).equals(Double.toString(otST))))
                changeST = false;
            else
                changeST = STrcvd;

            if(THrcvd&&!(Double.toString(totalTH).equals(Double.toString(otTH))))
                changeTH = false;
            else
                changeTH = THrcvd;

            if(DTrcvd&&!(Double.toString(totalDT).equals(Double.toString(otDT))))
                changeDT = false;
            else
                changeDT = DTrcvd;
 
            //update OT Database
            dcOT.updateOThrs(ID, totalST, totalTH, totalDT, changeST, changeTH, changeDT);
            

            dcOT.closeConnection();

        }catch (Exception e)
        {
            System.out.println(e);
        }

        return;
    }


    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public int getDayKey(int dayOfWeek){
        int key;
        switch(dayOfWeek){
                case 1:
                    key = 7;
                    break;
                case 2:
                    key = 9;
                    break;
                case 3:
                    key = 11;
                    break;
                case 4:
                    key = 13;
                    break;
                case 5:
                    key = 15;
                    break;
                case 6:
                    key = 17;
                    break;
                case 7:
                    key = 19;
                    break;
                default:
                    key = 7;
                    break;
            }
        return key;

    }




}
