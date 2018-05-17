package com.eksamen.eksamen.Controller;

import com.eksamen.eksamen.Base.Admin;
import com.eksamen.eksamen.Base.Leader;
import com.eksamen.eksamen.Base.Session;
import com.eksamen.eksamen.Base.Worker;
import com.eksamen.eksamen.Handler.DatabaseHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@RestController
public class AjaxController {

  /*
   * Method to get the employee list.
   * */
  @PostMapping(value = "/getEmployees")
  public ArrayList getEmployeeList(@RequestParam("checkboxesLocation[]") int[] filterLocations) {
    ArrayList<ArrayList<String>> staffs = new ArrayList<>();
    ArrayList<String> columnLabels = new ArrayList<>();

    //Is used in the query to filter the employees to the selected locations
    String filter = "";
    if(filterLocations.length >= 0) {
      filter += "location_id = '" + filterLocations[0] + "' ";

      if (filterLocations.length > 1) {
        for (int i = 1; i < filterLocations.length; i++) { //need to skip the first placeholder in the arraylist
          filter += " OR location_id = '" + filterLocations[i] + "'";
        }
      }
    }

    //If it's the admin then he should also see phone and email info on the employees
    String isAdmin = "";
    if (Session.getUserniveau() == 15) {
      isAdmin += "phone AS Telefonnummer, " +
          "email AS Email, ";
    }

    staffs.add(getColumnLabel(columnLabels, isAdmin));

    //Firstly adding all the admins to the list
    isAdmin(isAdmin, filter, columnLabels, staffs);

    //Secondly the leaders
    isLeader(isAdmin, filter, columnLabels, staffs);

    //Thirdly the workers
    isWorker(isAdmin, filter, columnLabels, staffs);

    return staffs;
  }

