package dao;

import bean.Message;
import util.DBUtil;
import util.DateUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    // 添加聊天记录
    public void addMessage(Message message) {
        String sql = "INSERT INTO message (user_id, username, content, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection(); // 使用 DBUtil 获取连接
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, message.getUserId());
            ps.setString(2, message.getUsername());
            ps.setString(3, message.getContent());
            ps.setTimestamp(4, DateUtil.d2t(message.getTimestamp())); // 使用 DateUtil 转换时间
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 获取最新聊天记录
    public List<Message> getRecentMessages(int limit) {
        String sql = "SELECT * FROM message ORDER BY timestamp DESC LIMIT ?";
        List<Message> messages = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection(); // 使用 DBUtil 获取连接
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Message message = new Message();
                    message.setId(rs.getInt("id"));
                    message.setUserId(rs.getInt("user_id"));
                    message.setUsername(rs.getString("username"));
                    message.setContent(rs.getString("content"));
                    message.setTimestamp(DateUtil.t2d(rs.getTimestamp("timestamp"))); // 使用 DateUtil 转换时间
                    messages.add(message);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
}
