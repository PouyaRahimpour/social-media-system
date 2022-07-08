package com.example.instagram;

import com.example.instagram.models.FollowRequest;
import com.example.instagram.models.Post;
import com.example.instagram.models.User;
import javafx.scene.image.Image;

import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class SqlManager {
    private final String url = "jdbc:mysql://localhost/instagram";
    private final String username = "root";
    private final String password = "";
    private static SqlManager sqlManager = null;

    private SqlManager() {

    }

    public static SqlManager getInstance() {
        if (sqlManager == null) {
            return new SqlManager();
        }
        return sqlManager;
    }

    public Connection connectDb()  {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, username, password);
            return con;
        } catch (Exception e) {
            System.out.println("Unable to connect to database");
            return null;
        }
    }

    public boolean addUser(User user) {
        Connection con = connectDb();
        try {
            String sqlCmd = String.format("INSERT INTO `users` (username, password, fullName, mobileNumber, pageState) " +
                            "VALUES ('%s', '%s', '%s', '%s', '%s');"
                    , user.getUsername(), user.getPassword(), user.getFullName(), user.getMobileNumber(), user.getPageState());
            Statement s = con.prepareStatement(sqlCmd);
            s.execute(sqlCmd);
            con.close();
            System.out.println("user '" + user.getUsername() + "' successfully ADDED to the database.");
            return true;
        } catch (Exception ex) {
            System.out.println("couldn't add user '" + user.getUsername() + "' to the database.");
            return false;
        }
    }

    public boolean deleteUser(String username) {
        Connection con = connectDb();
        try {
            String sqlCmd = String.format("DELETE FROM `users` WHERE `username`='%s';", username);
            Statement s = con.prepareStatement(sqlCmd);
            s.execute(sqlCmd);
            con.close();
            System.out.println("user '" + username + "' successfully DELETED to the database.");
            return true;
        } catch (Exception ex) {
            System.out.println("couldn't delete user '" + username + "' from the database.");
            return false;
        }
    }

