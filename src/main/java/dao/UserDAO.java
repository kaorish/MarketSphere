package dao;


import bean.User;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public void setOnlineStatus(int userId, boolean isOnline) {
        String sql = "UPDATE user SET is_online = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isOnline);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<User> getOnlineUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, name FROM user WHERE is_online = TRUE";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }



    private User getUser(ResultSet rs) throws SQLException {
        User bean = new User();
        int id = rs.getInt("id");
        bean.setId(id);
        String name = rs.getString("name");
        bean.setName(name);
        String password = rs.getString("password");
        bean.setPassword(password);
        String salt = rs.getString("salt");
        bean.setSalt(salt);
        String phone = rs.getString("phone");
        bean.setPhone(phone);
        boolean isOnline = rs.getBoolean("is_online");
        bean.setOnline(isOnline);
        return bean;
    }

    public int getTotal() {

        int total = 0;
        try (Connection c = DBUtil.getConnection(); Statement s = c.createStatement()) {

            String sql = "select count(*) from User";

            ResultSet rs = s.executeQuery(sql);
            while (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (SQLException e) {

            e.printStackTrace();
        }
        return total;
    }

    public void add(User bean) {

        String sql = "insert into user values(null ,? ,?, ?, ?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, bean.getName());
            ps.setString(2, bean.getPassword());
            ps.setString(3, bean.getSalt());
            ps.setString(4, bean.getPhone());

            ps.execute();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                bean.setId(id);
            }
        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    public void update(User bean) {

        String sql = "update user set name= ? , password = ?, phone = ? where id = ? ";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, bean.getName());
            ps.setString(2, bean.getPassword());
            ps.setString(3, bean.getPhone());
            ps.setInt(4, bean.getId());

            ps.execute();

        } catch (SQLException e) {

            e.printStackTrace();
        }

    }

    public void delete(int id) {

        try (Connection c = DBUtil.getConnection(); Statement s = c.createStatement()) {

            String sql = "delete from User where id = " + id;

            s.execute(sql);

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }

    // 方法重载，提供多个获取用户的方法
    public User get(int id) {
        User bean = null;

        try (Connection c = DBUtil.getConnection(); Statement s = c.createStatement()) {

            String sql = "select * from User where id = " + id;

            ResultSet rs = s.executeQuery(sql);

            if (rs.next()) {
                bean = getUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bean;
    }

    public User get(String name) {
        User bean = null;

        String sql = "select * from User where name = ?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                bean = getUser(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bean;
    }

    public User get(String name, String password) {
        User bean = null;

        String sql = "select * from User where name = ? and password=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                bean = getUser(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bean;
    }

    public List<User> list() {
        return list(0, Short.MAX_VALUE);
    }

    public List<User> list(int start, int count) {
        List<User> beans = new ArrayList<User>();

        String sql = "select * from User order by id desc limit ?,? ";

        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, start);
            ps.setInt(2, count);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User bean = getUser(rs);
                beans.add(bean);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return beans;
    }

    public boolean isExist(String name) {
        User user = get(name);
        return user != null;
    }
}