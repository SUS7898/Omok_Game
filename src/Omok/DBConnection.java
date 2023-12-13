package Omok;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DBConnection {
	Connection conn = null;
	public Connection getConnection() {
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");

			
				String url = "jdbc:mysql://localhost:3306/omokdb";
				String id = "root";
				String pwd = "1234";
				conn = DriverManager.getConnection(url, id, pwd);
				//System.out.println("연결 성공");
				
		}catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("드라이버 클래스가 없습니다.");
			}
		
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("드라이버클래스가 없습니다.");
		}
		
		
		return conn;
	}
	

	public static void main(String[] args) {
		new DBConnection().getConnection();
	}
	
}
