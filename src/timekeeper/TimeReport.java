/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TimeReport.java
 *
 * Created on Jun 18, 2010, 2:20:25 PM
 */

package timekeeper;
import java.util.ArrayList;
import javax.swing.JTextField;
import java.util.Calendar;
import tewin.*;


/**
 *
 * @author E0051024
 */
public class TimeReport extends javax.swing.JFrame {

    private ArrayList<Object> tasks;
    private ArrayList<Object> times;
    private ArrayList<Object> taskLine;
    private Utilities u;
    private Calendar now;
    private int ydim;
    private int xdim;
    private int currentLines;

    /** Creates new form TimeReport */
    public TimeReport() {
        taskLine = new ArrayList<Object>();
        tasks = new ArrayList<Object>();
        times = new ArrayList<Object>();
        u = new Utilities();
        now = Calendar.getInstance();
        currentLines=0;
        initComponents();
        xdim = lImport.getX()+4;
        ydim = lImport.getY();
    }
    
    public ArrayList<Object> updateReport(ArrayList<Object> tasks, ArrayList<Object> times)
    {
        //Update times on all records and add new
        if(tasks.size()>this.tasks.size())
        {
            for(int i=this.tasks.size();i<tasks.size();i++)
            {
                this.tasks.add(tasks.get(i));
                this.times.add(times.get(i));
            }
        }
        populateReport();
        return this.taskLine;

    }

    public void populateReport(){


        for(int i=0;i<tasks.size();i++)
        {
            if(i>=currentLines)
            {
                javax.swing.JComboBox job = u.makeJobComboBox(now.get(Calendar.YEAR), now.get(Calendar.WEEK_OF_YEAR));
                javax.swing.JTextField tid = new JTextField();
                javax.swing.JComboBox lbr = new javax.swing.JComboBox();
                javax.swing.JTextField hrs = new JTextField();
                javax.swing.JTextField com = new JTextField();
                javax.swing.JCheckBox inc = new  javax.swing.JCheckBox();

                //convert time 00:00:00 to hrs.min and populate text box
                String[] time = times.get(i).toString().split(":");
                Double tm1 = Double.parseDouble(time[0]);
                Double tm2 = Double.parseDouble(time[1])/60;
                Double tm3 = Double.parseDouble(time[2])/360;

                Double dblTime = roundTwoDecimals(tm1+tm2+tm3);
                hrs.setText(Double.toString(dblTime));

                //set comment box to task name
                com.setText(tasks.get(i).toString());

                //set job# to editable
                job.setEditable(true);

                //set comment & task field blank
                tid.setText("");
                inc.setSelected(true);
                lbr.addItem("EESD Labor");
                lbr.addItem("EESD Time and Half");
                lbr.addItem("EESD Double Time");
                lbr.addItem("EESD Unassigned");
                lbr.addItem("EESD Unapplied");
                lbr.addItem("EESD Authorized");
                lbr.addItem("EESD Holiday");
                lbr.addItem("EESD Vacation");
                lbr.addItem("EESD Safety");
                lbr.addItem("EESD Training");
                lbr.addItem("EESD Marketing");
                lbr.addItem("EESD Sick");
                lbr.setSelectedIndex(0);


                //add new task line components to panel
                this.add(inc);
                this.add(job);
                this.add(lbr);
                this.add(tid);
                this.add(hrs);
                this.add(com);

                //set component sizes
                inc.setSize(20,20);
                job.setSize(150, 20);
                lbr.setSize(120,20);
                tid.setSize(40,20);
                hrs.setSize(40, 20);
                com.setSize(200, 20);

                //ADD TO LINE VECTOR
                taskLine.add(new Object[]{inc,job,tid,lbr,hrs,com});

                //set locations
                ydim+=25;       //spacing between each task
                inc.setLocation(xdim,ydim);
                job.setLocation(xdim+45,ydim);
                tid.setLocation(xdim+200, ydim);
                lbr.setLocation(xdim+245, ydim);
                hrs.setLocation(xdim+370, ydim);
                com.setLocation(xdim+413, ydim);

                //update lincount
                currentLines++;

                //resize window
                int height = (int)this.getSize().getHeight() + 25;
                int width = (int)this.getSize().getWidth();
                this.setSize(width, height);
            }else
            {
                Object[] line = (Object[])taskLine.get(i);
                javax.swing.JTextField hrs = (JTextField)line[4];

                //Update Times
                String[] time = times.get(i).toString().split(":");
                Double tm1 = Double.parseDouble(time[0]);
                Double tm2 = Double.parseDouble(time[1])/60;
                Double tm3 = Double.parseDouble(time[2])/360;

                Double dblTime = roundTwoDecimals(tm1+tm2+tm3);
                hrs.setText(Double.toString(dblTime));
            }
            this.repaint();
        }//add next task or exit loop

    }

    private Double roundTwoDecimals(double d) {
        	java.text.DecimalFormat twoDForm = new java.text.DecimalFormat("#.##");
		return Double.valueOf(twoDForm.format(d));
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lJobList = new javax.swing.JLabel();
        lHrs = new javax.swing.JLabel();
        lComment = new javax.swing.JLabel();
        lImport = new javax.swing.JLabel();
        bImport = new javax.swing.JButton();
        lHrs1 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        bStore = new javax.swing.JButton();

        setTitle("TimeKeeper Report");
        setIconImages(null);

        lJobList.setText("Project Number");

        lHrs.setText("Hours");

        lComment.setText("Comment");

        lImport.setText("Export?");

        bImport.setText("Export to TEWIN");

        lHrs1.setText("Task");

        jLabel1.setText("Labor Type");

        bStore.setText("Save Entries to DB");
        bStore.setToolTipText("Saves Job# and Task#");
        bStore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bStoreActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(lImport)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lJobList, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(65, 65, 65)
                .addComponent(lHrs1)
                .addGap(28, 28, 28)
                .addComponent(jLabel1)
                .addGap(51, 51, 51)
                .addComponent(lHrs)
                .addGap(27, 27, 27)
                .addComponent(lComment)
                .addContainerGap(180, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bStore, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bImport, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bStore)
                    .addComponent(bImport))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(lJobList)
                    .addComponent(lImport)
                    .addComponent(lHrs1)
                    .addComponent(jLabel1)
                    .addComponent(lHrs)
                    .addComponent(lComment))
                .addContainerGap(45, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bStoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bStoreActionPerformed
        
    }//GEN-LAST:event_bStoreActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bImport;
    private javax.swing.JButton bStore;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lComment;
    private javax.swing.JLabel lHrs;
    private javax.swing.JLabel lHrs1;
    private javax.swing.JLabel lImport;
    private javax.swing.JLabel lJobList;
    // End of variables declaration//GEN-END:variables

}