  @PostMapping("/getProfile")
  public ArrayList<ArrayList<String>> getUser() {

    ArrayList<ArrayList<String>> user = new ArrayList<>();
    ArrayList<String> temp = new ArrayList<>();
    ArrayList<String> temp2 = new ArrayList<>();
    int userID = Session.getId();
    ResultSet userRS = DatabaseHandler.getInstance()
        .querySelect("select firstname, lastname, email, phone, niveau_name\n" +
            "from staff\n" +
            "inner join staff_niveau n on staff.fk_staff_niveau_id = n.staff_niveau_id\n" +
            "where staff_id = " + userID);


    try {
      userRS.next();
      for (int i = 1; i < userRS.getMetaData().getColumnCount() + 1; i++) {
        temp.add(userRS.getString(i));
      }
      userRS.close();
      user.add(temp);

      ResultSet locationRS = DatabaseHandler.getInstance()
          .querySelect("select location_name from location\n" +
              "inner join staff_location l on location.location_id = l.fk_location_id\n" +
              "inner  join staff s on l.fk_staff_id = s.staff_id\n" +
              "where staff_id = " + userID);

      while (locationRS.next())
        temp2.add(locationRS.getString(1));
      user.add(temp2);
      locationRS.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return user;
  }

  @PostMapping("/getUserName")
  public ArrayList<String> getUserName() {

    ArrayList<String> userName = new ArrayList<>();
    int userID = Session.getId();
    ResultSet userNameRS = DatabaseHandler.getInstance()
        .querySelect("select firstname, lastname\n" +
            "from staff\n" +
            "where staff_id = " + userID);
    try {
      userNameRS.next();
      for (int i = 1; i < userNameRS.getMetaData().getColumnCount() + 1; i++) {
        userName.add(userNameRS.getString(i));
      }
      //userNameRS.close();

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return userName;
  }

  public ArrayList getColumnLabel(ArrayList columnLabels, String isAdmin) {

    try {
      ResultSet rs = DatabaseHandler.getInstance().querySelect(" SELECT " +
          "CONCAT(firstname, ' ', lastname) AS Navn, " +
          isAdmin +
          "location_name AS Anlæg, " +
          "niveau_name AS Stilling " +
          "FROM staff " +
          "INNER JOIN staff_location l ON staff.staff_id = l.fk_staff_id " +
          "INNER JOIN staff_niveau n ON staff.fk_staff_niveau_id = n.staff_niveau_id " +
          "INNER JOIN location l2 ON l.fk_location_id = l2.location_id "
      );

      //Adding the column names to an array
      for (int i = 1; i < rs.getMetaData().getColumnCount() + 1; i++) {
        columnLabels.add(rs.getMetaData().getColumnLabel(i));
      }

      rs.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return columnLabels;
  }

  public ArrayList isAdmin(String isAdmin, String filter, ArrayList columnLabels, ArrayList staffs) {
    Admin admin = new Admin();
    try {
      ResultSet rs = DatabaseHandler.getInstance().querySelect(" SELECT " +
          "CONCAT(firstname, ' ', lastname) AS Navn, " +
          isAdmin +
          "location_name AS Anlæg, " +
          "niveau_name AS Stilling " +
          "FROM staff " +
          "INNER JOIN staff_location l ON staff.staff_id = l.fk_staff_id " +
          "INNER JOIN staff_niveau n ON staff.fk_staff_niveau_id = n.staff_niveau_id " +
          "INNER JOIN location l2 ON l.fk_location_id = l2.location_id " +
          "WHERE (" + filter +") "+
          " AND staff_niveau_id = " + admin.getStaffNiveau() + " "
      );

      //Adds the employee information to the array
      while (rs.next()) {
        ArrayList<String> staff = new ArrayList<>();

        for (int i = 1; i < columnLabels.size() + 1; i++) {
          staff.add(rs.getString(i));
        }
        staffs.add(staff);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return staffs;
  }

  public ArrayList isLeader(String isAdmin, String filter, ArrayList columnLabels, ArrayList staffs){
    Leader leader = new Leader();
    try {
      ResultSet rs = DatabaseHandler.getInstance().querySelect(" SELECT " +
          "CONCAT(firstname, ' ', lastname) AS Navn, " +
          isAdmin +
          "location_name AS Anlæg, " +
          "niveau_name AS Stilling " +
          "FROM staff " +
          "INNER JOIN staff_location l ON staff.staff_id = l.fk_staff_id " +
          "INNER JOIN staff_niveau n ON staff.fk_staff_niveau_id = n.staff_niveau_id " +
          "INNER JOIN location l2 ON l.fk_location_id = l2.location_id " +
          "WHERE (" + filter +") "+
          " AND staff_niveau_id = " + leader.getStaffNiveau() + " "
      );

      //Adds the employee information to the array
      while (rs.next()) {
        ArrayList<String> staff = new ArrayList<>();

        for (int i = 1; i < columnLabels.size() + 1; i++) {
          staff.add(rs.getString(i));
        }
        staffs.add(staff);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return staffs;
  }

  public ArrayList isWorker(String isAdmin, String filter, ArrayList columnLabels, ArrayList staffs){
    Worker worker = new Worker();
    try {
      ResultSet rs = DatabaseHandler.getInstance().querySelect(" SELECT " +
          "CONCAT(firstname, ' ', lastname) AS Navn, " +
          isAdmin +
          "location_name AS Anlæg, " +
          "niveau_name AS Stilling " +
          "FROM staff " +
          "INNER JOIN staff_location l ON staff.staff_id = l.fk_staff_id " +
          "INNER JOIN staff_niveau n ON staff.fk_staff_niveau_id = n.staff_niveau_id " +
          "INNER JOIN location l2 ON l.fk_location_id = l2.location_id " +
          "WHERE (" + filter +") "+
          " AND staff_niveau_id = " + worker.getStaffNiveau() + " "
      );

      //Adds the employee information to the array
      while (rs.next()) {
        ArrayList<String> staff = new ArrayList<>();

        for (int i = 1; i < columnLabels.size() + 1; i++) {
          staff.add(rs.getString(i));
        }
        staffs.add(staff);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return staffs;
  }
}
