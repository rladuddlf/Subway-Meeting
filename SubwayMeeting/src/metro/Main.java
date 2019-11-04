package metro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
   static Connection cn=null;
   static DBConnectionMgr db = DBConnectionMgr.getInstance();
   static PreparedStatement pstmt;
   static ResultSet rs;
   static String sql;
   static Scanner scanner = new Scanner(System.in);
   static int num;
   static int[] weight;

   public static void main(String[] args) {
      try {
        cn = db.getConnection();
         setDB();
         count();
         closedStation();
         weight();
         //흔적흔적흔적흔적흔적흔적
         //num=3;
         //menu = new int[num];
         input();
         Meeting meeting = new Meeting(num, weight,cn);
         meeting.findCenter();
         meeting.loop();
         meeting.show();
   } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
   }
      finally {
          db.freeConnection(cn, pstmt, rs);
       }
      // TODO Auto-generated method stub
         return;
   }

   public static void input() {
      String stationCode = "";
      String name = "";
      String line = "";
      String externalCode = "";
      double latitude = 0;
      double longitude = 0;
      
      System.out.println("출발역은 뒤에 ‘역’자를 빼고 단어 사이사이에 공백이 없어야한다.");
      for (int i = 0; i < num; i++) {
         System.out.println(i + 1 + "번째 역 입력 ");
         name = scanner.nextLine();

         // 띄어쓰기 예외처리
         String[] split = name.split(" ");
         if (split.length >= 2) {
            System.out.println("공백이 있습니다. 재입력하세요.");
            i--;
            continue;
         }
         
         //흔적흔적흔적흔적흔적흔적
         // 사용자별 메뉴 입력
         // System.out.println("메뉴 입력");
         // System.out.println("1)최소거리 2)최소환승");
         // menu[i] = scanner.nextInt();
         // if(menu[i] != 1 && menu[i] != 2) {
         // System.out.println("제대로 입력 ㄱㄱ");
         // i--;
         // continue;
         // }
         // meeting[i] = new Meeting(menu);

         sql = "select * from subway_info2 where name='" + name + "'";
         try {
            pstmt = cn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
               stationCode = rs.getString(1);
               name = rs.getString(2);
               line = rs.getString(3);
               externalCode = rs.getString(4);
               latitude = rs.getDouble(5);
               longitude = rs.getDouble(6);
               sql = "insert into departure values(?,?,?,?,?,?)";
               pstmt = cn.prepareStatement(sql);
               pstmt.setString(1, stationCode);
               pstmt.setString(2, name);
               pstmt.setString(3, line);
               pstmt.setString(4, externalCode);
               pstmt.setDouble(5, latitude);
               pstmt.setDouble(6, longitude);
               pstmt.executeUpdate();
            } else {
               System.out.println("해당 역이 없습니다 ");
               i--;
            }
         } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
   }
   
   public static void weight() {
      //가중치 추가 menu()에서 weight로 이름만 바꿈
      int temp = 0;
      int i=0;
      while(i<num) {
         System.out.println((i+1)+"번째 사용자의 환승 가중치를 입력하세요.");
            try{
               temp = scanner.nextInt();
               scanner.nextLine();
             }
             catch(InputMismatchException ime) {
                System.out.println("자연수만 입력하세요");
                System.exit(0);
             }
            if (temp < 1 || temp > 1000) {
                System.out.println("1~1000까지의 숫자를 입력하세요.");
             } else {
                weight[i] = temp;
                i++;
                System.out.println(i + "번째 사용자님의 환승 가중치는 " + temp + "입니다.");
             }
      }
   }

   public static void count() {
      while (true) {
         System.out.println("출발역이 몇 개 입니까 : ");
         try{
           num = scanner.nextInt();
           scanner.nextLine();
          }
          catch(InputMismatchException ime) {
             System.out.println("자연수만 입력하세요");
             System.exit(0);
          } 
         if (num < 3 || num > 5) {
            System.out.println("3~5까지의 숫자를 입력하세요.");
         } else {
            weight = new int[num];
           System.out.println("출발역은 " + num + "개 입니다." );
            break;
         }
      }
   }

   public static void closedStation() {
      int select=0;
      int number=0;
      String closedName="";
      String closedLine="";
      
      while (true) {
         System.out.println("1)폐쇄된 호선 2)폐쇄된 역 3)폐쇄 구간이 없습니다.");
         try{
             select = scanner.nextInt();
             scanner.nextLine();
          }
          catch(InputMismatchException ime) {
             System.out.println("자연수만 입력하세요");
             System.exit(0);
          } 

         if (select == 2) {
            System.out.println(select+"번을 선택하셨습니다.");
            System.out.println("폐쇄역이 몇개인가요?");
            try{
                number = scanner.nextInt();
                scanner.nextLine();
             }
             catch(InputMismatchException ime) {
                System.out.println("자연수만 입력하세요");
                System.exit(0);
             }
            if(number==0)
               break;
            if(number < 0 || number >= 601) {
                System.out.println("0~600개 까지 폐쇄역을 설정할 수 있습니다.");
                continue;
             }
            System.out.println("폐쇄역은 총 " + number + "개 입니다.");
            for (int i = 0; i < number; i++) {
               System.out.println("폐쇄역을 입력해주세요.단 '역'자는 빼주시고 띄어쓰기도 하지말아주세요.");               
               closedName = scanner.nextLine();
               
               sql = "select name from subway_info2 where name='" + closedName + "'";
               try {
                  pstmt = cn.prepareStatement(sql);
                  rs = pstmt.executeQuery();
                  if (!rs.next()) {
                     System.out.println("해당 역이 없습니다.");
                     //System.out.println(closedName + "역이 없습니다.");
                     i--;
                     continue;
                  } else {
                     sql = "delete from subway_info2 where name='" + closedName + "'";
                     try {
                        pstmt = cn.prepareStatement(sql);
                        rs = pstmt.executeQuery();
                        System.out.println("폐쇄합니다.");
                        //System.out.println(closedName + "역을 폐쇄합니다.");
                     } catch (SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                     }
                  }
               } catch (SQLException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
            }
            //break;
            
         } else if (select == 1) {
            System.out.println(select+"번을 선택하셨습니다.");
            System.out.println("폐쇄 호선이 몇개인가요?");
             try{
                 number = scanner.nextInt();
              }
              catch(InputMismatchException ime) {
                 System.out.println("자연수만 입력하세요.");
                 System.exit(0);
              }
             if(number==0)
                break;
             if(number < 0 || number >= 21) {
                System.out.println("0~20개 까지 폐쇄호선을 설정할 수 있습니다.");
                continue;
             }
             System.out.println("폐쇄 호선은 총 "+ number + "개 입니다.");
             for (int i = 0; i < number; i++) {
                System.out.println("폐쇄 호선을 입력해주세요.(1~9, 공항, 분당, 신분당, 에버라인, 경춘, 인천1, 인천2,"
                      + " 경의중앙, 경강, 수인, 의정부, 우이신설)");
                if(closedLine == "") {
                   closedLine = scanner.nextLine();
                }
                closedLine = scanner.nextLine();
                //System.out.println("closedLine = "+closedLine);
                closedLine = closedLine.trim();
                closedLine = closedLine.replaceAll(" ","");
                //System.out.println("closedLine = "+closedLine);
                
                switch(closedLine) {
                case "공항":
                   closedLine="A";
                   break;
                case "분당":
                   closedLine="B";
                   break;
                case "신분당":
                   closedLine="S";
                   break;
                case "에버라인":
                   closedLine="E";
                   break;
                case "경춘":
                   closedLine="G";
                   break;
                case "인천1":
                   closedLine="I";
                   break;
                case "인천2":
                   closedLine="I2";
                   break;
                case "경의중앙":
                   closedLine="K";
                   break;
                case "경강":
                   closedLine="KK";
                   break;
                case "수인":
                   closedLine="SU";
                   break;
                case "의정부":
                   closedLine="U";
                   break;
                case "우이신설":
                   closedLine="UI";
                   break;
                }
                
                sql = "select name from subway_info2 where line='" + closedLine + "'";
                try {
                   pstmt = cn.prepareStatement(sql);
                   rs = pstmt.executeQuery();
                   if (!rs.next()) {
                       System.out.println("해당 호선이 없습니다.");
                      //System.out.println(closedLine + "호선이 없습니다.");
                      i--;
                      continue;
                   } else {
                      sql = "delete from subway_info2 where line='" + closedLine + "'";
                      try {
                         pstmt = cn.prepareStatement(sql);
                         rs = pstmt.executeQuery();
                         //System.out.println("해당 호선을 폐쇄합니다");
                         System.out.println(closedLine + "호선을 폐쇄합니다");
                      } catch (SQLException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                      }
                   }
                } catch (SQLException e) {
                   // TODO Auto-generated catch block
                   e.printStackTrace();
                }
             }
             //break;
         }
         else if(select==3)
            break;
         else {
            System.out.println("1, 2, 3 중 하나를 입력하세요.");
         }
      }
   }

   public static void setDB() {  
         sql = "delete from departure where name is not null";
         try {
            pstmt = cn.prepareStatement(sql);
            rs = pstmt.executeQuery();
         } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         }
         sql = "delete from destination where name is not null";
         try {
            pstmt = cn.prepareStatement(sql);
            rs = pstmt.executeQuery();
         } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         }
         sql = "delete from scope where name is not null";
         try {
            pstmt = cn.prepareStatement(sql);
            rs = pstmt.executeQuery();
         } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         }
         sql = "delete from subway_info2 where name is not null";
         try {
            pstmt = cn.prepareStatement(sql);
            rs = pstmt.executeQuery();
         } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         }
                  
         sql = "insert into subway_info2 select * from subway_info";
            try {
               pstmt = cn.prepareStatement(sql);
               rs = pstmt.executeQuery(sql);
            } catch (SQLException e1) {
               // TODO Auto-generated catch block
               e1.printStackTrace();
            }
      }
   
   
}