//    private boolean changeUserInfo(User user) {
//        Connection con = connectDb();
//        try {
//                String sqlCmd = String.format("UPDATE `users` SET `username`='%s' `fullName`='%s' `password`='%s' `mobileNumber`='%s' WHERE `username`='%s';"
//                    , user.getUsername(), user.getPassword(), user.getFullName(), user.getMobileNumber(), user.getUsername());
//            Statement s = con.prepareStatement(sqlCmd);
//            s.execute(sqlCmd);
//            con.close();
//            System.out.println("info of user '"+user.getUsername()+"' CHANGED successfully.");
//            return true;
//        } catch (Exception e) {
//            System.out.println("couldn't change info of user '"+user.getUsername()+"' from database.");
//            return false;
//        }
//    }


    public ArrayList<FollowRequest> getFollowRequestsFollowersOf(String username) {
        ArrayList<FollowRequest> list = new ArrayList<>();
        Connection con = connectDb();
        try {
            PreparedStatement ps = con.prepareStatement(String.format("SELECT * FROM `follow_request` WHERE `followingUsername`='%s'", username));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new FollowRequest(
                    findUser(rs.getString("followerUsername")),
                    findUser(rs.getString("followingUsername")),
                    rs.getString("time"))
                );
            }
        } catch (Exception e) {
            System.out.println("trying to load follow requests failed");
            System.out.println(e.getMessage());
        }
        return list;
    }


    public boolean updateUserInfo(User user, String bio, String absolutePath, String date) {
        Connection con = connectDb();
        try {
            String sqlCmd = String.format("UPDATE users SET username='%s', password='%s', fullName='%s', mobileNumber='%s', pageState='%s' WHERE username='%s'"
                    , user.getUsername(), user.getPassword(), user.getFullName(), user.getMobileNumber(), user.getPageState(), user.getUsername());
            PreparedStatement pstmt = con.prepareStatement(sqlCmd);
            pstmt.execute(sqlCmd);

            String sqlCmd1 = String.format("DELETE FROM user_info WHERE username='%s'", user.getUsername());
            PreparedStatement pstmt1 = con.prepareStatement(sqlCmd1);
            pstmt1.execute(sqlCmd1);

            String sqlCmd2 = String.format("INSERT INTO user_info (username, bio, image, birthday) VALUES ('%s', '%s', '%s', '%s')"
                    , user.getUsername(), bio, absolutePath, date);

            PreparedStatement pstmt2 = con.prepareStatement(sqlCmd2);
//            pstmt2.setString(user.getUsername(), "usernmae");
//            pstmt2.setString(2, bio);
////            File file = new File(absolutePath);
////            InputStream in = new FileInputStream(file);
////            pstmt2.setBinaryStream(3, in, (int)file.length());
//            pstmt2.setString(3, absolutePath);
//            pstmt2.setString(4, date);
//            pstmt2.setString(5, pageState);
            pstmt2.execute();
            con.close();
            System.out.println("info of user '"+user.getUsername()+"' CHANGED successfully.");
            return true;
        } catch (Exception e) {
            System.out.println("couldn't change info of user '"+user.getUsername()+"' from database.");
            System.out.println(e.getMessage());
            return false;
        }
    }

    public User findUser(String username) {
        Connection con = connectDb();
        try {
            String sqlCmd = String.format("SELECT * FROM `users` WHERE `username`='%s';"
                    , username);
            Statement s = con.prepareStatement(sqlCmd);
            ResultSet rs = s.executeQuery(sqlCmd);

            User user = new User();
            while (rs.next()) {
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFullName((rs.getString("fullName")));
                user.setMobileNumber(rs.getString("mobileNumber"));
                user.setPageState(rs.getString("pageState"));
            }
            // check if this user did not exist before, we don't go for more information
            if (user.getUsername()==null) {
                return new User();
            }
            // secondary info setup
            sqlCmd = String.format("SELECT * FROM user_info WHERE `username`='%s'", user.getUsername());
            Statement ps = con.createStatement();
            rs = ps.executeQuery(sqlCmd);
//            int i = 0;
            while(rs.next()) {
                user.setBio(rs.getString("bio"));
                user.setBirthday(rs.getString("birthday"));
                user.setProfImagePath(rs.getString("image"));
                user.setBirthday(rs.getString("birthday"));
//                InputStream input = rs.getBinaryStream("image");
//                String name = "system_files/system_images/"+user.getUsername()+"_"+i+".jpg";
//                new File(name).mkdirs();
//                OutputStream output = new FileOutputStream(name);
//                i++;
//                int b = 0;
//                while((b = input.read()) > -1) {
//                    output.write(b);
//                }
//                user.setProfImage(new Image(new FileInputStream(name)));
//                output.close();
//                input.close();
            }
            con.close();
            System.out.println("Info of User '"+user.getUsername()+"' successfully retrieved from database.");
            return user;
        } catch (Exception ex) {
            System.out.println("Couldn't load info of User '"+username+"' from database.");
            System.out.println(ex.getMessage());
            return new User();
        }
    }

    public boolean addPost(Post post) {
        Connection con = connectDb();
        try {
            String sqlCmd = String.format("INSERT INTO `posts` (`username`, `content`, `score`, `image`) VALUES('%s', '%s', '%s', '%s')"
                    , post.getUser().getUsername(), post.getContent(), post.getScore(), post.getImagePath());
            System.out.println(sqlCmd);
            PreparedStatement s = con.prepareStatement(sqlCmd);
//            s.setString(1, post.getUser().getUsername());
//            s.setString(2, post.getContent());
//            s.setString(3, Integer.toString(post.getScore()));
//            s.setString(4, post.getImagePath());
            s.execute(sqlCmd);
            con.close();
            System.out.println("post of '" + post.getUser().getUsername() + "' successfully ADDED to the database.");
            return true;
        } catch (Exception ex) {
            System.out.println("couldn't add Post of '" + post.getUser().getUsername() + "' to the database.");
            System.out.println(ex.getMessage());
            return false;
        }
    }
    public boolean editPost(Post post) {
        Connection con = connectDb();
        try {
            String sqlCmd = String.format("UPDATE posts SET content='%s', score='%s', image='%s' WHERE username='%s'"
                    , post.getContent(), post.getScore(), post.getImagePath(), post.getUser().getUsername());
            PreparedStatement pstmt = con.prepareStatement(sqlCmd);
            pstmt.execute(sqlCmd);
            Statement s = con.prepareStatement(sqlCmd);
            s.execute(sqlCmd);
            con.close();
            System.out.println("post of '" + post.getUser().getUsername() + "' successfully EDITED to the database.");
            return true;
        } catch (Exception ex) {
            System.out.println("couldn't edit Post of '" + post.getUser().getUsername() + "' from the database.");
            return false;
        }
        //notify all followers
    }

    public ArrayList<Post> findPosts(String username) {
        ArrayList<Post> list = new ArrayList<>();
        Connection con = connectDb();
        try {
            PreparedStatement ps = con.prepareStatement(String.format("SELECT * FROM posts WHERE username='%s'", username));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Post(
                        rs.getString("content"),
                        rs.getInt("score"),
                        rs.getString("image")
                ));
            }
        } catch (Exception e) {
            System.out.println("trying to load follow requests failed");
            System.out.println(e.getMessage());
        }
        return list;
    }

    public boolean addFollowing(String username, String username1) {
        Connection con = connectDb();
        try {
            String sqlCmd1 = String.format("DELETE FROM follows WHERE followerUsername='%s' AND followingUsername='%s'",
                    username, username1);
            Statement s1 = con.prepareStatement(sqlCmd1);
            s1.execute(sqlCmd1);

            String sqlCmd = String.format("INSERT INTO `follows` (followerUsername, followingUsername) " +
                            "VALUES ('%s', '%s');"
                    , username, username1);
            Statement s = con.prepareStatement(sqlCmd);
            s.execute(sqlCmd);

            con.close();
            System.out.println("user '" + username + "' follows user '"+username1+"'.");
            return true;
        } catch (Exception ex) {
            System.out.println("user '" + username + "' couldn't follow user '"+username1+"'.");
            System.out.println(ex.getMessage());
            return false;
        }
    }


    public ArrayList<String> getFollowers(String username) {
        ArrayList<String> list = new ArrayList<>();
        Connection con = connectDb();
        try {
            PreparedStatement ps = con.prepareStatement(String.format("SELECT * FROM follows WHERE followingUsername='%s'", username));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("followerUsername"));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public ArrayList<String> getFollowings(String username) {
        ArrayList<String> list = new ArrayList<>();
        Connection con = connectDb();
        try {
            PreparedStatement ps = con.prepareStatement(String.format("SELECT * FROM follows WHERE followerUsername='%s'", username));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("followingUsername"));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    public boolean deleteFollowing(String username, String username1) {
        Connection con = connectDb();
        try {
            String sqlCmd1 = String.format("DELETE FROM follows WHERE followerUsername='%s' AND followingUsername='%s'",
                    username, username1);
            Statement s1 = con.prepareStatement(sqlCmd1);
            s1.execute(sqlCmd1);


            con.close();
            System.out.println("user '" + username + "' follows user '"+username1+"'.");
            return true;
        } catch (Exception ex) {
            System.out.println("user '" + username + "' couldn't follow user '"+username1+"'.");
            System.out.println(ex.getMessage());
            return false;
        }
    }

    public boolean addRequest(String username, String username1) {
        Connection con = connectDb();
        try {
            String sqlCmd1 = String.format("DELETE FROM `follow_request` WHERE followerUsername='%s' AND followingUsername='%s'",
                    username, username1);
            Statement s1 = con.prepareStatement(sqlCmd1);
            s1.execute(sqlCmd1);

            String sqlCmd = String.format("INSERT INTO `follow_request` (followerUsername, followingUsername, time) VALUES ('%s', '%s', '%s')"
                    , username, username1, currentTime());
            Statement s = con.prepareStatement(sqlCmd);
            s.execute(sqlCmd);
            con.close();
            System.out.println("request for user '" + username + "' to follow user '"+username1+"' successfully set.");
            return true;
        } catch (Exception ex) {
            System.out.println("setting request for user '" + username + "' to follow user '"+username1+"' failed.");
            System.out.println(ex.getMessage());
            return false;
        }
    }

    public boolean deleteRequest(String username, String username1) {
        Connection con = connectDb();
        try {
            String sqlCmd1 = String.format("DELETE FROM `follow_request` WHERE followerUsername='%s' AND followingUsername='%s'",
                    username, username1);
            Statement s1 = con.prepareStatement(sqlCmd1);
            s1.execute(sqlCmd1);
            return true;
        } catch (Exception e) {
            System.out.println("Unable to delete request");
            System.out.println(e.getMessage());
            return false;
        }
    }

    private static String currentTime() {
        long millis = System.currentTimeMillis();
        java.util.Date date = new java.util.Date(millis);
        return date.toString();
    }

    public boolean acceptRequest(String username, String username1) {
        Connection con = connectDb();
        try {
            addFollowing(username, username1);
            deleteRequest(username, username1);
            System.out.println("request of user '"+ username + "accepted form user '"+ username1);
            return true;
        } catch (Exception e) {
            System.out.println("Couldn't accept request of user '"+username+"' from user '" +username1);
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean rejectRequest(String username, String username1) {
        try {
            deleteRequest(username, username1);
            System.out.println("request of user '"+ username + "rejected form user '"+ username1);
            return true;
        } catch (Exception e) {
            System.out.println("Couldn't reject request of user '"+username+"' from user '" +username1);
            System.out.println(e.getMessage());
            return false;
        }
    }
}

