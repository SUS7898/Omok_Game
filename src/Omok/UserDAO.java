package Omok;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class UserDAO extends DBConnection {

    private Connection conn = null;
    private PreparedStatement pstmt = null;
    private ResultSet rs = null;

    // 사용자 생성 메서드
    public boolean createUser(String name,String id,String pw) {
        conn = getConnection();

        String sql = "INSERT INTO omokdb.users (name, id, pw) VALUES (?, ?, ?)";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, id);
            pstmt.setString(3, pw);

            int result = pstmt.executeUpdate();
            if (result > 0) {
                System.out.println("회원 가입 성공");
            } else {
                System.out.println("회원 가입 실패");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("회원 가입 실패");
        } finally {
            close();
        }
        return true;
    }

    
    // 사용자 로그인 메서드
    public boolean loginUser(String id, String pw) {
        conn = getConnection();

        String sql = "SELECT * FROM omokdb.users WHERE id = ? AND pw = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, pw);
            rs = pstmt.executeQuery();
            return rs.next(); // 존재하면 true, 그렇지 않으면 false
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return false;
    }
    public int updateTotal(String id) {
        conn = getConnection();

        String sql = "UPDATE omokdb.users SET total = total + 1 WHERE id = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            close();
        }
    }
    
    public int updateVictory(String id) {
        conn = getConnection();

        String sql = "UPDATE omokdb.users SET victory = victory + 1 WHERE id = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            close();
        }
    }
    public int updateDefeat(String id) {
        conn = getConnection();

        String sql = "UPDATE omokdb.users SET defeat = defeat + 1 WHERE id = ?";
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            close();
        }
    }
    public String infoUser(String id, String name) {
        conn = getConnection();
        String sql = "SELECT total, victory, defeat FROM omokdb.users WHERE id = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int total = rs.getInt("total");
                int victory = rs.getInt("victory");
                int defeat = rs.getInt("defeat");

                String result = name + "님 - 총 게임 수: " + total + ", 승리한 판: " + victory + ", 패배한 판: " + defeat + "\n";
                return result;
            } else {
                return "해당 사용자 정보를 찾을 수 없습니다.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "사용자 정보 조회 중 오류 발생";
        } finally {
            close();
        }
    }

    // 데이터베이스 연결 및 자원 반환 메서드
    private void close() {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}